import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, tap } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { Solde, TransactionCredit } from '../models/credit.model';

@Injectable({
  providedIn: 'root'
})
export class CreditService {
  private apiUrl = `${API_URLS.gateway}/credits`;

  // BehaviorSubject : garde en mémoire la dernière valeur du solde pour le badge du haut
  private soldeSubject = new BehaviorSubject<Solde | null>(null);
  
  /** Observable public que le Layout écoute pour afficher le badge */
  public solde$ = this.soldeSubject.asObservable();

  constructor(private http: HttpClient) { }

  /** Récupère le solde en arrière-plan et met à jour le badge */
  rafraichirSolde(): void {
    this.http.get<Solde>(`${this.apiUrl}/balance`).subscribe({
      next: (solde) => this.soldeSubject.next(solde),
      error: (err) => console.error('Erreur chargement solde', err)
    });
  }

  /** Méthode utilisée par la page "Mes Crédits" (US14) */
  getSolde(): Observable<Solde> {
    return this.http.get<Solde>(`${this.apiUrl}/balance`).pipe(
      // On en profite pour mettre à jour le badge du haut en même temps !
      tap((solde) => this.soldeSubject.next(solde))
    );
  }

  /** Historique de mes crédits (US14). */
  getTransactions(): Observable<TransactionCredit[]> {
    return this.http.get<TransactionCredit[]>(`${this.apiUrl}/transactions`);
  }
}