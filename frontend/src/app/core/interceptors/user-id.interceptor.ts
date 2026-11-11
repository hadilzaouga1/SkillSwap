import { inject } from '@angular/core';
import { HttpInterceptorFn } from '@angular/common/http';
import { SessionService } from '../services/session.service';
import { AuthService } from '../services/auth.service';

export const userIdInterceptor: HttpInterceptorFn = (req, next) => {
  const session = inject(SessionService);
  const auth = inject(AuthService);

  // On récupère l'ID de l'utilisateur (1 par défaut via ton SessionService)
  const userId = String(session.userId); 
  
  // On récupère le rôle. Si l'utilisateur n'est pas encore chargé, on met 'ETUDIANT' par défaut.
  const userRole = auth.utilisateur?.role ?? 'ETUDIANT'; 

  // On clone la requête pour y ajouter les en-têtes obligatoires pour le backend
  const clonedReq = req.clone({
    setHeaders: {
      'X-User-Id': userId,
      'X-User-Role': userRole 
    }
  });
  
  return next(clonedReq);
};