import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/api.config';
import { Competence, CompetenceRequest, FiltresCompetence } from '../models/competence.model';

/** Appels HTTP vers /api/competences (voir CompetenceController.java). */
@Injectable({
  providedIn: 'root'
})
export class CompetenceService {
  private apiUrl = `${API_URL}/competences`;

  constructor(private http: HttpClient) { }

  /** Catalogue public, avec filtres optionnels (US7). */
  getCompetences(filtres: FiltresCompetence = {}): Observable<Competence[]> {
    let params = new HttpParams();
    if (filtres.motCle && filtres.motCle.trim()) {
      params = params.set('motCle', filtres.motCle.trim());
    }
    if (filtres.categorieId) {
      params = params.set('categorieId', filtres.categorieId);
    }
    if (filtres.niveau) {
      params = params.set('niveau', filtres.niveau);
    }
    if (filtres.etudiantId) {
      params = params.set('etudiantId', filtres.etudiantId);
    }
    return this.http.get<Competence[]>(this.apiUrl, { params });
  }

  getCompetence(id: number): Observable<Competence> {
    return this.http.get<Competence>(`${this.apiUrl}/${id}`);
  }

  /** Compétences de l'utilisateur connecté (tous statuts). */
  getMesCompetences(): Observable<Competence[]> {
    return this.http.get<Competence[]>(`${this.apiUrl}/mes-competences`);
  }

  /**publier. */
  creer(data: CompetenceRequest): Observable<Competence> {
    return this.http.post<Competence>(this.apiUrl, data);
  }

  /** modifier. */
  modifier(id: number, data: CompetenceRequest): Observable<Competence> {
    return this.http.put<Competence>(`${this.apiUrl}/${id}`, data);
  }

  /**supprimer. */
  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
