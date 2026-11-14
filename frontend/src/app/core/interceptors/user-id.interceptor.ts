import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { SessionService } from '../services/session.service';
import { AuthService } from '../services/auth.service';

export const userIdInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const auth = inject(AuthService);

  const userId = String(session.userId); 
  
  const userRole = auth.utilisateur?.role ?? 'ETUDIANT'; 

  const clonedReq = req.clone({
    setHeaders: {
      'X-User-Id': userId,
      'X-User-Role': userRole 
    }
  });
  
  return next(clonedReq);
};