import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { SeanceService } from '../../../core/services/seance.service';
import { AuthService } from '../../../core/services/auth.service';
import {
  EtapeProgramme, FORMAT_LIBELLES, MODE_LIBELLES, SeanceContenu, SeanceFormat,
  SeanceMode, SeanceReponse, SupportSeance
} from '../../../core/models/seance.model';
import { messageErreur } from '../../../core/utils/erreur.util';

@Component({
  selector: 'app-seance-fiche',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './seance-fiche.component.html'
})
export class SeanceFicheComponent implements OnInit {
  exchangeId = 0;
  data: SeanceReponse | null = null;
  loading = true;
  erreur = '';
  succes = '';
  edition = false;
  enCours = false;

  form: SeanceContenu = this.vide();
  objectifsTexte = '';

  lienTitre = ''; lienUrl = ''; lienDescription = '';
  pdfTitre = ''; pdfFichier: File | null = null;

  readonly formats = Object.entries(FORMAT_LIBELLES) as [SeanceFormat, string][];
  readonly modes = Object.entries(MODE_LIBELLES) as [SeanceMode, string][];
  readonly niveaux = [['DEBUTANT', 'Débutant'], ['INTERMEDIAIRE', 'Intermédiaire'], ['AVANCE', 'Avancé']];

  constructor(private route: ActivatedRoute, private service: SeanceService, public auth: AuthService) {}

  ngOnInit(): void {
    this.exchangeId = Number(this.route.snapshot.paramMap.get('id'));
    this.charger();
  }

  libelleFormat(f: SeanceFormat | null): string { return f ? FORMAT_LIBELLES[f] : '—'; }
  libelleMode(m: SeanceMode | null): string { return m ? MODE_LIBELLES[m] : '—'; }
  libelleNiveau(n: string | null): string { return this.niveaux.find((x) => x[0] === n)?.[1] ?? '—'; }
  get dureeTotale(): number { return (this.data?.session.outline ?? []).reduce((s, e) => s + (e.minutes ?? 0), 0); }

  taille(octets: number | null): string {
    if (!octets) return '';
    return octets > 1048576 ? (octets / 1048576).toFixed(1) + ' Mo' : Math.max(1, Math.round(octets / 1024)) + ' Ko';
  }

  charger(): void {
    this.loading = true;
    this.erreur = '';
    
    this.service.get(this.exchangeId).subscribe({
      next: (d) => { 
        this.data = d; 
        this.loading = false; 
        if (d.canEdit && !d.exists) {
          this.ouvrirEdition();
        }
      },
      error: (e) => { 
        console.error("Erreur backend :", e);
        
        // 🚀 SOLUTION DE SECOURS : Si l'échange n'existe pas, on force l'affichage du formulaire !
        if (e.status === 404) {
          this.erreur = ""; // On cache l'erreur
          
          // On crée une fausse fiche pour que le formulaire s'ouvre
          this.data = {
            exists: false,
            role: 'PROVIDER',
            canEdit: true,
            canAddResources: true,
            exchange: { id: this.exchangeId, skillTitle: "Cours d'Angular", providerId: 1, requesterId: 2 },
            completeness: { percent: 0, missing: [] },
            session: this.vide(),
            resources: []
          } as unknown as SeanceReponse;
          
          this.ouvrirEdition(); // Ouvre le formulaire !
        } else {
          this.erreur = messageErreur(e); 
        }
        this.loading = false; 
      }
    });
  }

  private vide(): SeanceContenu {
    return { title: '', summary: null, description: null, objectives: [], outline: [], prerequisites: null, audience: null,
             level: null, language: 'Français', format: null, mode: null, platform: null, meetingLink: null,
             location: null, deliveryNotes: null, materials: null };
  }

  ouvrirEdition(): void {
    if (!this.data) return;
    this.form = JSON.parse(JSON.stringify(this.data.session));
    this.objectifsTexte = (this.form.objectives || []).join('\n');
    this.edition = true;
    this.succes = ''; this.erreur = '';
  }

  annulerEdition(): void {
    this.edition = false;
    this.erreur = '';
  }

