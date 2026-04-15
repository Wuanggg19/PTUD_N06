package gui;

import dao.PhongDAO;
import entity.Phong;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import util.AppTheme;
import util.RoomStatus;
import util.StatusUtils;

import java.util.List;
import java.util.stream.Collectors;

public class RoomManagementView {
    private final PhongDAO dao = new PhongDAO();
    private final TableView<Phong> tableView = new TableView<>();

    private List<Phong> allData;
    private TextField txtSearch;
    private TextField txtMa;
    private TextField txtSoNguoi;
    private TextField txtTang;
    private TextField txtGia;
    private ComboBox<String> cbLoaiPhong;
    private ComboBox<String> cbTrangThai;

    public Node createView() {
        HBox root = new HBox(24);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + AppTheme.BG_LIGHT + ";");

        VBox leftCol = new VBox(16);
        HBox.setHgrow(leftCol, Priority.ALWAYS);

        Label lblTitle = new Label("QUẢN LÝ PHÒNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web(AppTheme.TEXT));

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã phòng, loại phòng hoặc tầng...");
        txtSearch.textProperty().addListener((o, v, n) -> filterData());
        txtSearch.setPrefHeight(38);

        Button btnRefresh = createBtn("Làm mới", AppTheme.PRIMARY);
        btnRefresh.setOnAction(e -> loadData());

        HBox searchRow = new HBox(10, txtSearch, btnRefresh);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);
        configureTable();

        leftCol.getChildren().addAll(lblTitle, searchRow, tableView);

        VBox rightCol = new VBox(12);
        rightCol.setPrefWidth(320);
        rightCol.setPadding(new Insets(22));
        rightCol.setStyle("-fx-background-color: " + AppTheme.SURFACE + "; -fx-background-radius: 14;");

        txtMa = createField("P001");
        txtSoNguoi = createField("2");
        txtTang = createField("1");
        txtGia = createField("500000");

        cbLoaiPhong = new ComboBox<>();
        cbLoaiPhong.getItems().addAll("Standard", "Deluxe", "Suite", "VIP", "Family");
        cbLoaiPhong.setValue("Standard");
        cbLoaiPhong.setMaxWidth(Double.MAX_VALUE);

        cbTrangThai = new ComboBox<>();
        cbTrangThai.getItems().addAll(
                RoomStatus.TRONG.getCode(),
                RoomStatus.CHO_XAC_NHAN.getCode(),
                RoomStatus.DANG_O.getCode(),
                RoomStatus.SUA_CHUA.getCode()
        );
        cbTrangThai.setValue(RoomStatus.TRONG.getCode());
        cbTrangThai.setMaxWidth(Double.MAX_VALUE);

        rightCol.getChildren().addAll(
                formRow("Mã phòng", txtMa),
                formRow("Loại phòng", cbLoaiPhong),
                formRow("Số người", txtSoNguoi),
                formRow("Tầng", txtTang),
                formRow("Trạng thái", cbTrangThai),
                formRow("Giá gốc", txtGia),
                createBtn("Thêm mới", AppTheme.INFO, this::handleThem),
                createBtn("Cập nhật", AppTheme.PRIMARY, this::handleCapNhat),
                createBtn("Xóa phòng", AppTheme.DANGER, this::handleXoa),
                createBtn("Làm mới form", AppTheme.MUTED, this::clearForm)
        );

        root.getChildren().addAll(leftCol, rightCol);
        loadData();
        return root;
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

