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
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategorieController {

    private final CategorieRepository categorieRepository;

    @GetMapping
    public List<CategorieResponse> lister() {
        return categorieRepository.findAll().stream().map(CategorieResponse::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public CategorieResponse creer(@Valid @RequestBody CategorieRequest request) {
        Categorie categorie = Categorie.builder().nom(request.nom()).build();
        return CategorieResponse.from(categorieRepository.save(categorie));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategorieResponse modifier(@PathVariable Long id, @Valid @RequestBody CategorieRequest request) {
        Categorie categorie = categorieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Categorie introuvable: " + id));
        categorie.setNom(request.nom());
        return CategorieResponse.from(categorieRepository.save(categorie));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void supprimer(@PathVariable Long id) {
        categorieRepository.deleteById(id);
    }
}
