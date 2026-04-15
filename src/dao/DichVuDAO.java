package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.DichVu;

public class DichVuDAO {

    public List<DichVu> getAllDichVu() {
        List<DichVu> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM DichVu";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                DichVu dv = new DichVu(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getDouble(3),
                        rs.getString(4));
                ds.add(dv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public boolean create(DichVu dv) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            stmt = con.prepareStatement("INSERT INTO DichVu VALUES(?, ?, ?, ?)");
            stmt.setString(1, dv.getMaDichVu());
            stmt.setString(2, dv.getTenDichVu());
            stmt.setDouble(3, dv.getDonGia());
            stmt.setString(4, dv.getTrangThai());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
