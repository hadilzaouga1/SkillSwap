//  Stockage des PDF sur le disque
//  - le nom du fichier sur le disque est ALÉATOIRE (jamais le nom envoyé par l'utilisateur)
//  - les fichiers ne sont jamais servis directement : toujours via une route authentifiée
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

export function creerStockage(dossier) {
  const racine = path.resolve(dossier);
  fs.mkdirSync(racine, { recursive: true });

  return {
    /** Un vrai PDF commence toujours par "%PDF-" (on ne se fie pas à l'extension ni au type MIME). */
    estPdf(buffer) {
      return buffer.length > 5 && buffer.subarray(0, 5).toString('latin1') === '%PDF-';
    },
    async sauver(buffer) {
      const nom = `${crypto.randomUUID()}.pdf`;
      await fs.promises.writeFile(path.join(racine, nom), buffer, { flag: 'wx' });
      return nom;
    },
    chemin(nom) {
      // Empêche toute tentative de sortir du dossier (ex. "../../etc/passwd")
      const complet = path.join(racine, path.basename(nom));
      return complet;
    },
    async supprimer(nom) {
      try {
        await fs.promises.unlink(path.join(racine, path.basename(nom)));
      } catch {
        /* déjà supprimé : rien à faire */
      }
    },
  };
}
