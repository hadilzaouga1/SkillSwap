import { Kafka } from 'kafkajs';

const REGLES = {
  PENDING: {
    vers: 'provider',
    type: 'EXCHANGE_REQUEST',
    titre: "Nouvelle demande d'échange",
    texte: (t) => `Quelqu'un souhaite suivre « ${t} ». Répondez à la demande.`,
  },
  TIME_PROPOSED: {
    vers: 'requester',
    type: 'EXCHANGE_TIME',
    titre: 'Nouveau créneau proposé',
    texte: (t) => `Le prestataire propose un autre créneau pour « ${t} ».`,
  },
  ACCEPTED: {
    vers: 'requester',
    type: 'EXCHANGE_ACCEPTED',
    titre: 'Demande acceptée',
    texte: (t) => `Votre demande pour « ${t} » est acceptée. Consultez la fiche de la séance.`,
  },
  REJECTED: {
    vers: 'requester',
    type: 'EXCHANGE_REJECTED',
    titre: 'Demande refusée',
    texte: (t) => `Votre demande pour « ${t} » a été refusée.`,
  },
  AWAITING_CONFIRMATION: {
    vers: 'requester',
    type: 'EXCHANGE_DONE',
    titre: 'Confirmez la séance',
    texte: (t) => `La séance « ${t} » est déclarée terminée : confirmez qu'elle a eu lieu.`,
  },
  COMPLETED: {
    vers: 'provider',
    type: 'EXCHANGE_COMPLETED',
    titre: 'Échange terminé',
    texte: (t) => `L'échange « ${t} » est confirmé. Pensez à laisser un avis.`,
  },
  CANCELLED: {
    vers: 'other',
    type: 'EXCHANGE_CANCELLED',
    titre: 'Échange annulé',
    texte: (t) => `L'échange « ${t} » a été annulé.`,
  },
};

function resoudreCible(regle, event) {
  const reqId = Number(event.requesterId);
  const provId = Number(event.providerId);
  if (!Number.isInteger(reqId) || !Number.isInteger(provId)) return [];

  if (regle.vers === 'provider') return [provId];
  if (regle.vers === 'requester') return [reqId];
  return [reqId, provId];
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

export async function demarrerConsommateurNotifications({
  db,
  brokers = (process.env.KAFKA_BROKERS || 'localhost:9092').split(',').map((s) => s.trim()).filter(Boolean),
  topic = process.env.KAFKA_TOPIC_EXCHANGE_STATUS || 'exchange.status.changed',
  groupId = process.env.KAFKA_GROUP_NOTIFICATION || 'notification-service-group',
} = {}) {
  const kafka = new Kafka({
    clientId: 'notification-service',
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
            console.error('[notification-service][kafka] JSON invalide :', err.message);
            return;
          }

          const regle = REGLES[event.status];
          if (!regle) return;

          const cibles = resoudreCible(regle, event);
          const titreCompetence = event.skillTitle || 'votre échange';
          const lien = `/echanges?onglet=recues`;
          const dedupeKey = `ex-${event.exchangeId}-${event.status}`;

          for (const userId of cibles) {
            try {
              await db('notifications').insert({
                user_id: userId,
                type: regle.type,
                title: regle.titre,
                message: regle.texte(titreCompetence),
                link: lien,
                exchange_id: event.exchangeId ?? null,
                dedupe_key: dedupeKey,
              });
              console.log(`[notification-service][kafka] Notification créée pour user=${userId} (${event.status}, exchange=${event.exchangeId})`);
            } catch (err) {
              if (/unique/i.test(String(err.message))) {
                // Déjà traitée : idempotence garantie
              } else {
                console.error('[notification-service][kafka] Erreur insertion notification :', err.message);
                throw err;
              }
            }
          }
        },
      });

      console.log(`[notification-service][kafka] Connecté (groupId=${groupId}, topic=${topic})`);
    } catch (err) {
      console.error('[notification-service][kafka] En attente du topic/broker, nouvelle tentative dans 3s :', err.message);
      setTimeout(connecter, 3000);
    }
  };

  await connecter();
}