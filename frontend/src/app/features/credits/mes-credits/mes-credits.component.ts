import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { CreditService } from '../../../core/services/credit.service';
import { Solde, TransactionCredit, TypeTransaction } from '../../../core/models/credit.model';
import { messageErreur } from '../../../core/utils/erreur.util';

/** Page "Mes crédits" : solde et historique des mouvements (US14). Route : /credits */
@Component({
  selector: 'app-mes-credits',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './mes-credits.component.html',
  styleUrl: './mes-credits.component.scss'
})
export class MesCreditsComponent implements OnInit {
  solde: Solde | null = null;
  transactions: TransactionCredit[] = [];
  loading = true;
  erreur = '';

  constructor(private creditService: CreditService) {}

  ngOnInit(): void {
    this.creditService.getSolde().subscribe({
      next: (data) => (this.solde = data),
      error: (err) => (this.erreur = messageErreur(err))
    });

    this.creditService.getTransactions().subscribe({
      next: (data) => {
        this.transactions = data;
        this.loading = false;
      },
      error: (err) => {
        this.erreur = messageErreur(err);
        this.loading = false;
      }
    });
  }

  libelle(type: TypeTransaction): string {
    switch (type) {
      case 'WELCOME': return 'Crédits de bienvenue';
      case 'RESERVATION': return 'Crédits bloqués';
      case 'RELEASE': return 'Crédits débloqués';
      case 'TRANSFER_OUT': return 'Séance payée';
      case 'TRANSFER_IN': return 'Séance réalisée';
    }
  }

  /** Effet sur les crédits DISPONIBLES : + (gain), - (dépense ou blocage). */
  signe(type: TypeTransaction): string {
    return type === 'WELCOME' || type === 'RELEASE' || type === 'TRANSFER_IN' ? '+' : '−';
  }

  /** Classe CSS : vert pour un gain, rouge pour une dépense. */
  classe(type: TypeTransaction): string {
    return this.signe(type) === '+' ? 'gain' : 'depense';
  }
}
