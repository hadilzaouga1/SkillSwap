// Point d'entrée : lance la base, crée les tables puis démarre le serveur HTTP.
import { creerDb, migrer } from './db.js';
import { creerApp } from './app.js';

const port = Number(process.env.PORT || 8087);
const db = creerDb();
await migrer(db);

creerApp({ db }).listen(port, () => {
  console.log(`session-service démarré sur http://localhost:${port} (base : ${process.env.DB_CLIENT || 'better-sqlite3'})`);
});
