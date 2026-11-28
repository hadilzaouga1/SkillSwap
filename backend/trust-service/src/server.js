import { creerDb } from './core.js';
import { creerApp, migrer } from './app.js';
import { demarrerConsommateurTrust, migrerKafkaLog } from './kafkaConsumer.js';

const port = Number(process.env.PORT || 8088);
const db = creerDb({ nom: 'skillswap_trust' });
await migrer(db);
await migrerKafkaLog(db);

creerApp({ db }).listen(port, () => {
  console.log(`trust-service démarré sur http://localhost:${port}`);
});

if (process.env.KAFKA_ENABLED !== 'false') {
  demarrerConsommateurTrust({ db }).catch((err) => {
    console.error('[trust-service][kafka] Impossible de démarrer le consommateur :', err.message);
  });
}