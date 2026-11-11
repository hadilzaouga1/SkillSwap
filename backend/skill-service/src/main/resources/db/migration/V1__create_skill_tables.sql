CREATE TABLE categorie (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    CONSTRAINT pk_categorie PRIMARY KEY (id),
    CONSTRAINT uk_categorie_nom UNIQUE (nom)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE competence (
    id BIGINT NOT NULL AUTO_INCREMENT,
    titre VARCHAR(255) NOT NULL,
    description TEXT,
    niveau VARCHAR(20) NOT NULL,
    statut_validation VARCHAR(20) NOT NULL,
    disponibilites VARCHAR(255),
    categorie_id BIGINT NOT NULL,
    etudiant_id BIGINT NOT NULL,
    CONSTRAINT pk_competence PRIMARY KEY (id),
    CONSTRAINT fk_competence_categorie
        FOREIGN KEY (categorie_id) REFERENCES categorie(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_competence_etudiant ON competence(etudiant_id);
CREATE INDEX idx_competence_categorie ON competence(categorie_id);
CREATE INDEX idx_competence_statut ON competence(statut_validation);
