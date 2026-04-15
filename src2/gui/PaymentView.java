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
import dao.PhieuDatPhongDAO;
import entity.HoaDon;
import entity.PhieuDatPhong;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 *  THANH TOÁN VIEW
 *  Chức năng: Thực hiện thanh toán cho khách check-out (tạo hóa đơn).
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()            - Tải danh sách phiếu đặt phòng đang ở (trạng thái "DaNhanPhong")
 *   2. handleSelectBooking() - Chọn phiếu đặt phòng → tự động tính tiền phòng + dịch vụ
 *   3. handleCalculate()     - Tính tổng tiền (tiền phòng × số đêm + tổng tiền dịch vụ)
 *   4. handlePayment()       - Xác nhận thanh toán → tạo HoaDon trong DB,
 *                              cập nhật trạng thái phòng → "Trống",
 *                              cập nhật trạng thái phiếu → "Đã trả phòng"
 *   5. handlePrint()         - In hóa đơn sau thanh toán
 * ================================================================
 */
public class PaymentView {

    private static final String BG_LIGHT = "#f6f6f8";
    private static final String ACCENT   = "#2c0fbd";
    private static final String BTN_PAY  = "#27ae60";
    private static final String BTN_INFO = "#3498db";

    private TableView<PhieuDatPhong> tableBooking;
    private ObservableList<PhieuDatPhong> bookingList = FXCollections.observableArrayList();
    private TextField txtSearch;

    // Panel hiển thị chi tiết thanh toán
    private Label lblMaPhieu, lblKhachHang, lblPhong, lblNgayNhan, lblNgayTra;
    private Label lblSoDem, lblTienPhong, lblTienDV, lblTongTien;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("THANH TOÁN");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        // --- Layout chính: bảng phiếu bên trái + form tính tiền bên phải ---
        HBox mainContent = new HBox(20);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // === Bên trái: Danh sách phiếu đang ở ===
        VBox leftPanel = new VBox(12);
        HBox.setHgrow(leftPanel, Priority.ALWAYS);
        leftPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16;");

        Label lblListTitle = new Label("PHIẾU ĐANG CÓ KHÁCH");
        lblListTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblListTitle.setTextFill(Color.web(ACCENT));

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm theo tên / mã phiếu...");
        txtSearch.setPrefHeight(34);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7;");

