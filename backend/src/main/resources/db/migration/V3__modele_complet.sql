-- =====================================================================
-- V3 : modèle de données complet 
ALTER TABLE utilisateur
    ADD COLUMN role   VARCHAR(20) NOT NULL DEFAULT 'ETUDIANT',
    ADD COLUMN statut VARCHAR(20) NOT NULL DEFAULT 'ACTIF';

-- Validation par le modérateur (EN_ATTENTE, VALIDEE, REFUSEE) + disponibilités
ALTER TABLE competence
    ADD COLUMN statut_validation VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    ADD COLUMN disponibilites    VARCHAR(255);

-- Les compétences déjà présentes (données de test V2) sont considérées validées
UPDATE competence SET statut_validation = 'VALIDEE';

CREATE TABLE compte_credit (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    etudiant_id BIGINT NOT NULL UNIQUE,
    solde       DECIMAL(8,2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_compte_etudiant FOREIGN KEY (etudiant_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE echange (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    demandeur_id   BIGINT NOT NULL,
    prestataire_id BIGINT NOT NULL,
    competence_id  BIGINT NOT NULL,
    date_demande   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_seance    DATETIME NULL,
    duree_heures   DECIMAL(4,2) NOT NULL,
    statut         VARCHAR(20) NOT NULL DEFAULT 'EN_ATTENTE',
    CONSTRAINT fk_echange_demandeur   FOREIGN KEY (demandeur_id)   REFERENCES utilisateur(id),
    CONSTRAINT fk_echange_prestataire FOREIGN KEY (prestataire_id) REFERENCES utilisateur(id),
    CONSTRAINT fk_echange_competence  FOREIGN KEY (competence_id)  REFERENCES competence(id),
    CONSTRAINT chk_echange_duree CHECK (duree_heures > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Mouvement de crédits. type : BLOCAGE, TRANSFERT, REMBOURSEMENT, BIENVENUE
CREATE TABLE transaction_credit (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    compte_id          BIGINT NOT NULL,
    echange_id         BIGINT NULL,
    montant            DECIMAL(8,2) NOT NULL,
    type               VARCHAR(20) NOT NULL,
    date_transaction   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_compte  FOREIGN KEY (compte_id)  REFERENCES compte_credit(id),
    CONSTRAINT fk_transaction_echange FOREIGN KEY (echange_id) REFERENCES echange(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE evaluation (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    echange_id      BIGINT NOT NULL,
    auteur_id       BIGINT NOT NULL,
    destinataire_id BIGINT NOT NULL,
    note            TINYINT NOT NULL,
    commentaire     TEXT,
    date_evaluation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_eval_echange      FOREIGN KEY (echange_id)      REFERENCES echange(id),
    CONSTRAINT fk_eval_auteur       FOREIGN KEY (auteur_id)       REFERENCES utilisateur(id),
    CONSTRAINT fk_eval_destinataire FOREIGN KEY (destinataire_id) REFERENCES utilisateur(id),
    CONSTRAINT chk_eval_note CHECK (note BETWEEN 1 AND 5),
    CONSTRAINT uq_eval_par_echange UNIQUE (echange_id, auteur_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE signalement (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    auteur_id          BIGINT NOT NULL,
    moderateur_id      BIGINT NULL,
    type_cible         VARCHAR(20) NOT NULL,
    cible_id           BIGINT NOT NULL,
    motif              TEXT NOT NULL,
    date_signalement   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    statut             VARCHAR(20) NOT NULL DEFAULT 'OUVERT',
    analyse_ia         TEXT NULL,
    CONSTRAINT fk_signalement_auteur      FOREIGN KEY (auteur_id)     REFERENCES utilisateur(id),
    CONSTRAINT fk_signalement_moderateur  FOREIGN KEY (moderateur_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE notification (
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    destinataire_id   BIGINT NOT NULL,
    message           VARCHAR(255) NOT NULL,
    date_notification DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    lue               BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_notification_destinataire FOREIGN KEY (destinataire_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE interaction_ia (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    utilisateur_id   BIGINT NOT NULL,
    type             VARCHAR(30) NOT NULL,
    requete          TEXT NOT NULL,
    reponse          TEXT,
    date_interaction DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_interaction_utilisateur FOREIGN KEY (utilisateur_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- Compte de crédits (3 crédits de bienvenue) pour les utilisateurs de test
INSERT INTO compte_credit (etudiant_id, solde) SELECT id, 3.00 FROM utilisateur;
