import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/services/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent {
  title = 'skillswap-web';

  constructor(public auth: AuthService) { }

  /** Texte affiché à côté du nom de l'utilisateur. */
  get libelleRole(): string {
    switch (this.auth.role) {
      case 'ADMIN': return 'Administrateur';
      case 'MODERATEUR': return 'Modérateur';
      default: return 'Étudiant';
    }
  }
}
