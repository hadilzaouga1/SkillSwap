# session-service (Node.js / Express)

Microservice des **fiches de séance** SkillSwap : pour chaque échange, le prestataire décrit le cours
(description, objectifs, programme, public, niveau, format, mode, plateforme/lieu, déroulement, matériel)
et les deux participants peuvent y joindre des **PDF** et des **liens**.

- Port **8087**, derrière l'API Gateway (`/api/sessions/**`, route déjà ajoutée dans `api-gateway`).
- La Gateway valide le JWT et envoie `X-User-Id` / `X-User-Role` : ce service leur fait confiance.
- Il interroge `exchange-service` (`GET /api/exchanges/{id}`) pour savoir qui est prestataire / demandeur.
- Base : SQLite en développement (aucune installation), MySQL en production (`DB_CLIENT=mysql2`).

## Lancer
```bash
cd backend/session-service
npm install
cp .env.example .env     # facultatif
npm start                # http://localhost:8087/health
npm test                 # 8 tests (droits, validation, PDF, liens, complétude)
```

## API (clé = id de l'échange)
| Méthode | Route | Qui |
|---|---|---|
| GET | `/api/sessions/by-exchange/{id}` | participants, modérateurs (lecture) |
| PUT | `/api/sessions/by-exchange/{id}` | prestataire uniquement |
| GET | `/api/sessions/summaries?exchangeIds=1,2` | participants |
| POST | `.../resources/link` `{title,url}` | participants |
| POST | `.../resources/pdf` (multipart `file`, `title`) | participants |
| GET | `.../resources/{rid}/file` | participants, modérateurs |
| DELETE | `.../resources/{rid}` | auteur du support ou prestataire |

Sécurité : PDF vérifié par signature `%PDF-` (415 sinon), 10 Mo max (413), nom aléatoire sur le disque,
téléchargement uniquement via route authentifiée, liens limités à http/https, 15 supports max par séance,
fiche non modifiable si l'échange est refusé/annulé (409).
