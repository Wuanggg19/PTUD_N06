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

import dao.KhachHangDAO;
import entity.KhachHang;

public class CustomerView {

    private final String BG_LIGHT = "#f6f6f8";
    private final String PRIMARY_COLOR = "#2c0fbd";

    private TableView<KhachHang> tableView;
    private List<KhachHang> allData;
    private TextField txtSearch;

    // Form fields
    private TextField txtMa, txtTen, txtDiaChi, txtSdt;
    private ComboBox<String> cbGioiTinh;
    private final KhachHangDAO dao = new KhachHangDAO();

    public Node createView() {
        HBox root = new HBox(25);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- CỘT TRÁI: Bảng danh sách ---
        VBox leftCol = new VBox(15);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        Label lblTitle = new Label("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo tên hoặc số điện thoại...");
        txtSearch.setPrefHeight(42);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 0 12 0 12;");
        txtSearch.textProperty().addListener((o, v, n) -> filterData());

        tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setStyle("-fx-background-radius: 8;");
        tableView.setRowFactory(tv -> {
            TableRow<KhachHang> row = new TableRow<>();
            row.setPrefHeight(40);
            return row;
        });
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<KhachHang, String> colMa = new TableColumn<>("Mã KH");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaKhachHang()));
        colMa.setPrefWidth(80);

        TableColumn<KhachHang, String> colTen = new TableColumn<>("Tên Khách Hàng");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenKhachHang()));

        TableColumn<KhachHang, String> colGioiTinh = new TableColumn<>("Giới Tính");
        colGioiTinh.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isGioiTinh() ? "Nam" : "Nữ"));
        colGioiTinh.setPrefWidth(80);

        TableColumn<KhachHang, String> colDiaChi = new TableColumn<>("Địa Chỉ");
        colDiaChi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiaChi()));

        TableColumn<KhachHang, String> colSdt = new TableColumn<>("Số ĐT");
        colSdt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSoDienThoai()));
        colSdt.setPrefWidth(115);

        tableView.getColumns().addAll(colMa, colTen, colGioiTinh, colDiaChi, colSdt);
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> fillForm(newVal));

        leftCol.getChildren().addAll(lblTitle, txtSearch, tableView);

        // --- CỘT PHẢI: Form nhập liệu ---
        VBox rightCol = new VBox(16);
        rightCol.setPrefWidth(290);
        rightCol.setMinWidth(290);
        rightCol.setPadding(new Insets(25));
        rightCol.setStyle("-fx-background-color: white; -fx-background-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 14, 0, 0, 5);");

        Label lblForm = new Label("THÔNG TIN KHÁCH HÀNG");
        lblForm.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        lblForm.setTextFill(Color.web(PRIMARY_COLOR));

        txtMa = createField("VD: KH001");
        txtTen = createField("Họ và tên đầy đủ");
        txtDiaChi = createField("Địa chỉ thường trú");
        txtSdt = createField("0xxxxxxxxx");

        cbGioiTinh = new ComboBox<>();
        cbGioiTinh.getItems().addAll("Nam", "Nữ");
        cbGioiTinh.setValue("Nam");
        cbGioiTinh.setMaxWidth(Double.MAX_VALUE);
        cbGioiTinh.setPrefHeight(40);

        VBox formFields = new VBox(12);
        formFields.getChildren().addAll(
                formRow("Mã KH:", txtMa),
                formRow("Họ tên:", txtTen),
                formRow("Địa chỉ:", txtDiaChi),
                formRow("Số ĐT:", txtSdt),
                formRow("Giới tính:", cbGioiTinh)
        );

        Button btnThem = createBtn("  THÊM MỚI", "#27ae60");
        Button btnCapNhat = createBtn("  CẬP NHẬT", "#2980b9");
        Button btnXoa = createBtn("  XÓA", "#e74c3c");
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
        Task<List<KhachHang>> task = new Task<>() {
            @Override
            protected List<KhachHang> call() {
                return dao.getAllKhachHang();
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
                        .filter(k -> q.isEmpty()
                                || k.getTenKhachHang().toLowerCase().contains(q)
                                || (k.getSoDienThoai() != null && k.getSoDienThoai().contains(q)))
                        .collect(Collectors.toList())
        );
    }

    private void fillForm(KhachHang kh) {
        if (kh == null) return;
        txtMa.setText(kh.getMaKhachHang());
        txtMa.setEditable(false);
        txtMa.setStyle(txtMa.getStyle() + " -fx-background-color: #f8f8f8;");
        txtTen.setText(kh.getTenKhachHang());
        txtDiaChi.setText(kh.getDiaChi() != null ? kh.getDiaChi() : "");
        txtSdt.setText(kh.getSoDienThoai() != null ? kh.getSoDienThoai() : "");
        cbGioiTinh.setValue(kh.isGioiTinh() ? "Nam" : "Nữ");
    }

    private void handleThem() {
        try {
            String ma = txtMa.getText().trim();
            String ten = txtTen.getText().trim();
            String dc = txtDiaChi.getText().trim();
            String sdt = txtSdt.getText().trim();
            if (ma.isEmpty() || ten.isEmpty()) {
                showAlert("Thiếu thông tin", "Vui lòng nhập đầy đủ Mã KH và Tên.");
                return;
            }
            KhachHang kh = new KhachHang();
            kh.setMaKhachHang(ma);
            kh.setTenKhachHang(ten);
            kh.setDiaChi(dc.isEmpty() ? " " : dc);
            kh.setSoDienThoai(sdt);
            kh.setGioiTinh("Nam".equals(cbGioiTinh.getValue()));
            boolean ok = dao.create(kh);
            if (ok) {
                showAlert("Thành công", "Đã thêm khách hàng mới: " + ten);
                loadData();
                clearForm();
            } else {
                showAlert("Lỗi", "Không thể thêm. Mã KH có thể đã tồn tại.");
            }
        } catch (IllegalArgumentException e) {
            showAlert("Dữ liệu không hợp lệ", e.getMessage());
        }
    }

    private void handleCapNhat() {
        KhachHang selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn dòng cần cập nhật!");
            return;
        }
        try {
            selected.setTenKhachHang(txtTen.getText().trim());
            selected.setDiaChi(txtDiaChi.getText().trim());
            selected.setSoDienThoai(txtSdt.getText().trim());
            selected.setGioiTinh("Nam".equals(cbGioiTinh.getValue()));
            boolean ok = dao.update(selected);
            if (ok) {
                showAlert("Thành công", "Đã cập nhật thông tin khách hàng!");
                loadData();
            } else {
                showAlert("Lỗi", "Cập nhật thất bại.");
            }
        } catch (IllegalArgumentException e) {
            showAlert("Dữ liệu không hợp lệ", e.getMessage());
        }
    }

    private void handleXoa() {
        KhachHang selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn khách hàng cần xóa!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xóa khách hàng '" + selected.getTenKhachHang() + "'?\nHành động này không thể hoàn tác.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(t -> {
            if (t == ButtonType.YES) {
                boolean ok = dao.delete(selected.getMaKhachHang());
                if (ok) {
                    showAlert("Thành công", "Đã xóa khách hàng.");
                    loadData();
                    clearForm();
                } else {
                    showAlert("Lỗi", "Không thể xóa. Khách hàng có thể đang liên kết với phiếu đặt phòng.");
                }
            }
        });
    }

    private void clearForm() {
        txtMa.clear();
        txtMa.setEditable(true);
        txtMa.setStyle("-fx-background-radius: 6; -fx-border-color: #e0e0e0; -fx-border-radius: 6; -fx-padding: 0 10 0 10;");
        txtTen.clear();
        txtDiaChi.clear();
        txtSdt.clear();
        cbGioiTinh.setValue("Nam");
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
