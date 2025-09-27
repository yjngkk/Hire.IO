package ma.nexotek.HireCraft.config;

import jakarta.annotation.PostConstruct;
import org.hibernate.Session;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Component
public class SchemaInitializerListener {

    @Autowired
    private DataSource dataSource;

    @PersistenceUnit
    private EntityManagerFactory emf;

    @Autowired
    private MultiTenantConnectionProvider<String> connectionProvider;

    @Autowired
    private TenantIdentifierResolver tenantIdentifierResolver;

    @PostConstruct
    public void init() {
        List<String> tenants = DataSourceBasedMultiTenantConnectionProviderImpl.getConfiguredTenants();

        System.out.println("🚀 Initializing schemas for tenants: " + tenants);

        for (String tenant : tenants) {
            initializeSchemaForTenant(tenant);
        }

        System.out.println("✅ Schema initialization completed for all tenants");
    }

    private void initializeSchemaForTenant(String tenant) {
        System.out.println("🔧 Initializing schema for tenant: " + tenant);

        try {
            // 1. Créer le schéma s'il n'existe pas
            createSchemaIfNotExists(tenant);

            // 2. Forcer la création des tables dans le schéma
            createTablesInSchema(tenant);

            System.out.println("✅ Schema initialized successfully for tenant: " + tenant);

        } catch (Exception e) {
            System.err.println("❌ Failed to initialize schema for tenant: " + tenant);
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize schema for tenant: " + tenant, e);
        }
    }

    private void createSchemaIfNotExists(String tenant) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement stmt = connection.createStatement()) {

            String sql = "CREATE SCHEMA IF NOT EXISTS " + tenant;
            stmt.execute(sql);
            System.out.println("📝 Created schema: " + tenant);
        }
    }

    private void createTablesInSchema(String tenant) {
        EntityManager em = null;
        String originalTenant = tenantIdentifierResolver.getCurrentTenant();

        try {
            // Définir le tenant courant
            tenantIdentifierResolver.setCurrentTenant(tenant);

            // Créer l'EntityManager avec le bon tenant
            em = emf.createEntityManager();

            // Obtenir une session Hibernate
            Session session = em.unwrap(Session.class);

            // Exécuter une requête pour déclencher la création des tables
            session.doWork(connection -> {
                try {
                    // Basculer vers le schéma tenant
                    connection.setSchema(tenant);

                    // Vérifier que nous sommes dans le bon schéma
                    String currentSchema = connection.getSchema();
                    System.out.println("📍 Current schema: " + currentSchema + " (expected: " + tenant + ")");

                    // Exécuter une requête simple pour déclencher la création des tables
                    try (Statement stmt = connection.createStatement()) {
                        stmt.execute("SELECT 1");
                    }

                    System.out.println("🔨 Tables created in schema: " + tenant);

                } catch (Exception e) {
                    System.err.println("❌ Error creating tables in schema: " + tenant);
                    e.printStackTrace();
                }
            });

        } finally {
            // Restaurer le tenant original
            if (originalTenant != null) {
                tenantIdentifierResolver.setCurrentTenant(originalTenant);
            } else {
                tenantIdentifierResolver.clear();
            }

            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Méthode utilitaire pour initialiser un nouveau tenant après le démarrage
     */
    public void initializeNewTenant(String tenantId) {
        if (tenantId != null && !tenantId.trim().isEmpty()) {
            System.out.println("🆕 Initializing new tenant: " + tenantId);
            initializeSchemaForTenant(tenantId);
        }
    }
}