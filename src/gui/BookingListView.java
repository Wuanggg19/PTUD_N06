package gui;

import dao.ChiTietPhieuDatDAO;
import dao.PhieuDatPhongDAO;
import entity.ChiTietPhieuDat;
import entity.PhieuDatPhong;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import util.AppTheme;
import util.BookingStatus;
import util.StatusUtils;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BookingListView {
    private final TableView<PhieuDatPhong> tableView = new TableView<>();
    private final ObservableList<PhieuDatPhong> dataList = FXCollections.observableArrayList();
    private final PhieuDatPhongDAO phieuDatPhongDAO = new PhieuDatPhongDAO();
    private final ChiTietPhieuDatDAO chiTietPhieuDatDAO = new ChiTietPhieuDatDAO();

    private TextField txtSearch;
    private ComboBox<String> cboFilter;
    private List<PhieuDatPhong> allBookings = new ArrayList<>();

    public Node createView() {
        VBox root = new VBox(18);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + AppTheme.BG_LIGHT + ";");

        Label lblTitle = new Label("DANH SÁCH ĐẶT PHÒNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web(AppTheme.TEXT));

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã phiếu, khách hàng hoặc số điện thoại...");
        txtSearch.textProperty().addListener((obs, oldValue, newValue) -> applyFilters());
        txtSearch.setPrefHeight(38);

        cboFilter = new ComboBox<>(FXCollections.observableArrayList(
                "Tất cả",
                BookingStatus.CHO_XAC_NHAN.getDisplayName(),
                BookingStatus.DA_NHAN_PHONG.getDisplayName(),
                BookingStatus.DA_THANH_TOAN.getDisplayName(),
                BookingStatus.DA_HUY.getDisplayName()
        ));
        cboFilter.setValue("Tất cả");
        cboFilter.setPrefHeight(38);
        cboFilter.setOnAction(e -> applyFilters());

        Button btnRefresh = createButton("Làm mới", AppTheme.PRIMARY);
        btnRefresh.setOnAction(e -> loadData());

        HBox toolbar = new HBox(12, txtSearch, cboFilter, spacer(), btnRefresh);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(txtSearch, Priority.ALWAYS);

        configureTable();
        tableView.setItems(dataList);

        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        Button btnDetail = createButton("Xem chi tiết", AppTheme.INFO);
        Button btnCancel = createButton("Hủy phiếu", AppTheme.DANGER);
        btnDetail.setOnAction(e -> handleViewDetail());
        btnCancel.setOnAction(e -> handleCancel());
        actionBar.getChildren().addAll(btnDetail, btnCancel);

        root.getChildren().addAll(lblTitle, toolbar, tableView, actionBar);
        loadData();
        return root;
    }

    public void loadData() {
        Task<List<PhieuDatPhong>> task = new Task<>() {
            @Override
            protected List<PhieuDatPhong> call() {
                return phieuDatPhongDAO.getAllPhieuDatPhong();
            }
        };

        task.setOnSucceeded(e -> {
            allBookings = task.getValue() != null ? task.getValue() : new ArrayList<>();
            applyFilters();
        });
        task.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách phiếu đặt phòng."));
        new Thread(task).start();
    }

    private void configureTable() {
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("Không có dữ liệu đặt phòng."));

        TableColumn<PhieuDatPhong, String> colMa = new TableColumn<>("Mã phiếu");
        colMa.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMaDatPhong()));

        TableColumn<PhieuDatPhong, String> colKhach = new TableColumn<>("Khách hàng");
        colKhach.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getKhachHang() != null ? nullSafe(data.getValue().getKhachHang().getTenKhachHang()) : "Khách lẻ"));

        TableColumn<PhieuDatPhong, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getKhachHang() != null ? nullSafe(data.getValue().getKhachHang().getSoDienThoai()) : ""));

        TableColumn<PhieuDatPhong, String> colNgayDat = new TableColumn<>("Ngày đặt");
        colNgayDat.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNgayDat() != null ? data.getValue().getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));

        TableColumn<PhieuDatPhong, String> colPhong = new TableColumn<>("Danh sách phòng");
        colPhong.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(nullSafe(data.getValue().getDsMaPhong())));

        TableColumn<PhieuDatPhong, String> colSoLuong = new TableColumn<>("SL phòng");
        colSoLuong.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(String.valueOf(data.getValue().getSoLuongPhong())));

        TableColumn<PhieuDatPhong, String> colTrangThai = new TableColumn<>("Trạng thái");
        colTrangThai.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(
                StatusUtils.bookingLabel(data.getValue().getTrangThai())));
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
                    case "Đã nhận phòng" -> AppTheme.INFO;
                    case "Đã thanh toán" -> AppTheme.SUCCESS;
                    case "Đã hủy" -> AppTheme.DANGER;
                    default -> AppTheme.WARNING;
                } + ";");
            }
        });

        tableView.getColumns().setAll(colMa, colKhach, colSdt, colNgayDat, colPhong, colSoLuong, colTrangThai);
        tableView.setRowFactory(tv -> {
            TableRow<PhieuDatPhong> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleViewDetail();
                }
            });
            return row;
        });
    }

    private void applyFilters() {
        String keyword = nullSafe(txtSearch != null ? txtSearch.getText() : "").trim().toLowerCase(Locale.ROOT);
        String selectedLabel = cboFilter != null ? cboFilter.getValue() : "Tất cả";

        List<PhieuDatPhong> filtered = allBookings.stream()
                .filter(p -> {
                    String ma = nullSafe(p.getMaDatPhong()).toLowerCase(Locale.ROOT);
                    String ten = p.getKhachHang() != null ? nullSafe(p.getKhachHang().getTenKhachHang()).toLowerCase(Locale.ROOT) : "";
                    String sdt = p.getKhachHang() != null ? nullSafe(p.getKhachHang().getSoDienThoai()) : "";
                    boolean matchKeyword = keyword.isBlank() || ma.contains(keyword) || ten.contains(keyword) || sdt.contains(keyword);

                    boolean matchStatus = "Tất cả".equals(selectedLabel)
                            || StatusUtils.bookingLabel(p.getTrangThai()).equals(selectedLabel);
                    return matchKeyword && matchStatus;
                })
                .collect(Collectors.toList());
        dataList.setAll(filtered);
    }

    private void handleViewDetail() {
        PhieuDatPhong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một phiếu đặt phòng.");
            return;
        }

        Task<List<ChiTietPhieuDat>> task = new Task<>() {
            @Override
            protected List<ChiTietPhieuDat> call() {
                return chiTietPhieuDatDAO.getDSChiTietByMaPhieu(selected.getMaDatPhong());
            }
        };

        task.setOnSucceeded(e -> {
            List<ChiTietPhieuDat> details = task.getValue() != null ? task.getValue() : List.of();
            StringBuilder sb = new StringBuilder();
            sb.append("Mã phiếu: ").append(selected.getMaDatPhong()).append('\n');
            sb.append("Khách hàng: ").append(selected.getKhachHang() != null ? nullSafe(selected.getKhachHang().getTenKhachHang()) : "Khách lẻ").append('\n');
            sb.append("Trạng thái: ").append(StatusUtils.bookingLabel(selected.getTrangThai())).append('\n');
            sb.append("Số lượng phòng: ").append(selected.getSoLuongPhong()).append('\n');
            sb.append("Danh sách phòng: ").append(nullSafe(selected.getDsMaPhong())).append("\n\n");

            if (details.isEmpty()) {
                sb.append("Không có chi tiết phòng.");
            } else {
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                for (ChiTietPhieuDat ct : details) {
                    sb.append("- ").append(ct.getPhong() != null ? ct.getPhong().getMaPhong() : "").append(" | ");
                    sb.append(ct.getNgayNhan() != null ? ct.getNgayNhan().format(dtf) : "--").append(" -> ");
                    sb.append(ct.getNgayTra() != null ? ct.getNgayTra().format(dtf) : "--").append(" | ");
                    sb.append(String.format("%,.0f VNĐ", ct.getGiaThuePhong())).append('\n');
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Chi tiết phiếu đặt");
            alert.setHeaderText("Phiếu " + selected.getMaDatPhong());
            TextArea textArea = new TextArea(sb.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefRowCount(12);
            DialogPane pane = alert.getDialogPane();
            pane.setContent(textArea);
            alert.showAndWait();
        });
        task.setOnFailed(e -> showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải chi tiết phiếu đặt."));
        new Thread(task).start();
    }

    private void handleCancel() {
        PhieuDatPhong selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn phiếu cần hủy.");
            return;
        }
        if (StatusUtils.isBookingStatus(selected.getTrangThai(), BookingStatus.DA_HUY)
                || StatusUtils.isBookingStatus(selected.getTrangThai(), BookingStatus.DA_THANH_TOAN)) {
            showAlert(Alert.AlertType.WARNING, "Không thể hủy", "Phiếu đã thanh toán hoặc đã hủy.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận hủy phiếu " + selected.getMaDatPhong() + "?",
                ButtonType.YES, ButtonType.NO);
        confirm.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return phieuDatPhongDAO.huyPhieu(selected.getMaDatPhong());
                    }
                };
                task.setOnSucceeded(e -> {
                    if (Boolean.TRUE.equals(task.getValue())) {
                        loadData();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy phiếu.");
                    }
                });
                new Thread(task).start();
            }
        });
    }

    private Button createButton(String text, String color) {
        Button btn = new Button(text);
        btn.setTextFill(Color.WHITE);
        btn.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btn.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setPrefHeight(38);
        return btn;
    }

    private Region spacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
