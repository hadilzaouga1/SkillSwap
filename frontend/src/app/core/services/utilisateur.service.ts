import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URL } from '../config/api.config';
import { MonProfil, ProfilPublic, ProfilUpdateRequest } from '../models/profil.model';

/** Appels HTTP vers /api/utilisateurs (profils). */
@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = `${API_URL}/utilisateurs`;

  constructor(private http: HttpClient) { }

  /** Mon profil complet. */
  getMonProfil(): Observable<MonProfil> {
    return this.http.get<MonProfil>(`${this.apiUrl}/me`);
  }

  /** modifier mon profil. */
  modifierMonProfil(data: ProfilUpdateRequest): Observable<MonProfil> {
    return this.http.put<MonProfil>(`${this.apiUrl}/me`, data);
  }

  /** profil public d'un étudiant. */
  getProfilPublic(id: number): Observable<ProfilPublic> {
    return this.http.get<ProfilPublic>(`${this.apiUrl}/${id}`);
  }
}
