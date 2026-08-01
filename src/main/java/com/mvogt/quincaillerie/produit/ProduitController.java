package com.mvogt.quincaillerie.produit;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/produits")
@RequiredArgsConstructor
public class ProduitController {

    private final ProduitRepository produitRepository;
    private final ProduitService produitService;

    @GetMapping
    public List<ProduitResponse> lister() {
        return produitRepository.findAll().stream().map(ProduitResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ProduitResponse consulter(@PathVariable Long id) {
        return produitRepository.findById(id)
                .map(ProduitResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable: " + id));
    }

    @GetMapping("/alertes")
    public List<ProduitResponse> produitsSousSeuilAlerte() {
        return produitRepository.findProduitsSousSeuilAlerte().stream().map(ProduitResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GESTIONNAIRE', 'ADMIN')")
    public ProduitResponse creer(@Valid @RequestBody ProduitRequest request) {
        return ProduitResponse.from(produitService.creer(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GESTIONNAIRE', 'ADMIN')")
    public ProduitResponse modifier(@PathVariable Long id, @Valid @RequestBody ProduitRequest request) {
        return ProduitResponse.from(produitService.modifier(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void supprimer(@PathVariable Long id) {
        produitRepository.deleteById(id);
    }
}
