package dao;

import connectDB.ConnectDB;
import entity.BangGiaHeader;
import entity.ChiTietBangGia;
import entity.Phong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ChiTietBangGiaDAO {

    private final BangGiaHeaderDAO bangGiaHeaderDAO = new BangGiaHeaderDAO();

    public List<ChiTietBangGia> getDetailsByMaBangGia(String maBangGia) {
        List<ChiTietBangGia> ds = new ArrayList<>();
        String sql = """
                SELECT ct.maBangGia, ct.maLoai, ct.giaTheoGio, ct.giaQuaDem, ct.giaTheoNgay,
                       ct.giaCuoiTuan, ct.giaLe, ct.phuThu,
                       bh.tenBangGia, bh.loaiBangGia, bh.loaiNgay, bh.ngayBatDau, bh.ngayKetThuc, bh.trangThai
                FROM ChiTietBangGia ct
                JOIN BangGiaHeader bh ON ct.maBangGia = bh.maBangGia
                WHERE ct.maBangGia = ?
                ORDER BY ct.maLoai
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maBangGia);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapChiTietBangGia(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public ChiTietBangGia getById(String maBangGia, String maLoai) {
        String sql = """
                SELECT ct.maBangGia, ct.maLoai, ct.giaTheoGio, ct.giaQuaDem, ct.giaTheoNgay,
                       ct.giaCuoiTuan, ct.giaLe, ct.phuThu,
                       bh.tenBangGia, bh.loaiBangGia, bh.loaiNgay, bh.ngayBatDau, bh.ngayKetThuc, bh.trangThai
                FROM ChiTietBangGia ct
                JOIN BangGiaHeader bh ON ct.maBangGia = bh.maBangGia
                WHERE ct.maBangGia = ? AND ct.maLoai = ?
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maBangGia);
            stmt.setString(2, maLoai);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapChiTietBangGia(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean create(ChiTietBangGia ct) {
        String sql = """
                INSERT INTO ChiTietBangGia(maBangGia, maLoai, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            bindChiTiet(stmt, ct);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(ChiTietBangGia ct) {
        String sql = """
                UPDATE ChiTietBangGia
                SET giaTheoGio = ?, giaQuaDem = ?, giaTheoNgay = ?, giaCuoiTuan = ?, giaLe = ?, phuThu = ?
                WHERE maBangGia = ? AND maLoai = ?
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setDouble(1, ct.getGiaTheoGio());
            stmt.setDouble(2, ct.getGiaQuaDem());
            stmt.setDouble(3, ct.getGiaTheoNgay());
            stmt.setDouble(4, ct.getGiaCuoiTuan());
            stmt.setDouble(5, ct.getGiaLe());
            stmt.setDouble(6, ct.getPhuThu());
            stmt.setString(7, ct.getBangGia().getMaBangGia());
            stmt.setString(8, ct.getMaLoai());
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean saveOrUpdate(ChiTietBangGia ct) {
        ChiTietBangGia existing = getById(ct.getBangGia().getMaBangGia(), ct.getMaLoai());
        return existing == null ? create(ct) : update(ct);
    }

    public boolean delete(String maBangGia, String maLoai) {
        String sql = "DELETE FROM ChiTietBangGia WHERE maBangGia = ? AND maLoai = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maBangGia);
            stmt.setString(2, maLoai);
            return stmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteByMaBangGia(String maBangGia) {
        String sql = "DELETE FROM ChiTietBangGia WHERE maBangGia = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maBangGia);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ChiTietBangGia getChiTietApDung(Phong phong, LocalDate ngay) {
        if (phong == null || ngay == null) {
            return null;
        }
        BangGiaHeader bangGia = bangGiaHeaderDAO.getBangGiaTheoNgay(ngay);
        if (bangGia == null || !bangGia.isDangHoatDong()) {
            return null;
        }
        return getById(bangGia.getMaBangGia(), phong.getMaLoai());
    }

    public double getGiaTheoNgay(Phong phong, LocalDate ngay) {
        if (phong == null || ngay == null) {
            return 0;
        }
        ChiTietBangGia chiTiet = getChiTietApDung(phong, ngay);
        if (chiTiet == null) {
            return phong.getGiaPhong();
        }

        String loaiNgay = chiTiet.getBangGia().getLoaiNgay();
        if (BangGiaHeader.LOAI_NGAY_LE.equals(loaiNgay) && chiTiet.getGiaLe() > 0) {
            return chiTiet.getGiaLe();
        }
        if (bangGiaHeaderDAO.isWeekend(ngay) && chiTiet.getGiaCuoiTuan() > 0) {
            return chiTiet.getGiaCuoiTuan();
        }
        if (chiTiet.getGiaTheoNgay() > 0) {
            return chiTiet.getGiaTheoNgay();
        }
        return phong.getGiaPhong();
    }

    public double calculateStayPrice(Phong phong, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (phong == null || checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return 0;
        }

        LocalDate ngayBatDau = checkIn.toLocalDate();
        LocalDate ngayKetThuc = checkOut.toLocalDate();
        long soNgay = java.time.temporal.ChronoUnit.DAYS.between(ngayBatDau, ngayKetThuc);
        if (!checkOut.toLocalTime().equals(checkIn.toLocalTime()) || soNgay <= 0) {
            soNgay++;
        }

        double tongTien = 0;
        LocalDate current = ngayBatDau;
        for (int i = 0; i < soNgay; i++) {
            tongTien += getGiaTheoNgay(phong, current);
            current = current.plusDays(1);
        }
        return tongTien;
    }

    private void bindChiTiet(PreparedStatement stmt, ChiTietBangGia ct) throws SQLException {
        stmt.setString(1, ct.getBangGia().getMaBangGia());
        stmt.setString(2, ct.getMaLoai());
        stmt.setDouble(3, ct.getGiaTheoGio());
        stmt.setDouble(4, ct.getGiaQuaDem());
        stmt.setDouble(5, ct.getGiaTheoNgay());
        stmt.setDouble(6, ct.getGiaCuoiTuan());
        stmt.setDouble(7, ct.getGiaLe());
        stmt.setDouble(8, ct.getPhuThu());
    }

    private ChiTietBangGia mapChiTietBangGia(ResultSet rs) throws SQLException {
        BangGiaHeader bangGia = new BangGiaHeader(
                rs.getString("maBangGia"),
                rs.getString("tenBangGia"),
                rs.getString("loaiBangGia"),
                bangGiaHeaderDAO.normalizeLoaiNgay(rs.getString("loaiNgay")),
                rs.getDate("ngayBatDau").toLocalDate(),
                rs.getDate("ngayKetThuc") != null ? rs.getDate("ngayKetThuc").toLocalDate() : null,
                rs.getBoolean("trangThai"),
                0
        );

        return new ChiTietBangGia(
                bangGia,
                rs.getString("maLoai"),
                rs.getDouble("giaTheoGio"),
                rs.getDouble("giaQuaDem"),
                rs.getDouble("giaTheoNgay"),
                rs.getDouble("giaCuoiTuan"),
                rs.getDouble("giaLe"),
                rs.getDouble("phuThu")
        );
    }
}
