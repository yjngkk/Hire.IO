package ma.nexotek.HireCraft.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "api_config")
@Data
@NoArgsConstructor
public class ApiConfig {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "provider_name", unique = true)
    private String providerName;
    
    @Column(name = "api_key")
    private String apiKey;
    
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    public ApiConfig(String providerName, String apiKey, Boolean isActive) {
        this.providerName = providerName;
        this.apiKey = apiKey;
        this.isActive = isActive;
    }
}