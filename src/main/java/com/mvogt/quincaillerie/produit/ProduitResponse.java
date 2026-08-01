package com.mvogt.quincaillerie.produit;

import java.math.BigDecimal;

public record ProduitResponse(
        Long id,
        String reference,
        String nom,
        Long categorieId,
        String categorieNom,
        String unite,
        BigDecimal prixAchat,
        BigDecimal prixVente,
        int seuilAlerte,
        int stockActuel
) {
    public static ProduitResponse from(Produit produit) {
        Categorie categorie = produit.getCategorie();
        return new ProduitResponse(
                produit.getId(),
                produit.getReference(),
                produit.getNom(),
                categorie != null ? categorie.getId() : null,
                categorie != null ? categorie.getNom() : null,
                produit.getUnite(),
                produit.getPrixAchat(),
                produit.getPrixVente(),
                produit.getSeuilAlerte(),
                produit.getStockActuel());
    }
}
