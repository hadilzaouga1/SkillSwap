import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { SessionService } from './core/services/session.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'skillswap-web';

  constructor(public session: SessionService) { }

  /**
   * TEMPORAIRE (avant l'authentification) : change l'étudiant simulé
   * puis recharge la page pour rafraîchir toutes les données.
   */
  changerUtilisateur(id: number): void {
    this.session.userId = id;
    window.location.reload();
  }
}
