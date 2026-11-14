import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CreditService } from '../../core/services/credit.service';
import { AssistantIaComponent } from '../../shared/assistant-ia/assistant-ia.component';
import { NotificationBellComponent } from '../../shared/notification-bell/notification-bell.component';
import { 
  LucideAngularModule, LayoutDashboard, Search, BookOpen, RefreshCw, 
  Shield, LogOut, User, Bell, Settings, Menu, Coins
} from 'lucide-angular';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule, AssistantIaComponent, NotificationBellComponent],
  templateUrl: './main-layout.component.html'
})
export class MainLayoutComponent implements OnInit {
  readonly icons = { LayoutDashboard, Search, BookOpen, RefreshCw, Shield, LogOut, User, Bell, Settings, Menu, Coins };
  isSidebarOpen = true;
  solde$ = this.creditService.solde$;

  constructor(
    public auth: AuthService,
    private creditService: CreditService
  ) {}

  ngOnInit(): void {
    if (this.auth.estConnecte) {
      this.creditService.rafraichirSolde();
    }
  }

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  logout() {
    this.auth.deconnexion();
  }
}