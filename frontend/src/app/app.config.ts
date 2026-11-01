import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { userIdInterceptor } from './core/interceptors/user-id.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    // L'intercepteur ajoute l'en-tête X-User-Id à chaque requête (simulation avant l'authentification)
    provideHttpClient(withInterceptors([userIdInterceptor]))
  ]
};
