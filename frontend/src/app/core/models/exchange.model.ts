/** Statuts d'un échange : doivent correspondre à l'énumération Java "ExchangeStatus" (exchange-service). */
export type ExchangeStatus =
  | 'PENDING'
  | 'TIME_PROPOSED'
  | 'ACCEPTED'
  | 'AWAITING_CONFIRMATION'
  | 'COMPLETED'
  | 'REJECTED'
  | 'CANCELLED';

/** Libellés affichés à l'écran pour chaque statut. */
export const STATUT_LIBELLES: Record<ExchangeStatus, string> = {
  PENDING: 'En attente de réponse',
  TIME_PROPOSED: 'Nouveau créneau proposé',
  ACCEPTED: 'Acceptée',
  AWAITING_CONFIRMATION: 'En attente de confirmation',
  COMPLETED: 'Terminée',
  REJECTED: 'Refusée',
  CANCELLED: 'Annulée'
};

/** Échange tel que renvoyé par l'API (ExchangeResponse.java). */
export interface Exchange {
  id: number;
  requesterId: number;
  providerId: number;
  skillId: number;
  skillTitle: string;
  description: string | null;
  durationHours: number;
  proposedDateTime: string; // ex : "2026-11-20T18:00:00"
  status: ExchangeStatus;
  createdAt: string;
  updatedAt: string;
}

/**
 * Données envoyées pour créer une demande (ExchangeRequest.java).
 * Le demandeur et le prestataire n'y figurent PAS : le backend les déduit.
 */
export interface ExchangeRequest {
  skillId: number;
  description: string;
  durationHours: number;
  proposedDateTime: string;
}
