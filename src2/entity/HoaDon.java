package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class HoaDon {
    private String maHoaDon;
    private LocalDateTime ngayLap;
    private double thue;
    private double tongTienPhong;
    private double tongTienDichVu;
    private PhieuDatPhong phieuDatPhong;
    private NhanVien nhanVien;

    public HoaDon() {}

    public HoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public HoaDon(String maHoaDon, LocalDateTime ngayLap, double thue, double tongTienPhong, double tongTienDichVu, PhieuDatPhong phieuDatPhong, NhanVien nhanVien) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.thue = thue;
        this.tongTienPhong = tongTienPhong;
        this.tongTienDichVu = tongTienDichVu;
        this.phieuDatPhong = phieuDatPhong;
        this.nhanVien = nhanVien;
    }

    public String getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(String maHoaDon) { 
        if (!maHoaDon.matches("^HD\\d{3,}$")) {
            throw new IllegalArgumentException("Mã hóa đơn phải bắt đầu bằng HD và theo sau là ít nhất 3 chữ số");
        }
        this.maHoaDon = maHoaDon; 
    }

    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }

    public double getThue() { return thue; }
    public void setThue(double thue) { this.thue = thue; }

    public double getTongTienPhong() { return tongTienPhong; }
    public void setTongTienPhong(double tongTienPhong) { this.tongTienPhong = tongTienPhong; }

    public double getTongTienDichVu() { return tongTienDichVu; }
    public void setTongTienDichVu(double tongTienDichVu) { this.tongTienDichVu = tongTienDichVu; }

    public PhieuDatPhong getPhieuDatPhong() { return phieuDatPhong; }
    public void setPhieuDatPhong(PhieuDatPhong phieuDatPhong) { this.phieuDatPhong = phieuDatPhong; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    @Override
    public int hashCode() {
        return Objects.hash(maHoaDon);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HoaDon other = (HoaDon) obj;
        return Objects.equals(maHoaDon, other.maHoaDon);
    }
}
