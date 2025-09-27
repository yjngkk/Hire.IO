package ma.nexotek.HireCraft.controller;


import ma.nexotek.HireCraft.model.Entreprise;
import ma.nexotek.HireCraft.repository.EntrepriseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entreprises")
public class EntrepriseController {

    @Autowired
    private EntrepriseRepository entrepriseRepository;

    // Récupérer toutes les entreprises
    @GetMapping
    public List<Entreprise> getAllEntreprises() {
        return entrepriseRepository.findAll();
    }

    // Récupérer une entreprise par id
    @GetMapping("/{id}")
    public ResponseEntity<Entreprise> getEntrepriseById(@PathVariable Integer id) {
        Optional<Entreprise> entreprise = entrepriseRepository.findById(id);
        if (entreprise.isPresent()) {
            return ResponseEntity.ok(entreprise.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Créer une nouvelle entreprise
    @PostMapping
    public ResponseEntity<Entreprise> createEntreprise(@RequestBody Entreprise entreprise) {
        if (entreprise.getId() != null) {
            return ResponseEntity.badRequest().build();
        }

        Entreprise savedEntreprise = entrepriseRepository.save(entreprise);
        return ResponseEntity.ok(savedEntreprise);
    }

    // Mettre à jour une entreprise
    @PutMapping("/{id}")
    public ResponseEntity<Entreprise> updateEntreprise(@PathVariable Integer id, @RequestBody Entreprise entrepriseDetails) {
        Optional<Entreprise> entrepriseOptional = entrepriseRepository.findById(id);
        if (entrepriseOptional.isPresent()) {
            Entreprise entreprise = entrepriseOptional.get();
            entreprise.setNom(entrepriseDetails.getNom());
            // Mettre à jour d'autres champs si besoin
            Entreprise updatedEntreprise = entrepriseRepository.save(entreprise);
            return ResponseEntity.ok(updatedEntreprise);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Supprimer une entreprise
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEntreprise(@PathVariable Integer id) {
        Optional<Entreprise> entrepriseOptional = entrepriseRepository.findById(id);
        if (entrepriseOptional.isPresent()) {
            entrepriseRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
