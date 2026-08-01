package com.mvogt.quincaillerie.approvisionnement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.mvogt.quincaillerie.auth.Utilisateur;
import com.mvogt.quincaillerie.fournisseur.Fournisseur;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "approvisionnements")
@Getter
@Setter
@NoArgsConstructor
public class Approvisionnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_appro", nullable = false)
    private Instant dateAppro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gestionnaire_id", nullable = false)
    private Utilisateur gestionnaire;

    @OneToMany(mappedBy = "approvisionnement", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneApprovisionnement> lignes = new ArrayList<>();

    public void ajouterLigne(LigneApprovisionnement ligne) {
        ligne.setApprovisionnement(this);
        this.lignes.add(ligne);
    }
}
