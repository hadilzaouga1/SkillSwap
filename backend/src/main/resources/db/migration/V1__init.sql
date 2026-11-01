CREATE TABLE categorie (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(80) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE utilisateur (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(80) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    universite VARCHAR(120),
    filiere VARCHAR(120),
    note_moyenne DECIMAL(2,1) DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE competence (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    titre VARCHAR(120) NOT NULL,
    description TEXT,
    niveau VARCHAR(20) NOT NULL,
    categorie_id BIGINT NOT NULL,
    etudiant_id BIGINT NOT NULL,
    CONSTRAINT fk_competence_categorie FOREIGN KEY (categorie_id) REFERENCES categorie(id),
    CONSTRAINT fk_competence_etudiant FOREIGN KEY (etudiant_id) REFERENCES utilisateur(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;