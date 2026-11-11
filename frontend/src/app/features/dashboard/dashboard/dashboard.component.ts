import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LucideAngularModule, TrendingUp, Users, Clock, ArrowUpRight, ArrowDownRight, BookOpen, RefreshCw } from 'lucide-angular';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent {
  readonly icons = { TrendingUp, Users, Clock, ArrowUpRight, ArrowDownRight, BookOpen, RefreshCw };
}