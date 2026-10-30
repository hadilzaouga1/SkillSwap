import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Competence {
  id: number;
  titre: string;
  description: string;
  niveau: string;
  categorie: { id: number, nom: string };
  etudiant: { id: number, nom: string };
}

@Injectable({
  providedIn: 'root'
})
export class CompetenceService {
  private apiUrl = 'http://localhost:8081/api/competences';

  constructor(private http: HttpClient) { }

  getCompetences(): Observable<Competence[]> {
    return this.http.get<Competence[]>(this.apiUrl);
  }
}