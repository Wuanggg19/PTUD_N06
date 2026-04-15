package entity;

import java.time.LocalDateTime;
import java.util.Objects;

public class PhieuDatPhong {
    private String maDatPhong;
    private LocalDateTime ngayDat;
    private String trangThai;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private LocalDateTime ngayNhan;
    private LocalDateTime ngayTra;
    private String dsMaPhong;
    private int soLuongPhong;

    public PhieuDatPhong() {
    }

    public PhieuDatPhong(String maDatPhong) {
        this.maDatPhong = maDatPhong;
    }

    public PhieuDatPhong(String maDatPhong, LocalDateTime ngayDat, String trangThai, NhanVien nhanVien, KhachHang khachHang) {
        setMaDatPhong(maDatPhong);
        setNgayDat(ngayDat);
        this.trangThai = trangThai;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
    }

    public String getMaDatPhong() { return maDatPhong; }

    public void setMaDatPhong(String maDatPhong) {
        if (maDatPhong == null || !maDatPhong.matches("^DP\\d{3,}$")) {
            throw new IllegalArgumentException("Mã đặt phòng phải bắt đầu bằng DP và theo sau là ít nhất 3 chữ số");
        }
        this.maDatPhong = maDatPhong;
    }

    public LocalDateTime getNgayDat() { return ngayDat; }
    public void setNgayDat(LocalDateTime ngayDat) { this.ngayDat = ngayDat; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public LocalDateTime getNgayNhan() { return ngayNhan; }
    public void setNgayNhan(LocalDateTime ngayNhan) { this.ngayNhan = ngayNhan; }

    public LocalDateTime getNgayTra() { return ngayTra; }
    public void setNgayTra(LocalDateTime ngayTra) { this.ngayTra = ngayTra; }

    public String getDsMaPhong() { return dsMaPhong; }
    public void setDsMaPhong(String dsMaPhong) { this.dsMaPhong = dsMaPhong; }

    public int getSoLuongPhong() { return soLuongPhong; }
    public void setSoLuongPhong(int soLuongPhong) { this.soLuongPhong = soLuongPhong; }

    @Override
    public int hashCode() {
        return Objects.hash(maDatPhong);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PhieuDatPhong other = (PhieuDatPhong) obj;
        return Objects.equals(maDatPhong, other.maDatPhong);
    }
}
