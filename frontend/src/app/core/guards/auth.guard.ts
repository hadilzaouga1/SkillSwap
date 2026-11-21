import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/auth.model';
import { AuthService } from '../services/auth.service';

/**
 * Garde de route : réservée aux personnes connectées.
 * Sinon redirection vers la page de connexion Keycloak (retour sur la page demandée).
 * Usage dans app.routes.ts :  canActivate: [authGuard]
 */
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.estConnecte) return true;

  // Connecté à Keycloak mais profil non chargé : on évite une boucle de redirection.
  if (auth.sessionKeycloakActive) return router.createUrlTree(['/']);

  auth.connexion(state.url);
  return false;
};

/**
 * Garde de route par rôle.
 * Usage :  canActivate: [roleGuard('MODERATEUR', 'ADMIN')]
 *
 * ATTENTION : cette garde ne fait que cacher les pages dans l'interface.
 * La vraie sécurité est dans l'API Gateway et les microservices.
 */
export const roleGuard = (...rolesAutorises: Role[]): CanActivateFn => {
  return (route, state) => {
    const auth = inject(AuthService);
    const router = inject(Router);

    if (!auth.estConnecte) {
      if (auth.sessionKeycloakActive) return router.createUrlTree(['/']);
      auth.connexion(state.url);
      return false;
    }
    return auth.role !== null && rolesAutorises.includes(auth.role)
      ? true
      : router.createUrlTree(['/']);
  };
};

/** Pour /connexion : redirige directement vers Keycloak. */
export const connexionRedirectGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.sessionKeycloakActive) return router.createUrlTree(['/']);
  const retour = route.queryParamMap.get('retour');
  auth.connexion(retour && retour.startsWith('/') && !retour.startsWith('//') ? retour : '/');
  return false;
};

/** Pour /inscription : redirige directement vers le formulaire d'inscription Keycloak. */
export const inscriptionRedirectGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.sessionKeycloakActive) return router.createUrlTree(['/']);
  auth.inscription('/');
  return false;
};
