import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Observable, forkJoin } from 'rxjs';
import { ExchangeService } from '../../../core/services/exchange.service';
import { SeanceService } from '../../../core/services/seance.service';
import { ResumeSeance } from '../../../core/models/seance.model';
import { UtilisateurService } from '../../../core/services/utilisateur.service';
import { AuthService } from '../../../core/services/auth.service';
import { Exchange, ExchangeStatus, STATUT_LIBELLES } from '../../../core/models/exchange.model';
import { messageErreur } from '../../../core/utils/erreur.util';
import { estDansLeFutur, maintenantPourChamp, versFormatApi } from '../../../core/utils/date.util';

type Onglet = 'recues' | 'envoyees';

/**
 * Page "Mes échanges" : demandes reçues (je suis le prestataire) et envoyées (je suis le demandeur).
 * Les boutons affichés dépendent de mon rôle dans l'échange et de son statut ;
 * la vraie vérification est faite par le backend (403 / 409 en cas d'action interdite).
 */
@Component({
  selector: 'app-mes-echanges',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './mes-echanges.component.html',
  styleUrl: './mes-echanges.component.scss'
})
export class MesEchangesComponent implements OnInit {
  onglet: Onglet = 'recues';
  recues: Exchange[] = [];
  envoyees: Exchange[] = [];

  /** Noms des étudiants par id (récupérés auprès de l'identity-service). */
  noms: Record<number, string> = {};

  /** Avancement de la fiche de séance de chaque échange (session-service). */
  resumes: Record<number, ResumeSeance> = {};

  loading = true;
  erreur = '';
  succes = '';
  enCours: number | null = null;

  // Proposition d'un autre créneau (formulaire ouvert sur une seule carte à la fois)
  propositionOuverte: number | null = null;
  nouveauCreneau = '';
  creneauInvalide = false;
  minDate = maintenantPourChamp();

  constructor(
    private exchangeService: ExchangeService,
    private utilisateurService: UtilisateurService,
    private seanceService: SeanceService,
    private route: ActivatedRoute,
    public auth: AuthService
  ) {}

  ngOnInit(): void {
    const params = this.route.snapshot.queryParamMap;
    if (params.get('onglet') === 'envoyees') {
      this.onglet = 'envoyees';
    }
    if (params.get('cree')) {
      this.succes = 'Votre demande a bien été envoyée au prestataire.';
    }
    this.charger();
  }

  get liste(): Exchange[] {
    return this.onglet === 'recues' ? this.recues : this.envoyees;
  }

  choisirOnglet(onglet: Onglet): void {
    this.onglet = onglet;
    this.propositionOuverte = null;
  }

  charger(): void {
    this.loading = true;
    forkJoin({
      recues: this.exchangeService.getRecues(),
      envoyees: this.exchangeService.getEnvoyees()
    }).subscribe({
      next: ({ recues, envoyees }) => {
        this.recues = recues;
        this.envoyees = envoyees;
        this.loading = false;
        this.chargerNoms([...recues, ...envoyees]);
        const ids = [...recues, ...envoyees].map((x) => x.id);
        if (ids.length) {
          // Facultatif : si le session-service est arrêté, la liste reste utilisable
          this.seanceService.resumes(ids).subscribe({ next: (r) => (this.resumes = r), error: () => {} });
        }
      },
      error: (err) => {
        this.erreur = messageErreur(err);
        this.loading = false;
      }
    });
  }

  // ------------------------------------------------------------ affichage

  libelle(statut: ExchangeStatus): string {
    return STATUT_LIBELLES[statut];
  }

  classeStatut(statut: ExchangeStatus): string {
    return 'statut-' + statut.toLowerCase().replace(/_/g, '-');
  }

  /** L'autre personne de l'échange, selon l'onglet affiché. */
  interlocuteurId(e: Exchange): number {
    return this.onglet === 'recues' ? e.requesterId : e.providerId;
  }

  nom(id: number): string {
    return this.noms[id] ?? '…';
  }

  estPrestataire(e: Exchange): boolean {
    return e.providerId === this.auth.utilisateur?.id;
  }

  estDemandeur(e: Exchange): boolean {
    return e.requesterId === this.auth.utilisateur?.id;
  }

