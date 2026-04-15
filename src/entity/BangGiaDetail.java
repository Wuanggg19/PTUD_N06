package entity;

public class BangGiaDetail {
    private BangGiaHeader bangGia;
    private String loaiPhongApDung;
    private double giaTheoGio;
    private double giaQuaDem;
    private double giaTheoNgay;
    private double giaCuoiTuan;
    private double giaLe;
    private double phuThu;
    
    // Thêm trường phong để tương thích với các logic cũ nếu cần, 
    // nhưng thực tế SQL của bạn link qua loaiPhong
    private Phong phong; 

    public BangGiaDetail() {}

    public BangGiaDetail(BangGiaHeader bangGia, String loaiPhongApDung, double giaTheoGio, double giaQuaDem, double giaTheoNgay, double giaCuoiTuan, double giaLe, double phuThu) {
        this.bangGia = bangGia;
        this.loaiPhongApDung = loaiPhongApDung;
        this.giaTheoGio = giaTheoGio;
        this.giaQuaDem = giaQuaDem;
        this.giaTheoNgay = giaTheoNgay;
        this.giaCuoiTuan = giaCuoiTuan;
        this.giaLe = giaLe;
        this.phuThu = phuThu;
    }

    public BangGiaDetail(BangGiaHeader bangGia, Phong phong, String loaiPhongApDung, double giaPhongMoi) {
        this(
                bangGia,
                loaiPhongApDung,
                giaPhongMoi,
                giaPhongMoi,
                giaPhongMoi,
                giaPhongMoi,
                giaPhongMoi,
                0
        );
        this.phong = phong;
    }

    // Getter và Setter
    public BangGiaHeader getBangGia() { return bangGia; }
    public void setBangGia(BangGiaHeader bangGia) { this.bangGia = bangGia; }

    public String getLoaiPhongApDung() { return loaiPhongApDung; }
    public void setLoaiPhongApDung(String loaiPhongApDung) { this.loaiPhongApDung = loaiPhongApDung; }

    public double getGiaTheoGio() { return giaTheoGio; }
    public void setGiaTheoGio(double giaTheoGio) { this.giaTheoGio = giaTheoGio; }

    public double getGiaQuaDem() { return giaQuaDem; }
    public void setGiaQuaDem(double giaQuaDem) { this.giaQuaDem = giaQuaDem; }

    public double getGiaTheoNgay() { return giaTheoNgay; }
    public void setGiaTheoNgay(double giaTheoNgay) { this.giaTheoNgay = giaTheoNgay; }

    public double getGiaCuoiTuan() { return giaCuoiTuan; }
    public void setGiaCuoiTuan(double giaCuoiTuan) { this.giaCuoiTuan = giaCuoiTuan; }

    public double getGiaLe() { return giaLe; }
    public void setGiaLe(double giaLe) { this.giaLe = giaLe; }

    public double getPhuThu() { return phuThu; }
    public void setPhuThu(double phuThu) { this.phuThu = phuThu; }

    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }
    
    // Để tương thích với các view cũ đang gọi getGiaPhongMoi()
    public double getGiaPhongMoi() {
        return giaTheoNgay;
    }
}
