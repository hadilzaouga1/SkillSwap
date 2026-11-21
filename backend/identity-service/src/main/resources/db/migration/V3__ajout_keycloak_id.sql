ALTER TABLE utilisateur
    ADD COLUMN keycloak_id VARCHAR(36) NULL,
    ADD CONSTRAINT uk_utilisateur_keycloak_id UNIQUE (keycloak_id);