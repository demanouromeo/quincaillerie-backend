-- Jeu de donnees de demonstration pour tester l'application sur un environnement
-- fraichement provisionne (Railway) avec des donnees comparables a la base de dev
-- locale. Reutilise deliberement les memes noms de categories, le meme
-- fournisseur-marqueur et les memes comptes de demo que DemoDataSeeder
-- (CommandLineRunner, profil "demo-data", voir seed/DemoDataSeeder.java) : sur une
-- base ou ce seeder a deja tourne (dev locale), les inserts "reference" ci-dessous
-- (categories/fournisseur marqueur/comptes) sont detectes existants et sautes -
-- seuls les produits/mouvements propres a cette migration (prefixe "V8-") s'ajoutent,
-- sans collision ni doublon de compte.
-- N'inclut pas le compte admin (deja couvert par V7) ni parametres_magasin (deja
-- rempli par V5 + V6).

-- ============================================================
-- Categories (idempotent sur le nom)
-- ============================================================
INSERT INTO categories (nom) SELECT 'Ciment & Beton' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Ciment & Beton');
INSERT INTO categories (nom) SELECT 'Fer & Metallerie' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Fer & Metallerie');
INSERT INTO categories (nom) SELECT 'Bois & Menuiserie' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Bois & Menuiserie');
INSERT INTO categories (nom) SELECT 'Peinture & Enduits' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Peinture & Enduits');
INSERT INTO categories (nom) SELECT 'Plomberie' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Plomberie');
INSERT INTO categories (nom) SELECT 'Electricite' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Electricite');
INSERT INTO categories (nom) SELECT 'Quincaillerie Generale' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Quincaillerie Generale');
INSERT INTO categories (nom) SELECT 'Carrelage & Revetement' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Carrelage & Revetement');
INSERT INTO categories (nom) SELECT 'Outillage' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Outillage');
INSERT INTO categories (nom) SELECT 'Toiture & Etancheite' WHERE NOT EXISTS (SELECT 1 FROM categories WHERE nom = 'Toiture & Etancheite');

-- ============================================================
-- Fournisseurs (idempotent sur le nom - pas de contrainte UNIQUE en base,
-- garde applicative uniquement)
-- ============================================================
INSERT INTO fournisseurs (nom, contact, adresse)
SELECT 'CIMENCAM Distribution Yaounde', '+237 677 10 20 30', 'Zone Industrielle Mbankolo, Yaounde'
WHERE NOT EXISTS (SELECT 1 FROM fournisseurs WHERE nom = 'CIMENCAM Distribution Yaounde');

INSERT INTO fournisseurs (nom, contact, adresse)
SELECT 'Quincaillerie Generale du Mfoundi', '+237 699 11 22 33', 'Marche Mokolo, Yaounde'
WHERE NOT EXISTS (SELECT 1 FROM fournisseurs WHERE nom = 'Quincaillerie Generale du Mfoundi');

INSERT INTO fournisseurs (nom, contact, adresse)
SELECT 'Etablissements Fotso & Fils', '+237 655 44 55 66', 'Avenue Kennedy, Yaounde'
WHERE NOT EXISTS (SELECT 1 FROM fournisseurs WHERE nom = 'Etablissements Fotso & Fils');

INSERT INTO fournisseurs (nom, contact, adresse)
SELECT 'SOCADEP Materiaux BTP', '+237 677 88 99 00', 'Zone Industrielle Bassa, Douala'
WHERE NOT EXISTS (SELECT 1 FROM fournisseurs WHERE nom = 'SOCADEP Materiaux BTP');

INSERT INTO fournisseurs (nom, contact, adresse)
SELECT 'Import-Export Ngoumou', '+237 690 12 34 56', 'Carrefour Warda, Yaounde'
WHERE NOT EXISTS (SELECT 1 FROM fournisseurs WHERE nom = 'Import-Export Ngoumou');

