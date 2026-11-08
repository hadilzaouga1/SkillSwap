import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';

import { CompetenceListComponent } from './competence-list.component';

describe('CompetenceListComponent', () => {
  let component: CompetenceListComponent;
  let fixture: ComponentFixture<CompetenceListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CompetenceListComponent],
      // Le composant utilise HttpClient et routerLink : on fournit des versions de test
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CompetenceListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
