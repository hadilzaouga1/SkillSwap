// ============================================================
//  ai-service : l'agent IA de SkillSwap, utilisé par TOUT le projet (web + mobile)
//   - POST /api/ai/chat            assistant conversationnel + recommandation de compétences
//   - POST /api/ai/session-draft   brouillon complet de fiche de séance
//   - POST /api/ai/skill-draft     description d'une compétence à partir de son titre
//   - POST /api/ai/improve-text    reformulation + conseils (description, demande, avis...)
//  Avec ANTHROPIC_API_KEY -> Claude ; sans clé (ou en cas de panne) -> mode local déterministe.
// ============================================================
import express from 'express';
import morgan from 'morgan';
import { z } from 'zod';
import { ApiError, authentifier, valider, gestionErreurs } from './core.js';
import { SYSTEM_PROMPT } from './knowledge.js';
import { creerFournisseur } from './providers.js';
import { repondreLocal, brouillonSeance, ameliorerLocal, brouillonCompetence, motsCles, suggestionsPour } from './local.js';

const chatSchema = z.object({
  message: z.string().trim().min(1, 'Message vide').max(1000, '1000 caractères maximum'),
  history: z.array(z.object({ role: z.enum(['user', 'assistant']), content: z.string().max(2000) })).max(20).nullish().transform((v) => v ?? []),
  context: z.object({ page: z.string().max(100).nullish() }).nullish().transform((v) => v ?? {}),
});
const draftSeanceSchema = z.object({
  skillTitle: z.string().trim().min(2, 'Titre de la compétence requis').max(160),
  level: z.enum(['DEBUTANT', 'INTERMEDIAIRE', 'AVANCE']).nullish().transform((v) => v ?? null),
  durationHours: z.number().min(0.5).max(12).nullish().transform((v) => v ?? 1),
  mode: z.enum(['ONLINE', 'IN_PERSON', 'HYBRID']).nullish().transform((v) => v ?? null),
  extra: z.string().trim().max(500).nullish().transform((v) => v || null),
});
const skillDraftSchema = z.object({
  titre: z.string().trim().min(2, 'Titre requis').max(120),
  niveau: z.enum(['DEBUTANT', 'INTERMEDIAIRE', 'AVANCE']).nullish().transform((v) => v ?? null),
});
const improveSchema = z.object({
  text: z.string().trim().min(3, 'Texte trop court').max(4000),
  kind: z.enum(['description', 'skill', 'request', 'review']).nullish().transform((v) => v ?? 'description'),
});

/** Validation tolérante du brouillon renvoyé par le LLM : on borne tout, sinon on retombe sur le mode local. */
const sortieSeance = z.object({
  title: z.string().min(3).max(160), summary: z.string().max(300).nullish(), description: z.string().max(4000),
  objectives: z.array(z.string().max(200)).max(10), 
  outline: z.array(z.object({ title: z.string().min(1).max(120), minutes: z.number().int().min(5).max(480) })).max(12),
  prerequisites: z.string().max(500).nullish(), audience: z.string().max(300).nullish(),
  level: z.enum(['DEBUTANT', 'INTERMEDIAIRE', 'AVANCE']).nullish(), format: z.enum(['COURS', 'ATELIER', 'TUTORAT', 'REVISION', 'PROJET']).nullish(),
  mode: z.enum(['ONLINE', 'IN_PERSON', 'HYBRID']).nullish(), deliveryNotes: z.string().max(2000).nullish(), materials: z.string().max(800).nullish(),
});

function extraireJson(texte) {
  const debut = texte.indexOf('{'); const fin = texte.lastIndexOf('}');
  if (debut < 0 || fin <= debut) throw new Error('pas de JSON');
  return JSON.parse(texte.slice(debut, fin + 1));
}