-- ============================================================
-- Utilisateurs de demonstration (non-admin) - idempotent sur le login.
-- Hash BCrypt (strength 10) generes avec le meme BCryptPasswordEncoder que
-- SecurityConfig.passwordEncoder() : gestion123 / vendeur123.
-- ============================================================
INSERT INTO utilisateurs (nom, login, email, mot_de_passe, role, actif)
SELECT 'Gestionnaire Demo', 'gestionnaire_demo', 'gestionnaire_demo@quincaillerie.local',
       '$2a$10$Lgw0WoaN.G.9C1yWo4UcsO9rgFnYR8gpiMThe8wjDJ0TL6jro5Ca2', 'GESTIONNAIRE', TRUE
WHERE NOT EXISTS (SELECT 1 FROM utilisateurs WHERE login = 'gestionnaire_demo');

INSERT INTO utilisateurs (nom, login, email, mot_de_passe, role, actif)
SELECT 'Vendeur Demo', 'vendeur_demo', 'vendeur_demo@quincaillerie.local',
       '$2a$10$z2QLcYk0x6TG7vDTG/SADOdKl5hf2YvPyDMYO1lfRZFP2IR6nI24q', 'VENDEUR', TRUE
WHERE NOT EXISTS (SELECT 1 FROM utilisateurs WHERE login = 'vendeur_demo');

