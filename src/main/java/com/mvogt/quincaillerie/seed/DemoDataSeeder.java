package com.mvogt.quincaillerie.seed;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.mvogt.quincaillerie.approvisionnement.ApprovisionnementRequest;
import com.mvogt.quincaillerie.approvisionnement.ApprovisionnementService;
import com.mvogt.quincaillerie.approvisionnement.LigneApprovisionnementRequest;
import com.mvogt.quincaillerie.auth.Role;
import com.mvogt.quincaillerie.auth.Utilisateur;
import com.mvogt.quincaillerie.auth.UtilisateurRepository;
import com.mvogt.quincaillerie.fournisseur.Fournisseur;
import com.mvogt.quincaillerie.fournisseur.FournisseurRepository;
import com.mvogt.quincaillerie.produit.Categorie;
import com.mvogt.quincaillerie.produit.CategorieRepository;
import com.mvogt.quincaillerie.produit.Produit;
import com.mvogt.quincaillerie.produit.ProduitRepository;
import com.mvogt.quincaillerie.vente.LigneVenteRequest;
import com.mvogt.quincaillerie.vente.VenteRequest;
import com.mvogt.quincaillerie.vente.VenteService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Jeu de donnees de demonstration (catalogue quincaillerie/materiaux de construction camerounais).
 * N'est jamais execute au demarrage normal - actif uniquement via le profil Spring "demo-data"
 * (ex: mvn spring-boot:run -Dspring-boot.run.profiles=demo-data), et idempotent (marqueur sur le
 * nom du premier fournisseur cree) pour ne pas dupliquer les donnees si le profil est relance.
 */
@Component
@Profile("demo-data")
@RequiredArgsConstructor
@Slf4j
public class DemoDataSeeder implements CommandLineRunner {

    private static final String MARQUEUR_FOURNISSEUR = "CIMENCAM Distribution Yaounde";

    private final CategorieRepository categorieRepository;
    private final ProduitRepository produitRepository;
    private final FournisseurRepository fournisseurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApprovisionnementService approvisionnementService;
    private final VenteService venteService;

    private final Random random = new Random(20260729L);

    private static final Map<String, int[]> PRIX_PAR_CATEGORIE = new LinkedHashMap<>();
    static {
        PRIX_PAR_CATEGORIE.put("Ciment & Beton", new int[]{1200, 15000});
        PRIX_PAR_CATEGORIE.put("Fer & Metallerie", new int[]{800, 45000});
        PRIX_PAR_CATEGORIE.put("Bois & Menuiserie", new int[]{1000, 40000});
        PRIX_PAR_CATEGORIE.put("Peinture & Enduits", new int[]{800, 25000});
        PRIX_PAR_CATEGORIE.put("Plomberie", new int[]{500, 60000});
        PRIX_PAR_CATEGORIE.put("Electricite", new int[]{300, 40000});
        PRIX_PAR_CATEGORIE.put("Quincaillerie Generale", new int[]{300, 15000});
        PRIX_PAR_CATEGORIE.put("Carrelage & Revetement", new int[]{1500, 20000});
        PRIX_PAR_CATEGORIE.put("Outillage", new int[]{1500, 60000});
        PRIX_PAR_CATEGORIE.put("Toiture & Etancheite", new int[]{1500, 40000});
    }

    private static final Map<String, int[]> SURCLASSEMENT_PRIX = new LinkedHashMap<>();
    static {
        SURCLASSEMENT_PRIX.put("Groupe electrogene", new int[]{180000, 450000});
        SURCLASSEMENT_PRIX.put("Climatiseur", new int[]{150000, 300000});
        SURCLASSEMENT_PRIX.put("Panneau solaire", new int[]{35000, 90000});
        SURCLASSEMENT_PRIX.put("Batterie solaire", new int[]{60000, 150000});
        SURCLASSEMENT_PRIX.put("Onduleur", new int[]{40000, 120000});
        SURCLASSEMENT_PRIX.put("Betonniere", new int[]{150000, 350000});
        SURCLASSEMENT_PRIX.put("Poste a souder", new int[]{60000, 150000});
        SURCLASSEMENT_PRIX.put("Compresseur", new int[]{80000, 200000});
        SURCLASSEMENT_PRIX.put("Echafaudage", new int[]{100000, 300000});
        SURCLASSEMENT_PRIX.put("Chateau d'eau", new int[]{120000, 250000});
        SURCLASSEMENT_PRIX.put("Baignoire", new int[]{60000, 150000});
        SURCLASSEMENT_PRIX.put("Reservoir d'eau PVC 1000L", new int[]{100000, 180000});
        SURCLASSEMENT_PRIX.put("Pompe immergee", new int[]{80000, 180000});
        SURCLASSEMENT_PRIX.put("Portail metallique", new int[]{120000, 280000});
        SURCLASSEMENT_PRIX.put("Porte metallique", new int[]{60000, 150000});
        SURCLASSEMENT_PRIX.put("Charpente metallique prefabriquee", new int[]{200000, 500000});
        SURCLASSEMENT_PRIX.put("Echelle", new int[]{25000, 70000});
        SURCLASSEMENT_PRIX.put("Ventilateur plafond", new int[]{15000, 35000});
        SURCLASSEMENT_PRIX.put("Perceuse", new int[]{15000, 45000});
        SURCLASSEMENT_PRIX.put("Scie circulaire", new int[]{25000, 60000});
        SURCLASSEMENT_PRIX.put("Meuleuse d'angle", new int[]{12000, 35000});
    }

