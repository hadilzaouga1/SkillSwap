import { Injectable } from '@angular/core';

/**
 * ============================================================
 *  SERVICE TEMPORAIRE : simule l'utilisateur connecté.
 * ============================================================
 * L'authentification sera développée en dernier. En attendant, on mémorise
 * l'id de l'étudiant "connecté" dans le navigateur (localStorage).
 * Il est envoyé au backend dans l'en-tête X-User-Id (voir user-id.interceptor.ts).
 */
@Injectable({ providedIn: 'root' })
export class SessionService {
  private static readonly CLE = 'skillswap_user_id';

  /** Utilisateurs de test présents dans V2__seed.sql */
  readonly utilisateursTest = [
    { id: 1, nom: 'Hadil' },
    { id: 2, nom: 'Roua' }
  ];

  get userId(): number {
    const valeur = localStorage.getItem(SessionService.CLE);
    return valeur ? Number(valeur) : 1; // par défaut : Hadil
  }

  set userId(id: number) {
    localStorage.setItem(SessionService.CLE, String(id));
  }
}
