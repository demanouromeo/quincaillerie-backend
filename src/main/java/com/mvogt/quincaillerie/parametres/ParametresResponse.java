package com.mvogt.quincaillerie.parametres;

public record ParametresResponse(String telephone, String ville, String email) {

    public static ParametresResponse from(ParametresMagasin parametres) {
        return new ParametresResponse(parametres.getTelephone(), parametres.getVille(), parametres.getEmail());
    }
}
