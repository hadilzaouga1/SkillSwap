import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { MonProfil, ProfilPublic, ProfilUpdateRequest } from '../models/profil.model';

@Injectable({
  providedIn: 'root'
})
export class UtilisateurService {
  private apiUrl = `${API_URLS.gateway}/utilisateurs`;

  constructor(private http: HttpClient) { }

  getMonProfil(): Observable<MonProfil> {
    return this.http.get<MonProfil>(`${this.apiUrl}/me`);
  }

  modifierMonProfil(data: ProfilUpdateRequest): Observable<MonProfil> {
    return this.http.put<MonProfil>(`${this.apiUrl}/me`, data);
  }

  getProfilPublic(id: number): Observable<ProfilPublic> {
    return this.http.get<ProfilPublic>(`${this.apiUrl}/${id}`);
  }
}
