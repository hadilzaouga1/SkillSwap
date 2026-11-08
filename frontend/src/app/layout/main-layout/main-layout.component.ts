import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';
import { 
  LucideAngularModule, LayoutDashboard, Search, BookOpen, RefreshCw, 
  Shield, LogOut, User, Bell, Settings, Menu
} from 'lucide-angular';

@Component({
  selector: 'app-main-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './main-layout.component.html'
})
export class MainLayoutComponent {
  readonly icons = { LayoutDashboard, Search, BookOpen, RefreshCw, Shield, LogOut, User, Bell, Settings, Menu };
  isSidebarOpen = true;

  constructor(public auth: AuthService) {}

  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  logout() {
    this.auth.deconnexion();
  }
}