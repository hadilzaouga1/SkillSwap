import express from 'express';
import morgan from 'morgan';
import { ApiError, authentifier, lireId, creerClientEchanges, echangeDuParticipant, gestionErreurs } from './core.js';

const REGLES = {
  PENDING:               { vers: 'provider',  type: 'EXCHANGE_REQUEST',  titre: 'Nouvelle demande d\'échange', texte: (t) => `Quelqu'un souhaite suivre « ${t} ». Répondez à la demande.` },
  TIME_PROPOSED:         { vers: 'requester', type: 'EXCHANGE_TIME',     titre: 'Nouveau créneau proposé',     texte: (t) => `Le prestataire propose un autre créneau pour « ${t} ».` },
  ACCEPTED:              { vers: 'requester', type: 'EXCHANGE_ACCEPTED', titre: 'Demande acceptée',            texte: (t) => `Votre demande pour « ${t} » est acceptée. Consultez la fiche de la séance.` },
  REJECTED:              { vers: 'requester', type: 'EXCHANGE_REJECTED', titre: 'Demande refusée',             texte: (t) => `Votre demande pour « ${t} » a été refusée.` },
  AWAITING_CONFIRMATION: { vers: 'requester', type: 'EXCHANGE_DONE',     titre: 'Confirmez la séance',         texte: (t) => `La séance « ${t} » est déclarée terminée : confirmez qu'elle a eu lieu.` },
  COMPLETED:             { vers: 'provider',  type: 'EXCHANGE_COMPLETED',titre: 'Échange terminé',             texte: (t) => `L'échange « ${t} » est confirmé. Pensez à laisser un avis.` },
  CANCELLED:             { vers: 'other',     type: 'EXCHANGE_CANCELLED',titre: 'Échange annulé',              texte: (t) => `L'échange « ${t} » a été annulé.` },
};

export async function migrer(db) {
  if (await db.schema.hasTable('notifications')) return;
  await db.schema.createTable('notifications', (t) => {
    t.increments('id');
    t.integer('user_id').notNullable().index();
    t.string('type', 40).notNullable();
    t.string('title', 120).notNullable();
    t.string('message', 400).notNullable();
    t.string('link', 200).nullable();
    t.integer('exchange_id').nullable();
    t.string('dedupe_key', 120).nullable();
    t.boolean('is_read').notNullable().defaultTo(false);
    t.timestamp('created_at').defaultTo(db.fn.now());
    t.unique(['user_id', 'dedupe_key']);
  });
}

