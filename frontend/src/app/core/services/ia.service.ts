import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_URLS } from '../config/api.config';
import { IaBrouillonSeance, IaChatReponse, IaMessage } from '../models/ia.model';


@Injectable({ providedIn: 'root' })
export class IaService {
  private base = `${API_URLS.gateway}/ai`;
  constructor(private http: HttpClient) {}

  discuter(message: string, historique: IaMessage[], page: string): Observable<IaChatReponse> {
    const history = historique.slice(-10).map((m) => ({ role: m.role, content: m.content }));
    return this.http.post<IaChatReponse>(`${this.base}/chat`, { message, history, context: { page } });
  }

  brouillonSeance(skillTitle: string, opts: { level?: string | null; durationHours?: number | null; mode?: string | null; extra?: string | null } = {}):
    Observable<{ draft: IaBrouillonSeance; provider: string }> {
    return this.http.post<{ draft: IaBrouillonSeance; provider: string }>(`${this.base}/session-draft`, { skillTitle, ...opts });
  }

  brouillonCompetence(titre: string, niveau?: string | null): Observable<{ description: string; disponibilites?: string; provider: string }> {
    return this.http.post<{ description: string; disponibilites?: string; provider: string }>(`${this.base}/skill-draft`, { titre, niveau });
  }

  ameliorer(text: string, kind: 'description' | 'skill' | 'request' | 'review'): Observable<{ text: string; tips: string[]; provider: string }> {
    return this.http.post<{ text: string; tips: string[]; provider: string }>(`${this.base}/improve-text`, { text, kind });
  }
}
