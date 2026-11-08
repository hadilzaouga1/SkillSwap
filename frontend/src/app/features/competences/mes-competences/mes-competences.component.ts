import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CompetenceService } from '../../../core/services/competence.service';
import { Competence, StatutValidation } from '../../../core/models/competence.model';
import { NiveauPipe } from '../../../shared/pipes/niveau.pipe';
import { messageErreur } from '../../../core/utils/erreur.util';

/** Page "Mes compétences" : liste, modification et suppression de ses propres compétences (US6). */
@Component({
  selector: 'app-mes-competences',
  standalone: true,
  imports: [CommonModule, RouterLink, NiveauPipe],
  templateUrl: './mes-competences.component.html',
  styleUrl: './mes-competences.component.scss'
})
export class MesCompetencesComponent implements OnInit {
  competences: Competence[] = [];
  loading = true;
  erreur = '';

  constructor(private competenceService: CompetenceService) {}

  ngOnInit(): void {
    this.competenceService.getMesCompetences().subscribe({
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

  supprimer(c: Competence): void {
    if (!confirm(`Supprimer la compétence « ${c.titre} » ?`)) {
      return;
    }
    this.competenceService.supprimer(c.id).subscribe({
      // On retire la carte de la liste sans recharger la page
      next: () => (this.competences = this.competences.filter((x) => x.id !== c.id)),
      error: (err) => (this.erreur = messageErreur(err))
    });
  }

  libelleStatut(statut: StatutValidation): string {
    switch (statut) {
      case 'VALIDEE': return 'Validée';
      case 'REFUSEE': return 'Refusée';
      default: return 'En attente de validation';
    }
  }
}
