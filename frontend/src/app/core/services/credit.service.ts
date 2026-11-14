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
  private soldeSubject = new BehaviorSubject<Solde | null>(null);

  public solde$ = this.soldeSubject.asObservable();

  constructor(private http: HttpClient) { }

  rafraichirSolde(): void {
    this.http.get<Solde>(`${this.apiUrl}/balance`).subscribe({
      next: (solde) => this.soldeSubject.next(solde),
      error: (err) => console.error('Erreur chargement solde', err)
    });
  }

  getSolde(): Observable<Solde> {
    return this.http.get<Solde>(`${this.apiUrl}/balance`).pipe(
      tap((solde) => this.soldeSubject.next(solde))
    );
  }
  getTransactions(): Observable<TransactionCredit[]> {
    return this.http.get<TransactionCredit[]>(`${this.apiUrl}/transactions`);
  }
}