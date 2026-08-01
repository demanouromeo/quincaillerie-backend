package com.mvogt.quincaillerie.approvisionnement;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LigneApprovisionnementRequest(
        @NotNull Long produitId,
        @Positive int quantite,
        @NotNull @Positive BigDecimal prixAchatUnitaire
) {
}
