package com.mvogt.quincaillerie.produit;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProduitRepository extends JpaRepository<Produit, Long> {

    @Query("select p from Produit p where p.stockActuel <= p.seuilAlerte")
    List<Produit> findProduitsSousSeuilAlerte();
}
