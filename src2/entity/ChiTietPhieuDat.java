package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class ChiTietPhieuDat {
    private PhieuDatPhong phieuDatPhong;
    private Phong phong;
    private double giaThuePhong;
    private LocalDateTime ngayNhan;
    private LocalDateTime ngayTra;

    public ChiTietPhieuDat() {}

    public ChiTietPhieuDat(PhieuDatPhong phieuDatPhong, Phong phong, double giaThuePhong, LocalDateTime ngayNhan, LocalDateTime ngayTra) {
        this.phieuDatPhong = phieuDatPhong;
        this.phong = phong;
        this.giaThuePhong = giaThuePhong;
        this.ngayNhan = ngayNhan;
        this.ngayTra = ngayTra;
    }

    public PhieuDatPhong getPhieuDatPhong() { return phieuDatPhong; }
    public void setPhieuDatPhong(PhieuDatPhong phieuDatPhong) { this.phieuDatPhong = phieuDatPhong; }

    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }

    public double getGiaThuePhong() { return giaThuePhong; }
    public void setGiaThuePhong(double giaThuePhong) { 
        if (giaThuePhong < 0) throw new IllegalArgumentException("Giá thuê phòng phải >= 0");
        this.giaThuePhong = giaThuePhong; 
    }

    public LocalDateTime getNgayNhan() { return ngayNhan; }
    public void setNgayNhan(LocalDateTime ngayNhan) { this.ngayNhan = ngayNhan; }

    public LocalDateTime getNgayTra() { return ngayTra; }
    public void setNgayTra(LocalDateTime ngayTra) { 
        if (ngayTra != null && ngayNhan != null && ngayTra.isBefore(ngayNhan)) {
            throw new IllegalArgumentException("Ngày trả phải >= ngày nhận");
        }
        this.ngayTra = ngayTra; 
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiTietPhieuDat other = (ChiTietPhieuDat) obj;
        return Objects.equals(phieuDatPhong, other.phieuDatPhong) && Objects.equals(phong, other.phong);
    }

    @Override
    public int hashCode() {
        return Objects.hash(phieuDatPhong, phong);
    }
}
