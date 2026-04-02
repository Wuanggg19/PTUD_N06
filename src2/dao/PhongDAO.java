package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.Phong;

public class PhongDAO {

    public List<Phong> filterPhong(String trangThai, String loaiPhong) {
        List<Phong> ds = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection()) {
            String sql = "SELECT * FROM Phong WHERE 1=1";
            if (trangThai != null && !trangThai.isEmpty()) sql += " AND trangThai = ?";
            if (loaiPhong != null && !loaiPhong.isEmpty()) sql += " AND loaiPhong = ?";
            
            PreparedStatement stmt = con.prepareStatement(sql);
            int idx = 1;
            if (trangThai != null && !trangThai.isEmpty()) stmt.setString(idx++, trangThai);
            if (loaiPhong != null && !loaiPhong.isEmpty()) stmt.setString(idx++, loaiPhong);
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(new Phong(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getDouble(5)));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<Phong> getAllPhong() {
        List<Phong> dsPhong = new ArrayList<>();
        try {
            Connection con = ConnectDB.getInstance().getConnection();
            String sql = "SELECT * FROM Phong";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String ma = rs.getString(1);
                String loai = rs.getString(2);
                int soGiuong = rs.getInt(3);
                String trangThai = rs.getString(4);
                double gia = rs.getDouble(5);
                Phong p = new Phong(ma, loai, soGiuong, trangThai, gia);
                dsPhong.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsPhong;
    }

    public boolean create(Phong p) {
        Connection con = ConnectDB.getInstance().getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            stmt = con.prepareStatement("INSERT INTO Phong VALUES(?, ?, ?, ?, ?)");
            stmt.setString(1, p.getMaPhong());
            stmt.setString(2, p.getLoaiPhong());
            stmt.setInt(3, p.getSoGiuong());
            stmt.setString(4, p.getTrangThai());
            stmt.setDouble(5, p.getGiaPhong());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public boolean update(Phong p) {
        Connection con = ConnectDB.getInstance().getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            stmt = con.prepareStatement("UPDATE Phong SET loaiPhong=?, soGiuong=?, trangThai=?, giaPhong=? WHERE maPhong=?");
            stmt.setString(1, p.getLoaiPhong());
            stmt.setInt(2, p.getSoGiuong());
            stmt.setString(3, p.getTrangThai());
            stmt.setDouble(4, p.getGiaPhong());
            stmt.setString(5, p.getMaPhong());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }

    public Phong getPhongByMa(String ma) {
        try (Connection con = ConnectDB.getConnection()) {
            PreparedStatement stmt = con.prepareStatement("SELECT * FROM Phong WHERE maPhong = ?");
            stmt.setString(1, ma);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Phong(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getString(4), rs.getDouble(5));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete(String maPhong) {
        try (Connection con = ConnectDB.getConnection()) {
            PreparedStatement stmt = con.prepareStatement("DELETE FROM Phong WHERE maPhong = ?");
            stmt.setString(1, maPhong);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
