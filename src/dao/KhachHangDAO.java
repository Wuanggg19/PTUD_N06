package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.KhachHang;

public class KhachHangDAO {

    public List<KhachHang> getAllKhachHang() {
        List<KhachHang> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM KhachHang ORDER BY maKhachHang");
            while (rs.next())
                ds.add(mapRow(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public KhachHang getKhachHangBySdt(String sdt) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT * FROM KhachHang WHERE soDienThoai = ?");
            stmt.setString(1, sdt);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public KhachHang getKhachHangByMa(String ma) {
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(
                    "SELECT * FROM KhachHang WHERE maKhachHang = ?");
            stmt.setString(1, ma);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return mapRow(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(KhachHang kh) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO KhachHang (maKhachHang, tenKhachHang, diaChi, gioiTinh, soDienThoai) " +
                            "VALUES (?, ?, ?, ?, ?)");
            stmt.setString(1, kh.getMaKhachHang());
            stmt.setString(2, kh.getTenKhachHang());
            stmt.setString(3, kh.getDiaChi());
            stmt.setBoolean(4, kh.isGioiTinh());
            stmt.setString(5, kh.getSoDienThoai());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean update(KhachHang kh) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(
                    "UPDATE KhachHang " +
                            "SET tenKhachHang = ?, diaChi = ?, gioiTinh = ?, soDienThoai = ? " +
                            "WHERE maKhachHang = ?");
            stmt.setString(1, kh.getTenKhachHang());
            stmt.setString(2, kh.getDiaChi());
            stmt.setBoolean(3, kh.isGioiTinh());
            stmt.setString(4, kh.getSoDienThoai());
            stmt.setString(5, kh.getMaKhachHang());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean delete(String maKhachHang) {
        int n = 0;
        try {
            Connection con = ConnectDB.getConnection();
            PreparedStatement stmt = con.prepareStatement(
                    "DELETE FROM KhachHang WHERE maKhachHang = ?");
            stmt.setString(1, maKhachHang);
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public String generateNextId() {
        try {
            Connection con = ConnectDB.getConnection();
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "SELECT MAX(CAST(SUBSTRING(maKhachHang, 3, LEN(maKhachHang)) AS INT)) FROM KhachHang");
            if (rs.next())
                return String.format("KH%03d", rs.getInt(1) + 1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "KH001";
    }

    private KhachHang mapRow(ResultSet rs) throws SQLException {
        return new KhachHang(
                rs.getString("maKhachHang"),
                rs.getString("tenKhachHang"),
                rs.getString("diaChi"),
                rs.getBoolean("gioiTinh"),
                rs.getString("soDienThoai"));
    }
}
