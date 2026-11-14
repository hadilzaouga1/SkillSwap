export type TypeTransaction = 'WELCOME' | 'RESERVATION' | 'RELEASE' | 'TRANSFER_OUT' | 'TRANSFER_IN';


export interface Solde {
  userId: number;
  balance: number;
  reserved: number;
  available: number;
}

export interface TransactionCredit {
  id: number;
  type: TypeTransaction;
  amount: number;
  balanceAfter: number;
  exchangeId: number | null;
  description: string;
  createdAt: string;
}
