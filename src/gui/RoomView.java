package gui;

import dao.PhongDAO;
import entity.Phong;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import util.AppTheme;
import util.RoomAvailabilityService;
import util.RoomDisplayStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class RoomView {
    private final PhongDAO phongDAO = new PhongDAO();
    private final RoomAvailabilityService roomAvailabilityService = new RoomAvailabilityService();

    private FlowPane roomContainer;
    private List<Phong> allRooms = new ArrayList<>();
    private final Map<String, RoomDisplayStatus> roomStatusCache = new HashMap<>();
    private ComboBox<String> cbType;
    private ComboBox<String> cbFloor;
    private DatePicker dpFilterDate;
    private TextField txtSearchMaPhong;
    private java.util.function.Consumer<Phong> onBookingRequest;
    private java.util.function.Consumer<Phong> onCheckoutRequest;
    private int statusLoadVersion;

    public void setOnBookingRequest(java.util.function.Consumer<Phong> callback) {
        this.onBookingRequest = callback;
    }

    public void setOnCheckoutRequest(java.util.function.Consumer<Phong> callback) {
        this.onCheckoutRequest = callback;
    }

    public Node createView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + AppTheme.BG_LIGHT + ";");

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Label lblTitle = new Label("SO DO PHONG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web(AppTheme.TEXT));

        txtSearchMaPhong = new TextField();
        txtSearchMaPhong.setPromptText("Tim ma phong...");
        txtSearchMaPhong.setPrefWidth(150);
        txtSearchMaPhong.textProperty().addListener((o, ov, nv) -> renderRooms());

        cbType = new ComboBox<>();
        cbType.getItems().add("Tat ca loai phong");
        cbType.setValue("Tat ca loai phong");
        cbType.setOnAction(e -> renderRooms());

        cbFloor = new ComboBox<>();
        cbFloor.getItems().add("Tat ca tang");
        cbFloor.setValue("Tat ca tang");
        cbFloor.setOnAction(e -> renderRooms());

        dpFilterDate = new DatePicker(LocalDate.now());
        dpFilterDate.setOnAction(e -> loadRoomStatusesAsync());

        javafx.scene.control.Button btnRefresh = new javafx.scene.control.Button("Lam moi");
        btnRefresh.setStyle("-fx-background-color: " + AppTheme.PRIMARY + "; -fx-text-fill: white; -fx-background-radius: 8;");
        btnRefresh.setOnAction(e -> loadData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().addAll(lblTitle, spacer, txtSearchMaPhong, cbType, cbFloor, dpFilterDate, btnRefresh);

        HBox legend = new HBox(18,
                createLegendItem(RoomDisplayStatus.TRONG),
                createLegendItem(RoomDisplayStatus.DA_DAT),
                createLegendItem(RoomDisplayStatus.DANG_O),
                createLegendItem(RoomDisplayStatus.SUA_CHUA)
        );

        roomContainer = new FlowPane(18, 18);
        roomContainer.setPadding(new Insets(10, 0, 10, 0));

        ScrollPane scrollPane = new ScrollPane(roomContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + AppTheme.BG_LIGHT + ";");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(header, legend, scrollPane);
        loadData();
        return root;
    }

    public void loadData() {
        Task<List<Phong>> task = new Task<>() {
            @Override
            protected List<Phong> call() {
                return phongDAO.getAllPhong();
            }
        };
        task.setOnSucceeded(e -> {
            allRooms = task.getValue() != null ? task.getValue() : new ArrayList<>();
            rebuildFilterOptions();
            loadRoomStatusesAsync();
        });
        startDaemonTask(task, "room-view-load-data");
    }

    private void loadRoomStatusesAsync() {
        int requestVersion = ++statusLoadVersion;
        LocalDate selectedDate = dpFilterDate != null && dpFilterDate.getValue() != null
                ? dpFilterDate.getValue()
                : LocalDate.now();

        roomContainer.getChildren().setAll(new Label("Dang tai trang thai phong..."));
        Task<Map<String, RoomDisplayStatus>> task = new Task<>() {
            @Override
            protected Map<String, RoomDisplayStatus> call() {
                Map<String, RoomDisplayStatus> statuses = new HashMap<>();
                for (Phong room : allRooms) {
                    statuses.put(room.getMaPhong(), roomAvailabilityService.getRoomStatusOnDate(room, selectedDate));
                }
                return statuses;
            }
        };
        task.setOnSucceeded(e -> {
            if (requestVersion != statusLoadVersion) {
                return;
            }
            roomStatusCache.clear();
            roomStatusCache.putAll(task.getValue());
            renderRooms();
        });
        task.setOnFailed(e -> {
            if (requestVersion == statusLoadVersion) {
                roomContainer.getChildren().setAll(new Label("Khong the tai trang thai phong."));
            }
            task.getException().printStackTrace();
        });
        startDaemonTask(task, "room-view-load-status");
    }

    private void rebuildFilterOptions() {
        List<String> roomTypes = allRooms.stream()
                .map(Phong::getLoaiPhong)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .toList();
        String selectedType = cbType.getValue();
        cbType.getItems().setAll("Tat ca loai phong");
        cbType.getItems().addAll(roomTypes);
        cbType.setValue(cbType.getItems().contains(selectedType) ? selectedType : "Tat ca loai phong");

        List<String> floors = allRooms.stream()
                .map(Phong::getTang)
                .distinct()
                .sorted()
                .map(floor -> "Tang " + floor)
                .toList();
        String selectedFloor = cbFloor.getValue();
        cbFloor.getItems().setAll("Tat ca tang");
        cbFloor.getItems().addAll(floors);
        cbFloor.setValue(cbFloor.getItems().contains(selectedFloor) ? selectedFloor : "Tat ca tang");
    }

    private void renderRooms() {
        roomContainer.getChildren().clear();
        if (allRooms == null || allRooms.isEmpty()) {
            roomContainer.getChildren().add(new Label("Khong co du lieu phong."));
            return;
        }

        String keyword = txtSearchMaPhong.getText() == null ? "" : txtSearchMaPhong.getText().trim().toLowerCase();
        String selectedType = cbType.getValue();
        Integer selectedFloor = parseSelectedFloor();
        LocalDate selectedDate = dpFilterDate.getValue() != null ? dpFilterDate.getValue() : LocalDate.now();

        List<Phong> filteredRooms = roomAvailabilityService.filterRoomsByFloor(allRooms, selectedFloor).stream()
                .filter(room -> keyword.isBlank() || room.getMaPhong().toLowerCase().contains(keyword))
                .filter(room -> "Tat ca loai phong".equals(selectedType) || room.getLoaiPhong().equals(selectedType))
                .sorted(Comparator.comparingInt(Phong::getTang).thenComparing(Phong::getMaPhong))
                .collect(Collectors.toList());

        for (Phong room : filteredRooms) {
            RoomDisplayStatus displayStatus = roomStatusCache.getOrDefault(room.getMaPhong(), RoomDisplayStatus.TRONG);
            roomContainer.getChildren().add(createRoomCard(room, displayStatus, selectedDate));
        }
    }

    private Node createRoomCard(Phong room, RoomDisplayStatus displayStatus, LocalDate selectedDate) {
        VBox card = new VBox(8);
        card.setPrefWidth(180);
        card.setPadding(new Insets(14));
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(
                "-fx-background-color: white;"
                        + "-fx-border-color: " + displayStatus.getColor() + ";"
                        + "-fx-border-width: 0 0 4 0;"
                        + "-fx-background-radius: 12;"
                        + "-fx-border-radius: 0 0 12 12;"
                        + "-fx-effect: dropshadow(gaussian, rgba(15,23,42,0.12), 10, 0, 0, 4);"
                        + "-fx-cursor: hand;"
        );

        Label lblMa = new Label(room.getMaPhong());
        lblMa.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblMa.setTextFill(Color.web(AppTheme.TEXT));

        Label lblLoai = new Label(room.getLoaiPhong());
        lblLoai.setTextFill(Color.web(AppTheme.MUTED));
        Label lblTang = new Label("Tang " + room.getTang());
        lblTang.setTextFill(Color.web(AppTheme.MUTED));
        Label lblSoNguoi = new Label(room.getSoNguoi() + " nguoi");
        lblSoNguoi.setTextFill(Color.web(AppTheme.MUTED));
        Label lblTrangThai = new Label(displayStatus.getLabel());
        lblTrangThai.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblTrangThai.setTextFill(Color.web(displayStatus.getColor()));

        Label lblDate = new Label("Ngay: " + selectedDate);
        lblDate.setTextFill(Color.web(AppTheme.MUTED));
        lblDate.setFont(Font.font("Segoe UI", 11));

        card.getChildren().addAll(lblMa, lblLoai, lblTang, lblSoNguoi, lblTrangThai, lblDate);
        card.setOnMouseClicked(e -> showRoomDetailDialog(room, displayStatus, selectedDate));
        return card;
    }

    private HBox createLegendItem(RoomDisplayStatus status) {
        HBox hb = new HBox(8);
        hb.setAlignment(Pos.CENTER_LEFT);
        Region dot = new Region();
        dot.setPrefSize(14, 14);
        dot.setStyle("-fx-background-color: " + status.getColor() + "; -fx-background-radius: 4;");
        Label lbl = new Label(status.getLabel());
        lbl.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 13));
        hb.getChildren().addAll(dot, lbl);
        return hb;
    }

    private void showRoomDetailDialog(Phong room, RoomDisplayStatus displayStatus, LocalDate date) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Chi tiet phong");
        alert.setHeaderText("Phong " + room.getMaPhong());
        alert.setContentText(
                "Loai phong: " + room.getLoaiPhong() + "\n"
                        + "Tang: " + room.getTang() + "\n"
                        + "So nguoi: " + room.getSoNguoi() + "\n"
                        + "Gia goc: " + String.format("%,.0f VND", room.getGiaPhong()) + "\n"
                        + "Trang thai ngay " + date + ": " + displayStatus.getLabel()
        );
        alert.showAndWait();
    }

    private Integer parseSelectedFloor() {
        String value = cbFloor.getValue();
        if (value == null || "Tat ca tang".equals(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.replace("Tang", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void startDaemonTask(Task<?> task, String threadName) {
        Thread thread = new Thread(task, threadName);
        thread.setDaemon(true);
        thread.start();
    }
}
