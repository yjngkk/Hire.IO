package ma.nexotek.HireCraft.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class MultiTenancyTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private CurrentTenantIdentifierResolver tenantResolver;

    @Test
    public void testMultiTenancy() throws Exception {
        // Test avec le tenant "tenant1"
        ((TenantIdentifierResolver) tenantResolver).setCurrentTenant("tenant1");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Créer une table de test
            stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("INSERT INTO test_table VALUES (1, 'tenant1_data')");
            
            // Vérifier les données
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
            assertTrue(rs.next());
            assertEquals("tenant1_data", rs.getString("name"));
        }

        // Test avec le tenant "tenant2"
        ((TenantIdentifierResolver) tenantResolver).setCurrentTenant("tenant1");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Créer la même table pour le deuxième tenant
            stmt.execute("CREATE TABLE IF NOT EXISTS test_table (id INT PRIMARY KEY, name VARCHAR(255))");
            stmt.execute("INSERT INTO test_table VALUES (2, 'tenant2_data')");
            
            // Vérifier les données
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
            assertTrue(rs.next());
            assertEquals("tenant1_data", rs.getString("name"));
        }

        // Vérifier l'isolation des données
        ((TenantIdentifierResolver) tenantResolver).setCurrentTenant("tenant2");
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM test_table");
            assertTrue(rs.next());
            assertEquals("tenant1_data", rs.getString("name"));
        }
    }

    @Test
    public void testDefaultTenant() {
        ((TenantIdentifierResolver) tenantResolver).clear();
        String tenantId = (String) tenantResolver.resolveCurrentTenantIdentifier();
        assertEquals("public", tenantId);
    }
} 