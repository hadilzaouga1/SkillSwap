export interface ProfilPublic {
  id: number;
  nom: string;
  universite: string | null;
  filiere: string | null;
  noteMoyenne: number | null;
}
export interface MonProfil extends ProfilPublic {
  email: string;
}

export interface ProfilUpdateRequest {
  nom: string;
  universite: string;
  filiere: string;
}
