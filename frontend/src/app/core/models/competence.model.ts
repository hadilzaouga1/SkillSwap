export type Niveau = 'DEBUTANT' | 'INTERMEDIAIRE' | 'AVANCE';

export const NIVEAUX: { valeur: Niveau; libelle: string }[] = [
  { valeur: 'DEBUTANT', libelle: 'Débutant' },
  { valeur: 'INTERMEDIAIRE', libelle: 'Intermédiaire' },
  { valeur: 'AVANCE', libelle: 'Avancé' }
];

export type StatutValidation = 'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE';

/** Catégorie renvoyée dans une compétence par le skill-service. */
export interface CategorieInfo {
  id: number;
  nom: string;
}

/** Étudiant propriétaire d'une compétence (facultatif côté réponse). */
export interface EtudiantInfo {
  id: number;
  nom: string;
}

export interface Competence {
  id: number;
  titre: string;
  description?: string;
  niveau: string;
  categorieId?: number;
  disponibilites?: string;
  statutValidation: StatutValidation;
  categorie: CategorieInfo;
  etudiantId?: number;
  etudiant?: EtudiantInfo;
  documentUrl?: string;
  documentName?: string;
  documentSize?: number;
}

/** Filtres acceptés par CompetenceService.getCompetences(...). */
export interface FiltresCompetence {
  motCle?: string;
  categorieId?: number | null;
  niveau?: Niveau | '';
  etudiantId?: number;
}

export interface CompetenceRequest {
  titre: string;
  description?: string;
  niveau: Niveau;
  categorieId: number;
  disponibilites?: string;
}