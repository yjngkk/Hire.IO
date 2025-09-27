-- Migration pour ajouter les colonnes de publication LinkedIn
-- À exécuter sur la base de données existante

-- Ajouter la colonne published si elle n'existe pas
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'formulaire' AND column_name = 'published') THEN
        ALTER TABLE formulaire ADD COLUMN published BOOLEAN DEFAULT FALSE;
    END IF;
END $$;

-- Ajouter la colonne published_at si elle n'existe pas
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'formulaire' AND column_name = 'published_at') THEN
        ALTER TABLE formulaire ADD COLUMN published_at TIMESTAMP;
    END IF;
END $$;

-- Mettre à jour les offres existantes pour avoir published = false par défaut
UPDATE formulaire SET published = FALSE WHERE published IS NULL;
