-- Idempotent sur la colonne/contrainte (et non sur les simples ALTER classiques) car certaines
-- bases de dev locales avaient deja une colonne email ajoutee hors Flyway (drift constate le
-- 2026-08-01 sur la base XAMPP quincaillerie_mvogt : colonne email deja presente et peuplee avec
-- de vraies adresses) — on ne touche pas aux donnees existantes, on complete seulement ce qui manque.

SET @db := DATABASE();

SET @stmt := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'utilisateurs' AND COLUMN_NAME = 'email') = 0,
    'ALTER TABLE utilisateurs ADD COLUMN email VARCHAR(255) NULL',
    'SELECT 1'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE utilisateurs SET email = CONCAT(login, '@quincaillerie.local') WHERE email IS NULL;

ALTER TABLE utilisateurs MODIFY COLUMN email VARCHAR(255) NOT NULL;

SET @stmt := (SELECT IF(
    (SELECT COUNT(*) FROM information_schema.STATISTICS
     WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'utilisateurs' AND COLUMN_NAME = 'email' AND NON_UNIQUE = 0) = 0,
    'ALTER TABLE utilisateurs ADD CONSTRAINT uk_utilisateurs_email UNIQUE (email)',
    'SELECT 1'
));
PREPARE stmt FROM @stmt;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
