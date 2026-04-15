package entity;

import java.time.LocalDate;
import java.util.Objects;

public class BangGiaHeader {
    public static final String LOAI_NGAY_THUONG = "NGAY_THUONG";
    public static final String LOAI_CUOI_TUAN = "CUOI_TUAN";
    public static final String LOAI_NGAY_LE = "NGAY_LE";

    public static final String TRANG_THAI_DANG_HOAT_DONG = "Đang hoạt động";
    public static final String TRANG_THAI_KHONG_HOAT_DONG = "Không hoạt động";

    private String maBangGia;
    private String tenBangGia;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private String loaiNgay;
    private String trangThai;
    private double phanTramTang;

    public BangGiaHeader() {
        this.trangThai = TRANG_THAI_DANG_HOAT_DONG;
        this.phanTramTang = 0;
    }

    public BangGiaHeader(String maBangGia) {
        this();
        this.maBangGia = maBangGia;
    }

    public BangGiaHeader(String maBangGia, String tenBangGia, LocalDate ngayBatDau, LocalDate ngayKetThuc,
            String loaiNgay) {
        this(maBangGia, tenBangGia, ngayBatDau, ngayKetThuc, loaiNgay, TRANG_THAI_DANG_HOAT_DONG, 0);
    }

    public BangGiaHeader(String maBangGia, String tenBangGia, LocalDate ngayBatDau, LocalDate ngayKetThuc,
            String loaiNgay, String trangThai, double phanTramTang) {
        this.maBangGia = maBangGia;
        this.tenBangGia = tenBangGia;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.loaiNgay = loaiNgay;
        this.trangThai = trangThai;
        this.phanTramTang = phanTramTang;
    }

    public String getMaBangGia() {
        return maBangGia;
    }

    public void setMaBangGia(String maBangGia) {
        this.maBangGia = maBangGia;
    }

    public String getTenBangGia() {
        return tenBangGia;
    }

    public void setTenBangGia(String tenBangGia) {
        this.tenBangGia = tenBangGia;
    }

    public LocalDate getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDate ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDate getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDate ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public String getLoaiNgay() {
        return loaiNgay;
    }

    public void setLoaiNgay(String loaiNgay) {
        this.loaiNgay = loaiNgay;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public double getPhanTramTang() {
        return phanTramTang;
    }

    public void setPhanTramTang(double phanTramTang) {
        this.phanTramTang = phanTramTang;
    }

    public boolean isDangHoatDong() {
        return TRANG_THAI_DANG_HOAT_DONG.equalsIgnoreCase(trangThai);
    }

    public boolean isDacBiet() {
        return LOAI_NGAY_LE.equalsIgnoreCase(loaiNgay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maBangGia);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof BangGiaHeader))
            return false;
        BangGiaHeader other = (BangGiaHeader) obj;
        return Objects.equals(maBangGia, other.maBangGia);
    }
}
