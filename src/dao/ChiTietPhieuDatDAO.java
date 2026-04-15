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
import util.StatusUtils;

public class ChiTietPhieuDatDAO {

    public List<ChiTietPhieuDat> getDSChiTietByMaPhieu(String maPhieu) {
        List<ChiTietPhieuDat> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            // JOIN với bảng Phong để lấy loaiPhong và giaPhong phục vụ tính toán
            String sql = """
                    SELECT ct.*, p.loaiPhong, p.giaPhong, p.trangThai as ttPhong, p.soGiuong, p.sucChua
                    FROM ChiTietPhieuDat ct
                    JOIN Phong p ON ct.maPhong = p.maPhong
                    WHERE ct.maDatPhong = ?
                    """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPhieu);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Phong phong = new Phong(
                    rs.getString("maPhong"),
                    rs.getString("loaiPhong"),
                    rs.getInt("soGiuong"),
                    1, // tang - tạm thời để 1
                    rs.getString("ttPhong"),
                    rs.getDouble("giaPhong")
                );
                
                ChiTietPhieuDat ct = new ChiTietPhieuDat(
                    new PhieuDatPhong(rs.getString("maDatPhong")),
                    phong,
                    rs.getDouble("giaThuePhong"),
                    rs.getTimestamp("ngayNhan") != null ? rs.getTimestamp("ngayNhan").toLocalDateTime() : null,
                    rs.getTimestamp("ngayTra") != null ? rs.getTimestamp("ngayTra").toLocalDateTime() : null
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

    public List<ChiTietPhieuDat> getChiTietByRoom(String maPhong) {
        List<ChiTietPhieuDat> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = """
                    SELECT ct.*, pdp.trangThai as ttPhieu, p.loaiPhong, p.giaPhong
                    FROM ChiTietPhieuDat ct
                    JOIN PhieuDatPhong pdp ON pdp.maDatPhong = ct.maDatPhong
                    JOIN Phong p ON ct.maPhong = p.maPhong
                    WHERE ct.maPhong = ?
                    """;
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPhong);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                PhieuDatPhong pdp = new PhieuDatPhong(rs.getString("maDatPhong"));
                pdp.setTrangThai(StatusUtils.bookingCode(rs.getString("ttPhieu")));
                
                Phong phong = new Phong(rs.getString("maPhong"));
                phong.setLoaiPhong(rs.getString("loaiPhong"));
                phong.setGiaPhong(rs.getDouble("giaPhong"));

                ChiTietPhieuDat ct = new ChiTietPhieuDat(
                        pdp,
                        phong,
                        rs.getDouble("giaThuePhong"),
                        rs.getTimestamp("ngayNhan") != null ? rs.getTimestamp("ngayNhan").toLocalDateTime() : null,
                        rs.getTimestamp("ngayTra") != null ? rs.getTimestamp("ngayTra").toLocalDateTime() : null
                );
                ds.add(ct);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }
}
