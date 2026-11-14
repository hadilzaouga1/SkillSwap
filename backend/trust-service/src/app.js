import express from 'express';
import morgan from 'morgan';
import { z } from 'zod';
import { ApiError, authentifier, lireId, valider, creerClientEchanges, echangeDuParticipant, gestionErreurs } from './core.js';

const avisSchema = z.object({
  rating: z.number().int('La note doit être un entier').min(1, 'Note minimale : 1').max(5, 'Note maximale : 5'),
  comment: z.string().trim().max(500, '500 caractères maximum').nullish().transform((v) => v || null),
});

export async function migrer(db) {
  if (await db.schema.hasTable('reviews')) return;
  await db.schema.createTable('reviews', (t) => {
    t.increments('id');
    t.integer('exchange_id').notNullable();
    t.integer('reviewer_id').notNullable();
    t.integer('reviewee_id').notNullable().index();
    t.integer('rating').notNullable();
    t.string('comment', 500).nullable();
    t.string('skill_title', 160).nullable();
    t.timestamp('created_at').defaultTo(db.fn.now());
    t.unique(['exchange_id', 'reviewer_id']);
  });
}

export function badge(moyenne, nb) {
  if (nb >= 3 && moyenne >= 4.5) return { code: 'EXPERT', label: 'Expert fiable' };
  if (nb >= 1 && moyenne >= 4) return { code: 'WELL_RATED', label: 'Bien noté' };
  return null;
}

export function creerApp({ db, fetchExchange = creerClientEchanges(), journal = true } = {}) {
  const app = express();
  app.disable('x-powered-by');
  if (journal) app.use(morgan('tiny'));
  app.use(express.json({ limit: '50kb' }));
  app.get('/health', (_q, res) => res.json({ status: 'UP', service: 'trust-service' }));
  app.use('/api/reviews', authentifier);

  const vue = (r) => ({ id: r.id, exchangeId: r.exchange_id, reviewerId: r.reviewer_id, revieweeId: r.reviewee_id,
    rating: r.rating, comment: r.comment, skillTitle: r.skill_title, createdAt: r.created_at });

  async function reputation(userId) {
    const l = await db('reviews').where({ reviewee_id: userId });
    const nb = l.length;
    const moyenne = nb ? Math.round((l.reduce((s, r) => s + r.rating, 0) / nb) * 10) / 10 : 0;
    const repartition = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
    l.forEach((r) => { repartition[r.rating] += 1; });
    return { userId, average: moyenne, count: nb, distribution: repartition, badge: badge(moyenne, nb), rows: l };
  }

  app.post('/api/reviews/exchange/:exchangeId', async (req, res) => {
    const id = lireId(req.params.exchangeId, "Identifiant d'échange");
    const d = valider(avisSchema, req.body);
    const e = await echangeDuParticipant(fetchExchange, id, req.user);
    if (e.status !== 'COMPLETED') throw new ApiError(409, 'Vous pourrez donner votre avis une fois l\'échange terminé');
    const cible = e.providerId === req.user.id ? e.requesterId : e.providerId;
    try {
      await db('reviews').insert({ exchange_id: id, reviewer_id: req.user.id, reviewee_id: cible,
        rating: d.rating, comment: d.comment, skill_title: e.skillTitle ?? null });
    } catch (err) {
      if (/unique/i.test(String(err.message))) throw new ApiError(409, 'Vous avez déjà donné votre avis sur cet échange');
      throw err;
    }
    const r = await db('reviews').where({ exchange_id: id, reviewer_id: req.user.id }).first();
    res.status(201).json(vue(r));
  });

  app.get('/api/reviews/exchange/:exchangeId', async (req, res) => {
    const id = lireId(req.params.exchangeId, "Identifiant d'échange");
    const e = await echangeDuParticipant(fetchExchange, id, req.user);
    const mine = await db('reviews').where({ exchange_id: id, reviewer_id: req.user.id }).first();
    const theirs = mine ? await db('reviews').where({ exchange_id: id, reviewee_id: req.user.id }).first() : null;
    res.json({ canReview: e.status === 'COMPLETED' && !mine, status: e.status,
      mine: mine ? vue(mine) : null, theirs: theirs ? vue(theirs) : null });
  });

  app.get('/api/reviews/user/:userId', async (req, res) => {
    const uid = lireId(req.params.userId, "Identifiant d'étudiant");
    const rep = await reputation(uid);
    const { rows, ...resume } = rep;
    res.json({ ...resume, reviews: rows.sort((a, b) => b.id - a.id).slice(0, 20).map((r) => ({ ...vue(r), reviewerId: undefined })) });
  });

  app.get('/api/reviews/summary', async (req, res) => {
    const ids = String(req.query.userIds ?? '').split(',').map(Number).filter((n) => Number.isInteger(n) && n > 0).slice(0, 100);
    const out = {};
    for (const uid of ids) {
      const { rows, distribution, ...resume } = await reputation(uid);
      out[uid] = { average: resume.average, count: resume.count, badge: resume.badge };
    }
    res.json(out);
  });

  app.use((_q, _r, next) => next(new ApiError(404, 'Route introuvable')));
  app.use(gestionErreurs);
  return app;
}
