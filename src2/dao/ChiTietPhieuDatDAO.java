package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.ChiTietPhieuDat;
import entity.PhieuDatPhong;
import entity.Phong;

public class ChiTietPhieuDatDAO {

    public List<ChiTietPhieuDat> getDSChiTietByMaPhieu(String maPhieu) {
        List<ChiTietPhieuDat> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM ChiTietPhieuDat WHERE maDatPhong = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPhieu);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ChiTietPhieuDat ct = new ChiTietPhieuDat(
                    new PhieuDatPhong(rs.getString("maDatPhong")),
                    new Phong(rs.getString("maPhong")),
                    rs.getDouble("giaThuePhong"),
                    rs.getTimestamp("ngayNhan").toLocalDateTime(),
                    rs.getTimestamp("ngayTra").toLocalDateTime()
                );
                ds.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean create(ChiTietPhieuDat ct) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            stmt = con.prepareStatement("INSERT INTO ChiTietPhieuDat VALUES(?, ?, ?, ?, ?)");
            stmt.setString(1, ct.getPhieuDatPhong().getMaDatPhong());
            stmt.setString(2, ct.getPhong().getMaPhong());
            stmt.setDouble(3, ct.getGiaThuePhong());
            stmt.setTimestamp(4, Timestamp.valueOf(ct.getNgayNhan()));
            stmt.setTimestamp(5, Timestamp.valueOf(ct.getNgayTra()));
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
