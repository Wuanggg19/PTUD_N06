package gui;

import connectDB.ConnectDB;
import dao.BangGiaHeaderDAO;
import dao.ChiTietBangGiaDAO;
import entity.BangGiaHeader;
import entity.ChiTietBangGia;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import util.AppTheme;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class PricingView {
    private final ObservableList<BangGiaHeader> headerList = FXCollections.observableArrayList();
    private final ObservableList<ChiTietBangGia> detailList = FXCollections.observableArrayList();

    private final BangGiaHeaderDAO bangGiaHeaderDAO = new BangGiaHeaderDAO();
    private final ChiTietBangGiaDAO chiTietBangGiaDAO = new ChiTietBangGiaDAO();

    private volatile boolean loadingHeaders;
    private TableView<BangGiaHeader> tableHeader;
    private TableView<ChiTietBangGia> tableDetail;

    public Node createView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + AppTheme.BG_LIGHT + ";");

        Label lblTitle = new Label("QUẢN LÝ BẢNG GIÁ");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web(AppTheme.TEXT));

        Label lblHint = new Label("Dữ liệu bảng giá được lấy từ BangGiaHeader và ChiTietBangGia.");
        lblHint.setTextFill(Color.web(AppTheme.MUTED));

        HBox toolbar = new HBox(10);
        Button btnAdd = createButton("Thêm bảng giá", AppTheme.INFO);
        Button btnEdit = createButton("Sửa bảng giá", AppTheme.PRIMARY);
        Button btnDelete = createButton("Xóa bảng giá", AppTheme.DANGER);
        Button btnRefresh = createButton("Làm mới", AppTheme.MUTED);
        btnAdd.setOnAction(e -> handleAddHeader());
        btnEdit.setOnAction(e -> handleEditHeader());
        btnDelete.setOnAction(e -> handleDeleteHeader());
        btnRefresh.setOnAction(e -> loadData());
        toolbar.getChildren().addAll(btnAdd, btnEdit, btnDelete, spacer(), btnRefresh);

        tableHeader = createHeaderTable();
        tableDetail = createDetailTable();
        tableHeader.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> loadDetails(newV));

        HBox detailToolbar = new HBox(10);
        detailToolbar.setAlignment(Pos.CENTER_LEFT);
        detailToolbar.getChildren().addAll(
                new Label("Chi tiết bảng giá theo loại phòng"),
                createActionButton("Thêm chi tiết", AppTheme.INFO, this::handleAddDetail),
                createActionButton("Sửa chi tiết", AppTheme.PRIMARY, this::handleEditDetail),
                createActionButton("Xóa chi tiết", AppTheme.DANGER, this::handleDeleteDetail)
        );

        VBox left = new VBox(10, new Label("Danh sách bảng giá"), tableHeader);
        VBox right = new VBox(10, detailToolbar, tableDetail);
        VBox.setVgrow(tableHeader, Priority.ALWAYS);
        VBox.setVgrow(tableDetail, Priority.ALWAYS);
        HBox content = new HBox(18, left, right);
        HBox.setHgrow(left, Priority.ALWAYS);
        HBox.setHgrow(right, Priority.ALWAYS);

        root.getChildren().addAll(lblTitle, lblHint, toolbar, content);
        loadData();
        return root;
    }

    private TableView<BangGiaHeader> createHeaderTable() {
        TableView<BangGiaHeader> table = new TableView<>(headerList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<BangGiaHeader, String> colMa = new TableColumn<>("Mã bảng giá");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaBangGia()));

        TableColumn<BangGiaHeader, String> colTen = new TableColumn<>("Tên bảng giá");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenBangGia()));

        TableColumn<BangGiaHeader, String> colLoaiBangGia = new TableColumn<>("Loại bảng giá");
        colLoaiBangGia.setCellValueFactory(d -> new SimpleStringProperty(displayText(d.getValue().getLoaiBangGia())));

        TableColumn<BangGiaHeader, String> colLoaiNgay = new TableColumn<>("Loại ngày");
        colLoaiNgay.setCellValueFactory(d -> new SimpleStringProperty(displayLoaiNgay(d.getValue().getLoaiNgay())));

        TableColumn<BangGiaHeader, String> colHieuLuc = new TableColumn<>("Hiệu lực");
        colHieuLuc.setCellValueFactory(d -> new SimpleStringProperty(formatRange(d.getValue().getNgayBatDau(), d.getValue().getNgayKetThuc())));

        TableColumn<BangGiaHeader, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTrangThai()));

        table.getColumns().setAll(colMa, colTen, colLoaiBangGia, colLoaiNgay, colHieuLuc, colTrangThai);
        return table;
    }

    private TableView<ChiTietBangGia> createDetailTable() {
        TableView<ChiTietBangGia> table = new TableView<>(detailList);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<ChiTietBangGia, String> colMaLoai = new TableColumn<>("Mã loại phòng");
        colMaLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaLoai()));

        TableColumn<ChiTietBangGia, String> colGiaTheoGio = new TableColumn<>("Giá theo giờ");
        colGiaTheoGio.setCellValueFactory(d -> new SimpleStringProperty(formatMoney(d.getValue().getGiaTheoGio())));

        TableColumn<ChiTietBangGia, String> colGiaQuaDem = new TableColumn<>("Giá qua đêm");
        colGiaQuaDem.setCellValueFactory(d -> new SimpleStringProperty(formatMoney(d.getValue().getGiaQuaDem())));

        TableColumn<ChiTietBangGia, String> colGiaTheoNgay = new TableColumn<>("Giá theo ngày");
        colGiaTheoNgay.setCellValueFactory(d -> new SimpleStringProperty(formatMoney(d.getValue().getGiaTheoNgay())));

        TableColumn<ChiTietBangGia, String> colGiaCuoiTuan = new TableColumn<>("Giá cuối tuần");
        colGiaCuoiTuan.setCellValueFactory(d -> new SimpleStringProperty(formatMoney(d.getValue().getGiaCuoiTuan())));

        TableColumn<ChiTietBangGia, String> colGiaLe = new TableColumn<>("Giá lễ");
        colGiaLe.setCellValueFactory(d -> new SimpleStringProperty(formatMoney(d.getValue().getGiaLe())));

        TableColumn<ChiTietBangGia, String> colPhuThu = new TableColumn<>("Phụ thu");
        colPhuThu.setCellValueFactory(d -> new SimpleStringProperty(formatMoney(d.getValue().getPhuThu())));

        table.getColumns().setAll(colMaLoai, colGiaTheoGio, colGiaQuaDem, colGiaTheoNgay, colGiaCuoiTuan, colGiaLe, colPhuThu);
        return table;
    }

    public void loadData() {
        if (loadingHeaders) {
            return;
        }

        loadingHeaders = true;
        Task<List<BangGiaHeader>> task = new Task<>() {
            @Override
            protected List<BangGiaHeader> call() throws Exception {
                Connection connection = ConnectDB.getConnection();
                if (connection == null || connection.isClosed()) {
                    throw new IllegalStateException("Không thể kết nối tới cơ sở dữ liệu.");
                }
                return bangGiaHeaderDAO.getAllBangGiaHeader();
            }
        };

        task.setOnSucceeded(e -> {
            loadingHeaders = false;
            headerList.setAll(task.getValue());
            detailList.clear();
            if (!headerList.isEmpty()) {
                tableHeader.getSelectionModel().selectFirst();
            }
        });

        task.setOnFailed(e -> {
            loadingHeaders = false;
            detailList.clear();
            headerList.clear();
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
            showAlert(Alert.AlertType.ERROR, "Lỗi tải bảng giá",
                    "Không thể tải dữ liệu từ BangGiaHeader. Vui lòng kiểm tra kết nối hoặc câu lệnh SQL.");
        });

        Thread thread = new Thread(task, "pricing-load-header");
        thread.setDaemon(true);
        thread.start();
    }

    private void loadDetails(BangGiaHeader header) {
        detailList.clear();
        if (header == null) {
            return;
        }

        Task<List<ChiTietBangGia>> task = new Task<>() {
            @Override
            protected List<ChiTietBangGia> call() {
                return chiTietBangGiaDAO.getDetailsByMaBangGia(header.getMaBangGia());
            }
        };

        task.setOnSucceeded(e -> detailList.setAll(task.getValue()));
        task.setOnFailed(e -> {
            if (task.getException() != null) {
                task.getException().printStackTrace();
            }
            showAlert(Alert.AlertType.ERROR, "Lỗi tải chi tiết bảng giá",
                    "Không thể tải dữ liệu từ ChiTietBangGia cho bảng giá đã chọn.");
        });

        Thread thread = new Thread(task, "pricing-load-detail");
        thread.setDaemon(true);
        thread.start();
    }

    private void handleAddHeader() {
        Dialog<BangGiaHeader> dialog = buildHeaderDialog(null);
        Optional<BangGiaHeader> result = dialog.showAndWait();
        result.ifPresent(bg -> {
            if (bangGiaHeaderDAO.create(bg)) {
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Thêm bảng giá thất bại",
                        "Không thể thêm bảng giá vào BangGiaHeader.");
            }
        });
    }

    private void handleEditHeader() {
        BangGiaHeader selected = tableHeader.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng chọn bảng giá cần sửa.");
            return;
        }

        Dialog<BangGiaHeader> dialog = buildHeaderDialog(selected);
        Optional<BangGiaHeader> result = dialog.showAndWait();
        result.ifPresent(bg -> {
            if (bangGiaHeaderDAO.update(bg)) {
                loadData();
            } else {
                showAlert(Alert.AlertType.ERROR, "Cập nhật bảng giá thất bại",
                        "Không thể cập nhật BangGiaHeader. Vui lòng kiểm tra dữ liệu nhập.");
            }
        });
    }

    private void handleDeleteHeader() {
        BangGiaHeader selected = tableHeader.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng chọn bảng giá cần xóa.");
            return;
        }

        if (!chiTietBangGiaDAO.deleteByMaBangGia(selected.getMaBangGia())) {
            showAlert(Alert.AlertType.ERROR, "Xóa thất bại",
                    "Không thể xóa các dòng ChiTietBangGia của bảng giá đã chọn.");
            return;
        }
        if (bangGiaHeaderDAO.delete(selected.getMaBangGia())) {
            loadData();
        } else {
            showAlert(Alert.AlertType.ERROR, "Xóa thất bại",
                    "Không thể xóa BangGiaHeader sau khi đã xóa chi tiết.");
        }
    }

    private void handleAddDetail() {
        BangGiaHeader selectedHeader = tableHeader.getSelectionModel().getSelectedItem();
        if (selectedHeader == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng chọn bảng giá trước khi thêm chi tiết.");
            return;
        }

        Dialog<ChiTietBangGia> dialog = buildDetailDialog(selectedHeader, null);
        Optional<ChiTietBangGia> result = dialog.showAndWait();
        result.ifPresent(ct -> {
            if (chiTietBangGiaDAO.create(ct)) {
                loadDetails(selectedHeader);
            } else {
                showAlert(Alert.AlertType.ERROR, "Thêm chi tiết thất bại",
                        "Không thể thêm dữ liệu vào ChiTietBangGia.");
            }
        });
    }

    private void handleEditDetail() {
        BangGiaHeader selectedHeader = tableHeader.getSelectionModel().getSelectedItem();
        ChiTietBangGia selectedDetail = tableDetail.getSelectionModel().getSelectedItem();
        if (selectedHeader == null || selectedDetail == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng chọn dòng chi tiết cần sửa.");
            return;
        }

        Dialog<ChiTietBangGia> dialog = buildDetailDialog(selectedHeader, selectedDetail);
        Optional<ChiTietBangGia> result = dialog.showAndWait();
        result.ifPresent(ct -> {
            if (chiTietBangGiaDAO.update(ct)) {
                loadDetails(selectedHeader);
            } else {
                showAlert(Alert.AlertType.ERROR, "Cập nhật chi tiết thất bại",
                        "Không thể cập nhật dữ liệu trong ChiTietBangGia.");
            }
        });
    }

    private void handleDeleteDetail() {
        BangGiaHeader selectedHeader = tableHeader.getSelectionModel().getSelectedItem();
        ChiTietBangGia selectedDetail = tableDetail.getSelectionModel().getSelectedItem();
        if (selectedHeader == null || selectedDetail == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng chọn dòng chi tiết cần xóa.");
            return;
        }

        if (chiTietBangGiaDAO.delete(selectedHeader.getMaBangGia(), selectedDetail.getMaLoai())) {
            loadDetails(selectedHeader);
        } else {
            showAlert(Alert.AlertType.ERROR, "Xóa chi tiết thất bại",
                    "Không thể xóa dòng dữ liệu trong ChiTietBangGia.");
        }
    }

    private Dialog<BangGiaHeader> buildHeaderDialog(BangGiaHeader existing) {
        Dialog<BangGiaHeader> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm bảng giá" : "Sửa bảng giá");

        TextField txtMa = new TextField(existing != null ? existing.getMaBangGia() : "");
        txtMa.setDisable(existing != null);
        TextField txtTen = new TextField(existing != null ? existing.getTenBangGia() : "");
        TextField txtLoaiBangGia = new TextField(existing != null ? existing.getLoaiBangGia() : "");
        ComboBox<String> cboLoaiNgay = new ComboBox<>(FXCollections.observableArrayList(
                BangGiaHeader.LOAI_NGAY_THUONG,
                BangGiaHeader.LOAI_CUOI_TUAN,
                BangGiaHeader.LOAI_NGAY_LE
        ));
        cboLoaiNgay.setValue(existing != null ? existing.getLoaiNgay() : BangGiaHeader.LOAI_NGAY_THUONG);
        DatePicker dpBatDau = new DatePicker(existing != null ? existing.getNgayBatDau() : LocalDate.now());
        DatePicker dpKetThuc = new DatePicker(existing != null ? existing.getNgayKetThuc() : LocalDate.now().plusMonths(1));
        CheckBox chkTrangThai = new CheckBox("Đang hoạt động");
        chkTrangThai.setSelected(existing == null || existing.isDangHoatDong());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Mã bảng giá"), 0, 0);
        grid.add(txtMa, 1, 0);
        grid.add(new Label("Tên bảng giá"), 0, 1);
        grid.add(txtTen, 1, 1);
        grid.add(new Label("Loại bảng giá"), 0, 2);
        grid.add(txtLoaiBangGia, 1, 2);
        grid.add(new Label("Loại ngày"), 0, 3);
        grid.add(cboLoaiNgay, 1, 3);
        grid.add(new Label("Ngày bắt đầu"), 0, 4);
        grid.add(dpBatDau, 1, 4);
        grid.add(new Label("Ngày kết thúc"), 0, 5);
        grid.add(dpKetThuc, 1, 5);
        grid.add(new Label("Trạng thái"), 0, 6);
        grid.add(chkTrangThai, 1, 6);

        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }

            if (txtMa.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Mã bảng giá không được để trống.");
                return null;
            }
            if (txtTen.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Tên bảng giá không được để trống.");
                return null;
            }
            if (txtLoaiBangGia.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Loại bảng giá không được để trống.");
                return null;
            }
            if (dpBatDau.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Ngày bắt đầu không được để trống.");
                return null;
            }
            if (dpKetThuc.getValue() != null && dpKetThuc.getValue().isBefore(dpBatDau.getValue())) {
                showAlert(Alert.AlertType.WARNING, "Sai dữ liệu", "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.");
                return null;
            }

            return new BangGiaHeader(
                    txtMa.getText().trim(),
                    txtTen.getText().trim(),
                    txtLoaiBangGia.getText().trim(),
                    cboLoaiNgay.getValue(),
                    dpBatDau.getValue(),
                    dpKetThuc.getValue(),
                    chkTrangThai.isSelected(),
                    0
            );
        });
        return dialog;
    }

    private Dialog<ChiTietBangGia> buildDetailDialog(BangGiaHeader header, ChiTietBangGia existing) {
        Dialog<ChiTietBangGia> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm chi tiết bảng giá" : "Sửa chi tiết bảng giá");

        TextField txtMaLoai = new TextField(existing != null ? existing.getMaLoai() : "");
        txtMaLoai.setDisable(existing != null);
        TextField txtGiaTheoGio = new TextField(existing != null ? String.valueOf(existing.getGiaTheoGio()) : "0");
        TextField txtGiaQuaDem = new TextField(existing != null ? String.valueOf(existing.getGiaQuaDem()) : "0");
        TextField txtGiaTheoNgay = new TextField(existing != null ? String.valueOf(existing.getGiaTheoNgay()) : "0");
        TextField txtGiaCuoiTuan = new TextField(existing != null ? String.valueOf(existing.getGiaCuoiTuan()) : "0");
        TextField txtGiaLe = new TextField(existing != null ? String.valueOf(existing.getGiaLe()) : "0");
        TextField txtPhuThu = new TextField(existing != null ? String.valueOf(existing.getPhuThu()) : "0");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Mã loại phòng"), 0, 0);
        grid.add(txtMaLoai, 1, 0);
        grid.add(new Label("Giá theo giờ"), 0, 1);
        grid.add(txtGiaTheoGio, 1, 1);
        grid.add(new Label("Giá qua đêm"), 0, 2);
        grid.add(txtGiaQuaDem, 1, 2);
        grid.add(new Label("Giá theo ngày"), 0, 3);
        grid.add(txtGiaTheoNgay, 1, 3);
        grid.add(new Label("Giá cuối tuần"), 0, 4);
        grid.add(txtGiaCuoiTuan, 1, 4);
        grid.add(new Label("Giá lễ"), 0, 5);
        grid.add(txtGiaLe, 1, 5);
        grid.add(new Label("Phụ thu"), 0, 6);
        grid.add(txtPhuThu, 1, 6);

        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }

            if (txtMaLoai.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Mã loại phòng không được để trống.");
                return null;
            }

            try {
                return new ChiTietBangGia(
                        header,
                        txtMaLoai.getText().trim(),
                        parseMoney(txtGiaTheoGio.getText()),
                        parseMoney(txtGiaQuaDem.getText()),
                        parseMoney(txtGiaTheoNgay.getText()),
                        parseMoney(txtGiaCuoiTuan.getText()),
                        parseMoney(txtGiaLe.getText()),
                        parseMoney(txtPhuThu.getText())
                );
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                showAlert(Alert.AlertType.WARNING, "Sai dữ liệu", "Các trường giá phải là số hợp lệ.");
                return null;
            }
        });
        return dialog;
    }

    private double parseMoney(String value) {
        return Double.parseDouble(value.trim());
    }

    private String formatMoney(double value) {
        return String.format("%,.0f", value);
    }

    private String formatRange(LocalDate fromDate, LocalDate toDate) {
        return (fromDate != null ? fromDate : "") + " -> " + (toDate != null ? toDate : "Không giới hạn");
    }

    private String displayText(String value) {
        return value == null || value.isBlank() ? "Chưa cập nhật" : value;
    }

    private String displayLoaiNgay(String loaiNgay) {
        if (BangGiaHeader.LOAI_CUOI_TUAN.equals(loaiNgay)) {
            return "Cuối tuần";
        }
        if (BangGiaHeader.LOAI_NGAY_LE.equals(loaiNgay)) {
            return "Ngày lễ";
        }
        return "Ngày thường";
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setTextFill(Color.WHITE);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8;");
        btn.setPrefHeight(38);
        return btn;
    }

    private Button createActionButton(String text, String color, Runnable action) {
        Button btn = createButton(text, color);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
