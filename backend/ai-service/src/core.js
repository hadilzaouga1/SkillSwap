import knex from 'knex';
import fs from 'node:fs';
import path from 'node:path';

export class ApiError extends Error {
  constructor(status, message) { super(message); this.status = status; }
}

export function creerDb({ client = process.env.DB_CLIENT || 'better-sqlite3', fichier, nom } = {}) {
  if (client === 'mysql2') {
    return knex({
      client: 'mysql2',
      connection: {
        host: process.env.DB_HOST || 'localhost', port: Number(process.env.DB_PORT || 3306),
        user: process.env.DB_USER || 'root', password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || nom,
      },
    });
  }
  const f = fichier ?? `./data/${nom}.sqlite`;
  if (f !== ':memory:') fs.mkdirSync(path.dirname(path.resolve(f)), { recursive: true });
  return knex({
    client: 'better-sqlite3', connection: { filename: f }, useNullAsDefault: true,
    pool: f === ':memory:' ? { min: 1, max: 1 } : undefined,
  });
}

export function authentifier(req, _res, next) {
  const id = Number(req.get('X-User-Id'));
  if (!Number.isInteger(id) || id <= 0) return next(new ApiError(401, 'Authentification requise'));
  req.user = { id, role: req.get('X-User-Role') || 'ETUDIANT' };
  next();
}

export function lireId(valeur, nom = 'identifiant') {
  const n = Number(valeur);
  if (!Number.isInteger(n) || n <= 0) throw new ApiError(400, `${nom} invalide`);
  return n;
}

export function valider(schema, donnees) {
  const r = schema.safeParse(donnees ?? {});
  if (!r.success) {
    throw new ApiError(400, r.error.issues.map((i) => (i.path.length ? `${i.path.join('.')} : ${i.message}` : i.message)).join(' · '));
  }
  return r.data;
}

export function creerClientEchanges(baseUrl = process.env.EXCHANGE_SERVICE_URL || 'http://localhost:8082') {
  return async function recupererEchange(exchangeId, user) {
    let r;
    try {
      r = await fetch(`${baseUrl}/api/exchanges/${exchangeId}`, {
        headers: { 'X-User-Id': String(user.id), 'X-User-Role': user.role, Accept: 'application/json' },
        signal: AbortSignal.timeout(4000),
      });
    } catch { return null; }
    if (r.status === 404) throw new ApiError(404, 'Échange introuvable');
    if (r.status === 403) throw new ApiError(403, 'Vous ne participez pas à cet échange');
    if (!r.ok) return null;
    return r.json();
  };
}

export async function echangeDuParticipant(fetchExchange, exchangeId, user) {
  const e = await fetchExchange(exchangeId, user);
  if (!e) throw new ApiError(503, 'Le service des échanges est indisponible, réessayez plus tard');
  if (e.providerId !== user.id && e.requesterId !== user.id) throw new ApiError(403, 'Vous ne participez pas à cet échange');
  return e;
}

export function gestionErreurs(err, _req, res, _next) {
  if (err instanceof ApiError) return res.status(err.status).json({ status: err.status, message: err.message });
  if (err?.type === 'entity.parse.failed') return res.status(400).json({ status: 400, message: 'JSON invalide' });
  console.error(err);
  res.status(500).json({ status: 500, message: 'Erreur interne du serveur' });
}
