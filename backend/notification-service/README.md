# notification-service (Node.js / Express — port 8089)
Notifications « dans l'application » : cloche du site web et onglet **Alertes** de l'appli Android.

Les services existants ne sont pas modifiés. Après une action sur un échange, le client appelle
`POST /api/notifications/exchange/{id}/sync` ; le service relit le **statut réel** de l'échange dans
`exchange-service` et notifie la bonne personne (idempotent : une notification par statut).

| Méthode | Route | Rôle |
|---|---|---|
| GET | `/api/notifications` | mes notifications + compteur de non lues |
| GET | `/api/notifications/unread-count` | compteur |
| PATCH | `/api/notifications/{id}/read` · `/read-all` | marquer comme lu |
| DELETE | `/api/notifications/{id}` | supprimer |
| POST | `/api/notifications/exchange/{id}/sync` | notifier l'autre participant du statut actuel |
| POST | `/api/notifications/exchange/{id}/session-updated` | fiche de séance modifiée |

`npm install && npm start` · `npm test` (4 tests). SQLite en dev, MySQL avec `DB_CLIENT=mysql2`.
