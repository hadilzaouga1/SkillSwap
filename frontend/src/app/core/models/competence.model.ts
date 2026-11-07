export type Niveau = 'DEBUTANT' | 'INTERMEDIAIRE' | 'AVANCE';

/** Liste utilisée pour remplir les menus déroulants (valeur envoyée à l'API + texte affiché). */
export const NIVEAUX: { valeur: Niveau; libelle: string }[] = [
  { valeur: 'DEBUTANT', libelle: 'Débutant' },
  { valeur: 'INTERMEDIAIRE', libelle: 'Intermédiaire' },
  { valeur: 'AVANCE', libelle: 'Avancé' }
];

export type StatutValidation = 'EN_ATTENTE' | 'VALIDEE' | 'REFUSEE';

/** Compétence telle que renvoyée par l'API (CompetenceResponse.java). */
export interface Competence {
  id: number;
  titre: string;
  description: string;
  niveau: Niveau;
  disponibilites: string | null;
  statutValidation: StatutValidation;
  categorie: { id: number; nom: string };
  etudiant: { id: number; nom: string };
}

/** Données envoyées pour publier / modifier une compétence (CompetenceRequest.java). */
export interface CompetenceRequest {
  titre: string;
  description: string;
  niveau: Niveau;
  categorieId: number;
  disponibilites: string;
}

/** Critères de recherche du catalogue (tous optionnels). */
export interface FiltresCompetence {
  motCle?: string;
  categorieId?: number | null;
  niveau?: Niveau | '';
  etudiantId?: number | null;
}
