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
