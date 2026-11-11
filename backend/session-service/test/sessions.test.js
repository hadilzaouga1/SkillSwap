import test from 'node:test';
import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';
import { creerDb, migrer } from '../src/db.js';
import { creerApp } from '../src/app.js';
import { ApiError } from '../src/erreurs.js';

// Faux exchange-service : échange 1 (prestataire=10, demandeur=20, ACCEPTED), 2 (REJECTED)
const ECHANGES = {
  1: { id: 1, providerId: 10, requesterId: 20, skillId: 5, skillTitle: 'Java Spring', durationHours: 2, status: 'ACCEPTED' },
  2: { id: 2, providerId: 10, requesterId: 20, skillTitle: 'Python', status: 'REJECTED' },
};
const fetchExchange = async (id) => {
  if (!ECHANGES[id]) throw new ApiError(404, 'Échange introuvable');
  return ECHANGES[id];
};

async function demarrer() {
  const db = creerDb({ client: 'better-sqlite3', fichier: ':memory:' });
  await migrer(db);
  const dossier = fs.mkdtempSync(path.join(os.tmpdir(), 'sess-'));
  const app = creerApp({ db, fetchExchange, dossierUploads: dossier, journal: false, maxPdfMo: 1 });
  const serveur = await new Promise((r) => { const s = app.listen(0, () => r(s)); });
  serveur.db = db;
  const base = `http://localhost:${serveur.address().port}`;
  const appeler = (id, role = 'ETUDIANT') => (chemin, opts = {}) =>
    fetch(base + chemin, { ...opts, headers: { 'X-User-Id': String(id), 'X-User-Role': role, ...(opts.headers || {}) } });
  return { serveur, prest: appeler(10), dem: appeler(20), autre: appeler(30), modo: appeler(99, 'MODERATEUR'), base };
}

async function arreter(serveur) {
  serveur.closeAllConnections();
  await new Promise((r) => serveur.close(r));
  await serveur.db.destroy();
}

const fiche = {
  title: 'Introduction à Spring Boot',
  summary: 'Créer une API REST en 2 h',
  description: 'Nous construirons ensemble une API REST complète avec Spring Boot, de la création du projet jusqu aux tests.',
  objectives: ['Comprendre les contrôleurs', 'Écrire un test'],
  outline: [{ title: 'Installation', minutes: 20 }, { title: 'Contrôleurs', minutes: 60 }],
  prerequisites: 'Bases de Java',
  audience: 'Étudiants en L2',
  level: 'DEBUTANT', language: 'Français', format: 'ATELIER', mode: 'ONLINE',
  platform: 'Google Meet', meetingLink: 'https://meet.google.com/abc-defg-hij',
  deliveryNotes: 'Partage d écran, exercices en direct', materials: 'IntelliJ',
};
const json = { 'Content-Type': 'application/json' };

test('sans en-tête utilisateur -> 401', async () => {
  const { serveur, base } = await demarrer();
  const r = await fetch(`${base}/api/sessions/by-exchange/1`);
  assert.equal(r.status, 401);
  await arreter(serveur);
});

test('squelette vide, puis prestataire renseigne la fiche et la complétude monte', async () => {
  const { serveur, prest, dem } = await demarrer();
  let r = await prest('/api/sessions/by-exchange/1');
  let b = await r.json();
  assert.equal(b.exists, false);
  assert.equal(b.role, 'PROVIDER');
  assert.equal(b.canEdit, true);
  assert.equal(b.session.title, 'Java Spring');

  r = await prest('/api/sessions/by-exchange/1', { method: 'PUT', headers: json, body: JSON.stringify(fiche) });
  assert.equal(r.status, 200);
  b = await r.json();
  assert.equal(b.exists, true);
  assert.equal(b.session.outline.length, 2);
  assert.ok(b.completeness.percent >= 80, `percent=${b.completeness.percent}`);

  b = await (await dem('/api/sessions/by-exchange/1')).json();
  assert.equal(b.role, 'REQUESTER');
  assert.equal(b.canEdit, false);
  assert.equal(b.session.platform, 'Google Meet');
  await arreter(serveur);
});

test('le demandeur ne peut pas modifier ; un tiers est refusé', async () => {
  const { serveur, dem, autre } = await demarrer();
  let r = await dem('/api/sessions/by-exchange/1', { method: 'PUT', headers: json, body: JSON.stringify(fiche) });
  assert.equal(r.status, 403);
  r = await autre('/api/sessions/by-exchange/1');
  assert.equal(r.status, 403);
  await arreter(serveur);
});

