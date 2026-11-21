import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { from, switchMap } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { KeycloakService } from '../services/keycloak.service';


export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const keycloak = inject(KeycloakService);

  if (!req.url.startsWith(API_URLS.gateway) || !keycloak.authentifie) {
    return next(req);
  }

  return from(keycloak.assurerTokenValide()).pipe(
    switchMap(() => {
      const token = keycloak.token;
      return next(token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req);
    })
  );
};