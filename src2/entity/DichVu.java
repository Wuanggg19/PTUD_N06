package entity;

import java.util.Objects;

public class DichVu {
    private String maDichVu;
    private String tenDichVu;
    private double donGia;
    private String trangThai;

    public DichVu() {}

    public DichVu(String maDichVu) {
        this.maDichVu = maDichVu;
    }

    public DichVu(String maDichVu, String tenDichVu, double donGia, String trangThai) {
        this.maDichVu = maDichVu;
        this.tenDichVu = tenDichVu;
        this.donGia = donGia;
        this.trangThai = trangThai;
    }

    public String getMaDichVu() { return maDichVu; }
    public void setMaDichVu(String maDichVu) { 
        if (!maDichVu.matches("^DV\\d{3,}$")) {
            throw new IllegalArgumentException("Mã dịch vụ phải bắt đầu bằng DV và theo sau là ít nhất 3 chữ số");
        }
        this.maDichVu = maDichVu; 
    }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { 
        if (tenDichVu == null || tenDichVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được rỗng");
        }
        this.tenDichVu = tenDichVu; 
    }

    public double getDonGia() { return donGia; }
    public void setDonGia(double donGia) { 
        if (donGia < 0) {
            throw new IllegalArgumentException("Đơn giá phải >= 0");
        }
        this.donGia = donGia; 
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public int hashCode() {
        return Objects.hash(maDichVu);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DichVu other = (DichVu) obj;
        return Objects.equals(maDichVu, other.maDichVu);
    }

    @Override
    public String toString() {
        return "DichVu [maDichVu=" + maDichVu + ", tenDichVu=" + tenDichVu + "]";
    }
}
