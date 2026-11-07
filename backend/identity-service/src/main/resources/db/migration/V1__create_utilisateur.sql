CREATE TABLE utilisateur (
    id BIGINT NOT NULL AUTO_INCREMENT,
    nom VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    universite VARCHAR(255),
    filiere VARCHAR(255),
    note_moyenne DOUBLE,
    role VARCHAR(20) NOT NULL,
    statut VARCHAR(20) NOT NULL,
    CONSTRAINT pk_utilisateur PRIMARY KEY (id),
    CONSTRAINT uk_utilisateur_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
