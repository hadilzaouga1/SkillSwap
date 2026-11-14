export type Niveau = 'DEBUTANT' | 'INTERMEDIAIRE' | 'AVANCE';

export const NIVEAUX: { valeur: Niveau; libelle: string }[] = [
  { valeur: 'DEBUTANT', libelle: 'Débutant' },
  { valeur: 'INTERMEDIAIRE', libelle: 'Intermédiaire' },
  { valeur: 'AVANCE', libelle: 'Avancé' }
];

export type StatutValidation = 'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE';

export interface Competence {
  id: number;
  titre: string;
  description: string;
  niveau: Niveau;
  disponibilites: string | null;
  statutValidation: StatutValidation;
  categorie: { id: number; nom: string };
  etudiantId?: number;
  etudiant?: { id: number; nom: string };
}

export interface CompetenceRequest {
  titre: string;
  description: string;
  niveau: Niveau;
  categorieId: number;
  disponibilites: string;
}

export interface FiltresCompetence {
  motCle?: string;
  categorieId?: number | null;
  niveau?: Niveau | '';
  etudiantId?: number | null;
}
