import { Routes } from '@angular/router';
import { CompetenceListComponent } from './features/competences/competence-list/competence-list.component';
import { CompetenceFormComponent } from './features/competences/competence-form/competence-form.component';
import { MesCompetencesComponent } from './features/competences/mes-competences/mes-competences.component';
import { ProfilDetailComponent } from './features/profil/profil-detail/profil-detail.component';
import { MonProfilComponent } from './features/profil/mon-profil/mon-profil.component';

/**
 * Table des routes de l'application.
 * ATTENTION à l'ordre : les routes précises ('competences/nouvelle', 'profil/moi')
 * doivent être AVANT les routes à paramètre ('profil/:id').
 */
export const routes: Routes = [
  { path: '', component: CompetenceListComponent },                         // catalogue (US7)
  { path: 'competences/nouvelle', component: CompetenceFormComponent },     // publier (US5)
  { path: 'competences/:id/modifier', component: CompetenceFormComponent }, // modifier (US6)
  { path: 'mes-competences', component: MesCompetencesComponent },          // mes compétences (US6)
  { path: 'profil/moi', component: MonProfilComponent },                    // mon profil (US3)
  { path: 'profil/:id', component: ProfilDetailComponent },                 // profil d'un étudiant (US8)
  { path: '**', redirectTo: '' }
];
