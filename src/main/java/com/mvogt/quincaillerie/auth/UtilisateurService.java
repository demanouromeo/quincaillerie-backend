package com.mvogt.quincaillerie.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;

    public Utilisateur creer(UtilisateurRequest request) {
        if (request.motDePasse() == null || request.motDePasse().isBlank()) {
            throw new IllegalStateException("Le mot de passe est obligatoire a la creation.");
        }
        if (utilisateurRepository.existsByLogin(request.login())) {
            throw new IllegalStateException("Ce login est deja utilise : " + request.login());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.nom())
                .login(request.login())
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .role(request.role())
                .actif(request.actif())
                .build();
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur modifier(Long id, UtilisateurRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + id));

        utilisateur.setNom(request.nom());
        utilisateur.setLogin(request.login());
        utilisateur.setRole(request.role());
        utilisateur.setActif(request.actif());
        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        }
        return utilisateurRepository.save(utilisateur);
    }
}