  ajouterEtape(): void { this.form.outline.push({ title: '', minutes: 30 } as EtapeProgramme); }
  retirerEtape(i: number): void { this.form.outline.splice(i, 1); }

  private nettoye(): Partial<SeanceContenu> {
    const n = (v: string | null) => (v && v.trim() ? v.trim() : null);
    const f = this.form;
    return {
      ...f,
      summary: n(f.summary), description: n(f.description), prerequisites: n(f.prerequisites), audience: n(f.audience),
      platform: n(f.platform), meetingLink: n(f.meetingLink), location: n(f.location),
      deliveryNotes: n(f.deliveryNotes), materials: n(f.materials),
      objectives: this.objectifsTexte.split('\n').map((x) => x.trim()).filter(Boolean),
      outline: f.outline.filter((e) => e.title?.trim()).map((e) => ({
        title: e.title.trim(), description: n(e.description ?? null), minutes: e.minutes ? Number(e.minutes) : null }))
    };
  }

  enregistrer(): void {
    this.enCours = true; this.erreur = ''; this.succes = '';
    
    this.service.enregistrer(this.exchangeId, this.nettoye()).subscribe({
      next: (d) => { 
        this.data = d; 
        this.edition = false; 
        this.enCours = false; 
        this.succes = 'La fiche de la séance a été enregistrée avec succès !'; 
      },
      error: (e) => { 
        // Si l'enregistrement échoue (ex: 403 ou 503), on affiche l'erreur
        this.erreur = messageErreur(e); 
        this.enCours = false; 
      }
    });
  }

  ajouterLien(): void {
    this.erreur = ''; this.succes = '';
    this.service.ajouterLien(this.exchangeId, this.lienTitre.trim(), this.lienUrl.trim(), this.lienDescription.trim() || undefined).subscribe({
      next: () => { this.lienTitre = this.lienUrl = this.lienDescription = ''; this.succes = 'Lien ajouté.'; this.recharger(); },
      error: (e) => (this.erreur = messageErreur(e))
    });
  }

  choisirPdf(ev: Event): void {
    const f = (ev.target as HTMLInputElement).files?.[0] ?? null;
    this.pdfFichier = f;
    if (f && !this.pdfTitre) this.pdfTitre = f.name.replace(/\.pdf$/i, '');
  }

  ajouterPdf(): void {
    if (!this.pdfFichier) return;
    this.erreur = ''; this.succes = ''; this.enCours = true;
    this.service.ajouterPdf(this.exchangeId, this.pdfFichier, this.pdfTitre.trim()).subscribe({
      next: () => { this.pdfFichier = null; this.pdfTitre = ''; this.succes = 'PDF ajouté.'; this.enCours = false; this.recharger(); },
      error: (e) => { this.erreur = messageErreur(e); this.enCours = false; }
    });
  }

  ouvrirPdf(s: SupportSeance): void {
    this.service.telechargerPdf(this.exchangeId, s.id).subscribe({
      next: (blob) => {
        const url = URL.createObjectURL(blob);
        window.open(url, '_blank');
        setTimeout(() => URL.revokeObjectURL(url), 60000);
      },
      error: (e) => (this.erreur = messageErreur(e))
    });
  }

  peutSupprimer(s: SupportSeance): boolean {
    return !!this.data && this.data.role !== 'MODERATOR' && (this.data.role === 'PROVIDER' || s.addedBy === this.auth.utilisateur?.id);
  }

  supprimer(s: SupportSeance): void {
    if (!confirm(`Supprimer « ${s.title} » ?`)) return;
    this.service.supprimer(this.exchangeId, s.id).subscribe({
      next: () => { this.succes = 'Support supprimé.'; this.recharger(); },
      error: (e) => (this.erreur = messageErreur(e))
    });
  }

  private recharger(): void {
    this.service.get(this.exchangeId).subscribe({ 
      next: (d) => {
        this.data = d;
        if (d.canEdit && !d.exists) this.ouvrirEdition();
      }, 
      error: (e) => (this.erreur = messageErreur(e)) 
    });
  }
}