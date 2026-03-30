package gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.util.List;
import java.util.stream.Collectors;

import dao.PhongDAO;
import entity.Phong;

public class RoomManagementView {

    private final String BG_LIGHT = "#f6f6f8";
    private final String PRIMARY_COLOR = "#2c0fbd";

    private TableView<Phong> tableView;
    private List<Phong> allData;
    private TextField txtSearch;

    // Form fields
    private TextField txtMa, txtSoGiuong, txtGia;
    private ComboBox<String> cbLoaiPhong, cbTrangThai;
    private final PhongDAO dao = new PhongDAO();

    public Node createView() {
        HBox root = new HBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- CỘT TRÁI: Bảng danh sách ---
        VBox leftCol = new VBox(15);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        Label lblTitle = new Label("QUẢN LÝ PHÒNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        HBox searchRow = new HBox(10);
        searchRow.setAlignment(Pos.CENTER_LEFT);
        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã phòng hoặc loại phòng...");
        txtSearch.setPrefHeight(42);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 0 12 0 12;");
        txtSearch.textProperty().addListener((o, v, n) -> filterData());
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        Button btnRefresh = new Button("Làm mới");
        btnRefresh.setPrefHeight(42);
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRefresh.setOnAction(e -> loadData());
        searchRow.getChildren().addAll(txtSearch, btnRefresh);

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setStyle("-fx-background-radius: 8;");
        tableView.setRowFactory(tv -> {
            TableRow<Phong> row = new TableRow<>();
            row.setPrefHeight(40);
            return row;
        });
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Phong, String> colMa = new TableColumn<>("Mã Phòng");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaPhong()));
        colMa.setPrefWidth(90);

