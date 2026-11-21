import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { Role, UtilisateurConnecte } from '../models/auth.model';
import { MonProfil } from '../models/profil.model';
import { KeycloakService } from './keycloak.service';

/**
 * Point d'entrée de l'authentification pour toute l'application.
 * L'API publique (estConnecte, utilisateur, role, estModerateur, deconnexion)
 * est la même qu'avant : les composants existants n'ont pas à changer.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private utilisateurCourant: UtilisateurConnecte | null = null;

  constructor(private keycloak: KeycloakService, private http: HttpClient) {}

  /** Appelé au démarrage (APP_INITIALIZER) : restaure la session et charge le profil. */
  async init(): Promise<void> {
    const connecte = await this.keycloak.init();
    if (!connecte) return;

    try {
      // Le gateway crée / retrouve l'utilisateur local (id numérique) à cet appel.
      const profil = await firstValueFrom(
        this.http.get<MonProfil>(`${API_URLS.gateway}/utilisateurs/me`)
      );
      this.utilisateurCourant = {
        id: profil.id,
        nom: profil.nom,
        email: profil.email,
        role: this.keycloak.role
      };
    } catch (e) {
      console.error('Impossible de charger le profil après la connexion :', e);
    }
  }

  get token(): string | null {
    return this.keycloak.token ?? null;
  }

  get utilisateur(): UtilisateurConnecte | null {
    return this.utilisateurCourant;
  }

  /** Connecté à Keycloak ET profil local chargé. */
  get estConnecte(): boolean {
    return this.keycloak.authentifie && !!this.utilisateurCourant;
  }

  /** Connecté à Keycloak, même si le profil local n'a pas pu être chargé. */
  get sessionKeycloakActive(): boolean {
    return this.keycloak.authentifie;
  }

  get role(): Role | null {
    return this.utilisateurCourant?.role ?? null;
  }

  get estModerateur(): boolean {
    return this.role === 'MODERATEUR' || this.role === 'ADMIN';
  }

  connexion(urlRetour = '/'): Promise<void> {
    return this.keycloak.connexion(urlRetour);
  }

  inscription(urlRetour = '/'): Promise<void> {
    return this.keycloak.inscription(urlRetour);
  }

  deconnexion(): void {
    this.utilisateurCourant = null;
    this.keycloak.deconnexion();
  }
}
