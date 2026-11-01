import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { SessionService } from '../services/session.service';

/**
 * Intercepteur HTTP : ajoute l'en-tête X-User-Id à CHAQUE requête vers l'API.
 * Le backend (UtilisateurCourantService) s'en sert pour savoir qui agit.
 */
export const userIdInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  return next(req.clone({ setHeaders: { 'X-User-Id': String(session.userId) } }));
};
