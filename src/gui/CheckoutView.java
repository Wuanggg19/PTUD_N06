package gui;

import dao.ChiTietPhieuDatDAO;
import dao.HoaDonDAO;
import dao.PhieuDatPhongDAO;
import dao.PhongDAO;
import entity.ChiTietPhieuDat;
import entity.HoaDon;
import entity.NhanVien;
import entity.PhieuDatPhong;
import entity.Phong;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import util.AppTheme;
import util.BookingStatus;
import util.PricingService;
import util.RoomStatus;
import util.StatusUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CheckoutView {
    private final NhanVien loggedInNhanVien;
    private final PricingService pricingService = new PricingService();
    private final ChiTietPhieuDatDAO chiTietPhieuDatDAO = new ChiTietPhieuDatDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhieuDatPhongDAO phieuDatPhongDAO = new PhieuDatPhongDAO();
    private final PhongDAO phongDAO = new PhongDAO();

    private final TableView<PhieuDatPhong> tvBookings = new TableView<>();
    private final VBox roomsContainer = new VBox(8);
    private final List<CheckBox> roomCheckboxes = new ArrayList<>();

    private TextField txtSearch;
    private Label lblGuestName;
    private Label lblGuestPhone;
    private Label lblTotal;
    private VBox detailPane;

    private List<PhieuDatPhong> allBookings = new ArrayList<>();
    private PhieuDatPhong selectedPDP;

    public CheckoutView(NhanVien nhanVien) {
        this.loggedInNhanVien = nhanVien;
    }

    public Node createView() {
        HBox root = new HBox(18);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + AppTheme.BG_LIGHT + ";");

        VBox left = new VBox(12);
        left.setPrefWidth(560);
        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã phiếu, khách hàng, phòng...");
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> filterBookings());
        configureTable();
        left.getChildren().addAll(sectionLabel("TRẢ PHÒNG"), txtSearch, tvBookings);
        VBox.setVgrow(tvBookings, Priority.ALWAYS);

        detailPane = new VBox(12);
        detailPane.setPadding(new Insets(20));
        detailPane.setStyle("-fx-background-color: white; -fx-background-radius: 14;");
        HBox.setHgrow(detailPane, Priority.ALWAYS);

        lblGuestName = new Label();
        lblGuestPhone = new Label();
        lblTotal = new Label("0 VNĐ");
        lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTotal.setTextFill(Color.web(AppTheme.DANGER));

        ScrollPane scrollPane = new ScrollPane(roomsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(320);

        Button btnCheckout = new Button("Xác nhận trả phòng");
        btnCheckout.setTextFill(Color.WHITE);
        btnCheckout.setStyle("-fx-background-color: " + AppTheme.PRIMARY + "; -fx-background-radius: 8;");
        btnCheckout.setPrefHeight(42);
        btnCheckout.setOnAction(e -> handleCheckout());

        detailPane.getChildren().addAll(
                sectionLabel("CHI TIẾT THANH TOÁN"),
                new Label("Khách hàng"), lblGuestName,
                new Label("Số điện thoại"), lblGuestPhone,
                new Label("Chọn phòng cần trả"),
                scrollPane,
                new Label("Tổng tiền"), lblTotal,
                btnCheckout
        );

        root.getChildren().addAll(left, detailPane);
        loadData();
        return root;
    }

    public void selectRoom(String maPhong) {
        if (txtSearch != null) {
            txtSearch.setText(maPhong);
            filterBookings();
        }
    }

    public void loadData() {
        Task<List<PhieuDatPhong>> task = new Task<>() {
            @Override
            protected List<PhieuDatPhong> call() {
                return phieuDatPhongDAO.getAllPhieuDatPhong().stream()
                        .filter(p -> StatusUtils.isBookingStatus(p.getTrangThai(), BookingStatus.DA_NHAN_PHONG))
                        .collect(Collectors.toList());
            }
        };
        task.setOnSucceeded(e -> {
            allBookings = task.getValue();
            filterBookings();
        });
        new Thread(task).start();
    }

    private void configureTable() {
        tvBookings.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<PhieuDatPhong, String> colMa = new TableColumn<>("Mã phiếu");
        colMa.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMaDatPhong()));
        TableColumn<PhieuDatPhong, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getKhachHang() != null ? data.getValue().getKhachHang().getTenKhachHang() : "Khách lẻ"));
        TableColumn<PhieuDatPhong, String> colPhong = new TableColumn<>("Phòng");
        colPhong.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDsMaPhong()));
        TableColumn<PhieuDatPhong, String> colSoLuong = new TableColumn<>("SL phòng");
        colSoLuong.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getSoLuongPhong())));
        tvBookings.getColumns().setAll(colMa, colKhach, colPhong, colSoLuong);
        tvBookings.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> showDetail(newV));
    }

    private void filterBookings() {
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        tvBookings.getItems().setAll(allBookings.stream()
                .filter(p -> keyword.isBlank()
                        || p.getMaDatPhong().toLowerCase().contains(keyword)
                        || (p.getKhachHang() != null && p.getKhachHang().getTenKhachHang().toLowerCase().contains(keyword))
                        || (p.getDsMaPhong() != null && p.getDsMaPhong().toLowerCase().contains(keyword)))
                .collect(Collectors.toList()));
    }

    private void showDetail(PhieuDatPhong pdp) {
        selectedPDP = pdp;
        if (pdp == null) {
            return;
        }
        lblGuestName.setText(pdp.getKhachHang() != null ? pdp.getKhachHang().getTenKhachHang() : "Khách lẻ");
        lblGuestPhone.setText(pdp.getKhachHang() != null ? pdp.getKhachHang().getSoDienThoai() : "");
        roomsContainer.getChildren().clear();
        roomCheckboxes.clear();

        List<ChiTietPhieuDat> details = chiTietPhieuDatDAO.getDSChiTietByMaPhieu(pdp.getMaDatPhong());
        LocalDateTime checkoutTime = LocalDateTime.now();
        for (ChiTietPhieuDat ct : details) {
            Phong room = phongDAO.getPhongByMa(ct.getPhong().getMaPhong());
            if (room == null || !StatusUtils.isRoomStatus(room.getTrangThai(), RoomStatus.DANG_O)) {
                continue;
            }
            CheckBox cb = new CheckBox();
            cb.setSelected(true);
            cb.setUserData(ct);
            cb.setOnAction(e -> updateTotal());
            roomCheckboxes.add(cb);

            VBox info = new VBox(3);
            info.getChildren().add(new Label(room.getMaPhong() + " | Tầng " + room.getTang() + " | " + room.getLoaiPhong()));
            info.getChildren().add(new Label(
                    ct.getNgayNhan().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " - "
                            + String.format("%,.0f VNĐ", pricingService.calculateRoomCharge(ct, checkoutTime))));
            HBox line = new HBox(10, cb, info);
            line.setAlignment(Pos.CENTER_LEFT);
            roomsContainer.getChildren().add(line);
        }
        updateTotal();
    }

    private void updateTotal() {
        double total = roomCheckboxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (ChiTietPhieuDat) cb.getUserData())
                .mapToDouble(ct -> pricingService.calculateRoomCharge(ct, LocalDateTime.now()))
                .sum();
        lblTotal.setText(String.format("%,.0f VNĐ", total));
    }

    private void handleCheckout() {
        List<ChiTietPhieuDat> selectedRooms = roomCheckboxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> (ChiTietPhieuDat) cb.getUserData())
                .collect(Collectors.toList());
        if (selectedPDP == null || selectedRooms.isEmpty()) {
            showAlert("Thiếu dữ liệu", "Vui lòng chọn phiếu và ít nhất một phòng.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        double totalRoomCharge = pricingService.calculateRoomCharge(selectedRooms, now);
        HoaDon hoaDon = hoaDonDAO.getHoaDonByMaPhieu(selectedPDP.getMaDatPhong());
        if (hoaDon == null) {
            hoaDon = new HoaDon("HD" + System.currentTimeMillis(), now, 0.08, totalRoomCharge, 0, selectedPDP, loggedInNhanVien);
            hoaDonDAO.create(hoaDon);
        } else {
            hoaDonDAO.updateTongTien(hoaDon.getMaHoaDon(), totalRoomCharge, 0);
        }

        for (ChiTietPhieuDat ct : selectedRooms) {
            Phong room = phongDAO.getPhongByMa(ct.getPhong().getMaPhong());
            if (room != null) {
                room.setTrangThai(RoomStatus.TRONG.getCode());
                phongDAO.update(room);
            }
        }

        boolean hasRemaining = chiTietPhieuDatDAO.getDSChiTietByMaPhieu(selectedPDP.getMaDatPhong()).stream()
                .map(ChiTietPhieuDat::getPhong)
                .map(Phong::getMaPhong)
                .map(phongDAO::getPhongByMa)
                .anyMatch(room -> room != null && StatusUtils.isRoomStatus(room.getTrangThai(), RoomStatus.DANG_O));

        if (!hasRemaining) {
            selectedPDP.setTrangThai(BookingStatus.DA_THANH_TOAN.getCode());
            try {
                java.sql.Connection con = connectDB.ConnectDB.getConnection();
                java.sql.PreparedStatement st = con.prepareStatement("UPDATE PhieuDatPhong SET trangThai = ? WHERE maDatPhong = ?");
                st.setString(1, BookingStatus.DA_THANH_TOAN.getCode());
                st.setString(2, selectedPDP.getMaDatPhong());
                st.executeUpdate();
                st.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        loadData();
        showAlert("Thành công", "Đã trả phòng thành công.");
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        label.setTextFill(Color.web(AppTheme.PRIMARY));
        return label;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
