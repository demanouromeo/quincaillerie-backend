package com.mvogt.quincaillerie.parametres;

public record ParametresResponse(String nom, String domaine, String telephone, String ville, String email) {

    public static ParametresResponse from(ParametresMagasin parametres) {
        return new ParametresResponse(
                parametres.getNom(), parametres.getDomaine(), parametres.getTelephone(),
                parametres.getVille(), parametres.getEmail());
    }
}
