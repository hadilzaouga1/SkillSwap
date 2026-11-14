export interface Notification {
  id: number; type: string; title: string; message: string; link: string | null;
  exchangeId: number | null; read: boolean; createdAt: string;
}
export interface NotificationListe { unreadCount: number; items: Notification[]; }

export interface Avis { id: number; exchangeId: number; rating: number; comment: string | null; skillTitle: string | null; createdAt: string; }
export interface AvisEchange { canReview: boolean; status: string; mine: Avis | null; theirs: Avis | null; }
export interface Reputation {
  userId: number; average: number; count: number; distribution: Record<string, number>;
  badge: { code: string; label: string } | null; reviews: Avis[];
}
