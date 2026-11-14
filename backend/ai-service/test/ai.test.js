import test from 'node:test';
import assert from 'node:assert/strict';
import { creerApp } from '../src/app.js';
import { creerFournisseur } from '../src/providers.js';
import { motsCles, brouillonSeance, ameliorerLocal } from '../src/local.js';

const json = { 'Content-Type': 'application/json' };
const catalogue = [{ id: 7, titre: 'Java Spring Boot', niveau: 'DEBUTANT', statutValidation: 'VALIDEE', categorie: { nom: 'Informatique' }, etudiantId: 2 },
                   { id: 8, titre: 'Java brouillon', niveau: 'AVANCE', statutValidation: 'EN_ATTENTE' }];
const fetchSkills = async (url) => url.includes('/api/competences') && url.includes('java')
  ? new Response(JSON.stringify(catalogue), { status: 200 }) : new Response('[]', { status: 200 });

async function demarrer(options = {}) {
  const serveur = await new Promise((r) => { const s = creerApp({ journal: false, fournisseur: null, fetchImpl: fetchSkills, ...options }).listen(0, () => r(s)); });
  const base = `http://localhost:${serveur.address().port}`;
  const post = (p, body, id = 1) => fetch(base + p, { method: 'POST', headers: { ...json, 'X-User-Id': String(id) }, body: JSON.stringify(body) });
  return { serveur, post, base };
}
const arreter = async (s) => { s.closeAllConnections(); await new Promise((r) => s.close(r)); };

test('mots-clés extraits sans mots vides', () => {
  assert.deepEqual(motsCles('Je veux apprendre Java et Spring'), ['java', 'spring']);
});

test('401 sans utilisateur', async () => {
  const { serveur, base } = await demarrer();
  assert.equal((await fetch(base + '/api/ai/status')).status, 401);
  await arreter(serveur);
});

test('chat local : FAQ, recommandation validée uniquement, message vide refusé', async () => {
  const { serveur, post } = await demarrer();
  let b = await (await post('/api/ai/chat', { message: 'Comment fonctionnent les crédits ?' })).json();
  assert.equal(b.provider, 'local');
  assert.match(b.reply, /1 heure/);
  b = await (await post('/api/ai/chat', { message: 'Je veux apprendre Java' })).json();
  assert.equal(b.skills.length, 1);                      // la compétence EN_ATTENTE est exclue
  assert.equal(b.skills[0].titre, 'Java Spring Boot');
  b = await (await post('/api/ai/chat', { message: 'je veux apprendre le yodel' })).json();
  assert.deepEqual(b.skills, []);
  assert.equal((await post('/api/ai/chat', { message: '   ' })).status, 400);
  await arreter(serveur);
});

test('brouillon de séance local : durées cohérentes et champs de la fiche', async () => {
  const { serveur, post } = await demarrer();
  const b = await (await post('/api/ai/session-draft', { skillTitle: 'Python', durationHours: 2, level: 'DEBUTANT' })).json();
  const total = b.draft.outline.reduce((s, e) => s + e.minutes, 0);
  assert.ok(Math.abs(total - 120) <= 10, `total=${total}`);
  assert.ok(b.draft.objectives.length >= 3 && b.draft.description.length >= 80);
  assert.equal(b.draft.format, 'ATELIER');
  assert.equal((await post('/api/ai/session-draft', { skillTitle: '' })).status, 400);
  await arreter(serveur);
});

test('amélioration de texte et description de compétence', async () => {
  const { serveur, post } = await demarrer();
  let b = await (await post('/api/ai/improve-text', { text: 'je donne cours de maths ,niveau lycée', kind: 'skill' })).json();
  assert.equal(b.text, 'Je donne cours de maths, niveau lycée.');
  assert.ok(b.tips.length);
  b = await (await post('/api/ai/skill-draft', { titre: 'Guitare', niveau: 'DEBUTANT' })).json();
  assert.match(b.description, /Guitare/);
  assert.ok(ameliorerLocal('ok', 'review').tips.length);
  assert.ok(brouillonSeance({ skillTitle: 'X', durationHours: 3 }).format === 'COURS');
  await arreter(serveur);
});

test('avec un LLM : réponse du modèle, JSON de brouillon validé, repli si JSON invalide', async () => {
  let sortie = 'Bonjour, voici ma réponse.';
  const fournisseur = creerFournisseur({ apiKey: 'test', baseUrl: 'http://llm',
    fetchImpl: async () => new Response(JSON.stringify({ content: [{ type: 'text', text: sortie }] }), { status: 200 }) });
  const { serveur, post } = await demarrer({ fournisseur });
  let b = await (await post('/api/ai/chat', { message: 'Salut' })).json();
  assert.equal(b.provider, 'anthropic');
  assert.equal(b.reply, 'Bonjour, voici ma réponse.');

  sortie = 'Voici : ' + JSON.stringify({ title: 'Docker en 1h', description: 'x'.repeat(100), objectives: ['a'], outline: [{ title: 'Intro', minutes: 30 }] });
  b = await (await post('/api/ai/session-draft', { skillTitle: 'Docker' })).json();
  assert.equal(b.provider, 'anthropic');
  assert.equal(b.draft.title, 'Docker en 1h');

  sortie = 'pas du json du tout';
  b = await (await post('/api/ai/session-draft', { skillTitle: 'Docker' })).json();
  assert.equal(b.provider, 'local');                    // repli automatique
  await arreter(serveur);
});

test('limite de débit par utilisateur', async () => {
  const { serveur, post } = await demarrer({ limiteParMinute: 2 });
  await post('/api/ai/chat', { message: 'a' }); await post('/api/ai/chat', { message: 'b' });
  assert.equal((await post('/api/ai/chat', { message: 'c' })).status, 429);
  assert.equal((await post('/api/ai/chat', { message: 'c' }, 2)).status, 200);   // un autre utilisateur n'est pas bloqué
  await arreter(serveur);
});
