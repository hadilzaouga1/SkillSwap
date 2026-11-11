import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ModerationService } from '../../../core/services/moderation.service';
import { Competence } from '../../../core/models/competence.model';
import { NiveauPipe } from '../../../shared/pipes/niveau.pipe';
import { messageErreur } from '../../../core/utils/erreur.util';

/**
 * Page du modérateur : compétences en attente de validation (US20).
 * Route : /moderation/competences (protégée par roleGuard + par la Gateway + par le skill-service).
 */
@Component({
  selector: 'app-competences-a-valider',
  standalone: true,
  imports: [CommonModule, NiveauPipe],
  templateUrl: './competences-a-valider.component.html',
  styleUrl: './competences-a-valider.component.scss'
})
export class CompetencesAValiderComponent implements OnInit {
  competences: Competence[] = [];
  loading = true;
  erreur = '';
  succes = '';

  constructor(private moderationService: ModerationService) {}

  ngOnInit(): void {
    this.moderationService.getAValider().subscribe({
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

  valider(c: Competence): void {
  this.moderationService.valider(c.id).subscribe({
    next: () => {
      this.competences = this.competences.filter(x => x.id !== c.id);
      this.succes = `« ${c.titre} » a été validée.`;
    },
    error: (err) => (this.erreur = messageErreur(err))
  });
}

  refuser(c: Competence): void {
    this.moderationService.refuser(c.id).subscribe({
      next: () => this.retirer(c, 'refusée.'),
      error: (err) => (this.erreur = messageErreur(err))
    });
  }

  /** Retire la carte de la liste et affiche un message de confirmation. */
  private retirer(c: Competence, message: string): void {
    this.competences = this.competences.filter((x) => x.id !== c.id);
    this.erreur = '';
    this.succes = `« ${c.titre} » ${message}`;
  }
}
