package com.mvogt.quincaillerie.approvisionnement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.mvogt.quincaillerie.auth.Role;
import com.mvogt.quincaillerie.auth.UtilisateurRequest;
import com.mvogt.quincaillerie.fournisseur.FournisseurRequest;
import com.mvogt.quincaillerie.fournisseur.FournisseurResponse;
import com.mvogt.quincaillerie.produit.MouvementStockRepository;
import com.mvogt.quincaillerie.produit.ProduitRequest;
import com.mvogt.quincaillerie.produit.ProduitResponse;
import com.mvogt.quincaillerie.produit.TypeMouvement;
import com.mvogt.quincaillerie.support.AbstractIntegrationTest;

/**
 * Flux d'approvisionnement de bout en bout : un GESTIONNAIRE cree un fournisseur puis un
 * approvisionnement, le stock du produit est incremente et le mouvement journalise ; un VENDEUR
 * n'a pas acces a l'endpoint.
 */
class ApprovisionnementFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MouvementStockRepository mouvementStockRepository;

    @Test
    void approvisionnementIncrementeLeStockEtJournaliseLeMouvement() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        Long produitId = creerProduit(tokenAdmin, "REF-APPRO-1", "Clou", 5);
        Long fournisseurId = creerFournisseur(tokenAdmin, "Quincaillerie Fournisseur SA");
        creerUtilisateur(tokenAdmin, "Gestionnaire Appro", "gestionnaire.appro1", "motdepasse123", Role.GESTIONNAIRE);
        String tokenGestionnaire = obtenirToken("gestionnaire.appro1", "motdepasse123");

        long mouvementsAvant = mouvementStockRepository.count();

        ApprovisionnementRequest request = new ApprovisionnementRequest(fournisseurId,
                List.of(new LigneApprovisionnementRequest(produitId, 20, new BigDecimal("40.00"))));
        mockMvc.perform(post("/api/approvisionnements")
                        .header("Authorization", "Bearer " + tokenGestionnaire)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        ProduitResponse produitApresAppro = recupererProduit(tokenGestionnaire, produitId);
        assertThat(produitApresAppro.stockActuel()).isEqualTo(25);

        assertThat(mouvementStockRepository.count()).isEqualTo(mouvementsAvant + 1);
        assertThat(mouvementStockRepository.findAll().stream()
                .anyMatch(m -> m.getType() == TypeMouvement.APPRO && m.getQuantite() == 20))
                .isTrue();
    }

    @Test
    void vendeurNePeutPasCreerDApprovisionnement() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        Long produitId = creerProduit(tokenAdmin, "REF-APPRO-2", "Vis", 5);
        Long fournisseurId = creerFournisseur(tokenAdmin, "Autre Fournisseur");
        creerUtilisateur(tokenAdmin, "Vendeur Appro", "vendeur.appro1", "motdepasse123", Role.VENDEUR);
        String tokenVendeur = obtenirToken("vendeur.appro1", "motdepasse123");

        ApprovisionnementRequest request = new ApprovisionnementRequest(fournisseurId,
                List.of(new LigneApprovisionnementRequest(produitId, 10, new BigDecimal("10.00"))));
        mockMvc.perform(post("/api/approvisionnements")
                        .header("Authorization", "Bearer " + tokenVendeur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    private Long creerProduit(String tokenAdmin, String reference, String nom, int stockActuel) throws Exception {
        ProduitRequest request = new ProduitRequest(reference, nom, null, "piece",
                new BigDecimal("30.00"), new BigDecimal("60.00"), 1, stockActuel);
        String reponse = mockMvc.perform(post("/api/produits")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(reponse, ProduitResponse.class).id();
    }

    private Long creerFournisseur(String tokenAdmin, String nom) throws Exception {
        FournisseurRequest request = new FournisseurRequest(nom, "0000", "Yaounde");
        String reponse = mockMvc.perform(post("/api/fournisseurs")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(reponse, FournisseurResponse.class).id();
    }

    private ProduitResponse recupererProduit(String token, Long id) throws Exception {
        String reponse = mockMvc.perform(get("/api/produits/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readValue(reponse, ProduitResponse.class);
    }

    private void creerUtilisateur(String tokenAdmin, String nom, String login, String motDePasse, Role role)
            throws Exception {
        UtilisateurRequest request = new UtilisateurRequest(nom, login, motDePasse, role, true);
        mockMvc.perform(post("/api/utilisateurs")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
