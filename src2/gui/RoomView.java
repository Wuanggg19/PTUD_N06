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

import dao.PhongDAO;
import dao.PhieuDatPhongDAO;
import dao.ChiTietPhieuDatDAO;
import dao.HoaDonDAO;
import dao.ChiTietHoaDonDAO;
import entity.Phong;
import entity.PhieuDatPhong;
import entity.ChiTietPhieuDat;
import entity.HoaDon;
import entity.NhanVien;
import entity.ChiTietHoaDon;

public class RoomView {

    private final String BG_LIGHT = "#f6f6f8";
    private final String PRIMARY_COLOR = "#2c0fbd";
    private final String COLOR_EMPTY = "#2ecc71"; 
    private final String COLOR_OCCUPIED = "#e74c3c"; 
    private final String COLOR_REPAIR = "#f1c40f"; 
    
    private FlowPane roomContainer;
    private List<Phong> allRooms;
    private ComboBox<String> cbStatus;
    private ComboBox<String> cbType;
    private TextField txtSearchMaPhong;
    private java.util.function.Consumer<Phong> onBookingRequest;

    public void setOnBookingRequest(java.util.function.Consumer<Phong> callback) {
        this.onBookingRequest = callback;
    }

    public Node createView() {
        System.out.println("DEBUG: Creating RoomView UI...");
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);