test('validation : titre trop court et lien non http refusés', async () => {
  const { serveur, prest } = await demarrer();
  let r = await prest('/api/sessions/by-exchange/1', { method: 'PUT', headers: json, body: JSON.stringify({ ...fiche, title: 'ab' }) });
  assert.equal(r.status, 400);
  r = await prest('/api/sessions/by-exchange/1/resources/link', {
    method: 'POST', headers: json, body: JSON.stringify({ title: 'Mauvais', url: 'javascript:alert(1)' }),
  });
  assert.equal(r.status, 400);
  await arreter(serveur);
});

test('échange refusé : fiche non modifiable (409)', async () => {
  const { serveur, prest } = await demarrer();
  const r = await prest('/api/sessions/by-exchange/2', { method: 'PUT', headers: json, body: JSON.stringify(fiche) });
  assert.equal(r.status, 409);
  await arreter(serveur);
});

test('liens : ajout par le demandeur, suppression par le prestataire', async () => {
  const { serveur, prest, dem } = await demarrer();
  let r = await dem('/api/sessions/by-exchange/1/resources/link', {
    method: 'POST', headers: json, body: JSON.stringify({ title: 'Doc officielle', url: 'https://docs.spring.io/spring-boot/' }),
  });
  assert.equal(r.status, 201);
  const l = await r.json();
  assert.equal(l.kind, 'LINK');
  assert.equal(l.host, 'docs.spring.io');
  r = await prest(`/api/sessions/by-exchange/1/resources/${l.id}`, { method: 'DELETE' });
  assert.equal(r.status, 204);
  const b = await (await prest('/api/sessions/by-exchange/1')).json();
  assert.equal(b.resources.length, 0);
  await arreter(serveur);
});

function formPdf(contenu, nom = 'cours.pdf') {
  const f = new FormData();
  f.append('file', new Blob([contenu], { type: 'application/pdf' }), nom);
  f.append('title', 'Support du cours');
  return f;
}

test('PDF : envoi, téléchargement authentifié, refus faux PDF et fichier trop gros', async () => {
  const { serveur, prest, dem, autre, modo } = await demarrer();
  const pdf = '%PDF-1.4\n1 0 obj<<>>endobj\ntrailer<<>>\n%%EOF';
  let r = await prest('/api/sessions/by-exchange/1/resources/pdf', { method: 'POST', body: formPdf(pdf) });
  assert.equal(r.status, 201);
  const s = await r.json();
  assert.equal(s.kind, 'PDF');
  assert.equal(s.fileName, 'cours.pdf');

  r = await dem(`/api/sessions/by-exchange/1/resources/${s.id}/file`);
  assert.equal(r.status, 200);
  assert.equal(r.headers.get('content-type'), 'application/pdf');
  assert.equal(r.headers.get('x-content-type-options'), 'nosniff');
  assert.equal(await r.text(), pdf);

  assert.equal((await modo(`/api/sessions/by-exchange/1/resources/${s.id}/file`)).status, 200);
  assert.equal((await autre(`/api/sessions/by-exchange/1/resources/${s.id}/file`)).status, 403);

  r = await prest('/api/sessions/by-exchange/1/resources/pdf', { method: 'POST', body: formPdf('<html>pas un pdf</html>', 'x.pdf') });
  assert.equal(r.status, 415);

  r = await prest('/api/sessions/by-exchange/1/resources/pdf', { method: 'POST', body: formPdf('%PDF-' + 'a'.repeat(1.5 * 1024 * 1024)) });
  assert.equal(r.status, 413);
  await arreter(serveur);
});

test('modérateur en lecture seule ; résumés pour la liste', async () => {
  const { serveur, prest, dem, modo } = await demarrer();
  await prest('/api/sessions/by-exchange/1', { method: 'PUT', headers: json, body: JSON.stringify(fiche) });
  let b = await (await modo('/api/sessions/by-exchange/1')).json();
  assert.equal(b.role, 'MODERATOR');
  assert.equal(b.canEdit, false);
  const r = await modo('/api/sessions/by-exchange/1/resources/link', {
    method: 'POST', headers: json, body: JSON.stringify({ title: 'x y', url: 'https://example.com' }),
  });
  assert.equal(r.status, 403);

  const sum = await (await dem('/api/sessions/summaries?exchangeIds=1,2,abc')).json();
  assert.equal(sum[1].exists, true);
  assert.ok(sum[1].percent > 0);
  assert.equal(sum[2], undefined);
  await arreter(serveur);
});
