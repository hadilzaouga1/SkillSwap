import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // L'intercepteur ajoute le token JWT à chaque requête et gère les sessions expirées
    provideHttpClient(withInterceptors([authInterceptor]))
  ]
};
