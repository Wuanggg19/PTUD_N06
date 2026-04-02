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
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuDatPhong;
import entity.ChiTietPhieuDat;
import entity.Phong;

public class PhieuDatPhongDAO {

    public PhieuDatPhong getActivePhieuByMaPhong(String maPhong) {
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT pdp.*, kh.tenKhachHang, kh.soDienThoai, kh.diaChi, kh.gioiTinh FROM PhieuDatPhong pdp " +
                         "JOIN KhachHang kh ON pdp.maKhachHang = kh.maKhachHang " +
                         "JOIN ChiTietPhieuDat ct ON pdp.maDatPhong = ct.maDatPhong " +
                         "WHERE ct.maPhong = ? AND pdp.trangThai = N'DaNhanPhong'";
            PreparedStatement stmt = con.prepareStatement(sql);
            stmt.setString(1, maPhong);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                KhachHang kh = new KhachHang(
                    rs.getString("maKhachHang"),
                    rs.getString("tenKhachHang"),
                    rs.getString("diaChi"),
                    rs.getBoolean("gioiTinh"),
                    rs.getString("soDienThoai")
                );
                return new PhieuDatPhong(
                    rs.getString("maDatPhong"),
                    rs.getTimestamp("ngayDat").toLocalDateTime(),
                    rs.getString("trangThai"),
                    new NhanVien(rs.getString("maNhanVien")),
                    kh
                );
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public List<PhieuDatPhong> getAllPhieuDatPhong() {
        List<PhieuDatPhong> ds = new ArrayList<>();
        try {
            Connection con = ConnectDB.getConnection();
            String sql = "SELECT pdp.*, kh.tenKhachHang, kh.soDienThoai, kh.diaChi, kh.gioiTinh, " +
                         "(SELECT STRING_AGG(maPhong, ', ') FROM ChiTietPhieuDat ct WHERE ct.maDatPhong = pdp.maDatPhong) as dsMaPhong " +
                         "FROM PhieuDatPhong pdp " +
                         "JOIN KhachHang kh ON pdp.maKhachHang = kh.maKhachHang";
            
            Statement statement = con.createStatement();
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                KhachHang kh = new KhachHang(
                    rs.getString("maKhachHang"),
                    rs.getString("tenKhachHang"),
                    rs.getString("diaChi"),
                    rs.getBoolean("gioiTinh"),
                    rs.getString("soDienThoai")
                );

                PhieuDatPhong pdp = new PhieuDatPhong(
                    rs.getString("maDatPhong"),
                    rs.getTimestamp("ngayDat").toLocalDateTime(),
                    rs.getString("trangThai"),
                    new NhanVien(rs.getString("maNhanVien")),
                    kh
                );
                pdp.setTrangThai(rs.getString("dsMaPhong") + " | " + rs.getString("trangThai"));
                ds.add(pdp);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return ds;
    }

    public boolean doiPhong(String maPhieu, String maPhongCu, String maPhongMoi, double giaMoi) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            Timestamp now = new Timestamp(System.currentTimeMillis());

            // 1. Chốt phòng cũ: Cập nhật ngày trả là ngay bây giờ
            String sqlChotPhongCu = "UPDATE ChiTietPhieuDat SET ngayTra = ? WHERE maDatPhong = ? AND maPhong = ?";
            PreparedStatement st1 = con.prepareStatement(sqlChotPhongCu);
            st1.setTimestamp(1, now);
            st1.setString(2, maPhieu);
            st1.setString(3, maPhongCu);
            st1.executeUpdate();

            // 2. Trả phòng cũ về trạng thái Trống
            String sqlTraPhongCu = "UPDATE Phong SET trangThai = N'Trống' WHERE maPhong = ?";
            PreparedStatement st2 = con.prepareStatement(sqlTraPhongCu);
            st2.setString(1, maPhongCu);
            st2.executeUpdate();

            // 3. Mở phòng mới: Thêm bản ghi chi tiết mới
            String sqlMoPhongMoi = "INSERT INTO ChiTietPhieuDat (maDatPhong, maPhong, giaThuePhong, ngayNhan, ngayTra) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement st3 = con.prepareStatement(sqlMoPhongMoi);
            st3.setString(1, maPhieu);
            st3.setString(2, maPhongMoi);
            st3.setDouble(3, giaMoi);
            st3.setTimestamp(4, now);
            // Ngày trả dự kiến lấy tạm là +1 ngày, sẽ cập nhật khi thanh toán thực tế
            st3.setTimestamp(5, new Timestamp(System.currentTimeMillis() + 86400000)); 
            st3.executeUpdate();

            // 4. Chuyển phòng mới sang trạng thái Đang ở
            String sqlNhanPhongMoi = "UPDATE Phong SET trangThai = N'Đang ở' WHERE maPhong = ?";
            PreparedStatement st4 = con.prepareStatement(sqlNhanPhongMoi);
            st4.setString(1, maPhongMoi);
            st4.executeUpdate();

            con.commit();
            return true;
        } catch (SQLException e) {
            try { con.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (Exception ex) {}
        }
    }

    public boolean datPhong(PhieuDatPhong pdp, List<ChiTietPhieuDat> dsChiTiet) {
        Connection con = ConnectDB.getConnection();
        if (con == null) return false;
        
        try {
            con.setAutoCommit(false);
            
            PreparedStatement stmtHeader = con.prepareStatement("INSERT INTO PhieuDatPhong VALUES(?, ?, ?, ?, ?)");
            stmtHeader.setString(1, pdp.getMaDatPhong());
            stmtHeader.setTimestamp(2, Timestamp.valueOf(pdp.getNgayDat()));
            stmtHeader.setString(3, pdp.getTrangThai());
            stmtHeader.setString(4, pdp.getNhanVien().getMaNhanVien());
            stmtHeader.setString(5, pdp.getKhachHang().getMaKhachHang());
            stmtHeader.executeUpdate();

            PreparedStatement stmtDetail = con.prepareStatement("INSERT INTO ChiTietPhieuDat VALUES(?, ?, ?, ?, ?)");
            PreparedStatement stmtPhong = con.prepareStatement("UPDATE Phong SET trangThai = N'Đang ở' WHERE maPhong = ?");

            for (ChiTietPhieuDat ct : dsChiTiet) {
                stmtDetail.setString(1, pdp.getMaDatPhong());
                stmtDetail.setString(2, ct.getPhong().getMaPhong());
                stmtDetail.setDouble(3, ct.getGiaThuePhong());
                stmtDetail.setTimestamp(4, Timestamp.valueOf(ct.getNgayNhan()));
                stmtDetail.setTimestamp(5, Timestamp.valueOf(ct.getNgayTra()));
                stmtDetail.executeUpdate();
                
                stmtPhong.setString(1, ct.getPhong().getMaPhong());
                stmtPhong.executeUpdate();
            }
            
            con.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("SQL Error in datPhong: " + e.getMessage());
            try { if (con != null) con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try { if (con != null) con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    public boolean huyPhieu(String maPhieu) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            
            // 1. Lấy danh sách phòng trong phiếu
            List<entity.ChiTietPhieuDat> dsCT = new dao.ChiTietPhieuDatDAO().getDSChiTietByMaPhieu(maPhieu);
            
            // 2. Cập nhật trạng thái các phòng về "Trống"
            PreparedStatement stmtPhong = con.prepareStatement("UPDATE Phong SET trangThai = N'Trống' WHERE maPhong = ?");
            for (entity.ChiTietPhieuDat ct : dsCT) {
                stmtPhong.setString(1, ct.getPhong().getMaPhong());
                stmtPhong.executeUpdate();
            }
            
            // 3. Cập nhật trạng thái phiếu về "DaHuy"
            PreparedStatement stmtPhieu = con.prepareStatement("UPDATE PhieuDatPhong SET trangThai = N'DaHuy' WHERE maDatPhong = ?");
            stmtPhieu.setString(1, maPhieu);
            stmtPhieu.executeUpdate();
            
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { con.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }

    public boolean checkIn(String maPhieu) {
        Connection con = ConnectDB.getConnection();
        try {
            con.setAutoCommit(false);
            
            // 1. Cập nhật phiếu sang DaNhanPhong
            PreparedStatement stmtPhieu = con.prepareStatement("UPDATE PhieuDatPhong SET trangThai = N'DaNhanPhong' WHERE maDatPhong = ?");
            stmtPhieu.setString(1, maPhieu);
            stmtPhieu.executeUpdate();
            
            // 2. Lấy danh sách phòng và chuyển sang "Đang ở"
            List<entity.ChiTietPhieuDat> dsCT = new dao.ChiTietPhieuDatDAO().getDSChiTietByMaPhieu(maPhieu);
            PreparedStatement stmtPhong = con.prepareStatement("UPDATE Phong SET trangThai = N'Đang ở' WHERE maPhong = ?");
            for (entity.ChiTietPhieuDat ct : dsCT) {
                stmtPhong.setString(1, ct.getPhong().getMaPhong());
                stmtPhong.executeUpdate();
            }
            
            con.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { con.rollback(); } catch (SQLException ex) {}
            return false;
        } finally {
            try { con.setAutoCommit(true); } catch (SQLException e) {}
        }
    }
}
