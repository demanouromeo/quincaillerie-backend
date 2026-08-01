package com.mvogt.quincaillerie.approvisionnement;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovisionnementRepository extends JpaRepository<Approvisionnement, Long> {

    List<Approvisionnement> findByFournisseurId(Long fournisseurId);
}
