package dao;

import connectDB.ConnectDB;
import entity.BangGiaHeader;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BangGiaHeaderDAO {

    public List<BangGiaHeader> getAllBangGiaHeader() {
        List<BangGiaHeader> ds = new ArrayList<>();
        String sql = """
                SELECT maBangGia, tenBangGia, loaiBangGia, loaiNgay, ngayBatDau, ngayKetThuc, trangThai
                FROM BangGiaHeader
                ORDER BY maBangGia
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ds.add(mapHeader(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Không thể tải dữ liệu bảng giá từ BangGiaHeader.", e);
        }
        return ds;
    }

    public List<BangGiaHeader> getTatCaBangGia() {
        return getAllBangGiaHeader();
    }

    public List<BangGiaHeader> getDanhSachBangGiaTheoNgay(LocalDate ngay) {
        List<BangGiaHeader> ds = new ArrayList<>();
        String sql = """
                SELECT maBangGia, tenBangGia, loaiBangGia, loaiNgay, ngayBatDau, ngayKetThuc, trangThai
                FROM BangGiaHeader
                WHERE ngayBatDau <= ?
                  AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?)
                  AND trangThai = ?
                ORDER BY ngayBatDau DESC, maBangGia
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(ngay));
            stmt.setDate(2, Date.valueOf(ngay));
            stmt.setBoolean(3, true);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapHeader(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public BangGiaHeader getBangGiaByMa(String ma) {
        String sql = """
                SELECT maBangGia, tenBangGia, loaiBangGia, loaiNgay, ngayBatDau, ngayKetThuc, trangThai
                FROM BangGiaHeader
                WHERE maBangGia = ?
                """;
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
                INSERT INTO BangGiaHeader(maBangGia, tenBangGia, loaiBangGia, loaiNgay, ngayBatDau, ngayKetThuc, trangThai)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            bindHeader(stmt, bg, false);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(BangGiaHeader bg) {
        String sql = """
                UPDATE BangGiaHeader
                SET tenBangGia = ?, loaiBangGia = ?, loaiNgay = ?, ngayBatDau = ?, ngayKetThuc = ?, trangThai = ?
                WHERE maBangGia = ?
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            bindHeader(stmt, bg, true);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(String maBangGia) {
        String sql = "DELETE FROM BangGiaHeader WHERE maBangGia = ?";
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
        String sql = """
                SELECT TOP 1 maBangGia, tenBangGia, loaiBangGia, loaiNgay, ngayBatDau, ngayKetThuc, trangThai
                FROM BangGiaHeader
                WHERE loaiNgay = ?
                  AND trangThai = ?
                  AND ngayBatDau <= ?
                  AND (ngayKetThuc IS NULL OR ngayKetThuc >= ?)
                ORDER BY ngayBatDau DESC, maBangGia
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, normalizeLoaiNgay(loaiNgay));
            stmt.setBoolean(2, true);
            stmt.setDate(3, Date.valueOf(ngay));
            stmt.setDate(4, Date.valueOf(ngay));
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

    private void bindHeader(PreparedStatement stmt, BangGiaHeader bg, boolean update) throws SQLException {
        int index = 1;
        if (!update) {
            stmt.setString(index++, bg.getMaBangGia());
        }
        stmt.setString(index++, bg.getTenBangGia());
        stmt.setString(index++, normalizeLoaiBangGia(bg.getLoaiBangGia()));
        stmt.setString(index++, normalizeLoaiNgay(bg.getLoaiNgay()));
        stmt.setDate(index++, Date.valueOf(bg.getNgayBatDau()));
        if (bg.getNgayKetThuc() != null) {
            stmt.setDate(index++, Date.valueOf(bg.getNgayKetThuc()));
        } else {
            stmt.setDate(index++, null);
        }
        stmt.setBoolean(index++, bg.isDangHoatDong());
        if (update) {
            stmt.setString(index, bg.getMaBangGia());
        }
    }

    private BangGiaHeader mapHeader(ResultSet rs) throws SQLException {
        return new BangGiaHeader(
                rs.getString("maBangGia"),
                rs.getString("tenBangGia"),
                getStringOrDefault(rs, "loaiBangGia", ""),
                normalizeLoaiNgay(rs.getString("loaiNgay")),
                rs.getDate("ngayBatDau").toLocalDate(),
                rs.getDate("ngayKetThuc") != null ? rs.getDate("ngayKetThuc").toLocalDate() : null,
                getBooleanOrDefault(rs, "trangThai", true),
                0
        );
    }

    private String getStringOrDefault(ResultSet rs, String columnName, String defaultValue) throws SQLException {
        if (!hasColumn(rs, columnName)) {
            return defaultValue;
        }
        String value = rs.getString(columnName);
        return value == null ? defaultValue : value;
    }

    private boolean getBooleanOrDefault(ResultSet rs, String columnName, boolean defaultValue) throws SQLException {
        if (!hasColumn(rs, columnName)) {
            return defaultValue;
        }
        boolean value = rs.getBoolean(columnName);
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

    public String normalizeLoaiBangGia(String loaiBangGia) {
        return loaiBangGia == null ? "" : loaiBangGia.trim();
    }

    public String normalizeLoaiNgay(String loaiNgay) {
        if (loaiNgay == null) {
            return BangGiaHeader.LOAI_NGAY_THUONG;
        }

        String raw = loaiNgay.trim();
        String normalized = stripVietnamese(raw).replace(' ', '_');
        if (normalized.equals("NGAY_THUONG") || normalized.equals("THUONG")) {
            return BangGiaHeader.LOAI_NGAY_THUONG;
        }
        if (normalized.equals("CUOI_TUAN") || normalized.equals("WEEKEND")) {
            return BangGiaHeader.LOAI_CUOI_TUAN;
        }
        if (normalized.equals("NGAY_LE") || normalized.equals("LE") || normalized.equals("TET")) {
            return BangGiaHeader.LOAI_NGAY_LE;
        }

        return raw.toUpperCase(Locale.ROOT).replace(' ', '_');
    }

    public String normalizeTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) {
            return BangGiaHeader.TRANG_THAI_DANG_HOAT_DONG;
        }
        String normalized = stripVietnamese(trangThai);
        if (normalized.contains("KHONG")) {
            return BangGiaHeader.TRANG_THAI_KHONG_HOAT_DONG;
        }
        return BangGiaHeader.TRANG_THAI_DANG_HOAT_DONG;
    }

    private String stripVietnamese(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('Đ', 'D')
                .replace('đ', 'd');
        return normalized.trim().toUpperCase(Locale.ROOT);
    }
}
