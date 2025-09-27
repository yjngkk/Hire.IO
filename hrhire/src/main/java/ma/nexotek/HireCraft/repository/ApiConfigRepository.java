package ma.nexotek.HireCraft.repository;

import ma.nexotek.HireCraft.model.ApiConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiConfigRepository extends JpaRepository<ApiConfig, Long> {
    
    /**
     * Récupérer le modèle actif
     */
    Optional<ApiConfig> findByIsActiveTrue();
    
    /**
     * Récupérer une config par provider
     */
    Optional<ApiConfig> findByProviderName(String providerName);
}