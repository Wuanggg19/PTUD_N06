package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.DropShadow;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.sql.SQLException;

import connectDB.ConnectDB;
import dao.NhanVienDAO;
import entity.NhanVien;
import javax.swing.SwingUtilities;

public class LoginFrame {

    private final TextField txtUsername = new TextField();
    private final PasswordField txtPassword = new PasswordField();

    private static final String INPUT_BASE_STYLE =
        "-fx-background-color: #f8fafc;"
        + "-fx-background-radius: 12;"
        + "-fx-border-radius: 12;"
        + "-fx-border-color: #e2e8f0;"
        + "-fx-border-width: 1;"
        + "-fx-padding: 10 12;";

    private static final String INPUT_FOCUSED_STYLE =
        "-fx-background-color: #ffffff;"
        + "-fx-background-radius: 12;"
        + "-fx-border-radius: 12;"
        + "-fx-border-color: #2c0fbd;"
        + "-fx-border-width: 2;"
        + "-fx-padding: 10 12;";

    private static final String INPUT_ERROR_STYLE =
        "-fx-background-color: #ffffff;"
        + "-fx-background-radius: 12;"
        + "-fx-border-radius: 12;"
        + "-fx-border-color: #ef4444;"
        + "-fx-border-width: 2;"
        + "-fx-padding: 10 12;";

    private static final String INPUT_ERROR_FOCUSED_STYLE =
        "-fx-background-color: #ffffff;"
        + "-fx-background-radius: 12;"
        + "-fx-border-radius: 12;"
        + "-fx-border-color: #ef4444;"
        + "-fx-border-width: 2;"
        + "-fx-padding: 10 12;";

    private boolean isInputInError(TextInputControl control) {
        return Boolean.TRUE.equals(control.getUserData());
    }

