import { Kafka } from 'kafkajs';

export async function migrerKafkaLog(db) {
  if (await db.schema.hasTable('exchange_events_log')) return;
  await db.schema.createTable('exchange_events_log', (t) => {
    t.increments('id');
    t.integer('exchange_id').notNullable().index();
    t.integer('requester_id').nullable();
    t.integer('provider_id').nullable();
    t.integer('skill_id').nullable();
    t.string('skill_title', 160).nullable();
    t.string('status', 40).notNullable();
    t.string('dedupe_key', 140).notNullable().unique();
    t.timestamp('processed_at').defaultTo(db.fn.now());
  });
}

async function assurerTopicExiste(kafka, topic) {
  const admin = kafka.admin();
  await admin.connect();
  try {
    await admin.createTopics({
      waitForLeaders: true,
      topics: [{ topic, numPartitions: 1, replicationFactor: 1 }],
    });
  } finally {
    await admin.disconnect();
  }
}

export async function demarrerConsommateurTrust({
  db,
  brokers = (process.env.KAFKA_BROKERS || 'localhost:9092').split(',').map((s) => s.trim()).filter(Boolean),
  topic = process.env.KAFKA_TOPIC_EXCHANGE_STATUS || 'exchange.status.changed',
  groupId = process.env.KAFKA_GROUP_TRUST || 'trust-service-group',
} = {}) {
  const kafka = new Kafka({
    clientId: 'trust-service',
    brokers,
  });

  const connecter = async () => {
    try {
      await assurerTopicExiste(kafka, topic);
      const consumer = kafka.consumer({ groupId, allowAutoTopicCreation: true });
      await consumer.connect();
      await consumer.subscribe({ topic, fromBeginning: false });

      await consumer.run({
        eachMessage: async ({ message }) => {
          if (!message?.value) return;

          let event;
          try {
            event = JSON.parse(message.value.toString('utf8'));
          } catch (err) {
            console.error('[trust-service][kafka] JSON invalide :', err.message);
            return;
          }

          const dedupeKey = `trust-ex-${event.exchangeId}-${event.status}-${event.occurredAt || ''}`;

          try {
            await db('exchange_events_log').insert({
              exchange_id: event.exchangeId ?? 0,
              requester_id: event.requesterId ?? null,
              provider_id: event.providerId ?? null,
              skill_id: event.skillId ?? null,
              skill_title: event.skillTitle ? String(event.skillTitle).slice(0, 160) : null,
              status: String(event.status || 'UNKNOWN'),
              dedupe_key: dedupeKey,
            });
          } catch (err) {
            if (!/unique/i.test(String(err.message))) {
              console.error('[trust-service][kafka] Erreur écriture log :', err.message);
              throw err;
            }
          }

          if (event.status === 'COMPLETED') {
            console.log(`[trust-service][kafka] Échange ${event.exchangeId} TERMINÉ -> éligible à l'évaluation entre user ${event.requesterId} et user ${event.providerId}`);
          } else if (event.status === 'CANCELLED' || event.status === 'REJECTED') {
            console.log(`[trust-service][kafka] Échange ${event.exchangeId} en statut ${event.status} (provider=${event.providerId}, requester=${event.requesterId})`);
          } else {
            console.log(`[trust-service][kafka] Statut reçu pour échange ${event.exchangeId} : ${event.status}`);
          }
        },
      });

      console.log(`[trust-service][kafka] Connecté (groupId=${groupId}, topic=${topic})`);
    } catch (err) {
      console.error('[trust-service][kafka] En attente du topic/broker, nouvelle tentative dans 3s :', err.message);
      setTimeout(connecter, 3000);
    }
  };

  await connecter();
}