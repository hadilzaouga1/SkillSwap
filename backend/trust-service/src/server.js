import { creerDb } from './core.js';
import { creerApp, migrer } from './app.js';

const port = Number(process.env.PORT || 8088);
const db = creerDb({ nom: 'skillswap_trust' });
await migrer(db);
creerApp({ db }).listen(port, () => console.log(`trust-service démarré sur http://localhost:${port}`));
