# Sprint 2 — services Node, agent IA et application Android

## 1. Architecture (microservices)
| Service | Techno | Port | Rôle |
|---|---|---|---|
| api-gateway | Spring Cloud Gateway | 8085 | valide le JWT, pose `X-User-Id` / `X-User-Role`, route tout |
| identity / skill / exchange / credit | Spring Boot | 8081 / 8084 / 8082 / 8086 | (existants, **non modifiés**) |
| session-service | Express | 8087 | fiche de séance, PDF, liens |
| **trust-service** | Express | 8088 | avis et réputation |
| **notification-service** | Express | 8089 | notifications |
| **ai-service** | Express | 8090 | agent IA |

Seul changement côté Java : 3 routes ajoutées dans `api-gateway/src/main/resources/application.yml`
(`/api/reviews/**`, `/api/notifications/**`, `/api/ai/**`). Aucun service existant n'est modifié.

## 2. L'agent IA partout dans le projet
| Où | Ce que fait l'agent |
|---|---|
| Web — bouton ✨ flottant (toutes les pages) | répond sur le fonctionnement, recommande des compétences, adapte ses suggestions à la page |
| Web — fiche de séance | « Générer un brouillon » (description, objectifs, programme, déroulement…) + « Reformuler » |
| Web — nouvelle compétence | rédige ou améliore la description à partir du titre |
| Web — demande d'échange | améliore le message au prestataire |
| Web — avis | reformule le commentaire |
| Mobile — barre du haut (✨) | assistant (chat), reçoit aussi un texte partagé depuis une autre appli |
| Mobile — fiche de séance, demande, avis | mêmes aides que le web |
| Backend | `ai-service` unique, sans clé = mode local, avec `ANTHROPIC_API_KEY` = Claude |

## 3. Notifications et avis sans toucher aux services existants
Après une action sur un échange, web et mobile appellent `POST /api/notifications/exchange/{id}/sync`.
Le service relit le statut réel dans exchange-service : le client ne peut pas fabriquer de fausse notification.
Les avis ne sont possibles que sur un échange `COMPLETED`, une fois par participant (vérifié côté serveur).

## 4. Android — où voir chaque notion du cours
| Notion | Où dans `mobile-android` |
|---|---|
| **Activities** (7) | `ui/LoginActivity` (launcher), `RegisterActivity`, `HomeActivity`, `SkillDetailActivity`, `SeanceActivity`, `AvisActivity`, `AssistantActivity` |
| **Intent explicite** | `HomeActivity` → `intentVers<SkillDetailActivity>(…)` ; `LoginActivity` → `HomeActivity` ; `Extras.kt` |
| **Extras** (données entre écrans) | `Extras.SKILL_ID`, `EXCHANGE_ID`… lus avec `intent.getLongExtra(…)` |
| **Résultat d'Activity** | `registerForActivityResult` dans `HomeActivity` ; `setResult(RESULT_OK)` dans les enfants |
| **Pile d'écrans** | `finish()`, `FLAG_ACTIVITY_CLEAR_TASK` (déconnexion, inscription) |
| **Intent implicite** | `ACTION_VIEW` (liens, `Extras.kt`), `ACTION_SEND` (partager une fiche), `ACTION_INSERT` (ajouter au calendrier, `HomeActivity`), PDF via `FileProvider` |
| **Intent-filter** | `AssistantActivity` reçoit `ACTION_SEND text/plain` (menu « Partager ») — voir `AndroidManifest.xml` |
| **Listes (composables)** | `LazyColumn` : `CatalogueTab`, `EchangesTab`, `AlertesTab`, `ProfilTab`, `AssistantScreen` ; `LazyRow` : suggestions de l'assistant |
| **Composables** | `ui/screens/*` (Carte, Etiquette, onglets, formulaires), thème Material 3 dans `ui/theme` |
| **Réseau / données** | Retrofit + kotlinx-serialization (`data/Api.kt`, `Models.kt`), jeton dans DataStore (`Repository.kt`) |

## 5. Démarrer
1. Services Spring Boot comme d'habitude (Eureka, identity, skill, exchange, credit, gateway).
2. Services Node : `scripts\demarrer-services-node.ps1` (Windows) ou `scripts/demarrer-services-node.sh`.
3. Web : `cd frontend && ng serve` — Android : ouvrir `mobile-android/` dans Android Studio.
4. (Facultatif) `set ANTHROPIC_API_KEY=...` avant de lancer `ai-service` pour utiliser Claude.

## 6. Vérifié / non vérifié
- Vérifié ici : tests automatiques des 3 nouveaux services (4 + 3 + 7), démarrage des services, `ng build` du frontend.
- Non vérifié ici (outils bloqués) : compilation Android (Gradle/Kotlin), services Java + MySQL, Docker, appels réels à l'API Claude.
