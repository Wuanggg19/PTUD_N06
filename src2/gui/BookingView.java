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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import dao.KhachHangDAO;
import dao.PhongDAO;
import dao.PhieuDatPhongDAO;
import entity.KhachHang;
import entity.Phong;
import entity.PhieuDatPhong;
import entity.ChiTietPhieuDat;
import entity.NhanVien;

public class BookingView {

    private final String BG_LIGHT = "#f6f6f8";
    private final String PRIMARY_COLOR = "#2c0fbd";
    private final String DANGER_COLOR = "#e74c3c";
    
    private TextField txtSdt, txtTen, txtDiaChi;
    private RadioButton rbNam, rbNu;
    private DatePicker dpNgayNhan, dpNgayTra;
    private TableView<Phong> tvPhongTrong;
    private VBox selectedRoomsContainer; 
    private List<Phong> selectedRooms = new ArrayList<>();
    private final NhanVien loggedInNhanVien;

    private KhachHang currentKH = null;
    private Task<KhachHang> searchTask = null;
    private boolean isLoading = false;

    public BookingView(NhanVien nhanVien) {
        this.loggedInNhanVien = nhanVien;
    }

    public Node createView() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        Label lblTitle = new Label("LẬP PHIẾU ĐẶT PHÒNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        HBox mainContent = new HBox(30);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // --- CỘT TRÁI: THÔNG TIN KHÁCH ---
        VBox leftCol = new VBox(20);
        leftCol.setPrefWidth(420);
        leftCol.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5);");

        Label lblKH = new Label("1. THÔNG TIN KHÁCH HÀNG");
        lblKH.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        txtSdt = createStyledTextField("Nhập số điện thoại khách...");
        txtTen = createStyledTextField("Họ và tên khách hàng");
        txtDiaChi = createStyledTextField("Địa chỉ (Tỉnh/Thành phố)");
        
        txtSdt.textProperty().addListener((obs, oldVal, newVal) -> {
            resetStyle(txtSdt);
            if (newVal.length() < 10) {
                currentKH = null;
                txtTen.clear();
                txtDiaChi.clear();
                txtTen.setEditable(true);
            } else {
                searchKhachHang(newVal);
            }
        });
        txtTen.textProperty().addListener((o, v, n) -> resetStyle(txtTen));
        txtDiaChi.textProperty().addListener((o, v, n) -> resetStyle(txtDiaChi));
        
        ToggleGroup groupGT = new ToggleGroup();
        rbNam = new RadioButton("Nam"); rbNam.setToggleGroup(groupGT); rbNam.setSelected(true);
        rbNu = new RadioButton("Nữ"); rbNu.setToggleGroup(groupGT);
        HBox gtBox = new HBox(20, new Label("Giới tính:"), rbNam, rbNu);
        gtBox.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator();

        Label lblTime = new Label("2. THỜI GIAN ĐẶT PHÒNG");
        lblTime.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        dpNgayNhan = new DatePicker(LocalDate.now()); dpNgayNhan.setPrefHeight(40); dpNgayNhan.setMaxWidth(Double.MAX_VALUE);
        dpNgayTra = new DatePicker(LocalDate.now().plusDays(1)); dpNgayTra.setPrefHeight(40); dpNgayTra.setMaxWidth(Double.MAX_VALUE);
        
        dpNgayNhan.setOnAction(e -> resetStyle(dpNgayNhan));
        dpNgayTra.setOnAction(e -> resetStyle(dpNgayTra));

        leftCol.getChildren().addAll(lblKH, txtSdt, txtTen, txtDiaChi, gtBox, sep, lblTime, 
                                   new Label("Ngày nhận phòng:"), dpNgayNhan, 
                                   new Label("Ngày trả dự kiến:"), dpNgayTra);

        // PHÍM TẮT ENTER
        txtSdt.setOnAction(e -> txtTen.requestFocus());
        txtTen.setOnAction(e -> txtDiaChi.requestFocus());
        txtDiaChi.setOnAction(e -> dpNgayNhan.requestFocus());
        dpNgayNhan.getEditor().setOnAction(e -> dpNgayTra.requestFocus());
        dpNgayTra.getEditor().setOnAction(e -> tvPhongTrong.requestFocus());

