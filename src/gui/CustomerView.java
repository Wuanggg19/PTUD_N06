package gui;

import entity.KhachHang;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class CustomerView {
    private final KhachHangService service = new KhachHangService();
    private final TableView<KhachHang> tableView = new TableView<>();

    private List<KhachHang> allData = new ArrayList<>();
    private TextField txtSearch;
    private ComboBox<String> cboSearchType;
    private Consumer<KhachHang> onBookingRequest;

    public Node createView() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + AppTheme.BG_LIGHT + ";");

        Label lblTitle = new Label("QUẢN LÝ KHÁCH HÀNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web(AppTheme.TEXT));

        Button btnThem = createBtn("Thêm mới", AppTheme.INFO);
        btnThem.setOnAction(e -> openCreateDialog());

        Button btnDatPhong = createBtn("Đặt phòng", AppTheme.PRIMARY);
        btnDatPhong.setOnAction(e -> {
            KhachHang selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                new Alert(Alert.AlertType.WARNING, "Vui lòng chọn khách hàng.").showAndWait();
                return;
            }
            if (onBookingRequest != null) {
                onBookingRequest.accept(selected);
            }
        });

        HBox header = new HBox(10, lblTitle, spacer(), btnDatPhong, btnThem);
        header.setAlignment(Pos.CENTER_LEFT);

        cboSearchType = new ComboBox<>();
        cboSearchType.getItems().addAll("Theo mã khách hàng", "Theo số điện thoại");
        cboSearchType.setValue("Theo mã khách hàng");
        cboSearchType.setPrefHeight(38);
        cboSearchType.setOnAction(e -> filterData());

        txtSearch = new TextField();
        txtSearch.setPromptText("Nhập từ khóa...");
        txtSearch.setPrefHeight(38);
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> filterData());

        HBox searchBar = new HBox(10, cboSearchType, txtSearch);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        configureTable();
        root.getChildren().addAll(header, searchBar, tableView);
        loadData();
        return root;
    }

    public void setOnBookingRequest(Consumer<KhachHang> callback) {
        this.onBookingRequest = callback;
    }

    public void loadData() {
        Task<List<KhachHang>> task = new Task<>() {
            @Override
            protected List<KhachHang> call() {
                return service.getAll();
            }
        };
        task.setOnSucceeded(e -> {
            allData = task.getValue() != null ? task.getValue() : new ArrayList<>();
            filterData();
        });
        new Thread(task).start();
    }

    private void configureTable() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setRowFactory(tv -> {
            TableRow<KhachHang> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    openEditDialog(row.getItem());
                }
            });
            return row;
        });

        TableColumn<KhachHang, String> colMa = new TableColumn<>("Mã KH");
        colMa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaKhachHang()));
        TableColumn<KhachHang, String> colTen = new TableColumn<>("Tên khách hàng");
        colTen.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenKhachHang()));
        TableColumn<KhachHang, String> colGioiTinh = new TableColumn<>("Giới tính");
        colGioiTinh.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isGioiTinh() ? "Nam" : "Nữ"));
        TableColumn<KhachHang, String> colDiaChi = new TableColumn<>("Địa chỉ");
        colDiaChi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiaChi()));
        TableColumn<KhachHang, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSoDienThoai()));
        tableView.getColumns().setAll(colMa, colTen, colGioiTinh, colDiaChi, colSdt);
    }

    private void filterData() {
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";
        if (keyword.isBlank()) {
            tableView.getItems().setAll(allData);
            return;
        }

        if ("Theo số điện thoại".equals(cboSearchType.getValue())) {
            tableView.getItems().setAll(service.searchByPhone(keyword));
        } else {
            tableView.getItems().setAll(service.searchByMa(keyword));
        }
    }

    private void openCreateDialog() {
        new CustomerDialog(null, service, this::loadData).show();
    }

    private void openEditDialog(KhachHang kh) {
        new CustomerDialog(kh, service, this::loadData).show();
    }

    private Button createBtn(String text, String color) {
        Button btn = new Button(text);
        btn.setTextFill(Color.WHITE);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8;");
        btn.setPrefHeight(38);
        return btn;
    }

    private Region spacer() {
        Region region = new Region();
        HBox.setHgrow(region, Priority.ALWAYS);
        return region;
    }
}
