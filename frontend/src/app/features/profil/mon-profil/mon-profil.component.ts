import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { UtilisateurService } from '../../../core/services/utilisateur.service';
import { MonProfil, ProfilUpdateRequest } from '../../../core/models/profil.model';
import { messageErreur } from '../../../core/utils/erreur.util';

@Component({
  selector: 'app-mon-profil',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './mon-profil.component.html',
  styleUrl: './mon-profil.component.scss'
})
export class MonProfilComponent implements OnInit {
  // On déclare la variable "profil" qui manquait
  profil: MonProfil | null = null;
  form: FormGroup;
  envoiEnCours = false;
  erreur = '';
  succes = '';

  constructor(
    private fb: FormBuilder,
    private utilisateurService: UtilisateurService
  ) {
    this.form = this.fb.group({
      nom: ['', [Validators.required, Validators.maxLength(80)]],
      universite: ['', [Validators.maxLength(120)]],
      filiere: ['', [Validators.maxLength(120)]]
    });
  }

  ngOnInit(): void {
    this.utilisateurService.getMonProfil().subscribe({
      next: (data) => {
        this.profil = data;
        this.form.patchValue({
          nom: data.nom,
          universite: data.universite || '',
          filiere: data.filiere || ''
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

    const data: ProfilUpdateRequest = this.form.value;

    this.utilisateurService.modifierMonProfil(data).subscribe({
      next: (maj) => {
        this.profil = maj;
        this.succes = 'Profil mis à jour avec succès !';
        this.envoiEnCours = false;
      },
      error: (err) => {
        this.erreur = messageErreur(err);
        this.envoiEnCours = false;
      }
    });
  }
}