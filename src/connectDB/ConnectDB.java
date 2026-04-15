package connectDB;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectDB {
    private static final int LOGIN_TIMEOUT_SECONDS = 5;
    private static final String DEFAULT_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=QuanLyKhachSan;encrypt=false";
    private static final String LEGACY_URL =
            "jdbc:sqlserver://localhost:1433;databaseName=QuanLyKhachSan_N06_v5;encrypt=true;trustServerCertificate=true";
    private static final String DEFAULT_USER = "sa";
    private static final String DEFAULT_PASSWORD = "sapassword";

    public static Connection con = null;
    private static final ConnectDB instance = new ConnectDB();
    private final Properties dbProperties = loadDbProperties();
    private String activeUrl = DEFAULT_URL;

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() throws SQLException {
        String configuredUrl = getSetting("HOTEL_DB_URL", "db.url", DEFAULT_URL);
        String user = getSetting("HOTEL_DB_USER", "db.user", DEFAULT_USER);
        String password = getSetting("HOTEL_DB_PASSWORD", "db.password", DEFAULT_PASSWORD);

        if (con != null) {
            try {
                if (!con.isClosed()) {
                    return;
                }
            } catch (SQLException e) {
                con = null;
            }
        }

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Khong tim thay SQL Server JDBC driver tren classpath.", e);
        }

        DriverManager.setLoginTimeout(LOGIN_TIMEOUT_SECONDS);

        SQLException lastException = null;
        for (String candidateUrl : buildCandidateUrls(configuredUrl)) {
            try {
                con = DriverManager.getConnection(candidateUrl, user, password);
                activeUrl = candidateUrl;
                return;
            } catch (SQLException e) {
                lastException = e;
            }
        }

        throw new SQLException("Khong the ket noi den bat ky database cau hinh nao.", lastException);
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static synchronized Connection getConnection() {
        try {
            if (con == null || con.isClosed()) {
                instance.connect();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return con;
    }

    public String getConfiguredUrl() {
        return getSetting("HOTEL_DB_URL", "db.url", DEFAULT_URL);
    }

    public String getActiveUrl() {
        return activeUrl;
    }

    private Properties loadDbProperties() {
        Properties properties = new Properties();

        if (loadFromClasspath(properties)) {
            return properties;
        }
        if (loadFromFile(properties, Paths.get("db.properties"))) {
            return properties;
        }
        if (loadFromFile(properties, Paths.get("..", "db.properties"))) {
            return properties;
        }

        properties.setProperty("db.url", DEFAULT_URL);
        properties.setProperty("db.user", DEFAULT_USER);
        properties.setProperty("db.password", DEFAULT_PASSWORD);
        return properties;
    }

    private String[] buildCandidateUrls(String configuredUrl) {
        if (configuredUrl == null || configuredUrl.isBlank()) {
            return new String[] { DEFAULT_URL, LEGACY_URL };
        }
        if (LEGACY_URL.equals(configuredUrl)) {
            return new String[] { LEGACY_URL, DEFAULT_URL };
        }
        if (DEFAULT_URL.equals(configuredUrl)) {
            return new String[] { DEFAULT_URL, LEGACY_URL };
        }
        return new String[] { configuredUrl, DEFAULT_URL, LEGACY_URL };
    }

    private String getSetting(String envName, String propertyName, String defaultValue) {
        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.isBlank()) {
            return envValue;
        }
        return dbProperties.getProperty(propertyName, defaultValue);
    }

    private boolean loadFromClasspath(Properties properties) {
        try (InputStream inputStream = ConnectDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                return false;
            }
            properties.load(inputStream);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean loadFromFile(Properties properties, Path path) {
        Path normalizedPath = path.toAbsolutePath().normalize();
        if (!Files.exists(normalizedPath)) {
            return false;
        }

        try (InputStream inputStream = Files.newInputStream(normalizedPath)) {
            properties.load(inputStream);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
