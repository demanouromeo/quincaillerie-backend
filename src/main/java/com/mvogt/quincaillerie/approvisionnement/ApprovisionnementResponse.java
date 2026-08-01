package com.mvogt.quincaillerie.approvisionnement;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ApprovisionnementResponse(
        Long id,
        Instant dateAppro,
        Long fournisseurId,
        String fournisseurNom,
        String gestionnaireLogin,
        BigDecimal montantTotal,
        List<LigneApprovisionnementResponse> lignes
) {
    public static ApprovisionnementResponse from(Approvisionnement approvisionnement) {
        List<LigneApprovisionnementResponse> lignes = approvisionnement.getLignes().stream()
                .map(LigneApprovisionnementResponse::from)
                .toList();
        BigDecimal montantTotal = lignes.stream()
                .map(LigneApprovisionnementResponse::sousTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ApprovisionnementResponse(
                approvisionnement.getId(),
                approvisionnement.getDateAppro(),
                approvisionnement.getFournisseur().getId(),
                approvisionnement.getFournisseur().getNom(),
                approvisionnement.getGestionnaire().getLogin(),
                montantTotal,
                lignes);
    }
}