export function creerApp({
  fournisseur = creerFournisseur(),
  skillServiceUrl = process.env.SKILL_SERVICE_URL || 'http://localhost:8084',
  fetchImpl = fetch,
  limiteParMinute = Number(process.env.AI_RATE_LIMIT || 30),
  journal = true,
} = {}) {
  const app = express();
  app.disable('x-powered-by');
  if (journal) app.use(morgan('tiny'));
  app.use(express.json({ limit: '50kb' }));
  app.get('/health', (_q, res) => res.json({ status: 'UP', service: 'ai-service', provider: fournisseur?.name ?? 'local' }));
  app.use('/api/ai', authentifier);

  // Limite simple par utilisateur (protège le quota du LLM)
  const appels = new Map();
  app.use('/api/ai', (req, _res, next) => {
    const maintenant = Date.now();
    const l = (appels.get(req.user.id) || []).filter((t) => maintenant - t < 60000);
    if (l.length >= limiteParMinute) return next(new ApiError(429, 'Trop de demandes : réessaie dans une minute'));
    l.push(maintenant); appels.set(req.user.id, l);
    next();
  });

  /** Cherche dans le catalogue (skill-service, via les mêmes en-têtes que la Gateway). */
  async function chercherCompetences(message, user) {
    const mots = motsCles(message);
    if (!mots.length) return [];
    for (const mot of mots) {
      try {
        const r = await fetchImpl(`${skillServiceUrl}/api/competences?motCle=${encodeURIComponent(mot)}`, {
          headers: { 'X-User-Id': String(user.id), 'X-User-Role': user.role }, signal: AbortSignal.timeout(3000) });
        if (!r.ok) continue;
        const l = await r.json();
        if (Array.isArray(l) && l.length) {
          return l.filter((c) => !c.statutValidation || c.statutValidation === 'VALIDEE').slice(0, 5)
            .map((c) => ({ id: c.id, titre: c.titre, niveau: c.niveau, categorie: c.categorie?.nom ?? null, etudiantId: c.etudiantId ?? null }));
        }
      } catch { /* catalogue indisponible : on répond sans */ }
    }
    return [];
  }

  app.get('/api/ai/status', (_q, res) => res.json({ provider: fournisseur?.name ?? 'local', model: fournisseur?.model ?? null }));

  app.post('/api/ai/chat', async (req, res) => {
    const d = valider(chatSchema, req.body);
    const wantsSkills = /(apprendre|cherche|trouver|veux|voudrais|besoin|cours de|aide en|reviser|progresser)/i
      .test(d.message.normalize('NFD').replace(/[̀-ͯ]/g, ''));
    const competences = wantsSkills ? await chercherCompetences(d.message, req.user) : [];

    if (fournisseur) {
      try {
        const catalogue = competences.length ? `\nCompétences du catalogue pertinentes (données) : ${competences.map((c) => `${c.titre} [${c.niveau}]`).join('; ')}` : '';
        const reply = await fournisseur.complete(
          `${SYSTEM_PROMPT}\nPage actuelle de l'utilisateur : ${d.context.page || 'inconnue'}. Rôle : ${req.user.role}.${catalogue}`,
          [...d.history.slice(-10), { role: 'user', content: d.message }], { maxTokens: 500 });
        if (reply) return res.json({ reply, suggestions: suggestionsPour(d.context.page).slice(0, 3), skills: competences, provider: fournisseur.name });
      } catch (e) { console.error('LLM indisponible, repli local :', e.message); }
    }
    res.json({ ...repondreLocal(d.message, d.context, competences), provider: 'local' });
  });

  app.post('/api/ai/session-draft', async (req, res) => {
    const d = valider(draftSeanceSchema, req.body);
    if (fournisseur) {
      try {
        const texte = await fournisseur.complete(
          `${SYSTEM_PROMPT}\nTu rédiges des fiches de séance. Réponds UNIQUEMENT par un objet JSON avec exactement les clés : title, summary, description (80 à 600 caractères), objectives (3 à 5 chaînes), outline (liste de {title, minutes} dont la somme vaut environ la durée), prerequisites, audience, level (DEBUTANT|INTERMEDIAIRE|AVANCE), format (COURS|ATELIER|TUTORAT|REVISION|PROJET), mode (ONLINE|IN_PERSON|HYBRID), deliveryNotes, materials. En français.`,
          [{ role: 'user', content: JSON.stringify(d) }], { maxTokens: 1200 });
        const sortie = sortieSeance.parse(extraireJson(texte));
        return res.json({ draft: { ...sortie, language: 'Français' }, provider: fournisseur.name });
      } catch (e) { console.error('Brouillon LLM invalide, repli local :', e.message); }
    }
    res.json({ draft: { ...brouillonSeance(d), language: 'Français' }, provider: 'local' });
  });

  app.post('/api/ai/skill-draft', async (req, res) => {
    const d = valider(skillDraftSchema, req.body);
    if (fournisseur) {
      try {
        const texte = await fournisseur.complete(
          `${SYSTEM_PROMPT}\nRéponds UNIQUEMENT par un JSON {"description": "...(60 à 400 caractères)", "disponibilites": "..."} pour la compétence donnée.`,
          [{ role: 'user', content: JSON.stringify(d) }], { maxTokens: 400 });
        const s = z.object({ description: z.string().min(20).max(1000), disponibilites: z.string().max(200).nullish() }).parse(extraireJson(texte));
        return res.json({ ...s, provider: fournisseur.name });
      } catch (e) { console.error('Brouillon LLM invalide, repli local :', e.message); }
    }
    res.json({ ...brouillonCompetence(d), provider: 'local' });
  });

  app.post('/api/ai/improve-text', async (req, res) => {
    const d = valider(improveSchema, req.body);
    if (fournisseur) {
      try {
        const texte = await fournisseur.complete(
          `${SYSTEM_PROMPT}\nReformule le texte de l'utilisateur en français clair et chaleureux, sans rien inventer ni changer le sens. Réponds uniquement par le texte reformulé.`,
          [{ role: 'user', content: d.text }], { maxTokens: 900 });
        if (texte) return res.json({ text: texte, tips: ameliorerLocal(d.text, d.kind).tips, provider: fournisseur.name });
      } catch (e) { console.error('LLM indisponible, repli local :', e.message); }
    }
    res.json({ ...ameliorerLocal(d.text, d.kind), provider: 'local' });
  });

  app.use((_q, _r, next) => next(new ApiError(404, 'Route introuvable')));
  app.use(gestionErreurs);
  return app;
}
