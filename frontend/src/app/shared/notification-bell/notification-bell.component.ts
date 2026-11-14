import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { NotificationService } from '../../core/services/notification.service';
import { AuthService } from '../../core/services/auth.service';
import { Notification } from '../../core/models/notification.model';

/** Cloche de notifications de la barre du haut : compteur, liste déroulante, lecture, suppression. */
@Component({
  selector: 'app-notification-bell',
  standalone: true,
  imports: [CommonModule],
  template: `
  <div class="relative" *ngIf="auth.estConnecte">
    <button (click)="ouvert = !ouvert" aria-label="Notifications"
            class="p-2.5 text-gray-400 hover:text-[#5B8A72] hover:bg-[#5B8A72]/5 rounded-full transition-colors relative">
      <span class="text-xl leading-none">🔔</span>
      <span *ngIf="(liste$ | async)?.unreadCount as n"
            class="absolute -top-0.5 -right-0.5 min-w-[1.1rem] h-[1.1rem] px-1 bg-red-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center border-2 border-white">
        {{ n > 9 ? '9+' : n }}
      </span>
    </button>

    <div *ngIf="ouvert" class="absolute right-0 mt-3 w-96 max-w-[calc(100vw-2rem)] bg-white rounded-2xl shadow-2xl border border-gray-100 z-50 overflow-hidden">
      <div class="flex items-center justify-between px-4 py-3 border-b border-gray-100">
        <p class="font-extrabold text-gray-900">Notifications</p>
        <button (click)="toutLire()" class="text-xs font-bold text-[#5B8A72] hover:underline">Tout marquer comme lu</button>
      </div>
      <ul class="max-h-96 overflow-y-auto divide-y divide-gray-50">
        <li *ngIf="!(liste$ | async)?.items?.length" class="px-4 py-8 text-center text-sm text-gray-400">Aucune notification pour le moment.</li>
        <li *ngFor="let n of (liste$ | async)?.items" (click)="ouvrir(n)"
            [ngClass]="n.read ? 'bg-white' : 'bg-[#F0F9F4]'" class="px-4 py-3 cursor-pointer hover:bg-gray-50 flex gap-3">
          <span class="mt-1.5 w-2 h-2 rounded-full flex-shrink-0" [ngClass]="n.read ? 'bg-transparent' : 'bg-[#5B8A72]'"></span>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-bold text-gray-800">{{ n.title }}</p>
            <p class="text-xs text-gray-500">{{ n.message }}</p>
            <p class="text-[11px] text-gray-300 mt-1">{{ n.createdAt | date:'dd/MM HH:mm' }}</p>
          </div>
          <button (click)="supprimer($event, n)" class="text-gray-300 hover:text-red-500 text-sm self-start" aria-label="Supprimer">✕</button>
        </li>
      </ul>
    </div>
  </div>`
})
export class NotificationBellComponent implements OnInit {
  ouvert = false;
  liste$ = this.service.liste$;

  constructor(private service: NotificationService, public auth: AuthService, private router: Router) {}

  ngOnInit(): void { if (this.auth.estConnecte) this.service.demarrer(); }

  ouvrir(n: Notification): void {
    if (!n.read) this.service.lire(n.id).subscribe();
    this.ouvert = false;
    if (n.link) this.router.navigateByUrl(n.link);
  }
  toutLire(): void { this.service.toutLire().subscribe(); }
  supprimer(ev: Event, n: Notification): void { ev.stopPropagation(); this.service.supprimer(n.id).subscribe(); }
}
