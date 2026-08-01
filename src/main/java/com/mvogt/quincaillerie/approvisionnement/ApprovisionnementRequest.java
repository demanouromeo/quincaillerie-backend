package com.mvogt.quincaillerie.approvisionnement;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record ApprovisionnementRequest(
        @NotNull Long fournisseurId,
        @NotEmpty @Valid List<LigneApprovisionnementRequest> lignes
) {
}
