package ma.nexotek.HireCraft.config;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

@Component
public class DataSourceBasedMultiTenantConnectionProviderImpl implements MultiTenantConnectionProvider<String> {

    private static final long serialVersionUID = 1L;

    // Liste des tenants configurés
    private static final List<String> CONFIGURED_TENANTS = Arrays.asList("nexotek", "nemo", "sii");
    private static final String DEFAULT_TENANT = "public";

    @Autowired
    private DataSource dataSource;

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        try {
            // Utiliser le tenant par défaut si null ou vide
            if (tenantIdentifier == null || tenantIdentifier.trim().isEmpty()) {
                tenantIdentifier = DEFAULT_TENANT;
            }

            // Valider le tenant
            if (!CONFIGURED_TENANTS.contains(tenantIdentifier) && !DEFAULT_TENANT.equals(tenantIdentifier)) {
                System.err.println("⚠️ Unknown tenant: " + tenantIdentifier + ", using default");
                tenantIdentifier = DEFAULT_TENANT;
            }

            System.out.println("🔄 Switching to tenant schema: " + tenantIdentifier);

            // Créer le schéma s'il n'existe pas (sauf pour public)
            if (!DEFAULT_TENANT.equals(tenantIdentifier)) {
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("CREATE SCHEMA IF NOT EXISTS " + tenantIdentifier);
                }
            }

            // Changer le schéma courant
            if (DEFAULT_TENANT.equals(tenantIdentifier)) {
                connection.setSchema(null); // PostgreSQL public schema
            } else {
                connection.setSchema(tenantIdentifier);
            }

            return connection;
        } catch (SQLException e) {
            connection.close();
            throw new SQLException("Could not initialize tenant schema: " + tenantIdentifier, e);
        }
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            if (!connection.isClosed()) {
                // Remettre le schéma par défaut
                connection.setSchema(null); // PostgreSQL public schema
            }
        } catch (SQLException e) {
            System.err.println("Warning during schema reset: " + e.getMessage());
        } finally {
            if (!connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        releaseConnection(null, connection);
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return false;
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        return null;
    }

    // Méthode utilitaire pour obtenir la liste des tenants
    public static List<String> getConfiguredTenants() {
        return CONFIGURED_TENANTS;
    }
}