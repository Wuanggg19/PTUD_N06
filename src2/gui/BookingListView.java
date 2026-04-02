package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
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
 *  DANH SÁCH ĐẶT PHÒNG VIEW
 *  Chức năng: Xem toàn bộ danh sách phiếu đặt phòng, tìm kiếm, lọc trạng thái.
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()        - Tải danh sách phiếu đặt phòng từ DB
 *   2. handleSearch()    - Tìm kiếm theo tên khách hàng / mã phiếu
 *   3. handleFilter()    - Lọc theo trạng thái (Chờ xác nhận, Đã xác nhận, Đã hủy...)
 *   4. handleViewDetail()- Xem chi tiết phiếu đặt phòng
 *   5. handleCancel()    - Hủy phiếu đặt phòng đã chọn
 * ================================================================
 */
public class BookingListView {

    private static final String BG_LIGHT   = "#f6f6f8";
    private static final String ACCENT     = "#2c0fbd";
    private static final String BTN_INFO   = "#3498db";
    private static final String BTN_DANGER = "#e74c3c";

    private TableView<PhieuDatPhong> tableView;
    private ObservableList<PhieuDatPhong> dataList = FXCollections.observableArrayList();
    private TextField txtSearch;
    private ComboBox<String> cboFilter;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("DANH SÁCH ĐẶT PHÒNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        // --- Thanh công cụ tìm kiếm + lọc ---
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm theo tên khách / mã phiếu...");
        txtSearch.setPrefWidth(280);
        txtSearch.setPrefHeight(36);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7; -fx-padding: 0 10;");

        Button btnSearch = createButton("Tìm kiếm", BTN_INFO);
        btnSearch.setOnAction(e -> handleSearch());

        cboFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả", "Chờ xác nhận", "Đã xác nhận", "Đang ở", "Đã trả phòng", "Đã hủy"
        ));
        cboFilter.setValue("Tất cả");
        cboFilter.setPrefHeight(36);
        cboFilter.setOnAction(e -> handleFilter());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = createButton("🔄  Làm mới", "#27ae60");
        btnRefresh.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(txtSearch, btnSearch, new Label("Trạng thái:"), cboFilter, spacer, btnRefresh);

        // --- Bảng dữ liệu ---
        tableView = new TableView<>(dataList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setPlaceholder(new Label("Chưa có dữ liệu. Nhấn 'Làm mới' để tải."));

        TableColumn<PhieuDatPhong, String> colMa = new TableColumn<>("Mã Phiếu");
        colMa.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getMaDatPhong()));

        TableColumn<PhieuDatPhong, String> colKhach = new TableColumn<>("Khách Hàng");
        colKhach.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getKhachHang() != null
                        ? data.getValue().getKhachHang().getTenKhachHang()
                        : ""));

        TableColumn<PhieuDatPhong, String> colPhong = new TableColumn<>("Mã Phòng");
        colPhong.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getMaPhong()));

        TableColumn<PhieuDatPhong, String> colNgayDat = new TableColumn<>("Ngày Đặt");
        colNgayDat.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNgayDat() != null
                        ? data.getValue().getNgayDat().toString()
                        : ""));

        TableColumn<PhieuDatPhong, String> colNgayNhan = new TableColumn<>("Ngày Nhận Phòng");
        colNgayNhan.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNgayNhanPhong() != null
                        ? data.getValue().getNgayNhanPhong().toString()
                        : ""));

        TableColumn<PhieuDatPhong, String> colNgayTra = new TableColumn<>("Ngày Trả Phòng");
        colNgayTra.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNgayTraPhong() != null
                        ? data.getValue().getNgayTraPhong().toString()
                        : ""));

        TableColumn<PhieuDatPhong, String> colTrangThai = new TableColumn<>("Trạng Thái");
        colTrangThai.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTrangThai()));

        tableView.getColumns().addAll(colMa, colKhach, colPhong, colNgayDat, colNgayNhan, colNgayTra, colTrangThai);

        // --- Nút hành động ---
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        Button btnDetail = createButton("📄  Xem chi tiết", BTN_INFO);
        btnDetail.setOnAction(e -> handleViewDetail());
        Button btnCancel = createButton("❌  Hủy phiếu", BTN_DANGER);
        btnCancel.setOnAction(e -> handleCancel());
        actionBar.getChildren().addAll(btnDetail, btnCancel);

        root.getChildren().addAll(lblTitle, toolbar, tableView, actionBar);
        return root;
    }

    /**
     * TODO: Tải toàn bộ danh sách phiếu đặt phòng từ database.
     *       Gợi ý: Dùng PhieuDatPhongDAO.getAllPhieuDatPhong()
     */
    public void loadData() {
        try {
            if (ConnectDB.getConnection() == null) return;
            PhieuDatPhongDAO dao = new PhieuDatPhongDAO();
            List<PhieuDatPhong> list = dao.getAllPhieuDatPhong();
            dataList.setAll(list != null ? list : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu: " + e.getMessage());
        }
    }

    /**
     * TODO: Tìm kiếm phiếu đặt phòng theo từ khóa trong txtSearch.
     *       Lọc từ dataList theo tên khách hàng hoặc mã phiếu.
     */
    private void handleSearch() {
        // TODO: Implement tìm kiếm
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng tìm kiếm đang được phát triển.");
    }

    /**
     * TODO: Lọc danh sách theo trạng thái được chọn trong cboFilter.
     */
    private void handleFilter() {
        // TODO: Implement lọc theo trạng thái
    }

    /**
     * TODO: Hiển thị chi tiết phiếu đặt phòng đang được chọn trong bảng.
     *       Gợi ý: Dùng tableView.getSelectionModel().getSelectedItem()
     */
    private void handleViewDetail() {
        PhieuDatPhong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một phiếu đặt phòng.");
            return;
        }
        // TODO: Hiển thị dialog chi tiết phiếu đặt phòng
        showAlert(Alert.AlertType.INFORMATION, "Chi tiết", "Mã phiếu: " + selected.getMaDatPhong() + "\nChức năng chi tiết đang phát triển.");
    }

    /**
     * TODO: Hủy phiếu đặt phòng đang được chọn.
     *       Gợi ý: Cập nhật trạng thái thành "Đã hủy" trong DB.
     */
    private void handleCancel() {
        PhieuDatPhong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một phiếu đặt phòng để hủy.");
            return;
        }
        // TODO: Implement hủy phiếu đặt phòng
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng hủy phiếu đang được phát triển.");
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefHeight(36);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setTextFill(Color.WHITE);
        String base = "-fx-background-color: " + color + "; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 16;";
        btn.setStyle(base);
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
