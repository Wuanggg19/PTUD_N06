package entity;

import java.util.Objects;

public class BangGiaDetail {
    private BangGiaHeader bangGia;
    private Phong phong;
    private double giaPhongMoi;

    public BangGiaDetail() {}

    public BangGiaDetail(BangGiaHeader bangGia, Phong phong, double giaPhongMoi) {
        this.bangGia = bangGia;
        this.phong = phong;
        this.giaPhongMoi = giaPhongMoi;
    }

    public BangGiaHeader getBangGia() { return bangGia; }
    public void setBangGia(BangGiaHeader bangGia) { this.bangGia = bangGia; }

    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }

    public double getGiaPhongMoi() { return giaPhongMoi; }
    public void setGiaPhongMoi(double giaPhongMoi) { this.giaPhongMoi = giaPhongMoi; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BangGiaDetail other = (BangGiaDetail) obj;
        return Objects.equals(bangGia, other.bangGia) && Objects.equals(phong, other.phong);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bangGia, phong);
    }
}
