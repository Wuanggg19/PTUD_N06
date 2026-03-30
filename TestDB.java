public class TestDB {
    public static void main(String[] args) {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyKhachSan_N06_v5;encrypt=true;trustServerCertificate=true;";
        String user = "sa";        
        String password = "123456"; 
        
        System.out.println("Dang thu ket noi den database...");
        try {
            java.sql.Connection con = java.sql.DriverManager.getConnection(url, user, password);
            System.out.println("Ket noi thanh cong!");
            con.close();
        } catch (java.sql.SQLException e) {
            System.out.println("Ket noi THAT BAI!");
            e.printStackTrace();
        }
    }
}
