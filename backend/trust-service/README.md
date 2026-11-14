# trust-service (Node.js / Express — port 8088)
Avis et réputation. Après un échange **terminé**, chaque participant note l'autre (1 à 5 + commentaire), une seule fois.
Badges : « Bien noté » (moyenne ≥ 4) et « Expert fiable » (≥ 3 avis, moyenne ≥ 4,5).

| Méthode | Route | Rôle |
|---|---|---|
| POST | `/api/reviews/exchange/{id}` | publier mon avis (échange COMPLETED, participant) |
| GET | `/api/reviews/exchange/{id}` | mon avis + avis reçu (visible après avoir donné le sien) |
| GET | `/api/reviews/user/{id}` | réputation publique (moyenne, répartition, badge, avis anonymes) |
| GET | `/api/reviews/summary?userIds=1,2` | résumés pour le catalogue |

`npm install && npm start` · `npm test` (3 tests).
