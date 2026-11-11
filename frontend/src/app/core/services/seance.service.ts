import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { ResumeSeance, SeanceContenu, SeanceReponse, SupportSeance } from '../models/seance.model';

/**
 * Appels HTTP vers /api/sessions (session-service, Node/Express, port 8087 derrière la Gateway).
 * Une "séance" est toujours identifiée par l'id de l'échange auquel elle appartient.
 * (Ne pas confondre avec SessionService, qui simule l'utilisateur connecté.)
 */
@Injectable({ providedIn: 'root' })
export class SeanceService {
  private base = `${API_URLS.gateway}/sessions`;
  constructor(private http: HttpClient) {}

  get(exchangeId: number): Observable<SeanceReponse> {
    return this.http.get<SeanceReponse>(`${this.base}/by-exchange/${exchangeId}`);
  }

  enregistrer(exchangeId: number, contenu: Partial<SeanceContenu>): Observable<SeanceReponse> {
    return this.http.put<SeanceReponse>(`${this.base}/by-exchange/${exchangeId}`, contenu);
  }

  resumes(ids: number[]): Observable<Record<number, ResumeSeance>> {
    return this.http.get<Record<number, ResumeSeance>>(`${this.base}/summaries`, { params: { exchangeIds: ids.join(',') } });
  }

  ajouterLien(exchangeId: number, title: string, url: string, description?: string): Observable<SupportSeance> {
    return this.http.post<SupportSeance>(`${this.base}/by-exchange/${exchangeId}/resources/link`, { title, url, description });
  }

  ajouterPdf(exchangeId: number, fichier: File, title: string, description?: string): Observable<SupportSeance> {
    const f = new FormData();
    f.append('file', fichier, fichier.name);
    f.append('title', title);
    if (description) f.append('description', description);
    return this.http.post<SupportSeance>(`${this.base}/by-exchange/${exchangeId}/resources/pdf`, f);
  }

  /** Le PDF exige le jeton JWT : on le télécharge en blob puis on l'ouvre via une URL locale. */
  telechargerPdf(exchangeId: number, resourceId: number): Observable<Blob> {
    return this.http.get(`${this.base}/by-exchange/${exchangeId}/resources/${resourceId}/file`, { responseType: 'blob' });
  }

  supprimer(exchangeId: number, resourceId: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/by-exchange/${exchangeId}/resources/${resourceId}`);
  }
}
