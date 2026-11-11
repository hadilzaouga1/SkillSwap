import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, Validators
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { InscriptionRequest } from '../../../core/models/auth.model';
import { messageErreur } from '../../../core/utils/erreur.util';

function motsDePasseIdentiques(groupe: AbstractControl): ValidationErrors | null {
  const mdp = groupe.get('motDePasse')?.value;
  const confirmation = groupe.get('confirmation')?.value;
  return mdp === confirmation ? null : { motsDifferents: true };
}

@Component({
  selector: 'app-inscription',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './inscription.component.html',
  styleUrl: './inscription.component.scss'
})
export class InscriptionComponent {
  form: FormGroup;
  erreur = '';
  envoiEnCours = false;
  
  // NOUVELLE VARIABLE : pour afficher l'alerte globale
  alerteSaisie = false; 

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {
    this.form = this.fb.group(
      {
        nom: ['', [Validators.required, Validators.maxLength(80)]],
        email: ['', [Validators.required, Validators.email, Validators.maxLength(120)]],
        motDePasse: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
        confirmation: ['', Validators.required],
        universite: ['', Validators.maxLength(120)],
        filiere: ['', Validators.maxLength(120)]
      },
      { validators: motsDePasseIdentiques }
    );
  }

  sInscrire(): void {
    if (this.form.invalid) {
      // Le formulaire est invalide : on affiche l'alerte et on "touche" tous les champs
      this.alerteSaisie = true;
      this.form.markAllAsTouched();
      return; // ON ARRÊTE ICI, la requête n'est pas envoyée
    }

    this.envoiEnCours = true;
    this.erreur = '';
    this.alerteSaisie = false; // On cache l'alerte si tout va bien

    const { nom, email, motDePasse, universite, filiere } = this.form.value;
    const data: InscriptionRequest = { nom, email, motDePasse, universite, filiere };

    this.auth.inscription(data).subscribe({
      next: () => this.router.navigate(['/']),
      error: (err) => {
        this.erreur = messageErreur(err);
        this.envoiEnCours = false;
      }
    });
  }

  invalide(champ: string): boolean {
    const c = this.form.get(champ);
    return !!c && c.invalid && c.touched;
  }
}