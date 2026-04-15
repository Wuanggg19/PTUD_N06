package gui;

import connectDB.ConnectDB;
import dao.BangGiaDetailDAO;
import dao.BangGiaHeaderDAO;
import dao.PhongDAO;
import entity.BangGiaDetail;
import entity.BangGiaHeader;
import entity.Phong;
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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class PricingView {
    private final ObservableList<BangGiaHeader> headerList = FXCollections.observableArrayList();
    private final ObservableList<BangGiaDetail> detailList = FXCollections.observableArrayList();

    private final BangGiaHeaderDAO bangGiaHeaderDAO = new BangGiaHeaderDAO();
    private final BangGiaDetailDAO bangGiaDetailDAO = new BangGiaDetailDAO();
    private final PhongDAO phongDAO = new PhongDAO();

    private volatile boolean loadingHeaders;
    private volatile String detailLoadingFor;

    private TableView<BangGiaHeader> tableHeader;
    private TableView<BangGiaDetail> tableDetail;

    public Node createView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + AppTheme.BG_LIGHT + ";");

        Label lblTitle = new Label("QUẢN LÝ BẢNG GIÁ");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web(AppTheme.TEXT));

        Label lblHint = new Label("Gợi ý: Bảng giá ngày lễ sẽ cộng thêm % vào giá nền (ngày thường/cuối tuần).");
        lblHint.setTextFill(Color.web(AppTheme.MUTED));

        HBox toolbar = new HBox(10);
        Button btnAdd = createButton("Thêm bảng giá", AppTheme.INFO);
        Button btnEdit = createButton("Sửa bảng giá", AppTheme.PRIMARY);
        Button btnDelete = createButton("Xóa bảng giá", AppTheme.DANGER);
        Button btnRefresh = createButton("Làm mới", AppTheme.MUTED);
        btnAdd.setOnAction(e -> handleAdd());
        btnEdit.setOnAction(e -> handleEdit());
        btnDelete.setOnAction(e -> handleDelete());
        btnRefresh.setOnAction(e -> loadData());
        toolbar.getChildren().addAll(btnAdd, btnEdit, btnDelete, spacer(), btnRefresh);

        tableHeader = new TableView<>(headerList);
        tableHeader.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableHeader.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> showDetails(newV));

        TableColumn<BangGiaHeader, String> colMa = new TableColumn<>("Mã");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaBangGia()));

        TableColumn<BangGiaHeader, String> colTen = new TableColumn<>("Tên bảng giá");
        colTen.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTenBangGia()));

        TableColumn<BangGiaHeader, String> colLoaiNgay = new TableColumn<>("Loại ngày");
        colLoaiNgay.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(displayLoaiNgay(d.getValue().getLoaiNgay())));

        TableColumn<BangGiaHeader, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTrangThai()));

        TableColumn<BangGiaHeader, String> colPhanTram = new TableColumn<>("% tăng");
        colPhanTram.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("%.2f%%", d.getValue().getPhanTramTang())));

        TableColumn<BangGiaHeader, String> colHieuLuc = new TableColumn<>("Hiệu lực");
        colHieuLuc.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getNgayBatDau() + " -> " + d.getValue().getNgayKetThuc()));

        tableHeader.getColumns().setAll(colMa, colTen, colLoaiNgay, colTrangThai, colPhanTram, colHieuLuc);

        tableDetail = new TableView<>(detailList);
        tableDetail.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<BangGiaDetail, String> colLoai = new TableColumn<>("Loại phòng");
        colLoai.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getLoaiPhongApDung()));

        TableColumn<BangGiaDetail, String> colGiaGio = new TableColumn<>("Giá giờ");
        colGiaGio.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("%,.0f", d.getValue().getGiaTheoGio())));

        TableColumn<BangGiaDetail, String> colGiaNgay = new TableColumn<>("Giá ngày");
        colGiaNgay.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("%,.0f", d.getValue().getGiaTheoNgay())));

        TableColumn<BangGiaDetail, String> colGiaCuoiTuan = new TableColumn<>("Cuối tuần");
        colGiaCuoiTuan.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("%,.0f", d.getValue().getGiaCuoiTuan())));

        TableColumn<BangGiaDetail, String> colGiaLe = new TableColumn<>("Giá lễ");
        colGiaLe.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(String.format("%,.0f", d.getValue().getGiaLe())));

        tableDetail.getColumns().setAll(colLoai, colGiaGio, colGiaNgay, colGiaCuoiTuan, colGiaLe);

        HBox detailToolbar = new HBox(10);
        detailToolbar.setAlignment(Pos.CENTER_LEFT);
        detailToolbar.getChildren().addAll(
                new Label("Chi tiết theo loại phòng"),
                createActionButton("Cập nhật giá loại phòng", AppTheme.INFO, this::handleEditDetail)
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
                    throw new IllegalStateException("Khong the ket noi toi database: "
                            + ConnectDB.getInstance().getConfiguredUrl());
                }
                return bangGiaHeaderDAO.getAllBangGiaHeader();
            }
        };

        task.setOnSucceeded(e -> {
            loadingHeaders = false;
            List<BangGiaHeader> headers = task.getValue();
            headerList.setAll(headers);
            detailList.clear();
            if (!headers.isEmpty()) {
                tableHeader.getSelectionModel().selectFirst();
            }
        });
        task.setOnFailed(e -> {
            loadingHeaders = false;
            headerList.clear();
            detailList.clear();
            showAlert(
                    Alert.AlertType.ERROR,
                    "Loi tai bang gia",
                    "Khong the tai du lieu BangGiaHeader.\nKiem tra ket noi DB: "
                            + ConnectDB.getInstance().getActiveUrl()
            );
            task.getException().printStackTrace();
        });

        Thread thread = new Thread(task, "pricing-load-headers");
        thread.setDaemon(true);
        thread.start();
    }

    private void showDetails(BangGiaHeader header) {
        if (header == null) {
            detailList.clear();
            return;
        }

        String maBangGia = header.getMaBangGia();
        detailLoadingFor = maBangGia;
        Task<List<BangGiaDetail>> task = new Task<>() {
            @Override
            protected List<BangGiaDetail> call() {
                return bangGiaDetailDAO.getDetailsByMaBangGia(maBangGia)
                        .stream()
                        .collect(Collectors.toMap(
                                BangGiaDetail::getLoaiPhongApDung,
                                item -> item,
                                (oldValue, newValue) -> oldValue,
                                LinkedHashMap::new
                        ))
                        .values().stream()
                        .sorted(Comparator.comparing(BangGiaDetail::getLoaiPhongApDung))
                        .toList();
            }
        };
        task.setOnSucceeded(e -> {
            BangGiaHeader selected = tableHeader.getSelectionModel().getSelectedItem();
            if (selected != null && maBangGia.equals(selected.getMaBangGia()) && maBangGia.equals(detailLoadingFor)) {
                detailList.setAll(task.getValue());
            }
        });
        task.setOnFailed(e -> task.getException().printStackTrace());

        Thread thread = new Thread(task, "pricing-load-details");
        thread.setDaemon(true);
        thread.start();
    }

    private void handleAdd() {
        Dialog<BangGiaHeader> dialog = buildHeaderDialog(null);
        Optional<BangGiaHeader> result = dialog.showAndWait();
        result.ifPresent(bg -> {
            if (bangGiaHeaderDAO.create(bg)) {
                if (!BangGiaHeader.LOAI_NGAY_LE.equals(bg.getLoaiNgay())) {
                    seedDefaultDetails(bg);
                }
                loadData();
            }
        });
    }

    private void handleEdit() {
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
                tableHeader.getSelectionModel().select(selected);
            }
        });
    }

    private void handleDelete() {
        BangGiaHeader selected = tableHeader.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng chọn bảng giá cần xóa.");
            return;
        }
        if (bangGiaDetailDAO.deleteByMaBangGia(selected.getMaBangGia()) && bangGiaHeaderDAO.delete(selected.getMaBangGia())) {
            loadData();
        }
    }

    private void handleEditDetail() {
        BangGiaHeader selectedHeader = tableHeader.getSelectionModel().getSelectedItem();
        BangGiaDetail selectedDetail = tableDetail.getSelectionModel().getSelectedItem();
        if (selectedHeader == null || selectedDetail == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Vui lòng chọn bảng giá và loại phòng.");
            return;
        }

        if (BangGiaHeader.LOAI_NGAY_LE.equals(selectedHeader.getLoaiNgay())) {
            showAlert(Alert.AlertType.INFORMATION, "Ngày lễ", "Bảng giá ngày lễ chỉ dùng % tăng, không sửa giá cứng theo loại phòng.");
            return;
        }

        TextField txtGia = new TextField(String.valueOf((long) selectedDetail.getGiaPhongMoi()));
        Dialog<Double> dialog = new Dialog<>();
        dialog.setTitle("Cập nhật giá");
        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        dialog.getDialogPane().setContent(new VBox(10,
                new Label("Loại phòng: " + selectedDetail.getLoaiPhongApDung()),
                new Label("Giá áp dụng"),
                txtGia));
        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }
            try {
                return Double.parseDouble(txtGia.getText().trim());
            } catch (Exception ex) {
                return null;
            }
        });

        Optional<Double> result = dialog.showAndWait();
        result.ifPresent(giaMoi -> updatePriceForRoomType(selectedHeader, selectedDetail.getLoaiPhongApDung(), giaMoi));
    }

    private void updatePriceForRoomType(BangGiaHeader header, String loaiPhong, double giaMoi) {
        List<Phong> sameTypeRooms = phongDAO.getAllPhong().stream()
                .filter(room -> loaiPhong.equals(room.getLoaiPhong()))
                .toList();
        for (Phong room : sameTypeRooms) {
            bangGiaDetailDAO.saveOrUpdate(new BangGiaDetail(header, room, loaiPhong, giaMoi));
        }
        showDetails(header);
    }

    private void seedDefaultDetails(BangGiaHeader header) {
        Map<String, Phong> representativeRooms = phongDAO.getAllPhong().stream()
                .collect(Collectors.toMap(Phong::getLoaiPhong, room -> room, (oldValue, newValue) -> oldValue));
        representativeRooms.forEach((roomType, room) ->
                updatePriceForRoomType(header, roomType, room.getGiaPhong()));
    }

    private Dialog<BangGiaHeader> buildHeaderDialog(BangGiaHeader existing) {
        Dialog<BangGiaHeader> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Thêm bảng giá" : "Sửa bảng giá");

        TextField txtMa = new TextField(existing != null ? existing.getMaBangGia() : "");
        TextField txtTen = new TextField(existing != null ? existing.getTenBangGia() : "");

        ComboBox<String> cboLoai = new ComboBox<>(FXCollections.observableArrayList(
                BangGiaHeader.LOAI_NGAY_THUONG,
                BangGiaHeader.LOAI_CUOI_TUAN,
                BangGiaHeader.LOAI_NGAY_LE
        ));
        cboLoai.setValue(existing != null ? existing.getLoaiNgay() : BangGiaHeader.LOAI_NGAY_THUONG);

        ComboBox<String> cboTrangThai = new ComboBox<>(FXCollections.observableArrayList(
                BangGiaHeader.TRANG_THAI_DANG_HOAT_DONG,
                BangGiaHeader.TRANG_THAI_KHONG_HOAT_DONG
        ));
        cboTrangThai.setValue(existing != null ? existing.getTrangThai() : BangGiaHeader.TRANG_THAI_DANG_HOAT_DONG);

        TextField txtPhanTramTang = new TextField(existing != null ? String.valueOf(existing.getPhanTramTang()) : "0");

        DatePicker dpBatDau = new DatePicker(existing != null ? existing.getNgayBatDau() : LocalDate.now());
        DatePicker dpKetThuc = new DatePicker(existing != null ? existing.getNgayKetThuc() : LocalDate.now().plusMonths(1));

        Label lblRule = new Label("Ngày lễ sẽ cộng thêm % vào giá nền.");
        lblRule.setTextFill(Color.web(AppTheme.MUTED));

        cboLoai.valueProperty().addListener((obs, oldV, newV) -> {
            if (BangGiaHeader.LOAI_NGAY_LE.equals(newV)) {
                txtPhanTramTang.setDisable(false);
            } else {
                txtPhanTramTang.setText("0");
                txtPhanTramTang.setDisable(true);
            }
        });
        if (!BangGiaHeader.LOAI_NGAY_LE.equals(cboLoai.getValue())) {
            txtPhanTramTang.setDisable(true);
        }

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Mã"), 0, 0);
        grid.add(txtMa, 1, 0);
        grid.add(new Label("Tên"), 0, 1);
        grid.add(txtTen, 1, 1);
        grid.add(new Label("Loại ngày"), 0, 2);
        grid.add(cboLoai, 1, 2);
        grid.add(new Label("Trạng thái"), 0, 3);
        grid.add(cboTrangThai, 1, 3);
        grid.add(new Label("% tăng"), 0, 4);
        grid.add(txtPhanTramTang, 1, 4);
        grid.add(new Label("Bắt đầu"), 0, 5);
        grid.add(dpBatDau, 1, 5);
        grid.add(new Label("Kết thúc"), 0, 6);
        grid.add(dpKetThuc, 1, 6);
        grid.add(lblRule, 1, 7);

        dialog.getDialogPane().getButtonTypes().addAll(new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE), ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(btn -> {
            if (btn.getButtonData() != ButtonBar.ButtonData.OK_DONE) {
                return null;
            }

            if (txtTen.getText().trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Tên bảng giá không được trống.");
                return null;
            }
            if (dpBatDau.getValue() == null || dpKetThuc.getValue() == null || dpKetThuc.getValue().isBefore(dpBatDau.getValue())) {
                showAlert(Alert.AlertType.WARNING, "Sai hiệu lực", "Ngày kết thúc phải lớn hơn hoặc bằng ngày bắt đầu.");
                return null;
            }

            double phanTramTang = 0;
            try {
                phanTramTang = Double.parseDouble(txtPhanTramTang.getText().trim());
            } catch (Exception ex) {
                showAlert(Alert.AlertType.WARNING, "Sai dữ liệu", "% tăng phải là số.");
                return null;
            }

            if (BangGiaHeader.LOAI_NGAY_LE.equals(cboLoai.getValue())) {
                if (phanTramTang < 0) {
                    showAlert(Alert.AlertType.WARNING, "Sai dữ liệu", "% tăng ngày lễ phải >= 0.");
                    return null;
                }
            } else {
                phanTramTang = 0;
            }

            String maBangGia = existing != null ? existing.getMaBangGia() : txtMa.getText().trim();
            if (maBangGia.isBlank()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu dữ liệu", "Mã bảng giá không được trống.");
                return null;
            }

            return new BangGiaHeader(
                    maBangGia,
                    txtTen.getText().trim(),
                    dpBatDau.getValue(),
                    dpKetThuc.getValue(),
                    cboLoai.getValue(),
                    cboTrangThai.getValue(),
                    phanTramTang);
        });
        return dialog;
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
