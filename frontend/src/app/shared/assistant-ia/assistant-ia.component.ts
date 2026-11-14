import { Component, ElementRef, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { IaService } from '../../core/services/ia.service';
import { AuthService } from '../../core/services/auth.service';
import { IaMessage } from '../../core/models/ia.model';
import { messageErreur } from '../../core/utils/erreur.util';

/**
 * Assistant IA flottant, présent sur TOUTES les pages de l'application (inclus dans MainLayout).
 * Il connaît la page où se trouve l'étudiant (contexte) pour proposer des questions adaptées,
 * et peut recommander des compétences du catalogue.
 */
@Component({
  selector: 'app-assistant-ia',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
  <ng-container *ngIf="auth.estConnecte">
    <button (click)="basculer()" aria-label="Ouvrir l'assistant IA"
            class="fixed bottom-6 right-6 z-40 w-14 h-14 rounded-full bg-[#5B8A72] text-white text-2xl shadow-xl shadow-[#5B8A72]/40 hover:bg-[#4A725D] hover:scale-105 transition-all flex items-center justify-center">
      {{ ouvert ? '✕' : '✨' }}
    </button>

    <section *ngIf="ouvert" class="fixed bottom-24 right-6 z-40 w-[22rem] max-w-[calc(100vw-2rem)] h-[32rem] max-h-[70vh] bg-white rounded-3xl shadow-2xl border border-gray-100 flex flex-col overflow-hidden animate-fade-in-up">
      <header class="bg-gradient-to-r from-[#5B8A72] to-[#4A725D] text-white px-5 py-4">
        <p class="font-extrabold">Assistant SkillSwap</p>
        <p class="text-xs text-white/80">Crédits, échanges, séances, recommandations…</p>
      </header>

      <div #zone class="flex-1 overflow-y-auto p-4 space-y-3 bg-[#F4F7F6]">
        <div *ngIf="!messages.length" class="text-sm text-gray-500">
          Salut {{ auth.utilisateur?.nom }} 👋 Pose-moi une question ou dis-moi ce que tu veux apprendre.
        </div>
        <div *ngFor="let m of messages" [ngClass]="m.role === 'user' ? 'text-right' : ''">
          <div [ngClass]="m.role === 'user' ? 'bg-[#5B8A72] text-white ml-8' : 'bg-white text-gray-700 mr-8 border border-gray-100'"
               class="inline-block text-left text-sm rounded-2xl px-4 py-2.5 whitespace-pre-line shadow-sm">{{ m.content }}</div>
          <div *ngIf="m.skills?.length" class="mt-2 mr-8 space-y-2 text-left">
            <a *ngFor="let c of m.skills" [routerLink]="['/echanges/nouveau', c.id]" (click)="ouvert = false"
               class="block bg-white border border-[#5B8A72]/30 rounded-xl px-3 py-2 text-sm hover:bg-[#F0F9F4]">
              <span class="font-bold text-gray-800">{{ c.titre }}</span>
              <span class="block text-xs text-gray-400">{{ c.categorie }} · {{ c.niveau }} — Demander un échange →</span>
            </a>
          </div>
        </div>
        <p *ngIf="enCours" class="text-xs text-gray-400">L'assistant réfléchit…</p>
        <p *ngIf="erreur" class="text-xs text-red-500">{{ erreur }}</p>
      </div>

      <div class="px-4 pt-2 pb-1 flex flex-wrap gap-1.5 bg-white border-t border-gray-100">
        <button *ngFor="let s of suggestions" (click)="envoyer(s)"
                class="text-xs bg-[#F0F9F4] text-[#4A725D] font-semibold rounded-full px-3 py-1 hover:bg-[#dff1e7]">{{ s }}</button>
      </div>
      <form (ngSubmit)="envoyer()" class="p-3 bg-white flex gap-2">
        <input name="saisie" [(ngModel)]="saisie" maxlength="1000" placeholder="Écris ta question…" autocomplete="off"
               class="flex-1 rounded-xl border border-gray-200 px-3 py-2 text-sm outline-none focus:ring-2 focus:ring-[#5B8A72]/40">
        <button type="submit" [disabled]="!saisie.trim() || enCours"
                class="bg-[#5B8A72] text-white rounded-xl px-4 font-bold text-sm disabled:opacity-50">Envoyer</button>
      </form>
    </section>
  </ng-container>`
})
export class AssistantIaComponent {
  @ViewChild('zone') zone?: ElementRef<HTMLDivElement>;
  ouvert = false;
  saisie = '';
  enCours = false;
  erreur = '';
  messages: IaMessage[] = [];
  suggestions: string[] = ['Comment fonctionnent les crédits ?', 'Je veux apprendre Java', 'Comment demander un échange ?'];

  constructor(private ia: IaService, public auth: AuthService, private router: Router) {}

  basculer(): void { this.ouvert = !this.ouvert; }

  envoyer(texte?: string): void {
    const message = (texte ?? this.saisie).trim();
    if (!message || this.enCours) return;
    this.saisie = '';
    this.erreur = '';
    const historique = [...this.messages];
    this.messages.push({ role: 'user', content: message });
    this.enCours = true;
    this.defiler();
    this.ia.discuter(message, historique, this.router.url).subscribe({
      next: (r) => {
        this.messages.push({ role: 'assistant', content: r.reply, skills: r.skills });
        this.suggestions = r.suggestions;
        this.enCours = false;
        this.defiler();
      },
      error: (e) => { this.erreur = messageErreur(e); this.enCours = false; }
    });
  }

  private defiler(): void {
    setTimeout(() => { if (this.zone) this.zone.nativeElement.scrollTop = this.zone.nativeElement.scrollHeight; }, 0);
  }
}
