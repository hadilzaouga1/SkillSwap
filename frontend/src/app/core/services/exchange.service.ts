import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { Exchange, ExchangeRequest } from '../models/exchange.model';


@Injectable({
  providedIn: 'root'
})
export class ExchangeService {
  private apiUrl = `${API_URLS.gateway}/exchanges`;

  constructor(private http: HttpClient) { }

  creer(data: ExchangeRequest): Observable<Exchange> {
    return this.http.post<Exchange>(this.apiUrl, data);
  }
  getEnvoyees(): Observable<Exchange[]> {
    return this.http.get<Exchange[]>(`${this.apiUrl}/my-requests`);
  }

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

  terminer(id: number): Observable<Exchange> {
    return this.http.patch<Exchange>(`${this.apiUrl}/${id}/complete`, null);
  }
  
  confirmer(id: number): Observable<Exchange> {
    return this.http.patch<Exchange>(`${this.apiUrl}/${id}/confirm`, null);
  }
}
