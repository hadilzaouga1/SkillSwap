export type SeanceFormat = 'COURS' | 'ATELIER' | 'TUTORAT' | 'REVISION' | 'PROJET';
export type SeanceMode = 'ONLINE' | 'IN_PERSON' | 'HYBRID';
export type SeanceRole = 'PROVIDER' | 'REQUESTER' | 'MODERATOR';

export const FORMAT_LIBELLES: Record<SeanceFormat, string> = {
  COURS: 'Cours', ATELIER: 'Atelier pratique', TUTORAT: 'Tutorat individuel',
  REVISION: 'Séance de révision', PROJET: 'Travail sur projet'
};
export const MODE_LIBELLES: Record<SeanceMode, string> = {
  ONLINE: 'En ligne', IN_PERSON: 'En présentiel', HYBRID: 'Hybride'
};

export interface EtapeProgramme { title: string; description?: string | null; minutes?: number | null; }

/** Contenu décrit par le prestataire (miroir de sessionSchema côté session-service). */
export interface SeanceContenu {
  title: string;
  summary: string | null;
  description: string | null;
  objectives: string[];
  outline: EtapeProgramme[];
  prerequisites: string | null;
  audience: string | null;
  level: 'DEBUTANT' | 'INTERMEDIAIRE' | 'AVANCE' | null;
  language: string | null;
  format: SeanceFormat | null;
  mode: SeanceMode | null;
  platform: string | null;
  meetingLink: string | null;
  location: string | null;
  deliveryNotes: string | null;
  materials: string | null;
  updatedAt?: string | null;
}

export interface SupportSeance {
  id: number;
  kind: 'PDF' | 'LINK';
  title: string;
  description: string | null;
  url: string | null;
  host: string | null;
  fileName: string | null;
  size: number | null;
  downloadPath: string | null;
  addedBy: number;
  createdAt: string;
}

export interface SeanceReponse {
  exists: boolean;
  role: SeanceRole;
  canEdit: boolean;
  canAddResources: boolean;
  exchange: { id: number; skillTitle?: string | null; providerId: number; requesterId: number;
              durationHours?: number | null; proposedDateTime?: string | null; status?: string | null };
  completeness: { percent: number; missing: string[] };
  session: SeanceContenu;
  resources: SupportSeance[];
}

export interface ResumeSeance { exists: boolean; percent: number; resources: number; mode: SeanceMode | null; }
