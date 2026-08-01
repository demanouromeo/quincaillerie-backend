package com.mvogt.quincaillerie.vente;

import java.math.BigDecimal;

public record LigneVenteResponse(
        Long produitId,
        String produitNom,
        int quantite,
        BigDecimal prixVenteUnitaire,
        BigDecimal sousTotal
) {
    public static LigneVenteResponse from(LigneVente ligne) {
        return new LigneVenteResponse(
                ligne.getProduit().getId(),
                ligne.getProduit().getNom(),
                ligne.getQuantite(),
                ligne.getPrixVenteUnitaire(),
                ligne.sousTotal());
    }
}