-- ============================================================
-- Produits (24, prefixe "V8-" pour ne jamais entrer en collision avec des
-- references existantes) - idempotent sur la reference (UNIQUE en base).
-- Stock initial des produits V8-CIM-001, V8-FER-001, V8-CIM-002, V8-BOI-001,
-- V8-PEI-001, V8-PLO-001, V8-ELE-001, V8-QUI-001, V8-CAR-001, V8-OUT-001,
-- V8-TOI-001, V8-FER-002 calcule pour rester coherent avec les mouvements de
-- stock (approvisionnements/ventes) inseres plus bas dans ce fichier. Les
-- autres produits n'ont pas de mouvement associe : stock statique, dont deux
-- volontairement sous le seuil d'alerte (V8-PLO-003, V8-ELE-003) pour
-- pouvoir tester l'ecran d'alertes.
-- ============================================================
INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-CIM-001', 'Ciment CIMENCAM 32.5 50kg', (SELECT id FROM categories WHERE nom = 'Ciment & Beton'), 'sac', 4300, 5000, 20, 160
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-CIM-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-FER-001', 'Fer a beton 8mm barre 12m', (SELECT id FROM categories WHERE nom = 'Fer & Metallerie'), 'barre', 3800, 4500, 30, 185
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-FER-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-CIM-002', 'Sable de riviere', (SELECT id FROM categories WHERE nom = 'Ciment & Beton'), 'm3', 8000, 10000, 5, 32
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-CIM-002');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-BOI-001', 'Planche de coffrage 4m', (SELECT id FROM categories WHERE nom = 'Bois & Menuiserie'), 'piece', 1800, 2500, 15, 74
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-BOI-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-PEI-001', 'Peinture vinylique blanche 20L', (SELECT id FROM categories WHERE nom = 'Peinture & Enduits'), 'bidon', 22000, 28000, 8, 27
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-PEI-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-PLO-001', 'Tuyau PVC 100mm 4m', (SELECT id FROM categories WHERE nom = 'Plomberie'), 'piece', 3200, 4200, 10, 42
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-PLO-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-ELE-001', 'Cable electrique 2.5mm2 (rouleau 100m)', (SELECT id FROM categories WHERE nom = 'Electricite'), 'rouleau', 35000, 42000, 5, 18
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-ELE-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-QUI-001', 'Cadenas laiton 50mm', (SELECT id FROM categories WHERE nom = 'Quincaillerie Generale'), 'piece', 1500, 2200, 25, 82
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-QUI-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-CAR-001', 'Carreau ceramique 40x40', (SELECT id FROM categories WHERE nom = 'Carrelage & Revetement'), 'm2', 3500, 4500, 20, 85
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-CAR-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-OUT-001', 'Marteau menuisier 500g', (SELECT id FROM categories WHERE nom = 'Outillage'), 'piece', 2200, 3000, 10, 35
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-OUT-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-TOI-001', 'Tole bac alu 2m', (SELECT id FROM categories WHERE nom = 'Toiture & Etancheite'), 'piece', 4800, 6000, 15, 34
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-TOI-001');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-FER-002', 'Pointe 5cm boite 1kg', (SELECT id FROM categories WHERE nom = 'Fer & Metallerie'), 'boite', 900, 1300, 30, 165
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-FER-002');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-CIM-003', 'Ciment CIMENCAM 32.5 25kg', (SELECT id FROM categories WHERE nom = 'Ciment & Beton'), 'sac', 2200, 2700, 25, 80
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-CIM-003');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-BOI-002', 'Contreplaque 18mm 2440x1220', (SELECT id FROM categories WHERE nom = 'Bois & Menuiserie'), 'feuille', 15000, 19000, 8, 25
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-BOI-002');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-BOI-003', 'Chevron 6x8 4m', (SELECT id FROM categories WHERE nom = 'Bois & Menuiserie'), 'piece', 1600, 2200, 20, 60
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-BOI-003');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-PEI-002', 'Enduit de facade 25kg', (SELECT id FROM categories WHERE nom = 'Peinture & Enduits'), 'sac', 9000, 11500, 10, 40
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-PEI-002');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-PLO-002', 'Robinet melangeur evier', (SELECT id FROM categories WHERE nom = 'Plomberie'), 'piece', 6500, 8500, 8, 18
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-PLO-002');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-PLO-003', 'Siphon lavabo PVC', (SELECT id FROM categories WHERE nom = 'Plomberie'), 'piece', 1200, 1800, 10, 3
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-PLO-003');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-ELE-002', 'Disjoncteur 20A', (SELECT id FROM categories WHERE nom = 'Electricite'), 'piece', 2800, 3600, 15, 45
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-ELE-002');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-ELE-003', 'Ampoule LED 12W', (SELECT id FROM categories WHERE nom = 'Electricite'), 'piece', 900, 1400, 15, 5
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-ELE-003');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-QUI-002', 'Charniere inox 100mm', (SELECT id FROM categories WHERE nom = 'Quincaillerie Generale'), 'piece', 800, 1200, 20, 60
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-QUI-002');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-CAR-002', 'Colle carrelage 25kg', (SELECT id FROM categories WHERE nom = 'Carrelage & Revetement'), 'sac', 4200, 5500, 10, 30
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-CAR-002');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-OUT-002', 'Scie egoine', (SELECT id FROM categories WHERE nom = 'Outillage'), 'piece', 3500, 4800, 8, 12
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-OUT-002');

INSERT INTO produits (reference, nom, categorie_id, unite, prix_achat, prix_vente, seuil_alerte, stock_actuel)
SELECT 'V8-TOI-002', 'Vis autoperceuse toiture (boite de 100)', (SELECT id FROM categories WHERE nom = 'Toiture & Etancheite'), 'boite', 6000, 7800, 10, 22
WHERE NOT EXISTS (SELECT 1 FROM produits WHERE reference = 'V8-TOI-002');

-- ============================================================
-- Approvisionnements + lignes + mouvements de stock (type APPRO).
-- Non idempotent (pas de cle metier naturelle) : Flyway garantit que ce
-- fichier ne s'execute qu'une seule fois par base, donc pas de risque de
-- doublon a chaque redemarrage.
-- ============================================================
SET @gestionnaire_id := (SELECT id FROM utilisateurs WHERE login = 'gestionnaire_demo');
SET @vendeur_id := (SELECT id FROM utilisateurs WHERE login = 'vendeur_demo');

