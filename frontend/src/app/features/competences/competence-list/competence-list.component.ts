import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { LucideAngularModule, Search, ArrowRight } from 'lucide-angular'; // <-- LES ICÔNES ICI
import { CompetenceService } from '../../../core/services/competence.service';
import { CategorieService } from '../../../core/services/categorie.service';
import { UtilisateurService } from '../../../core/services/utilisateur.service';
import { AuthService } from '../../../core/services/auth.service';
import { Competence, FiltresCompetence, NIVEAUX } from '../../../core/models/competence.model';
import { Categorie } from '../../../core/models/categorie.model';
import { NiveauPipe } from '../../../shared/pipes/niveau.pipe';
import { messageErreur } from '../../../core/utils/erreur.util';

@Component({
  selector: 'app-competence-list',
  standalone: true,
  imports: [
    CommonModule, 
    FormsModule, 
    RouterLink, 
    NiveauPipe, 
    LucideAngularModule 
  ],
  templateUrl: './competence-list.component.html',
  styleUrl: './competence-list.component.scss'
})
export class CompetenceListComponent implements OnInit {
  competences: Competence[] = [];
  categories: Categorie[] = [];
  niveaux = NIVEAUX;

  readonly icons = { Search, ArrowRight };

  filtres: FiltresCompetence = { motCle: '', categorieId: null, niveau: '' };
  noms: Record<number, string> = {};
  loading = true;
  erreur = '';

  constructor(
    private competenceService: CompetenceService,
    private categorieService: CategorieService,
    private utilisateurService: UtilisateurService,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    this.categorieService.getCategories().subscribe({
      next: (data) => (this.categories = data),
      error: (err) => (this.erreur = messageErreur(err))
    });
    this.charger();
  }

  charger(): void {
    this.loading = true;
    this.erreur = '';
    this.competenceService.getCompetences(this.filtres).subscribe({
      next: (data) => {
        this.competences = data;
        this.loading = false;
        this.chargerNoms();
      },
      error: (err) => {
        this.erreur = messageErreur(err);
        this.loading = false;
      }
    });
  }

  reinitialiser(): void {
    this.filtres = { motCle: '', categorieId: null, niveau: '' };
    this.charger();
  }

  changerCategorie(id: number | null): void {
    this.filtres.categorieId = id;
    this.charger();
  }

  changerNiveau(niveau: FiltresCompetence['niveau']): void {
    this.filtres.niveau = niveau;
    this.charger();
  }

  idEtudiant(c: Competence): number | undefined {
    return c.etudiant?.id ?? c.etudiantId;
  }

  peutDemander(c: Competence): boolean {
    return this.auth.estConnecte && this.idEtudiant(c) !== this.auth.utilisateur?.id;
  }

  nomEtudiant(c: Competence): string {
    if (c.etudiant?.nom) {
      return c.etudiant.nom;
    }
    const id = c.etudiantId;
    return id !== undefined && this.noms[id] ? this.noms[id] : 'Étudiant';
  }

  private chargerNoms(): void {
    const manquants = new Set<number>();
    for (const c of this.competences) {
      if (!c.etudiant && c.etudiantId !== undefined && this.noms[c.etudiantId] === undefined) {
        manquants.add(c.etudiantId);
      }
    }
    manquants.forEach((id) => {
      this.noms[id] = '…';
      this.utilisateurService.getProfilPublic(id).subscribe({
        next: (profil) => (this.noms[id] = profil.nom),
        error: () => (this.noms[id] = 'Étudiant')
      });
    });
  }
}