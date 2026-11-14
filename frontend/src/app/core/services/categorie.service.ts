import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { Categorie } from '../models/categorie.model';

@Injectable({
  providedIn: 'root'
})
export class CategorieService {
  constructor(private http: HttpClient) { }
  getCategories(): Observable<Categorie[]> {
    return this.http.get<Categorie[]>(`${API_URLS.gateway}/categories`);
  }
}
