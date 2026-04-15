package util;

public enum BookingStatus {
    CHO_XAC_NHAN("CHO_XAC_NHAN", "Chờ xác nhận"),
    DA_NHAN_PHONG("DA_NHAN_PHONG", "Đã nhận phòng"),
    DA_THANH_TOAN("DA_THANH_TOAN", "Đã thanh toán"),
    DA_HUY("DA_HUY", "Đã hủy");

    private final String code;
    private final String displayName;

    BookingStatus(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }
}
