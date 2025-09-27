-- src/main/resources/schema.sql

-- Création des schémas
CREATE SCHEMA IF NOT EXISTS nexotek;
CREATE SCHEMA IF NOT EXISTS nemo;
CREATE SCHEMA IF NOT EXISTS sii;

-- Création des tables Entreprise dans chaque schéma
CREATE TABLE IF NOT EXISTS nexotek.entreprise (
    id INT PRIMARY KEY,
    nom VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS nemo.entreprise (
    id INT PRIMARY KEY,
    nom VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS sii.entreprise (
    id INT PRIMARY KEY,
    nom VARCHAR(100)
);

-- Inserting quelques données dans chaque schéma
--INSERT INTO nexotek.entreprise (id, nom) VALUES (1, 'Nexotek SAS'), (2, 'Nexotek Solutions'), (3, 'Nexotek Innovations');
--INSERT INTO nemo.entreprise (id, nom) VALUES (1, 'Nemo Tech'), (2, 'Nemo Digital'), (3, 'Nemo Innovation Hub');
--INSERT INTO sii.entreprise (id, nom) VALUES (1, 'SII Services'), (2, 'SII Conseil'), (3, 'SII Developpement');

-- Ajouter la colonne published à la table Formulaire si elle n'existe pas
ALTER TABLE Formulaire ADD COLUMN IF NOT EXISTS published BOOLEAN DEFAULT FALSE;
ALTER TABLE Formulaire ADD COLUMN IF NOT EXISTS published_at TIMESTAMP;
