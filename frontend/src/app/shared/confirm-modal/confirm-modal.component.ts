import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-confirm-modal',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div *ngIf="isOpen" class="fixed inset-0 z-[90] flex items-center justify-center p-4 bg-gray-900/40 backdrop-blur-sm animate-fade-in">
      <div class="bg-white rounded-[2rem] p-8 max-w-sm w-full shadow-2xl animate-scale-in">
        <h3 class="text-xl font-bold text-gray-900 mb-2">{{ title }}</h3>
        <p class="text-gray-500 mb-6">{{ message }}</p>
        <div class="flex justify-end gap-3">
          <button (click)="onCancel.emit()" class="px-5 py-2.5 rounded-xl font-bold text-gray-500 hover:bg-gray-100 transition-colors">
            Annuler
          </button>
          <button (click)="onConfirm.emit()" class="px-5 py-2.5 rounded-xl font-bold text-white bg-[#5B8A72] hover:bg-[#4A725D] transition-colors shadow-lg shadow-[#5B8A72]/30">
            Confirmer
          </button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    @keyframes fadeIn { from { opacity: 0; } to { opacity: 1; } }
    @keyframes scaleIn { from { transform: scale(0.95); opacity: 0; } to { transform: scale(1); opacity: 1; } }
    .animate-fade-in { animation: fadeIn 0.2s ease-out; }
    .animate-scale-in { animation: scaleIn 0.2s ease-out; }
  `]
})
export class ConfirmModalComponent {
  @Input() isOpen = false;
  @Input() title = 'Confirmation';
  @Input() message = 'Êtes-vous sûr ?';
  @Output() onConfirm = new EventEmitter<void>();
  @Output() onCancel = new EventEmitter<void>();
}