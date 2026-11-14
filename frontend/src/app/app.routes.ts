import { Routes } from '@angular/router';
import { MainLayoutComponent } from './layout/main-layout/main-layout.component';

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
import { SeanceFicheComponent } from './features/seances/seance-fiche/seance-fiche.component';
import { InfoPageComponent } from './features/info-page/info-page.component';
import { MesCreditsComponent } from './features/credits/mes-credits/mes-credits.component'; 
import { AdminDashboardComponent } from './features/admin/admin-dashboard/admin-dashboard.component';

export const routes: Routes = [
  { path: 'connexion', component: ConnexionComponent },
  { path: 'inscription', component: InscriptionComponent },
  {
    path: '',
    component: MainLayoutComponent,
    children: [
      { path: '', redirectTo: 'competences', pathMatch: 'full' },
      { path: 'competences', component: CompetenceListComponent },
      { path: 'competences/nouvelle', component: CompetenceFormComponent, canActivate: [authGuard] },
      { path: 'competences/:id/modifier', component: CompetenceFormComponent, canActivate: [authGuard] },
      { path: 'mes-competences', component: MesCompetencesComponent, canActivate: [authGuard] },
      
      { path: 'profil/moi', component: MonProfilComponent, canActivate: [authGuard] },
      { path: 'profil/:id', component: ProfilDetailComponent },
      
      { path: 'echanges/nouveau/:skillId', component: DemandeEchangeComponent, canActivate: [authGuard] },
      { path: 'echanges', component: MesEchangesComponent, canActivate: [authGuard] },
      { path: 'echanges/:id/seance', component: SeanceFicheComponent, canActivate: [authGuard] },
      { path: 'credits', component: MesCreditsComponent, canActivate: [authGuard] },
      
      { 
        path: 'moderation/competences', 
        component: CompetencesAValiderComponent,
        canActivate: [roleGuard('MODERATEUR', 'ADMIN')] 
      },

      { 
        path: 'admin', 
        component: AdminDashboardComponent,
        canActivate: [roleGuard('ADMIN')] 
      },
      
      { path: 'info/:type', component: InfoPageComponent },
    ]
  },

  { path: '**', redirectTo: '' }
];