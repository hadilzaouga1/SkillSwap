import { FAQ, SUGGESTIONS_PAR_PAGE, MOTS_VIDES } from './knowledge.js';

export const sansAccents = (s) => s.normalize('NFD').replace(/[̀-ͯ]/g, '').toLowerCase();

export function motsCles(message) {
  return sansAccents(message).replace(/[^a-z0-9+#.\s]/g, ' ').split(/\s+/)
    .filter((m) => m.length >= 2 && !MOTS_VIDES.has(m)).slice(0, 4);
}

const INTENTION_RECHERCHE = /(apprendre|cherche|trouver|veux|voudrais|besoin|cours de|aide en|reviser|progresser|competence)/;

export function suggestionsPour(page) {
  const cle = Object.keys(SUGGESTIONS_PAR_PAGE).find((k) => k !== 'defaut' && String(page || '').includes(k));
  return SUGGESTIONS_PAR_PAGE[cle] || SUGGESTIONS_PAR_PAGE.defaut;
}

export function repondreLocal(message, context = {}, competences = []) {
  const m = sansAccents(message);

  if (competences.length) {
    const liste = competences.slice(0, 5).map((c) => `• ${c.titre}${c.niveau ? ` (${c.niveau.toLowerCase()})` : ''}`).join('\n');
    return { reply: `Voici ce que j'ai trouvé dans le catalogue :\n${liste}\nOuvre la compétence qui t'intéresse et clique sur « Demander un échange ».`,
      suggestions: ['Comment demander un échange ?', 'Comment fonctionnent les crédits ?'], skills: competences.slice(0, 5) };
  }
  if (INTENTION_RECHERCHE.test(m) && motsCles(message).length) {
    return { reply: "Je n'ai trouvé aucune compétence correspondante pour le moment. Essaie un mot plus général, ou propose toi-même une compétence : d'autres étudiants chercheront peut-être la même chose.",
      suggestions: ['Comment proposer une compétence ?'], skills: [] };
  }
  let meilleur = null; let score = 0;
  for (const f of FAQ) {
    const s = f.mots.filter((mot) => m.includes(mot)).length;
    if (s > score) { meilleur = f; score = s; }
  }
  if (meilleur) return { reply: meilleur.reponse, suggestions: meilleur.suggestions, skills: [] };
  return { reply: "Je ne suis pas sûr de bien comprendre. Je peux t'expliquer les crédits, les échanges, les fiches de séance ou les avis, ou te recommander des compétences du catalogue (dis par exemple « je veux apprendre Java »).",
    suggestions: suggestionsPour(context.page), skills: [] };
}

const arrondi5 = (n) => Math.max(5, Math.round(n / 5) * 5);

export function brouillonSeance({ skillTitle, level, durationHours, mode, extra }) {
  const titre = skillTitle.trim();
  const total = Math.max(30, Math.round((durationHours || 1) * 60));
  const parts = [['Accueil et objectifs', 0.1], ['Notions essentielles', 0.3], ['Pratique guidée', 0.35], ['Mise en situation', 0.15], ['Bilan et questions', 0.1]];
  const outline = parts.map(([t, p]) => ({ title: t, minutes: arrondi5(total * p) }));
  const complement = extra ? ` ${extra.trim().replace(/[.!?]*$/, '.')}` : '';
  return {
    title: `${titre} : séance d'initiation`.slice(0, 160),
    summary: `Une séance pratique pour découvrir ${titre} pas à pas.`,
    description: `Cette séance de ${durationHours || 1} h t'aide à comprendre et à pratiquer ${titre}. Nous commencerons par les notions essentielles, puis nous mettrons en pratique avec des exercices guidés et une courte mise en situation.${complement} À la fin, tu sauras quoi faire pour continuer à progresser seul.`,
    objectives: [`Comprendre les bases de ${titre}`, `Réaliser un premier exercice concret en autonomie`, `Savoir où continuer à apprendre`],
    outline,
    prerequisites: level === 'AVANCE' ? 'Bonne maîtrise des bases' : 'Aucun prérequis, juste de la curiosité',
    audience: 'Étudiants débutants ou curieux',
    level: level || 'DEBUTANT',
    format: (durationHours || 1) <= 2 ? 'ATELIER' : 'COURS',
    mode: mode || 'ONLINE',
    deliveryNotes: 'Explications courtes suivies d\'exercices en direct ; partage d\'écran ; temps de questions à la fin.',
    materials: 'Un ordinateur et de quoi prendre des notes',
  };
}

export function ameliorerLocal(texte, kind) {
  let t = texte.replace(/\s+/g, ' ').replace(/\s+([,.;:!?])/g, '$1').replace(/([,.;:!?])(?=\p{L})/gu, '$1 ').trim();
  t = t.charAt(0).toUpperCase() + t.slice(1);
  if (!/[.!?…]$/.test(t)) t += '.';
  const tips = [];
  const min = { description: 80, skill: 60, request: 30, review: 20 }[kind] ?? 30;
  if (t.length < min) tips.push(`Ajoute des détails : ce texte est un peu court (vise au moins ${min} caractères).`);
  if (kind === 'description') tips.push('Précise ce que l\'étudiant saura faire à la fin de la séance.');
  if (kind === 'skill') tips.push('Indique ton niveau et un exemple concret de ce que tu peux enseigner.');
  if (kind === 'request') tips.push('Dis ce que tu sais déjà et ce que tu voudrais obtenir de la séance.');
  if (kind === 'review') tips.push('Reste factuel et bienveillant : mentionne ce qui t\'a aidé.');
  return { text: t, tips };
}

export function brouillonCompetence({ titre, niveau }) {
  const t = titre.trim();
  const n = { DEBUTANT: 'les bases', INTERMEDIAIRE: 'un niveau intermédiaire', AVANCE: 'un niveau avancé' }[niveau] || 'les bases';
  return {
    description: `Je propose de t'aider à progresser en ${t} (${n}). Nous partirons de tes objectifs, avec des explications simples et des exercices pratiques pour que tu puisses avancer seul ensuite.`,
    disponibilites: 'En soirée en semaine et le week-end (à adapter)',
  };
}
