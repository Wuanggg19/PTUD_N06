package entity;

import java.util.Objects;

public class ChiTietHoaDon {
    private HoaDon hoaDon;
    private DichVu dichVu;
    private int soLuong;
    private double donGiaLuuTru;

    public ChiTietHoaDon() {}

    public ChiTietHoaDon(HoaDon hoaDon, DichVu dichVu, int soLuong, double donGiaLuuTru) {
        this.hoaDon = hoaDon;
        this.dichVu = dichVu;
        this.soLuong = soLuong;
        this.donGiaLuuTru = donGiaLuuTru;
    }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public DichVu getDichVu() { return dichVu; }
    public void setDichVu(DichVu dichVu) { this.dichVu = dichVu; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { 
        if (soLuong <= 0) throw new IllegalArgumentException("Số lượng phải > 0");
        this.soLuong = soLuong; 
    }

    public double getDonGiaLuuTru() { return donGiaLuuTru; }
    public void setDonGiaLuuTru(double donGiaLuuTru) { 
        if (donGiaLuuTru < 0) throw new IllegalArgumentException("Đơn giá lưu trữ phải >= 0");
        this.donGiaLuuTru = donGiaLuuTru; 
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChiTietHoaDon other = (ChiTietHoaDon) obj;
        return Objects.equals(hoaDon, other.hoaDon) && Objects.equals(dichVu, other.dichVu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hoaDon, dichVu);
    }
}
