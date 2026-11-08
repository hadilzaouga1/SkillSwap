import { Injectable, ApplicationRef, ComponentRef, createComponent, EnvironmentInjector } from '@angular/core';
import { ToastComponent } from './toast/toast.component';

@Injectable({ providedIn: 'root' })
export class ToastService {
  private toastRef: ComponentRef<ToastComponent> | null = null;

  constructor(
    private appRef: ApplicationRef,
    private injector: EnvironmentInjector
  ) {}

  show(message: string, type: 'success' | 'error' | 'info' = 'success') {
    if (!this.toastRef) {
      this.toastRef = createComponent(ToastComponent, { environmentInjector: this.injector });
      this.appRef.attachView(this.toastRef.hostView);
      document.body.appendChild(this.toastRef.location.nativeElement);
    }
    this.toastRef.instance.show(message, type);
  }
}