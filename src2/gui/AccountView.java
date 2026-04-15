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
import dao.NhanVienDAO;
import entity.NhanVien;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 *  TÀI KHOẢN (NHÂN VIÊN) VIEW
 *  Chức năng: Xem và quản lý thông tin tài khoản nhân viên (Admin).
 *             Thêm / sửa / xóa tài khoản nhân viên.
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()      - Tải danh sách tài khoản nhân viên từ DB
 *   2. handleAdd()     - Thêm tài khoản mới (mở dialog nhập liệu)
 *   3. handleEdit()    - Sửa thông tin tài khoản đang chọn
 *   4. handleDelete()  - Xóa tài khoản đang chọn (hỏi xác nhận)
 *   5. handleSearch()  - Tìm kiếm theo tên / username
 * ================================================================
 */
public class AccountView {

    private static final String BG_LIGHT   = "#f6f6f8";
    private static final String BTN_ADD    = "#27ae60";
    private static final String BTN_EDIT   = "#f39c12";
    private static final String BTN_DEL    = "#e74c3c";
    private static final String BTN_INFO   = "#3498db";

    private TableView<NhanVien> tableView;
    private ObservableList<NhanVien> dataList = FXCollections.observableArrayList();
    private TextField txtSearch;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("QUẢN LÝ TÀI KHOẢN");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        Label lblNote = new Label("⚠  Chỉ Admin mới có quyền quản lý tài khoản nhân viên.");
        lblNote.setFont(Font.font("Segoe UI", 13));
        lblNote.setTextFill(Color.web("#c0392b"));
        lblNote.setStyle("-fx-background-color: #fde8e8; -fx-padding: 10 14; -fx-background-radius: 8;");

        // --- Toolbar ---
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm theo tên / tên đăng nhập...");
        txtSearch.setPrefWidth(280);
        txtSearch.setPrefHeight(36);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7; -fx-padding: 0 10;");
        Button btnSearch = createButton("Tìm kiếm", BTN_INFO);
        btnSearch.setOnAction(e -> handleSearch());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnAdd    = createButton("➕  Thêm tài khoản", BTN_ADD);
        Button btnEdit   = createButton("✏  Sửa", BTN_EDIT);
        Button btnDelete = createButton("🗑  Xóa", BTN_DEL);
        Button btnRefresh = createButton("🔄  Làm mới", "#7f8c8d");
        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadData());
        toolbar.getChildren().addAll(txtSearch, btnSearch, spacer, btnAdd, btnEdit, btnDelete, btnRefresh);

        // --- Bảng tài khoản ---
        tableView = new TableView<>(dataList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setPlaceholder(new Label("Chưa có dữ liệu tài khoản."));

        TableColumn<NhanVien, String> colMa    = new TableColumn<>("Mã Nhân Viên");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaNhanVien()));
        TableColumn<NhanVien, String> colTen   = new TableColumn<>("Họ và Tên");
        colTen.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTenNhanVien()));
        TableColumn<NhanVien, String> colUser  = new TableColumn<>("Tên Đăng Nhập");
        colUser.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTenDangNhap()));
        TableColumn<NhanVien, String> colVT    = new TableColumn<>("Vai Trò");
        colVT.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getVaiTro()));
        TableColumn<NhanVien, String> colSDT   = new TableColumn<>("Số Điện Thoại");
        colSDT.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSoDienThoai()));
        TableColumn<NhanVien, String> colCV  = new TableColumn<>("Chức Vụ");
        colCV.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getChucVu()));

        tableView.getColumns().addAll(colMa, colTen, colUser, colVT, colSDT, colCV);

        root.getChildren().addAll(lblTitle, lblNote, toolbar, tableView);
        return root;
    }

    /**
     * TODO: Tải danh sách tài khoản nhân viên từ DB.
     *       Gợi ý: Dùng NhanVienDAO.getAllNhanVien()
     */
    public void loadData() {
        try {
            if (ConnectDB.getConnection() == null) return;
            NhanVienDAO dao = new NhanVienDAO();
            List<NhanVien> list = dao.getAllNhanVien();
            dataList.setAll(list != null ? list : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu tài khoản: " + e.getMessage());
        }
    }

    /**
     * TODO: Mở dialog thêm tài khoản mới.
     *       Các trường: Mã NV, Họ tên, Username, Password, Vai trò, SĐT, Email.
     */
    private void handleAdd() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng thêm tài khoản đang được phát triển.");
    }

    /**
     * TODO: Mở dialog sửa thông tin tài khoản đang chọn.
     */
    private void handleEdit() {
        NhanVien selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một tài khoản để sửa.");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng sửa tài khoản đang được phát triển.");
    }

    /**
     * TODO: Xóa tài khoản đang chọn (hỏi xác nhận trước).
     *       Lưu ý: Không nên xóa tài khoản Admin duy nhất.
     */
    private void handleDelete() {
        NhanVien selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một tài khoản để xóa.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn xóa tài khoản: " + selected.getTenDangNhap() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                // TODO: Implement xóa tài khoản
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng xóa đang được phát triển.");
            }
        });
    }

    /**
     * TODO: Tìm kiếm tài khoản theo từ khóa.
     */
    private void handleSearch() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng tìm kiếm đang được phát triển.");
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefHeight(36);
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