    public Scene createScene(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #2c0fbd, #5839ff);");
        root.setPadding(new Insets(24));

        VBox card = new VBox();
        card.setAlignment(Pos.TOP_CENTER);
        card.setSpacing(16);
        card.setPadding(new Insets(22));
        // Không để kích thước quá nhỏ khi có thêm hint/phím tắt
        card.setPrefSize(420, 360);
        card.setStyle(
            "-fx-background-color: #ffffff;" +
            "-fx-background-radius: 16;" +
            "-fx-border-radius: 16;" +
            "-fx-border-color: #e2e8f0;" +
            "-fx-border-width: 1;"
        );
        card.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.18)));

        Label lblTitle = new Label("ĐĂNG NHẬP HỆ THỐNG");
        lblTitle.setFont(Font.font("Inter", FontWeight.BOLD, 18));
        lblTitle.setTextFill(Color.web("#2c0fbd"));
        lblTitle.setTextAlignment(TextAlignment.CENTER);

        Label lblSub = new Label("Vui lòng nhập thông tin để tiếp tục.");
        lblSub.setFont(Font.font("Inter", FontWeight.NORMAL, 13));
        lblSub.setTextFill(Color.web("#64748b"));

        Label lblUser = new Label("Tên đăng nhập");
        lblUser.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        lblUser.setTextFill(Color.web("#334155"));

        Label lblPass = new Label("Mật khẩu");
        lblPass.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        lblPass.setTextFill(Color.web("#334155"));

        txtUsername.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        txtPassword.setFont(Font.font("Inter", FontWeight.NORMAL, 14));
        txtUsername.setPrefWidth(300);
        txtPassword.setPrefWidth(300);
        txtUsername.setPromptText("Ví dụ: nv01");
        txtPassword.setPromptText("Nhập mật khẩu");

        applyInputStyle(txtUsername);
        applyInputStyle(txtPassword);

        Button btnLogin = new Button("Đăng nhập");
        btnLogin.setPrefSize(120, 38);
        btnLogin.setFont(Font.font("Inter", FontWeight.BOLD, 14));
        btnLogin.setTextFill(Color.WHITE);
        btnLogin.setStyle("-fx-background-color: #2c0fbd; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-weight: bold;");
        btnLogin.setOnMouseEntered(e -> btnLogin.setStyle("-fx-background-color: #5839ff; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-weight: bold;"));
        btnLogin.setOnMouseExited(e -> btnLogin.setStyle("-fx-background-color: #2c0fbd; -fx-background-radius: 12; -fx-cursor: hand; -fx-font-weight: bold;"));

        btnLogin.setOnAction(e -> handleLogin(primaryStage));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        grid.add(lblUser, 0, 0);
        grid.add(txtUsername, 0, 1);
        grid.add(lblPass, 0, 2);
        grid.add(txtPassword, 0, 3);

        Label lblHint = new Label("Phím tắt: Enter chuyển ô, Ctrl+Enter đăng nhập, Esc xóa.");
        lblHint.setFont(Font.font("Inter", FontWeight.NORMAL, 12));
        lblHint.setTextFill(Color.web("#94a3b8"));
        lblHint.setWrapText(true);
        lblHint.setTextAlignment(TextAlignment.CENTER);

        VBox buttonWrap = new VBox(btnLogin);
        buttonWrap.setAlignment(Pos.CENTER);
        buttonWrap.setPadding(new Insets(10, 0, 0, 0));

        // Điều hướng bàn phím
        txtUsername.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                txtPassword.requestFocus();
                txtPassword.selectAll();
                e.consume();
            }
        });

        txtPassword.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                handleLogin(primaryStage);
                e.consume();
            }
        });

        // Shortcut toàn màn hình
        root.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                txtUsername.clear();
                txtPassword.clear();
                txtUsername.requestFocus();
                e.consume();
            } else if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                handleLogin(primaryStage);
                e.consume();
            }
        });

        card.getChildren().addAll(lblTitle, lblSub, grid, buttonWrap, lblHint);
        root.setCenter(card);

        // Đủ không gian cho button + hint để tránh bị cắt khi hiển thị
        Scene scene = new Scene(root, 520, 420);
        primaryStage.setOnShown(ev -> txtUsername.requestFocus());
        return scene;
    }

    private void applyInputStyle(TextInputControl control) {
        control.setUserData(Boolean.FALSE);
        control.setStyle(INPUT_BASE_STYLE);

        control.focusedProperty().addListener((obs, oldV, focused) -> {
            if (isInputInError(control)) {
                control.setStyle(focused ? INPUT_ERROR_FOCUSED_STYLE : INPUT_ERROR_STYLE);
            } else {
                control.setStyle(focused ? INPUT_FOCUSED_STYLE : INPUT_BASE_STYLE);
            }
        });

        // Khi người dùng bắt đầu gõ lại thì bỏ highlight lỗi
        control.textProperty().addListener((obs, oldV, newV) -> {
            if (isInputInError(control)) {
                control.setUserData(Boolean.FALSE);
                control.setStyle(control.isFocused() ? INPUT_FOCUSED_STYLE : INPUT_BASE_STYLE);
            }
        });
    }

    private void focusAndHighlightError(TextInputControl control) {
        control.requestFocus();
        control.selectAll();
        control.setUserData(Boolean.TRUE);
        control.setStyle(INPUT_ERROR_FOCUSED_STYLE);
    }

    private void handleLogin(Stage primaryStage) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Thiếu thông tin");
            alert.setHeaderText(null);
            alert.setContentText("Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu.");
            alert.showAndWait();
            return;
        }

        try {
            // Đảm bảo đã có connection trước khi gọi DAO
            if (ConnectDB.getConnection() == null) {
                ConnectDB.getInstance().connect();
            }

            NhanVienDAO nvDao = new NhanVienDAO();
            NhanVien nv = nvDao.getNhanVienByUsername(username);

            if (nv == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Đăng nhập thất bại");
                alert.setHeaderText(null);
                alert.setContentText("Sai tên đăng nhập.");
                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        focusAndHighlightError(txtUsername);
                    }
                });
                return;
            }

            String dbPass = nv.getMatKhau();
            boolean passwordOk = dbPass != null && dbPass.equals(password);
            if (!passwordOk) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Đăng nhập thất bại");
                alert.setHeaderText(null);
                alert.setContentText("Sai mật khẩu.");
                alert.showAndWait().ifPresent(buttonType -> {
                    if (buttonType == ButtonType.OK) {
                        focusAndHighlightError(txtPassword);
                    }
                });
                return;
            }

            // Mở MainFrame sau khi đăng nhập thành công
            final NhanVien loggedIn = nv;
            SwingUtilities.invokeLater(() -> {
                new MainFrame(loggedIn).setVisible(true);
            });
            primaryStage.close();
        } catch (SQLException ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi kết nối CSDL");
            alert.setHeaderText(null);
            alert.setContentText("Không thể kết nối đến database. Vui lòng kiểm tra lại cấu hình.");
            alert.showAndWait();
            ex.printStackTrace();
        }
    }
}
