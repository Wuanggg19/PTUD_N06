package gui;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;

import dao.PhieuDatPhongDAO;
import dao.ChiTietPhieuDatDAO;
import dao.PhongDAO;
import dao.HoaDonDAO;
import entity.PhieuDatPhong;
import entity.ChiTietPhieuDat;
import entity.Phong;
import entity.HoaDon;
import entity.NhanVien;

public class CheckoutView {

    private final String BG_LIGHT = "#f6f6f8";
    private final String PRIMARY_COLOR = "#2c0fbd";
    
    private TableView<PhieuDatPhong> tvBookings;
    private List<PhieuDatPhong> allBookings;
    private TextField txtSearch;
    
    private VBox detailPane;
    private Label lblGuestName, lblGuestPhone;
    private VBox roomsContainer;
    private Label lblTotal;
    
    private PhieuDatPhong selectedPDP;
    private List<CheckBox> roomCheckboxes = new ArrayList<>();
    private final NhanVien loggedInNhanVien;

    public CheckoutView(NhanVien nhanVien) {
        this.loggedInNhanVien = nhanVien;
    }

    public Node createView() {
        HBox root = new HBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- CỘT TRÁI: DANH SÁCH PHIẾU ĐANG Ở ---
        VBox leftCol = new VBox(15);
        leftCol.setPrefWidth(550);

        Label lblTitle = new Label("TRẢ PHÒNG & THANH TOÁN");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        
        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo tên khách, SĐT hoặc mã phiếu...");
        txtSearch.setPrefHeight(40);
        txtSearch.textProperty().addListener((o, v, n) -> filterBookings());

        tvBookings = new TableView<>();
        tvBookings.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tvBookings, Priority.ALWAYS);

