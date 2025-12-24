package com.mrh.buscharter.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Konfigurasi database dengan HikariCP connection pool dan Hibernate/JPA.
 * Membaca konfigurasi dari application.properties.
 */
public class DatabaseConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static EntityManagerFactory entityManagerFactory;
    private static HikariDataSource dataSource;
    
    private DatabaseConfig() {
        // Private constructor untuk singleton pattern
    }
    
    /**
     * Inisialisasi koneksi database.
     * Harus dipanggil sekali saat aplikasi startup.
     */
    public static void initialize() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            logger.warn("Database sudah diinisialisasi");
            return;
        }
        
        try {
            Properties props = loadProperties();
            setupDataSource(props);
            setupEntityManagerFactory(props);
            logger.info("Database berhasil diinisialisasi");
        } catch (Exception e) {
            logger.error("Gagal inisialisasi database", e);
            throw new RuntimeException("Gagal inisialisasi database: " + e.getMessage(), e);
        }
    }
    
    /**
     * Load properties dari file application.properties.
     */
    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();
        
        // Coba load dari classpath dulu
        try (InputStream is = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
                return props;
            }
        }
        
        // Jika tidak ada, coba load dari file system
        try (FileInputStream fis = new FileInputStream("application.properties")) {
            props.load(fis);
            return props;
        } catch (IOException e) {
            // Gunakan default values
            logger.warn("application.properties tidak ditemukan, menggunakan default values");
            props.setProperty("db.url", "jdbc:postgresql://localhost:5432/mrh_buscharter");
            props.setProperty("db.username", "mrh_user");
            props.setProperty("db.password", "mrh_password_2024");
            props.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
            props.setProperty("hibernate.show_sql", "false");
            props.setProperty("hibernate.hbm2ddl.auto", "validate");
            props.setProperty("hikari.maximum-pool-size", "10");
            props.setProperty("hikari.minimum-idle", "2");
            props.setProperty("hikari.idle-timeout", "30000");
            return props;
        }
    }
    
    /**
     * Setup HikariCP DataSource.
     */
    private static void setupDataSource(Properties props) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getProperty("db.url"));
        config.setUsername(props.getProperty("db.username"));
        config.setPassword(props.getProperty("db.password"));
        config.setDriverClassName("org.postgresql.Driver");
        
        // Pool settings
        config.setMaximumPoolSize(Integer.parseInt(
            props.getProperty("hikari.maximum-pool-size", "10")));
        config.setMinimumIdle(Integer.parseInt(
            props.getProperty("hikari.minimum-idle", "2")));
        config.setIdleTimeout(Long.parseLong(
            props.getProperty("hikari.idle-timeout", "30000")));
        config.setConnectionTimeout(30000);
        config.setPoolName("MRH-HikariPool");
        
        dataSource = new HikariDataSource(config);
        logger.info("HikariCP DataSource berhasil dibuat");
    }
    
    /**
     * Setup Hibernate EntityManagerFactory.
     */
    private static void setupEntityManagerFactory(Properties props) {
        Map<String, Object> jpaProps = new HashMap<>();
        jpaProps.put("hibernate.dialect", props.getProperty("hibernate.dialect"));
        jpaProps.put("hibernate.show_sql", props.getProperty("hibernate.show_sql", "false"));
        jpaProps.put("hibernate.format_sql", "true");
        jpaProps.put("hibernate.hbm2ddl.auto", props.getProperty("hibernate.hbm2ddl.auto", "validate"));
        jpaProps.put("hibernate.connection.datasource", dataSource);
        
        entityManagerFactory = Persistence.createEntityManagerFactory("mrh-buscharter", jpaProps);
        logger.info("EntityManagerFactory berhasil dibuat");
    }
    
    /**
     * Mendapatkan EntityManager baru.
     * Caller bertanggung jawab untuk menutup EntityManager setelah selesai.
     */
    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            throw new IllegalStateException("Database belum diinisialisasi. Panggil initialize() terlebih dahulu.");
        }
        return entityManagerFactory.createEntityManager();
    }
    
    /**
     * Mendapatkan DataSource untuk keperluan khusus.
     */
    public static HikariDataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Menutup semua koneksi database.
     * Dipanggil saat aplikasi shutdown.
     */
    public static void shutdown() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
            logger.info("EntityManagerFactory ditutup");
        }
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("DataSource ditutup");
        }
    }
}