        tableBooking = new TableView<>(bookingList);
        tableBooking.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableBooking, Priority.ALWAYS);
        tableBooking.setPlaceholder(new Label("Không có phòng nào đang có khách."));
        tableBooking.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> populateDetail(selected));

        TableColumn<PhieuDatPhong, String> colMa = new TableColumn<>("Mã Phiếu");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaDatPhong()));
        TableColumn<PhieuDatPhong, String> colKhach = new TableColumn<>("Khách Hàng");
        colKhach.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getKhachHang() != null ? d.getValue().getKhachHang().getTenKhachHang() : ""));
        TableColumn<PhieuDatPhong, String> colPhong = new TableColumn<>("Phòng");
        colPhong.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaPhong()));

        tableBooking.getColumns().addAll(colMa, colKhach, colPhong);

        Button btnRefresh = createButton("🔄  Tải lại", "#7f8c8d");
        btnRefresh.setOnAction(e -> loadData());

        leftPanel.getChildren().addAll(lblListTitle, txtSearch, tableBooking, btnRefresh);

        // === Bên phải: Form chi tiết thanh toán ===
        VBox rightPanel = new VBox(14);
        rightPanel.setPrefWidth(350);
        rightPanel.setMinWidth(320);
        rightPanel.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 20;");

        Label lblFormTitle = new Label("CHI TIẾT THANH TOÁN");
        lblFormTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblFormTitle.setTextFill(Color.web(ACCENT));

        lblMaPhieu   = createInfoLabel("—");
        lblKhachHang = createInfoLabel("—");
        lblPhong     = createInfoLabel("—");
        lblNgayNhan  = createInfoLabel("—");
        lblNgayTra   = createInfoLabel("—");
        lblSoDem     = createInfoLabel("—");
        lblTienPhong = createInfoLabel("—");
        lblTienDV    = createInfoLabel("—");

        // Tổng tiền nổi bật
        lblTongTien = new Label("— VNĐ");
        lblTongTien.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTongTien.setTextFill(Color.web("#e74c3c"));

        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(10);
        String[][] rows = {
            {"Mã Phiếu:", null}, {"Khách Hàng:", null}, {"Phòng:", null},
            {"Ngày Nhận:", null}, {"Ngày Trả:", null}, {"Số Đêm:", null},
            {"Tiền Phòng:", null}, {"Tiền Dịch Vụ:", null}
        };
        Label[] valueLabels = {lblMaPhieu, lblKhachHang, lblPhong, lblNgayNhan, lblNgayTra, lblSoDem, lblTienPhong, lblTienDV};
        for (int i = 0; i < rows.length; i++) {
            Label lKey = new Label(rows[i][0]);
            lKey.setFont(Font.font("Segoe UI", 13));
            lKey.setTextFill(Color.web("#7f8c8d"));
            infoGrid.add(lKey, 0, i);
            infoGrid.add(valueLabels[i], 1, i);
        }

        Separator sep = new Separator();
        Label lblTongLabel = new Label("TỔNG TIỀN:");
        lblTongLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        Button btnCalc = createButton("🧮  Tính tiền", BTN_INFO);
        btnCalc.setMaxWidth(Double.MAX_VALUE);
        btnCalc.setOnAction(e -> handleCalculate());

        Button btnPay = createButton("💳  Xác nhận Thanh Toán", BTN_PAY);
        btnPay.setMaxWidth(Double.MAX_VALUE);
        btnPay.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnPay.setOnAction(e -> handlePayment());

        Button btnPrint = createButton("🖨  In Hóa Đơn", "#8e44ad");
        btnPrint.setMaxWidth(Double.MAX_VALUE);
        btnPrint.setOnAction(e -> handlePrint());

        rightPanel.getChildren().addAll(
                lblFormTitle, infoGrid, sep, lblTongLabel, lblTongTien,
                new Region(), btnCalc, btnPay, btnPrint
        );

        mainContent.getChildren().addAll(leftPanel, rightPanel);
        root.getChildren().addAll(lblTitle, mainContent);
        return root;
    }

    /**
     * TODO: Tải danh sách phòng đang có khách (trạng thái "DaNhanPhong").
     */
    public void loadData() {
        try {
            if (ConnectDB.getConnection() == null) return;
            PhieuDatPhongDAO dao = new PhieuDatPhongDAO();
            List<PhieuDatPhong> all = dao.getAllPhieuDatPhong();
            // TODO: Lọc chỉ lấy phiếu đang ở  (TrangThai = "DaNhanPhong" hoặc "Đang ở")
            bookingList.setAll(all != null ? all : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: Khi chọn phiếu trong bảng → điền thông tin vào panel bên phải.
     */
    private void populateDetail(PhieuDatPhong p) {
        if (p == null) return;
        lblMaPhieu.setText(p.getMaDatPhong() != null ? p.getMaDatPhong() : "—");
        lblKhachHang.setText(p.getKhachHang() != null ? p.getKhachHang().getTenKhachHang() : "—");
        lblPhong.setText(p.getMaPhong() != null ? p.getMaPhong() : "—");
        lblNgayNhan.setText(p.getNgayNhanPhong() != null ? p.getNgayNhanPhong().toString() : "—");
        lblNgayTra.setText(p.getNgayTraPhong() != null ? p.getNgayTraPhong().toString() : "—");
        // TODO: Tính số đêm, tiền phòng, tiền dịch vụ
        lblSoDem.setText("(Chưa tính)");
        lblTienPhong.setText("(Chưa tính)");
        lblTienDV.setText("(Chưa tính)");
        lblTongTien.setText("(Nhấn 'Tính tiền')");
    }

    /**
     * TODO: Tính tổng tiền dựa trên phiếu đang chọn.
     *       Công thức: Tổng = (GiaPhong × SoDem) + TongTienDichVu
     */
    private void handleCalculate() {
        PhieuDatPhong selected = tableBooking.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn phiếu đặt phòng cần thanh toán.");
            return;
        }
        // TODO: Implement tính tiền
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng tính tiền đang được phát triển.");
    }

    /**
     * TODO: Xác nhận thanh toán.
     *       1. Tạo HoaDon mới và lưu vào DB (dùng HoaDonDAO)
     *       2. Cập nhật trạng thái Phong → "Trống"
     *       3. Cập nhật trạng thái PhieuDatPhong → "Đã trả phòng"
     *       4. Reload dữ liệu
     */
    private void handlePayment() {
        // TODO: Implement thanh toán
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng thanh toán đang được phát triển.");
    }

    /**
     * TODO: In hóa đơn sau khi thanh toán.
     *       Gợi ý: Có thể dùng JasperReport hoặc xuất file PDF/text đơn giản.
     */
    private void handlePrint() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng in hóa đơn đang được phát triển.");
    }

    private Label createInfoLabel(String text) {
        Label lbl = new Label(text);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbl.setTextFill(Color.web("#2c3e50"));
        return lbl;
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefHeight(38);
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