-- APPRO A : CIMENCAM Distribution Yaounde
SET @dt := DATE_SUB(NOW(), INTERVAL 20 DAY);
INSERT INTO approvisionnements (date_appro, fournisseur_id, gestionnaire_id)
VALUES (@dt, (SELECT id FROM fournisseurs WHERE nom = 'CIMENCAM Distribution Yaounde'), @gestionnaire_id);
SET @appro := LAST_INSERT_ID();
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-CIM-001'), 200, 4300);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-CIM-001'), 'APPRO', 200, @dt, CONCAT('APPRO#', @appro));
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-CIM-002'), 40, 8000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-CIM-002'), 'APPRO', 40, @dt, CONCAT('APPRO#', @appro));
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-FER-001'), 150, 3800);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-FER-001'), 'APPRO', 150, @dt, CONCAT('APPRO#', @appro));

-- APPRO B : Quincaillerie Generale du Mfoundi
SET @dt := DATE_SUB(NOW(), INTERVAL 17 DAY);
INSERT INTO approvisionnements (date_appro, fournisseur_id, gestionnaire_id)
VALUES (@dt, (SELECT id FROM fournisseurs WHERE nom = 'Quincaillerie Generale du Mfoundi'), @gestionnaire_id);
SET @appro := LAST_INSERT_ID();
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-QUI-001'), 100, 1500);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-QUI-001'), 'APPRO', 100, @dt, CONCAT('APPRO#', @appro));
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-FER-002'), 200, 900);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-FER-002'), 'APPRO', 200, @dt, CONCAT('APPRO#', @appro));
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-OUT-001'), 40, 2200);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-OUT-001'), 'APPRO', 40, @dt, CONCAT('APPRO#', @appro));

-- APPRO C : Etablissements Fotso & Fils
SET @dt := DATE_SUB(NOW(), INTERVAL 14 DAY);
INSERT INTO approvisionnements (date_appro, fournisseur_id, gestionnaire_id)
VALUES (@dt, (SELECT id FROM fournisseurs WHERE nom = 'Etablissements Fotso & Fils'), @gestionnaire_id);
SET @appro := LAST_INSERT_ID();
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-BOI-001'), 80, 1800);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-BOI-001'), 'APPRO', 80, @dt, CONCAT('APPRO#', @appro));
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-CAR-001'), 100, 3500);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-CAR-001'), 'APPRO', 100, @dt, CONCAT('APPRO#', @appro));

-- APPRO D : SOCADEP Materiaux BTP
SET @dt := DATE_SUB(NOW(), INTERVAL 11 DAY);
INSERT INTO approvisionnements (date_appro, fournisseur_id, gestionnaire_id)
VALUES (@dt, (SELECT id FROM fournisseurs WHERE nom = 'SOCADEP Materiaux BTP'), @gestionnaire_id);
SET @appro := LAST_INSERT_ID();
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-PEI-001'), 30, 22000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-PEI-001'), 'APPRO', 30, @dt, CONCAT('APPRO#', @appro));
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-PLO-001'), 50, 3200);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-PLO-001'), 'APPRO', 50, @dt, CONCAT('APPRO#', @appro));
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-TOI-001'), 40, 4800);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-TOI-001'), 'APPRO', 40, @dt, CONCAT('APPRO#', @appro));

-- APPRO E : Import-Export Ngoumou
SET @dt := DATE_SUB(NOW(), INTERVAL 9 DAY);
INSERT INTO approvisionnements (date_appro, fournisseur_id, gestionnaire_id)
VALUES (@dt, (SELECT id FROM fournisseurs WHERE nom = 'Import-Export Ngoumou'), @gestionnaire_id);
SET @appro := LAST_INSERT_ID();
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-ELE-001'), 20, 35000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-ELE-001'), 'APPRO', 20, @dt, CONCAT('APPRO#', @appro));
INSERT INTO lignes_approvisionnement (approvisionnement_id, produit_id, quantite, prix_achat_unitaire)
VALUES (@appro, (SELECT id FROM produits WHERE reference = 'V8-FER-001'), 50, 3800);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-FER-001'), 'APPRO', 50, @dt, CONCAT('APPRO#', @appro));

