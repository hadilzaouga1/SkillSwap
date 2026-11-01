
// Importer Injectable pour déclarer une classe comme service Angular.
import { Injectable } from '@angular/core';

// Importer HttpClient pour envoyer des requêtes HTTP au backend.
import { HttpClient } from '@angular/common/http';

// Importer Observable pour gérer les réponses asynchrones du serveur.
import { Observable } from 'rxjs';

// Importer l'adresse de base de notre API.
import { API_URL } from '../config/api.config';

// Importer le modèle qui définit la structure d'une catégorie.
import { Categorie } from '../models/categorie.model';

// Déclarer cette classe comme un service injectable dans Angular.
// providedIn: 'root' signifie que le service est disponible dans toute l'application.
@Injectable({
  providedIn: 'root'
})
export class CategorieService {

  // Injecter HttpClient pour pouvoir effectuer des requêtes HTTP.
  constructor(private http: HttpClient) { }

  // Récupérer la liste des catégories depuis le backend.
  // Observable<Categorie[]> : la réponse sera une liste de catégories reçue de manière asynchrone.
  getCategories(): Observable<Categorie[]> {

    // Envoyer une requête HTTP GET à l'URL /api/categories
    // pour récupérer toutes les catégories.
    return this.http.get<Categorie[]>(`${API_URL}/categories`);
  }
}
