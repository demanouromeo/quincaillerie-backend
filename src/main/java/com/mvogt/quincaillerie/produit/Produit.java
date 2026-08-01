package com.mvogt.quincaillerie.produit;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "produits")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id")
    private Categorie categorie;

    /** Unite de vente : piece, sac, metre... */
    @Column(nullable = false)
    private String unite;

    @Column(name = "prix_achat", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixAchat;

    @Column(name = "prix_vente", nullable = false, precision = 12, scale = 2)
    private BigDecimal prixVente;

    @Column(name = "seuil_alerte", nullable = false)
    @Builder.Default
    private int seuilAlerte = 0;

    @Column(name = "stock_actuel", nullable = false)
    @Builder.Default
    private int stockActuel = 0;

    /** Verrouillage optimiste pour securiser les decrements concurrents lors des ventes. */
    @Version
    private Long version;
}
