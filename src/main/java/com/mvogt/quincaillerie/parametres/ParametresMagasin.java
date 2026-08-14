package com.mvogt.quincaillerie.parametres;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Ligne unique (id fixe, voir {@link ParametresController}) portant les coordonnees du magasin. */
@Entity
@Table(name = "parametres_magasin")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParametresMagasin {

    @Id
    private Long id;

    @Column(nullable = false, length = 150)
    private String nom;

    @Column(nullable = false, length = 255)
    private String domaine;

    @Column(nullable = false, length = 30)
    private String telephone;

    @Column(nullable = false, length = 150)
    private String ville;

    @Column(nullable = false)
    private String email;
}
