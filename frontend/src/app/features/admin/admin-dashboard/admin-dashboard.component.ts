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

  competencesEnAttente = [
    { id: 1, titre: 'Cours d\'Angular Avancé', auteur: 'Hadil Zaouga', categorie: 'Informatique' }
  ];

  utilisateurs = [
    { id: 1, nom: 'Hadil Zaouga', email: 'hadil@univ.tn', role: 'ETUDIANT', statut: 'ACTIF' },
    { id: 2, nom: 'Roua Ben Gara', email: 'roua@univ.tn', role: 'ETUDIANT', statut: 'ACTIF' },
    { id: 3, nom: 'Moez Bahrini', email: 'moez@univ.tn', role: 'MODERATEUR', statut: 'ACTIF' },
    { id: 4, nom: 'Ali Ben Salah', email: 'ali@univ.tn', role: 'ETUDIANT', statut: 'SUSPENDU' },
  ];

  // Gestion de la modale
  @ViewChild(ConfirmModalComponent) modal!: ConfirmModalComponent;
  modalTitle = '';
  modalMessage = '';
  actionEnAttente: { type: 'valider' | 'refuser' | 'suspendre' | 'reactiver', id: number } | null = null;

  constructor(private http: HttpClient, private toastService: ToastService) {}

  ngOnInit(): void {
    this.chargerStatistiques();
  }

  // ==================== STATISTIQUES ====================
  chargerStatistiques(): void {
    this.http.get<any>(`${API_URLS.gateway}/admin/stats`).subscribe({
      next: (data) => this.statistiques = data,
      error: () => {
        this.statistiques = {
          totalEtudiants: 4,
          totalCompetences: 2,
          enAttente: 1,
          echangesEnCours: 0
        };
      }
    });
  }

  // ==================== MODALE : COMPÉTENCES ====================
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

  // ==================== MODALE : UTILISATEURS ====================
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

  // ==================== CONFIRMATION ====================
  confirmerAction() {
    if (!this.actionEnAttente) return;
    const { type, id } = this.actionEnAttente;

    if (type === 'valider') {
      this.competencesEnAttente = this.competencesEnAttente.filter(c => c.id !== id);
      this.toastService.show('Compétence validée avec succès !', 'success');
    } 
    else if (type === 'refuser') {
      this.competencesEnAttente = this.competencesEnAttente.filter(c => c.id !== id);
      this.toastService.show('Compétence refusée.', 'info');
    }
    else if (type === 'suspendre') {
      const user = this.utilisateurs.find(u => u.id === id);
      if (user) user.statut = 'SUSPENDU';
      this.toastService.show('Utilisateur suspendu avec succès.', 'error');
    }
    else if (type === 'reactiver') {
      const user = this.utilisateurs.find(u => u.id === id);
      if (user) user.statut = 'ACTIF';
      this.toastService.show('Utilisateur réactivé avec succès.', 'success');
    }

    this.fermerModale();
  }

  fermerModale() {
    this.modal.isOpen = false;
    this.actionEnAttente = null;
  }
}