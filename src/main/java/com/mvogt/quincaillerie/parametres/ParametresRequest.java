package com.mvogt.quincaillerie.parametres;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ParametresRequest(
        @NotBlank String nom,
        @NotBlank String domaine,
        @NotBlank String telephone,
        @NotBlank String ville,
        @NotBlank @Email String email) {
}
