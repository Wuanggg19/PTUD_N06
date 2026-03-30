package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.NhanVien;

public class NhanVienDAO {

    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                NhanVien nv = new NhanVien(
                    rs.getString("maNhanVien"),
                    rs.getString("tenNhanVien"),
                    rs.getString("chucVu"),
                    rs.getDouble("luongNhanVien"),
                    rs.getBoolean("phaiNhanVien"),
                    rs.getString("soDienThoai"),
                    rs.getString("tenDangNhap"),
                    rs.getString("matKhau"),
                    rs.getString("vaiTro")
                );
                ds.add(nv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public NhanVien getNhanVienByAccount(String user, String pass) {
        NhanVien nv = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien WHERE tenDangNhap = ? AND matKhau = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, user);
            stmt.setString(2, pass);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                nv = new NhanVien(
                    rs.getString("maNhanVien"),
                    rs.getString("tenNhanVien"),
                    rs.getString("chucVu"),
                    rs.getDouble("luongNhanVien"),
                    rs.getBoolean("phaiNhanVien"),
                    rs.getString("soDienThoai"),
                    rs.getString("tenDangNhap"),
                    rs.getString("matKhau"),
                    rs.getString("vaiTro")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nv;
    }

    // Lấy nhân viên theo tài khoản để phân biệt "sai tài khoản" và "sai mật khẩu"
    public NhanVien getNhanVienByUsername(String user) {
        NhanVien nv = null;
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM NhanVien WHERE tenDangNhap = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, user);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                nv = new NhanVien(
                    rs.getString("maNhanVien"),
                    rs.getString("tenNhanVien"),
                    rs.getString("chucVu"),
                    rs.getDouble("luongNhanVien"),
                    rs.getBoolean("phaiNhanVien"),
                    rs.getString("soDienThoai"),
                    rs.getString("tenDangNhap"),
                    rs.getString("matKhau"),
                    rs.getString("vaiTro")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nv;
    }
}
