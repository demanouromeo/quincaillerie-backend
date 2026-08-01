package com.mvogt.quincaillerie.auth;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Gestion des comptes utilisateurs (Vendeur/Gestionnaire/Admin) — reserve a l'Admin. */
@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UtilisateurController {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurService utilisateurService;

    @GetMapping
    public List<UtilisateurResponse> lister() {
        return utilisateurRepository.findAll().stream().map(UtilisateurResponse::from).toList();
    }

    @PostMapping
    public UtilisateurResponse creer(@Valid @RequestBody UtilisateurRequest request) {
        return UtilisateurResponse.from(utilisateurService.creer(request));
    }

    @PutMapping("/{id}")
    public UtilisateurResponse modifier(@PathVariable Long id, @Valid @RequestBody UtilisateurRequest request) {
        return UtilisateurResponse.from(utilisateurService.modifier(id, request));
    }
}