-- ============================================================
-- Ventes + lignes + mouvements de stock (type VENTE, quantite negative).
-- ============================================================

-- V1
SET @dt := DATE_SUB(NOW(), INTERVAL 8 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 175000);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-CIM-001'), 25, 5000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-CIM-001'), 'VENTE', -25, @dt, CONCAT('VENTE#', @vente));
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-CIM-002'), 5, 10000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-CIM-002'), 'VENTE', -5, @dt, CONCAT('VENTE#', @vente));

-- V2
SET @dt := DATE_SUB(NOW(), INTERVAL 7 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 48000);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-QUI-001'), 10, 2200);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-QUI-001'), 'VENTE', -10, @dt, CONCAT('VENTE#', @vente));
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-FER-002'), 20, 1300);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-FER-002'), 'VENTE', -20, @dt, CONCAT('VENTE#', @vente));

-- V3
SET @dt := DATE_SUB(NOW(), INTERVAL 7 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 15000);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-BOI-001'), 6, 2500);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-BOI-001'), 'VENTE', -6, @dt, CONCAT('VENTE#', @vente));

-- V4
SET @dt := DATE_SUB(NOW(), INTERVAL 6 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 112500);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-CAR-001'), 15, 4500);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-CAR-001'), 'VENTE', -15, @dt, CONCAT('VENTE#', @vente));
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-FER-001'), 10, 4500);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-FER-001'), 'VENTE', -10, @dt, CONCAT('VENTE#', @vente));

-- V5
SET @dt := DATE_SUB(NOW(), INTERVAL 5 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 48600);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-OUT-001'), 5, 3000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-OUT-001'), 'VENTE', -5, @dt, CONCAT('VENTE#', @vente));
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-PLO-001'), 8, 4200);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-PLO-001'), 'VENTE', -8, @dt, CONCAT('VENTE#', @vente));

-- V6
SET @dt := DATE_SUB(NOW(), INTERVAL 5 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 75000);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-CIM-001'), 15, 5000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-CIM-001'), 'VENTE', -15, @dt, CONCAT('VENTE#', @vente));

-- V7
SET @dt := DATE_SUB(NOW(), INTERVAL 4 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 120000);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-TOI-001'), 6, 6000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-TOI-001'), 'VENTE', -6, @dt, CONCAT('VENTE#', @vente));
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-ELE-001'), 2, 42000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-ELE-001'), 'VENTE', -2, @dt, CONCAT('VENTE#', @vente));

-- V8
SET @dt := DATE_SUB(NOW(), INTERVAL 3 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 84000);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-PEI-001'), 3, 28000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-PEI-001'), 'VENTE', -3, @dt, CONCAT('VENTE#', @vente));

-- V9
SET @dt := DATE_SUB(NOW(), INTERVAL 2 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 47600);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-QUI-001'), 8, 2200);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-QUI-001'), 'VENTE', -8, @dt, CONCAT('VENTE#', @vente));
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-CIM-002'), 3, 10000);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-CIM-002'), 'VENTE', -3, @dt, CONCAT('VENTE#', @vente));

-- V10
SET @dt := DATE_SUB(NOW(), INTERVAL 1 DAY);
INSERT INTO ventes (date_vente, vendeur_id, montant_total) VALUES (@dt, @vendeur_id, 42000);
SET @vente := LAST_INSERT_ID();
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-FER-002'), 15, 1300);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-FER-002'), 'VENTE', -15, @dt, CONCAT('VENTE#', @vente));
INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_vente_unitaire)
VALUES (@vente, (SELECT id FROM produits WHERE reference = 'V8-FER-001'), 5, 4500);
INSERT INTO mouvements_stock (produit_id, type, quantite, date, reference)
VALUES ((SELECT id FROM produits WHERE reference = 'V8-FER-001'), 'VENTE', -5, @dt, CONCAT('VENTE#', @vente));
