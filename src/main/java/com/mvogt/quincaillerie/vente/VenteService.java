package com.mvogt.quincaillerie.vente;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mvogt.quincaillerie.auth.Utilisateur;
import com.mvogt.quincaillerie.auth.UtilisateurRepository;
import com.mvogt.quincaillerie.produit.MouvementStock;
import com.mvogt.quincaillerie.produit.MouvementStockRepository;
import com.mvogt.quincaillerie.produit.Produit;
import com.mvogt.quincaillerie.produit.ProduitRepository;
import com.mvogt.quincaillerie.produit.TypeMouvement;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VenteService {

    private final VenteRepository venteRepository;
    private final ProduitRepository produitRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MouvementStockRepository mouvementStockRepository;

    /**
     * Enregistre une vente et decremente le stock ligne par ligne dans la meme transaction.
     * Le verrouillage optimiste (@Version sur Produit) fait echouer la transaction en cas de
     * modification concurrente du meme produit par une autre vente.
     */
    @Transactional
    public Vente enregistrerVente(VenteRequest request, String vendeurLogin) {
        Utilisateur vendeur = utilisateurRepository.findByLogin(vendeurLogin)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + vendeurLogin));

        Vente vente = new Vente();
        vente.setDateVente(Instant.now());
        vente.setVendeur(vendeur);

        BigDecimal montantTotal = BigDecimal.ZERO;

        for (LigneVenteRequest ligneRequest : request.lignes()) {
            Produit produit = produitRepository.findById(ligneRequest.produitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit introuvable: " + ligneRequest.produitId()));

            if (produit.getStockActuel() < ligneRequest.quantite()) {
                throw new IllegalStateException(
                        "Stock insuffisant pour " + produit.getNom() + " (disponible: " + produit.getStockActuel() + ")");
            }

            produit.setStockActuel(produit.getStockActuel() - ligneRequest.quantite());
            produitRepository.save(produit);

            LigneVente ligne = LigneVente.builder()
                    .produit(produit)
                    .quantite(ligneRequest.quantite())
                    .prixVenteUnitaire(produit.getPrixVente())
                    .build();
            vente.ajouterLigne(ligne);

            montantTotal = montantTotal.add(ligne.sousTotal());
        }

        vente.setMontantTotal(montantTotal);
        Vente venteEnregistree = venteRepository.save(vente);

        for (LigneVente ligne : venteEnregistree.getLignes()) {
            mouvementStockRepository.save(MouvementStock.builder()
                    .produit(ligne.getProduit())
                    .type(TypeMouvement.VENTE)
                    .quantite(-ligne.getQuantite())
                    .date(venteEnregistree.getDateVente())
                    .reference("VENTE#" + venteEnregistree.getId())
                    .build());
        }

        return venteEnregistree;
    }
}
