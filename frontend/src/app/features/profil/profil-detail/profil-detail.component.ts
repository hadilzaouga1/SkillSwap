import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { UtilisateurService } from '../../../core/services/utilisateur.service';
import { CompetenceService } from '../../../core/services/competence.service';
import { ProfilPublic } from '../../../core/models/profil.model';
import { Competence } from '../../../core/models/competence.model';
import { NiveauPipe } from '../../../shared/pipes/niveau.pipe';
import { messageErreur } from '../../../core/utils/erreur.util';

/** Profil public d'un étudiant et ses compétences (US8). Route : /profil/:id */
@Component({
  selector: 'app-profil-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, NiveauPipe],
  templateUrl: './profil-detail.component.html',
  styleUrl: './profil-detail.component.scss'
})
export class ProfilDetailComponent implements OnInit {
  profil: ProfilPublic | null = null;
  competences: Competence[] = [];
  erreur = '';

  constructor(
    private route: ActivatedRoute,
    private utilisateurService: UtilisateurService,
    private competenceService: CompetenceService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.utilisateurService.getProfilPublic(id).subscribe({
      next: (data) => (this.profil = data),
      error: (err) => (this.erreur = messageErreur(err))
    });

    // On réutilise la recherche du catalogue avec le filtre "etudiantId"
    this.competenceService.getCompetences({ etudiantId: id }).subscribe({
      next: (data) => (this.competences = data),
      error: (err) => (this.erreur = messageErreur(err))
    });
  }
}
