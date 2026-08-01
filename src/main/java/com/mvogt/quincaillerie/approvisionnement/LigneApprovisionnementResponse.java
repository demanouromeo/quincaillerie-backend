package com.mvogt.quincaillerie.approvisionnement;

import java.math.BigDecimal;

public record LigneApprovisionnementResponse(
        Long produitId,
        String produitNom,
        int quantite,
        BigDecimal prixAchatUnitaire,
        BigDecimal sousTotal
) {
    public static LigneApprovisionnementResponse from(LigneApprovisionnement ligne) {
        return new LigneApprovisionnementResponse(
                ligne.getProduit().getId(),
                ligne.getProduit().getNom(),
                ligne.getQuantite(),
                ligne.getPrixAchatUnitaire(),
                ligne.sousTotal());
    }
}
