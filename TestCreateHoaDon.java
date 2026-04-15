
import connectDB.ConnectDB;
import dao.HoaDonDAO;
import entity.HoaDon;
import entity.PhieuDatPhong;
import entity.NhanVien;
import java.time.LocalDateTime;

public class TestCreateHoaDon {
    public static void main(String[] args) {
        try {
            ConnectDB.connect(); 
            System.out.println("Connected to DB.");
            
            HoaDon hd = new HoaDon(
                "HD999999",
                LocalDateTime.now(),
                0.08,
                100000,
                0,
                new PhieuDatPhong("DP0001"),
                new NhanVien("NV001")
            );
            
            HoaDonDAO hdDao = new HoaDonDAO();
            boolean res = hdDao.create(hd);
            System.out.println("Result: " + res);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

