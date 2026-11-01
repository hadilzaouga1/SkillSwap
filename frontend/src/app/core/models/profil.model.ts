/** Profil visible par les autres étudiants (sans email). */
export interface ProfilPublic {
  id: number;
  nom: string;
  universite: string | null;
  filiere: string | null;
  noteMoyenne: number | null;
}

/** Mon profil (avec email). */
export interface MonProfil extends ProfilPublic {
  email: string;
}

/** Champs modifiables du profil  */
export interface ProfilUpdateRequest {
  nom: string;
  universite: string;
  filiere: string;
}
