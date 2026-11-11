import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
// 1. MODIFICATION ICI :
import { API_URLS } from '../config/api.config'; 
import {
  AuthResponse, ConnexionRequest, InscriptionRequest, Role, UtilisateurConnecte
} from '../models/auth.model';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private static readonly CLE_TOKEN = 'skillswap_token';
  private static readonly CLE_UTILISATEUR = 'skillswap_utilisateur';

  // 2. MODIFICATION ICI (ajout de .gateway) :
  private urlAuth = `${API_URLS.gateway}/auth`; 
  private utilisateurCourant: UtilisateurConnecte | null = null;

  constructor(private http: HttpClient, private router: Router) {
    this.utilisateurCourant = this.lireUtilisateurMemorise();
  }

  // ... (le reste du fichier reste inchangé)
  get token(): string | null {
    return localStorage.getItem(AuthService.CLE_TOKEN);
  }

  get utilisateur(): UtilisateurConnecte | null {
    return this.utilisateurCourant;
  }

  get estConnecte(): boolean {
    return !!this.token && !!this.utilisateurCourant;
  }

  get role(): Role | null {
    return this.utilisateurCourant?.role ?? null;
  }

  get estModerateur(): boolean {
    return this.role === 'MODERATEUR' || this.role === 'ADMIN';
  }

  connexion(data: ConnexionRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.urlAuth}/connexion`, data)
      .pipe(tap((reponse) => this.memoriser(reponse)));
  }

  inscription(data: InscriptionRequest): Observable<AuthResponse> {
    return this.http
      .post<AuthResponse>(`${this.urlAuth}/inscription`, data)
      .pipe(tap((reponse) => this.memoriser(reponse)));
  }

  deconnexion(): void {
    localStorage.removeItem(AuthService.CLE_TOKEN);
    localStorage.removeItem(AuthService.CLE_UTILISATEUR);
    this.utilisateurCourant = null;
    this.router.navigate(['/connexion']);
  }

  private memoriser(reponse: AuthResponse): void {
    localStorage.setItem(AuthService.CLE_TOKEN, reponse.token);
    localStorage.setItem(AuthService.CLE_UTILISATEUR, JSON.stringify(reponse.utilisateur));
    this.utilisateurCourant = reponse.utilisateur;
  }

  private lireUtilisateurMemorise(): UtilisateurConnecte | null {
    try {
      const brut = localStorage.getItem(AuthService.CLE_UTILISATEUR);
      return brut ? (JSON.parse(brut) as UtilisateurConnecte) : null;
    } catch {
      return null;
    }
  }
}