    private void configureTable() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);

        TableColumn<Phong, String> colMa = new TableColumn<>("Mã");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaPhong()));

        TableColumn<Phong, String> colLoai = new TableColumn<>("Loại");
        colLoai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLoaiPhong()));

        TableColumn<Phong, String> colNguoi = new TableColumn<>("Số người");
        colNguoi.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoNguoi())));

        TableColumn<Phong, String> colTang = new TableColumn<>("Tầng");
        colTang.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getTang())));

        TableColumn<Phong, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(StatusUtils.roomLabel(d.getValue().getTrangThai())));
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(item);
                setStyle("-fx-font-weight: bold; -fx-text-fill: " + switch (item) {
                    case "Đang ở" -> AppTheme.DANGER;
                    case "Chờ xác nhận" -> AppTheme.WARNING;
                    case "Sửa chữa" -> AppTheme.MUTED;
                    default -> AppTheme.SUCCESS;
                } + ";");
            }
        });

        TableColumn<Phong, String> colGia = new TableColumn<>("Giá gốc");
        colGia.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f", d.getValue().getGiaPhong())));

        tableView.getColumns().setAll(colMa, colLoai, colNguoi, colTang, colTrangThai, colGia);
        tableView.setRowFactory(tv -> {
            TableRow<Phong> row = new TableRow<>();
            row.setOnMouseClicked(event -> fillForm(row.getItem()));
            return row;
        });
    }

    private void filterData() {
        if (allData == null) return;
        String keyword = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase();
        tableView.getItems().setAll(allData.stream()
                .filter(p -> keyword.isBlank()
                        || p.getMaPhong().toLowerCase().contains(keyword)
                        || p.getLoaiPhong().toLowerCase().contains(keyword)
                        || String.valueOf(p.getTang()).contains(keyword))
                .collect(Collectors.toList()));
    }

    private void fillForm(Phong p) {
        if (p == null) return;
        txtMa.setText(p.getMaPhong());
        txtMa.setEditable(false);
        txtSoNguoi.setText(String.valueOf(p.getSoNguoi()));
        txtTang.setText(String.valueOf(p.getTang()));
        txtGia.setText(String.valueOf((long) p.getGiaPhong()));
        cbLoaiPhong.setValue(p.getLoaiPhong());
        cbTrangThai.setValue(StatusUtils.roomCode(p.getTrangThai()));
    }

    private void handleThem() {
        try {
            Phong p = buildPhongFromForm();
            if (dao.create(p)) {
                clearForm();
                loadData();
            } else {
                showAlert("Lỗi", "Không thể thêm phòng.");
            }
        } catch (Exception ex) {
            showAlert("Lỗi dữ liệu", ex.getMessage());
        }
    }

    private void handleCapNhat() {
        Phong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn phòng cần cập nhật.");
            return;
        }
        try {
            Phong updated = buildPhongFromForm();
            updated.setMaPhong(selected.getMaPhong());
            if (dao.update(updated)) {
                loadData();
            } else {
                showAlert("Lỗi", "Cập nhật thất bại.");
            }
        } catch (Exception ex) {
            showAlert("Lỗi dữ liệu", ex.getMessage());
        }
    }

    private void handleXoa() {
        Phong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn phòng cần xóa.");
            return;
        }
        if (!StatusUtils.isRoomStatus(selected.getTrangThai(), RoomStatus.TRONG)) {
            showAlert("Không thể xóa", "Chỉ được xóa phòng đang trống.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Xóa phòng " + selected.getMaPhong() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES && dao.delete(selected.getMaPhong())) {
                clearForm();
                loadData();
            }
        });
    }

    private Phong buildPhongFromForm() {
        Phong p = new Phong();
        p.setMaPhong(txtMa.getText().trim());
        p.setLoaiPhong(cbLoaiPhong.getValue());
        p.setSoNguoi(Integer.parseInt(txtSoNguoi.getText().trim()));
        p.setTang(Integer.parseInt(txtTang.getText().trim()));
        p.setTrangThai(cbTrangThai.getValue());
        p.setGiaPhong(Double.parseDouble(txtGia.getText().trim().replace(",", "")));
        return p;
    }

    private void clearForm() {
        txtMa.clear();
        txtMa.setEditable(true);
        txtSoNguoi.clear();
        txtTang.clear();
        txtGia.clear();
        cbLoaiPhong.setValue("Standard");
        cbTrangThai.setValue(RoomStatus.TRONG.getCode());
        tableView.getSelectionModel().clearSelection();
    }

    private TextField createField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(38);
        return field;
    }

    private VBox formRow(String label, Control field) {
        VBox box = new VBox(6);
        Label lbl = new Label(label);
        lbl.setTextFill(Color.web(AppTheme.MUTED));
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        box.getChildren().addAll(lbl, field);
        return box;
    }

    private Button createBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setTextFill(Color.WHITE);
        btn.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8;");
        btn.setPrefHeight(38);
        return btn;
    }

    private Node createBtn(String text, String color, Runnable action) {
        Button btn = createBtn(text, color);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }
}