    @Override
    public void run(String... args) {
        boolean dejaExecute = fournisseurRepository.findAll().stream()
                .anyMatch(f -> MARQUEUR_FOURNISSEUR.equals(f.getNom()));
        if (dejaExecute) {
            log.info("Jeu de donnees demo deja present (fournisseur marqueur trouve) - seed ignore.");
            return;
        }

        Map<String, Categorie> categories = creerCategories();
        List<Fournisseur> fournisseurs = creerFournisseurs();
        Utilisateur gestionnaire = creerUtilisateurSiAbsent("gestionnaire_demo", "Jean Mvondo", Role.GESTIONNAIRE,
                "gestion123", "gestionnaire_demo@quincaillerie.local");
        Utilisateur vendeur = creerUtilisateurSiAbsent("vendeur_demo", "Aicha Ngo Bakang", Role.VENDEUR,
                "vendeur123", "vendeur_demo@quincaillerie.local");

        List<Produit> produits = creerProduits(categories);
        log.info("Seed: {} produits crees.", produits.size());

        int nbAppros = creerApprovisionnements(fournisseurs, produits, gestionnaire.getLogin(), 40);
        log.info("Seed: {} approvisionnements crees.", nbAppros);

        int nbVentes = creerVentes(produits, vendeur.getLogin(), 150);
        log.info("Seed: {} ventes creees.", nbVentes);
    }

    private Map<String, Categorie> creerCategories() {
        Map<String, Categorie> resultat = new LinkedHashMap<>();
        for (String nom : PRIX_PAR_CATEGORIE.keySet()) {
            Categorie categorie = categorieRepository.findAll().stream()
                    .filter(c -> c.getNom().equals(nom))
                    .findFirst()
                    .orElseGet(() -> categorieRepository.save(Categorie.builder().nom(nom).build()));
            resultat.put(nom, categorie);
        }
        return resultat;
    }

    private List<Fournisseur> creerFournisseurs() {
        List<Fournisseur> fournisseurs = new ArrayList<>();
        fournisseurs.add(fournisseurRepository.save(Fournisseur.builder()
                .nom(MARQUEUR_FOURNISSEUR)
                .contact("+237 677 12 34 56")
                .adresse("Zone Industrielle Nlongkak, Yaounde")
                .build()));
        fournisseurs.add(fournisseurRepository.save(Fournisseur.builder()
                .nom("Etablissements Fotso & Fils")
                .contact("+237 699 45 67 89")
                .adresse("Marche Mokolo, Yaounde")
                .build()));
        fournisseurs.add(fournisseurRepository.save(Fournisseur.builder()
                .nom("Quincaillerie Generale du Mfoundi")
                .contact("+237 655 22 11 33")
                .adresse("Carrefour Warda, Yaounde")
                .build()));
        fournisseurs.add(fournisseurRepository.save(Fournisseur.builder()
                .nom("SOCAQUIP Materiaux Sarl")
                .contact("+237 691 78 90 12")
                .adresse("Zone Industrielle Bassa, Douala")
                .build()));
        fournisseurs.add(fournisseurRepository.save(Fournisseur.builder()
                .nom("Comptoir Metallique du Cameroun (COMETAL)")
                .contact("+237 673 33 44 55")
                .adresse("Zone Industrielle Bonaberi, Douala")
                .build()));
        return fournisseurs;
    }

    private Utilisateur creerUtilisateurSiAbsent(String login, String nom, Role role, String motDePasse, String email) {
        return utilisateurRepository.findByLogin(login).orElseGet(() -> utilisateurRepository.save(Utilisateur.builder()
                .nom(nom)
                .login(login)
                .email(email)
                .motDePasse(passwordEncoder.encode(motDePasse))
                .role(role)
                .actif(true)
                .build()));
    }

