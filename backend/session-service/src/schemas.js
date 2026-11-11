//  Validation des données reçues (zod) + calcul de complétude de la fiche
import { z } from 'zod';

export const FORMATS = ['COURS', 'ATELIER', 'TUTORAT', 'REVISION', 'PROJET'];
export const MODES = ['ONLINE', 'IN_PERSON', 'HYBRID'];
export const NIVEAUX = ['DEBUTANT', 'INTERMEDIAIRE', 'AVANCE'];

/** Seuls http:// et https:// sont acceptés (jamais javascript:, data:, file:...). */
export function estUrlWeb(valeur) {
  try {
    const u = new URL(valeur);
    return u.protocol === 'http:' || u.protocol === 'https:';
  } catch {
    return false;
  }
}

const urlWeb = z.string().trim().max(500, 'Lien trop long').refine(estUrlWeb, 'Le lien doit commencer par http:// ou https://');

/** Texte optionnel : "" ou absent => null. */
const texte = (max) =>
  z.string().trim().max(max, `Maximum ${max} caractères`).nullish().transform((v) => (v ? v : null));
const choix = (valeurs) =>
  z.enum(valeurs).nullish().transform((v) => v ?? null);

export const sessionSchema = z.object({
  title: z.string().trim().min(3, 'Le titre doit contenir au moins 3 caractères').max(160, 'Titre trop long (160 max)'),
  summary: texte(300),
  description: texte(4000),
  objectives: z.array(z.string().trim().min(1).max(200)).max(10, '10 objectifs maximum').nullish().transform((v) => v ?? []),
  outline: z
    .array(
      z.object({
        title: z.string().trim().min(1, "Chaque étape doit avoir un titre").max(120),
        description: texte(500),
        minutes: z.number().int().min(5, 'Une étape dure au moins 5 minutes').max(480).nullish().transform((v) => v ?? null),
      }),
    )
    .max(12, '12 étapes maximum')
    .nullish()
    .transform((v) => v ?? []),
  prerequisites: texte(500),
  audience: texte(300),
  level: choix(NIVEAUX),
  language: texte(40),
  format: choix(FORMATS),
  mode: choix(MODES),
  platform: texte(80),
  meetingLink: z.union([urlWeb, z.literal(''), z.null()]).optional().transform((v) => v || null),
  location: texte(200),
  deliveryNotes: texte(2000),
  materials: texte(800),
});

export const lienSchema = z.object({
  title: z.string().trim().min(2, 'Le titre doit contenir au moins 2 caractères').max(160),
  url: urlWeb,
  description: texte(300),
});

/** Transforme une erreur zod en une phrase lisible. */
export function messageErreur(erreur) {
  return erreur.issues
    .map((i) => (i.path.length ? `${i.path.join('.')} : ${i.message}` : i.message))
    .join(' · ');
}

/**
 * Pourcentage de complétude de la fiche (9 critères) et liste de ce qui manque.
 * `s` = séance au format API (camelCase), `nbSupports` = nombre de PDF/liens.
 */
export function calculerCompletude(s, nbSupports = 0) {
  const criteres = [
    ['description détaillée', (s.description || '').length >= 80],
    ['objectifs', (s.objectives || []).length > 0],
    ['programme', (s.outline || []).length > 0],
    ['public visé', !!s.audience],
    ['niveau', !!s.level],
    ['format et mode', !!s.format && !!s.mode],
    ['lieu ou lien de réunion', !!(s.meetingLink || s.location || s.platform)],
    ['déroulement de la séance', !!s.deliveryNotes],
    ['supports (PDF ou liens)', nbSupports > 0],
  ];
  const missing = criteres.filter(([, ok]) => !ok).map(([nom]) => nom);
  return { percent: Math.round(((criteres.length - missing.length) / criteres.length) * 100), missing };
}
