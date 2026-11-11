import { Pipe, PipeTransform } from '@angular/core';
import { NIVEAUX } from '../../core/models/competence.model';

/**
 * Affiche un niveau de façon lisible : {{ competence.niveau | niveau }}
 * "INTERMEDIAIRE" devient "Intermédiaire".
 */
@Pipe({ name: 'niveau', standalone: true })
export class NiveauPipe implements PipeTransform {
  transform(valeur: string | null | undefined): string {
    const trouve = NIVEAUX.find(n => n.valeur === valeur);
    return trouve ? trouve.libelle : (valeur ?? '');
  }
}