        TableColumn<Phong, String> colLoai = new TableColumn<>("Loại Phòng");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLoaiPhong()));

        TableColumn<Phong, String> colSoGiuong = new TableColumn<>("Số Giường");
        colSoGiuong.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoGiuong())));
        colSoGiuong.setPrefWidth(90);

        TableColumn<Phong, String> colTrangThai = new TableColumn<>("Trạng Thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));
        colTrangThai.setPrefWidth(100);
        // Tô màu theo trạng thái
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Trống" -> setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        case "Đang ở" -> setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        case "Sửa chữa" -> setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                        default -> setStyle("");
                    }
                }
            }
        });

        TableColumn<Phong, String> colGia = new TableColumn<>("Giá Phòng (VNĐ)");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f", d.getValue().getGiaPhong())));
        colGia.setPrefWidth(140);

        tableView.getColumns().addAll(colMa, colLoai, colSoGiuong, colTrangThai, colGia);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> fillForm(newVal));

        leftCol.getChildren().addAll(lblTitle, searchRow, tableView);

        // --- CỘT PHẢI: Form nhập liệu ---
        VBox rightCol = new VBox(16);
        rightCol.setPrefWidth(290);
        rightCol.setMinWidth(290);
        rightCol.setPadding(new Insets(25));
        rightCol.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 14, 0, 0, 5);");

        Label lblForm = new Label("THÔNG TIN PHÒNG");
        lblForm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblForm.setTextFill(Color.web(PRIMARY_COLOR));

        txtMa = createField("VD: P001");
        txtSoGiuong = createField("VD: 2");
        txtGia = createField("VD: 500000");

        cbLoaiPhong = new ComboBox<>();
        cbLoaiPhong.getItems().addAll("Standard", "Deluxe", "Suite", "VIP", "Family");
        cbLoaiPhong.setValue("Standard");
        cbLoaiPhong.setMaxWidth(Double.MAX_VALUE);
        cbLoaiPhong.setPrefHeight(40);

        cbTrangThai = new ComboBox<>();
        cbTrangThai.getItems().addAll("Trống", "Đang ở", "Sửa chữa");
        cbTrangThai.setValue("Trống");
        cbTrangThai.setMaxWidth(Double.MAX_VALUE);
        cbTrangThai.setPrefHeight(40);

        VBox formFields = new VBox(12);
        formFields.getChildren().addAll(
                formRow("Mã Phòng:", txtMa),
                formRow("Loại Phòng:", cbLoaiPhong),
                formRow("Số Giường:", txtSoGiuong),
                formRow("Trạng Thái:", cbTrangThai),
                formRow("Giá Phòng (VNĐ):", txtGia)
        );

        Button btnThem = createBtn("  THÊM MỚI", "#27ae60");
        Button btnCapNhat = createBtn("  CẬP NHẬT", "#2980b9");
        Button btnXoa = createBtn("  XÓA PHÒNG", "#e74c3c");
        Button btnReset = createBtn("  LÀM MỚI FORM", "#95a5a6");

        btnThem.setOnAction(e -> handleThem());
        btnCapNhat.setOnAction(e -> handleCapNhat());
        btnXoa.setOnAction(e -> handleXoa());
        btnReset.setOnAction(e -> clearForm());

        rightCol.getChildren().addAll(
                lblForm,
                new Separator(),
                formFields,
                new Separator(),
                btnThem, btnCapNhat, btnXoa, btnReset
        );

        root.getChildren().addAll(leftCol, rightCol);
        loadData();
        return root;
    }

    private TextField createField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
        tf.setStyle("-fx-background-radius: 6; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-padding: 0 10 0 10;");
        return tf;
    }

    private VBox formRow(String label, Control field) {
        VBox vb = new VBox(4);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#7f8c8d"));
        vb.getChildren().addAll(lbl, field);
        return vb;
    }

    private Button createBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(42);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: derive(" + color + ", -10%); -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;"));
        return btn;
    }

    public void loadData() {
        Task<List<Phong>> task = new Task<>() {
            @Override
            protected List<Phong> call() {
                return dao.getAllPhong();
            }
        };
        task.setOnSucceeded(e -> {
            allData = task.getValue();
            filterData();
        });
        new Thread(task).start();
    }

    private void filterData() {
        if (allData == null) return;
        String q = txtSearch.getText().toLowerCase().trim();
        tableView.getItems().setAll(
                allData.stream()
                        .filter(p -> q.isEmpty()
                                || p.getMaPhong().toLowerCase().contains(q)
                                || p.getLoaiPhong().toLowerCase().contains(q))
                        .collect(Collectors.toList())
        );
    }

    private void fillForm(Phong p) {
        if (p == null) return;
        txtMa.setText(p.getMaPhong());
        txtMa.setEditable(false);
        txtMa.setStyle(txtMa.getStyle() + " -fx-background-color: #f8f8f8;");
        cbLoaiPhong.setValue(p.getLoaiPhong());
        txtSoGiuong.setText(String.valueOf(p.getSoGiuong()));
        cbTrangThai.setValue(p.getTrangThai());
        txtGia.setText(String.valueOf((long) p.getGiaPhong()));
    }

    private void handleThem() {
        try {
            String ma = txtMa.getText().trim();
            if (ma.isEmpty()) {
                showAlert("Thiếu thông tin", "Vui lòng nhập Mã Phòng.");
                return;
            }
            int soGiuong = Integer.parseInt(txtSoGiuong.getText().trim());
            double gia = Double.parseDouble(txtGia.getText().trim().replace(",", "").replace(".", ""));

            Phong p = new Phong();
            p.setMaPhong(ma);
            p.setLoaiPhong(cbLoaiPhong.getValue());
            p.setSoGiuong(soGiuong);
            p.setTrangThai(cbTrangThai.getValue());
            p.setGiaPhong(gia);

            boolean ok = dao.create(p);
            if (ok) {
                showAlert("Thành công", "Đã thêm phòng " + ma + " thành công!");
                loadData();
                clearForm();
            } else {
                showAlert("Lỗi", "Không thể thêm. Mã phòng có thể đã tồn tại.");
            }
        } catch (NumberFormatException e) {
            showAlert("Dữ liệu không hợp lệ", "Số giường và giá phòng phải là số nguyên hợp lệ.");
        } catch (IllegalArgumentException e) {
            showAlert("Dữ liệu không hợp lệ", e.getMessage());
        }
    }

    private void handleCapNhat() {
        Phong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn phòng cần cập nhật!");
            return;
        }
        try {
            int soGiuong = Integer.parseInt(txtSoGiuong.getText().trim());
            double gia = Double.parseDouble(txtGia.getText().trim().replace(",", "").replace(".", ""));
            selected.setLoaiPhong(cbLoaiPhong.getValue());
            selected.setSoGiuong(soGiuong);
            selected.setTrangThai(cbTrangThai.getValue());
            selected.setGiaPhong(gia);

            boolean ok = dao.update(selected);
            if (ok) {
                showAlert("Thành công", "Đã cập nhật phòng " + selected.getMaPhong() + "!");
                loadData();
            } else {
                showAlert("Lỗi", "Cập nhật thất bại.");
            }
        } catch (NumberFormatException e) {
            showAlert("Dữ liệu không hợp lệ", "Số giường và giá phòng phải là số hợp lệ.");
        } catch (IllegalArgumentException e) {
            showAlert("Dữ liệu không hợp lệ", e.getMessage());
        }
    }

    private void handleXoa() {
        Phong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn phòng cần xóa!");
            return;
        }
        if (!"Trống".equals(selected.getTrangThai())) {
            showAlert("Không thể xóa", "Chỉ có thể xóa phòng khi trạng thái là 'Trống'.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa phòng '" + selected.getMaPhong() + "' (" + selected.getLoaiPhong() + ")?\nHành động này không thể hoàn tác.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa phòng");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(t -> {
            if (t == ButtonType.YES) {
                boolean ok = dao.delete(selected.getMaPhong());
                if (ok) {
                    showAlert("Thành công", "Đã xóa phòng " + selected.getMaPhong() + ".");
                    loadData();
                    clearForm();
                } else {
                    showAlert("Lỗi", "Không thể xóa. Phòng có thể đang liên kết với dữ liệu đặt phòng.");
                }
            }
        });
    }

    private void clearForm() {
        txtMa.clear();
        txtMa.setEditable(true);
        txtMa.setStyle("-fx-background-radius: 6; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-padding: 0 10 0 10;");
        cbLoaiPhong.setValue("Standard");
        txtSoGiuong.clear();
        cbTrangThai.setValue("Trống");
        txtGia.clear();
        tableView.getSelectionModel().clearSelection();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
