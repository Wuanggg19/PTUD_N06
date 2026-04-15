package dao;

import connectDB.ConnectDB;
import entity.BangGiaDetail;
import entity.BangGiaHeader;
import entity.Phong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BangGiaDetailDAO {

    private final BangGiaHeaderDAO headerDAO = new BangGiaHeaderDAO();

    public List<BangGiaDetail> getDetailsByMaBangGia(String maBangGia) {
        List<BangGiaDetail> ds = new ArrayList<>();
        String sql = """
                SELECT cd.*, bh.tenBangGia, bh.ngayBatDau, bh.ngayKetThuc, bh.loaiNgay, bh.trangThai, bh.phanTramTang
                FROM ChiTietBangGia cd
                JOIN BangGiaHeader bh ON cd.maBangGia = bh.maBangGia
                WHERE cd.maBangGia = ?
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maBangGia);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ds.add(mapDetail(rs));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ds;
    }

    public BangGiaDetail getDetailByBangGiaAndLoaiPhong(String maBangGia, String loaiPhong) {
        String sql = """
                SELECT cd.*, bh.tenBangGia, bh.ngayBatDau, bh.ngayKetThuc, bh.loaiNgay, bh.trangThai, bh.phanTramTang
                FROM ChiTietBangGia cd
                JOIN BangGiaHeader bh ON cd.maBangGia = bh.maBangGia
                WHERE cd.maBangGia = ? AND cd.loaiPhong = ?
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maBangGia);
            stmt.setString(2, loaiPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapDetail(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public BangGiaDetail getDetailByBangGiaAndLoai(String maBangGia, String loaiPhong) {
        return getDetailByBangGiaAndLoaiPhong(maBangGia, loaiPhong);
    }

    public double getGiaTheoNgay(Phong phong, LocalDate ngay) {
        if (phong == null || ngay == null) {
            return 0;
        }

        try {
            // 1) Ưu tiên bảng giá lễ đang hoạt động
            BangGiaHeader bangGiaLe = headerDAO.getBangGiaLeDangHoatDong(ngay);
            if (bangGiaLe != null) {
                double giaNen = headerDAO.isWeekend(ngay) ? getGiaCuoiTuan(phong, ngay) : getGiaNgayThuong(phong, ngay);
                return tinhGiaNgayLeTheoPhanTram(giaNen, bangGiaLe.getPhanTramTang());
            }

            // 2) Cuối tuần
            if (headerDAO.isWeekend(ngay)) {
                return getGiaCuoiTuan(phong, ngay);
            }

            // 3) Ngày thường
            return getGiaNgayThuong(phong, ngay);
        } catch (Exception e) {
            e.printStackTrace();
            return phong.getGiaPhong();
        }
    }

    public double getGiaNgayThuong(Phong phong, LocalDate ngay) {
        BangGiaHeader bg = headerDAO.getBangGiaDangHoatDongTheoLoai(BangGiaHeader.LOAI_NGAY_THUONG, ngay);
        return getGiaTheoBangGiaHoacGiaGoc(bg, phong);
    }

    public double getGiaCuoiTuan(Phong phong, LocalDate ngay) {
        BangGiaHeader bg = headerDAO.getBangGiaDangHoatDongTheoLoai(BangGiaHeader.LOAI_CUOI_TUAN, ngay);
        return getGiaTheoBangGiaHoacGiaGoc(bg, phong);
    }

    public double getGiaNgayLe(Phong phong, LocalDate ngay) {
        BangGiaHeader bgLe = headerDAO.getBangGiaLeDangHoatDong(ngay);
        if (bgLe == null) {
            return getGiaTheoNgay(phong, ngay);
        }
        double giaNen = headerDAO.isWeekend(ngay) ? getGiaCuoiTuan(phong, ngay) : getGiaNgayThuong(phong, ngay);
        return tinhGiaNgayLeTheoPhanTram(giaNen, bgLe.getPhanTramTang());
    }

    public double calculateStayPrice(String loaiPhong, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (loaiPhong == null || loaiPhong.isBlank() || checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return 0;
        }

        Phong phongTam = new Phong("P000", loaiPhong, 1, "TRONG", 0);
        LocalDate ngayBatDau = checkIn.toLocalDate();
        LocalDate ngayKetThuc = checkOut.toLocalDate();

        long soNgay = java.time.temporal.ChronoUnit.DAYS.between(ngayBatDau, ngayKetThuc);
        if (!checkOut.toLocalTime().equals(checkIn.toLocalTime()) || soNgay <= 0) {
            soNgay++;
        }

        double tongTien = 0;
        LocalDate current = ngayBatDau;
        for (int i = 0; i < soNgay; i++) {
            tongTien += getGiaTheoNgay(phongTam, current);
            current = current.plusDays(1);
        }
        return tongTien;
    }

    public double tinhGiaNgayLeTheoPhanTram(double giaNen, double phanTramTang) {
        if (giaNen < 0) {
            giaNen = 0;
        }
        if (phanTramTang < 0) {
            phanTramTang = 0;
        }
        return giaNen + (giaNen * phanTramTang / 100.0);
    }

    private double getGiaTheoBangGiaHoacGiaGoc(BangGiaHeader bg, Phong phong) {
        if (bg != null) {
            BangGiaDetail detail = getDetailByBangGiaAndLoaiPhong(bg.getMaBangGia(), phong.getLoaiPhong());
            if (detail != null && detail.getGiaTheoNgay() > 0) {
                return detail.getGiaTheoNgay();
            }
        }
        return phong.getGiaPhong();
    }

    private BangGiaDetail mapDetail(ResultSet rs) throws Exception {
        BangGiaHeader bg = new BangGiaHeader(
                rs.getString("maBangGia"),
                rs.getString("tenBangGia"),
                rs.getDate("ngayBatDau").toLocalDate(),
                rs.getDate("ngayKetThuc").toLocalDate(),
                headerDAO.normalizeLoaiNgay(rs.getString("loaiNgay")),
                headerDAO.normalizeTrangThai(rs.getString("trangThai")),
                rs.getDouble("phanTramTang"));

        return new BangGiaDetail(
                bg,
                rs.getString("loaiPhong"),
                rs.getDouble("giaTheoGio"),
                rs.getDouble("giaQuaDem"),
                rs.getDouble("giaTheoNgay"),
                rs.getDouble("giaCuoiTuan"),
                rs.getDouble("giaLe"),
                rs.getDouble("phuThu")
        );
    }

    public boolean deleteByMaBangGia(String maBangGia) {
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement("DELETE FROM ChiTietBangGia WHERE maBangGia=?")) {
            stmt.setString(1, maBangGia);
            stmt.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveOrUpdate(BangGiaDetail ct) {
        if (ct == null || ct.getBangGia() == null || ct.getBangGia().getMaBangGia() == null
                || ct.getLoaiPhongApDung() == null || ct.getLoaiPhongApDung().isBlank()) {
            return false;
        }

        BangGiaDetail existing = getDetailByBangGiaAndLoaiPhong(
                ct.getBangGia().getMaBangGia(),
                ct.getLoaiPhongApDung()
        );

        double giaTheoGio = effectivePrice(ct.getGiaTheoGio(), existing != null ? existing.getGiaTheoGio() : 0);
        double giaQuaDem = effectivePrice(ct.getGiaQuaDem(), existing != null ? existing.getGiaQuaDem() : 0);
        double giaTheoNgay = effectivePrice(ct.getGiaTheoNgay(), existing != null ? existing.getGiaTheoNgay() : 0);
        double giaCuoiTuan = effectivePrice(ct.getGiaCuoiTuan(), existing != null ? existing.getGiaCuoiTuan() : 0);
        double giaLe = effectivePrice(ct.getGiaLe(), existing != null ? existing.getGiaLe() : 0);
        double phuThu = existing != null ? existing.getPhuThu() : ct.getPhuThu();

        try (Connection con = ConnectDB.getConnection()) {
            if (existing == null) {
                String insertSql = """
                        INSERT INTO ChiTietBangGia
                        (maBangGia, loaiPhong, giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """;
                try (PreparedStatement stmt = con.prepareStatement(insertSql)) {
                    bindPrices(stmt, ct.getBangGia().getMaBangGia(), ct.getLoaiPhongApDung(),
                            giaTheoGio, giaQuaDem, giaTheoNgay, giaCuoiTuan, giaLe, phuThu);
                    return stmt.executeUpdate() > 0;
                }
            }

            String updateSql = """
                    UPDATE ChiTietBangGia
                    SET giaTheoGio = ?, giaQuaDem = ?, giaTheoNgay = ?, giaCuoiTuan = ?, giaLe = ?, phuThu = ?
                    WHERE maBangGia = ? AND loaiPhong = ?
                    """;
            try (PreparedStatement stmt = con.prepareStatement(updateSql)) {
                stmt.setDouble(1, giaTheoGio);
                stmt.setDouble(2, giaQuaDem);
                stmt.setDouble(3, giaTheoNgay);
                stmt.setDouble(4, giaCuoiTuan);
                stmt.setDouble(5, giaLe);
                stmt.setDouble(6, phuThu);
                stmt.setString(7, ct.getBangGia().getMaBangGia());
                stmt.setString(8, ct.getLoaiPhongApDung());
                return stmt.executeUpdate() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void bindPrices(PreparedStatement stmt, String maBangGia, String loaiPhong,
                            double giaTheoGio, double giaQuaDem, double giaTheoNgay,
                            double giaCuoiTuan, double giaLe, double phuThu) throws SQLException {
        stmt.setString(1, maBangGia);
        stmt.setString(2, loaiPhong);
        stmt.setDouble(3, giaTheoGio);
        stmt.setDouble(4, giaQuaDem);
        stmt.setDouble(5, giaTheoNgay);
        stmt.setDouble(6, giaCuoiTuan);
        stmt.setDouble(7, giaLe);
        stmt.setDouble(8, phuThu);
    }

    private double effectivePrice(double candidate, double fallback) {
        return candidate > 0 ? candidate : fallback;
    }
}
