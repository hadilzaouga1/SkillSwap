import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute } from '@angular/router';
import { LucideAngularModule, Shield, Mail, FileText, ArrowLeft } from 'lucide-angular';

@Component({
  selector: 'app-info-page',
  standalone: true,
  imports: [CommonModule, RouterModule, LucideAngularModule],
  templateUrl: './info-page.component.html',
  styleUrl: './info-page.component.scss'
})
export class InfoPageComponent implements OnInit {
  readonly icons = { Shield, Mail, FileText, ArrowLeft };
  typePage: string = '';
  titre: string = '';
  contenu: string = '';

  constructor(private route: ActivatedRoute) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.typePage = params.get('type') || 'info';
      this.setContenu();
    });
  }

  setContenu(): void {
    switch (this.typePage) {
      case 'confidentialite':
        this.titre = 'Politique de Confidentialité';
        this.contenu = 'Chez SkillSwap, nous prenons la protection de vos données très au sérieux. Toutes les informations partagées sur la plateforme sont sécurisées et ne sont jamais revendues à des tiers. Votre email universitaire est utilisé uniquement pour vérifier votre statut étudiant.';
        break;
      case 'conditions':
        this.titre = 'Conditions d\'Utilisation';
        this.contenu = 'En utilisant SkillSwap, vous acceptez de respecter les règles de courtoisie et d\'entraide. Le système de crédits-temps (1h = 1 crédit) doit être respecté. Tout comportement inapproprié pourra entraîner la suspension du compte.';
        break;
      case 'contact':
        this.titre = 'Nous Contacter';
        this.contenu = 'Une question ? Un problème technique ? L\'équipe SkillSwap est là pour vous aider. Écrivez-nous à : support@skillswap.tn. Nous vous répondrons dans les plus brefs délais.';
        break;
      default:
        this.titre = 'Information';
        this.contenu = 'Page en construction.';
    }
  }
}