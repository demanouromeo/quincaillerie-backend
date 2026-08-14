-- Garantit un compte administrateur (login/mot de passe connus) des le premier
-- deploiement, independamment de AdminSeeder (CommandLineRunner qui ne seme
-- que si la table est vide au demarrage de l'appli - cette migration couvre
-- le cas d'un environnement fraichement provisionne, ex. Railway, ou l'ordre
-- exact migration/seeder au tout premier boot n'est pas garanti).
-- Hash BCrypt (strength 10) de "admin123", genere avec le meme
-- BCryptPasswordEncoder que SecurityConfig.passwordEncoder().
-- A CHANGER en production via le changement de mot de passe self-service
-- (PUT /api/compte/mot-de-passe) juste apres la premiere connexion.
INSERT INTO utilisateurs (nom, login, email, mot_de_passe, role, actif)
SELECT 'Administrateur', 'admin', 'admin@quincaillerie.local',
       '$2a$10$3FZlEbpg43NRi0U3jA71meAr2C13F0UafLwV14vgMoi6Oits.0.6m', 'ADMIN', TRUE
WHERE NOT EXISTS (SELECT 1 FROM utilisateurs WHERE login = 'admin');
