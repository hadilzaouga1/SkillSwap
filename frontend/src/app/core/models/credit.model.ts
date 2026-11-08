/** Nature d'un mouvement : doit correspondre à l'énumération Java "TransactionType" (credit-service). */
export type TypeTransaction = 'WELCOME' | 'RESERVATION' | 'RELEASE' | 'TRANSFER_OUT' | 'TRANSFER_IN';

/**
 * Solde d'un utilisateur (BalanceResponse.java).
 *  - balance   : crédits possédés
 *  - reserved  : crédits bloqués pour des échanges acceptés
 *  - available : crédits utilisables (balance - reserved)
 */
export interface Solde {
  userId: number;
  balance: number;
  reserved: number;
  available: number;
}

/** Une ligne de l'historique (TransactionResponse.java). */
export interface TransactionCredit {
  id: number;
  type: TypeTransaction;
  amount: number;
  balanceAfter: number;
  exchangeId: number | null;
  description: string;
  createdAt: string;
}
