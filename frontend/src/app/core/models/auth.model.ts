export type Role = 'ETUDIANT' | 'MODERATEUR' | 'ADMIN';

export interface UtilisateurConnecte {
  id: number;
  nom: string;
  email: string;
  role: Role;
}

export interface AuthResponse {
  token: string;
  utilisateur: UtilisateurConnecte;
}

export interface ConnexionRequest {
  email: string;
  motDePasse: string;
}

export interface InscriptionRequest extends ConnexionRequest {
  nom: string;
  universite: string;
  filiere: string;
}