export function creerApp({ db, fetchExchange = creerClientEchanges(), journal = true } = {}) {
  const app = express();
  app.disable('x-powered-by');
  if (journal) app.use(morgan('tiny'));
  app.use(express.json({ limit: '50kb' }));
  app.get('/health', (_q, res) => res.json({ status: 'UP', service: 'notification-service' }));

  // ======================================================================
  // AJOUT : Route interne appelée par exchange-service pour les rappels
  // ======================================================================
  app.post('/internal/notifications/reminder', async (req, res) => {
    const { exchangeId, requesterId, providerId, skillTitle, meetingLink } = req.body;
    
    try {
      await db('notifications').insert([
        {
          user_id: requesterId, type: 'MEETING_REMINDER', title: 'Rappel de réunion',
          message: `Votre séance pour « ${skillTitle} » commence dans 10 minutes.`,
          link: meetingLink, exchange_id: exchangeId, dedupe_key: `rem-${exchangeId}-req`
        },
        {
          user_id: providerId, type: 'MEETING_REMINDER', title: 'Rappel de réunion',
          message: `Votre séance pour « ${skillTitle} » commence dans 10 minutes.`,
          link: meetingLink, exchange_id: exchangeId, dedupe_key: `rem-${exchangeId}-prov`
        }
      ]);
      res.status(201).json({ created: true });
    } catch (err) {
      if (/unique/i.test(String(err.message))) return res.json({ created: false }); // Déjà envoyé
      console.error("Erreur rappel:", err);
      res.status(500).json({ error: err.message });
    }
  });

  app.use('/api/notifications', authentifier);

  const vue = (n) => ({ id: n.id, type: n.type, title: n.title, message: n.message, link: n.link,
    exchangeId: n.exchange_id, read: !!n.is_read, createdAt: n.created_at });
  const nonLues = async (userId) => Number((await db('notifications').where({ user_id: userId, is_read: false }).count({ n: '*' }).first()).n);

  app.get('/api/notifications', async (req, res) => {
    const limite = Math.min(Math.max(Number(req.query.limit) || 30, 1), 100);
    const lignes = await db('notifications').where({ user_id: req.user.id }).orderBy('id', 'desc').limit(limite);
    res.json({ unreadCount: await nonLues(req.user.id), items: lignes.map(vue) });
  });

  app.get('/api/notifications/unread-count', async (req, res) => res.json({ unreadCount: await nonLues(req.user.id) }));

  app.patch('/api/notifications/read-all', async (req, res) => {
    await db('notifications').where({ user_id: req.user.id, is_read: false }).update({ is_read: true });
    res.json({ unreadCount: 0 });
  });

  app.patch('/api/notifications/:id/read', async (req, res) => {
    const id = lireId(req.params.id);
    const n = await db('notifications').where({ id, user_id: req.user.id }).first();
    if (!n) throw new ApiError(404, 'Notification introuvable');
    await db('notifications').where({ id }).update({ is_read: true });
    res.json({ unreadCount: await nonLues(req.user.id) });
  });

  app.delete('/api/notifications/:id', async (req, res) => {
    const id = lireId(req.params.id);
    const n = await db('notifications').where({ id, user_id: req.user.id }).del();
    if (!n) throw new ApiError(404, 'Notification introuvable');
    res.status(204).end();
  });

  async function creer(userId, regle, e, lien, cle) {
    try {
      await db('notifications').insert({
        user_id: userId, type: regle.type, title: regle.titre, message: regle.texte(e.skillTitle || 'votre échange'),
        link: lien, exchange_id: e.id, dedupe_key: cle,
      });
      return true;
    } catch (err) {
      if (/unique/i.test(String(err.message))) return false;
      throw err;
    }
  }

  app.post('/api/notifications/exchange/:exchangeId/sync', async (req, res) => {
    const id = lireId(req.params.exchangeId, "Identifiant d'échange");
    const e = await echangeDuParticipant(fetchExchange, id, req.user);
    const regle = REGLES[e.status];
    if (!regle) return res.json({ created: false });
    const autre = e.providerId === req.user.id ? e.requesterId : e.providerId;
    const cible = regle.vers === 'provider' ? e.providerId : regle.vers === 'requester' ? e.requesterId : autre;
    if (cible === req.user.id) return res.json({ created: false }); // on ne se notifie pas soi-même
    const created = await creer(cible, regle, e, `/echanges?onglet=${cible === e.providerId ? 'recues' : 'envoyees'}`, `ex-${id}-${e.status}`);
    res.status(created ? 201 : 200).json({ created });
  });

  app.post('/api/notifications/exchange/:exchangeId/session-updated', async (req, res) => {
    const id = lireId(req.params.exchangeId, "Identifiant d'échange");
    const e = await echangeDuParticipant(fetchExchange, id, req.user);
    const cible = e.providerId === req.user.id ? e.requesterId : e.providerId;
    const deja = await db('notifications').where({ user_id: cible, exchange_id: id, type: 'SESSION_UPDATED', is_read: false }).first();
    if (deja) return res.json({ created: false });
    await db('notifications').insert({
      user_id: cible, type: 'SESSION_UPDATED', title: 'Fiche de séance mise à jour',
      message: `La fiche ou les supports de « ${e.skillTitle || 'la séance'} » ont changé.`,
      link: `/echanges/${id}/seance`, exchange_id: id, dedupe_key: `sess-${id}-${Date.now()}`,
    });
    res.status(201).json({ created: true });
  });

  app.use((_q, _r, next) => next(new ApiError(404, 'Route introuvable')));
  app.use(gestionErreurs);
  return app;
}