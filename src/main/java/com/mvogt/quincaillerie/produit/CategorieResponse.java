package com.mvogt.quincaillerie.produit;

public record CategorieResponse(Long id, String nom) {

    public static CategorieResponse from(Categorie categorie) {
        return new CategorieResponse(categorie.getId(), categorie.getNom());
    }
}
