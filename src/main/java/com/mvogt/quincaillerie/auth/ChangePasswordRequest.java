package com.mvogt.quincaillerie.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
        @NotBlank String ancienMotDePasse,
        @NotBlank @Size(min = 6) String nouveauMotDePasse
) {
}
