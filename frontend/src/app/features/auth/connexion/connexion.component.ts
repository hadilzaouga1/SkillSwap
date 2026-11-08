import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { messageErreur } from '../../../core/utils/erreur.util';

/** Page de connexion (US2). Route : /connexion */
@Component({
  selector: 'app-connexion',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './connexion.component.html',
  styleUrl: './connexion.component.scss'
})
export class ConnexionComponent {
  form: FormGroup;
  erreur = '';
  envoiEnCours = false;

  constructor(
    private fb: FormBuilder,
    private auth: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.form = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      motDePasse: ['', Validators.required]
    });
  }

  seConnecter(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.envoiEnCours = true;
    this.erreur = '';

    this.auth.connexion(this.form.value).subscribe({
      next: () => this.router.navigateByUrl(this.pageDeRetour()),
      error: (err) => {
        this.erreur = messageErreur(err);
        this.envoiEnCours = false;
      }
    });
  }

  /**
   * Page à ouvrir après la connexion : celle demandée avant la redirection (?retour=...),
   * sinon l'accueil. On n'accepte que des adresses internes (commençant par un seul "/").
   */
  private pageDeRetour(): string {
    const retour = this.route.snapshot.queryParamMap.get('retour');
    return retour && retour.startsWith('/') && !retour.startsWith('//') ? retour : '/';
  }

  invalide(champ: string): boolean {
    const c = this.form.get(champ);
    return !!c && c.invalid && c.touched;
  }
}
