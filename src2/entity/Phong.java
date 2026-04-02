package entity;

import java.util.Objects;

public class Phong {
    private String maPhong;
    private String loaiPhong;
    private int soGiuong;
    private String trangThai;
    private double giaPhong;

    public Phong() {}

    public Phong(String maPhong) {
        this.maPhong = maPhong;
    }

    public Phong(String maPhong, String loaiPhong, int soGiuong, String trangThai, double giaPhong) {
        this.maPhong = maPhong;
        this.loaiPhong = loaiPhong;
        this.soGiuong = soGiuong;
        this.trangThai = trangThai;
        this.giaPhong = giaPhong;
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { 
        if (!maPhong.matches("^P\\d{3,}$")) {
            throw new IllegalArgumentException("Mã phòng phải bắt đầu bằng P và theo sau là ít nhất 3 chữ số");
        }
        this.maPhong = maPhong; 
    }

    public String getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(String loaiPhong) { 
        if (loaiPhong == null || loaiPhong.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại phòng không được rỗng");
        }
        this.loaiPhong = loaiPhong; 
    }

    public int getSoGiuong() { return soGiuong; }
    public void setSoGiuong(int soGiuong) { 
        if (soGiuong <= 0) {
            throw new IllegalArgumentException("Số giường phải > 0");
        }
        this.soGiuong = soGiuong; 
    }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { 
        if (trangThai == null || trangThai.trim().isEmpty()) {
            throw new IllegalArgumentException("Trạng thái không được rỗng");
        }
        this.trangThai = trangThai; 
    }

    public double getGiaPhong() { return giaPhong; }
    public void setGiaPhong(double giaPhong) { 
        if (giaPhong < 0) {
            throw new IllegalArgumentException("Giá phòng phải >= 0");
        }
        this.giaPhong = giaPhong; 
    }

    @Override
    public int hashCode() {
        return Objects.hash(maPhong);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Phong other = (Phong) obj;
        return Objects.equals(maPhong, other.maPhong);
    }

    @Override
    public String toString() {
        return "Phong [maPhong=" + maPhong + ", loaiPhong=" + loaiPhong + ", trangThai=" + trangThai + "]";
    }
}
