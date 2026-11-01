import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { CompetenceService } from '../../../core/services/competence.service';
import { CategorieService } from '../../../core/services/categorie.service';
import { Competence, FiltresCompetence, NIVEAUX } from '../../../core/models/competence.model';
import { Categorie } from '../../../core/models/categorie.model';
import { NiveauPipe } from '../../../shared/pipes/niveau.pipe';
import { messageErreur } from '../../../core/utils/erreur.util';

/**
 * Page d'accueil : catalogue des compétences avec recherche (US7).
 * Les filtres sont envoyés au backend (GET /api/competences?motCle=...&categorieId=...&niveau=...).
 */
@Component({
  selector: 'app-competence-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NiveauPipe],
  templateUrl: './competence-list.component.html',
  styleUrl: './competence-list.component.scss'
})
export class CompetenceListComponent implements OnInit {
  competences: Competence[] = [];
  categories: Categorie[] = [];
  niveaux = NIVEAUX;

  // Valeurs saisies dans la barre de recherche
  filtres: FiltresCompetence = { motCle: '', categorieId: null, niveau: '' };

  loading = true;
  erreur = '';

  constructor(
    private competenceService: CompetenceService,
    private categorieService: CategorieService
  ) {}

  ngOnInit(): void {
    // Les catégories alimentent le menu déroulant des filtres
    this.categorieService.getCategories().subscribe({
      next: (data) => (this.categories = data),
      error: (err) => (this.erreur = messageErreur(err))
    });
    this.charger();
  }

  /** Appelle l'API avec les filtres actuels et met à jour la liste. */
  charger(): void {
    this.loading = true;
    this.erreur = '';
    this.competenceService.getCompetences(this.filtres).subscribe({
      next: (data) => {
        this.competences = data;
        this.loading = false;
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

  /** Mise à jour d'un filtre déroulant puis relance de la recherche. */
  changerCategorie(id: number | null): void {
    this.filtres.categorieId = id;
    this.charger();
  }

  changerNiveau(niveau: FiltresCompetence['niveau']): void {
    this.filtres.niveau = niveau;
    this.charger();
  }
}
