package com.mvogt.quincaillerie.approvisionnement;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mvogt.quincaillerie.auth.Utilisateur;
import com.mvogt.quincaillerie.auth.UtilisateurRepository;
import com.mvogt.quincaillerie.fournisseur.Fournisseur;
import com.mvogt.quincaillerie.fournisseur.FournisseurRepository;
import com.mvogt.quincaillerie.produit.MouvementStock;
import com.mvogt.quincaillerie.produit.MouvementStockRepository;
import com.mvogt.quincaillerie.produit.Produit;
import com.mvogt.quincaillerie.produit.ProduitRepository;
import com.mvogt.quincaillerie.produit.TypeMouvement;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ApprovisionnementService {

    private final ApprovisionnementRepository approvisionnementRepository;
    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final MouvementStockRepository mouvementStockRepository;

    /**
     * Enregistre un approvisionnement et incremente le stock ligne par ligne dans la meme
     * transaction. Le verrouillage optimiste (@Version sur Produit) fait echouer la transaction
     * en cas de modification concurrente du meme produit par une autre operation de stock.
     */
    @Transactional
    public Approvisionnement enregistrerApprovisionnement(ApprovisionnementRequest request, String gestionnaireLogin) {
        Utilisateur gestionnaire = utilisateurRepository.findByLogin(gestionnaireLogin)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + gestionnaireLogin));

        Fournisseur fournisseur = fournisseurRepository.findById(request.fournisseurId())
                .orElseThrow(() -> new EntityNotFoundException("Fournisseur introuvable: " + request.fournisseurId()));

        Approvisionnement approvisionnement = new Approvisionnement();
        approvisionnement.setDateAppro(Instant.now());
        approvisionnement.setFournisseur(fournisseur);
        approvisionnement.setGestionnaire(gestionnaire);

        for (LigneApprovisionnementRequest ligneRequest : request.lignes()) {
            Produit produit = produitRepository.findById(ligneRequest.produitId())
                    .orElseThrow(() -> new EntityNotFoundException("Produit introuvable: " + ligneRequest.produitId()));

            produit.setStockActuel(produit.getStockActuel() + ligneRequest.quantite());
            produitRepository.save(produit);

            LigneApprovisionnement ligne = LigneApprovisionnement.builder()
                    .produit(produit)
                    .quantite(ligneRequest.quantite())
                    .prixAchatUnitaire(ligneRequest.prixAchatUnitaire())
                    .build();
            approvisionnement.ajouterLigne(ligne);
        }

        Approvisionnement approvisionnementEnregistre = approvisionnementRepository.save(approvisionnement);

        for (LigneApprovisionnement ligne : approvisionnementEnregistre.getLignes()) {
            mouvementStockRepository.save(MouvementStock.builder()
                    .produit(ligne.getProduit())
                    .type(TypeMouvement.APPRO)
                    .quantite(ligne.getQuantite())
                    .date(approvisionnementEnregistre.getDateAppro())
                    .reference("APPRO#" + approvisionnementEnregistre.getId())
                    .build());
        }

        return approvisionnementEnregistre;
    }
}
