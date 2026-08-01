CREATE TABLE utilisateurs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    login VARCHAR(100) NOT NULL,
    mot_de_passe VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    actif BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_utilisateurs_login UNIQUE (login)
) ENGINE = InnoDB;

CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(150) NOT NULL,
    CONSTRAINT uk_categories_nom UNIQUE (nom)
) ENGINE = InnoDB;

CREATE TABLE fournisseurs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nom VARCHAR(200) NOT NULL,
    contact VARCHAR(150),
    adresse VARCHAR(255)
) ENGINE = InnoDB;

CREATE TABLE produits (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    reference VARCHAR(50) NOT NULL,
    nom VARCHAR(200) NOT NULL,
    categorie_id BIGINT,
    unite VARCHAR(30) NOT NULL,
    prix_achat DECIMAL(12, 2) NOT NULL,
    prix_vente DECIMAL(12, 2) NOT NULL,
    seuil_alerte INT NOT NULL DEFAULT 0,
    stock_actuel INT NOT NULL DEFAULT 0,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_produits_reference UNIQUE (reference),
    CONSTRAINT fk_produits_categorie FOREIGN KEY (categorie_id) REFERENCES categories (id)
) ENGINE = InnoDB;
