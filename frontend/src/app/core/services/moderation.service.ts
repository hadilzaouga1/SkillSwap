import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { Competence } from '../models/competence.model';

@Injectable({
  providedIn: 'root'
})
export class ModerationService {
  private apiUrl = `${API_URLS.gateway}/moderation/competences`;

  constructor(private http: HttpClient) { }

  getAValider(): Observable<Competence[]> {
    return this.http.get<Competence[]>(this.apiUrl);
  }

  valider(id: number): Observable<Competence> {
    return this.http.put<Competence>(`${this.apiUrl}/${id}/valider`, {});
  }

  refuser(id: number): Observable<Competence> {
    return this.http.put<Competence>(`${this.apiUrl}/${id}/refuser`, {});
  }
}