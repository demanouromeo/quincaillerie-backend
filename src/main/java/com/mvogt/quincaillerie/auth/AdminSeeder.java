package com.mvogt.quincaillerie.auth;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Cree un compte administrateur par defaut si aucun utilisateur n'existe encore. */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private static final String DEFAULT_LOGIN = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (utilisateurRepository.count() > 0) {
            return;
        }

        Utilisateur admin = Utilisateur.builder()
                .nom("Administrateur")
                .login(DEFAULT_LOGIN)
                .email("admin@quincaillerie.local")
                .motDePasse(passwordEncoder.encode(DEFAULT_PASSWORD))
                .role(Role.ADMIN)
                .actif(true)
                .build();

        utilisateurRepository.save(admin);
        log.warn("Compte administrateur par defaut cree (login={}, mot de passe={}) - a changer immediatement.",
                DEFAULT_LOGIN, DEFAULT_PASSWORD);
    }
}
