ALTER TABLE parametres_magasin
    ADD COLUMN nom VARCHAR(150) NOT NULL DEFAULT 'QUINCAILLERIE MVOGT',
    ADD COLUMN domaine VARCHAR(255) NOT NULL DEFAULT 'Materiaux de construction & quincaillerie generale';
