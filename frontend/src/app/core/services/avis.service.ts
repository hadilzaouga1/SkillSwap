import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { Avis, AvisEchange, Reputation } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class AvisService {
  private base = `${API_URLS.gateway}/reviews`;
  constructor(private http: HttpClient) {}

  deLEchange(exchangeId: number): Observable<AvisEchange> {
    return this.http.get<AvisEchange>(`${this.base}/exchange/${exchangeId}`);
  }
  publier(exchangeId: number, rating: number, comment: string | null): Observable<Avis> {
    return this.http.post<Avis>(`${this.base}/exchange/${exchangeId}`, { rating, comment });
  }
  reputation(userId: number): Observable<Reputation> {
    return this.http.get<Reputation>(`${this.base}/user/${userId}`);
  }
}
