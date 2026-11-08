/**
 * Utilitaires pour les champs <input type="datetime-local"> :
 * valeur "2026-11-20T18:00" <-> format attendu par l'API "2026-11-20T18:00:00".
 */

/** Date et heure actuelles au format du champ datetime-local (pour l'attribut min). */
export function maintenantPourChamp(): string {
  const d = new Date();
  d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
  return d.toISOString().slice(0, 16);
}

/** Valeur du champ -> format API (ajoute les secondes si besoin). */
export function versFormatApi(valeurChamp: string): string {
  return valeurChamp.length === 16 ? `${valeurChamp}:00` : valeurChamp;
}

/** Vrai si la valeur du champ est une date valide et située dans le futur. */
export function estDansLeFutur(valeurChamp: string): boolean {
  const date = new Date(valeurChamp);
  return !isNaN(date.getTime()) && date.getTime() > Date.now();
}
