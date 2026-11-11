# Restructuration SkillSwap — guide

## Ce qui a été fait dans cette archive

- `services/skill-service` a été déplacé vers `backend/skill-service`.
- L'ancien `backend` a été déplacé vers `backend/identity-service`.
- Les contrôleurs, DTO, entités, repositories et service de compétences dupliqués ont été retirés de l'Identity Service. Les versions conservées sont dans Skill Service.
- Le POM de Skill Service a été aligné sur Spring Boot 3.2.5 / Java 21 et Flyway a été ajouté.
- Les deux services ont des configurations de base distinctes et leurs migrations propres.
- Les anciennes migrations du backend ont été archivées dans `docs/legacy-schema-reference/` car elles créaient des tables de plusieurs domaines.
- Un API Gateway Spring Cloud a été ajouté sur le port 8080.
- Angular utilise le Gateway pour les URLs utilisateur et compétences.

## Ce qui n'est PAS terminé

- JWT / Spring Security et authentification complète.
- Remplacement du header `X-User-Id` par un principal JWT validé.
- Extraction des domaines échange/crédit/notification/IA en services séparés.
- Docker Compose et tests d'intégration inter-services.
- Migration des données historiques de l'ancienne base `skillswap`.

## Avertissement de données

Les nouvelles migrations sont faites pour des bases neuves. Ne supprimez pas les anciennes bases. Exportez-les avant toute migration. Les migrations Flyway déjà exécutées ne doivent pas être modifiées sur une base existante.

## Contrôles à effectuer sur votre machine

Les dépendances Maven et npm doivent être téléchargées dans l'environnement local. Exécuter `mvn clean test` dans chacun des trois projets puis `npm ci` et `npm run build` dans frontend. Cette archive a été restructurée et vérifiée statiquement, mais les tests d'exécution n'ont pas été exécutés ici.
