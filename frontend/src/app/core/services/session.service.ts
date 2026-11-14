import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class SessionService {
  private static readonly CLE = 'skillswap_user_id';

  readonly utilisateursTest = [
    { id: 1, nom: 'Hadil' },
    { id: 2, nom: 'Roua' }
  ];

  get userId(): number {
    const valeur = localStorage.getItem(SessionService.CLE);
    return valeur ? Number(valeur) : 1; 
  }

  set userId(id: number) {
    localStorage.setItem(SessionService.CLE, String(id));
  }
}
