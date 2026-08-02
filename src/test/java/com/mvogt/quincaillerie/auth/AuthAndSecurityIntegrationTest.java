package com.mvogt.quincaillerie.auth;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.mvogt.quincaillerie.support.AbstractIntegrationTest;

/**
 * Couvre le flux d'authentification et les regles d'autorisation par role, via de vraies requetes
 * HTTP (MockMvc) traversant la chaine de securite JWT complete, contre un MySQL reel
 * (Testcontainers). Les cas "compte desactive" et "login en double" reprennent des scenarios deja
 * verifies manuellement le 2026-07-29 (voir CLAUDE.md) et les transforment en regression automatisee.
 */
class AuthAndSecurityIntegrationTest extends AbstractIntegrationTest {

    @Test
    void loginReussiRenvoieUnTokenEtLeRole() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "admin123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void loginAvecMauvaisMotDePasseRenvoie401() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("admin", "mauvais-mot-de-passe"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginAvecCompteDesactiveEstRefuse() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        creerUtilisateur(tokenAdmin, "Vendeur Inactif", "vendeur.inactif", "motdepasse123", Role.VENDEUR, false);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("vendeur.inactif", "motdepasse123"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void accesSansTokenEstRefuse() throws Exception {
        mockMvc.perform(get("/api/produits"))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendeurNePeutPasAccederAuxFournisseurs() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        creerUtilisateur(tokenAdmin, "Vendeur Test", "vendeur.securite", "motdepasse123", Role.VENDEUR, true);
        String tokenVendeur = obtenirToken("vendeur.securite", "motdepasse123");

        mockMvc.perform(get("/api/fournisseurs").header("Authorization", "Bearer " + tokenVendeur))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendeurNePeutPasAccederAuxApprovisionnements() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        creerUtilisateur(tokenAdmin, "Vendeur Test 2", "vendeur.securite2", "motdepasse123", Role.VENDEUR, true);
        String tokenVendeur = obtenirToken("vendeur.securite2", "motdepasse123");

        mockMvc.perform(get("/api/approvisionnements").header("Authorization", "Bearer " + tokenVendeur))
                .andExpect(status().isForbidden());
    }

    @Test
    void creationUtilisateurAvecLoginEnDoubleRenvoie409() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        creerUtilisateur(tokenAdmin, "Doublon Un", "doublon.login", "motdepasse123", Role.VENDEUR, true);

        UtilisateurRequest doublon =
                new UtilisateurRequest("Doublon Deux", "doublon.login", "doublon.deux@test.local", "motdepasse123",
                        Role.VENDEUR, true);
        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doublon)))
                .andExpect(status().isConflict());
    }

    @Test
    void creationUtilisateurAvecEmailEnDoubleRenvoie409() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        creerUtilisateur(tokenAdmin, "Email Un", "email.un", "motdepasse123", Role.VENDEUR, true);

        UtilisateurRequest doublon =
                new UtilisateurRequest("Email Deux", "email.deux", "email.un@test.local", "motdepasse123",
                        Role.VENDEUR, true);
        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doublon)))
                .andExpect(status().isConflict());
    }

    @Test
    void changerMotDePasseAvecAncienMotDePasseValideFonctionne() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        creerUtilisateur(tokenAdmin, "Changement Mdp", "changement.mdp", "ancienmdp123", Role.VENDEUR, true);
        String tokenUtilisateur = obtenirToken("changement.mdp", "ancienmdp123");

        ChangePasswordRequest request = new ChangePasswordRequest("ancienmdp123", "nouveaumdp456");
        mockMvc.perform(put("/api/compte/mot-de-passe")
                        .header("Authorization", "Bearer " + tokenUtilisateur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("changement.mdp", "nouveaumdp456"))))
                .andExpect(status().isOk());
    }

    @Test
    void changerMotDePasseAvecAncienMotDePasseInvalideRenvoie400() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        creerUtilisateur(tokenAdmin, "Changement Mdp Invalide", "changement.mdp.invalide", "motdepasse123",
                Role.VENDEUR, true);
        String tokenUtilisateur = obtenirToken("changement.mdp.invalide", "motdepasse123");

        ChangePasswordRequest request = new ChangePasswordRequest("mauvais-ancien-mdp", "nouveaumdp456");
        mockMvc.perform(put("/api/compte/mot-de-passe")
                        .header("Authorization", "Bearer " + tokenUtilisateur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void suppressionUtilisateurLeDesactiveEtBloqueLaConnexion() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        String reponse = mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UtilisateurRequest("A Supprimer", "a.supprimer", "a.supprimer@test.local",
                                        "motdepasse123", Role.VENDEUR, true))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        UtilisateurResponse cree = objectMapper.readValue(reponse, UtilisateurResponse.class);

        mockMvc.perform(delete("/api/utilisateurs/" + cree.id())
                        .header("Authorization", "Bearer " + tokenAdmin))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest("a.supprimer", "motdepasse123"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void suppressionUtilisateurParNonAdminEstRefusee() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        creerUtilisateur(tokenAdmin, "Vendeur Suppression", "vendeur.suppression", "motdepasse123", Role.VENDEUR, true);
        String tokenVendeur = obtenirToken("vendeur.suppression", "motdepasse123");

        mockMvc.perform(delete("/api/utilisateurs/1").header("Authorization", "Bearer " + tokenVendeur))
                .andExpect(status().isForbidden());
    }

    private void creerUtilisateur(String tokenAdmin, String nom, String login, String motDePasse, Role role,
            boolean actif) throws Exception {
        UtilisateurRequest request = new UtilisateurRequest(nom, login, login + "@test.local", motDePasse, role, actif);
        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