    private List<Produit> creerProduits(Map<String, Categorie> categories) {
        Map<String, List<String>> catalogue = catalogueProduits();
        List<Produit> produits = new ArrayList<>();
        int reference = 1;

        for (Map.Entry<String, List<String>> entry : catalogue.entrySet()) {
            Categorie categorie = categories.get(entry.getKey());
            int[] plage = plagePrix(entry.getKey());

            for (String nom : entry.getValue()) {
                int[] plageEffective = plage;
                for (Map.Entry<String, int[]> surclassement : SURCLASSEMENT_PRIX.entrySet()) {
                    if (nom.contains(surclassement.getKey())) {
                        plageEffective = surclassement.getValue();
                        break;
                    }
                }

                BigDecimal prixAchat = BigDecimal.valueOf(arrondiA(randomEntre(plageEffective[0], plageEffective[1]), 5));
                double marge = 0.15 + random.nextDouble() * 0.20;
                BigDecimal prixVente = BigDecimal.valueOf(arrondiA((int) Math.round(prixAchat.doubleValue() * (1 + marge)), 5));

                int seuilAlerte = randomEntre(5, 20);
                int stockActuel = tirerStockActuel(seuilAlerte);

                Produit produit = Produit.builder()
                        .reference(String.format("PRD-%04d", reference++))
                        .nom(nom)
                        .categorie(categorie)
                        .unite(deriverUnite(nom))
                        .prixAchat(prixAchat.setScale(2, RoundingMode.UNNECESSARY))
                        .prixVente(prixVente.setScale(2, RoundingMode.UNNECESSARY))
                        .seuilAlerte(seuilAlerte)
                        .stockActuel(stockActuel)
                        .build();
                produits.add(produitRepository.save(produit));
            }
        }
        return produits;
    }

    private int tirerStockActuel(int seuilAlerte) {
        double tirage = random.nextDouble();
        if (tirage < 0.10) {
            return 0;
        } else if (tirage < 0.30) {
            return randomEntre(1, Math.max(seuilAlerte, 1));
        }
        return seuilAlerte + randomEntre(10, 150);
    }

    private int[] plagePrix(String categorie) {
        return PRIX_PAR_CATEGORIE.get(categorie);
    }

    private int randomEntre(int min, int max) {
        if (max <= min) {
            return min;
        }
        return min + random.nextInt(max - min + 1);
    }

    private int arrondiA(int valeur, int pas) {
        return Math.max(pas, Math.round(valeur / (float) pas) * pas);
    }

    private String deriverUnite(String nom) {
        if (nom.contains("(m3)")) return "m3";
        if (nom.contains("(m²)") || nom.contains("(m2)")) return "m2";
        if (nom.contains("sac ")) return "sac";
        if (nom.contains("barre ")) return "barre";
        if (nom.contains("rouleau")) return "rouleau";
        if (nom.contains("panneau")) return "panneau";
        if (nom.contains("boite") || nom.contains("boîte")) return "boite";
        if (nom.contains("(m)") || nom.contains(" (m ")) return "metre";
        if (nom.contains("kg)")) return "kg";
        if (nom.contains("bidon") || nom.contains("pot ") || nom.contains("L)")) return "litre";
        if (nom.contains("paire")) return "paire";
        return "piece";
    }

    private int creerApprovisionnements(List<Fournisseur> fournisseurs, List<Produit> produits,
            String gestionnaireLogin, int nombre) {
        int crees = 0;
        for (int i = 0; i < nombre; i++) {
            Fournisseur fournisseur = fournisseurs.get(random.nextInt(fournisseurs.size()));
            int nbLignes = randomEntre(1, 6);
            List<LigneApprovisionnementRequest> lignes = new ArrayList<>();
            for (int l = 0; l < nbLignes; l++) {
                Produit produit = produits.get(random.nextInt(produits.size()));
                int quantite = randomEntre(10, 300);
                double variance = 0.9 + random.nextDouble() * 0.2;
                BigDecimal prixUnitaire = BigDecimal.valueOf(
                        arrondiA((int) Math.round(produit.getPrixAchat().doubleValue() * variance), 5))
                        .setScale(2, RoundingMode.UNNECESSARY);
                lignes.add(new LigneApprovisionnementRequest(produit.getId(), quantite, prixUnitaire));
            }
            approvisionnementService.enregistrerApprovisionnement(
                    new ApprovisionnementRequest(fournisseur.getId(), lignes), gestionnaireLogin);
            crees++;
        }
        return crees;
    }

