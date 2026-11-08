import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

// Importation de tous vos composants
import { authGuard, roleGuard } from './core/guards/auth.guard';
import { CompetenceListComponent } from './features/competences/competence-list/competence-list.component';
import { CompetenceFormComponent } from './features/competences/competence-form/competence-form.component';
import { MesCompetencesComponent } from './features/competences/mes-competences/mes-competences.component';
import { ProfilDetailComponent } from './features/profil/profil-detail/profil-detail.component';
import { MonProfilComponent } from './features/profil/mon-profil/mon-profil.component';
import { ConnexionComponent } from './features/auth/connexion/connexion.component';
import { InscriptionComponent } from './features/auth/inscription/inscription.component';
import { CompetencesAValiderComponent } from './features/moderation/competences-a-valider/competences-a-valider.component';
import { DemandeEchangeComponent } from './features/exchanges/demande-echange/demande-echange.component';
import { MesEchangesComponent } from './features/exchanges/mes-echanges/mes-echanges.component';
import { InfoPageComponent } from './features/info-page/info-page.component'; 

// L'import qui manquait pour l'admin
import { AdminDashboardComponent } from './features/admin/admin-dashboard/admin-dashboard.component';

/**
 * Table des routes.
 */
export const routes: Routes = [
  // 1. PAGES SANS MENU (Plein écran)
  { path: 'connexion', component: ConnexionComponent },
  { path: 'inscription', component: InscriptionComponent },

  // 2. PAGES AVEC MENU (Layout principal)
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      // Par défaut, on affiche la liste des compétences
      { path: '', redirectTo: 'competences', pathMatch: 'full' },
      
      // Catalogue et gestion des compétences
      { path: 'competences', component: CompetenceListComponent },
      { path: 'competences/nouvelle', component: CompetenceFormComponent, canActivate: [authGuard] },
      { path: 'competences/:id/modifier', component: CompetenceFormComponent, canActivate: [authGuard] },
      { path: 'mes-competences', component: MesCompetencesComponent, canActivate: [authGuard] },
      
      // Profils
      { path: 'profil/moi', component: MonProfilComponent, canActivate: [authGuard] },
      { path: 'profil/:id', component: ProfilDetailComponent },
      
      // Échanges
      { path: 'echanges/nouveau/:skillId', component: DemandeEchangeComponent, canActivate: [authGuard] },
      { path: 'echanges', component: MesEchangesComponent, canActivate: [authGuard] },
      
      // Modération (Réservé aux modérateurs)
      { 
        path: 'moderation/competences', 
        component: CompetencesAValiderComponent,
        canActivate: [roleGuard('MODERATEUR', 'ADMIN')] 
      },

      // LA ROUTE ADMIN AJOUTÉE ICI
      { 
        path: 'admin', 
        component: AdminDashboardComponent,
        canActivate: [roleGuard('ADMIN')] 
      },
      
      // Pages d'information (Footer)
      { path: 'info/:type', component: InfoPageComponent },
    ]
  },

  // 3. REDIRECTION SI PAGE INTROUVABLE
  { path: '**', redirectTo: '' }
];