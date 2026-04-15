package gui;

import entity.KhachHang;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
public class CustomerDialog {

    private static final String COLOR_PRIMARY = "#2c0fbd";
    private static final String COLOR_SUCCESS = "#27ae60";
    private static final String COLOR_INFO = "#2980b9";
    private static final String COLOR_DANGER = "#e74c3c";
    private static final String COLOR_NEUTRAL = "#7f8c8d";

    private final Stage stage;
    private final KhachHang original;
    private final boolean isEditMode;
    private final KhachHangService service;
    private final Runnable onSuccess;

    private TextField txtTen;
    private TextField txtSdt;
    private TextField txtDiaChi;
    private ComboBox<String> cbGioiTinh;

    /**
     * @param khachHang null → CREATE; không null → EDIT
     * @param service   KhachHangService
     * @param onSuccess callback reload TableView sau CRUD
     */
    public CustomerDialog(KhachHang khachHang, KhachHangService service, Runnable onSuccess) {
        this.original = khachHang;
        this.service = service;
        this.onSuccess = onSuccess;
        this.isEditMode = (khachHang != null);

        stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setResizable(false);
        stage.setTitle(isEditMode ? "Chi tiết khách hàng" : "Thêm khách hàng mới");
        stage.setScene(new Scene(buildUI(), 430, isEditMode ? 470 : 440));
    }

    public void show() {
        stage.showAndWait();
    }

