CREATE TABLE approvisionnements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    date_appro DATETIME NOT NULL,
    fournisseur_id BIGINT NOT NULL,
    gestionnaire_id BIGINT NOT NULL,
    CONSTRAINT fk_approvisionnements_fournisseur FOREIGN KEY (fournisseur_id) REFERENCES fournisseurs (id),
    CONSTRAINT fk_approvisionnements_gestionnaire FOREIGN KEY (gestionnaire_id) REFERENCES utilisateurs (id)
) ENGINE = InnoDB;

CREATE TABLE lignes_approvisionnement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    approvisionnement_id BIGINT NOT NULL,
    produit_id BIGINT NOT NULL,
    quantite INT NOT NULL,
    prix_achat_unitaire DECIMAL(12, 2) NOT NULL,
    CONSTRAINT fk_lignes_appro_approvisionnement FOREIGN KEY (approvisionnement_id) REFERENCES approvisionnements (id),
    CONSTRAINT fk_lignes_appro_produit FOREIGN KEY (produit_id) REFERENCES produits (id)
) ENGINE = InnoDB;
