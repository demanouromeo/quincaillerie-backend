package com.mvogt.quincaillerie.fournisseur;

public record FournisseurResponse(Long id, String nom, String contact, String adresse) {

    public static FournisseurResponse from(Fournisseur fournisseur) {
        return new FournisseurResponse(fournisseur.getId(), fournisseur.getNom(),
                fournisseur.getContact(), fournisseur.getAdresse());
    }
}
