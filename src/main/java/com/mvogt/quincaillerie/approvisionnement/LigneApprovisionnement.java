package com.mvogt.quincaillerie.approvisionnement;

import java.math.BigDecimal;

import com.mvogt.quincaillerie.produit.Produit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "lignes_approvisionnement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LigneApprovisionnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approvisionnement_id", nullable = false)
    private Approvisionnement approvisionnement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produit_id", nullable = false)
    private Produit produit;

    @Column(nullable = false)
    private int quantite;

    @Column(name = "prix_achat_unitaire", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixAchatUnitaire;

    public BigDecimal sousTotal() {
        return prixAchatUnitaire.multiply(BigDecimal.valueOf(quantite));
    }
}
