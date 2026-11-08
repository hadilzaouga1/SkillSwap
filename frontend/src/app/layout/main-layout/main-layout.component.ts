import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { CreditService } from '../../core/services/credit.service';
import { 
  LucideAngularModule, LayoutDashboard, Search, BookOpen, RefreshCw, 
  Shield, LogOut, User, Bell, Settings, Menu, Coins
} from 'lucide-angular';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './main-layout.component.html'
})
export class MainLayoutComponent implements OnInit {
  // Ajout de l'icône Coins ici
  readonly icons = { LayoutDashboard, Search, BookOpen, RefreshCw, Shield, LogOut, User, Bell, Settings, Menu, Coins };
  isSidebarOpen = true;

  // On expose l'observable du solde
  solde$ = this.creditService.solde$;

  constructor(
    public auth: AuthService,
    private creditService: CreditService
  ) {}

  ngOnInit(): void {
    // Si l'utilisateur est connecté, on récupère son solde dès l'ouverture du layout
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