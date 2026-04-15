package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import connectDB.ConnectDB;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.NhanVien;
import entity.PhieuDatPhong;

public class HoaDonDAO {

    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT hd.*, kh.tenKhachHang FROM HoaDon hd " +
                         "JOIN PhieuDatPhong pdp ON hd.maDatPhong = pdp.maDatPhong " +
                         "JOIN KhachHang kh ON pdp.maKhachHang = kh.maKhachHang";
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                entity.KhachHang kh = new entity.KhachHang();
                kh.setTenKhachHang(rs.getString("tenKhachHang"));
                
                PhieuDatPhong pdp = new PhieuDatPhong(rs.getString("maDatPhong"));
                pdp.setKhachHang(kh);

                HoaDon hd = new HoaDon(
                    rs.getString("maHoaDon"),
                    rs.getTimestamp("ngayLap").toLocalDateTime(),
                    rs.getDouble("thue"),
                    rs.getDouble("tongTienPhong"),
                    rs.getDouble("tongTienDichVu"),
                    pdp,
                    new NhanVien(rs.getString("maNhanVien"))
                );
                ds.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public List<HoaDon> filterHoaDon(String keyword, java.time.LocalDate fromDate, java.time.LocalDate toDate) {
        List<HoaDon> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT hd.*, kh.tenKhachHang FROM HoaDon hd " +
                "JOIN PhieuDatPhong pdp ON hd.maDatPhong = pdp.maDatPhong " +
                "JOIN KhachHang kh ON pdp.maKhachHang = kh.maKhachHang WHERE 1=1"
            );
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                sql.append(" AND (hd.maHoaDon LIKE ? OR kh.tenKhachHang LIKE ?)");
            }
            if (fromDate != null) {
                sql.append(" AND CAST(hd.ngayLap AS DATE) >= ?");
            }
            if (toDate != null) {
                sql.append(" AND CAST(hd.ngayLap AS DATE) <= ?");
            }
            
            PreparedStatement stmt = con.prepareStatement(sql.toString());
            int paramIdx = 1;
            if (keyword != null && !keyword.trim().isEmpty()) {
                stmt.setString(paramIdx++, "%" + keyword + "%");
                stmt.setString(paramIdx++, "%" + keyword + "%");
            }
            if (fromDate != null) {
                stmt.setDate(paramIdx++, java.sql.Date.valueOf(fromDate));
            }
            if (toDate != null) {
                stmt.setDate(paramIdx++, java.sql.Date.valueOf(toDate));
            }
            
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                entity.KhachHang kh = new entity.KhachHang();
                kh.setTenKhachHang(rs.getString("tenKhachHang"));
                
                PhieuDatPhong pdp = new PhieuDatPhong(rs.getString("maDatPhong"));
                pdp.setKhachHang(kh);
                
                HoaDon hd = new HoaDon(
                    rs.getString("maHoaDon"),
                    rs.getTimestamp("ngayLap").toLocalDateTime(),
                    rs.getDouble("thue"),
                    rs.getDouble("tongTienPhong"),
                    rs.getDouble("tongTienDichVu"),
                    pdp,
                    new NhanVien(rs.getString("maNhanVien"))
                );
                ds.add(hd);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ds;
    }

    public HoaDon getHoaDonByMaPhieu(String maPhieu) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT * FROM HoaDon WHERE maDatPhong = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPhieu);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new HoaDon(
                    rs.getString("maHoaDon"),
                    rs.getTimestamp("ngayLap").toLocalDateTime(),
                    rs.getDouble("thue"),
                    rs.getDouble("tongTienPhong"),
                    rs.getDouble("tongTienDichVu"),
                    new PhieuDatPhong(rs.getString("maDatPhong")),
                    new NhanVien(rs.getString("maNhanVien"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateTongTien(String maHD, double themTienPhong, double themTienDV) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "UPDATE HoaDon SET tongTienPhong = tongTienPhong + ?, tongTienDichVu = tongTienDichVu + ? WHERE maHoaDon = ?";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setDouble(1, themTienPhong);
            stmt.setDouble(2, themTienDV);
            stmt.setString(3, maHD);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean thanhToan(HoaDon hd, List<ChiTietHoaDon> dsChiTiet) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmtHeader = null;
        PreparedStatement stmtDetail = null;
        PreparedStatement stmtPhong = null;
        
        try {
            con.setAutoCommit(false);

            // 1. Lưu Hóa đơn
            stmtHeader = con.prepareStatement("INSERT INTO HoaDon VALUES(?, ?, ?, ?, ?, ?, ?)");
            stmtHeader.setString(1, hd.getMaHoaDon());
            stmtHeader.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap()));
            stmtHeader.setDouble(3, hd.getThue());
            stmtHeader.setDouble(4, hd.getTongTienPhong());
            stmtHeader.setDouble(5, hd.getTongTienDichVu());
            stmtHeader.setString(6, hd.getPhieuDatPhong().getMaDatPhong());
            stmtHeader.setString(7, hd.getNhanVien().getMaNhanVien());
            stmtHeader.executeUpdate();

            // 2. Lưu Chi tiết Dịch vụ
            if (dsChiTiet != null && !dsChiTiet.isEmpty()) {
                stmtDetail = con.prepareStatement("INSERT INTO ChiTietHoaDon VALUES(?, ?, ?, ?)");
                for (ChiTietHoaDon ct : dsChiTiet) {
                    stmtDetail.setString(1, hd.getMaHoaDon());
                    stmtDetail.setString(2, ct.getDichVu().getMaDichVu());
                    stmtDetail.setInt(3, ct.getSoLuong());
                    stmtDetail.setDouble(4, ct.getDonGiaLuuTru());
                    stmtDetail.executeUpdate();
                }
            }

            // 3. (Đã xóa) Cập nhật trạng thái các phòng và phiếu đặt được thực hiện tại RoomView để hỗ trợ trả từng phòng

            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("LỖI THANH TOÁN: " + e.getMessage());
            e.printStackTrace(); 
            try { con.rollback(); } catch (SQLException e1) { e1.printStackTrace(); }
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
        return false;
    }

    public boolean create(HoaDon hd) {
        Connection con = ConnectDB.getConnection();
        PreparedStatement stmt = null;
        int n = 0;
        try {
            stmt = con.prepareStatement("INSERT INTO HoaDon VALUES(?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, hd.getMaHoaDon());
            stmt.setTimestamp(2, Timestamp.valueOf(hd.getNgayLap()));
            stmt.setDouble(3, hd.getThue());
            stmt.setDouble(4, hd.getTongTienPhong());
            stmt.setDouble(5, hd.getTongTienDichVu());
            stmt.setString(6, hd.getPhieuDatPhong().getMaDatPhong());
            stmt.setString(7, hd.getNhanVien().getMaNhanVien());
            n = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return n > 0;
    }
}
