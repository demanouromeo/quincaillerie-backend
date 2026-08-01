package com.mvogt.quincaillerie.vente;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ventes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('VENDEUR', 'GESTIONNAIRE', 'ADMIN')")
public class VenteController {

    private final VenteService venteService;
    private final VenteRepository venteRepository;

    @PostMapping
    public VenteResponse enregistrer(@Valid @RequestBody VenteRequest request, Authentication authentication) {
        Vente vente = venteService.enregistrerVente(request, authentication.getName());
        return VenteResponse.from(vente);
    }

    @GetMapping
    public List<VenteResponse> lister() {
        return venteRepository.findAll().stream().map(VenteResponse::from).toList();
    }

    @GetMapping("/{id}")
    public VenteResponse consulter(@PathVariable Long id) {
        return venteRepository.findById(id)
                .map(VenteResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Vente introuvable: " + id));
    }
}
