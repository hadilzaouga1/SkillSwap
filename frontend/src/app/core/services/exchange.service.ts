import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { Exchange, ExchangeRequest } from '../models/exchange.model';

/**
 * Appels HTTP vers /api/exchanges (voir ExchangeController.java).
 * Tout passe par l'API Gateway ; le token JWT est ajouté par authInterceptor.
 */
@Injectable({
  providedIn: 'root'
})
export class ExchangeService {
  private apiUrl = `${API_URLS.gateway}/exchanges`;

  constructor(private http: HttpClient) { }

  creer(data: ExchangeRequest): Observable<Exchange> {
    return this.http.post<Exchange>(this.apiUrl, data);
  }

  /** Demandes que j'ai envoyées (je suis le demandeur). */
  getEnvoyees(): Observable<Exchange[]> {
    return this.http.get<Exchange[]>(`${this.apiUrl}/my-requests`);
  }

  /** Demandes que j'ai reçues (je suis le prestataire). */
  getRecues(): Observable<Exchange[]> {
    return this.http.get<Exchange[]>(`${this.apiUrl}/received`);
  }

  getParId(id: number): Observable<Exchange> {
    return this.http.get<Exchange>(`${this.apiUrl}/${id}`);
  }

  accepter(id: number): Observable<Exchange> {
    return this.http.patch<Exchange>(`${this.apiUrl}/${id}/accept`, null);
  }

  refuser(id: number): Observable<Exchange> {
    return this.http.patch<Exchange>(`${this.apiUrl}/${id}/reject`, null);
  }

  proposerCreneau(id: number, proposedDateTime: string): Observable<Exchange> {
    return this.http.patch<Exchange>(`${this.apiUrl}/${id}/propose-time`, { proposedDateTime });
  }

  accepterCreneau(id: number): Observable<Exchange> {
    return this.http.patch<Exchange>(`${this.apiUrl}/${id}/accept-proposal`, null);
  }

  annuler(id: number): Observable<Exchange> {
    return this.http.patch<Exchange>(`${this.apiUrl}/${id}/cancel`, null);
  }

  /** Le prestataire déclare la séance terminée. */
  terminer(id: number): Observable<Exchange> {
    return this.http.patch<Exchange>(`${this.apiUrl}/${id}/complete`, null);
  }

  /** Le demandeur confirme que la séance a bien eu lieu. */
  confirmer(id: number): Observable<Exchange> {
    return this.http.patch<Exchange>(`${this.apiUrl}/${id}/confirm`, null);
  }
}
