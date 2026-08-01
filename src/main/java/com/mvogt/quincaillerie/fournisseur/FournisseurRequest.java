package com.mvogt.quincaillerie.fournisseur;

import jakarta.validation.constraints.NotBlank;

public record FournisseurRequest(
        @NotBlank String nom,
        String contact,
        String adresse
) {
}
