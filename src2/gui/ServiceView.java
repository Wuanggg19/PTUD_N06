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
import dao.DichVuDAO;
import entity.DichVu;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 *  DỊCH VỤ VIEW
 *  Chức năng: Quản lý danh sách dịch vụ (tên, giá, loại dịch vụ).
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()      - Tải danh sách dịch vụ từ DB
 *   2. handleAdd()     - Thêm dịch vụ mới (mở dialog nhập liệu)
 *   3. handleEdit()    - Sửa thông tin dịch vụ đang chọn
 *   4. handleDelete()  - Xóa dịch vụ đang chọn (hỏi xác nhận trước)
 *   5. handleSearch()  - Tìm kiếm dịch vụ theo tên
 * ================================================================
 */
public class ServiceView {

    private static final String BG_LIGHT   = "#f6f6f8";
    private static final String BTN_ADD    = "#27ae60";
    private static final String BTN_EDIT   = "#f39c12";
    private static final String BTN_DEL    = "#e74c3c";
    private static final String BTN_INFO   = "#3498db";

    private TableView<DichVu> tableView;
    private ObservableList<DichVu> dataList = FXCollections.observableArrayList();
    private TextField txtSearch;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("QUẢN LÝ DỊCH VỤ");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        // --- Toolbar ---
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm kiếm dịch vụ...");
        txtSearch.setPrefWidth(260);
        txtSearch.setPrefHeight(36);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7; -fx-padding: 0 10;");

        Button btnSearch = createButton("Tìm kiếm", BTN_INFO);
        btnSearch.setOnAction(e -> handleSearch());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnAdd    = createButton("➕  Thêm mới", BTN_ADD);
        Button btnEdit   = createButton("✏  Sửa", BTN_EDIT);
        Button btnDelete = createButton("🗑  Xóa", BTN_DEL);
        Button btnRefresh = createButton("🔄  Làm mới", "#7f8c8d");

        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadData());

        toolbar.getChildren().addAll(txtSearch, btnSearch, spacer, btnAdd, btnEdit, btnDelete, btnRefresh);

        // --- Bảng dữ liệu ---
        tableView = new TableView<>(dataList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setPlaceholder(new Label("Chưa có dữ liệu dịch vụ."));

        TableColumn<DichVu, String> colMa = new TableColumn<>("Mã Dịch Vụ");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaDichVu()));

        TableColumn<DichVu, String> colTen = new TableColumn<>("Tên Dịch Vụ");
        colTen.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTenDichVu()));

        TableColumn<DichVu, String> colGia = new TableColumn<>("Đơn Giá");
        colGia.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f VNĐ", d.getValue().getDonGia())));

        // TODO: Thêm cột Loại Dịch Vụ / Đơn Vị Tính nếu cột này được thêm vào entity DichVu và bảng DB
        TableColumn<DichVu, String> colTT = new TableColumn<>("Trạng Thái");
        colTT.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTrangThai()));

        tableView.getColumns().addAll(colMa, colTen, colGia, colTT);

        root.getChildren().addAll(lblTitle, toolbar, tableView);
        return root;
    }

    /**
     * TODO: Tải danh sách dịch vụ từ database.
     *       Gợi ý: Dùng DichVuDAO.getAllDichVu()
     */
    public void loadData() {
        try {
            if (ConnectDB.getConnection() == null) return;
            DichVuDAO dao = new DichVuDAO();
            List<DichVu> list = dao.getAllDichVu();
            dataList.setAll(list != null ? list : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu dịch vụ: " + e.getMessage());
        }
    }

    /**
     * TODO: Mở dialog thêm dịch vụ mới.
     *       Các trường cần nhập: Mã DV, Tên DV, Loại DV, Đơn giá, Đơn vị tính.
     *       Sau khi thêm thành công → gọi loadData() để cập nhật bảng.
     */
    private void handleAdd() {
        // TODO: Implement dialog thêm dịch vụ
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng thêm dịch vụ đang được phát triển.");
    }

    /**
     * TODO: Mở dialog sửa thông tin dịch vụ đang chọn trong bảng.
     *       Điền sẵn dữ liệu hiện có vào form, cho phép sửa rồi lưu.
     */
    private void handleEdit() {
        DichVu selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một dịch vụ để sửa.");
            return;
        }
        // TODO: Implement dialog sửa dịch vụ
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng sửa dịch vụ đang được phát triển.");
    }

    /**
     * TODO: Xóa dịch vụ đang chọn.
     *       Hỏi xác nhận trước khi xóa → Dùng DichVuDAO để xóa → Reload dữ liệu.
     */
    private void handleDelete() {
        DichVu selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một dịch vụ để xóa.");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Bạn có chắc muốn xóa dịch vụ: " + selected.getTenDichVu() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                // TODO: Implement delete logic ở đây, sau đó gọi loadData()
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng xóa đang được phát triển.");
            }
        });
    }

    /**
     * TODO: Tìm kiếm dịch vụ theo từ khóa nhập vào txtSearch.
     *       Có thể lọc daList theo tên dịch vụ hoặc loại dịch vụ.
     */
    private void handleSearch() {
        // TODO: Implement tìm kiếm
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
