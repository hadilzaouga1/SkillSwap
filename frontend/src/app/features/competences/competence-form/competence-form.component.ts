import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CompetenceService } from '../../../core/services/competence.service';
import { CategorieService } from '../../../core/services/categorie.service';
import { Categorie } from '../../../core/models/categorie.model';
import { CompetenceRequest, NIVEAUX } from '../../../core/models/competence.model';
import { messageErreur } from '../../../core/utils/erreur.util';

/**
 * Formulaire unique pour :
 *  - PUBLIER une compétence   -> route /competences/nouvelle        (US5)
 *  - MODIFIER une compétence  -> route /competences/:id/modifier    (US6)
 * Le mode est déterminé par la présence du paramètre :id dans l'URL.
 */
@Component({
  selector: 'app-competence-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './competence-form.component.html',
  styleUrl: './competence-form.component.scss'
})
export class CompetenceFormComponent implements OnInit {
  form: FormGroup;
  categories: Categorie[] = [];
  niveaux = NIVEAUX;

  competenceId: number | null = null; // null = mode création
  envoiEnCours = false;
  erreur = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private competenceService: CompetenceService,
    private categorieService: CategorieService
  ) {
    // Règles de validation (mêmes limites que CompetenceRequest.java côté backend)
    this.form = this.fb.group({
      titre: ['', [Validators.required, Validators.maxLength(120)]],
      categorieId: [null, Validators.required],
      niveau: [null, Validators.required],
      description: ['', Validators.maxLength(2000)],
      disponibilites: ['', Validators.maxLength(255)]
    });
  }

  get modeModification(): boolean {
    return this.competenceId !== null;
  }

  ngOnInit(): void {
    this.categorieService.getCategories().subscribe({
      next: (data) => (this.categories = data),
      error: (err) => (this.erreur = messageErreur(err))
    });

    // Mode modification : on charge la compétence et on pré-remplit le formulaire
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.competenceId = Number(id);
      this.competenceService.getCompetence(this.competenceId).subscribe({
        next: (c) =>
          this.form.patchValue({
            titre: c.titre,
            categorieId: c.categorie.id,
            niveau: c.niveau,
            description: c.description,
            disponibilites: c.disponibilites ?? ''
          }),
        error: (err) => (this.erreur = messageErreur(err))
      });
    }
  }

  /** Envoie le formulaire : création (POST) ou modification (PUT). */
  enregistrer(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched(); // affiche les messages d'erreur
      return;
    }

    this.envoiEnCours = true;
    this.erreur = '';
    const data = this.form.value as CompetenceRequest;

    const appel = this.competenceId !== null
      ? this.competenceService.modifier(this.competenceId, data)
      : this.competenceService.creer(data);

    appel.subscribe({
      next: () => this.router.navigate(['/mes-competences']),
      error: (err) => {
        this.erreur = messageErreur(err);
        this.envoiEnCours = false;
      }
    });
  }

  /** Raccourci pour le template : le champ est-il invalide ET touché ? */
  invalide(champ: string): boolean {
    const c = this.form.get(champ);
    return !!c && c.invalid && c.touched;
  }
}
