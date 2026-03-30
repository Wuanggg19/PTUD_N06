package entity;

import java.util.Objects;

public class NhanVien {
    private String maNhanVien;
    private String tenNhanVien;
    private String chucVu;
    private double luongNhanVien;
    private boolean phaiNhanVien;
    private String soDienThoai;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;

    public NhanVien() {}

    public NhanVien(String maNhanVien) {
        this.maNhanVien = maNhanVien;
    }

    public NhanVien(String maNhanVien, String tenNhanVien, String chucVu, double luongNhanVien, boolean phaiNhanVien, String soDienThoai, String tenDangNhap, String matKhau, String vaiTro) {
        setMaNhanVien(maNhanVien);
        setTenNhanVien(tenNhanVien);
        setChucVu(chucVu);
        setLuongNhanVien(luongNhanVien);
        this.phaiNhanVien = phaiNhanVien;
        setSoDienThoai(soDienThoai);
        setTenDangNhap(tenDangNhap);
        setMatKhau(matKhau);
        this.vaiTro = vaiTro;
    }

    public String getMaNhanVien() { return maNhanVien; }
    public void setMaNhanVien(String maNhanVien) {
        if (!maNhanVien.matches("^NV\\d{3,}$")) {
            throw new IllegalArgumentException("Mã nhân viên phải có định dạng NVxxx (Ví dụ: NV001)");
        }
        this.maNhanVien = maNhanVien;
    }

    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { 
        if (tenNhanVien == null || tenNhanVien.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhân viên không được rỗng");
        }
        this.tenNhanVien = tenNhanVien; 
    }

    public String getChucVu() { return chucVu; }
    public void setChucVu(String chucVu) { 
        if (chucVu == null || chucVu.trim().isEmpty()) {
            throw new IllegalArgumentException("Chức vụ không được rỗng");
        }
        this.chucVu = chucVu; 
    }

    public double getLuongNhanVien() { return luongNhanVien; }
    public void setLuongNhanVien(double luongNhanVien) { 
        if (luongNhanVien < 0) throw new IllegalArgumentException("Lương không được âm");
        this.luongNhanVien = luongNhanVien; 
    }

    public boolean isPhaiNhanVien() { return phaiNhanVien; }
    public void setPhaiNhanVien(boolean phaiNhanVien) { this.phaiNhanVien = phaiNhanVien; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { 
        if (!soDienThoai.matches("^0\\d{9,10}$")) {
            throw new IllegalArgumentException("Số điện thoại phải gồm 10 hoặc 11 chữ số và bắt đầu bằng số 0");
        }
        this.soDienThoai = soDienThoai; 
    }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) {
        if (tenDangNhap == null || tenDangNhap.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được rỗng");
        }
        this.tenDangNhap = tenDangNhap;
    }

    public String getMatKhau() { return matKhau; }
    public void setMatKhau(String matKhau) {
        if (matKhau == null || matKhau.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu phải từ 6 ký tự trở lên");
        }
        this.matKhau = matKhau;
    }

    public String getVaiTro() { return vaiTro; }
    public void setVaiTro(String vaiTro) { this.vaiTro = vaiTro; }

    @Override
    public int hashCode() {
        return Objects.hash(maNhanVien);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        NhanVien other = (NhanVien) obj;
        return Objects.equals(maNhanVien, other.maNhanVien);
    }
}
