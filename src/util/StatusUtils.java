package util;

import java.util.Arrays;

public final class StatusUtils {
    private StatusUtils() {
    }

    public static BookingStatus toBookingStatus(String raw) {
        String normalized = normalize(raw);
        if (normalized.contains("THANH_TOAN")) {
            return BookingStatus.DA_THANH_TOAN;
        }
        if (normalized.contains("HUY")) {
            return BookingStatus.DA_HUY;
        }
        if (normalized.contains("NHAN_PHONG") || normalized.contains("DANG_O")) {
            return BookingStatus.DA_NHAN_PHONG;
        }
        return BookingStatus.CHO_XAC_NHAN;
    }

    public static RoomStatus toRoomStatus(String raw) {
        String normalized = normalize(raw);
        if (normalized.contains("SUA_CHUA") || normalized.contains("BAO_TRI")) {
            return RoomStatus.SUA_CHUA;
        }
        if (normalized.contains("DANG_O") || normalized.contains("NHAN_PHONG")) {
            return RoomStatus.DANG_O;
        }
        if (normalized.contains("CHO_XAC_NHAN") || normalized.contains("DA_DAT")) {
            return RoomStatus.CHO_XAC_NHAN;
        }
        return RoomStatus.TRONG;
    }

    public static String bookingCode(String raw) {
        return toBookingStatus(raw).getCode();
    }

    public static String roomCode(String raw) {
        return toRoomStatus(raw).getCode();
    }

    public static String bookingLabel(String raw) {
        return toBookingStatus(raw).getDisplayName();
    }

    public static String roomLabel(String raw) {
        return toRoomStatus(raw).getDisplayName();
    }

    public static boolean isBookingStatus(String raw, BookingStatus expected) {
        return toBookingStatus(raw) == expected;
    }

    public static boolean isRoomStatus(String raw, RoomStatus expected) {
        return toRoomStatus(raw) == expected;
    }

    public static String normalize(String raw) {
        if (raw == null) {
            return "";
        }

        String normalized = raw.trim()
                .replace('Đ', 'D')
                .replace('đ', 'd')
                .replaceAll("[^\\p{L}\\p{Nd}]+", "_")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "")
                .toUpperCase();

        return Arrays.stream(normalized.split("_"))
                .filter(part -> !part.isBlank())
                .reduce((a, b) -> a + "_" + b)
                .orElse("");
    }
}
