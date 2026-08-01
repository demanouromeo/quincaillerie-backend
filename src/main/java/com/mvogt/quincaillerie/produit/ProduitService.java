package com.mvogt.quincaillerie.produit;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProduitService {

    private final ProduitRepository produitRepository;
    private final CategorieRepository categorieRepository;

    public Produit creer(ProduitRequest request) {
        Produit produit = Produit.builder()
                .reference(request.reference())
                .nom(request.nom())
                .categorie(resoudreCategorie(request.categorieId()))
                .unite(request.unite())
                .prixAchat(request.prixAchat())
                .prixVente(request.prixVente())
                .seuilAlerte(request.seuilAlerte())
                .stockActuel(request.stockActuel())
                .build();
        return produitRepository.save(produit);
    }

    public Produit modifier(Long id, ProduitRequest request) {
        Produit produit = produitRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable: " + id));

        produit.setReference(request.reference());
        produit.setNom(request.nom());
        produit.setCategorie(resoudreCategorie(request.categorieId()));
        produit.setUnite(request.unite());
        produit.setPrixAchat(request.prixAchat());
        produit.setPrixVente(request.prixVente());
        produit.setSeuilAlerte(request.seuilAlerte());
        return produitRepository.save(produit);
    }

    private Categorie resoudreCategorie(Long categorieId) {
        if (categorieId == null) {
            return null;
        }
        return categorieRepository.findById(categorieId)
                .orElseThrow(() -> new EntityNotFoundException("Categorie introuvable: " + categorieId));
    }
}
