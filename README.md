# SkillSwap — architecture réorganisée

Cette archive réorganise le code fourni en deux services Spring Boot indépendants, un API Gateway et le frontend Angular.

## Structure

```text
SkillSwap/
├── backend/
│   ├── api-gateway/       # Spring Cloud Gateway, port 8080
│   ├── identity-service/  # profils/utilisateurs, port 8081
│   └── skill-service/     # compétences/catégories, port 8082
├── frontend/              # Angular
├── docs/
├── infrastructure/       # à compléter avec Docker Compose
└── README.md
```

## Prérequis

- JDK 21
- Maven 3.9+ (ou Maven Wrapper lorsqu'il est présent)
- MySQL 8+
- Node.js compatible avec le `package.json` du frontend

## Bases de données

- `skillswap_identity` : table `utilisateur` uniquement.
- `skillswap_skill` : tables `categorie` et `competence`.

Les deux services utilisent Flyway. Hibernate est configuré en `validate`.
Les anciennes migrations ont été déplacées dans `docs/legacy-schema-reference/` à titre documentaire et ne sont pas exécutées.

## Démarrage local

Créer les bases MySQL ou laisser `createDatabaseIfNotExist=true` les créer avec un compte autorisé.

Démarrer dans trois terminaux distincts :

```powershell
cd backend/identity-service
mvn spring-boot:run
```

```powershell
cd backend/skill-service
mvn spring-boot:run
```

```powershell
cd backend/api-gateway
mvn spring-boot:run
```

Puis démarrer Angular :

```powershell
cd frontend
npm ci
npm start
```

Le frontend appelle le Gateway sur `http://localhost:8080/api`.

## Swagger

- Identity Service : http://localhost:8081/swagger-ui.html
- Skill Service : http://localhost:8082/swagger-ui.html

## Limites importantes à connaître

1. Le mécanisme `X-User-Id` présent dans le code existant est conservé temporairement pour ne pas casser les écrans. Il n'est PAS une authentification sécurisée. Il doit être remplacé par JWT et Spring Security avant toute mise en production.
2. L'API Gateway route les URL, mais ne valide pas encore les JWT.
3. L'Identity Service actuel gère les profils, pas encore l'inscription/connexion complète avec mot de passe.
4. Les services Exchange, Credit, Notification et AI ne sont pas créés dans cette archive : ils doivent être extraits du modèle métier et développés séparément après stabilisation des deux premiers services.
5. L'archive ne contient volontairement pas `.git`, `node_modules`, `.angular`, `.idea` ou `target`. Ne remplace pas ton dépôt Git original sans sauvegarde.

## Migration depuis l'ancienne base

Les migrations originales sont archivées dans `docs/legacy-schema-reference/`. Les nouvelles migrations sont prévues pour des bases propres `skillswap_identity` et `skillswap_skill`. Ne pointe pas ces migrations vers ton ancienne base `skillswap` sans plan de migration et sauvegarde préalable.

## Vérification

```powershell
cd backend/identity-service; mvn clean test
cd ../skill-service; mvn clean test
cd ../api-gateway; mvn clean test
cd ../../frontend; npm ci; npm run build
```
