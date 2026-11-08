import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { SessionService } from '../services/session.service';
export const userIdInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  
  // On clone la requête pour ajouter l'en-tête
  const clonedReq = req.clone({
    setHeaders: {
      'X-User-Id': String(session.userId)
    }
  });
  
  return next(clonedReq);
};