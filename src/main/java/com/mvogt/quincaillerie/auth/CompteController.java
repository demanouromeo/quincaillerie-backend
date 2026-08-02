package com.mvogt.quincaillerie.auth;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Actions en self-service sur son propre compte — accessible a tout utilisateur authentifie,
 * quel que soit son role (pas de restriction @PreAuthorize contrairement a UtilisateurController). */
@RestController
@RequestMapping("/api/compte")
@RequiredArgsConstructor
public class CompteController {

    private final UtilisateurService utilisateurService;

    @PutMapping("/mot-de-passe")
    public MessageResponse changerMotDePasse(Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        utilisateurService.changerMotDePasse(authentication.getName(), request);
        return new MessageResponse("Mot de passe modifie avec succes.");
    }
}
