-- V2 : ajout du lien de réunion (généré à l'acceptation de l'échange)
ALTER TABLE exchanges
    ADD COLUMN meeting_link VARCHAR(500) NULL;