    private int creerVentes(List<Produit> produits, String vendeurLogin, int nombre) {
        int crees = 0;
        for (int i = 0; i < nombre; i++) {
            int nbLignes = randomEntre(1, 4);
            List<LigneVenteRequest> lignes = new ArrayList<>();
            for (int l = 0; l < nbLignes; l++) {
                Produit produit = produitRepository.findById(produits.get(random.nextInt(produits.size())).getId())
                        .orElse(null);
                if (produit == null || produit.getStockActuel() <= 0) {
                    continue;
                }
                int quantite = Math.min(randomEntre(1, 10), produit.getStockActuel());
                lignes.add(new LigneVenteRequest(produit.getId(), quantite));
            }
            if (lignes.isEmpty()) {
                continue;
            }
            try {
                venteService.enregistrerVente(new VenteRequest(lignes), vendeurLogin);
                crees++;
            } catch (IllegalStateException stockInsuffisant) {
                // une autre ligne de la meme boucle a deja consomme le stock disponible - on ignore cette vente
            }
        }
        return crees;
    }

    private Map<String, List<String>> catalogueProduits() {
        Map<String, List<String>> catalogue = new LinkedHashMap<>();

        catalogue.put("Ciment & Beton", List.of(
                "Ciment CIMENCAM 32.5R (sac 50kg)",
                "Ciment CIMENCAM 42.5R (sac 50kg)",
                "Ciment Dangote 32.5R (sac 50kg)",
                "Ciment Dangote 42.5R (sac 50kg)",
                "Ciment Medcem 32.5R (sac 50kg)",
                "Ciment CIMAF 32.5R (sac 50kg)",
                "Ciment blanc (sac 25kg)",
                "Chaux hydraulique NHL 3.5 (sac 25kg)",
                "Fer a beton HA 6mm (barre 12m)",
                "Fer a beton HA 8mm (barre 12m)",
                "Fer a beton HA 10mm (barre 12m)",
                "Fer a beton HA 12mm (barre 12m)",
                "Fer a beton HA 14mm (barre 12m)",
                "Fer a beton HA 16mm (barre 12m)",
                "Fer a beton HA 20mm (barre 12m)",
                "Fer a beton HA 25mm (barre 12m)",
                "Treillis soude ST10 (panneau 2.4x1.2m)",
                "Treillis soude ST15 (panneau 2.4x1.2m)",
                "Treillis soude ST25 (panneau 2.4x1.2m)",
                "Fil d'attache recuit (rouleau 25kg)",
                "Parpaing creux 10x20x40",
                "Parpaing creux 15x20x40",
                "Parpaing creux 20x20x40",
                "Parpaing plein 15x20x40",
                "Parpaing plein 20x20x40",
                "Hourdis 12cm",
                "Hourdis 16cm",
                "Hourdis 20cm",
                "Poutrelle beton precontrainte 3m",
                "Poutrelle beton precontrainte 4m",
                "Poutrelle beton precontrainte 5m",
                "Poteau beton precontraint 4m",
                "Poteau beton precontraint 6m",
                "Buse beton diametre 300mm",
                "Buse beton diametre 400mm",
                "Buse beton diametre 600mm",
                "Regard beton prefabrique 40x40",
                "Dalle beton prefabriquee 40x40",
                "Bordure beton T2",
                "Sable de riviere (m3)",
                "Sable de carriere (m3)",
                "Gravier 5/15 (m3)",
                "Gravier 15/25 (m3)",
                "Gravillon 5/10 (m3)",
                "Laterite (m3)",
                "Adjuvant plastifiant (bidon 5L)",
                "Fibre de polypropylene (sac 1kg)",
                "Contreplaque coffrage 18mm (panneau)",
                "Etai metallique telescopique 3m",
                "Serre-joint de coffrage"
        ));

        catalogue.put("Fer & Metallerie", List.of(
                "Tole bac aluminium 4/10 (2m)",
                "Tole bac aluminium 4/10 (3m)",
                "Tole bac galvanisee 6/10 (2m)",
                "Tole bac galvanisee 6/10 (3m)",
                "Tole plate galvanisee (feuille)",
                "Faitiere tole aluminium",
                "Corniere metallique 40x40x4 (barre 6m)",
                "Corniere metallique 50x50x5 (barre 6m)",
                "Fer plat 20x5mm (barre 6m)",
                "Fer plat 30x5mm (barre 6m)",
                "Fer rond lisse 8mm (barre 6m)",
                "Fer rond lisse 10mm (barre 6m)",
                "Tube carre 20x20x1.5 (barre 6m)",
                "Tube carre 30x30x2 (barre 6m)",
                "Tube rectangulaire 40x20x1.5 (barre 6m)",
                "Tube rond 26.9mm (barre 6m)",
                "Tube rond 33.7mm (barre 6m)",
                "Profile IPN 100 (barre 6m)",
                "Profile UPN 80 (barre 6m)",
                "Tole striee 3mm (feuille)",
                "Grillage soude galvanise (rouleau 20m)",
                "Grillage a mailles losangees (rouleau 20m)",
                "Fil de fer galvanise 2mm (rouleau)",
                "Barbele galvanise (rouleau 200m)",
                "Portail metallique 2 vantaux 3x1.8m",
                "Portillon metallique 1x1.8m",
                "Fenetre metallique 100x100",
                "Porte metallique pleine 90x210",
                "Grille de defense fenetre 100x100",
                "Rivet pop 4x10mm (boite 100)",
                "Boulon HM 8x40 (boite 50)",
                "Boulon HM 10x50 (boite 50)",
                "Ecrou hexagonal M8 (boite 100)",
                "Ecrou hexagonal M10 (boite 100)",
                "Rondelle plate M8 (boite 100)",
                "Electrode de soudure 2.5mm (boite 5kg)",
                "Electrode de soudure 3.2mm (boite 5kg)",
                "Disque a tronconner metal 115mm",
                "Disque a tronconner metal 230mm",
                "Disque a meuler metal 115mm",
                "Peinture antirouille (pot 1L)",
                "Peinture antirouille (pot 5L)",
                "Charniere renforcee 4 pouces",
                "Verrou de securite",
                "Cadenas laiton 50mm",
                "Targette metallique 6 pouces",
                "Serrure 3 points",
                "Poignee de porte metallique",
                "Gond a sceller",
                "Equerre de fixation metallique"
        ));

        catalogue.put("Bois & Menuiserie", List.of(
                "Planche Iroko 3x25cm (4m)",
                "Planche Sapelli 3x25cm (4m)",
                "Chevron Bilinga 6x8cm (4m)",
                "Chevron Bilinga 8x8cm (4m)",
                "Madrier 6x15cm (4m)",
                "Madrier 8x20cm (4m)",
                "Contreplaque Okoume 10mm (panneau)",
                "Contreplaque Okoume 15mm (panneau)",
                "Contreplaque Okoume 18mm (panneau)",
                "MDF 16mm (panneau)",
                "MDF 18mm (panneau)",
                "Panneau de particules 16mm (panneau)",
                "Latte de bois 3x5cm (3m)",
                "Tasseau bois 4x4cm (3m)",
                "Porte bois pleine 80x210",
                "Porte bois alveolaire 80x210",
                "Porte bois isoplane 90x210",
                "Fenetre bois 100x100",
                "Volet bois persienne 100x120",
                "Charpente prefabriquee (kit)",
                "Colle a bois (pot 1kg)",
                "Vernis bois (pot 1L)",
                "Vernis bois (pot 5L)",
                "Lasure bois (pot 1L)",
                "Pointes a bois 40mm (kg)",
                "Pointes a bois 60mm (kg)",
                "Pointes a bois 80mm (kg)",
                "Vis a bois 4x40mm (boite 100)",
                "Vis a bois 5x60mm (boite 100)",
                "Charniere bois 3 pouces",
                "Charniere bois 4 pouces",
                "Poignee de meuble",
                "Coulisse de tiroir 45cm",
                "Equerre d'assemblage bois",
                "Colle contact neoprene (pot 500ml)",
                "Baguette de finition bois 2m",
                "Plinthe bois 2m",
                "Plinthe MDF 2m",
                "Parquet flottant stratifie (m²)",
                "Lambris PVC blanc (lame 4m)",
                "Lambris bois (lame 3m)",
                "Escabeau bois 4 marches",
                "Manche de houe",
                "Manche de pioche",
                "Manche de marteau",
                "Planche de coffrage 2.5x20cm (4m)",
                "Poteau bois traite 3m",
                "Poteau bois traite 4m",
                "Grume de bois rond 4m",
                "Palette bois standard"
        ));

        catalogue.put("Peinture & Enduits", List.of(
                "Peinture vinylique blanche (pot 1L)",
                "Peinture vinylique blanche (pot 4L)",
                "Peinture vinylique blanche (pot 20L)",
                "Peinture glycero blanche (pot 1L)",
                "Peinture glycero blanche (pot 4L)",
                "Peinture a l'huile brillante (pot 1L)",
                "Peinture pour facade (pot 20L)",
                "Sous-couche d'impression (pot 4L)",
                "Sous-couche d'impression (pot 20L)",
                "Enduit de rebouchage (sac 25kg)",
                "Enduit de ragreage (sac 25kg)",
                "Enduit de facade taloche (sac 25kg)",
                "Enduit monocouche (sac 25kg)",
                "Mastic acrylique (cartouche 300ml)",
                "Mastic silicone (cartouche 300ml)",
                "Colle carrelage (sac 25kg)",
                "Colle carrelage flexible (sac 25kg)",
                "Joint de carrelage (sac 5kg)",
                "Diluant / white spirit (bidon 1L)",
                "Diluant / white spirit (bidon 5L)",
                "Rouleau a peindre 18cm",
                "Rouleau a peindre 25cm",
                "Pinceau plat 2 pouces",
                "Pinceau plat 4 pouces",
                "Bac a peinture",
                "Grille d'essorage",
                "Ruban de masquage 48mm",
                "Bache de protection plastique",
                "Enduit decoratif crepi",
                "Peinture antirouille couleur (pot 1L)",
                "Peinture routiere jaune (pot 5L)",
                "Peinture routiere blanche (pot 5L)",
                "Chaux aerienne pour badigeon (sac 25kg)",
                "Colorant universel (flacon 100ml)",
                "Papier de verre grain 80",
                "Papier de verre grain 120",
                "Spatule inox 10cm",
                "Spatule inox 15cm",
                "Taloche plastique",
                "Taloche eponge",
                "Peinture epoxy sol (kit 5L)",
                "Vernis pour beton (pot 1L)",
                "Resine d'etancheite (bidon 5L)",
                "Fixateur de fond (bidon 5L)",
                "Peinture anti-moisissure (pot 4L)",
                "Peinture ignifuge (pot 1L)",
                "Ruban a joint placo (rouleau)",
                "Enduit joint placo (sac 25kg)",
                "Peinture piscine bleue (pot 4L)",
                "Kit de renovation facade"
        ));

        catalogue.put("Plomberie", List.of(
                "Tuyau PVC evacuation 32mm (2m)",
                "Tuyau PVC evacuation 40mm (2m)",
                "Tuyau PVC evacuation 100mm (2m)",
                "Tuyau PVC evacuation 110mm (4m)",
                "Tuyau PVC pression 20mm (4m)",
                "Tuyau PVC pression 25mm (4m)",
                "Tuyau PVC pression 32mm (4m)",
                "Tube PPR 20mm (4m)",
                "Tube PPR 25mm (4m)",
                "Coude PVC 90 degres 100mm",
                "Coude PVC 45 degres 100mm",
                "Te PVC 100mm",
                "Raccord PVC union 25mm",
                "Colle PVC (pot 500ml)",
                "Robinet d'arret 1/2 pouce",
                "Robinet d'arret 3/4 pouce",
                "Mitigeur evier",
                "Mitigeur lavabo",
                "Mitigeur douche",
                "Robinet de puisage",
                "Chasse d'eau reservoir",
                "Cuvette WC a l'anglaise",
                "Cuvette WC a la turque",
                "Lavabo ceramique blanc",
                "Evier inox 1 bac",
                "Evier inox 2 bacs",
                "Douchette avec flexible",
                "Colonne de douche",
                "Siphon de sol",
                "Siphon lavabo",
                "Flexible d'alimentation 40cm",
                "Flexible d'alimentation 60cm",
                "Reservoir d'eau PVC 500L",
                "Reservoir d'eau PVC 1000L",
                "Pompe a eau surface 0.5CV",
                "Pompe immergee 1CV",
                "Tuyau flexible d'arrosage 20m",
                "Raccord rapide karcher",
                "Ruban teflon",
                "Cle a molette 10 pouces",
                "Cle a pipe sanitaire",
                "Pince multiprise 250mm",
                "Bonde d'evier",
                "Bonde de baignoire",
                "Baignoire acrylique 170cm",
                "Receveur de douche 90x90",
                "Chateau d'eau 2000L",
                "Filtre a eau domestique",
                "Robinet flotteur reservoir",
                "Compteur d'eau 15mm"
        ));

        catalogue.put("Electricite", List.of(
                "Cable electrique H07V-U 1.5mm² (rouleau 100m)",
                "Cable electrique H07V-U 2.5mm² (rouleau 100m)",
                "Cable electrique H07V-U 4mm² (rouleau 100m)",
                "Cable electrique H07V-U 6mm² (rouleau 100m)",
                "Cable meplat 2x1.5mm² (rouleau 100m)",
                "Cable souple 3x2.5mm² (rouleau 50m)",
                "Gaine ICTA 16mm (rouleau 25m)",
                "Gaine ICTA 20mm (rouleau 25m)",
                "Disjoncteur 10A",
                "Disjoncteur 16A",
                "Disjoncteur 20A",
                "Disjoncteur 32A",
                "Disjoncteur differentiel 30mA",
                "Coffret electrique 6 modules",
                "Coffret electrique 12 modules",
                "Coffret electrique 18 modules",
                "Interrupteur simple allumage",
                "Interrupteur va-et-vient",
                "Prise de courant 2P+T",
                "Prise de courant etanche",
                "Boite de derivation",
                "Boite d'encastrement",
                "Douille B22",
                "Douille E27",
                "Ampoule LED 9W",
                "Ampoule LED 12W",
                "Ampoule LED 18W",
                "Tube neon LED 1.2m",
                "Reglette LED etanche 1.2m",
                "Projecteur LED 50W",
                "Projecteur LED 100W",
                "Lampe torche rechargeable",
                "Rallonge electrique 5m",
                "Multiprise 4 sorties",
                "Domino electrique",
                "Serre-cable",
                "Chatterton isolant (rouleau)",
                "Pince coupante electricien",
                "Tournevis testeur de tension",
                "Perceuse sans fil 18V",
                "Onduleur 1000VA",
                "Regulateur de charge solaire",
                "Panneau solaire 100W",
                "Panneau solaire 150W",
                "Batterie solaire 12V 100Ah",
                "Inverseur automatique groupe electrogene",
                "Groupe electrogene 3.5kVA",
                "Groupe electrogene 5kVA",
                "Ventilateur plafond",
                "Climatiseur split 1.5CV"
        ));

        catalogue.put("Quincaillerie Generale", List.of(
                "Cadenas laiton 40mm",
                "Cadenas laiton 60mm",
                "Serrure encastree",
                "Serrure a cylindre",
                "Poignee de porte",
                "Charniere porte 4 pouces",
                "Butoir de porte",
                "Verrou a targette",
                "Chaine de securite (m)",
                "Corde en nylon 10mm (m)",
                "Corde en sisal 8mm (m)",
                "Adhesif double face",
                "Ruban adhesif large",
                "Colle forte multi-usage",
                "Silicone sanitaire transparent",
                "Gants de protection (paire)",
                "Casque de chantier",
                "Lunettes de protection",
                "Masque anti-poussiere",
                "Bottes de securite",
                "Ceinture de securite harnais",
                "Brouette de chantier",
                "Pelle ronde",
                "Pelle carree",
                "Pioche",
                "Houe",
                "Rateau de jardin",
                "Machette",
                "Seau plastique 15L",
                "Seau plastique 20L",
                "Bassine plastique",
                "Arrosoir 10L",
                "Brosse metallique",
                "Eponge abrasive",
                "Balai de chantier",
                "Bache plastique 4x5m",
                "Corde a linge",
                "Fil nylon tresse",
                "Punaises (boite)",
                "Agrafes pour agrafeuse",
                "Cordon bungee",
                "Cadenas a combinaison",
                "Chaine a cadenasser",
                "Metre ruban 5m",
                "Metre ruban 8m",
                "Niveau a bulle 60cm",
                "Niveau a bulle 100cm",
                "Fil a plomb",
                "Equerre de menuisier",
                "Marqueur de chantier"
        ));

        catalogue.put("Carrelage & Revetement", List.of(
                "Carreau sol gres cerame 30x30 (m²)",
                "Carreau sol gres cerame 40x40 (m²)",
                "Carreau sol gres cerame 60x60 (m²)",
                "Carreau mural faience 20x30 (m²)",
                "Carreau mural faience 25x40 (m²)",
                "Carreau exterieur antiderapant 30x30 (m²)",
                "Plinthe carrelage assortie (m)",
                "Nez de marche carrelage (m)",
                "Croisillon carrelage 2mm (sachet 100)",
                "Croisillon carrelage 3mm (sachet 100)",
                "Coupe-carreaux manuel",
                "Disque diamant carrelage 115mm",
                "Disque diamant carrelage 230mm",
                "Peigne a colle 6mm",
                "Truelle crantee",
                "Eponge a joint",
                "Profile de finition carrelage aluminium",
                "Dalle exterieure beton 40x40",
                "Pave autobloquant gris (m²)",
                "Pave autobloquant rouge (m²)",
                "Bordure de jardin beton (m)",
                "Marbre reconstitue 60x60 (m²)",
                "Granito poli 40x40 (m²)",
                "Revetement PVC sol (m²)",
                "Papier peint intisse (rouleau)",
                "Colle papier peint (paquet)",
                "Faux plafond dalle 60x60",
                "Ossature faux plafond (kit)",
                "Rail de faux plafond (m)",
                "Plaque de platre BA13 (panneau)",
                "Plaque de platre hydrofuge (panneau)",
                "Vis placo 25mm (boite 100)",
                "Enduit a joint placo (sac 5kg)",
                "Bande a joint placo (rouleau)",
                "Isolant laine de verre (rouleau)",
                "Polystyrene expanse 4cm (panneau)",
                "Film pare-vapeur (rouleau)",
                "Seuil de porte aluminium",
                "Corniere de protection d'angle",
                "Baguette d'angle carrelage",
                "Grille de ventilation",
                "Grille caillebotis",
                "Revetement mural PVC lambris",
                "Adhesif de finition carrelage",
                "Kit de renovation joint",
                "Nettoyant carrelage acide",
                "Produit hydrofuge dalle",
                "Vernis pour terrasse",
                "Peinture pour sol garage",
                "Carreau ciment decoratif 20x20 (m²)"
        ));

        catalogue.put("Outillage", List.of(
                "Marteau de charpentier 500g",
                "Marteau de macon 1kg",
                "Masse 3kg",
                "Masse 5kg",
                "Pied de biche 60cm",
                "Scie egoine",
                "Scie a metaux",
                "Scie circulaire electrique",
                "Scie sauteuse electrique",
                "Perceuse a percussion filaire",
                "Perceuse visseuse sans fil 12V",
                "Meuleuse d'angle 115mm",
                "Meuleuse d'angle 230mm",
                "Ponceuse electrique",
                "Rabot electrique",
                "Defonceuse electrique",
                "Betonniere 150L",
                "Vibreur a beton electrique",
                "Truelle de macon",
                "Taloche de macon",
                "Auge de macon",
                "Fil a tracer",
                "Cordeau traceur",
                "Niveau laser",
                "Metre laser electronique",
                "Equerre metallique 40cm",
                "Pince coupante universelle",
                "Pince etau",
                "Tournevis plat (jeu)",
                "Tournevis cruciforme (jeu)",
                "Jeu de cles plates",
                "Jeu de cles a pipe",
                "Cle a molette 8 pouces",
                "Cle a molette 12 pouces",
                "Etau de table",
                "Enclume portable",
                "Chalumeau a gaz",
                "Poste a souder electrique 200A",
                "Masque de soudure",
                "Gants de soudeur",
                "Coffret a outils 50 pieces",
                "Caisse a outils metallique",
                "Etabli pliant",
                "Echelle telescopique 3.8m",
                "Echelle coulissante 5m",
                "Echafaudage modulaire (kit)",
                "Compresseur d'air 50L",
                "Cloueuse pneumatique",
                "Agrafeuse pneumatique",
                "Diable de manutention"
        ));

        catalogue.put("Toiture & Etancheite", List.of(
                "Tole ondulee aluminium 4/10 (2m)",
                "Tole ondulee aluminium 4/10 (3m)",
                "Tole ondulee galvanisee 6/10 (2m)",
                "Tole ondulee galvanisee 6/10 (3m)",
                "Bac acier isole sandwich (m²)",
                "Tuile romane terre cuite",
                "Tuile plate beton",
                "Faitiere de toiture aluminium",
                "Faitiere de toiture tuile",
                "Gouttiere PVC 2m",
                "Descente d'eau pluviale PVC 2m",
                "Coude gouttiere 90 degres",
                "Crochet de gouttiere",
                "Bande d'etancheite bitumineuse (rouleau)",
                "Membrane d'etancheite EPDM (rouleau)",
                "Feutre bitume 36S (rouleau)",
                "Enduit d'etancheite toiture (bidon 20kg)",
                "Peinture d'etancheite toiture (pot 5L)",
                "Mastic bitumineux (cartouche)",
                "Panne de charpente 6x8cm (4m)",
                "Panne de charpente 8x10cm (4m)",
                "Chevron de toiture 6x8cm (4m)",
                "Liteau bois 3x4cm (3m)",
                "Fixation tole auto-perceuse (boite 100)",
                "Vis a toiture avec rondelle EPDM (boite 100)",
                "Solin de toiture aluminium",
                "Closoir de toiture ventile",
                "Grille anti-oiseaux gouttiere",
                "Isolant toiture laine de roche (rouleau)",
                "Ecran de sous-toiture (rouleau)",
                "Fenetre de toit 55x78",
                "Lanterneau translucide",
                "Plaque translucide polycarbonate (2m)",
                "Plaque translucide polycarbonate (3m)",
                "Crapaudine de gouttiere",
                "Naissance de gouttiere",
                "Talon de gouttiere",
                "Bardage bois exterieur (lame 4m)",
                "Bardage PVC exterieur (lame 4m)",
                "Charpente metallique prefabriquee (kit)",
                "Ferme de charpente prefabriquee",
                "Contre-latte 2x5cm (3m)",
                "Ecran pare-pluie (rouleau)",
                "Colle neoprene toiture (bidon 1L)",
                "Resine polyurethane etancheite (kit 5kg)",
                "Grille de ventilation faitage (m)",
                "Kit reparation fuite toiture",
                "Peinture reflechissante toiture (pot 5L)",
                "Cable de securite toiture (m)",
                "Point d'ancrage toiture"
        ));

        return catalogue;
    }
}
