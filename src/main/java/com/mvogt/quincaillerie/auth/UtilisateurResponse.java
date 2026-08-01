package com.mvogt.quincaillerie.auth;

public record UtilisateurResponse(Long id, String nom, String login, Role role, boolean actif) {

    public static UtilisateurResponse from(Utilisateur utilisateur) {
        return new UtilisateurResponse(
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getLogin(),
                utilisateur.getRole(),
                utilisateur.isActif());
    }
}
