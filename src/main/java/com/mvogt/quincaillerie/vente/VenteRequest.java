package com.mvogt.quincaillerie.vente;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record VenteRequest(
        @NotEmpty @Valid List<LigneVenteRequest> lignes
) {
}