  peutAnnuler(e: Exchange): boolean {
    return ['PENDING', 'TIME_PROPOSED', 'ACCEPTED'].includes(e.status);
  }

  /** Phrase d'aide indiquant ce qu'on attend de qui. */
  aide(e: Exchange): string {
    switch (e.status) {
      case 'PENDING':
        return this.estPrestataire(e) ? 'À vous de répondre à cette demande.' : 'En attente de la réponse du prestataire.';
      case 'TIME_PROPOSED':
        return this.estDemandeur(e) ? 'Le prestataire propose un autre créneau : acceptez-le ou annulez.' : 'En attente de la réponse du demandeur au nouveau créneau.';
      case 'ACCEPTED':
        return this.estPrestataire(e) ? 'Une fois la séance réalisée, déclarez-la terminée.' : 'Demande acceptée : la séance doit avoir lieu au créneau prévu.';
      case 'AWAITING_CONFIRMATION':
        return this.estDemandeur(e) ? 'Le prestataire a déclaré la séance terminée : confirmez-vous qu\'elle a eu lieu ?' : 'En attente de la confirmation du demandeur.';
      default:
        return '';
    }
  }

  // -------------------------------------------------------------- actions

  accepter(e: Exchange): void {
    this.executer(e, this.exchangeService.accepter(e.id), 'Demande acceptée.');
  }

  refuser(e: Exchange): void {
    this.executer(e, this.exchangeService.refuser(e.id), 'Demande refusée.');
  }

  accepterCreneau(e: Exchange): void {
    this.executer(e, this.exchangeService.accepterCreneau(e.id), 'Nouveau créneau accepté.');
  }

  annuler(e: Exchange): void {
    this.executer(e, this.exchangeService.annuler(e.id), 'Échange annulé.');
  }

  terminer(e: Exchange): void {
    this.executer(e, this.exchangeService.terminer(e.id), 'Séance déclarée terminée : en attente de la confirmation du demandeur.');
  }

  confirmer(e: Exchange): void {
    this.executer(e, this.exchangeService.confirmer(e.id), 'Réalisation confirmée : l\'échange est terminé.');
  }

  ouvrirProposition(e: Exchange): void {
    this.propositionOuverte = e.id;
    this.nouveauCreneau = '';
    this.creneauInvalide = false;
  }

  fermerProposition(): void {
    this.propositionOuverte = null;
  }

  envoyerProposition(e: Exchange): void {
    this.creneauInvalide = !estDansLeFutur(this.nouveauCreneau);
    if (this.creneauInvalide) {
      return;
    }
    this.executer(
      e,
      this.exchangeService.proposerCreneau(e.id, versFormatApi(this.nouveauCreneau)),
      'Nouveau créneau proposé au demandeur.'
    );
    this.propositionOuverte = null;
  }

  // ------------------------------------------------------------- internes

  /** Exécute une action, remplace l'échange modifié dans les listes et affiche le résultat. */
  private executer(e: Exchange, appel: Observable<Exchange>, messageSucces: string): void {
    this.enCours = e.id;
    this.erreur = '';
    this.succes = '';
    appel.subscribe({
      next: (maj) => {
        this.recues = this.recues.map((x) => (x.id === maj.id ? maj : x));
        this.envoyees = this.envoyees.map((x) => (x.id === maj.id ? maj : x));
        this.succes = messageSucces;
        this.enCours = null;
      },
      error: (err) => {
        this.erreur = messageErreur(err);
        this.enCours = null;
        // Statut périmé (l'autre personne a agi entre-temps) : on recharge l'état réel
        if (err.status === 409) {
          this.charger();
        }
      }
    });
  }

  private chargerNoms(echanges: Exchange[]): void {
    const ids = new Set<number>();
    for (const e of echanges) {
      ids.add(e.requesterId);
      ids.add(e.providerId);
    }
    ids.forEach((id) => {
      if (this.noms[id] !== undefined) {
        return;
      }
      this.noms[id] = '…';
      this.utilisateurService.getProfilPublic(id).subscribe({
        next: (profil) => (this.noms[id] = profil.nom),
        error: () => (this.noms[id] = 'Étudiant')
      });
    });
  }
}
