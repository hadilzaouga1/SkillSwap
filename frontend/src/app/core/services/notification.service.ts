import { Injectable, OnDestroy } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, Subscription, interval, tap } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { NotificationListe } from '../models/notification.model';

@Injectable({ providedIn: 'root' })
export class NotificationService implements OnDestroy {
  private base = `${API_URLS.gateway}/notifications`;
  private liste = new BehaviorSubject<NotificationListe>({ unreadCount: 0, items: [] });
  readonly liste$ = this.liste.asObservable();
  private timer?: Subscription;

  constructor(private http: HttpClient) {}

  demarrer(): void {
    this.rafraichir();
    this.timer ??= interval(30000).subscribe(() => this.rafraichir());
  }

  arreter(): void { this.timer?.unsubscribe(); this.timer = undefined; }
  ngOnDestroy(): void { this.arreter(); }

  rafraichir(): void {
    this.http.get<NotificationListe>(this.base).subscribe({ next: (l) => this.liste.next(l), error: () => {} });
  }

  lire(id: number): Observable<{ unreadCount: number }> {
    return this.http.patch<{ unreadCount: number }>(`${this.base}/${id}/read`, null).pipe(tap(() => this.rafraichir()));
  }

  toutLire(): Observable<{ unreadCount: number }> {
    return this.http.patch<{ unreadCount: number }>(`${this.base}/read-all`, null).pipe(tap(() => this.rafraichir()));
  }

  supprimer(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`).pipe(tap(() => this.rafraichir()));
  }

  signaler(exchangeId: number): void {
    this.http.post(`${this.base}/exchange/${exchangeId}/sync`, null).subscribe({ error: () => {} });
  }

  signalerFiche(exchangeId: number): void {
    this.http.post(`${this.base}/exchange/${exchangeId}/session-updated`, null).subscribe({ error: () => {} });
  }
}
