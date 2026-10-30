INSERT INTO categorie (nom) VALUES ('Langues'), ('Informatique'), ('Design');

INSERT INTO utilisateur (nom, email, universite) VALUES
                                                     ('Hadil', 'hadil@univ.tn', 'Tunis'),
                                                     ('Roua', 'roua@univ.tn', 'Tunis');

INSERT INTO competence (titre, description, niveau, categorie_id, etudiant_id) VALUES
                                                                                   ('Cours d''anglais', 'Conversation', 'INTERMEDIAIRE', 1, 1),
                                                                                   ('Initiation Python', 'Bases', 'DEBUTANT', 2, 2);