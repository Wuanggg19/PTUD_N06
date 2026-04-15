package gui;

import dao.KhachHangDAO;
import entity.KhachHang;

import java.util.List;

public class KhachHangService {

    private final KhachHangDAO dao = new KhachHangDAO();

    public List<KhachHang> getAll() {
        return dao.getAllKhachHang();
    }

    public List<KhachHang> searchByMa(String keyword) {
        String normalized = nullSafe(keyword).trim().toLowerCase();
        return getAll().stream()
                .filter(kh -> nullSafe(kh.getMaKhachHang()).toLowerCase().contains(normalized))
                .toList();
    }

    public List<KhachHang> searchByPhone(String keyword) {
        String normalized = nullSafe(keyword).trim();
        return getAll().stream()
                .filter(kh -> nullSafe(kh.getSoDienThoai()).contains(normalized))
                .toList();
    }

    public KhachHang findByPhone(String phone) {
        String normalized = nullSafe(phone).trim();
        return getAll().stream()
                .filter(kh -> normalized.equals(nullSafe(kh.getSoDienThoai()).trim()))
                .findFirst()
                .orElse(null);
    }

    public boolean create(KhachHang kh) {
        return dao.create(kh);
    }

    public boolean update(KhachHang kh) {
        return dao.update(kh);
    }

    public boolean delete(String maKhachHang) {
        return dao.delete(maKhachHang);
    }

    public String generateNextId() {
        return dao.generateNextId();
    }

    public boolean hasChanges(KhachHang original, KhachHang updated) {
        if (!nullSafe(original.getTenKhachHang()).trim()
                .equals(nullSafe(updated.getTenKhachHang()).trim()))
            return true;
        if (!nullSafe(original.getDiaChi()).trim()
                .equals(nullSafe(updated.getDiaChi()).trim()))
            return true;
        if (!nullSafe(original.getSoDienThoai()).trim()
                .equals(nullSafe(updated.getSoDienThoai()).trim()))
            return true;
        if (original.isGioiTinh() != updated.isGioiTinh())
            return true;
        return false;
    }

    public String validateTen(String ten) {
        if (ten == null || ten.trim().isEmpty())
            return "Tên khách hàng không được để trống!";
        return null;
    }

    public String validateSdt(String sdt) {
        if (sdt == null || sdt.trim().isEmpty())
            return "Số điện thoại không được để trống!";
        if (!sdt.matches("^0[0-9]{9}$"))
            return "Số điện thoại không hợp lệ!\n(Phải có 10 chữ số, bắt đầu bằng 0)";
        return null;
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
