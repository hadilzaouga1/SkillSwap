# ai-service — l'agent IA de SkillSwap (Node.js / Express — port 8090)
Utilisé par **tout le projet** (web et mobile). Deux modes :
- **sans clé** : mode local déterministe (FAQ SkillSwap, recommandations dans le catalogue, brouillons structurés) — gratuit, fonctionne hors ligne ;
- **avec `ANTHROPIC_API_KEY`** : l'agent utilise Claude (modèle `AI_MODEL`). Si l'API est en panne ou renvoie un JSON invalide, repli automatique sur le mode local.

La clé se met dans l'environnement (jamais dans git) : `ANTHROPIC_API_KEY=... npm start`.

| Méthode | Route | Rôle |
|---|---|---|
| POST | `/api/ai/chat` | assistant conversationnel (contexte = page) + recommandation de compétences validées |
| POST | `/api/ai/session-draft` | brouillon complet de fiche de séance |
| POST | `/api/ai/skill-draft` | description d'une compétence à partir du titre |
| POST | `/api/ai/improve-text` | reformulation + conseils (description, compétence, demande, avis) |
| GET | `/api/ai/status` | fournisseur actif |

Sécurité : authentification via la Gateway, 30 requêtes/minute/utilisateur (429 sinon), entrées bornées (zod),
sorties du LLM validées avant usage, texte de l'utilisateur traité comme donnée (jamais comme instruction).
`npm install && npm start` · `npm test` (7 tests).
