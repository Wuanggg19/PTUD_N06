package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.ChiTietHoaDon;
import entity.DichVu;
import entity.HoaDon;

public class ChiTietHoaDonDAO {
    public List<ChiTietHoaDon> getByMaHoaDon(String maHD) {
        return getDSChiTietByMaHD(maHD);
    }

    public List<ChiTietHoaDon> getDSChiTietByMaHD(String maHD) {
        List<ChiTietHoaDon> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM ChiTietHoaDon WHERE maHoaDon = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maHD);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChiTietHoaDon ct = new ChiTietHoaDon(
                    new HoaDon(rs.getString("maHoaDon")),
                    new DichVu(rs.getString("maDichVu")),
                    rs.getInt("soLuong"),
                    rs.getDouble("donGiaLuuTru")
                );
                ds.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean addOrUpdateService(ChiTietHoaDon ct) {
        String sqlCheck = "SELECT soLuong FROM ChiTietHoaDon WHERE maHoaDon = ? AND maDichVu = ?";
        String sqlUpdate = "UPDATE ChiTietHoaDon SET soLuong = soLuong + ? WHERE maHoaDon = ? AND maDichVu = ?";
        String sqlInsert = "INSERT INTO ChiTietHoaDon(maHoaDon, maDichVu, soLuong, donGiaLuuTru) VALUES(?, ?, ?, ?)";
        
        try (Connection con = ConnectDB.getConnection()) {
            try (PreparedStatement psCheck = con.prepareStatement(sqlCheck)) {
                psCheck.setString(1, ct.getHoaDon().getMaHoaDon());
                psCheck.setString(2, ct.getDichVu().getMaDichVu());
                ResultSet rs = psCheck.executeQuery();
                
                if (rs.next()) {
                    try (PreparedStatement psUpdate = con.prepareStatement(sqlUpdate)) {
                        psUpdate.setInt(1, ct.getSoLuong());
                        psUpdate.setString(2, ct.getHoaDon().getMaHoaDon());
                        psUpdate.setString(3, ct.getDichVu().getMaDichVu());
                        return psUpdate.executeUpdate() > 0;
                    }
                } else {
                    try (PreparedStatement psInsert = con.prepareStatement(sqlInsert)) {
                        psInsert.setString(1, ct.getHoaDon().getMaHoaDon());
                        psInsert.setString(2, ct.getDichVu().getMaDichVu());
                        psInsert.setInt(3, ct.getSoLuong());
                        psInsert.setDouble(4, ct.getDonGiaLuuTru());
                        return psInsert.executeUpdate() > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean create(ChiTietHoaDon ct) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            stmt = con.prepareStatement("INSERT INTO ChiTietHoaDon VALUES(?, ?, ?, ?)");
            stmt.setString(1, ct.getHoaDon().getMaHoaDon());
            stmt.setString(2, ct.getDichVu().getMaDichVu());
            stmt.setInt(3, ct.getSoLuong());
            stmt.setDouble(4, ct.getDonGiaLuuTru());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
