import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AvisService } from '../../core/services/avis.service';
import { IaService } from '../../core/services/ia.service';
import { AvisEchange } from '../../core/models/notification.model';
import { messageErreur } from '../../core/utils/erreur.util';

/** Avis d'un échange terminé : formulaire (étoiles + commentaire, reformulation IA) ou avis déjà donnés. */
@Component({
  selector: 'app-avis-echange',
  standalone: true,
  imports: [CommonModule, FormsModule],
  template: `
  <div *ngIf="data" class="mt-4 bg-amber-50/50 border border-amber-100 rounded-2xl p-4 space-y-3">
    <ng-container *ngIf="data.canReview">
      <p class="text-sm font-bold text-gray-800">Donnez votre avis sur cet échange</p>
      <div class="flex gap-1 text-2xl">
        <button *ngFor="let n of [1,2,3,4,5]" type="button" (click)="note = n" [attr.aria-label]="n + ' étoiles'"
                [ngClass]="n <= note ? 'text-amber-400' : 'text-gray-300'">★</button>
      </div>
      <textarea [(ngModel)]="commentaire" name="commentaire" rows="2" maxlength="500" placeholder="Votre commentaire (facultatif)"
                class="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-amber-300/50"></textarea>
      <div class="flex flex-wrap items-center gap-2">
        <button (click)="publier()" [disabled]="!note || enCours"
                class="bg-[#5B8A72] text-white px-4 py-2 rounded-xl text-sm font-bold disabled:opacity-50">Publier mon avis</button>
        <button (click)="reformuler()" [disabled]="commentaire.trim().length < 3 || enCours" type="button"
                class="text-sm font-bold text-[#5B8A72] hover:underline disabled:opacity-40">✨ Reformuler avec l'IA</button>
      </div>
      <p *ngFor="let t of conseils" class="text-xs text-gray-500">💡 {{ t }}</p>
    </ng-container>

    <ng-container *ngIf="data.mine">
      <p class="text-sm text-gray-700"><span class="font-bold">Votre avis :</span>
        <span class="text-amber-400"> {{ etoiles(data.mine.rating) }}</span> <span *ngIf="data.mine.comment">— {{ data.mine.comment }}</span></p>
    </ng-container>
    <p *ngIf="data.theirs" class="text-sm text-gray-700"><span class="font-bold">Avis reçu :</span>
      <span class="text-amber-400"> {{ etoiles(data.theirs.rating) }}</span> <span *ngIf="data.theirs.comment">— {{ data.theirs.comment }}</span></p>
    <p *ngIf="erreur" class="text-xs text-red-500">{{ erreur }}</p>
  </div>`
})
export class AvisEchangeComponent implements OnInit {
  @Input({ required: true }) exchangeId!: number;
  data: AvisEchange | null = null;
  note = 0;
  commentaire = '';
  conseils: string[] = [];
  erreur = '';
  enCours = false;

  constructor(private avis: AvisService, private ia: IaService) {}

  ngOnInit(): void { this.charger(); }

  etoiles(n: number): string { return '★'.repeat(n) + '☆'.repeat(5 - n); }

  private charger(): void {
    this.avis.deLEchange(this.exchangeId).subscribe({ next: (d) => (this.data = d), error: () => (this.data = null) });
  }

  publier(): void {
    this.enCours = true; this.erreur = '';
    this.avis.publier(this.exchangeId, this.note, this.commentaire.trim() || null).subscribe({
      next: () => { this.enCours = false; this.charger(); },
      error: (e) => { this.erreur = messageErreur(e); this.enCours = false; }
    });
  }

  reformuler(): void {
    this.enCours = true;
    this.ia.ameliorer(this.commentaire, 'review').subscribe({
      next: (r) => { this.commentaire = r.text; this.conseils = r.tips; this.enCours = false; },
      error: (e) => { this.erreur = messageErreur(e); this.enCours = false; }
    });
  }
}
