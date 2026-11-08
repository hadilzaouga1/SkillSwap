import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

/**
 * Intercepteur HTTP appliqué à CHAQUE requête vers l'API :
 *  1. ajoute "Authorization: Bearer <token>" si la personne est connectée ;
 *  2. si l'API répond 401 (token expiré ou invalide) : déconnecte et renvoie vers la connexion.
 *     (sauf pour les routes /auth/, où un 401 signifie simplement "mauvais mot de passe").
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token;

  const requete = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(requete).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401 && !req.url.includes('/auth/')) {
        auth.deconnexion();
      }
      return throwError(() => err);
    })
  );
};
