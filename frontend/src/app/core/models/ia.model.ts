export interface IaCompetence { id: number; titre: string; niveau: string | null; categorie: string | null; etudiantId: number | null; }

export interface IaChatReponse { reply: string; suggestions: string[]; skills: IaCompetence[]; provider: string; }

export interface IaMessage { role: 'user' | 'assistant'; content: string; skills?: IaCompetence[]; }

/** Brouillon de fiche de séance généré par l'agent IA (mêmes champs que la fiche). */
export interface IaBrouillonSeance {
  title: string; summary?: string | null; description: string; objectives: string[];
  outline: { title: string; minutes: number }[]; prerequisites?: string | null; audience?: string | null;
  level?: 'DEBUTANT' | 'INTERMEDIAIRE' | 'AVANCE' | null; format?: string | null; mode?: string | null;
  deliveryNotes?: string | null; materials?: string | null; language?: string | null;
}
