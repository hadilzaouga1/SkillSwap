import test from 'node:test';
import assert from 'node:assert/strict';
import { creerDb, ApiError } from '../src/core.js';
import { creerApp, migrer, badge } from '../src/app.js';

const echanges = { 1: { id: 1, providerId: 10, requesterId: 20, skillTitle: 'Java', status: 'COMPLETED' },
                   2: { id: 2, providerId: 10, requesterId: 20, skillTitle: 'Python', status: 'ACCEPTED' } };
const fetchExchange = async (id) => { if (!echanges[id]) throw new ApiError(404, 'Échange introuvable'); return echanges[id]; };
const json = { 'Content-Type': 'application/json' };

async function demarrer() {
  const db = creerDb({ fichier: ':memory:' });
  await migrer(db);
  const serveur = await new Promise((r) => { const s = creerApp({ db, fetchExchange, journal: false }).listen(0, () => r(s)); });
  serveur.db = db;
  const base = `http://localhost:${serveur.address().port}`;
  const as = (id) => (p, o = {}) => fetch(base + p, { ...o, headers: { 'X-User-Id': String(id), ...(o.headers || {}) } });
  return { serveur, prest: as(10), dem: as(20), autre: as(30), base };
}
async function arreter(s) { s.closeAllConnections(); await new Promise((r) => s.close(r)); await s.db.destroy(); }
const body = (rating, comment) => ({ method: 'POST', headers: json, body: JSON.stringify({ rating, comment }) });

test('badge selon moyenne et nombre', () => {
  assert.equal(badge(4.8, 3).code, 'EXPERT');
  assert.equal(badge(4.2, 1).code, 'WELL_RATED');
  assert.equal(badge(3, 5), null);
});

test('avis seulement après échange terminé, une seule fois, par un participant', async () => {
  const { serveur, prest, dem, autre } = await demarrer();
  assert.equal((await dem('/api/reviews/exchange/2', body(5, 'x'))).status, 409);   // pas terminé
  assert.equal((await autre('/api/reviews/exchange/1', body(5))).status, 403);       // tiers
  assert.equal((await dem('/api/reviews/exchange/1', body(6))).status, 400);         // note invalide
  const r = await dem('/api/reviews/exchange/1', body(5, 'Excellent cours'));
  assert.equal(r.status, 201);
  assert.equal((await dem('/api/reviews/exchange/1', body(4))).status, 409);         // déjà fait
  assert.equal((await prest('/api/reviews/exchange/1', body(4, 'Étudiant sérieux'))).status, 201);
  await arreter(serveur);
});

test("l'avis reçu n'est visible qu'après avoir donné le sien ; réputation publique", async () => {
  const { serveur, prest, dem } = await demarrer();
  await dem('/api/reviews/exchange/1', body(5, 'Top'));
  let b = await (await prest('/api/reviews/exchange/1')).json();
  assert.equal(b.canReview, true);
  assert.equal(b.theirs, null);                                  // je n'ai pas encore noté
  await prest('/api/reviews/exchange/1', body(3));
  b = await (await prest('/api/reviews/exchange/1')).json();
  assert.equal(b.canReview, false);
  assert.equal(b.theirs.rating, 5);

  const rep = await (await dem('/api/reviews/user/10')).json();   // le prestataire a reçu un 5
  assert.equal(rep.count, 1);
  assert.equal(rep.average, 5);
  assert.equal(rep.badge.code, 'WELL_RATED');
  assert.equal(rep.reviews[0].reviewerId, undefined);             // anonyme côté public
  const sum = await (await dem('/api/reviews/summary?userIds=10,20,x')).json();
  assert.equal(sum[20].count, 1);
  await arreter(serveur);
});
