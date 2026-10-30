import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CompetenceService, Competence } from '../../../core/services/competence.service';

@Component({
  selector: 'app-competence-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './competence-list.component.html',
  styleUrl: './competence-list.component.scss'
})
export class CompetenceListComponent implements OnInit {
  competences: Competence[] = [];
  loading = true;

  constructor(private competenceService: CompetenceService) {}

  ngOnInit(): void {
    this.competenceService.getCompetences().subscribe({
      next: (data) => {
        this.competences = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur API', err);
        this.loading = false;
      }
    });
  }
}