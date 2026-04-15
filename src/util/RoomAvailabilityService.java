package util;

import dao.ChiTietPhieuDatDAO;
import entity.ChiTietPhieuDat;
import entity.Phong;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RoomAvailabilityService {
    private final ChiTietPhieuDatDAO chiTietPhieuDatDAO;

    public RoomAvailabilityService() {
        this(new ChiTietPhieuDatDAO());
    }

    public RoomAvailabilityService(ChiTietPhieuDatDAO chiTietPhieuDatDAO) {
        this.chiTietPhieuDatDAO = chiTietPhieuDatDAO;
    }

    public RoomDisplayStatus getRoomStatusOnDate(Phong room, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();
        if (room == null) {
            return RoomDisplayStatus.TRONG;
        }
        if (StatusUtils.isRoomStatus(room.getTrangThai(), RoomStatus.SUA_CHUA)) {
            return RoomDisplayStatus.SUA_CHUA;
        }

        List<ChiTietPhieuDat> bookings = chiTietPhieuDatDAO.getChiTietByRoom(room.getMaPhong());

        boolean occupied = bookings.stream().anyMatch(detail ->
                detail.getPhieuDatPhong() != null
                        && StatusUtils.isBookingStatus(detail.getPhieuDatPhong().getTrangThai(), BookingStatus.DA_NHAN_PHONG)
                        && isDateInStayRange(targetDate, detail)
        );
        if (occupied) {
            return RoomDisplayStatus.DANG_O;
        }

        boolean booked = bookings.stream().anyMatch(detail ->
                detail.getPhieuDatPhong() != null
                        && StatusUtils.isBookingStatus(detail.getPhieuDatPhong().getTrangThai(), BookingStatus.CHO_XAC_NHAN)
                        && isDateInStayRange(targetDate, detail)
        );
        if (booked) {
            return RoomDisplayStatus.DA_DAT;
        }

        return RoomDisplayStatus.TRONG;
    }

    public List<Phong> filterRoomsByFloor(List<Phong> rooms, Integer floor) {
        if (rooms == null) {
            return List.of();
        }
        return rooms.stream()
                .filter(room -> floor == null || room.getTang() == floor)
                .sorted(Comparator.comparingInt(Phong::getTang).thenComparing(Phong::getMaPhong))
                .collect(Collectors.toList());
    }

    private boolean isDateInStayRange(LocalDate date, ChiTietPhieuDat detail) {
        if (detail == null || detail.getNgayNhan() == null || detail.getNgayTra() == null) {
            return false;
        }
        LocalDate checkInDate = detail.getNgayNhan().toLocalDate();
        LocalDate checkOutDate = detail.getNgayTra().toLocalDate();
        if (!checkOutDate.isAfter(checkInDate)) {
            return date.equals(checkInDate);
        }
        return !date.isBefore(checkInDate) && date.isBefore(checkOutDate);
    }
}