        // --- CỘT PHẢI: CHỌN PHÒNG ---
        VBox rightCol = new VBox(20);
        HBox.setHgrow(rightCol, Priority.ALWAYS);

        VBox roomBox = new VBox(15);
        roomBox.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 5);");
        VBox.setVgrow(roomBox, Priority.ALWAYS);

        HBox roomHeader = new HBox(10);
        roomHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblRoom = new Label("3. CHỌN PHÒNG ĐANG TRỐNG");
        lblRoom.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        
        Button btnRefreshRoom = new Button("🔄");
        btnRefreshRoom.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-font-size: 16;");
        btnRefreshRoom.setTooltip(new Tooltip("Tải lại danh sách phòng trống"));
        btnRefreshRoom.setOnAction(e -> loadPhongTrong());
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        roomHeader.getChildren().addAll(lblRoom, spacer, btnRefreshRoom);

        tvPhongTrong = new TableView<>();
        tvPhongTrong.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<Phong, String> colMa = new TableColumn<>("Mã Phòng");
        colMa.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMaPhong()));
        TableColumn<Phong, String> colLoai = new TableColumn<>("Loại");
        colLoai.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLoaiPhong()));
        TableColumn<Phong, String> colGia = new TableColumn<>("Giá/Ngày");
        colGia.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.format("%,.0f VNĐ", data.getValue().getGiaPhong())));
        
        tvPhongTrong.getColumns().addAll(colMa, colLoai, colGia);
        
        // HIGHLIGHT PHÒNG ĐÃ CHỌN
        tvPhongTrong.setRowFactory(tv -> {
            TableRow<Phong> row = new TableRow<Phong>() {
                @Override
                protected void updateItem(Phong item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setStyle("");
                    } else if (selectedRooms.contains(item)) {
                        setStyle("-fx-background-color: #3498db; -fx-text-background-color: white;");
                    } else {
                        setStyle("");
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Phong p = row.getItem();
                    if (selectedRooms.contains(p)) {
                        selectedRooms.remove(p);
                    } else {
                        selectedRooms.add(p);
                    }
                    tvPhongTrong.refresh(); // Buộc bảng vẽ lại để hiện highlight
                    renderSelectedRooms();
                }
            });
            return row;
        });

        Label lblSel = new Label("DANH SÁCH PHÒNG ĐÃ CHỌN:");
        lblSel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        selectedRoomsContainer = new VBox(10);
        ScrollPane spSelected = new ScrollPane(selectedRoomsContainer);
        spSelected.setFitToWidth(true);
        spSelected.setPrefHeight(150);
        spSelected.setStyle("-fx-background-color: transparent; -fx-background: #f9f9fb;");

        HBox actionButtons = new HBox(15);
        Button btnSave = new Button("XÁC NHẬN ĐẶT PHÒNG");
        btnSave.setPrefHeight(50);
        HBox.setHgrow(btnSave, Priority.ALWAYS);
        btnSave.setMaxWidth(Double.MAX_VALUE);
        btnSave.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16; -fx-cursor: hand; -fx-background-radius: 8;");
        btnSave.setOnAction(e -> handleBooking());

        Button btnReset = new Button("LÀM MỚI");
        btnReset.setPrefHeight(50);
        btnReset.setMinWidth(120);
        btnReset.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14; -fx-cursor: hand; -fx-background-radius: 8;");
        btnReset.setOnAction(e -> clearFields());

        actionButtons.getChildren().addAll(btnReset, btnSave);

        roomBox.getChildren().addAll(roomHeader, tvPhongTrong, lblSel, spSelected, actionButtons);
        rightCol.getChildren().add(roomBox);

        mainContent.getChildren().addAll(leftCol, rightCol);
        root.getChildren().addAll(lblTitle, mainContent);

        loadPhongTrong();
        return root;
    }

    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
        tf.setStyle("-fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
        return tf;
    }

    private void resetStyle(Control c) {
        c.setStyle("-fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
    }

    private void setInvalidStyle(Control c) {
        c.setStyle("-fx-background-radius: 6; -fx-border-color: " + DANGER_COLOR + "; -fx-border-radius: 6; -fx-border-width: 2px;");
    }

    private void renderSelectedRooms() {
        selectedRoomsContainer.getChildren().clear();
        for (Phong p : selectedRooms) {
            HBox card = new HBox(15);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(10, 20, 10, 20));
            card.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 8; -fx-border-color: #e1e2e6; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.02), 5, 0, 0, 2);");

            Label lblInfo = new Label(p.getMaPhong() + " - " + p.getLoaiPhong() + " (" + String.format("%,.0f VNĐ", p.getGiaPhong()) + ")");
            lblInfo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            HBox.setHgrow(lblInfo, Priority.ALWAYS);

            Button btnRemove = new Button("✕");
            btnRemove.setMinWidth(32); btnRemove.setMinHeight(32);
            btnRemove.setStyle("-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 16; -fx-cursor: hand;");
            btnRemove.setVisible(false);

            card.setOnMouseEntered(e -> btnRemove.setVisible(true));
            card.setOnMouseExited(e -> btnRemove.setVisible(false));

            btnRemove.setOnAction(e -> {
                selectedRooms.remove(p);
                tvPhongTrong.refresh();
                renderSelectedRooms();
            });

            card.getChildren().addAll(lblInfo, btnRemove);
            selectedRoomsContainer.getChildren().add(card);
        }
    }

    private void handleBooking() {
        List<String> errors = new ArrayList<>();
        Control firstErrorControl = null;

        if (txtSdt.getText().trim().isEmpty() || !txtSdt.getText().matches("^0\\d{9,10}$")) {
            errors.add("- Số điện thoại không hợp lệ.");
            setInvalidStyle(txtSdt);
            if (firstErrorControl == null) firstErrorControl = txtSdt;
        }
        if (txtTen.getText().trim().isEmpty()) {
            errors.add("- Vui lòng nhập tên khách hàng.");
            setInvalidStyle(txtTen);
            if (firstErrorControl == null) firstErrorControl = txtTen;
        }
        if (txtDiaChi.getText().trim().isEmpty()) {
            errors.add("- Vui lòng nhập địa chỉ.");
            setInvalidStyle(txtDiaChi);
            if (firstErrorControl == null) firstErrorControl = txtDiaChi;
        }

        LocalDate nhan = dpNgayNhan.getValue();
        LocalDate tra = dpNgayTra.getValue();
        if (nhan == null) {
            errors.add("- Vui lòng chọn ngày nhận phòng.");
            setInvalidStyle(dpNgayNhan);
            if (firstErrorControl == null) firstErrorControl = dpNgayNhan;
        }
        if (tra == null || (nhan != null && !tra.isAfter(nhan))) {
            errors.add("- Ngày trả phải sau ngày nhận.");
            setInvalidStyle(dpNgayTra);
            if (firstErrorControl == null) firstErrorControl = dpNgayTra;
        }
        if (selectedRooms.isEmpty()) {
            errors.add("- Vui lòng chọn ít nhất 1 phòng.");
            tvPhongTrong.setStyle("-fx-border-color: " + DANGER_COLOR + "; -fx-border-width: 2px;");
            if (firstErrorControl == null) firstErrorControl = tvPhongTrong;
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi nhập liệu");
            alert.setHeaderText("Phát hiện " + errors.size() + " mục chưa đúng:");
            alert.setContentText(String.join("\n", errors));
            final Control focusTarget = firstErrorControl;
            alert.showAndWait().ifPresent(r -> { if (focusTarget != null) focusTarget.requestFocus(); });
            return;
        }

        Task<Boolean> saveTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                KhachHangDAO khDAO = new KhachHangDAO();
                if (currentKH == null) {
                    String maKH = "KH" + System.currentTimeMillis() % 1000000;
                    currentKH = new KhachHang(maKH, txtTen.getText(), txtDiaChi.getText(), rbNam.isSelected(), txtSdt.getText());
                    khDAO.create(currentKH);
                } else {
                    // Nếu Tên hoặc SĐT hiện tại khác với dữ liệu cũ của khách hàng này 
                    // -> Tạo khách hàng mới để giữ tính toàn vẹn cho hóa đơn cũ
                    if (!currentKH.getTenKhachHang().equals(txtTen.getText()) || !currentKH.getSoDienThoai().equals(txtSdt.getText())) {
                        String maNew = "KH" + System.currentTimeMillis() % 1000000;
                        currentKH = new KhachHang(maNew, txtTen.getText(), txtDiaChi.getText(), rbNam.isSelected(), txtSdt.getText());
                        khDAO.create(currentKH);
                    } else {
                        // Chỉ cập nhật thông tin phụ nếu cần
                        currentKH.setDiaChi(txtDiaChi.getText());
                        currentKH.setGioiTinh(rbNam.isSelected());
                        khDAO.update(currentKH);
                    }
                }
                String maDP = "DP" + System.currentTimeMillis() % 1000000;
                PhieuDatPhong pdp = new PhieuDatPhong(maDP, LocalDateTime.now(), "DaNhanPhong", loggedInNhanVien, currentKH);
                List<ChiTietPhieuDat> dsCT = new ArrayList<>();
                for (Phong p : selectedRooms) {
                    dsCT.add(new ChiTietPhieuDat(pdp, p, p.getGiaPhong(), LocalDateTime.of(nhan, LocalTime.of(14, 0)), LocalDateTime.of(tra, LocalTime.of(12, 0))));
                }
                return new PhieuDatPhongDAO().datPhong(pdp, dsCT);
            }
        };
        saveTask.setOnSucceeded(e -> {
            if (saveTask.getValue()) {
                showAlert("Thành công", "Đã tạo phiếu đặt phòng thành công!");
                clearFields();
                loadPhongTrong();
            } else showAlert("Lỗi", "Không thể lưu dữ liệu!");
        });
        new Thread(saveTask).start();
    }

    private void searchKhachHang(String sdt) {
        if (searchTask != null && searchTask.isRunning()) {
            searchTask.cancel();
        }
        searchTask = new Task<KhachHang>() {
            @Override protected KhachHang call() throws Exception { 
                return new KhachHangDAO().getKhachHangBySdt(sdt); 
            }
        };
        searchTask.setOnSucceeded(e -> {
            if (!sdt.equals(txtSdt.getText())) return;
            
            currentKH = searchTask.getValue();
            if (currentKH != null) {
                txtTen.setText(currentKH.getTenKhachHang());
                txtDiaChi.setText(currentKH.getDiaChi());
                if (currentKH.isGioiTinh()) rbNam.setSelected(true); else rbNu.setSelected(true);
                txtTen.setEditable(true);
            } else {
                txtTen.clear();
                txtDiaChi.clear();
                txtTen.setEditable(true);
            }
        });
        new Thread(searchTask).start();
    }

    public void loadPhongTrong() {
        if (isLoading) return;
        isLoading = true;

        Task<List<Phong>> task = new Task<List<Phong>>() {
            @Override protected List<Phong> call() throws Exception { 
                return new PhongDAO().filterPhong("Trống", null); 
            }
        };
        task.setOnSucceeded(e -> {
            isLoading = false;
            List<Phong> list = task.getValue();
            if (list != null) {
                tvPhongTrong.getItems().setAll(list);
                tvPhongTrong.refresh();
            }
        });
        task.setOnFailed(ev -> {
            isLoading = false;
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
            javafx.application.Platform.runLater(() -> {
                showAlert("Lỗi", "Không thể tải danh sách phòng trống: " + (ex != null ? ex.getMessage() : "Unknown error"));
            });
        });
        new Thread(task).start();
    }

    public void preSelectRoom(Phong p) {
        clearFields();
        // Tải lại dữ liệu ngay lập tức
        loadPhongTrong();
        
        // Chờ UI sẵn sàng rồi mới thực hiện chọn
        javafx.application.Platform.runLater(() -> {
            if (p != null) {
                selectedRooms.add(p);
                renderSelectedRooms();
                tvPhongTrong.refresh();
            }
        });
    }

    private void clearFields() {
        txtSdt.clear(); txtTen.clear(); txtDiaChi.clear();
        resetStyle(txtSdt); resetStyle(txtTen); resetStyle(txtDiaChi);
        resetStyle(dpNgayNhan); resetStyle(dpNgayTra);
        tvPhongTrong.setStyle(""); tvPhongTrong.getSelectionModel().clearSelection();
        selectedRooms.clear(); renderSelectedRooms();
        currentKH = null;
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION); 
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}
