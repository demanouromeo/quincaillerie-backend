package com.mvogt.quincaillerie.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * motDePasse est optionnel a la modification (null/vide = mot de passe inchange) ;
 * il est obligatoire a la creation (verifie dans UtilisateurService).
 */
public record UtilisateurRequest(
        @NotBlank String nom,
        @NotBlank String login,
        @NotBlank @Email String email,
        String motDePasse,
        @NotNull Role role,
        boolean actif
) {
}
