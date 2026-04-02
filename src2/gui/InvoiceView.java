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
import dao.HoaDonDAO;
import entity.HoaDon;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 *  HÓA ĐƠN VIEW
 *  Chức năng: Xem danh sách hóa đơn, lọc theo ngày, xem chi tiết, in hóa đơn.
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()          - Tải toàn bộ hóa đơn từ DB
 *   2. handleSearch()      - Tìm kiếm theo mã hóa đơn / tên khách
 *   3. handleFilterDate()  - Lọc hóa đơn theo khoảng ngày
 *   4. handleViewDetail()  - Xem chi tiết hóa đơn + danh sách dịch vụ đã dùng
 *   5. handlePrint()       - In hóa đơn
 *   6. handleExport()      - Xuất danh sách hóa đơn ra Excel (tùy chọn)
 * ================================================================
 */
public class InvoiceView {

    private static final String BG_LIGHT  = "#f6f6f8";
    private static final String BTN_INFO  = "#3498db";
    private static final String BTN_PRINT = "#8e44ad";

    private TableView<HoaDon> tableView;
    private ObservableList<HoaDon> dataList = FXCollections.observableArrayList();
    private TextField txtSearch;
    private DatePicker dpFrom, dpTo;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("QUẢN LÝ HÓA ĐƠN");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        // --- Bộ lọc theo ngày ---
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12 16;");

        Label lblFrom = new Label("Từ ngày:");
        lblFrom.setFont(Font.font("Segoe UI", 13));
        dpFrom = new DatePicker();
        dpFrom.setPromptText("Từ ngày");

        Label lblTo = new Label("Đến ngày:");
        lblTo.setFont(Font.font("Segoe UI", 13));
        dpTo = new DatePicker();
        dpTo.setPromptText("Đến ngày");

        Button btnFilterDate = createButton("🔍  Lọc theo ngày", BTN_INFO);
        btnFilterDate.setOnAction(e -> handleFilterDate());

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã HĐ / tên khách...");
        txtSearch.setPrefWidth(220);
        txtSearch.setPrefHeight(34);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnRefresh = createButton("🔄  Tất cả", "#7f8c8d");
        btnRefresh.setOnAction(e -> loadData());

        filterBar.getChildren().addAll(lblFrom, dpFrom, lblTo, dpTo, btnFilterDate,
                spacer, new Label("  Tìm:"), txtSearch, btnRefresh);

        // --- Bảng hóa đơn ---
        tableView = new TableView<>(dataList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setPlaceholder(new Label("Chưa có hóa đơn nào."));

        TableColumn<HoaDon, String> colMa = new TableColumn<>("Mã Hóa Đơn");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaHoaDon()));

        TableColumn<HoaDon, String> colPhieu = new TableColumn<>("Mã Phiếu Đặt");
        colPhieu.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaDatPhong()));

        TableColumn<HoaDon, String> colNV = new TableColumn<>("Nhân Viên Lập");
        colNV.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaNhanVien()));

        TableColumn<HoaDon, String> colNgay = new TableColumn<>("Ngày Lập");
        colNgay.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getNgayLap() != null ? d.getValue().getNgayLap().toString() : ""));

        TableColumn<HoaDon, String> colTienPhong = new TableColumn<>("Tiền Phòng");
        colTienPhong.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f VNĐ", d.getValue().getTongTienPhong())));

        TableColumn<HoaDon, String> colTienDV = new TableColumn<>("Tiền Dịch Vụ");
        colTienDV.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f VNĐ", d.getValue().getTongTienDichVu())));

        TableColumn<HoaDon, String> colTong = new TableColumn<>("Tổng Tiền");
        colTong.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f VNĐ", d.getValue().getTongTienPhong() + d.getValue().getTongTienDichVu())));

        tableView.getColumns().addAll(colMa, colPhieu, colNV, colNgay, colTienPhong, colTienDV, colTong);

        // --- Nút hành động ---
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        Button btnDetail = createButton("📄  Xem chi tiết", BTN_INFO);
        btnDetail.setOnAction(e -> handleViewDetail());
        Button btnPrint = createButton("🖨  In hóa đơn", BTN_PRINT);
        btnPrint.setOnAction(e -> handlePrint());
        Button btnExport = createButton("📊  Xuất Excel", "#16a085");
        btnExport.setOnAction(e -> handleExport());
        actionBar.getChildren().addAll(btnDetail, btnPrint, btnExport);

        root.getChildren().addAll(lblTitle, filterBar, tableView, actionBar);
        return root;
    }

    /**
     * TODO: Tải toàn bộ hóa đơn từ database.
     *       Gợi ý: Dùng HoaDonDAO.getAllHoaDon()
     */
    public void loadData() {
        try {
            if (ConnectDB.getConnection() == null) return;
            HoaDonDAO dao = new HoaDonDAO();
            List<HoaDon> list = dao.getAllHoaDon();
            dataList.setAll(list != null ? list : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu hóa đơn: " + e.getMessage());
        }
    }

    /**
     * TODO: Lọc hóa đơn theo khoảng ngày từ dpFrom đến dpTo.
     */
    private void handleFilterDate() {
        if (dpFrom.getValue() == null || dpTo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn cả ngày bắt đầu và kết thúc.");
            return;
        }
        // TODO: Implement lọc theo khoảng ngày
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng lọc ngày đang được phát triển.");
    }

    /**
     * TODO: Hiển thị chi tiết hóa đơn đang chọn (bao gồm danh sách dịch vụ đã dùng).
     */
    private void handleViewDetail() {
        HoaDon selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }
        // TODO: Hiển thị dialog chi tiết hóa đơn (bao gồm ChiTietHoaDon)
        showAlert(Alert.AlertType.INFORMATION, "Chi tiết", "Mã HĐ: " + selected.getMaHoaDon() + "\nChức năng đang phát triển.");
    }

    /**
     * TODO: In hóa đơn đang chọn.
     */
    private void handlePrint() {
        HoaDon selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một hóa đơn để in.");
            return;
        }
        // TODO: Implement in hóa đơn
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng in hóa đơn đang được phát triển.");
    }

    /**
     * TODO: Xuất danh sách hóa đơn ra file Excel (tùy chọn nâng cao).
     */
    private void handleExport() {
        // TODO: Implement xuất Excel (có thể dùng Apache POI)
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng xuất Excel đang được phát triển.");
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefHeight(36);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 14;");
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
