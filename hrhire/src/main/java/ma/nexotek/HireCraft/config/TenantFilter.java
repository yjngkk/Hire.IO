package ma.nexotek.HireCraft.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    private final TenantIdentifierResolver tenantIdentifierResolver;
    private static final String TENANT_ID_CLAIM = "tenant_id";
    private static final String TENANT_HEADER = "X-Tenant-ID";

    public TenantFilter(TenantIdentifierResolver tenantIdentifierResolver) {
        this.tenantIdentifierResolver = tenantIdentifierResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        System.out.println("🔍 TenantFilter processing request: " + requestUri);

        try {
            String tenantId = extractTenantId(request);
            System.out.println("➡️ TenantFilter: extracted tenant_id = " + tenantId);

            if (tenantId != null && !tenantId.trim().isEmpty()) {
                tenantIdentifierResolver.setCurrentTenant(tenantId);
                System.out.println("✅ TenantFilter: set current tenant to " + tenantId);
            } else {
                System.out.println("⚠️ TenantFilter: no tenant found, using default");
            }

            filterChain.doFilter(request, response);
        } finally {
            tenantIdentifierResolver.clear();
            System.out.println("🧹 TenantFilter: cleared tenant context");
        }
    }

    private String extractTenantId(HttpServletRequest request) {
        // 1. Essayer d'abord le header HTTP
        String tenantFromHeader = request.getHeader(TENANT_HEADER);
        if (tenantFromHeader != null && !tenantFromHeader.trim().isEmpty()) {
            System.out.println("📋 TenantFilter: found tenant in header: " + tenantFromHeader);
            return tenantFromHeader.trim();
        }

        // 2. Essayer le JWT
        String tenantFromJwt = extractTenantFromJwt();
        if (tenantFromJwt != null && !tenantFromJwt.trim().isEmpty()) {
            System.out.println("🔐 TenantFilter: found tenant in JWT: " + tenantFromJwt);
            return tenantFromJwt.trim();
        }

        // 3. Essayer les paramètres de requête
        String tenantFromParam = request.getParameter("tenant");
        if (tenantFromParam != null && !tenantFromParam.trim().isEmpty()) {
            System.out.println("🔗 TenantFilter: found tenant in param: " + tenantFromParam);
            return tenantFromParam.trim();
        }

        return null;
    }

    private String extractTenantFromJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                return jwt.getClaimAsString(TENANT_ID_CLAIM);
            }
        } catch (Exception e) {
            System.err.println("❌ Error extracting tenant from JWT: " + e.getMessage());
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // Ne pas filtrer les endpoints publics
        return path.startsWith("/public/") ||
                path.startsWith("/h2-console/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/actuator/");
    }
}