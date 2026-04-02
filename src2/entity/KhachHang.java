package entity;

import java.util.Objects;

public class KhachHang {
    private String maKhachHang;
    private String tenKhachHang;
    private String diaChi;
    private boolean gioiTinh;
    private String soDienThoai;

    public KhachHang() {}

    public KhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public KhachHang(String maKhachHang, String tenKhachHang, String diaChi, boolean gioiTinh, String soDienThoai) {
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.diaChi = diaChi;
        this.gioiTinh = gioiTinh;
        this.soDienThoai = soDienThoai;
    }

    public String getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(String maKhachHang) { 
        if (!maKhachHang.matches("^KH\\d{3,}$")) {
            throw new IllegalArgumentException("Mã khách hàng phải bắt đầu bằng KH và theo sau là ít nhất 3 chữ số");
        }
        this.maKhachHang = maKhachHang; 
    }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { 
        if (tenKhachHang == null || tenKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được rỗng");
        }
        this.tenKhachHang = tenKhachHang; 
    }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { 
        if (diaChi == null || diaChi.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được rỗng");
        }
        this.diaChi = diaChi; 
    }

    public boolean isGioiTinh() { return gioiTinh; }
    public void setGioiTinh(boolean gioiTinh) { this.gioiTinh = gioiTinh; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    @Override
    public int hashCode() {
        return Objects.hash(maKhachHang);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        KhachHang other = (KhachHang) obj;
        return Objects.equals(maKhachHang, other.maKhachHang);
    }

    @Override
    public String toString() {
        return "KhachHang [maKhachHang=" + maKhachHang + ", tenKhachHang=" + tenKhachHang + "]";
    }
}
