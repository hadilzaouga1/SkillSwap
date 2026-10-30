CREATE TABLE categorie (
                           id BIGSERIAL PRIMARY KEY,
                           nom VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE utilisateur (
                             id BIGSERIAL PRIMARY KEY,
                             nom VARCHAR(80) NOT NULL,
                             email VARCHAR(120) NOT NULL UNIQUE,
                             universite VARCHAR(120),
                             filiere VARCHAR(120),
                             note_moyenne NUMERIC(2,1) DEFAULT 0
);

CREATE TABLE competence (
                            id BIGSERIAL PRIMARY KEY,
                            titre VARCHAR(120) NOT NULL,
                            description TEXT,
                            niveau VARCHAR(20) NOT NULL,
                            categorie_id BIGINT NOT NULL REFERENCES categorie(id),
                            etudiant_id BIGINT NOT NULL REFERENCES utilisateur(id)
);