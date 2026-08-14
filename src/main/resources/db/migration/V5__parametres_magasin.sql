CREATE TABLE parametres_magasin (
    id BIGINT PRIMARY KEY,
    telephone VARCHAR(30) NOT NULL,
    ville VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL
) ENGINE = InnoDB;

-- Ligne unique (id fixe = 1) portant les coordonnees du magasin affichees
-- sur l'en-tete des recus de vente.
INSERT INTO parametres_magasin (id, telephone, ville, email)
VALUES (1, '+237 677 28 29 27', 'Yaounde', 'qmvogt@gmail.com');