        TableColumn<PhieuDatPhong, String> colMa = new TableColumn<>("Mã Phiếu");
        colMa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaDatPhong()));
        TableColumn<PhieuDatPhong, String> colKhach = new TableColumn<>("Khách Hàng");
        colKhach.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getKhachHang().getTenKhachHang()));
        TableColumn<PhieuDatPhong, String> colNgay = new TableColumn<>("Ngày Đặt");
        colNgay.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));

        tvBookings.getColumns().addAll(colMa, colKhach, colNgay);
        tvBookings.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> showDetail(newV));

        leftCol.getChildren().addAll(lblTitle, txtSearch, tvBookings);

        // --- CỘT PHẢI: CHI TIẾT TRẢ PHÒNG ---
        detailPane = new VBox(20);
        detailPane.setPadding(new Insets(25));
        detailPane.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        HBox.setHgrow(detailPane, Priority.ALWAYS);
        detailPane.setVisible(false);

        Label lblDetailTitle = new Label("THÔNG TIN THANH TOÁN");
        lblDetailTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        lblDetailTitle.setTextFill(Color.web(PRIMARY_COLOR));

        GridPane guestGrid = new GridPane();
        guestGrid.setHgap(20); guestGrid.setVgap(10);
        lblGuestName = new Label(); lblGuestName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        lblGuestPhone = new Label();
        guestGrid.add(new Label("Khách hàng:"), 0, 0); guestGrid.add(lblGuestName, 1, 0);
        guestGrid.add(new Label("Số điện thoại:"), 0, 1); guestGrid.add(lblGuestPhone, 1, 1);

        Label lblInstruction = new Label("Chọn các phòng muốn trả:");
        lblInstruction.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        roomsContainer = new VBox(10);
        ScrollPane spRooms = new ScrollPane(roomsContainer);
        spRooms.setFitToWidth(true);
        spRooms.setPrefHeight(300);
        spRooms.setStyle("-fx-background-color: transparent; -fx-background: white;");

        HBox totalBox = new HBox(20);
        totalBox.setAlignment(Pos.CENTER_RIGHT);
        lblTotal = new Label("TỔNG CỘNG: 0 VNĐ");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTotal.setTextFill(Color.RED);
        totalBox.getChildren().add(lblTotal);

        Button btnCheckout = new Button("XÁC NHẬN THANH TOÁN & TRẢ PHÒNG");
        btnCheckout.setMaxWidth(Double.MAX_VALUE);
        btnCheckout.setPrefHeight(50);
        btnCheckout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-cursor: hand; -fx-background-radius: 8;");
        btnCheckout.setOnAction(e -> handleCheckout());

        detailPane.getChildren().addAll(lblDetailTitle, new Separator(), guestGrid, lblInstruction, spRooms, new Separator(), totalBox, btnCheckout);

        root.getChildren().addAll(leftCol, detailPane);
        
        loadData();
        return root;
    }

    public void loadData() {
        Task<List<PhieuDatPhong>> task = new Task<List<PhieuDatPhong>>() {
            @Override
            protected List<PhieuDatPhong> call() throws Exception {
                // Chỉ lấy các phiếu đang ở (DaNhanPhong)
                return new PhieuDatPhongDAO().getAllPhieuDatPhong().stream()
                    .filter(p -> p.getTrangThai().contains("DaNhanPhong"))
                    .collect(Collectors.toList());
            }
        };
        task.setOnSucceeded(e -> {
            allBookings = task.getValue();
            filterBookings();
        });
        new Thread(task).start();
    }

    private void filterBookings() {
        if (allBookings == null) return;
        String query = txtSearch.getText().toLowerCase().trim();
        List<PhieuDatPhong> filtered = allBookings.stream()
            .filter(p -> query.isEmpty() || 
                    p.getMaDatPhong().toLowerCase().contains(query) || 
                    p.getKhachHang().getTenKhachHang().toLowerCase().contains(query) ||
                    p.getKhachHang().getSoDienThoai().contains(query))
            .collect(Collectors.toList());
        tvBookings.getItems().setAll(filtered);
    }

    private void showDetail(PhieuDatPhong pdp) {
        if (pdp == null) {
            detailPane.setVisible(false);
            return;
        }
        selectedPDP = pdp;
        detailPane.setVisible(true);
        lblGuestName.setText(pdp.getKhachHang().getTenKhachHang());
        lblGuestPhone.setText(pdp.getKhachHang().getSoDienThoai());
        
        roomsContainer.getChildren().clear();
        roomCheckboxes.clear();
        
        Task<List<ChiTietPhieuDat>> task = new Task<List<ChiTietPhieuDat>>() {
            @Override
            protected List<ChiTietPhieuDat> call() throws Exception {
                return new ChiTietPhieuDatDAO().getDSChiTietByMaPhieu(pdp.getMaDatPhong());
            }
        };
        task.setOnSucceeded(e -> {
            List<ChiTietPhieuDat> dsCT = task.getValue();
            for (ChiTietPhieuDat ct : dsCT) {
                // Kiểm tra xem phòng có còn đang ở không
                // (Phòng có thể đã trả trước đó)
                Phong p = new PhongDAO().getPhongByMa(ct.getPhong().getMaPhong());
                if (p != null && "Đang ở".equals(p.getTrangThai())) {
                    roomsContainer.getChildren().add(createRoomRow(ct));
                }
            }
            updateTotal();
        });
        new Thread(task).start();
    }

    private Node createRoomRow(ChiTietPhieuDat ct) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 15, 10, 15));
        row.setStyle("-fx-border-color: #f1f2f6; -fx-border-width: 0 0 1 0;");

        CheckBox cb = new CheckBox();
        cb.setSelected(true);
        cb.setUserData(ct);
        cb.setOnAction(e -> updateTotal());
        roomCheckboxes.add(cb);

        VBox info = new VBox(2);
        Label lblMa = new Label("Phòng " + ct.getPhong().getMaPhong());
        lblMa.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        long days = Duration.between(ct.getNgayNhan(), LocalDateTime.now()).toDays();
        if (days == 0) days = 1;
        
        double subtotal = days * ct.getGiaThuePhong();
        Label lblPrice = new Label(days + " ngày x " + String.format("%,.0f VNĐ", ct.getGiaThuePhong()) + " = " + String.format("%,.0f VNĐ", subtotal));
        lblPrice.setTextFill(Color.GRAY);
        
        info.getChildren().addAll(lblMa, lblPrice);
        HBox.setHgrow(info, Priority.ALWAYS);

        row.getChildren().addAll(cb, info);
        return row;
    }

    private void updateTotal() {
        double total = 0;
        for (CheckBox cb : roomCheckboxes) {
            if (cb.isSelected()) {
                ChiTietPhieuDat ct = (ChiTietPhieuDat) cb.getUserData();
                long days = Duration.between(ct.getNgayNhan(), LocalDateTime.now()).toDays();
                if (days == 0) days = 1;
                total += days * ct.getGiaThuePhong();
            }
        }
        lblTotal.setText("TỔNG CỘNG: " + String.format("%,.0f VNĐ", total * 1.08));
    }

    private void handleCheckout() {
        List<ChiTietPhieuDat> selectedRooms = roomCheckboxes.stream()
            .filter(CheckBox::isSelected)
            .map(cb -> (ChiTietPhieuDat) cb.getUserData())
            .collect(Collectors.toList());

        if (selectedRooms.isEmpty()) {
            showAlert("Thông báo", "Vui lòng chọn ít nhất 1 phòng để trả!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Xác nhận thanh toán cho " + selectedRooms.size() + " phòng?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                performCheckoutProcess(selectedRooms);
            }
        });
    }

    private void performCheckoutProcess(List<ChiTietPhieuDat> rooms) {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                HoaDonDAO hdDAO = new HoaDonDAO();
                HoaDon hdTonTai = hdDAO.getHoaDonByMaPhieu(selectedPDP.getMaDatPhong());
                LocalDateTime now = LocalDateTime.now();
                
                double totalBasePrice = 0;
                for (ChiTietPhieuDat ct : rooms) {
                    long days = Duration.between(ct.getNgayNhan(), now).toDays();
                    if (days == 0) days = 1;
                    totalBasePrice += days * ct.getGiaThuePhong();
                }

                // 1. Cập nhật hóa đơn
                if (hdTonTai == null) {
                    hdDAO.thanhToan(new HoaDon("HD" + System.currentTimeMillis() % 1000000, now, 0.08, totalBasePrice, 0, selectedPDP, loggedInNhanVien), new ArrayList<>());
                } else {
                    hdDAO.updateTongTien(hdTonTai.getMaHoaDon(), totalBasePrice, 0);
                }

                // 2. Cập nhật trạng thái từng phòng
                PhongDAO pDAO = new PhongDAO();
                for (ChiTietPhieuDat ct : rooms) {
                    Phong p = pDAO.getPhongByMa(ct.getPhong().getMaPhong());
                    if (p != null) {
                        p.setTrangThai("Trống");
                        pDAO.update(p);
                    }
                }

                // 3. Kiểm tra xem toàn bộ các phòng trong phiếu đã được trả chưa
                try (java.sql.Connection con = connectDB.ConnectDB.getConnection(); 
                     java.sql.PreparedStatement stCnt = con.prepareStatement(
                        "SELECT COUNT(*) FROM Phong p JOIN ChiTietPhieuDat ct ON p.maPhong = ct.maPhong " +
                        "WHERE ct.maDatPhong = ? AND p.trangThai = N'Đang ở'")) {
                    stCnt.setString(1, selectedPDP.getMaDatPhong());
                    java.sql.ResultSet rs = stCnt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (java.sql.PreparedStatement stUpd = con.prepareStatement("UPDATE PhieuDatPhong SET trangThai = N'DaThanhToan' WHERE maDatPhong = ?")) {
                            stUpd.setString(1, selectedPDP.getMaDatPhong());
                            stUpd.executeUpdate();
                        }
                    }
                }
                return null;
            }
        };
        task.setOnSucceeded(e -> {
            showAlert("Thành công", "Đã thanh toán và trả phòng thành công!");
            loadData();
            detailPane.setVisible(false);
        });
        task.setOnFailed(e -> {
            task.getException().printStackTrace();
            showAlert("Lỗi", "Gặp lỗi trong quá trình xử lý!");
        });
        new Thread(task).start();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
