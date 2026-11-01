import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UtilisateurService } from '../../../core/services/utilisateur.service';
import { ProfilUpdateRequest } from '../../../core/models/profil.model';
import { messageErreur } from '../../../core/utils/erreur.util';

/** Modification de mon profil (US3). Route : /profil/moi */
@Component({
  selector: 'app-mon-profil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './mon-profil.component.html',
  styleUrl: './mon-profil.component.scss'
})
export class MonProfilComponent implements OnInit {
  form: FormGroup;
  email = '';        // affiché en lecture seule
  erreur = '';
  succes = '';
  envoiEnCours = false;

  constructor(private fb: FormBuilder, private utilisateurService: UtilisateurService) {
    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.maxLength(80)]],
      universite: ['', Validators.maxLength(120)],
      filiere: ['', Validators.maxLength(120)]
    });
  }

  ngOnInit(): void {
    this.utilisateurService.getMonProfil().subscribe({
      next: (p) => {
        this.email = p.email;
        this.form.patchValue({
          nom: p.nom,
          universite: p.universite ?? '',
          filiere: p.filiere ?? ''
        });
      },
      error: (err) => (this.erreur = messageErreur(err))
    });
  }

  enregistrer(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.envoiEnCours = true;
    this.erreur = '';
    this.succes = '';

    this.utilisateurService.modifierMonProfil(this.form.value as ProfilUpdateRequest).subscribe({
      next: () => {
        this.succes = 'Profil mis à jour.';
        this.envoiEnCours = false;
      },
      error: (err) => {
        this.erreur = messageErreur(err);
        this.envoiEnCours = false;
      }
    });
  }
}
