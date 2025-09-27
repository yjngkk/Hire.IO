# Fonctionnalités de Publication LinkedIn

## Vue d'ensemble

Cette mise à jour ajoute la gestion de la publication unique sur LinkedIn avec possibilité d'annulation.

## Nouvelles fonctionnalités

### Backend

#### Modèle Form
- Ajout de la colonne `published` (BOOLEAN, défaut FALSE)
- Ajout de la colonne `publishedAt` (TIMESTAMP)

#### Contrôleur LinkedIn
- **POST** `/api/linkedin/publish/{formId}` : Publie une offre sur LinkedIn
  - Vérifie que l'offre n'a pas déjà été publiée
  - Met à jour le statut de publication
  - Retourne une erreur si déjà publiée

- **DELETE** `/api/linkedin/unpublish/{formId}` : Annule la publication
  - Marque l'offre comme non publiée dans la base de données
  - Note : LinkedIn API ne permet pas de supprimer des posts

#### Contrôleur Form
- **GET** `/api/forms/published` : Récupère toutes les offres publiées
- **GET** `/api/forms/unpublished` : Récupère toutes les offres non publiées

#### Repository
- `findByPublishedTrue()` : Offres publiées
- `findByPublishedFalse()` : Offres non publiées
- `findByPublishedTrueOrderByPublishedAtDesc()` : Offres publiées par date

### Frontend

#### Interface Jobs
- Affichage du statut de publication pour chaque offre
- Bouton "Publier" pour les offres non publiées
- Badge "Publié" avec date pour les offres publiées
- Bouton "Annuler" pour annuler la publication
- Statistiques : total, publiées, non publiées
- Filtres : toutes, publiées, non publiées

#### États visuels
- **Non publié** : Bouton "Publier" bleu
- **Publié** : Badge vert "Publié" + date + bouton "Annuler" rouge

## Migration de base de données

Exécuter le fichier `src/main/resources/migration.sql` pour ajouter les nouvelles colonnes.

## Utilisation

1. **Publication** : Cliquer sur "Publier" → Confirmation → Publication sur LinkedIn
2. **Annulation** : Cliquer sur "Annuler" → Suppression du statut publié
3. **Filtrage** : Utiliser les boutons de filtre pour voir les offres par statut
4. **Statistiques** : Voir le nombre d'offres par statut en haut de page

## Notes importantes

- Une offre ne peut être publiée qu'une seule fois
- L'annulation ne supprime pas le post LinkedIn (limitation API)
- Les offres existantes sont marquées comme non publiées par défaut
