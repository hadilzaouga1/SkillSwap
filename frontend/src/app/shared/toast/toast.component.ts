import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LucideAngularModule, CheckCircle, XCircle, Info } from 'lucide-angular';

@Component({
  selector: 'app-toast',
  standalone: true,
  imports: [CommonModule, LucideAngularModule],
  template: `
    <div class="fixed top-6 right-6 z-[100] space-y-3">
      <div *ngFor="let t of toasts" 
           class="flex items-center gap-3 px-6 py-4 rounded-2xl shadow-2xl border animate-slide-in"
           [ngClass]="{
             'bg-white border-green-100 text-green-800': t.type === 'success',
             'bg-white border-red-100 text-red-800': t.type === 'error',
             'bg-white border-blue-100 text-blue-800': t.type === 'info'
           }">
        <lucide-icon *ngIf="t.type === 'success'" [img]="icons.CheckCircle" class="w-6 h-6 text-green-500"></lucide-icon>
        <lucide-icon *ngIf="t.type === 'error'" [img]="icons.XCircle" class="w-6 h-6 text-red-500"></lucide-icon>
        <lucide-icon *ngIf="t.type === 'info'" [img]="icons.Info" class="w-6 h-6 text-blue-500"></lucide-icon>
        <p class="font-bold text-sm">{{ t.message }}</p>
      </div>
    </div>
  `,
  styles: [`
    @keyframes slideIn {
      from { transform: translateX(100%); opacity: 0; }
      to { transform: translateX(0); opacity: 1; }
    }
    .animate-slide-in {
      animation: slideIn 0.3s cubic-bezier(0.16, 1, 0.3, 1) forwards;
    }
  `]
})
export class ToastComponent {
  readonly icons = { CheckCircle, XCircle, Info };
  toasts: { message: string, type: 'success' | 'error' | 'info' }[] = [];

  show(message: string, type: 'success' | 'error' | 'info' = 'success') {
    const toast = { message, type };
    this.toasts.push(toast);
    setTimeout(() => {
      this.toasts = this.toasts.filter(t => t !== toast);
    }, 3000);
  }
}