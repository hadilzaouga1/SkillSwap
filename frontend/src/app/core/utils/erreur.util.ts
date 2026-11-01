
import { HttpErrorResponse } from '@angular/common/http';

// Transformer une erreur HTTP en un message compréhensible pour l'utilisateur.
export function messageErreur(err: HttpErrorResponse): string {

  // Vérifier si le statut est 0.
  // Cela signifie généralement que le frontend n'arrive pas à joindre le backend.
  if (err.status === 0) {
    return "Impossible de joindre le serveur. Vérifiez que le backend est démarré (port 8081).";
  }

  // Récupérer les détails des erreurs envoyées par le backend
  // join(' · ') rassemble ces valeurs dans une seule chaîne de caractères.
  // Si aucune erreur détaillée n'existe, utiliser une chaîne vide.
  const detail = err.error?.erreurs
    ? Object.values<string>(err.error.erreurs).join(' · ')
    : '';

  // Récupérer le message général envoyé par le backend.
  // Si le message n'existe pas, afficher un message par défaut.
  const message = err.error?.message ?? 'Une erreur est survenue.';

  // Si des détails existent, les ajouter au message général.
  // Sinon, retourner uniquement le message général.
  return detail ? `${message} : ${detail}` : message;
}
