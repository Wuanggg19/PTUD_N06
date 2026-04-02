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
import entity.BangGiaHeader;
import entity.BangGiaDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * ================================================================
 *  QUẢN LÝ BẢNG GIÁ VIEW
 *  Chức năng: Quản lý các bảng giá phòng (theo ngày thường/lễ tết),
 *             chi tiết bảng giá cho từng loại phòng.
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()      - Tải danh sách Bảng Giá (BangGiaHeader) từ DB
 *   2. handleAdd()     - Thêm bảng giá mới (mở form nhập thông tin Header + Details)
 *   3. handleEdit()    - Sửa bảng giá đang chọn
 *   4. handleDelete()  - Xóa bảng giá (cần cảnh báo nếu đã được áp dụng)
 *   5. showDetails()   - Hiển thị danh sách BangGiaDetail khi chọn một Header
 * ================================================================
 */
public class PricingView {

    private static final String BG_LIGHT   = "#f6f6f8";
    private static final String BTN_ADD    = "#27ae60";
    private static final String BTN_EDIT   = "#f39c12";
    private static final String BTN_DEL    = "#e74c3c";
    private static final String BTN_INFO   = "#3498db";

    private TableView<BangGiaHeader> tableHeader;
    private TableView<BangGiaDetail> tableDetail;
    
    private ObservableList<BangGiaHeader> headerList = FXCollections.observableArrayList();
    private ObservableList<BangGiaDetail> detailList = FXCollections.observableArrayList();

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("QUẢN LÝ BẢNG GIÁ");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        // --- Toolbar ---
        HBox toolbar = new HBox(12);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        
        Button btnAdd    = createButton("➕  Thêm Bảng Giá", BTN_ADD);
        Button btnEdit   = createButton("✏  Sửa Bảng Giá", BTN_EDIT);
        Button btnDelete = createButton("🗑  Xóa Bảng Giá", BTN_DEL);
        Button btnRefresh = createButton("🔄  Làm mới", "#7f8c8d");

        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadData());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        toolbar.getChildren().addAll(btnAdd, btnEdit, btnDelete, spacer, btnRefresh);

        // --- Layout chia 2 bảng (Header và Detail) ---
        HBox tablesBox = new HBox(20);
        VBox.setVgrow(tablesBox, Priority.ALWAYS);

        // Bảng danh sách Bảng Giá Header (Bên trái)
        VBox leftBox = new VBox(10);
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        Label lblLeft = new Label("DANH SÁCH BẢNG GIÁ");
        lblLeft.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        tableHeader = new TableView<>(headerList);
        tableHeader.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableHeader, Priority.ALWAYS);
        tableHeader.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> showDetails(newV));

        TableColumn<BangGiaHeader, String> colMaBG = new TableColumn<>("Mã Bảng Giá");
        colMaBG.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaBangGia()));
        
        TableColumn<BangGiaHeader, String> colTenBG = new TableColumn<>("Tên Bảng Giá");
        colTenBG.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTenBangGia()));

        TableColumn<BangGiaHeader, String> colLoaiNgay = new TableColumn<>("Loại Ngày");
        colLoaiNgay.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getLoaiNgay()));

        TableColumn<BangGiaHeader, String> colHieuLuc = new TableColumn<>("Hiệu Lực");
        colHieuLuc.setCellValueFactory(d -> {
            String from = d.getValue().getNgayBatDau() != null ? d.getValue().getNgayBatDau().toString() : "...";
            String to = d.getValue().getNgayKetThuc() != null ? d.getValue().getNgayKetThuc().toString() : "...";
            return new javafx.beans.property.SimpleStringProperty(from + " đến " + to);
        });

        tableHeader.getColumns().addAll(colMaBG, colTenBG, colLoaiNgay, colHieuLuc);
        leftBox.getChildren().addAll(lblLeft, tableHeader);

        // Bảng Chi tiết Bảng Giá (Bên phải)
        VBox rightBox = new VBox(10);
        rightBox.setPrefWidth(400);
        Label lblRight = new Label("CHI TIẾT GIÁ THEO PHÒNG");
        lblRight.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        tableDetail = new TableView<>(detailList);
        tableDetail.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableDetail, Priority.ALWAYS);

        TableColumn<BangGiaDetail, String> colPhong = new TableColumn<>("Phòng");
        colPhong.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getPhong() != null ? d.getValue().getPhong().getMaPhong() : ""));

        TableColumn<BangGiaDetail, String> colGiaMoi = new TableColumn<>("Giá Áp Dụng");
        colGiaMoi.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f VNĐ", d.getValue().getGiaPhongMoi())));

        tableDetail.getColumns().addAll(colPhong, colGiaMoi);
        rightBox.getChildren().addAll(lblRight, tableDetail);

        tablesBox.getChildren().addAll(leftBox, rightBox);

        root.getChildren().addAll(lblTitle, toolbar, tablesBox);
        return root;
    }

    /**
     * TODO: Tải toàn bộ danh sách BangGiaHeader từ database.
     */
    public void loadData() {
        try {
            if (ConnectDB.getConnection() == null) return;
            // TODO: Tạo BangGiaDAO để lấy dữ liệu
            // headerList.setAll(new BangGiaDAO().getAllBangGiaHeader());
            showAlert(Alert.AlertType.INFORMATION, "Phát triển", "Chức năng tải bảng giá đang phát triển. Hãy viết BangGiaDAO.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * TODO: Tải chi tiết giá phòng (BangGiaDetail) tương ứng với bảng giá được chọn.
     */
    private void showDetails(BangGiaHeader header) {
        if (header == null) {
            detailList.clear();
            return;
        }
        // TODO: Gọi BangGiaDAO.getDetailByMaBangGia(header.getMaBangGia())
        detailList.clear(); // Placeholder
    }

    private void handleAdd() {
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng thêm bảng giá đang được phát triển.");
    }

    private void handleEdit() {
        if (tableHeader.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng chọn bảng giá để sửa!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng sửa bảng giá đang được phát triển.");
    }

    private void handleDelete() {
        if (tableHeader.getSelectionModel().getSelectedItem() == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng chọn bảng giá để xóa!");
            return;
        }
        showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng xóa bảng giá đang được phát triển.");
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
