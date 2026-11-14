
import { HttpErrorResponse } from '@angular/common/http';

export function messageErreur(err: HttpErrorResponse): string {
  if (err.status === 0) {
    return "Impossible de joindre le serveur. Vérifiez que le backend est démarré (port 8081).";
  }

  const detail = err.error?.erreurs
    ? Object.values<string>(err.error.erreurs).join(' · ')
    : '';
  const message = err.error?.message ?? `Erreur ${err.status} (${err.statusText || 'sans détail'})`;
  return detail ? `${message} : ${detail}` : message;
}
