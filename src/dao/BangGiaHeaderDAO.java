package dao;

import connectDB.ConnectDB;
import entity.BangGiaHeader;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BangGiaHeaderDAO {

    public List<BangGiaHeader> getAllBangGiaHeader() {
        List<BangGiaHeader> ds = new ArrayList<>();
        String sql = "SELECT * FROM BangGiaHeader ORDER BY maBangGia";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ds.add(mapHeader(rs));
            }
        } catch (Exception e) {
            throw new RuntimeException("Khong the tai du lieu BangGiaHeader.", e);
        }
        return ds;
    }

    public List<BangGiaHeader> getTatCaBangGia() {
        return getAllBangGiaHeader();
    }

    public List<BangGiaHeader> getDanhSachBangGiaTheoNgay(LocalDate ngay) {
        List<BangGiaHeader> ds = new ArrayList<>();
        try (Connection con = ConnectDB.getConnection()) {
            boolean hasTrangThai = hasColumn(con, "BangGiaHeader", "trangThai");
            String sql = hasTrangThai
                    ? """
                        SELECT *
                        FROM BangGiaHeader
                        WHERE ? BETWEEN ngayBatDau AND ngayKetThuc
                          AND trangThai = ?
                        ORDER BY maBangGia
                    """
                    : """
                        SELECT *
                        FROM BangGiaHeader
                        WHERE ? BETWEEN ngayBatDau AND ngayKetThuc
                        ORDER BY maBangGia
                    """;
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setDate(1, Date.valueOf(ngay));
                if (hasTrangThai) {
                    stmt.setString(2, BangGiaHeader.TRANG_THAI_DANG_HOAT_DONG);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        ds.add(mapHeader(rs));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public BangGiaHeader getBangGiaByMa(String ma) {
        String sql = "SELECT * FROM BangGiaHeader WHERE maBangGia = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, ma);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapHeader(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(BangGiaHeader bg) {
        String sql = """
                INSERT INTO BangGiaHeader(maBangGia, tenBangGia, ngayBatDau, ngayKetThuc, loaiNgay, trangThai, phanTramTang)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, bg.getMaBangGia());
            stmt.setString(2, bg.getTenBangGia());
            stmt.setDate(3, Date.valueOf(bg.getNgayBatDau()));
            stmt.setDate(4, Date.valueOf(bg.getNgayKetThuc()));
            stmt.setString(5, normalizeLoaiNgay(bg.getLoaiNgay()));
            stmt.setString(6, normalizeTrangThai(bg.getTrangThai()));
            stmt.setDouble(7, bg.getPhanTramTang());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(BangGiaHeader bg) {
        String sql = """
                UPDATE BangGiaHeader
                SET tenBangGia=?, ngayBatDau=?, ngayKetThuc=?, loaiNgay=?, trangThai=?, phanTramTang=?
                WHERE maBangGia=?
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, bg.getTenBangGia());
            stmt.setDate(2, Date.valueOf(bg.getNgayBatDau()));
            stmt.setDate(3, Date.valueOf(bg.getNgayKetThuc()));
            stmt.setString(4, normalizeLoaiNgay(bg.getLoaiNgay()));
            stmt.setString(5, normalizeTrangThai(bg.getTrangThai()));
            stmt.setDouble(6, bg.getPhanTramTang());
            stmt.setString(7, bg.getMaBangGia());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maBangGia) {
        String sql = "DELETE FROM BangGiaHeader WHERE maBangGia=?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maBangGia);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public BangGiaHeader getBangGiaDangHoatDongTheoLoai(String loaiNgay, LocalDate ngay) {
        try (Connection con = ConnectDB.getConnection()) {
            boolean hasTrangThai = hasColumn(con, "BangGiaHeader", "trangThai");
            String sql = hasTrangThai
                    ? """
                        SELECT TOP 1 *
                        FROM BangGiaHeader
                        WHERE loaiNgay = ?
                          AND trangThai = ?
                          AND ? BETWEEN ngayBatDau AND ngayKetThuc
                        ORDER BY ngayBatDau DESC, maBangGia
                    """
                    : """
                        SELECT TOP 1 *
                        FROM BangGiaHeader
                        WHERE loaiNgay = ?
                          AND ? BETWEEN ngayBatDau AND ngayKetThuc
                        ORDER BY ngayBatDau DESC, maBangGia
                    """;
            try (PreparedStatement stmt = con.prepareStatement(sql)) {
                stmt.setString(1, normalizeLoaiNgay(loaiNgay));
                if (hasTrangThai) {
                    stmt.setString(2, BangGiaHeader.TRANG_THAI_DANG_HOAT_DONG);
                    stmt.setDate(3, Date.valueOf(ngay));
                } else {
                    stmt.setDate(2, Date.valueOf(ngay));
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return mapHeader(rs);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BangGiaHeader getBangGiaLeDangHoatDong(LocalDate ngay) {
        return getBangGiaDangHoatDongTheoLoai(BangGiaHeader.LOAI_NGAY_LE, ngay);
    }

    public BangGiaHeader getBangGiaHopLe(LocalDate ngay) {
        BangGiaHeader bangGiaLe = getBangGiaLeDangHoatDong(ngay);
        if (bangGiaLe != null) {
            return bangGiaLe;
        }

        if (isWeekend(ngay)) {
            BangGiaHeader bangGiaCuoiTuan = getBangGiaDangHoatDongTheoLoai(BangGiaHeader.LOAI_CUOI_TUAN, ngay);
            if (bangGiaCuoiTuan != null) {
                return bangGiaCuoiTuan;
            }
        }

        return getBangGiaDangHoatDongTheoLoai(BangGiaHeader.LOAI_NGAY_THUONG, ngay);
    }

    public BangGiaHeader getBangGiaTheoNgay(LocalDate ngay) {
        return getBangGiaHopLe(ngay);
    }

    public boolean isWeekend(LocalDate ngay) {
        if (ngay == null) {
            return false;
        }
        DayOfWeek d = ngay.getDayOfWeek();
        return d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
    }

    private BangGiaHeader mapHeader(ResultSet rs) throws SQLException {
        return new BangGiaHeader(
                rs.getString("maBangGia"),
                rs.getString("tenBangGia"),
                rs.getDate("ngayBatDau").toLocalDate(),
                rs.getDate("ngayKetThuc").toLocalDate(),
                normalizeLoaiNgay(rs.getString("loaiNgay")),
                normalizeTrangThai(getStringOrDefault(rs, "trangThai", BangGiaHeader.TRANG_THAI_DANG_HOAT_DONG)),
                getDoubleOrDefault(rs, "phanTramTang", 0));
    }

    private boolean hasColumn(Connection con, String tableName, String columnName) throws SQLException {
        DatabaseMetaData metaData = con.getMetaData();
        try (ResultSet rs = metaData.getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private String getStringOrDefault(ResultSet rs, String columnName, String defaultValue) throws SQLException {
        if (!hasColumn(rs, columnName)) {
            return defaultValue;
        }
        String value = rs.getString(columnName);
        return value == null ? defaultValue : value;
    }

    private double getDoubleOrDefault(ResultSet rs, String columnName, double defaultValue) throws SQLException {
        if (!hasColumn(rs, columnName)) {
            return defaultValue;
        }
        double value = rs.getDouble(columnName);
        return rs.wasNull() ? defaultValue : value;
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (columnName.equalsIgnoreCase(metaData.getColumnLabel(i))) {
                return true;
            }
        }
        return false;
    }

    public String normalizeLoaiNgay(String loaiNgay) {
        if (loaiNgay == null) {
            return BangGiaHeader.LOAI_NGAY_THUONG;
        }
        String value = loaiNgay.trim().toUpperCase();

        if (value.equals(BangGiaHeader.LOAI_NGAY_THUONG) || "NGAY THUONG".equals(value)
                || "NGÀY THƯỜNG".equals(value) || "THUONG".equals(value)) {
            return BangGiaHeader.LOAI_NGAY_THUONG;
        }
        if (value.equals(BangGiaHeader.LOAI_CUOI_TUAN) || "CUOI TUAN".equals(value)
                || "CUỐI TUẦN".equals(value) || "WEEKEND".equals(value)) {
            return BangGiaHeader.LOAI_CUOI_TUAN;
        }
        if (value.equals(BangGiaHeader.LOAI_NGAY_LE) || "NGAY LE".equals(value)
                || "LỄ".equals(value) || "LE".equals(value) || "TET".equals(value) || "TẾT".equals(value)) {
            return BangGiaHeader.LOAI_NGAY_LE;
        }

        return BangGiaHeader.LOAI_NGAY_THUONG;
    }

    public String normalizeTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) {
            return BangGiaHeader.TRANG_THAI_DANG_HOAT_DONG;
        }
        if (BangGiaHeader.TRANG_THAI_KHONG_HOAT_DONG.equalsIgnoreCase(trangThai.trim())) {
            return BangGiaHeader.TRANG_THAI_KHONG_HOAT_DONG;
        }
        return BangGiaHeader.TRANG_THAI_DANG_HOAT_DONG;
    }
}
