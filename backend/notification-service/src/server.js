import { creerDb } from './core.js';
import { creerApp, migrer } from './app.js';

const port = Number(process.env.PORT || 8089);
const db = creerDb({ nom: 'skillswap_notification' });
await migrer(db);
creerApp({ db }).listen(port, () => console.log(`notification-service démarré sur http://localhost:${port}`));
