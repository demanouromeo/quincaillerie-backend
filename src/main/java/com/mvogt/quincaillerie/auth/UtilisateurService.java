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
        if (utilisateurRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("Cet email est deja utilise : " + request.email());
        }

        Utilisateur utilisateur = Utilisateur.builder()
                .nom(request.nom())
                .login(request.login())
                .email(request.email())
                .motDePasse(passwordEncoder.encode(request.motDePasse()))
                .role(request.role())
                .actif(request.actif())
                .build();
        return utilisateurRepository.save(utilisateur);
    }

    public Utilisateur modifier(Long id, UtilisateurRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + id));

        utilisateurRepository.findByEmail(request.email())
                .filter(existant -> !existant.getId().equals(id))
                .ifPresent(existant -> {
                    throw new IllegalStateException("Cet email est deja utilise : " + request.email());
                });

        utilisateur.setNom(request.nom());
        utilisateur.setLogin(request.login());
        utilisateur.setEmail(request.email());
        utilisateur.setRole(request.role());
        utilisateur.setActif(request.actif());
        if (request.motDePasse() != null && !request.motDePasse().isBlank()) {
            utilisateur.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        }
        return utilisateurRepository.save(utilisateur);
    }

    /** Suppression = desactivation (actif=false), pour ne pas casser les references historiques
     * (Vente.vendeur, Approvisionnement.gestionnaire) qui pointent vers cet utilisateur. */
    public void supprimer(Long id) {
        Utilisateur utilisateur = utilisateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + id));
        utilisateur.setActif(false);
        utilisateurRepository.save(utilisateur);
    }

    public void changerMotDePasse(String login, ChangePasswordRequest request) {
        Utilisateur utilisateur = utilisateurRepository.findByLogin(login)
                .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable: " + login));
        if (!passwordEncoder.matches(request.ancienMotDePasse(), utilisateur.getMotDePasse())) {
            throw new MotDePasseIncorrectException("L'ancien mot de passe est incorrect.");
        }
        utilisateur.setMotDePasse(passwordEncoder.encode(request.nouveauMotDePasse()));
        utilisateurRepository.save(utilisateur);
    }
}
