package util;

import dao.BangGiaDetailDAO;
import dao.ChiTietHoaDonDAO;
import entity.ChiTietHoaDon;
import entity.ChiTietPhieuDat;
import entity.Phong;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

public class PricingService {
    private final BangGiaDetailDAO bangGiaDetailDAO;
    private final ChiTietHoaDonDAO chiTietHoaDonDAO;

    public PricingService() {
        this(new BangGiaDetailDAO(), new ChiTietHoaDonDAO());
    }

    public PricingService(BangGiaDetailDAO bangGiaDetailDAO, ChiTietHoaDonDAO chiTietHoaDonDAO) {
        this.bangGiaDetailDAO = bangGiaDetailDAO;
        this.chiTietHoaDonDAO = chiTietHoaDonDAO;
    }

    public double getAppliedPriceByDate(Phong phong, LocalDate ngay) {
        if (phong == null || ngay == null) {
            return 0;
        }
        double price = bangGiaDetailDAO.getGiaTheoNgay(phong, ngay);
        // Nếu không có bảng giá cho ngày này, lấy giá gốc của phòng
        return price > 0 ? price : phong.getGiaPhong();
    }

    public double getAppliedPriceForRoomAndDate(Phong phong, LocalDate ngay) {
        return getAppliedPriceByDate(phong, ngay);
    }

    public double calculateEstimatedBookingPrice(Phong phong, LocalDate fromDate, LocalDate toDate) {
        if (phong == null || fromDate == null) {
            return 0;
        }

        if (toDate == null || !toDate.isAfter(fromDate)) {
            return getAppliedPriceByDate(phong, fromDate);
        }

        double total = 0;
        for (LocalDate date = fromDate; date.isBefore(toDate); date = date.plusDays(1)) {
            total += getAppliedPriceByDate(phong, date);
        }
        return total;
    }

    public double calculateRoomCharge(ChiTietPhieuDat chiTiet, LocalDateTime checkoutTime) {
        if (chiTiet == null || chiTiet.getPhong() == null || chiTiet.getNgayNhan() == null) {
            return 0;
        }

        LocalDateTime effectiveCheckout = checkoutTime != null ? checkoutTime : LocalDateTime.now();
        if (effectiveCheckout.isBefore(chiTiet.getNgayNhan())) {
            effectiveCheckout = chiTiet.getNgayNhan();
        }

        LocalDate startDate = chiTiet.getNgayNhan().toLocalDate();
        LocalDate endDateExclusive = effectiveCheckout.toLocalDate();
        
        // Nếu trả cùng ngày nhận, hoặc trả trước 12h ngày hôm sau mà vẫn tính 1 ngày
        if (endDateExclusive.isBefore(startDate) || endDateExclusive.equals(startDate)) {
            endDateExclusive = startDate.plusDays(1);
        }

        double total = 0;
        for (LocalDate date = startDate; date.isBefore(endDateExclusive); date = date.plusDays(1)) {
            total += getAppliedPriceByDate(chiTiet.getPhong(), date);
        }
        return total;
    }

    public double calculateRoomCharge(List<ChiTietPhieuDat> chiTietList, LocalDateTime checkoutTime) {
        if (chiTietList == null) {
            return 0;
        }
        return chiTietList.stream()
                .mapToDouble(ct -> calculateRoomCharge(ct, checkoutTime))
                .sum();
    }

    public long countChargeDays(ChiTietPhieuDat chiTiet, LocalDateTime checkoutTime) {
        if (chiTiet == null || chiTiet.getNgayNhan() == null) {
            return 0;
        }
        LocalDateTime effectiveCheckout = checkoutTime != null ? checkoutTime : LocalDateTime.now();
        LocalDate start = chiTiet.getNgayNhan().toLocalDate();
        LocalDate end = effectiveCheckout.toLocalDate();
        long days = ChronoUnit.DAYS.between(start, end);
        return Math.max(1, days + 1);
    }

    public double calculateServiceCharge(String maHoaDon) {
        if (maHoaDon == null || maHoaDon.isBlank()) {
            return 0;
        }
        List<ChiTietHoaDon> details = chiTietHoaDonDAO.getByMaHoaDon(maHoaDon);
        if (details == null) {
            details = Collections.emptyList();
        }
        return details.stream()
                .mapToDouble(item -> item.getSoLuong() * item.getDonGiaLuuTru())
                .sum();
    }
}
