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

import connectDB.ConnectDB;
import dao.PhieuDatPhongDAO;
import entity.PhieuDatPhong;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 *  CHECK-IN VIEW
 *  Chức năng: Xác nhận check-in cho khách khi họ đến nhận phòng.
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()          - Tải danh sách phiếu đặt phòng có trạng thái "Đã xác nhận" / chờ check-in
 *   2. handleSearch()      - Tìm kiếm theo tên khách / mã phiếu
 *   3. handleCheckIn()     - Xác nhận check-in: cập nhật trạng thái phiếu → "DaNhanPhong",
 *                            cập nhật trạng thái phòng → "Đang ở"
 *   4. handleViewDetail()  - Xem thông tin chi tiết phiếu đặt trước khi check-in
 * ================================================================
 */
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

        // --- Header ---
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label("CHECK-IN KHÁCH HÀNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));
        Label lblSub = new Label("  —  Xác nhận nhận phòng cho khách");
        lblSub.setFont(Font.font("Segoe UI", 15));
        lblSub.setTextFill(Color.web("#7f8c8d"));
        header.getChildren().addAll(lblTitle, lblSub);

        // --- Hướng dẫn ---
        Label lblGuide = new Label("📌  Chọn phiếu đặt phòng trong danh sách bên dưới và nhấn 'Check-in' để xác nhận khách đã nhận phòng.");
        lblGuide.setFont(Font.font("Segoe UI", 13));
        lblGuide.setTextFill(Color.web("#5d6d7e"));
        lblGuide.setWrapText(true);
        lblGuide.setStyle("-fx-background-color: #eaf4fb; -fx-padding: 10 14; -fx-background-radius: 8;");

        // --- Thanh tìm kiếm ---
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm theo tên khách / mã phiếu...");
        txtSearch.setPrefWidth(300);
        txtSearch.setPrefHeight(36);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7; -fx-padding: 0 10;");

        Button btnSearch = createButton("Tìm kiếm", BTN_INFO);
        btnSearch.setOnAction(e -> handleSearch());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnRefresh = createButton("🔄  Làm mới", "#7f8c8d");
        btnRefresh.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(txtSearch, btnSearch, spacer, btnRefresh);

        // --- Bảng danh sách phiếu chờ check-in ---
        tableView = new TableView<>(dataList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setPlaceholder(new Label("Không có phiếu đặt phòng nào đang chờ check-in."));

        TableColumn<PhieuDatPhong, String> colMa = new TableColumn<>("Mã Phiếu");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaDatPhong()));

        TableColumn<PhieuDatPhong, String> colKhach = new TableColumn<>("Khách Hàng");
        colKhach.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getKhachHang() != null ? d.getValue().getKhachHang().getTenKhachHang() : ""));

        TableColumn<PhieuDatPhong, String> colPhong = new TableColumn<>("Phòng");
        colPhong.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaPhong()));

        TableColumn<PhieuDatPhong, String> colNgayNhan = new TableColumn<>("Ngày Nhận Phòng");
        colNgayNhan.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getNgayNhanPhong() != null ? d.getValue().getNgayNhanPhong().toString() : ""));

        TableColumn<PhieuDatPhong, String> colNgayTra = new TableColumn<>("Ngày Trả Phòng");
        colNgayTra.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getNgayTraPhong() != null ? d.getValue().getNgayTraPhong().toString() : ""));

        TableColumn<PhieuDatPhong, String> colTT = new TableColumn<>("Trạng Thái");
        colTT.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTrangThai()));

        tableView.getColumns().addAll(colMa, colKhach, colPhong, colNgayNhan, colNgayTra, colTT);

        // --- Nút hành động ---
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        Button btnDetail = createButton("📄  Xem chi tiết", BTN_INFO);
        btnDetail.setOnAction(e -> handleViewDetail());
        Button btnCheckIn = createButton("✅  Xác nhận Check-in", BTN_OK);
        btnCheckIn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnCheckIn.setOnAction(e -> handleCheckIn());
        actionBar.getChildren().addAll(btnDetail, btnCheckIn);

        root.getChildren().addAll(header, lblGuide, toolbar, tableView, actionBar);
        return root;
    }

    /**
     * TODO: Tải danh sách phiếu đặt phòng có trạng thái "Đã xác nhận" hoặc chờ check-in.
     *       Gợi ý: Dùng PhieuDatPhongDAO.getAllPhieuDatPhong() rồi lọc theo trạng thái.
     */
    public void loadData() {
        try {
            if (ConnectDB.getConnection() == null) return;
            PhieuDatPhongDAO dao = new PhieuDatPhongDAO();
            List<PhieuDatPhong> all = dao.getAllPhieuDatPhong();
            // TODO: Lọc chỉ lấy phiếu có trạng thái "Đã xác nhận" hoặc "Chờ xác nhận"
            dataList.setAll(all != null ? all : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: Tìm kiếm phiếu trong dataList theo từ khóa nhập vào txtSearch.
     */
    private void handleSearch() {
        // TODO: Implement tìm kiếm
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng tìm kiếm đang được phát triển.");
    }

    /**
     * TODO: Xem chi tiết phiếu đặt phòng đang chọn.
     */
    private void handleViewDetail() {
        PhieuDatPhong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một phiếu đặt phòng.");
            return;
        }
        // TODO: Hiển thị dialog chi tiết
        showAlert(Alert.AlertType.INFORMATION, "Chi tiết", "Mã phiếu: " + selected.getMaDatPhong());
    }

    /**
     * TODO: Xác nhận check-in cho phiếu đặt phòng đang chọn.
     *       Các bước cần làm:
     *       1. Lấy phiếu đang chọn
     *       2. Hỏi xác nhận người dùng
     *       3. Cập nhật TrangThai của PhieuDatPhong → "DaNhanPhong"
     *       4. Cập nhật TrangThai của Phong → "Đang ở"
     *       5. Load lại dữ liệu
     */
    private void handleCheckIn() {
        PhieuDatPhong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn phiếu đặt phòng cần check-in.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận check-in cho khách: " + (selected.getKhachHang() != null ? selected.getKhachHang().getTenKhachHang() : selected.getMaDatPhong()) + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận Check-in");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                // TODO: Implement logic check-in ở đây
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng check-in đang được phát triển.");
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
