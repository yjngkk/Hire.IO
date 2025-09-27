package ma.nexotek.HireCraft.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class TenantIdentifierResolver implements CurrentTenantIdentifierResolver {

    private static final String DEFAULT_TENANT = "public";
    private static final String TENANT_ID_CLAIM = "tenant_id";

    private final ThreadLocal<String> currentTenant = new ThreadLocal<>();

    @Override
    public String resolveCurrentTenantIdentifier() {
        // 1. Vérifier si un tenant est déjà défini dans le ThreadLocal
        String tenantId = currentTenant.get();
        if (StringUtils.hasText(tenantId)) {
            System.out.println("✅ TenantIdentifierResolver: using ThreadLocal tenant_id = " + tenantId);
            return tenantId;
        }

        // 2. Essayer d'obtenir le tenant depuis le token JWT
        tenantId = getTenantFromJwt();
        if (StringUtils.hasText(tenantId)) {
            // Valider le tenant
            if (isValidTenant(tenantId)) {
                currentTenant.set(tenantId);
                System.out.println("✅ TenantIdentifierResolver: extracted tenant_id from JWT = " + tenantId);
                return tenantId;
            } else {
                System.out.println("⚠️ Invalid tenant from JWT: " + tenantId + ", using default");
            }
        }

        // 3. Retourner le tenant par défaut
        System.out.println("✅ TenantIdentifierResolver: using default tenant = " + DEFAULT_TENANT);
        return DEFAULT_TENANT;
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    private String getTenantFromJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getClaimAsString(TENANT_ID_CLAIM);
            }
        } catch (Exception e) {
            System.err.println("Error extracting tenant from JWT: " + e.getMessage());
        }
        return null;
    }

    private boolean isValidTenant(String tenantId) {
        return tenantId != null &&
                (DataSourceBasedMultiTenantConnectionProviderImpl.getConfiguredTenants().contains(tenantId) ||
                        DEFAULT_TENANT.equals(tenantId));
    }

    public void setCurrentTenant(String tenantId) {
        if (StringUtils.hasText(tenantId) && isValidTenant(tenantId)) {
            currentTenant.set(tenantId);
            System.out.println("🔄 TenantIdentifierResolver: manually set tenant to = " + tenantId);
        } else {
            currentTenant.remove();
            System.out.println("🔄 TenantIdentifierResolver: cleared tenant (invalid: " + tenantId + ")");
        }
    }

    public void clear() {
        currentTenant.remove();
        System.out.println("🔄 TenantIdentifierResolver: cleared tenant");
    }

    public String getCurrentTenant() {
        return currentTenant.get();
    }
}