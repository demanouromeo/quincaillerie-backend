package com.mvogt.quincaillerie.vente;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.mvogt.quincaillerie.auth.Role;
import com.mvogt.quincaillerie.auth.UtilisateurRequest;
import com.mvogt.quincaillerie.produit.MouvementStockRepository;
import com.mvogt.quincaillerie.produit.ProduitRequest;
import com.mvogt.quincaillerie.produit.ProduitResponse;
import com.mvogt.quincaillerie.produit.TypeMouvement;
import com.mvogt.quincaillerie.support.AbstractIntegrationTest;

/**
 * Flux de vente de bout en bout : creation du produit et du vendeur par un admin, vente par le
 * vendeur via une vraie requete HTTP authentifiee, verification du decrement de stock et du
 * journal MouvementStock, puis rejet d'une vente qui depasse le stock disponible.
 */
class VenteFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MouvementStockRepository mouvementStockRepository;

    @Test
    void venteDecrementeLeStockEtJournaliseLeMouvement() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        Long produitId = creerProduit(tokenAdmin, "REF-VENTE-1", "Marteau", 10);
        creerUtilisateur(tokenAdmin, "Vendeur Vente", "vendeur.vente1", "motdepasse123", Role.VENDEUR);
        String tokenVendeur = obtenirToken("vendeur.vente1", "motdepasse123");

        long mouvementsAvant = mouvementStockRepository.count();

        VenteRequest venteRequest = new VenteRequest(List.of(new LigneVenteRequest(produitId, 3)));
        mockMvc.perform(post("/api/ventes")
                        .header("Authorization", "Bearer " + tokenVendeur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(venteRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.montantTotal").value(300.0));

        ProduitResponse produitApresVente = recupererProduit(tokenVendeur, produitId);
        assertThat(produitApresVente.stockActuel()).isEqualTo(7);

        assertThat(mouvementStockRepository.count()).isEqualTo(mouvementsAvant + 1);
        assertThat(mouvementStockRepository.findAll().stream()
                .anyMatch(m -> m.getType() == TypeMouvement.VENTE && m.getQuantite() == -3))
                .isTrue();
    }

    @Test
    void venteAvecStockInsuffisantRenvoie409() throws Exception {
        String tokenAdmin = obtenirToken("admin", "admin123");
        Long produitId = creerProduit(tokenAdmin, "REF-VENTE-2", "Tournevis", 2);
        creerUtilisateur(tokenAdmin, "Vendeur Vente 2", "vendeur.vente2", "motdepasse123", Role.VENDEUR);
        String tokenVendeur = obtenirToken("vendeur.vente2", "motdepasse123");

        VenteRequest venteRequest = new VenteRequest(List.of(new LigneVenteRequest(produitId, 5)));
        mockMvc.perform(post("/api/ventes")
                        .header("Authorization", "Bearer " + tokenVendeur)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(venteRequest)))
                .andExpect(status().isConflict());

        ProduitResponse produitInchange = recupererProduit(tokenVendeur, produitId);
        assertThat(produitInchange.stockActuel()).isEqualTo(2);
    }

    private Long creerProduit(String tokenAdmin, String reference, String nom, int stockActuel) throws Exception {
        ProduitRequest request = new ProduitRequest(reference, nom, null, "piece",
                new BigDecimal("50.00"), new BigDecimal("100.00"), 1, stockActuel);
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
