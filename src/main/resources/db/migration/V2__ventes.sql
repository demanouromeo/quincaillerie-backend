CREATE TABLE ventes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_vente DATETIME NOT NULL,
    vendeur_id BIGINT NOT NULL,
    montant_total DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_ventes_vendeur FOREIGN KEY (vendeur_id) REFERENCES utilisateurs (id)
) ENGINE = InnoDB;

CREATE TABLE lignes_vente (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vente_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    quantite INT NOT NULL,
    prix_vente_unitaire DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_lignes_vente_vente FOREIGN KEY (vente_id) REFERENCES ventes (id),
    CONSTRAINT fk_lignes_vente_produit FOREIGN KEY (produit_id) REFERENCES produits (id)
) ENGINE = InnoDB;

CREATE TABLE mouvements_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    produit_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL,
    quantite INT NOT NULL,
    date DATETIME NOT NULL,
    reference VARCHAR(100),
    CONSTRAINT fk_mouvements_stock_produit FOREIGN KEY (produit_id) REFERENCES produits (id)
) ENGINE = InnoDB;
