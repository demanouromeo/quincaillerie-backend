package com.mvogt.quincaillerie.approvisionnement;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/approvisionnements")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('GESTIONNAIRE', 'ADMIN')")
public class ApprovisionnementController {

    private final ApprovisionnementService approvisionnementService;
    private final ApprovisionnementRepository approvisionnementRepository;

    @PostMapping
    public ApprovisionnementResponse enregistrer(@Valid @RequestBody ApprovisionnementRequest request,
            Authentication authentication) {
        Approvisionnement approvisionnement =
                approvisionnementService.enregistrerApprovisionnement(request, authentication.getName());
        return ApprovisionnementResponse.from(approvisionnement);
    }

    @GetMapping
    public List<ApprovisionnementResponse> lister() {
        return approvisionnementRepository.findAll().stream().map(ApprovisionnementResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ApprovisionnementResponse consulter(@PathVariable Long id) {
        return approvisionnementRepository.findById(id)
                .map(ApprovisionnementResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Approvisionnement introuvable: " + id));
    }

    @GetMapping("/fournisseur/{fournisseurId}")
    public List<ApprovisionnementResponse> listerParFournisseur(@PathVariable Long fournisseurId) {
        return approvisionnementRepository.findByFournisseurId(fournisseurId).stream()
                .map(ApprovisionnementResponse::from)
                .toList();
    }
}
