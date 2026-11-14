import test from 'node:test';
import assert from 'node:assert/strict';
import { creerDb, ApiError } from '../src/core.js';
import { creerApp, migrer } from '../src/app.js';

const echange = { id: 1, providerId: 10, requesterId: 20, skillTitle: 'Java', status: 'PENDING' };
const fetchExchange = async (id, user) => {
  if (id !== 1) throw new ApiError(404, 'Échange introuvable');
  return { ...echange };
};

async function demarrer() {
  const db = creerDb({ fichier: ':memory:' });
  await migrer(db);
  const serveur = await new Promise((r) => { const s = app_(db).listen(0, () => r(s)); });
  serveur.db = db;
  const base = `http://localhost:${serveur.address().port}`;
  const as = (id) => (p, o = {}) => fetch(base + p, { ...o, headers: { 'X-User-Id': String(id), ...(o.headers || {}) } });
  return { serveur, prest: as(10), dem: as(20), autre: as(30), base };
}
const app_ = (db) => creerApp({ db, fetchExchange: (...a) => fetchExchange(...a), journal: false });
async function arreter(s) { s.closeAllConnections(); await new Promise((r) => s.close(r)); await s.db.destroy(); }

test('401 sans utilisateur', async () => {
  const { serveur, base } = await demarrer();
  assert.equal((await fetch(base + '/api/notifications')).status, 401);
  await arreter(serveur);
});

test('demande -> le prestataire est notifié, idempotent, lecture et compteur', async () => {
  const { serveur, prest, dem } = await demarrer();
  let r = await dem('/api/notifications/exchange/1/sync', { method: 'POST' });
  assert.equal(r.status, 201);
  r = await dem('/api/notifications/exchange/1/sync', { method: 'POST' });
  assert.equal((await r.json()).created, false);        // pas de doublon

  let b = await (await prest('/api/notifications')).json();
  assert.equal(b.unreadCount, 1);
  assert.equal(b.items[0].type, 'EXCHANGE_REQUEST');
  assert.equal((await (await dem('/api/notifications')).json()).items.length, 0);

  r = await prest(`/api/notifications/${b.items[0].id}/read`, { method: 'PATCH' });
  assert.equal((await r.json()).unreadCount, 0);
  await arreter(serveur);
});

test('on ne se notifie pas soi-même ; un tiers est refusé ; échange inconnu 404', async () => {
  const { serveur, prest, autre } = await demarrer();
  // PENDING notifie le prestataire : s'il appelle lui-même, rien n'est créé
  assert.equal((await (await prest('/api/notifications/exchange/1/sync', { method: 'POST' })).json()).created, false);
  // exchange-service (faux) accepte tout le monde ici : la vérification de participation est dans le service
  assert.equal((await autre('/api/notifications/exchange/1/sync', { method: 'POST' })).status, 403);
  assert.equal((await prest('/api/notifications/exchange/9/sync', { method: 'POST' })).status, 404);
  await arreter(serveur);
});

test('statut accepté -> le demandeur est notifié ; fiche mise à jour ; read-all ; suppression', async () => {
  const { serveur, prest, dem } = await demarrer();
  echange.status = 'ACCEPTED';
  assert.equal((await prest('/api/notifications/exchange/1/sync', { method: 'POST' })).status, 201);
  assert.equal((await prest('/api/notifications/exchange/1/session-updated', { method: 'POST' })).status, 201);
  assert.equal((await (await prest('/api/notifications/exchange/1/session-updated', { method: 'POST' })).json()).created, false);
  let b = await (await dem('/api/notifications')).json();
  assert.equal(b.unreadCount, 2);
  assert.equal((await (await dem('/api/notifications/read-all', { method: 'PATCH' })).json()).unreadCount, 0);
  assert.equal((await dem(`/api/notifications/${b.items[0].id}`, { method: 'DELETE' })).status, 204);
  assert.equal((await (await dem('/api/notifications')).json()).items.length, 1);
  echange.status = 'PENDING';
  await arreter(serveur);
});
