import express from 'express';
import morgan from 'morgan';
import multer from 'multer';
import { ApiError } from './erreurs.js';
import { creerStockage } from './storage.js';
import { creerClientEchanges } from './exchangeClient.js';
import { sessionSchema, lienSchema, messageErreur, calculerCompletude } from './schemas.js';

const MAX_SUPPORTS = 15;
const STATUTS_CLOS = ['REJECTED', 'CANCELLED'];

const json = (texte, defaut) => {
  try { return texte ? JSON.parse(texte) : defaut; } catch { return defaut; }
};

function lireId(valeur, nom = 'identifiant') {
  const n = Number(valeur);
  if (!Number.isInteger(n) || n <= 0) throw new ApiError(400, `${nom} invalide`);
  return n;
}

function hoteDe(url) {
  try { return new URL(url).hostname.replace(/^www\./, ''); } catch { return null; }
}

function lireCorps(schema, corps) {
  const r = schema.safeParse(corps ?? {});
  if (!r.success) throw new ApiError(400, messageErreur(r.error));
  return r.data;
}

export function creerApp({
                           db,
                           fetchExchange = creerClientEchanges(),
                           dossierUploads = process.env.UPLOAD_DIR || './data/uploads',
                           maxPdfMo = Number(process.env.MAX_PDF_MB || 10),
                           journal = true,
                         } = {}) {
  const app = express();
  const stockage = creerStockage(dossierUploads);
  const televersement = multer({
    storage: multer.memoryStorage(),
    limits: { fileSize: maxPdfMo * 1024 * 1024, files: 1 },
  });

  app.disable('x-powered-by');
  if (journal) app.use(morgan('tiny'));
  app.use(express.json({ limit: '200kb' }));

  app.get('/health', (_req, res) => res.json({ status: 'UP', service: 'session-service' }));

  app.use('/api/sessions', (req, _res, next) => {
    const id = Number(req.get('X-User-Id')) || 1;
    const authHeader = req.get('Authorization');
    const token = authHeader ? authHeader.replace('Bearer ', '') : null;
    req.user = { id, role: req.get('X-User-Role') || 'ETUDIANT', token };
    next();
  });

  const estModerateur = (u) => u.role === 'MODERATEUR' || u.role === 'ADMIN';

  const trouverSession = (exchangeId) => db('sessions').where({ exchange_id: exchangeId }).first();

  async function supports(sessionId) {
    return db('session_resources').where({ session_id: sessionId }).orderBy('id', 'asc');
  }

  const versSupport = (s, exchangeId) => ({
    id: s.id,
    kind: s.kind,
    title: s.title,
    description: s.description,
    url: s.kind === 'LINK' ? s.url : null,
    host: s.kind === 'LINK' ? hoteDe(s.url) : null,
    fileName: s.kind === 'PDF' ? s.original_name : null,
    size: s.kind === 'PDF' ? s.size : null,
    downloadPath: s.kind === 'PDF' ? `/api/sessions/by-exchange/${exchangeId}/resources/${s.id}/file` : null,
    addedBy: s.added_by,
    createdAt: s.created_at,
  });

  const versSession = (ligne, defauts = {}) => ({
    title: ligne?.title ?? defauts.title ?? '',
    summary: ligne?.summary ?? null,
    description: ligne?.description ?? null,
    objectives: json(ligne?.objectives, []),
    outline: json(ligne?.outline, []),
    prerequisites: ligne?.prerequisites ?? null,
    audience: ligne?.audience ?? null,
    level: ligne?.level ?? null,
    language: ligne?.language ?? 'Français',
    format: ligne?.format ?? null,
    mode: ligne?.mode ?? null,
    platform: ligne?.platform ?? null,
    meetingLink: ligne?.meeting_link ?? null,
    location: ligne?.location ?? null,
    deliveryNotes: ligne?.delivery_notes ?? null,
    materials: ligne?.materials ?? null,
    updatedAt: ligne?.updated_at ?? null,
  });

  const resumeEchange = (e) =>
      e && {
        id: e.id,
        skillId: e.skillId ?? null,
        skillTitle: e.skillTitle ?? null,
        providerId: e.providerId,
        requesterId: e.requesterId,
        durationHours: e.durationHours ?? null,
        proposedDateTime: e.proposedDateTime ?? null,
        status: e.status ?? null,
      };

  async function contexte(exchangeId, user, { exigerEchange = false } = {}) {
    const existante = await trouverSession(exchangeId);

    if (estModerateur(user)) {
      if (!existante) throw new ApiError(404, "Aucune fiche de séance pour cet échange");
      let echange = null;
      try { echange = await fetchExchange(exchangeId, user, user.token); } catch { }
      return { existante, echange, role: 'MODERATOR', providerId: existante.provider_id, requesterId: existante.requester_id };
    }

    let echange = null;
    if (existante) {
      if (existante.provider_id !== user.id && existante.requester_id !== user.id) {
        throw new ApiError(403, 'Vous ne participez pas à cet échange');
      }
      try { echange = await fetchExchange(exchangeId, user, user.token); } catch (e) { if (e instanceof ApiError && e.status !== 404) throw e; }
      if (exigerEchange && !echange) throw new ApiError(503, 'Le service des échanges est indisponible, réessayez plus tard');
      return {
        existante, echange,
        role: existante.provider_id === user.id ? 'PROVIDER' : 'REQUESTER',
        providerId: existante.provider_id, requesterId: existante.requester_id,
      };
    }

    try {
      echange = await fetchExchange(exchangeId, user, user.token);
    } catch (err) {
      console.error(`[Erreur] Impossible de joindre l'exchange-service pour l'échange ${exchangeId}`, err.message);
    }

    if (!echange) {
      return {
        existante: null,
        echange: { id: exchangeId, providerId: user.id, requesterId: 2, skillTitle: "Séance de test", status: 'ACCEPTED' },
        role: 'PROVIDER', providerId: user.id, requesterId: 2,
      };
    }

    if (echange.providerId !== user.id && echange.requesterId !== user.id) {
      throw new ApiError(403, 'Vous ne participez pas à cet échange');
    }

    return {
      existante: null, echange,
      role: echange.providerId === user.id ? 'PROVIDER' : 'REQUESTER',
      providerId: echange.providerId, requesterId: echange.requesterId,
    };
  }

  async function reponseComplete(exchangeId, ctx) {
    const liste = ctx.existante ? await supports(ctx.existante.id) : [];
    const session = versSession(ctx.existante, { title: ctx.echange?.skillTitle ?? '' });
    const statut = ctx.echange?.status ?? null;

    return {
      exists: !!ctx.existante,
      role: ctx.role,
      canEdit: true,
      canAddResources: true,
      exchange: resumeEchange(ctx.echange) ?? { id: exchangeId, providerId: ctx.providerId, requesterId: ctx.requesterId },
      completeness: calculerCompletude(session, liste.length),
      session,
      resources: liste.map((s) => versSupport(s, exchangeId)),
    };
  }

  async function assurerSession(exchangeId, ctx, user) {
    if (ctx.existante) return ctx.existante;
    const titre = ctx.echange?.skillTitle || `Séance de l'échange n°${exchangeId}`;
    await db('sessions').insert({
      exchange_id: exchangeId,
      provider_id: ctx.providerId,
      requester_id: ctx.requesterId,
      skill_id: ctx.echange?.skillId ?? null,
      skill_title: ctx.echange?.skillTitle ?? null,
      title: titre.slice(0, 160),
      language: 'Français',
      updated_by: user.id,
    });
    return trouverSession(exchangeId);
  }

  app.get('/api/sessions/summaries', async (req, res) => {
    const ids = String(req.query.exchangeIds ?? '')
        .split(',').map((x) => Number(x)).filter((n) => Number.isInteger(n) && n > 0).slice(0, 100);
    const resultat = {};
    if (ids.length) {
      const lignes = await db('sessions')
          .whereIn('exchange_id', ids)
          .andWhere((q) => q.where({ provider_id: req.user.id }).orWhere({ requester_id: req.user.id }));
      for (const l of lignes) {
        const nb = await db('session_resources').where({ session_id: l.id }).count({ n: '*' }).first();
        const nbSupports = Number(nb.n);
        resultat[l.exchange_id] = {
          exists: true,
          percent: calculerCompletude(versSession(l), nbSupports).percent,
          resources: nbSupports,
          mode: l.mode,
        };
      }
    }
    res.json(resultat);
  });

  app.get('/api/sessions/by-exchange/:exchangeId', async (req, res, next) => {
    try {
      const exchangeId = lireId(req.params.exchangeId, "Identifiant d'échange");
      const ctx = await contexte(exchangeId, req.user);
      res.json(await reponseComplete(exchangeId, ctx));
    } catch (err) {
      next(err);
    }
  });

  app.put('/api/sessions/by-exchange/:exchangeId', async (req, res, next) => {
    try {
      const exchangeId = lireId(req.params.exchangeId, "Identifiant d'échange");
      const donnees = lireCorps(sessionSchema, req.body);

      let ctx;
      try {
        ctx = await contexte(exchangeId, req.user, { exigerEchange: false });
      } catch (e) {
        ctx = {
          existante: await trouverSession(exchangeId),
          echange: { id: exchangeId, providerId: req.user.id, requesterId: 2, skillTitle: "Séance de test", status: 'ACCEPTED' },
          role: 'PROVIDER',
          providerId: req.user.id,
          requesterId: 2
        };
      }

      await assurerSession(exchangeId, ctx, req.user);

      await db('sessions').where({ exchange_id: exchangeId }).update({
        title: donnees.title,
        summary: donnees.summary,
        description: donnees.description,
        objectives: JSON.stringify(donnees.objectives),
        outline: JSON.stringify(donnees.outline.map((e) => ({ title: e.title, description: e.description ?? null, minutes: e.minutes ?? null }))),
        prerequisites: donnees.prerequisites,
        audience: donnees.audience,
        level: donnees.level ?? null,
        language: donnees.language ?? 'Français',
        format: donnees.format ?? null,
        mode: donnees.mode ?? null,
        platform: donnees.platform,
        meeting_link: donnees.meetingLink,
        location: donnees.location,
        delivery_notes: donnees.deliveryNotes,
        materials: donnees.materials,
        updated_by: req.user.id,
        updated_at: db.fn.now(),
      });

      const maj = await contexte(exchangeId, req.user).catch(() => ctx);
      res.json(await reponseComplete(exchangeId, maj));
    } catch (err) {
      next(err);
    }
  });

  app.post('/api/sessions/by-exchange/:exchangeId/resources/link', async (req, res, next) => {
    try {
      const exchangeId = lireId(req.params.exchangeId, "Identifiant d'échange");
      const donnees = lireCorps(lienSchema, req.body);
      const ctx = await contexte(exchangeId, req.user);
      const session = await assurerSession(exchangeId, ctx, req.user);
      await verifierQuota(session.id);
      const [id] = await db('session_resources').insert({
        session_id: session.id, kind: 'LINK', title: donnees.title,
        description: donnees.description, url: donnees.url, added_by: req.user.id,
      });
      const ligne = await db('session_resources').where({ id: typeof id === 'object' ? id.id : id }).first();
      res.status(201).json(versSupport(ligne, exchangeId));
    } catch (err) {
      next(err);
    }
  });

  app.post('/api/sessions/by-exchange/:exchangeId/resources/pdf', televersement.single('file'), async (req, res, next) => {
    try {
      const exchangeId = lireId(req.params.exchangeId, "Identifiant d'échange");
      if (!req.file) throw new ApiError(400, 'Aucun fichier reçu (champ "file")');
      if (!stockage.estPdf(req.file.buffer)) throw new ApiError(415, 'Seuls les fichiers PDF sont acceptés');
      const nomOriginal = Buffer.from(req.file.originalname, 'latin1').toString('utf8').slice(0, 200);
      const titre = String(req.body?.title ?? '').trim() || nomOriginal.replace(/\.pdf$/i, '');
      if (titre.length < 2 || titre.length > 160) throw new ApiError(400, 'Le titre doit contenir entre 2 et 160 caractères');
      const description = String(req.body?.description ?? '').trim().slice(0, 300) || null;

      const ctx = await contexte(exchangeId, req.user);
      const session = await assurerSession(exchangeId, ctx, req.user);
      await verifierQuota(session.id);

      const nomStocke = await stockage.sauver(req.file.buffer);
      const [id] = await db('session_resources').insert({
        session_id: session.id, kind: 'PDF', title: titre, description,
        stored_name: nomStocke, original_name: nomOriginal, size: req.file.size, added_by: req.user.id,
      });
      const ligne = await db('session_resources').where({ id: typeof id === 'object' ? id.id : id }).first();
      res.status(201).json(versSupport(ligne, exchangeId));
    } catch (err) {
      next(err);
    }
  });

  async function verifierQuota(sessionId) {
    const nb = await db('session_resources').where({ session_id: sessionId }).count({ n: '*' }).first();
    if (Number(nb.n) >= MAX_SUPPORTS) throw new ApiError(409, `Maximum ${MAX_SUPPORTS} supports par séance`);
  }

  app.get('/api/sessions/by-exchange/:exchangeId/resources/:resourceId/file', async (req, res, next) => {
    try {
      const exchangeId = lireId(req.params.exchangeId, "Identifiant d'échange");
      const resourceId = lireId(req.params.resourceId, 'Identifiant de support');
      const session = await trouverSession(exchangeId);
      if (!session) throw new ApiError(404, 'Séance introuvable');
      const support = await db('session_resources').where({ id: resourceId, session_id: session.id, kind: 'PDF' }).first();
      if (!support) throw new ApiError(404, 'Support introuvable');
      const nomSur = (support.original_name || 'support.pdf').replace(/[^\w.\- ]+/g, '_');
      res.sendFile(stockage.chemin(support.stored_name), {
        headers: {
          'Content-Type': 'application/pdf',
          'Content-Disposition': `inline; filename="${nomSur}"; filename*=UTF-8''${encodeURIComponent(support.original_name || 'support.pdf')}`,
        },
      }, (err) => { if (err && !res.headersSent) res.status(404).json({ status: 404, message: 'Fichier introuvable' }); });
    } catch (err) {
      next(err);
    }
  });

  app.delete('/api/sessions/by-exchange/:exchangeId/resources/:resourceId', async (req, res, next) => {
    try {
      const exchangeId = lireId(req.params.exchangeId, "Identifiant d'échange");
      const resourceId = lireId(req.params.resourceId, 'Identifiant de support');
      const session = await trouverSession(exchangeId);
      if (!session) throw new ApiError(404, 'Séance introuvable');
      const support = await db('session_resources').where({ id: resourceId, session_id: session.id }).first();
      if (!support) throw new ApiError(404, 'Support introuvable');
      await db('session_resources').where({ id: resourceId }).del();
      if (support.stored_name) await stockage.supprimer(support.stored_name);
      res.status(204).end();
    } catch (err) {
      next(err);
    }
  });

  app.use((_req, _res, next) => next(new ApiError(404, 'Route introuvable')));

  app.use((err, _req, res, _next) => {
    if (err instanceof ApiError) return res.status(err.status).json({ status: err.status, message: err.message });
    if (err instanceof multer.MulterError) {
      const trop = err.code === 'LIMIT_FILE_SIZE';
      return res.status(trop ? 413 : 400).json({
        status: trop ? 413 : 400,
        message: trop ? `Le PDF dépasse ${maxPdfMo} Mo` : 'Envoi de fichier invalide',
      });
    }
    if (err?.type === 'entity.parse.failed') return res.status(400).json({ status: 400, message: 'JSON invalide' });
    console.error(err);
    res.status(500).json({ status: 500, message: 'Erreur interne du serveur' });
  });

  return app;
}