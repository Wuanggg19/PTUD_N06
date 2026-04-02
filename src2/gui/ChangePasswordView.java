package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import connectDB.ConnectDB;
import dao.NhanVienDAO;
import entity.NhanVien;

/**
 * ================================================================
 *  ĐỔI MẬT KHẨU VIEW
 *  Chức năng: Cho phép nhân viên đang đăng nhập tự đổi mật khẩu.
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. handleChangePassword() - Xác thực mật khẩu cũ, kiểm tra mật khẩu mới,
 *                               cập nhật mật khẩu mới vào DB.
 *   Lưu ý:
 *   - Mật khẩu mới phải ≥ 6 ký tự
 *   - Mật khẩu mới phải khớp với ô xác nhận
 *   - Mật khẩu cũ phải đúng với DB
 * ================================================================
 */
public class ChangePasswordView {

    private static final String BG_LIGHT = "#f6f6f8";
    private static final String ACCENT   = "#2c0fbd";

    private final NhanVien currentUser;

    private PasswordField txtOldPass, txtNewPass, txtConfirm;
    private Label lblStatus;

    /**
     * @param currentUser Nhân viên đang đăng nhập (lấy từ MainFrame).
     */
    public ChangePasswordView(NhanVien currentUser) {
        this.currentUser = currentUser;
    }

    public Node createView() {
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Card trung tâm ---
        VBox card = new VBox(18);
        card.setPadding(new Insets(36));
        card.setMaxWidth(480);
        card.setAlignment(Pos.TOP_CENTER);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 16;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 16, 0, 0, 4);"
        );

        // --- Tiêu đề ---
        Label lblTitle = new Label("🔒  ĐỔI MẬT KHẨU");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        lblTitle.setTextFill(Color.web(ACCENT));

        Label lblSub = new Label("Thay đổi mật khẩu đăng nhập của bạn.");
        lblSub.setFont(Font.font("Segoe UI", 13));
        lblSub.setTextFill(Color.web("#7f8c8d"));

        Separator sep = new Separator();

        // --- Form ---
        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(14);
        form.setAlignment(Pos.CENTER_LEFT);

        String inputStyle = "-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7; -fx-padding: 8 12; -fx-pref-height: 38;";

        Label lblOld = new Label("Mật khẩu hiện tại:");
        lblOld.setFont(Font.font("Segoe UI", 13));
        txtOldPass = new PasswordField();
        txtOldPass.setPromptText("Nhập mật khẩu hiện tại");
        txtOldPass.setStyle(inputStyle);
        txtOldPass.setPrefWidth(300);

        Label lblNew = new Label("Mật khẩu mới:");
        lblNew.setFont(Font.font("Segoe UI", 13));
        txtNewPass = new PasswordField();
        txtNewPass.setPromptText("Tối thiểu 6 ký tự");
        txtNewPass.setStyle(inputStyle);
        txtNewPass.setPrefWidth(300);

        Label lblConfirm = new Label("Xác nhận mật khẩu mới:");
        lblConfirm.setFont(Font.font("Segoe UI", 13));
        txtConfirm = new PasswordField();
        txtConfirm.setPromptText("Nhập lại mật khẩu mới");
        txtConfirm.setStyle(inputStyle);
        txtConfirm.setPrefWidth(300);

        form.add(lblOld, 0, 0);    form.add(txtOldPass, 1, 0);
        form.add(lblNew, 0, 1);    form.add(txtNewPass, 1, 1);
        form.add(lblConfirm, 0, 2); form.add(txtConfirm, 1, 2);

        // --- Ghi chú quy tắc mật khẩu ---
        Label lblRules = new Label("📌  Mật khẩu mới phải có ít nhất 6 ký tự và khớp với ô xác nhận.");
        lblRules.setFont(Font.font("Segoe UI", 12));
        lblRules.setTextFill(Color.web("#5d6d7e"));
        lblRules.setWrapText(true);
        lblRules.setStyle("-fx-background-color: #eaf4fb; -fx-padding: 10 12; -fx-background-radius: 8;");

        // --- Thông báo trạng thái ---
        lblStatus = new Label("");
        lblStatus.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lblStatus.setWrapText(true);

        // --- Nút ---
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button btnClear = new Button("Xóa tất cả");
        btnClear.setPrefHeight(38);
        btnClear.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #7f8c8d; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 18;");
        btnClear.setOnAction(e -> {
            txtOldPass.clear(); txtNewPass.clear(); txtConfirm.clear();
            lblStatus.setText(""); lblStatus.setTextFill(Color.web("#7f8c8d"));
        });

        Button btnChange = new Button("✅  Đổi mật khẩu");
        btnChange.setPrefHeight(38);
        btnChange.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        btnChange.setTextFill(Color.WHITE);
        btnChange.setStyle("-fx-background-color: " + ACCENT + "; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 22;");
        btnChange.setOnMouseEntered(e -> btnChange.setStyle("-fx-background-color: #5839ff; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 22;"));
        btnChange.setOnMouseExited(e -> btnChange.setStyle("-fx-background-color: " + ACCENT + "; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 22;"));
        btnChange.setOnAction(e -> handleChangePassword());

        btnBox.getChildren().addAll(btnClear, btnChange);

        card.getChildren().addAll(lblTitle, lblSub, sep, form, lblRules, lblStatus, btnBox);
        root.getChildren().add(card);
        VBox.setVgrow(root, Priority.ALWAYS);
        return root;
    }

    /**
     * TODO: Xử lý đổi mật khẩu.
     *
     * Các bước cần thực hiện:
     *  1. Kiểm tra txtOldPass, txtNewPass, txtConfirm không được trống
     *  2. Kiểm tra txtNewPass.length() >= 6
     *  3. Kiểm tra txtNewPass.equals(txtConfirm)
     *  4. Kiểm tra txtOldPass khớp với mật khẩu hiện tại của currentUser trong DB
     *  5. Nếu hợp lệ → Gọi NhanVienDAO để UPDATE mật khẩu mới vào DB
     *  6. Hiển thị thông báo thành công / thất bại qua lblStatus
     */
    private void handleChangePassword() {
        String oldPass  = txtOldPass.getText().trim();
        String newPass  = txtNewPass.getText().trim();
        String confirm  = txtConfirm.getText().trim();

        // --- Kiểm tra đầu vào ---
        if (oldPass.isEmpty() || newPass.isEmpty() || confirm.isEmpty()) {
            setStatus("⚠  Vui lòng điền đầy đủ tất cả các trường.", "#e74c3c");
            return;
        }
        if (newPass.length() < 6) {
            setStatus("⚠  Mật khẩu mới phải có ít nhất 6 ký tự.", "#e74c3c");
            return;
        }
        if (!newPass.equals(confirm)) {
            setStatus("⚠  Mật khẩu mới không khớp với xác nhận.", "#e74c3c");
            return;
        }

        // TODO: Bước 4 & 5 — Implement logic xác thực và cập nhật mật khẩu
        // Gợi ý:
        // if (!currentUser.getMatKhau().equals(oldPass)) { setStatus("Sai mật khẩu cũ."); return; }
        // NhanVienDAO dao = new NhanVienDAO();
        // dao.updateMatKhau(currentUser.getMaNhanVien(), newPass);
        // setStatus("✅ Đổi mật khẩu thành công!", "#27ae60");

        setStatus("ℹ  Chức năng đổi mật khẩu đang được phát triển.", "#f39c12");
    }

    private void setStatus(String msg, String hexColor) {
        lblStatus.setText(msg);
        lblStatus.setTextFill(Color.web(hexColor));
    }
}