        javafx.scene.control.Label lblTitle = new javafx.scene.control.Label("SƠ ĐỒ PHÒNG");
        lblTitle.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 28));
        lblTitle.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

        txtSearchMaPhong = new TextField();
        txtSearchMaPhong.setPromptText("Tìm mã phòng...");
        txtSearchMaPhong.setPrefWidth(150);
        txtSearchMaPhong.setPrefHeight(35);
        txtSearchMaPhong.textProperty().addListener((o, ov, nv) -> filterRooms());

        cbStatus = new ComboBox<>();
        cbStatus.getItems().addAll("Tất cả trạng thái", "Trống", "Đang ở", "Sửa chữa");
        cbStatus.setValue("Tất cả trạng thái");
        cbStatus.setPrefWidth(150);
        cbStatus.setPrefHeight(35);
        cbStatus.setOnAction(e -> filterRooms());

        cbType = new ComboBox<>();
        cbType.getItems().addAll("Tất cả loại phòng", "Standard", "Deluxe", "Suite", "VIP");
        cbType.setValue("Tất cả loại phòng");
        cbType.setPrefWidth(150);
        cbType.setPrefHeight(35);
        cbType.setOnAction(e -> filterRooms());

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnRefresh.setPrefHeight(35);
        btnRefresh.setOnAction(e -> loadData());

        header.getChildren().addAll(lblTitle, spacer, txtSearchMaPhong, cbStatus, cbType, btnRefresh);

        javafx.scene.layout.HBox legend = new javafx.scene.layout.HBox(20);
        legend.getChildren().addAll(
            createLegendItem("Trống", COLOR_EMPTY),
            createLegendItem("Đang ở", COLOR_OCCUPIED),
            createLegendItem("Sửa chữa", COLOR_REPAIR)
        );

        roomContainer = new javafx.scene.layout.FlowPane(20, 20);
        roomContainer.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
        
        javafx.scene.control.ScrollPane scrollPane = new javafx.scene.control.ScrollPane(roomContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: " + BG_LIGHT + ";");
        javafx.scene.layout.VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

        root.getChildren().addAll(header, legend, scrollPane);

        loadData();
        return root;
    }

    private javafx.scene.layout.HBox createLegendItem(String text, String colorHex) {
        javafx.scene.layout.HBox hb = new javafx.scene.layout.HBox(8);
        hb.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        javafx.scene.layout.Region dot = new javafx.scene.layout.Region();
        dot.setPrefSize(15, 15);
        dot.setStyle("-fx-background-color: " + colorHex + "; -fx-background-radius: 3;");
        javafx.scene.control.Label lbl = new javafx.scene.control.Label(text);
        lbl.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.SEMI_BOLD, 14));
        hb.getChildren().addAll(dot, lbl);
        return hb;
    }

    public void loadData() {
        Task<List<Phong>> task = new Task<List<Phong>>() {
            @Override
            protected List<Phong> call() throws Exception {
                return new PhongDAO().getAllPhong();
            }
        };
        task.setOnSucceeded(e -> {
            allRooms = task.getValue();
            if (allRooms == null || allRooms.isEmpty()) {
                roomContainer.getChildren().clear();
                roomContainer.getChildren().add(new Label("Không có dữ liệu!"));
            } else filterRooms();
        });
        new Thread(task).start();
    }

    private void filterRooms() {
        if (allRooms == null) return;
        String search = txtSearchMaPhong.getText().toLowerCase().trim();
        String statusFilter = cbStatus.getValue();
        String typeFilter = cbType.getValue();

        List<Phong> filtered = allRooms.stream()
            .filter(p -> search.isEmpty() || p.getMaPhong().toLowerCase().contains(search))
            .filter(p -> statusFilter.equals("Tất cả trạng thái") || p.getTrangThai().equals(statusFilter))
            .filter(p -> typeFilter.equals("Tất cả loại phòng") || p.getLoaiPhong().contains(typeFilter))
            .collect(Collectors.toList());

        roomContainer.getChildren().clear();
        for (Phong p : filtered) roomContainer.getChildren().add(createRoomCard(p));
    }

    private Node createRoomCard(Phong p) {
        VBox card = new VBox(10);
        card.setPrefSize(160, 160);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        
        String color;
        switch (p.getTrangThai()) {
            case "Đang ở": color = COLOR_OCCUPIED; break;
            case "Sửa chữa": color = COLOR_REPAIR; break;
            default: color = COLOR_EMPTY; break;
        }

        card.setStyle("-fx-background-color: white; -fx-border-color: " + color + "; -fx-border-width: 0 0 5 0; " +
                     "-fx-background-radius: 8; -fx-border-radius: 0 0 8 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3); -fx-cursor: hand;");

        Label lblId = new Label(p.getMaPhong());
        lblId.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblId.setTextFill(Color.web("#2c3e50"));

        Label lblType = new Label(p.getLoaiPhong());
        lblType.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        lblType.setTextFill(Color.GRAY);

        Label lblStatus = new Label(p.getTrangThai().toUpperCase());
        lblStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lblStatus.setTextFill(Color.web(color));

        card.getChildren().addAll(lblId, lblType, lblStatus);

        card.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) showRoomDetailDialog(p);
        });

        ContextMenu menu = new ContextMenu();
        MenuItem miCheckin = new MenuItem("Nhận phòng");
        MenuItem miCheckout = new MenuItem("Thanh toán & Trả phòng");
        MenuItem miChange = new MenuItem("Đổi phòng");
        MenuItem miDetail = new MenuItem("Xem chi tiết");

        if ("Trống".equals(p.getTrangThai())) {
            menu.getItems().addAll(miCheckin, miDetail);
        } else if ("Đang ở".equals(p.getTrangThai())) {
            menu.getItems().addAll(miChange, miCheckout, miDetail);
        } else menu.getItems().add(miDetail);

        card.setOnContextMenuRequested(e -> menu.show(card, e.getScreenX(), e.getScreenY()));
        miCheckout.setOnAction(e -> showCheckoutDialog(p));
        miChange.setOnAction(e -> showChangeRoomDialog(p));

        return card;
    }

    private void showRoomDetailDialog(Phong p) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi tiết phòng " + p.getMaPhong());
        ButtonType closeBtn = new ButtonType("Đóng", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeBtn);

        HBox root = new HBox(30); root.setPadding(new Insets(20)); root.setPrefWidth(650);
        javafx.scene.layout.VBox infoBox = new javafx.scene.layout.VBox(15); 
        infoBox.setPrefWidth(380);
        javafx.scene.control.Label lblHeader = new javafx.scene.control.Label("THÔNG TIN PHÒNG " + p.getMaPhong());
        lblHeader.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 22)); 
        lblHeader.setTextFill(javafx.scene.paint.Color.web(PRIMARY_COLOR));

        GridPane grid = new GridPane(); grid.setHgap(15); grid.setVgap(12);
        addRow(grid, 0, "Loại phòng:", p.getLoaiPhong());
        addRow(grid, 1, "Số giường:", String.valueOf(p.getSoGiuong()));
        addRow(grid, 2, "Giá niêm yết:", String.format("%,.0f VNĐ", p.getGiaPhong()));
        addRow(grid, 3, "Trạng thái:", p.getTrangThai());
        infoBox.getChildren().addAll(lblHeader, new Separator(), grid);

        if ("Đang ở".equals(p.getTrangThai())) {
            PhieuDatPhong activePDP = new PhieuDatPhongDAO().getActivePhieuByMaPhong(p.getMaPhong());
            if (activePDP != null) {
                VBox guestBox = new VBox(10); guestBox.setPadding(new Insets(20, 0, 0, 0));
                Label lblGuestHeader = new Label("THÔNG TIN KHÁCH HÀNG");
                lblGuestHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16)); lblGuestHeader.setTextFill(Color.web("#e67e22"));
                GridPane guestGrid = new GridPane(); guestGrid.setHgap(15); guestGrid.setVgap(10);
                addRow(guestGrid, 0, "Khách hàng:", activePDP.getKhachHang().getTenKhachHang());
                addRow(guestGrid, 1, "Mã phiếu:", activePDP.getMaDatPhong());
                addRow(guestGrid, 2, "Ngày vào:", activePDP.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                guestBox.getChildren().addAll(new Separator(), lblGuestHeader, guestGrid);
                infoBox.getChildren().add(guestBox);
            }
        }

        javafx.scene.layout.VBox actionBox = new javafx.scene.layout.VBox(12); 
        actionBox.setAlignment(javafx.geometry.Pos.TOP_CENTER); 
        actionBox.setPrefWidth(220); 
        actionBox.setPadding(new javafx.geometry.Insets(50, 0, 0, 0));
        if ("Trống".equals(p.getTrangThai())) {
            actionBox.getChildren().addAll(createActionButton("NHẬN PHÒNG NHANH", "#2ecc71"), createActionButton("ĐẶT PHÒNG TRƯỚC", "#3498db"));
        } else if ("Đang ở".equals(p.getTrangThai())) {
            Button btnChange = createActionButton("ĐỔI PHÒNG", "#f39c12");
            btnChange.setOnAction(e -> { 
                dialog.close(); 
                javafx.application.Platform.runLater(() -> showChangeRoomDialog(p)); 
            });
            Button btnCheckout = createActionButton("THANH TOÁN & TRẢ PHÒNG", "#e74c3c");
            btnCheckout.setOnAction(e -> { 
                dialog.close(); 
                javafx.application.Platform.runLater(() -> showCheckoutDialog(p)); 
            });
            actionBox.getChildren().addAll(createActionButton("THÊM DỊCH VỤ", "#9b59b6"), btnChange, btnCheckout);
        } else actionBox.getChildren().add(createActionButton("HOÀN TẤT SỬA CHỮA", "#2c3e50"));

        root.getChildren().addAll(infoBox, actionBox);
        dialog.getDialogPane().setContent(root);
        System.out.println("DEBUG: Showing room detail dialog for " + p.getMaPhong());
        dialog.showAndWait();
    }

    private void showChangeRoomDialog(Phong oldRoom) {
        try {
            System.out.println("Bắt đầu xử lý đổi phòng cho: " + oldRoom.getMaPhong());
            PhieuDatPhong activePDP = new PhieuDatPhongDAO().getActivePhieuByMaPhong(oldRoom.getMaPhong());
            
            if (activePDP == null) {
                showAlert("Thông báo", "Không tìm thấy phiếu đặt phòng 'DaNhanPhong' cho phòng " + oldRoom.getMaPhong());
                return;
            }

            Dialog<Phong> dialog = new Dialog<>();
            dialog.setTitle("ĐỔI PHÒNG: " + oldRoom.getMaPhong());
            dialog.setHeaderText("Chọn phòng trống để chuyển khách sang:");
            ButtonType confirmBtn = new ButtonType("Xác nhận đổi", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(confirmBtn, ButtonType.CANCEL);

            ListView<Phong> lvVacant = new ListView<>();
            lvVacant.setPrefHeight(250);
            
            Task<List<Phong>> task = new Task<List<Phong>>() {
                @Override protected List<Phong> call() throws Exception { 
                    return new PhongDAO().filterPhong("Trống", null); 
                }
            };
            task.setOnSucceeded(e -> lvVacant.getItems().setAll(task.getValue()));
            new Thread(task).start();

            lvVacant.setCellFactory(lv -> new ListCell<Phong>() {
                @Override protected void updateItem(Phong p, boolean empty) {
                    super.updateItem(p, empty);
                    if (empty || p == null) setText(null);
                    else setText(p.getMaPhong() + " - " + p.getLoaiPhong() + " (" + String.format("%,.0f VNĐ", p.getGiaPhong()) + ")");
                }
            });

            dialog.getDialogPane().setContent(lvVacant);
            dialog.setResultConverter(btn -> (btn == confirmBtn) ? lvVacant.getSelectionModel().getSelectedItem() : null);

            dialog.showAndWait().ifPresent(newRoom -> {
                if (newRoom == null) {
                    showAlert("Lỗi", "Bạn chưa chọn phòng mới!");
                    return;
                }
                boolean ok = new PhieuDatPhongDAO().doiPhong(activePDP.getMaDatPhong(), oldRoom.getMaPhong(), newRoom.getMaPhong(), newRoom.getGiaPhong());
                if (ok) {
                    showAlert("Thành công", "Đã chuyển khách sang phòng " + newRoom.getMaPhong());
                    loadData();
                } else {
                    showAlert("Lỗi", "Lỗi khi cập nhật database (doiPhong thất bại)");
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi hệ thống", "Có lỗi xảy ra: " + ex.getMessage());
        }
    }

    private void showCheckoutDialog(Phong p) {
        try {
            PhieuDatPhong activePDP = new PhieuDatPhongDAO().getActivePhieuByMaPhong(p.getMaPhong());
            if (activePDP == null) { showAlert("Lỗi", "Không tìm thấy lượt ở!"); return; }

            List<ChiTietPhieuDat> dsCT = new ChiTietPhieuDatDAO().getDSChiTietByMaPhieu(activePDP.getMaDatPhong());
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("THANH TOÁN CHI TIẾT - " + p.getMaPhong());
            VBox layout = new VBox(15); layout.setPadding(new Insets(20)); layout.setPrefWidth(550);
            
            VBox detailsBox = new VBox(10);
            double tongTienPhong = 0;
            LocalDateTime now = LocalDateTime.now();

            for (ChiTietPhieuDat ct : dsCT) {
                LocalDateTime ngayTraThucTe = (ct.getPhong().getMaPhong().equals(p.getMaPhong())) ? now : ct.getNgayTra();
                long days = Duration.between(ct.getNgayNhan(), ngayTraThucTe).toDays();
                if (days == 0) days = 1;
                double thanhTien = days * ct.getGiaThuePhong();
                tongTienPhong += thanhTien;
                detailsBox.getChildren().add(new Label(String.format("• Phòng %s: %d ngày x %,.0f = %,.0f VNĐ", ct.getPhong().getMaPhong(), days, ct.getGiaThuePhong(), thanhTien)));
            }

            double vat = tongTienPhong * 0.08;
            Label lblTotal = new Label("TỔNG TIỀN (GỒM 8% VAT): " + String.format("%,.0f VNĐ", (tongTienPhong + vat)));
            lblTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16)); lblTotal.setTextFill(Color.RED);

            layout.getChildren().addAll(new Label("CHI TIẾT THANH TOÁN"), new Separator(), detailsBox, new Separator(), lblTotal);
            dialog.getDialogPane().setContent(layout);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

            dialog.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    try {
                        double finalTongTienPhong = tongTienPhong; // effectively final copy
                        new HoaDonDAO().thanhToan(new entity.HoaDon("HD"+System.currentTimeMillis()%1000000, now, 0.08, finalTongTienPhong, 0, activePDP, activePDP.getNhanVien()), new java.util.ArrayList<>());
                        PhongDAO pDAO = new PhongDAO();
                        for (ChiTietPhieuDat ct : dsCT) {
                            Phong target = pDAO.getPhongByMa(ct.getPhong().getMaPhong());
                            if (target != null) {
                                target.setTrangThai("Trống");
                                pDAO.update(target);
                            }
                        }
                        try (java.sql.Connection con = connectDB.ConnectDB.getConnection(); java.sql.PreparedStatement st = con.prepareStatement("UPDATE PhieuDatPhong SET trangThai = N'DaThanhToan' WHERE maDatPhong = ?")) {
                            st.setString(1, activePDP.getMaDatPhong()); st.executeUpdate();
                        }
                        loadData();
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert("Lỗi hệ thống", "Lỗi khi thanh toán: " + ex.getMessage());
        }
    }

    private void addRow(GridPane grid, int row, String label, String value) {
        Label lbl = new Label(label); lbl.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14)); lbl.setTextFill(Color.GRAY);
        Label val = new Label(value); val.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14)); val.setTextFill(Color.web("#2c3e50"));
        grid.add(lbl, 0, row); grid.add(val, 1, row);
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text); btn.setMaxWidth(Double.MAX_VALUE); btn.setPrefHeight(45);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
        return btn;
    }

    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION); 
        alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(content); alert.showAndWait();
    }
}
