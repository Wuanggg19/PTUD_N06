package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableRow;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.beans.property.SimpleStringProperty;

import dao.PhieuDatPhongDAO;
import entity.PhieuDatPhong;
import util.BookingStatus;
import util.StatusUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

public class CheckInView {

    private static final String BG_LIGHT   = "#f6f6f8";
    private static final String BTN_OK     = "#27ae60";
    private static final String BTN_INFO   = "#3498db";

    private TableView<PhieuDatPhong> tableView;
    private ObservableList<PhieuDatPhong> dataList = FXCollections.observableArrayList();
    private TextField txtSearch;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label("CHECK-IN KHÁCH HÀNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));
        Label lblSub = new Label("  —  Xác nhận nhận phòng cho khách");
        lblSub.setFont(Font.font("Segoe UI", 15));
        lblSub.setTextFill(Color.web("#7f8c8d"));
        header.getChildren().addAll(lblTitle, lblSub);

        Label lblGuide = new Label("📌 Chọn phiếu đặt phòng và nhấn 'Check-in'. Hệ thống sẽ cập nhật trạng thái phòng sang 'Đang ở'.");
        lblGuide.setFont(Font.font("Segoe UI", 13));
        lblGuide.setTextFill(Color.web("#5d6d7e"));
        lblGuide.setWrapText(true);
        lblGuide.setStyle("-fx-background-color: #eaf4fb; -fx-padding: 10 14; -fx-background-radius: 8;");

        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍 Tìm theo tên khách / mã phiếu...");
        txtSearch.setPrefWidth(300);
        txtSearch.setPrefHeight(36);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7;");

        Button btnSearch = createButton("Tìm kiếm", BTN_INFO);
        btnSearch.setOnAction(e -> handleSearch());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnRefresh = createButton("🔄 Làm mới", "#7f8c8d");
        btnRefresh.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(txtSearch, btnSearch, spacer, btnRefresh);

        tableView = new TableView<>(dataList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<PhieuDatPhong, String> colMa = new TableColumn<>("Mã Phiếu");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaDatPhong()));

        TableColumn<PhieuDatPhong, String> colKhach = new TableColumn<>("Khách Hàng");
        colKhach.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getKhachHang() != null ? d.getValue().getKhachHang().getTenKhachHang() : "Khách vãng lai"));

        TableColumn<PhieuDatPhong, String> colPhong = new TableColumn<>("Phòng");
        colPhong.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDsMaPhong()));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        TableColumn<PhieuDatPhong, String> colNgayNhan = new TableColumn<>("Ngày Nhận");
        colNgayNhan.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNgayNhan() != null ? d.getValue().getNgayNhan().format(dtf) : "N/A"));

        TableColumn<PhieuDatPhong, String> colStatus = new TableColumn<>("Trạng Thái");
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(StatusUtils.bookingLabel(d.getValue().getTrangThai())));

        tableView.getColumns().addAll(colMa, colKhach, colPhong, colNgayNhan, colStatus);

        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        Button btnCheckIn = createButton("✅ Xác nhận Check-in", BTN_OK);
        btnCheckIn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnCheckIn.setOnAction(e -> handleCheckIn());
        actionBar.getChildren().addAll(btnCheckIn);

        root.getChildren().addAll(header, lblGuide, toolbar, tableView, actionBar);
        loadData();
        return root;
    }

    public void loadData() {
        Task<List<PhieuDatPhong>> task = new Task<>() {
            @Override
            protected List<PhieuDatPhong> call() {
                return new PhieuDatPhongDAO().getAllPhieuDatPhong().stream()
                    .filter(p -> StatusUtils.isBookingStatus(p.getTrangThai(), BookingStatus.CHO_XAC_NHAN))
                    .collect(Collectors.toList());
            }
        };
        task.setOnSucceeded(e -> dataList.setAll(task.getValue()));
        task.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu từ database."));
        new Thread(task).start();
    }

    private void handleSearch() {
        String text = txtSearch.getText().toLowerCase().trim();
        if (text.isEmpty()) {
            loadData();
            return;
        }
        dataList.setAll(dataList.stream()
                .filter(p -> p.getMaDatPhong().toLowerCase().contains(text) || 
                             (p.getKhachHang() != null && p.getKhachHang().getTenKhachHang().toLowerCase().contains(text)))
                .collect(Collectors.toList()));
    }

    private void handleCheckIn() {
        PhieuDatPhong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn một phiếu đặt phòng để check-in.");
            return;
        }

        // Kiểm tra trạng thái phiếu bằng BookingStatus
        if (StatusUtils.isBookingStatus(selected.getTrangThai(), BookingStatus.DA_NHAN_PHONG)) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Phiếu này đã được check-in trước đó.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
            "Xác nhận check-in cho phiếu " + selected.getMaDatPhong() + "?", 
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận");
        confirm.setHeaderText(null);
        
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return new PhieuDatPhongDAO().checkIn(selected.getMaDatPhong());
                    }
                };
                
                task.setOnSucceeded(e -> {
                    if (task.getValue()) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Check-in thành công cho phiếu " + selected.getMaDatPhong());
                        loadData();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Có lỗi xảy ra trong quá trình cập nhật Database.");
                    }
                });
                
                task.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể thực hiện tác vụ Check-in."));
                new Thread(task).start();
            }
        });
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefHeight(36);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 16;");
        return btn;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
