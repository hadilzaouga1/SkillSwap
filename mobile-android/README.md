# SkillSwap – application Android (Kotlin + Jetpack Compose)

## Ouvrir et lancer
1. Android Studio (Koala ou plus récent) → **Open** → dossier `mobile-android/`. Laisser Gradle se synchroniser
   (JDK 17 ; Android Studio propose de générer le wrapper si besoin).
2. Démarrer le backend (Eureka, identity, skill, exchange, credit, **session-service**, **api-gateway** 8085).
3. Lancer sur un émulateur : l'app appelle `http://10.0.2.2:8085/api/` (= `localhost` du PC).
   Téléphone réel : changer `API_BASE_URL` dans `app/build.gradle.kts` (IP du PC) .
4. Compte de démo pré-rempli : `hadil@univ.tn` / `Password123!`.

## Écrans
Connexion → Accueil (Catalogue + Mes échanges, actions accepter/refuser/terminer/confirmer/annuler)
→ **Fiche de séance** (description, objectifs, programme, public, déroulement, lien de réunion,
supports PDF/liens : ouvrir, ajouter, supprimer ; édition par le prestataire).

## Structure
`data/` (Models, Api Retrofit, Repository + DataStore du jeton) · `ui/` (AppNav, theme, screens).
Même palette que le web (vert sauge `#5B8A72`).

> Ce code n'a pas pu être compilé dans l'environnement de génération (accès Google/Maven bloqué) :
> à la première synchronisation, corrigez d'éventuelles petites erreurs de version/import.
