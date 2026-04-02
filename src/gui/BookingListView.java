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

import connectDB.ConnectDB;
import dao.PhieuDatPhongDAO;
import dao.PhongDAO;
import dao.ChiTietPhieuDatDAO;
import entity.PhieuDatPhong;
import entity.ChiTietPhieuDat;
import entity.Phong;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

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
    private List<PhieuDatPhong> fullData = new ArrayList<>();
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
        txtSearch.textProperty().addListener((o, v, n) -> handleSearch());

        cboFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả", "DaNhanPhong", "DaThanhToan", "DaHuy"
        ));
        cboFilter.setValue("Tất cả");
        cboFilter.setPrefHeight(36);
        cboFilter.setPrefWidth(150);
        cboFilter.setOnAction(e -> handleFilter());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnRefresh = createButton("🔄  Làm mới", "#27ae60");
        btnRefresh.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(txtSearch, new Label("Trạng thái:"), cboFilter, spacer, btnRefresh);

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

        TableColumn<PhieuDatPhong, String> colNgayDat = new TableColumn<>("Ngày Lập Phiếu");
        colNgayDat.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(
                        data.getValue().getNgayDat() != null
                        ? data.getValue().getNgayDat().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : ""));

        TableColumn<PhieuDatPhong, String> colTrangThai = new TableColumn<>("Trạng Thái");
        colTrangThai.setPrefWidth(200);
        colTrangThai.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty(data.getValue().getTrangThai()));

        tableView.getColumns().addAll(colMa, colKhach, colNgayDat, colTrangThai);

        // Double click to view detail
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                handleViewDetail();
            }
        });

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

    public void loadData() {
        Task<List<PhieuDatPhong>> task = new Task<List<PhieuDatPhong>>() {
            @Override
            protected List<PhieuDatPhong> call() throws Exception {
                return new PhieuDatPhongDAO().getAllPhieuDatPhong();
            }
        };
        task.setOnSucceeded(e -> {
            fullData = task.getValue();
            applyFilterAndSearch();
        });
        task.setOnFailed(e -> {
            e.getSource().getException().printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu từ server.");
        });
        new Thread(task).start();
    }

    private void handleSearch() {
        applyFilterAndSearch();
    }

    private void handleFilter() {
        applyFilterAndSearch();
    }

    private void applyFilterAndSearch() {
        if (fullData == null) return;
        String search = txtSearch.getText().toLowerCase().trim();
        String filter = cboFilter.getValue();

        List<PhieuDatPhong> filtered = fullData.stream()
            .filter(p -> filter.equals("Tất cả") || p.getTrangThai().contains(filter))
            .filter(p -> search.isEmpty() 
                    || p.getMaDatPhong().toLowerCase().contains(search) 
                    || (p.getKhachHang() != null && p.getKhachHang().getTenKhachHang().toLowerCase().contains(search)))
            .collect(Collectors.toList());
        
        dataList.setAll(filtered);
    }

    private void handleViewDetail() {
        PhieuDatPhong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một phiếu đặt phòng.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("CHI TIẾT PHIÊU ĐẶT PHÒNG - " + selected.getMaDatPhong());
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(550);

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(30); infoGrid.setVgap(10);
        
        Label lblKH = new Label(selected.getKhachHang() != null ? selected.getKhachHang().getTenKhachHang() : "N/A");
        lblKH.setStyle("-fx-font-weight: bold;");
        Label lblSdt = new Label(selected.getKhachHang() != null ? selected.getKhachHang().getSoDienThoai() : "N/A");
        Label lblNV = new Label(selected.getNhanVien() != null ? selected.getNhanVien().getMaNhanVien() : "N/A");
        Label lblStatus = new Label(selected.getTrangThai());
        lblStatus.setTextFill(selected.getTrangThai().contains("DaHuy") ? Color.RED : Color.GREEN);

        infoGrid.add(new Label("Khách hàng:"), 0, 0); infoGrid.add(lblKH, 1, 0);
        infoGrid.add(new Label("Số điện thoại:"), 0, 1); infoGrid.add(lblSdt, 1, 1);
        infoGrid.add(new Label("Nhân viên lập:"), 0, 2); infoGrid.add(lblNV, 1, 2);
        infoGrid.add(new Label("Trạng thái:"), 0, 3); infoGrid.add(lblStatus, 1, 3);

        Label lblRoomTitle = new Label("DANH SÁCH CHI TIẾT PHÒNG:");
        lblRoomTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: " + ACCENT + ";");

        VBox detailBox = new VBox(8);
        detailBox.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #e2e8f0;");
        
        List<ChiTietPhieuDat> dsCT = new ChiTietPhieuDatDAO().getDSChiTietByMaPhieu(selected.getMaDatPhong());
        if (dsCT.isEmpty()) {
            detailBox.getChildren().add(new Label("(Không lấy được thông tin chi tiết phòng)"));
        } else {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (ChiTietPhieuDat ct : dsCT) {
                Phong p = new PhongDAO().getPhongByMa(ct.getPhong().getMaPhong());
                Label roomInfo = new Label(String.format("• Phòng %s | Loại: %s | Giá: %,.0f VNĐ", 
                        ct.getPhong().getMaPhong(), 
                        (p != null ? p.getLoaiPhong() : "N/A"),
                        ct.getGiaThuePhong()));
                Label dateInfo = new Label(String.format("  Nhận: %s -> Trả: %s", 
                        ct.getNgayNhan().format(dtf), 
                        ct.getNgayTra().format(dtf)));
                dateInfo.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
                detailBox.getChildren().addAll(roomInfo, dateInfo);
            }
        }

        content.getChildren().addAll(new Label("MÃ PHIẾU: " + selected.getMaDatPhong()), infoGrid, new Separator(), lblRoomTitle, detailBox);
        dialog.getDialogPane().setContent(content);
        dialog.showAndWait();
    }

    private void handleCancel() {
        PhieuDatPhong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một phiếu đặt phòng để hủy.");
            return;
        }

        if (selected.getTrangThai().contains("DaThanhToan") || selected.getTrangThai().contains("DaHuy")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy phiếu đã thanh toán hoặc đã hủy.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "Bạn có chắc chắn muốn hủy phiếu " + selected.getMaDatPhong() + "?\nHành động này sẽ trả tất cả phòng trong phiếu về trạng thái 'Trống'.", 
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận hủy phiếu");
        confirm.setHeaderText(null);
        
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                try {
                    boolean ok = new PhieuDatPhongDAO().huyPhieu(selected.getMaDatPhong());
                    if (ok) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy phiếu đặt phòng thành công.");
                        loadData();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi khi cập nhật database.");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi hệ thống: " + e.getMessage());
                }
            }
        });
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
