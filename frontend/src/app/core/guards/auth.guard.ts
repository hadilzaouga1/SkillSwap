import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { Role } from '../models/auth.model';
import { AuthService } from '../services/auth.service';

/**
 * Garde de route : réservée aux personnes connectées.
 * Sinon redirection vers /connexion (avec l'adresse demandée, pour y revenir après).
 * Usage dans app.routes.ts :  canActivate: [authGuard]
 */
export const authGuard: CanActivateFn = (route, state) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  return auth.estConnecte
    ? true
    : router.createUrlTree(['/connexion'], { queryParams: { retour: state.url } });
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
      return router.createUrlTree(['/connexion'], { queryParams: { retour: state.url } });
    }
    return auth.role !== null && rolesAutorises.includes(auth.role)
      ? true
      : router.createUrlTree(['/']);
  };
};
