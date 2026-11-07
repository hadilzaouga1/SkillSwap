import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { CompetenceListComponent } from './features/competences/competence-list/competence-list.component';
import { CompetenceFormComponent } from './features/competences/competence-form/competence-form.component';
import { MesCompetencesComponent } from './features/competences/mes-competences/mes-competences.component';
import { ProfilDetailComponent } from './features/profil/profil-detail/profil-detail.component';
import { MonProfilComponent } from './features/profil/mon-profil/mon-profil.component';
import { ConnexionComponent } from './features/auth/connexion/connexion.component';
import { InscriptionComponent } from './features/auth/inscription/inscription.component';
import { CompetencesAValiderComponent } from './features/moderation/competences-a-valider/competences-a-valider.component';

/**
 * Table des routes.
 *  - Pages PUBLIQUES   : catalogue, profil public, connexion, inscription
 *  - authGuard         : réservé aux personnes connectées
 *  - roleGuard(...)    : réservé à certains rôles
 * ATTENTION à l'ordre : les routes précises ('profil/moi') AVANT les routes à paramètre ('profil/:id').
 */
export const routes: Routes = [
  { path: '', component: CompetenceListComponent },
  { path: 'connexion', component: ConnexionComponent },
  { path: 'inscription', component: InscriptionComponent },

  { path: 'competences/nouvelle', component: CompetenceFormComponent, canActivate: [authGuard] },
  { path: 'competences/:id/modifier', component: CompetenceFormComponent, canActivate: [authGuard] },
  { path: 'mes-competences', component: MesCompetencesComponent, canActivate: [authGuard] },
  { path: 'profil/moi', component: MonProfilComponent, canActivate: [authGuard] },
  { path: 'profil/:id', component: ProfilDetailComponent },

  { path: 'moderation/competences', component: CompetencesAValiderComponent,
    canActivate: [roleGuard('MODERATEUR', 'ADMIN')] },

  { path: '**', redirectTo: '' }
];
