import { Injectable } from '@angular/core';
import Keycloak from 'keycloak-js';
import { KEYCLOAK_CONFIG } from '../config/keycloak.config';
import { Role } from '../models/auth.model';

/**
 * Enveloppe autour de keycloak-js : connexion, inscription, déconnexion, token.
 * Ce service n'utilise pas HttpClient (l'intercepteur HTTP en dépend).
 */
@Injectable({ providedIn: 'root' })
export class KeycloakService {
  private readonly keycloak = new Keycloak(KEYCLOAK_CONFIG);

  /** À appeler une seule fois au démarrage de l'application. */
  async init(): Promise<boolean> {
    try {
      return await this.keycloak.init({
        onLoad: 'check-sso',
        pkceMethod: 'S256',
        silentCheckSsoRedirectUri: `${window.location.origin}/assets/silent-check-sso.html`,
        checkLoginIframe: false
      });
    } catch (e) {
      console.error('Keycloak indisponible :', e);
      return false;
    }
  }

  get authentifie(): boolean {
    return !!this.keycloak.authenticated;
  }

  get token(): string | undefined {
    return this.keycloak.token;
  }

  /** Rôle le plus élevé trouvé dans le token (realm_access.roles). */
  get role(): Role {
    const roles = this.keycloak.realmAccess?.roles ?? [];
    if (roles.includes('ADMIN')) return 'ADMIN';
    if (roles.includes('MODERATEUR')) return 'MODERATEUR';
    return 'ETUDIANT';
  }

  /** Renouvelle le token s'il expire dans moins de `minValiditeSecondes`. */
  async assurerTokenValide(minValiditeSecondes = 30): Promise<void> {
    if (!this.authentifie) return;
    try {
      await this.keycloak.updateToken(minValiditeSecondes);
    } catch {
      // Session expirée : on redemande une connexion.
      await this.connexion(window.location.pathname + window.location.search);
    }
  }

  /** Redirige vers la page de connexion Keycloak, puis revient sur `urlRetour`. */
  connexion(urlRetour = '/'): Promise<void> {
    return this.keycloak.login({ redirectUri: window.location.origin + urlRetour });
  }

  /** Redirige vers le formulaire d'inscription Keycloak. */
  inscription(urlRetour = '/'): Promise<void> {
    return this.keycloak.register({ redirectUri: window.location.origin + urlRetour });
  }

  deconnexion(): Promise<void> {
    return this.keycloak.logout({ redirectUri: window.location.origin });
  }
}
