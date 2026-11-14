import { creerApp } from './app.js';
import { creerFournisseur } from './providers.js';

const port = Number(process.env.PORT || 8090);
const fournisseur = creerFournisseur();
creerApp({ fournisseur }).listen(port, () =>
  console.log(`ai-service démarré sur http://localhost:${port} (mode : ${fournisseur ? 'Claude (' + fournisseur.model + ')' : 'local, sans clé API'})`));
