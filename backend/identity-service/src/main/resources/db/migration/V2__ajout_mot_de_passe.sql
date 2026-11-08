-- V2 : mot de passe chiffré (BCrypt) pour l'authentification.
-- Colonne NULLABLE : les utilisateurs déjà présents reçoivent leur mot de passe
-- au démarrage (voir ComptesParDefautInitializer, mode développement).
-- RÈGLE FLYWAY : ne jamais modifier une migration déjà poussée sur Git.
ALTER TABLE utilisateur
    ADD COLUMN mot_de_passe VARCHAR(255) NULL;
