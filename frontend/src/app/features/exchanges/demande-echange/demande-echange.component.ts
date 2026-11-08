import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CompetenceService } from '../../../core/services/competence.service';
import { UtilisateurService } from '../../../core/services/utilisateur.service';
import { ExchangeService } from '../../../core/services/exchange.service';
import { AuthService } from '../../../core/services/auth.service';
import { Competence } from '../../../core/models/competence.model';
import { NiveauPipe } from '../../../shared/pipes/niveau.pipe';
import { messageErreur } from '../../../core/utils/erreur.util';
import { estDansLeFutur, maintenantPourChamp, versFormatApi } from '../../../core/utils/date.util';

/**
 * Formulaire "Demander un échange" pour une compétence du catalogue.
 * Route : /echanges/nouveau/:skillId (réservée aux personnes connectées).
 * Le prestataire n'est pas envoyé : le backend le déduit de la compétence.
 */
@Component({
  selector: 'app-demande-echange',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, NiveauPipe],
  templateUrl: './demande-echange.component.html',
  styleUrl: './demande-echange.component.scss'
})
export class DemandeEchangeComponent implements OnInit {
  competence: Competence | null = null;
  nomPrestataire = '';

  form = { description: '', durationHours: 1, proposedDateTime: '' };
  minDate = maintenantPourChamp();

  loading = true;
  envoiEnCours = false;
  erreur = '';
  dateInvalide = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private competenceService: CompetenceService,
    private utilisateurService: UtilisateurService,
    private exchangeService: ExchangeService,
    private auth: AuthService
  ) {}

  ngOnInit(): void {
    const skillId = Number(this.route.snapshot.paramMap.get('skillId'));
    if (!skillId) {
      this.erreur = 'Compétence invalide.';
      this.loading = false;
      return;
    }

    this.competenceService.getCompetence(skillId).subscribe({
      next: (c) => {
        this.competence = c;
        this.loading = false;
        const idPrestataire = c.etudiant?.id ?? c.etudiantId;
        if (idPrestataire === this.auth.utilisateur?.id) {
          this.erreur = 'Vous ne pouvez pas demander un échange sur votre propre compétence.';
        }
        if (idPrestataire !== undefined) {
          this.utilisateurService.getProfilPublic(idPrestataire).subscribe({
            next: (p) => (this.nomPrestataire = p.nom),
            error: () => (this.nomPrestataire = '')
          });
        }
      },
      error: (err) => {
        this.erreur = messageErreur(err);
        this.loading = false;
      }
    });
  }

  get estMaCompetence(): boolean {
    const id = this.competence?.etudiant?.id ?? this.competence?.etudiantId;
    return id !== undefined && id === this.auth.utilisateur?.id;
  }

  envoyer(): void {
    this.erreur = '';
    this.dateInvalide = !estDansLeFutur(this.form.proposedDateTime);
    if (!this.competence || this.dateInvalide || this.estMaCompetence) {
      return;
    }

    this.envoiEnCours = true;
    this.exchangeService
      .creer({
        skillId: this.competence.id,
        description: this.form.description.trim(),
        durationHours: this.form.durationHours,
        proposedDateTime: versFormatApi(this.form.proposedDateTime)
      })
      .subscribe({
        next: () => this.router.navigate(['/echanges'], { queryParams: { onglet: 'envoyees', cree: 1 } }),
        error: (err) => {
          this.erreur = messageErreur(err);
          this.envoiEnCours = false;
        }
      });
  }
}
