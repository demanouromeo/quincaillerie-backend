package com.mvogt.quincaillerie.vente;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record VenteResponse(
        Long id,
        Instant dateVente,
        String vendeurLogin,
        BigDecimal montantTotal,
        List<LigneVenteResponse> lignes
) {
    public static VenteResponse from(Vente vente) {
        return new VenteResponse(
                vente.getId(),
                vente.getDateVente(),
                vente.getVendeur().getLogin(),
                vente.getMontantTotal(),
                vente.getLignes().stream().map(LigneVenteResponse::from).toList());
    }
}
