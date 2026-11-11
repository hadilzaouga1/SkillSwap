import knex from 'knex';
import fs from 'node:fs';
import path from 'node:path';

export function creerDb({ client = process.env.DB_CLIENT || 'better-sqlite3', fichier } = {}) {
  if (client === 'mysql2') {
    return knex({
      client: 'mysql2',
      connection: {
        host: process.env.DB_HOST || 'localhost',
        port: Number(process.env.DB_PORT || 3306),
        user: process.env.DB_USER || 'root',
        password: process.env.DB_PASSWORD || '',
        database: process.env.DB_NAME || 'skillswap_session',
      },
    });
  }
  const f = fichier ?? process.env.SQLITE_FILE ?? './data/session.sqlite';
  if (f !== ':memory:') fs.mkdirSync(path.dirname(path.resolve(f)), { recursive: true });
  return knex({
    client: 'better-sqlite3',
    connection: { filename: f },
    useNullAsDefault: true,
    pool: f === ':memory:' ? { min: 1, max: 1 } : undefined,
  });
}

/** Crée les tables si elles n'existent pas (idempotent). */
export async function migrer(db) {
  if (!(await db.schema.hasTable('sessions'))) {
    await db.schema.createTable('sessions', (t) => {
      t.increments('id');
      t.integer('exchange_id').notNullable().unique();
      t.integer('provider_id').notNullable();
      t.integer('requester_id').notNullable();
      t.integer('skill_id').nullable();
      t.string('skill_title', 160).nullable();
      t.string('title', 160).notNullable();
      t.string('summary', 300).nullable();
      t.text('description').nullable();
      t.text('objectives').nullable();   // JSON : ["...", "..."]
      t.text('outline').nullable();      // JSON : [{title, description, minutes}]
      t.string('prerequisites', 500).nullable();
      t.string('audience', 300).nullable();
      t.string('level', 20).nullable();
      t.string('language', 40).nullable();
      t.string('format', 20).nullable();
      t.string('mode', 20).nullable();
      t.string('platform', 80).nullable();
      t.string('meeting_link', 500).nullable();
      t.string('location', 200).nullable();
      t.text('delivery_notes').nullable();
      t.string('materials', 800).nullable();
      t.integer('updated_by').nullable();
      t.timestamp('created_at').defaultTo(db.fn.now());
      t.timestamp('updated_at').defaultTo(db.fn.now());
    });
  }
  if (!(await db.schema.hasTable('session_resources'))) {
    await db.schema.createTable('session_resources', (t) => {
      t.increments('id');
      t.integer('session_id').notNullable().references('id').inTable('sessions').onDelete('CASCADE');
      t.string('kind', 10).notNullable();          // PDF | LINK
      t.string('title', 160).notNullable();
      t.string('description', 300).nullable();
      t.string('url', 500).nullable();
      t.string('stored_name', 80).nullable();      // nom aléatoire sur le disque
      t.string('original_name', 200).nullable();
      t.integer('size').nullable();
      t.integer('added_by').notNullable();
      t.timestamp('created_at').defaultTo(db.fn.now());
    });
  }
}
