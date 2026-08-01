package com.mvogt.quincaillerie.fournisseur;

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
@RequestMapping("/api/fournisseurs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GESTIONNAIRE', 'ADMIN')")
public class FournisseurController {

    private final FournisseurRepository fournisseurRepository;

    @GetMapping
    public List<FournisseurResponse> lister() {
        return fournisseurRepository.findAll().stream().map(FournisseurResponse::from).toList();
    }

    @PostMapping
    public FournisseurResponse creer(@Valid @RequestBody FournisseurRequest request) {
        Fournisseur fournisseur = Fournisseur.builder()
                .nom(request.nom())
                .contact(request.contact())
                .adresse(request.adresse())
                .build();
        return FournisseurResponse.from(fournisseurRepository.save(fournisseur));
    }

    @PutMapping("/{id}")
    public FournisseurResponse modifier(@PathVariable Long id, @Valid @RequestBody FournisseurRequest request) {
        Fournisseur fournisseur = fournisseurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable: " + id));
        fournisseur.setNom(request.nom());
        fournisseur.setContact(request.contact());
        fournisseur.setAdresse(request.adresse());
        return FournisseurResponse.from(fournisseurRepository.save(fournisseur));
    }

    @DeleteMapping("/{id}")
    public void supprimer(@PathVariable Long id) {
        fournisseurRepository.deleteById(id);
    }
}
