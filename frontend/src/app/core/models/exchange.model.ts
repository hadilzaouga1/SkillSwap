export type ExchangeStatus =
  | 'PENDING'
  | 'TIME_PROPOSED'
  | 'ACCEPTED'
  | 'AWAITING_CONFIRMATION'
  | 'COMPLETED'
  | 'REJECTED'
  | 'CANCELLED';

export const STATUT_LIBELLES: Record<ExchangeStatus, string> = {
  PENDING: 'En attente de réponse',
  TIME_PROPOSED: 'Nouveau créneau proposé',
  ACCEPTED: 'Acceptée',
  AWAITING_CONFIRMATION: 'En attente de confirmation',
  COMPLETED: 'Terminée',
  REJECTED: 'Refusée',
  CANCELLED: 'Annulée'
};

export interface Exchange {
  id: number;
  requesterId: number;
  providerId: number;
  skillId: number;
  skillTitle: string;
  description: string | null;
  durationHours: number;
  proposedDateTime: string;
  status: ExchangeStatus;
  meetingLink: string | null; // <-- AJOUT ICI
  createdAt: string;
  updatedAt: string;
}

export interface ExchangeRequest {
  skillId: number;
  description: string;
  durationHours: number;
  proposedDateTime: string;
}
