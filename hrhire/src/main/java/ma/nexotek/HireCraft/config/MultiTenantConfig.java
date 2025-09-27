package ma.nexotek.HireCraft.config;

import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class MultiTenantConfig {

    @Autowired
    private DataSource dataSource;

    @Bean
    public JpaVendorAdapter jpaVendorAdapter() {
        return new HibernateJpaVendorAdapter();
    }

    @Bean
    @Primary
    public MultiTenantConnectionProvider<String> multiTenantConnectionProvider() {
        return new DataSourceBasedMultiTenantConnectionProviderImpl();
    }

    @Bean
    @Primary
    public TenantIdentifierResolver tenantIdentifierResolver() {
        return new TenantIdentifierResolver();
    }

    @Bean
    public CurrentTenantIdentifierResolver currentTenantIdentifierResolver(TenantIdentifierResolver tenantIdentifierResolver) {
        return tenantIdentifierResolver;
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource,
            MultiTenantConnectionProvider<String> multiTenantConnectionProvider,
            CurrentTenantIdentifierResolver currentTenantIdentifierResolver) {

        LocalContainerEntityManagerFactoryBean emfBean = new LocalContainerEntityManagerFactoryBean();
        emfBean.setDataSource(dataSource);
        emfBean.setPackagesToScan("ma.nexotek.HireCraft");
        emfBean.setJpaVendorAdapter(jpaVendorAdapter());

        Map<String, Object> properties = new HashMap<>();

        // Configuration multi-tenancy
        properties.put("hibernate.multiTenancy", "SCHEMA");
        properties.put("hibernate.multi_tenant_connection_provider", multiTenantConnectionProvider);
        properties.put("hibernate.tenant_identifier_resolver", currentTenantIdentifierResolver);

        // Configuration base de données
        properties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        properties.put("hibernate.show_sql", true);
        properties.put("hibernate.format_sql", true);

        // Configuration DDL - Plus agressive pour la création des tables
        properties.put("hibernate.hbm2ddl.auto", "update");
        properties.put("hibernate.hbm2ddl.create_schemas", true);
        properties.put("hibernate.hbm2ddl.create_namespaces", true);

        // Nouvelles propriétés pour forcer la création dans tous les schémas
        properties.put("hibernate.hbm2ddl.schema_generation_script_append", false);
        properties.put("hibernate.hbm2ddl.delimiter", ";");

        // Configuration pour ignorer les erreurs de schéma manquant
        properties.put("hibernate.check_schema", false);

        // Configuration pour éviter les problèmes avec les schémas par défaut
        properties.put("hibernate.default_schema", "");

        // Configuration pour la gestion des LOBs PostgreSQL
        properties.put("hibernate.jdbc.lob.non_contextual_creation", true);

        // Configuration pour améliorer les performances
        properties.put("hibernate.connection.provider_disables_autocommit", false);
        properties.put("hibernate.cache.use_second_level_cache", false);
        properties.put("hibernate.cache.use_query_cache", false);

        // Configuration pour le logging détaillé
        properties.put("hibernate.generate_statistics", false);
        properties.put("hibernate.use_sql_comments", true);

        // Configuration pour les transactions
        properties.put("hibernate.connection.autocommit", false);
        properties.put("hibernate.connection.isolation", "2"); // READ_COMMITTED

        emfBean.setJpaPropertyMap(properties);
        return emfBean;
    }
}