package entity;

import java.time.LocalDate;
import java.util.Objects;

public class BangGiaHeader {
    private String maBangGia;
    private String tenBangGia;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String loaiNgay;

    public BangGiaHeader() {}

    public BangGiaHeader(String maBangGia) {
        this.maBangGia = maBangGia;
    }

    public BangGiaHeader(String maBangGia, String tenBangGia, LocalDate ngayBatDau, LocalDate ngayKetThuc, String loaiNgay) {
        this.maBangGia = maBangGia;
        this.tenBangGia = tenBangGia;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.loaiNgay = loaiNgay;
    }

    public String getMaBangGia() { return maBangGia; }
    public void setMaBangGia(String maBangGia) { this.maBangGia = maBangGia; }

    public String getTenBangGia() { return tenBangGia; }
    public void setTenBangGia(String tenBangGia) { this.tenBangGia = tenBangGia; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    public String getLoaiNgay() { return loaiNgay; }
    public void setLoaiNgay(String loaiNgay) { this.loaiNgay = loaiNgay; }

    @Override
    public int hashCode() {
        return Objects.hash(maBangGia);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BangGiaHeader other = (BangGiaHeader) obj;
        return Objects.equals(maBangGia, other.maBangGia);
    }
}
