import connectDB.ConnectDB;

public class TestDB {
    public static void main(String[] args) {
        System.out.println("Dang thu ket noi den database...");
        try {
            java.sql.Connection con = ConnectDB.getConnection();
            if (con == null || con.isClosed()) {
                System.out.println("Ket noi THAT BAI!");
                return;
            }
            System.out.println("Ket noi thanh cong!");
            con.close();
        } catch (java.sql.SQLException e) {
            System.out.println("Ket noi THAT BAI!");
            e.printStackTrace();
        }
    }
}
