-- V3 : ajout du support PDF (document attaché) à la table competence
ALTER TABLE competence
    ADD COLUMN document_url  VARCHAR(500) NULL,
    ADD COLUMN document_name VARCHAR(255) NULL,
    ADD COLUMN stored_name   VARCHAR(255) NULL,
    ADD COLUMN document_size BIGINT       NULL;