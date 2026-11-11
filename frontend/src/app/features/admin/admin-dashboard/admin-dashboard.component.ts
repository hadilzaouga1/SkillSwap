import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { 
  LucideAngularModule, Users, BookOpen, ShieldAlert, CheckCircle, 
  XCircle, Search, MoreVertical, TrendingUp, Settings
} from 'lucide-angular';
import { API_URLS } from '../../../core/config/api.config';
import { ToastService } from '../../../shared/toast.service';
import { ConfirmModalComponent } from '../../../shared/confirm-modal/confirm-modal.component';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule, ConfirmModalComponent],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.scss'
})
export class AdminDashboardComponent implements OnInit {
  readonly icons = { Users, BookOpen, ShieldAlert, CheckCircle, XCircle, Search, MoreVertical, TrendingUp, Settings };

  ongletActif: string = 'dashboard';

  statistiques = {
    totalEtudiants: 0,
    totalCompetences: 0,
    enAttente: 0,
    echangesEnCours: 0
  };

  competencesEnAttente: any[] = [];
  utilisateurs: any[] = [];

  @ViewChild(ConfirmModalComponent) modal!: ConfirmModalComponent;
  modalTitle = '';
  modalMessage = '';
  actionEnAttente: { type: 'valider' | 'refuser' | 'suspendre' | 'reactiver', id: number } | null = null;

  constructor(private http: HttpClient, private toastService: ToastService) {}

  ngOnInit(): void {
    this.chargerUtilisateurs();
    this.chargerCompetencesEnAttente();
    this.chargerStatistiques();
  }

  chargerStatistiques(): void {
    this.http.get<any>(`${API_URLS.gateway}/admin/stats`).subscribe({
      next: (data) => {
        this.statistiques = data;
      },
      error: () => {
        this.calculerStatistiquesParDefaut();
      }
    });
  }

  chargerUtilisateurs(): void {
    this.http.get<any[]>(`${API_URLS.gateway}/utilisateurs`).subscribe({
      next: (data) => {
        this.utilisateurs = data.map(u => ({
          id: u.id,
          nom: u.nom,
          email: u.email,
          role: u.role || 'ETUDIANT',
          statut: u.statut || 'ACTIF'
        }));
        this.calculerStatistiquesParDefaut();
      },
      error: () => {
        this.utilisateurs = [
          { id: 1, nom: 'Hadil Zaouga', email: 'hadil@univ.tn', role: 'ADMIN', statut: 'ACTIF' },
          { id: 2, nom: 'Roua Ben Gara', email: 'roua@univ.tn', role: 'ETUDIANT', statut: 'ACTIF' }
        ];
        this.calculerStatistiquesParDefaut();
      }
    });
  }

  chargerCompetencesEnAttente(): void {
    this.http.get<any[]>(`${API_URLS.gateway}/moderation/competences?statut=EN_ATTENTE`).subscribe({
      next: (data) => {
        this.competencesEnAttente = data.map(c => ({
          id: c.id,
          titre: c.titre,
          auteur: 'Étudiant ' + (c.etudiantId || ''),
          categorie: c.categorie?.nom || 'Informatique'
        }));
        this.calculerStatistiquesParDefaut();
      },
      error: (err) => {
        console.error("Erreur chargement modération", err);
      }
    });
  }

  calculerStatistiquesParDefaut(): void {
    this.statistiques.totalEtudiants = this.utilisateurs.length;
    this.statistiques.enAttente = this.competencesEnAttente.length;
    this.statistiques.totalCompetences = this.competencesEnAttente.length + 2;
  }

  demanderValidation(id: number) {
    this.actionEnAttente = { type: 'valider', id };
    this.modalTitle = 'Valider la compétence';
    this.modalMessage = 'Voulez-vous vraiment accepter cette compétence dans le catalogue ?';
    this.modal.isOpen = true;
  }

  demanderRefus(id: number) {
    this.actionEnAttente = { type: 'refuser', id };
    this.modalTitle = 'Refuser la compétence';
    this.modalMessage = 'Voulez-vous vraiment refuser cette compétence ?';
    this.modal.isOpen = true;
  }

  demanderSuspension(id: number) {
    this.actionEnAttente = { type: 'suspendre', id };
    this.modalTitle = 'Suspendre l\'utilisateur';
    this.modalMessage = 'Cet utilisateur ne pourra plus se connecter à la plateforme.';
    this.modal.isOpen = true;
  }

  demanderReactivation(id: number) {
    this.actionEnAttente = { type: 'reactiver', id };
    this.modalTitle = 'Réactiver l\'utilisateur';
    this.modalMessage = 'Cet utilisateur pourra à nouveau se connecter.';
    this.modal.isOpen = true;
  }

  confirmerAction() {
    if (!this.actionEnAttente) return;
    const { type, id } = this.actionEnAttente;

    if (type === 'valider') {
      this.http.put(`${API_URLS.gateway}/moderation/competences/${id}/valider`, {}).subscribe({
        next: () => {
          this.competencesEnAttente = this.competencesEnAttente.filter(c => c.id !== id);
          this.calculerStatistiquesParDefaut();
          this.toastService.show('Compétence validée avec succès !', 'success');
          this.fermerModale();
        },
        error: () => {
          this.toastService.show('Erreur lors de la validation', 'error');
          this.fermerModale();
        }
      });
    } 
    else if (type === 'refuser') {
      this.http.put(`${API_URLS.gateway}/moderation/competences/${id}/refuser`, {}).subscribe({
        next: () => {
          this.competencesEnAttente = this.competencesEnAttente.filter(c => c.id !== id);
          this.calculerStatistiquesParDefaut();
          this.toastService.show('Compétence refusée.', 'info');
          this.fermerModale();
        },
        error: () => {
          this.toastService.show('Erreur lors du refus', 'error');
          this.fermerModale();
        }
      });
    }
    else if (type === 'suspendre') {
      const user = this.utilisateurs.find(u => u.id === id);
      if (user) user.statut = 'SUSPENDU';
      this.toastService.show('Utilisateur suspendu avec succès.', 'error');
      this.fermerModale();
    }
    else if (type === 'reactiver') {
      const user = this.utilisateurs.find(u => u.id === id);
      if (user) user.statut = 'ACTIF';
      this.toastService.show('Utilisateur réactivé avec succès.', 'success');
      this.fermerModale();
    }
  }

  fermerModale() {
    if (this.modal) this.modal.isOpen = false;
    this.actionEnAttente = null;
  }

  ouvrirParametres() {
    this.toastService.show('La page des paramètres est en cours de développement.', 'info');
  }
}