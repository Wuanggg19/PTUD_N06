package util;

public enum RoomDisplayStatus {
    TRONG("Trống", AppTheme.INFO),
    DA_DAT("Đã đặt", AppTheme.WARNING),
    DANG_O("Đang ở", AppTheme.PRIMARY),
    SUA_CHUA("Sửa chữa", AppTheme.MUTED);

    private final String label;
    private final String color;

    RoomDisplayStatus(String label, String color) {
        this.label = label;
        this.color = color;
    }

    public String getLabel() {
        return label;
    }

    public String getColor() {
        return color;
    }
}
