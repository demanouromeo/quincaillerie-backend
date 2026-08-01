package com.mvogt.quincaillerie.produit;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProduitRequest(
        @NotBlank String reference,
        @NotBlank String nom,
        Long categorieId,
        @NotBlank String unite,
        @NotNull @PositiveOrZero BigDecimal prixAchat,
        @NotNull @PositiveOrZero BigDecimal prixVente,
        @PositiveOrZero int seuilAlerte,
        @PositiveOrZero int stockActuel
) {
}
