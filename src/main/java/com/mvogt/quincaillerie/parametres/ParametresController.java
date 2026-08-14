package com.mvogt.quincaillerie.parametres;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Identite et coordonnees du magasin (nom/domaine/telephone/ville/email) affichees sur
 * l'en-tete des recus de vente — une seule ligne en base (id fixe), lecture ouverte a tout
 * utilisateur authentifie (le vendeur en a besoin pour imprimer un recu), modification
 * reservee a l'ADMIN.
 */
@RestController
@RequestMapping("/api/parametres")
@RequiredArgsConstructor
public class ParametresController {

    private static final Long ID_UNIQUE = 1L;

    private final ParametresMagasinRepository parametresRepository;

    @GetMapping
    public ParametresResponse recuperer() {
        return ParametresResponse.from(recupererOuCreer());
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ParametresResponse modifier(@Valid @RequestBody ParametresRequest request) {
        ParametresMagasin parametres = recupererOuCreer();
        parametres.setNom(request.nom());
        parametres.setDomaine(request.domaine());
        parametres.setTelephone(request.telephone());
        parametres.setVille(request.ville());
        parametres.setEmail(request.email());
        return ParametresResponse.from(parametresRepository.save(parametres));
    }

    private ParametresMagasin recupererOuCreer() {
        return parametresRepository.findById(ID_UNIQUE)
                .orElseGet(() -> parametresRepository.save(ParametresMagasin.builder()
                        .id(ID_UNIQUE)
                        .nom("QUINCAILLERIE MVOGT")
                        .domaine("Materiaux de construction & quincaillerie generale")
                        .telephone("+237 677 28 29 27")
                        .ville("Yaounde")
                        .email("qmvogt@gmail.com")
                        .build()));
    }
}
