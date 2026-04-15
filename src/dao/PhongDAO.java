package dao;

import connectDB.ConnectDB;
import entity.Phong;
import util.StatusUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PhongDAO {

    public List<Phong> filterPhong(String trangThai, String loaiPhong) {
        List<Phong> ds = new ArrayList<>();
        String normalizedStatus = trangThai != null && !trangThai.isBlank() ? StatusUtils.roomCode(trangThai) : null;

        try (Connection con = ConnectDB.getConnection()) {
            StringBuilder sql = new StringBuilder("SELECT * FROM Phong WHERE 1=1");
            if (normalizedStatus != null) {
                sql.append(" AND trangThai = ?");
            }
            if (loaiPhong != null && !loaiPhong.isBlank()) {
                sql.append(" AND loaiPhong = ?");
            }

            PreparedStatement stmt = con.prepareStatement(sql.toString());
            int idx = 1;
            if (normalizedStatus != null) {
                stmt.setString(idx++, normalizedStatus);
            }
            if (loaiPhong != null && !loaiPhong.isBlank()) {
                stmt.setString(idx, loaiPhong);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(mapPhong(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<Phong> getAllPhong() {
        List<Phong> dsPhong = new ArrayList<>();
        try (Connection con = ConnectDB.getInstance().getConnection();
             Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM Phong")) {
            while (rs.next()) {
                dsPhong.add(mapPhong(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsPhong;
    }

    public boolean create(Phong p) {
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(
                    "INSERT INTO Phong(maPhong, loaiPhong, soNguoi, tang, trangThai, giaPhong) VALUES(?, ?, ?, ?, ?, ?)")) {
                fillWriteStatement(stmt, p, true);
                return stmt.executeUpdate() > 0;
            } catch (SQLException ex) {
                try (PreparedStatement legacyStmt = con.prepareStatement(
                        "INSERT INTO Phong(maPhong, loaiPhong, soGiuong, trangThai, giaPhong) VALUES(?, ?, ?, ?, ?)")) {
                    fillWriteStatement(legacyStmt, p, false);
                    return legacyStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Phong p) {
        try (Connection con = ConnectDB.getInstance().getConnection()) {
            try (PreparedStatement stmt = con.prepareStatement(
                    "UPDATE Phong SET loaiPhong=?, soNguoi=?, tang=?, trangThai=?, giaPhong=? WHERE maPhong=?")) {
                stmt.setString(1, p.getLoaiPhong());
                stmt.setInt(2, p.getSoNguoi());
                stmt.setInt(3, p.getTang());
                stmt.setString(4, StatusUtils.roomCode(p.getTrangThai()));
                stmt.setDouble(5, p.getGiaPhong());
                stmt.setString(6, p.getMaPhong());
                return stmt.executeUpdate() > 0;
            } catch (SQLException ex) {
                try (PreparedStatement legacyStmt = con.prepareStatement(
                        "UPDATE Phong SET loaiPhong=?, soGiuong=?, trangThai=?, giaPhong=? WHERE maPhong=?")) {
                    legacyStmt.setString(1, p.getLoaiPhong());
                    legacyStmt.setInt(2, p.getSoNguoi());
                    legacyStmt.setString(3, StatusUtils.roomCode(p.getTrangThai()));
                    legacyStmt.setDouble(4, p.getGiaPhong());
                    legacyStmt.setString(5, p.getMaPhong());
                    return legacyStmt.executeUpdate() > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Phong getPhongByMa(String ma) {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement("SELECT * FROM Phong WHERE maPhong = ?")) {
            stmt.setString(1, ma);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapPhong(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean delete(String maPhong) {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM Phong WHERE maPhong = ?")) {
            stmt.setString(1, maPhong);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Phong> getPhongTrongTheoThoiGian(java.time.LocalDateTime checkIn, java.time.LocalDateTime checkOut) {
        List<Phong> ds = new ArrayList<>();
        String sql = """
                SELECT * FROM Phong p
                WHERE p.trangThai <> 'SUA_CHUA'
                AND p.maPhong NOT IN (
                    SELECT ct.maPhong FROM ChiTietPhieuDat ct
                    JOIN PhieuDatPhong pdp ON ct.maDatPhong = pdp.maDatPhong
                    WHERE pdp.trangThai NOT IN ('DA_HUY', 'DA_THANH_TOAN')
                    AND NOT (ct.ngayTra <= ? OR ct.ngayNhan >= ?)
                )
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(checkIn));
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(checkOut));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ds.add(mapPhong(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<Phong> findAvailableRooms(java.time.LocalDateTime checkIn, java.time.LocalDateTime checkOut) {
        return getPhongTrongTheoThoiGian(checkIn, checkOut);
    }

    private void fillWriteStatement(PreparedStatement stmt, Phong p, boolean withTang) throws SQLException {
        stmt.setString(1, p.getMaPhong());
        stmt.setString(2, p.getLoaiPhong());
        stmt.setInt(3, p.getSoNguoi());
        if (withTang) {
            stmt.setInt(4, p.getTang());
            stmt.setString(5, StatusUtils.roomCode(p.getTrangThai()));
            stmt.setDouble(6, p.getGiaPhong());
        } else {
            stmt.setString(4, StatusUtils.roomCode(p.getTrangThai()));
            stmt.setDouble(5, p.getGiaPhong());
        }
    }

    private Phong mapPhong(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        boolean hasSoNguoi = hasColumn(meta, "soNguoi");
        boolean hasTang = hasColumn(meta, "tang");

        String ma = rs.getString("maPhong");
        String loai = rs.getString("loaiPhong");
        int soNguoi = hasSoNguoi ? rs.getInt("soNguoi") : rs.getInt("soGiuong");
        int tang = hasTang ? rs.getInt("tang") : inferTang(ma);
        String trangThai = StatusUtils.roomCode(rs.getString("trangThai"));
        double gia = rs.getDouble("giaPhong");

        return new Phong(ma, loai, soNguoi, tang, trangThai, gia);
    }

    private boolean hasColumn(ResultSetMetaData meta, String columnName) throws SQLException {
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(meta.getColumnLabel(i)) || columnName.equalsIgnoreCase(meta.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    private int inferTang(String maPhong) {
        if (maPhong == null || maPhong.length() < 2) {
            return 1;
        }
        String digits = maPhong.replaceAll("\\D+", "");
        if (digits.length() < 2) {
            return 1;
        }
        try {
            return Integer.parseInt(digits.substring(0, digits.length() - 2));
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
