package connectDB;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Quản lý kết nối CSDL SQL Server.
 *
 * Cấu hình được đọc từ file "db.properties" ở thư mục gốc dự án.
 * Nếu không tìm thấy file, sẽ dùng giá trị mặc định.
 *
 * ============================================================
 *  HƯỚNG DẪN THIẾT LẬP CHO TỪNG THÀNH VIÊN NHÓM:
 *  1. Mở file "db.properties" ở thư mục gốc dự án
 *  2. Sửa db.password thành mật khẩu SQL Server trên máy bạn
 *  3. Lưu lại và chạy chương trình
 * ============================================================
 */
public class ConnectDB {

    public static Connection con = null;
    private static ConnectDB instance = new ConnectDB();

    // Giá trị mặc định (fallback nếu không đọc được file)
    private static String DB_URL      = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyKhachSan_N06_v5;encrypt=true;trustServerCertificate=true";
    private static String DB_USER     = "sa";
    private static String DB_PASSWORD = "123456";

    static {
        loadConfig();
    }

    /**
     * Đọc cấu hình từ file db.properties.
     * File db.properties nằm ở thư mục gốc của project (cùng cấp với src/).
     */
    private static void loadConfig() {
        Properties props = new Properties();

        // Thử load từ thư mục làm việc hiện tại (khi chạy từ IDE)
        try (InputStream input = new FileInputStream("db.properties")) {
            props.load(input);
            applyProps(props);
            System.out.println("[DB Config] Đã đọc cấu hình từ db.properties");
            return;
        } catch (IOException ignored) {
            // Thử tiếp từ classpath
        }

        // Thử load từ classpath (khi đóng gói thành jar)
        try (InputStream input = ConnectDB.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input != null) {
                props.load(input);
                applyProps(props);
                System.out.println("[DB Config] Đã đọc cấu hình từ classpath/db.properties");
                return;
            }
        } catch (IOException ignored) {
            // Bỏ qua
        }

        System.out.println("[DB Config] Không tìm thấy db.properties. Dùng cấu hình mặc định.");
        System.out.println("[DB Config] URL=" + DB_URL + " | USER=" + DB_USER);
    }

    private static void applyProps(Properties props) {
        if (props.containsKey("db.url"))      DB_URL      = props.getProperty("db.url").trim();
        if (props.containsKey("db.user"))     DB_USER     = props.getProperty("db.user").trim();
        if (props.containsKey("db.password")) DB_PASSWORD = props.getProperty("db.password").trim();
    }

    public static ConnectDB getInstance() {
        return instance;
    }

    public void connect() throws SQLException {
        con = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        System.out.println("[DB] Kết nối thành công tới: " + DB_URL);
    }

    public void disconnect() {
        if (con != null) {
            try {
                con.close();
                con = null;
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
            System.err.println("[DB ERROR] Không thể kết nối CSDL: " + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }
}
