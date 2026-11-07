/** Rôles : doivent correspondre à l'énumération Java "Role" (identity-service). */
export type Role = 'ETUDIANT' | 'MODERATEUR' | 'ADMIN';

/** Utilisateur connecté (informations non sensibles, jamais le mot de passe). */
export interface UtilisateurConnecte {
  id: number;
  nom: string;
  email: string;
  role: Role;
}

/** Réponse de /api/auth/connexion et /api/auth/inscription. */
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
