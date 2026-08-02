package com.mvogt.quincaillerie.auth;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByLogin(String login);

    boolean existsByLogin(String login);

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);
}
