package com.mvogt.quincaillerie.vente;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record LigneVenteRequest(
        @NotNull Long produitId,
        @Positive int quantite
) {
}
