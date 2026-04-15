package entity;

public class ChiTietBangGia {
    private BangGiaHeader bangGia;
    private String maLoai;
    private double giaTheoGio;
    private double giaQuaDem;
    private double giaTheoNgay;
    private double giaCuoiTuan;
    private double giaLe;
    private double phuThu;

    public ChiTietBangGia() {
    }

    public ChiTietBangGia(BangGiaHeader bangGia, String maLoai, double giaTheoGio, double giaQuaDem,
                          double giaTheoNgay, double giaCuoiTuan, double giaLe, double phuThu) {
        this.bangGia = bangGia;
        this.maLoai = maLoai;
        this.giaTheoGio = giaTheoGio;
        this.giaQuaDem = giaQuaDem;
        this.giaTheoNgay = giaTheoNgay;
        this.giaCuoiTuan = giaCuoiTuan;
        this.giaLe = giaLe;
        this.phuThu = phuThu;
    }

    public BangGiaHeader getBangGia() {
        return bangGia;
    }

    public void setBangGia(BangGiaHeader bangGia) {
        this.bangGia = bangGia;
    }

    public String getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(String maLoai) {
        this.maLoai = maLoai;
    }

    public double getGiaTheoGio() {
        return giaTheoGio;
    }

    public void setGiaTheoGio(double giaTheoGio) {
        this.giaTheoGio = giaTheoGio;
    }

    public double getGiaQuaDem() {
        return giaQuaDem;
    }

    public void setGiaQuaDem(double giaQuaDem) {
        this.giaQuaDem = giaQuaDem;
    }

    public double getGiaTheoNgay() {
        return giaTheoNgay;
    }

    public void setGiaTheoNgay(double giaTheoNgay) {
        this.giaTheoNgay = giaTheoNgay;
    }

    public double getGiaCuoiTuan() {
        return giaCuoiTuan;
    }

    public void setGiaCuoiTuan(double giaCuoiTuan) {
        this.giaCuoiTuan = giaCuoiTuan;
    }

    public double getGiaLe() {
        return giaLe;
    }

    public void setGiaLe(double giaLe) {
        this.giaLe = giaLe;
    }

    public double getPhuThu() {
        return phuThu;
    }

    public void setPhuThu(double phuThu) {
        this.phuThu = phuThu;
    }
}