    private VBox buildUI() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white;");
        root.getChildren().addAll(buildHeader(), buildFormBody(), buildButtonBar());
        return root;
    }

    private VBox buildHeader() {
        VBox header = new VBox(5);
        header.setPadding(new Insets(22, 28, 18, 28));
        header.setStyle("-fx-background-color: " + COLOR_PRIMARY + ";");

        Label lblTitle = new Label(isEditMode ? "CHỈNH SỬA KHÁCH HÀNG" : "THÊM KHÁCH HÀNG MỚI");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTitle.setTextFill(Color.WHITE);

        Label lblSub = new Label(isEditMode
                ? "Mã KH: " + original.getMaKhachHang()
                : "Điền đầy đủ thông tin rồi bấm Lưu");
        lblSub.setFont(Font.font("Segoe UI", 12));
        lblSub.setTextFill(Color.web("#c0baee"));

        header.getChildren().addAll(lblTitle, lblSub);
        return header;
    }

    private VBox buildFormBody() {
        txtTen = createField("Họ và tên đầy đủ");
        txtSdt = createField("VD: 0912345678");
        txtDiaChi = createField("Địa chỉ thường trú");

        cbGioiTinh = new ComboBox<>();
        cbGioiTinh.getItems().addAll("Nam", "Nữ");
        cbGioiTinh.setValue("Nam");
        cbGioiTinh.setMaxWidth(Double.MAX_VALUE);
        cbGioiTinh.setPrefHeight(40);
        cbGioiTinh.setStyle("-fx-background-radius: 6; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 6; -fx-background-color: white;");

        if (isEditMode) {
            txtTen.setText(nullSafe(original.getTenKhachHang()));
            txtSdt.setText(nullSafe(original.getSoDienThoai()));
            txtDiaChi.setText(nullSafe(original.getDiaChi()).trim());
            cbGioiTinh.setValue(original.isGioiTinh() ? "Nam" : "Nữ");
        }

        VBox body = new VBox(14);
        body.setPadding(new Insets(24, 28, 16, 28));
        body.getChildren().addAll(
                formRow("Họ và tên *", txtTen),
                formRow("Số điện thoại *", txtSdt),
                formRow("Địa chỉ", txtDiaChi),
                formRow("Giới tính", cbGioiTinh));
        return body;
    }

    private HBox buildButtonBar() {
        HBox bar = new HBox(10);
        bar.setAlignment(Pos.CENTER_RIGHT);
        bar.setPadding(new Insets(8, 28, 24, 28));

        if (!isEditMode) {
            Button btnHuy = createBtn("Hủy", COLOR_NEUTRAL, 90);
            Button btnLuu = createBtn("Lưu", COLOR_SUCCESS, 110);
            btnHuy.setOnAction(e -> stage.close());
            btnLuu.setOnAction(e -> handleCreate());
            bar.getChildren().addAll(btnHuy, btnLuu);
        } else {
            Button btnXoa = createBtn("Xóa", COLOR_DANGER, 95);
            Button btnDong = createBtn("Đóng", COLOR_NEUTRAL, 90);
            Button btnCapNhat = createBtn("Cập nhật", COLOR_INFO, 115);
            btnXoa.setOnAction(e -> handleDelete());
            btnDong.setOnAction(e -> stage.close());
            btnCapNhat.setOnAction(e -> handleUpdate());
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            bar.getChildren().addAll(btnXoa, spacer, btnDong, btnCapNhat);
        }
        return bar;
    }

    private void handleCreate() {
        String ten = txtTen.getText().trim();
        String sdt = txtSdt.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        boolean gt = "Nam".equals(cbGioiTinh.getValue());

        String e1 = service.validateTen(ten);
        if (e1 != null) {
            showError(e1);
            txtTen.requestFocus();
            return;
        }
        String e2 = service.validateSdt(sdt);
        if (e2 != null) {
            showError(e2);
            txtSdt.requestFocus();
            return;
        }

        try {
            String newId = service.generateNextId();
            KhachHang kh = new KhachHang();
            kh.setMaKhachHang(newId);
            kh.setTenKhachHang(ten);
            kh.setDiaChi(diaChi.isEmpty() ? " " : diaChi);
            kh.setSoDienThoai(sdt);
            kh.setGioiTinh(gt);
            if (service.create(kh)) {
                showInfo("Thành công", "Đã thêm khách hàng mới!\nMã KH: " + newId + "\nTên: " + ten);
                onSuccess.run();
                stage.close();
            } else {
                showError("Không thể thêm. Số điện thoại có thể đã tồn tại.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleUpdate() {
        String ten = txtTen.getText().trim();
        String sdt = txtSdt.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        boolean gt = "Nam".equals(cbGioiTinh.getValue());

        String e1 = service.validateTen(ten);
        if (e1 != null) {
            showError(e1);
            txtTen.requestFocus();
            return;
        }
        String e2 = service.validateSdt(sdt);
        if (e2 != null) {
            showError(e2);
            txtSdt.requestFocus();
            return;
        }

        KhachHang updated = new KhachHang(
                original.getMaKhachHang(), ten,
                diaChi.isEmpty() ? " " : diaChi, gt, sdt);

        if (!service.hasChanges(original, updated)) {
            showInfo("Thông báo", "Không có thay đổi nào để cập nhật.");
            return;
        }
        try {
            if (service.update(updated)) {
                showInfo("Thành công", "Đã cập nhật thông tin khách hàng!");
                onSuccess.run();
                stage.close();
            } else {
                showError("Cập nhật thất bại. Vui lòng thử lại.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void handleDelete() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(stage);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa khách hàng: " + original.getTenKhachHang());
        confirm.setContentText("Bạn có chắc chắn muốn xóa?\nHành động này không thể hoàn tác.");
        ButtonType yes = new ButtonType("Có, xóa", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("Không", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yes, no);
        confirm.showAndWait().ifPresent(r -> {
            if (r == yes) {
                if (service.delete(original.getMaKhachHang())) {
                    showInfo("Đã xóa", "Đã xóa khách hàng: " + original.getTenKhachHang());
                    onSuccess.run();
                    stage.close();
                } else {
                    showError("Không thể xóa. Khách hàng có thể đang liên kết với phiếu đặt phòng.");
                }
            }
        });
    }

    private TextField createField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
        tf.setStyle("-fx-background-radius: 6; -fx-border-color: #e0e0e0; " +
                "-fx-border-radius: 6; -fx-padding: 0 10 0 10; -fx-background-color: white;");
        tf.focusedProperty().addListener((obs, was, is) -> {
            String b = is ? "#2c0fbd" : "#e0e0e0";
            tf.setStyle("-fx-background-radius: 6; -fx-border-color: " + b + "; " +
                    "-fx-border-radius: 6; -fx-padding: 0 10 0 10; -fx-background-color: white;");
        });
        return tf;
    }

    private VBox formRow(String label, Control field) {
        VBox vb = new VBox(5);
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lbl.setTextFill(Color.web("#555"));
        vb.getChildren().addAll(lbl, field);
        return vb;
    }

    private Button createBtn(String text, String color, double w) {
        Button btn = new Button(text);
        btn.setPrefWidth(w);
        btn.setPrefHeight(40);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        String base = "-fx-background-color: " + color
                + "; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;";
        String hover = "-fx-background-color: derive(" + color
                + ", -15%); -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand;";
        btn.setStyle(base);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(base));
        return btn;
    }

    private void showError(String msg) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(stage);
        a.setTitle("Lỗi");
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.initOwner(stage);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String nullSafe(String s) {
        return s == null ? "" : s;
    }
}
