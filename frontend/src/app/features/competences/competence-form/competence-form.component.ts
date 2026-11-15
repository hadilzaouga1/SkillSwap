import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CompetenceService } from '../../../core/services/competence.service';
import { IaService } from '../../../core/services/ia.service';
import { CategorieService } from '../../../core/services/categorie.service';
import { Categorie } from '../../../core/models/categorie.model';
import { CompetenceRequest, NIVEAUX } from '../../../core/models/competence.model';
import { messageErreur } from '../../../core/utils/erreur.util';

/**
 * Formulaire unique pour :
 *  - PUBLIER une compétence   -> route /competences/nouvelle        (US5)
 *  - MODIFIER une compétence  -> route /competences/:id/modifier    (US6)
 * Le mode est déterminé par la présence du paramètre :id dans l'URL.
 *
 * Fonctionnalités :
 *  - assistance IA pour rédiger / améliorer la description ;
 *  - ajout optionnel d'un document PDF (10 Mo maximum).
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

  // Assistance IA
  iaEnCours = false;
  iaConseils: string[] = [];

  // Document PDF facultatif
  selectedFile: File | null = null;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private competenceService: CompetenceService,
    private categorieService: CategorieService,
    private ia: IaService
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
            categorieId: c.categorie?.id ?? null,
            niveau: c.niveau,
            description: c.description,
            disponibilites: c.disponibilites ?? ''
          }),
        error: (err) => (this.erreur = messageErreur(err))
      });
    }
  }

  /** Assistance IA : rédiger (si la description est vide) ou améliorer (sinon) la description. */
  assisterDescription(): void {
    const titre = (this.form.value.titre || '').trim();
    const texte = (this.form.value.description || '').trim();

    if (titre.length < 2 && texte.length < 3) {
      this.erreur = "Saisissez d'abord un titre ou un début de description.";
      return;
    }

    this.iaEnCours = true;
    this.erreur = '';

    if (texte.length >= 3) {
      // Une description existe déjà : l'IA l'améliore
      this.ia.ameliorer(texte, 'skill').subscribe({
        next: (r) => {
          this.form.patchValue({ description: r.text });
          this.iaConseils = r.tips;
          this.iaEnCours = false;
        },
        error: (e) => {
          this.erreur = messageErreur(e);
          this.iaEnCours = false;
        }
      });
    } else {
      // Pas de description : l'IA propose un brouillon à partir du titre
      this.ia.brouillonCompetence(titre, this.form.value.niveau).subscribe({
        next: (r) => {
          this.form.patchValue({ description: r.description });

          if (!this.form.value.disponibilites && r.disponibilites) {
            this.form.patchValue({ disponibilites: r.disponibilites });
          }

          this.iaConseils = [];
          this.iaEnCours = false;
        },
        error: (e) => {
          this.erreur = messageErreur(e);
          this.iaEnCours = false;
        }
      });
    }
  }

  /** Sélection d'un document PDF (facultatif, 10 Mo maximum). */
  choisirPdfCompetence(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];

    if (!file) {
      return;
    }

    if (file.type !== 'application/pdf') {
      this.erreur = 'Veuillez sélectionner un fichier PDF valide.';
      this.selectedFile = null;
      input.value = '';
      return;
    }

    if (file.size > 10 * 1024 * 1024) {
      this.erreur = 'Le fichier ne doit pas dépasser 10 Mo.';
      this.selectedFile = null;
      input.value = '';
      return;
    }

    this.selectedFile = file;
    this.erreur = '';
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

    // Sans PDF : JSON classique. Avec PDF : requête multipart (partie "competence" + partie "file").
    const corps: CompetenceRequest | FormData = this.selectedFile
      ? this.construireFormData(data, this.selectedFile)
      : data;

    const appel = this.competenceId !== null
      ? this.competenceService.modifier(this.competenceId, corps)
      : this.competenceService.creer(corps);

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

  /** Construit la requête multipart envoyée quand un PDF est joint. */
  private construireFormData(data: CompetenceRequest, fichier: File): FormData {
    const formData = new FormData();
    formData.append('competence', new Blob([JSON.stringify(data)], { type: 'application/json' }));
    formData.append('file', fichier);
    return formData;
  }
}