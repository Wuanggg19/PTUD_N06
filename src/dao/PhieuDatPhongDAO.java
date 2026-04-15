package dao;

import connectDB.ConnectDB;
import entity.ChiTietPhieuDat;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuDatPhong;
import util.BookingStatus;
import util.RoomStatus;
import util.StatusUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PhieuDatPhongDAO {

    public List<PhieuDatPhong> getAllPhieuDatPhong() {
        List<PhieuDatPhong> ds = new ArrayList<>();
        String sql = """
                SELECT pdp.*, kh.tenKhachHang, kh.soDienThoai, kh.diaChi, kh.gioiTinh,
                       MIN(ct.ngayNhan) AS ngayNhan, MAX(ct.ngayTra) AS ngayTra,
                       STRING_AGG(ct.maPhong, ', ') AS dsMaPhong, COUNT(ct.maPhong) AS soLuongPhong
                FROM PhieuDatPhong pdp
                JOIN KhachHang kh ON pdp.maKhachHang = kh.maKhachHang
                LEFT JOIN ChiTietPhieuDat ct ON pdp.maDatPhong = ct.maDatPhong
                GROUP BY pdp.maDatPhong, pdp.ngayDat, pdp.trangThai, pdp.maNhanVien, pdp.maKhachHang,
                         kh.tenKhachHang, kh.soDienThoai, kh.diaChi, kh.gioiTinh
                ORDER BY pdp.ngayDat DESC, pdp.maDatPhong DESC
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ds.add(mapPhieuDatPhong(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public PhieuDatPhong getPhieuDangOTheoPhong(String maPhong) {
        String sql = """
                SELECT pdp.*, kh.tenKhachHang, kh.soDienThoai, kh.diaChi, kh.gioiTinh,
                       ct.ngayNhan, ct.ngayTra, ct.maPhong AS dsMaPhong, 1 AS soLuongPhong
                FROM PhieuDatPhong pdp
                JOIN ChiTietPhieuDat ct ON pdp.maDatPhong = ct.maDatPhong
                JOIN KhachHang kh ON pdp.maKhachHang = kh.maKhachHang
                WHERE ct.maPhong = ?
                  AND (pdp.trangThai LIKE '%NHAN_PHONG%' OR pdp.trangThai LIKE N'%nhận phòng%')
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, maPhong);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapPhieuDatPhong(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public PhieuDatPhong getActivePhieuByMaPhong(String maPhong) {
        return getPhieuDangOTheoPhong(maPhong);
    }

    public boolean checkIn(String maPhieu) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }
        try {
            con.setAutoCommit(false);
            try (PreparedStatement st1 = con.prepareStatement(
                    "UPDATE PhieuDatPhong SET trangThai = ? WHERE maDatPhong = ?")) {
                st1.setString(1, BookingStatus.DA_NHAN_PHONG.getCode());
                st1.setString(2, maPhieu);
                st1.executeUpdate();
            }
            List<String> dsMaPhong = new ArrayList<>();
            try (PreparedStatement st2 = con.prepareStatement(
                    "SELECT maPhong FROM ChiTietPhieuDat WHERE maDatPhong = ?")) {
                st2.setString(1, maPhieu);
                try (ResultSet rs = st2.executeQuery()) {
                    while (rs.next()) {
                        dsMaPhong.add(rs.getString("maPhong"));
                    }
                }
            }
            if (!dsMaPhong.isEmpty()) {
                try (PreparedStatement st3 = con.prepareStatement(
                        "UPDATE Phong SET trangThai = ? WHERE maPhong = ?")) {
                    for (String maPhong : dsMaPhong) {
                        st3.setString(1, RoomStatus.DANG_O.getCode());
                        st3.setString(2, maPhong);
                        st3.executeUpdate();
                    }
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean doiPhong(String maPhieu, String maPhongCu, String maPhongMoi, double giaMoi) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }
        try {
            con.setAutoCommit(false);
            Timestamp now = Timestamp.valueOf(LocalDateTime.now());

            try (PreparedStatement st1 = con.prepareStatement(
                    "UPDATE ChiTietPhieuDat SET ngayTra = ? WHERE maDatPhong = ? AND maPhong = ?")) {
                st1.setTimestamp(1, now);
                st1.setString(2, maPhieu);
                st1.setString(3, maPhongCu);
                st1.executeUpdate();
            }

            try (PreparedStatement st2 = con.prepareStatement(
                    "UPDATE Phong SET trangThai = ? WHERE maPhong = ?")) {
                st2.setString(1, RoomStatus.TRONG.getCode());
                st2.setString(2, maPhongCu);
                st2.executeUpdate();
            }

            try (PreparedStatement st3 = con.prepareStatement(
                    "INSERT INTO ChiTietPhieuDat (maDatPhong, maPhong, giaThuePhong, ngayNhan, ngayTra) VALUES (?, ?, ?, ?, ?)")) {
                st3.setString(1, maPhieu);
                st3.setString(2, maPhongMoi);
                st3.setDouble(3, giaMoi);
                st3.setTimestamp(4, now);
                st3.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now().plusDays(1)));
                st3.executeUpdate();
            }

            try (PreparedStatement st4 = con.prepareStatement(
                    "UPDATE Phong SET trangThai = ? WHERE maPhong = ?")) {
                st4.setString(1, RoomStatus.DANG_O.getCode());
                st4.setString(2, maPhongMoi);
                st4.executeUpdate();
            }

            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean datPhong(PhieuDatPhong pdp, List<ChiTietPhieuDat> dsChiTiet) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }
        try {
            con.setAutoCommit(false);
            try (PreparedStatement stmtHeader = con.prepareStatement(
                    "INSERT INTO PhieuDatPhong VALUES(?, ?, ?, ?, ?)");
                 PreparedStatement stmtDetail = con.prepareStatement(
                         "INSERT INTO ChiTietPhieuDat VALUES(?, ?, ?, ?, ?)")) {
                stmtHeader.setString(1, pdp.getMaDatPhong());
                stmtHeader.setTimestamp(2, Timestamp.valueOf(pdp.getNgayDat()));
                stmtHeader.setString(3, StatusUtils.bookingCode(pdp.getTrangThai()));
                stmtHeader.setString(4, pdp.getNhanVien().getMaNhanVien());
                stmtHeader.setString(5, pdp.getKhachHang().getMaKhachHang());
                stmtHeader.executeUpdate();

                for (ChiTietPhieuDat ct : dsChiTiet) {
                    stmtDetail.setString(1, pdp.getMaDatPhong());
                    stmtDetail.setString(2, ct.getPhong().getMaPhong());
                    stmtDetail.setDouble(3, ct.getGiaThuePhong());
                    stmtDetail.setTimestamp(4, Timestamp.valueOf(ct.getNgayNhan()));
                    stmtDetail.setTimestamp(5, Timestamp.valueOf(ct.getNgayTra()));
                    stmtDetail.executeUpdate();
                }
            }

            if (!dsChiTiet.isEmpty()) {
                String roomStatus = StatusUtils.isBookingStatus(pdp.getTrangThai(), BookingStatus.DA_NHAN_PHONG)
                        ? RoomStatus.DANG_O.getCode()
                        : RoomStatus.CHO_XAC_NHAN.getCode();
                try (PreparedStatement stmtRoom = con.prepareStatement(
                        "UPDATE Phong SET trangThai = ? WHERE maPhong = ?")) {
                    for (ChiTietPhieuDat ct : dsChiTiet) {
                        stmtRoom.setString(1, roomStatus);
                        stmtRoom.setString(2, ct.getPhong().getMaPhong());
                        stmtRoom.executeUpdate();
                    }
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            try {
                con.rollback();
            } catch (SQLException ignored) {
            }
            e.printStackTrace();
            return false;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (SQLException ignored) {
            }
        }
    }

    public boolean huyPhieu(String maDatPhong) {
        String sql = """
                UPDATE PhieuDatPhong
                SET trangThai = ?
                WHERE maDatPhong = ?
                  AND UPPER(ISNULL(trangThai, '')) NOT IN ('DA_THANH_TOAN', 'DA_HUY')
                """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, BookingStatus.DA_HUY.getCode());
            stmt.setString(2, maDatPhong);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String generateNextId() {
        try (Connection con = ConnectDB.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT MAX(CAST(SUBSTRING(maDatPhong, 3, LEN(maDatPhong)) AS INT)) FROM PhieuDatPhong")) {
            if (rs.next()) {
                return String.format("DP%03d", rs.getInt(1) + 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "DP001";
    }

    private PhieuDatPhong mapPhieuDatPhong(ResultSet rs) throws SQLException {
        KhachHang kh = new KhachHang(
                rs.getString("maKhachHang"),
                rs.getString("tenKhachHang"),
                rs.getString("diaChi"),
                rs.getBoolean("gioiTinh"),
                rs.getString("soDienThoai")
        );

        PhieuDatPhong phieu = new PhieuDatPhong(
                rs.getString("maDatPhong"),
                rs.getTimestamp("ngayDat").toLocalDateTime(),
                rs.getString("trangThai"),
                new NhanVien(rs.getString("maNhanVien")),
                kh
        );

        Timestamp ngayNhan = rs.getTimestamp("ngayNhan");
        Timestamp ngayTra = rs.getTimestamp("ngayTra");
        phieu.setNgayNhan(ngayNhan != null ? ngayNhan.toLocalDateTime() : null);
        phieu.setNgayTra(ngayTra != null ? ngayTra.toLocalDateTime() : null);
        phieu.setDsMaPhong(rs.getString("dsMaPhong"));
        phieu.setSoLuongPhong(rs.getInt("soLuongPhong"));
        return phieu;
    }
}
