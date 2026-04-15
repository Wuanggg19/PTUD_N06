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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import connectDB.ConnectDB;
import dao.HoaDonDAO;
import dao.ChiTietHoaDonDAO;
import dao.ChiTietPhieuDatDAO;
import entity.HoaDon;
import entity.ChiTietHoaDon;
import entity.ChiTietPhieuDat;

import java.io.File;
import java.io.PrintWriter;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class InvoiceView {

    private static final String BG_LIGHT  = "#f6f6f8";
    private static final String BTN_INFO  = "#3498db";
    private static final String BTN_PRINT = "#8e44ad";

    private TableView<HoaDon> tableView;
    private ObservableList<HoaDon> dataList = FXCollections.observableArrayList();
    private TextField txtSearch;
    private DatePicker dpFrom, dpTo;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("QUẢN LÝ HÓA ĐƠN");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        // --- Bộ lọc theo ngày ---
        HBox filterBar = new HBox(12);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12 16;");

        Label lblFrom = new Label("Từ ngày:");
        lblFrom.setFont(Font.font("Segoe UI", 13));
        dpFrom = new DatePicker();
        dpFrom.setPromptText("Từ ngày");

        Label lblTo = new Label("Đến ngày:");
        lblTo.setFont(Font.font("Segoe UI", 13));
        dpTo = new DatePicker();
        dpTo.setPromptText("Đến ngày");

        Button btnFilterDate = createButton("🔍  Lọc theo ngày", BTN_INFO);
        btnFilterDate.setOnAction(e -> handleFilterDate());

        txtSearch = new TextField();
        txtSearch.setPromptText("Tìm theo mã HĐ / tên khách...");
        txtSearch.setPrefWidth(220);
        txtSearch.setPrefHeight(34);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7;");
        
        // 2. TÌM KIẾM THEO GÕ PHÍM (Realtime search)
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
            searchInvoice();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnRefresh = createButton("🔄  Tất cả", "#7f8c8d");
        btnRefresh.setOnAction(e -> {
            txtSearch.clear();
            dpFrom.setValue(null);
            dpTo.setValue(null);
            loadData();
        });

        filterBar.getChildren().addAll(lblFrom, dpFrom, lblTo, dpTo, btnFilterDate,
                spacer, new Label("  Tìm:"), txtSearch, btnRefresh);

        // --- Bảng hóa đơn ---
        tableView = new TableView<>(dataList);
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tableView, Priority.ALWAYS);
        tableView.setPlaceholder(new Label("Chưa có hóa đơn nào."));
        
        // Xử lý Double Click JTable (TableView)
        tableView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tableView.getSelectionModel().getSelectedItem() != null) {
                handleViewDetail();
            }
        });

        TableColumn<HoaDon, String> colMa = new TableColumn<>("Mã Hóa Đơn");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaHoaDon()));

        TableColumn<HoaDon, String> colPhieu = new TableColumn<>("Mã Phiếu Đặt");
        colPhieu.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getPhieuDatPhong() != null ? d.getValue().getPhieuDatPhong().getMaDatPhong() : ""));

        TableColumn<HoaDon, String> colKhach = new TableColumn<>("Khách Hàng");
        colKhach.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                (d.getValue().getPhieuDatPhong() != null && d.getValue().getPhieuDatPhong().getKhachHang() != null) ? 
                d.getValue().getPhieuDatPhong().getKhachHang().getTenKhachHang() : ""));

        TableColumn<HoaDon, String> colNV = new TableColumn<>("Nhân Viên Lập");
        colNV.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getNhanVien() != null ? d.getValue().getNhanVien().getMaNhanVien() : ""));

        TableColumn<HoaDon, String> colNgay = new TableColumn<>("Ngày Lập");
        colNgay.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getNgayLap() != null ? d.getValue().getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));

        TableColumn<HoaDon, String> colTienPhong = new TableColumn<>("Tiền Phòng");
        colTienPhong.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f VNĐ", d.getValue().getTongTienPhong())));

        TableColumn<HoaDon, String> colTienDV = new TableColumn<>("Tiền Dịch Vụ");
        colTienDV.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f VNĐ", d.getValue().getTongTienDichVu())));

        TableColumn<HoaDon, String> colTong = new TableColumn<>("Tổng Tiền");
        colTong.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f VNĐ", d.getValue().getTongTienPhong() + d.getValue().getTongTienDichVu())));

        tableView.getColumns().addAll(colMa, colKhach, colPhieu, colNV, colNgay, colTienPhong, colTienDV, colTong);

        // --- Nút hành động ---
        HBox actionBar = new HBox(12);
        actionBar.setAlignment(Pos.CENTER_RIGHT);
        Button btnDetail = createButton("📄  Xem chi tiết", BTN_INFO);
        btnDetail.setOnAction(e -> handleViewDetail());
        Button btnPrint = createButton("🖨  In hóa đơn", BTN_PRINT);
        btnPrint.setOnAction(e -> handlePrint());
        Button btnExport = createButton("📊  Xuất Excel", "#16a085");
        btnExport.setOnAction(e -> exportExcel());
        actionBar.getChildren().addAll(btnDetail, btnPrint, btnExport);

        root.getChildren().addAll(lblTitle, filterBar, tableView, actionBar);
        return root;
    }

    public void loadData() {
        try {
            if (ConnectDB.getConnection() == null) return;
            HoaDonDAO dao = new HoaDonDAO();
            List<HoaDon> list = dao.getAllHoaDon();
            dataList.setAll(list != null ? list : new ArrayList<>());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu hóa đơn: " + e.getMessage());
        }
    }

    private void searchInvoice() {
        String kw = txtSearch.getText();
        HoaDonDAO dao = new HoaDonDAO();
        LocalDate from = dpFrom.getValue();
        LocalDate to = dpTo.getValue();
        
        List<HoaDon> list = dao.filterHoaDon(kw, from, to);
        dataList.setAll(list != null ? list : new ArrayList<>());
    }

    private void handleFilterDate() {
        if (dpFrom.getValue() == null || dpTo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn cả ngày bắt đầu và kết thúc.");
            return;
        }
        searchInvoice(); // Dùng chung query filter có Date
    }

    // 1. XEM CHI TIẾT -> POPUP
    private void handleViewDetail() {
        HoaDon selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một hóa đơn để xem chi tiết.");
            return;
        }
        openInvoiceDetailDialog(selected);
    }

    private void openInvoiceDetailDialog(HoaDon hd) {
        Stage detailStage = new Stage();
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setTitle("Chi tiết hóa đơn - " + hd.getMaHoaDon());

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white;");

        Label lblTitle = new Label("CHI TIẾT HÓA ĐƠN");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        // Thông tin chung
        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(10);
        infoGrid.setHgap(20);
        infoGrid.add(new Label("Mã HĐ:"), 0, 0);
        infoGrid.add(new Label(hd.getMaHoaDon()), 1, 0);
        infoGrid.add(new Label("Khách hàng:"), 0, 1);
        String tenKh = (hd.getPhieuDatPhong() != null && hd.getPhieuDatPhong().getKhachHang() != null) ? 
                       hd.getPhieuDatPhong().getKhachHang().getTenKhachHang() : "N/A";
        infoGrid.add(new Label(tenKh), 1, 1);
        infoGrid.add(new Label("Ngày lập:"), 0, 2);
        infoGrid.add(new Label(hd.getNgayLap() != null ? hd.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""), 1, 2);

        // Bảng chi tiết phòng/dịch vụ
        TableView<Object> tbDetails = new TableView<>();
        tbDetails.setPrefHeight(250);
        TableColumn<Object, String> colName = new TableColumn<>("Mục Đã Dùng");
        colName.setCellValueFactory(d -> {
            if (d.getValue() instanceof ChiTietPhieuDat) {
                return new javafx.beans.property.SimpleStringProperty("Phòng " + ((ChiTietPhieuDat)d.getValue()).getPhong().getMaPhong());
            } else if (d.getValue() instanceof ChiTietHoaDon) {
                return new javafx.beans.property.SimpleStringProperty("Dịch vụ " + ((ChiTietHoaDon)d.getValue()).getDichVu().getMaDichVu());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        TableColumn<Object, String> colPrice = new TableColumn<>("Đơn Giá");
        colPrice.setCellValueFactory(d -> {
            if (d.getValue() instanceof ChiTietPhieuDat) {
                return new javafx.beans.property.SimpleStringProperty(String.format("%,.0f", ((ChiTietPhieuDat)d.getValue()).getGiaThuePhong()));
            } else if (d.getValue() instanceof ChiTietHoaDon) {
                return new javafx.beans.property.SimpleStringProperty(String.format("%,.0f", ((ChiTietHoaDon)d.getValue()).getDonGiaLuuTru()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        TableColumn<Object, String> colQty = new TableColumn<>("Số Lượng");
        colQty.setCellValueFactory(d -> {
            if (d.getValue() instanceof ChiTietPhieuDat) {
                return new javafx.beans.property.SimpleStringProperty("1 (Phòng)"); // Mocking the display string
            } else if (d.getValue() instanceof ChiTietHoaDon) {
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(((ChiTietHoaDon)d.getValue()).getSoLuong()));
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });

        tbDetails.getColumns().addAll(colName, colQty, colPrice);
        tbDetails.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // Load data in
        ChiTietPhieuDatDAO d1 = new ChiTietPhieuDatDAO();
        ChiTietHoaDonDAO d2 = new ChiTietHoaDonDAO();
        if (hd.getPhieuDatPhong() != null) {
            tbDetails.getItems().addAll(d1.getDSChiTietByMaPhieu(hd.getPhieuDatPhong().getMaDatPhong()));
        }
        tbDetails.getItems().addAll(d2.getDSChiTietByMaHD(hd.getMaHoaDon()));

        Label lblTong = new Label(String.format("Tổng Tiền: %,.0f VNĐ", hd.getTongTienPhong() + hd.getTongTienDichVu()));
        lblTong.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lblTong.setTextFill(Color.web("#e74c3c"));

        Button btnClose = new Button("Đóng");
        btnClose.setOnAction(e -> detailStage.close());

        root.getChildren().addAll(lblTitle, infoGrid, tbDetails, lblTong, btnClose);
        Scene scene = new Scene(root, 550, 500);
        detailStage.setScene(scene);
        detailStage.show();
    }

    // 4. IN HÓA ĐƠN
    private void handlePrint() {
        HoaDon selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một hóa đơn để in.");
            return;
        }
        printInvoice(selected);
    }

    private void printInvoice(HoaDon hd) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu hóa đơn");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Text Files", "*.txt"));
        fileChooser.setInitialFileName("invoice_" + hd.getMaHoaDon() + ".txt");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
                writer.println("==========================================");
                writer.println("              HÓA ĐƠN               ");
                writer.println("==========================================");
                writer.println("Mã Hóa Đơn : " + hd.getMaHoaDon());
                String tenKh = (hd.getPhieuDatPhong() != null && hd.getPhieuDatPhong().getKhachHang() != null) ? 
                       hd.getPhieuDatPhong().getKhachHang().getTenKhachHang() : "N/A";
                writer.println("Khách Hàng : " + tenKh);
                writer.println("Ngày Lập   : " + (hd.getNgayLap() != null ? hd.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""));
                writer.println("------------------------------------------");
                writer.println("Danh sách dịch vụ / phòng:");
                
                ChiTietPhieuDatDAO d1 = new ChiTietPhieuDatDAO();
                ChiTietHoaDonDAO d2 = new ChiTietHoaDonDAO();
                if (hd.getPhieuDatPhong() != null) {
                    List<ChiTietPhieuDat> phongList = d1.getDSChiTietByMaPhieu(hd.getPhieuDatPhong().getMaDatPhong());
                    for (ChiTietPhieuDat p : phongList) {
                         writer.println(" - Phòng " + p.getPhong().getMaPhong() + " | Đơn giá: " + String.format("%,.0f", p.getGiaThuePhong()));
                    }
                }
                List<ChiTietHoaDon> dvList = d2.getDSChiTietByMaHD(hd.getMaHoaDon());
                for (ChiTietHoaDon dv : dvList) {
                     writer.println(" - Dịch vụ " + dv.getDichVu().getMaDichVu() + " x" + dv.getSoLuong() + " | Thành tiền: " + String.format("%,.0f", dv.getDonGiaLuuTru()));
                }
                
                writer.println("------------------------------------------");
                writer.println("Tổng Tiền Phòng : " + String.format("%,.0f VNĐ", hd.getTongTienPhong()));
                writer.println("Tổng Tiền DV    : " + String.format("%,.0f VNĐ", hd.getTongTienDichVu()));
                writer.println("==========================================");
                writer.println("TỔNG CỘNG  : " + String.format("%,.0f VNĐ", hd.getTongTienPhong() + hd.getTongTienDichVu()));
                writer.println("==========================================");
                
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã in hóa đơn thành công ra file txt!");
            } catch (Exception ex) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi in hóa đơn: " + ex.getMessage());
            }
        }
    }

    // 5. XUẤT EXCEL
    private void exportExcel() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Xuất Excel");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName("invoice.xlsx");
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Danh_Sach_Hoa_Don");
                
                // Header row
                Row header = sheet.createRow(0);
                String[] columns = {"Mã HĐ", "Mã Phiếu", "Khách Hàng", "Nhân Viên", "Ngày Lập", "Tiền Phòng", "Tiền Dịch Vụ", "Tổng Tiền"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = header.createCell(i);
                    cell.setCellValue(columns[i]);
                }

                // Data rows
                int rowNum = 1;
                for (HoaDon hd : dataList) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(hd.getMaHoaDon());
                    row.createCell(1).setCellValue(hd.getPhieuDatPhong() != null ? hd.getPhieuDatPhong().getMaDatPhong() : "");
                    
                    String tenKh = (hd.getPhieuDatPhong() != null && hd.getPhieuDatPhong().getKhachHang() != null) ? 
                       hd.getPhieuDatPhong().getKhachHang().getTenKhachHang() : "";
                    row.createCell(2).setCellValue(tenKh);
                    
                    row.createCell(3).setCellValue(hd.getNhanVien() != null ? hd.getNhanVien().getMaNhanVien() : "");
                    row.createCell(4).setCellValue(hd.getNgayLap() != null ? hd.getNgayLap().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "");
                    row.createCell(5).setCellValue(hd.getTongTienPhong());
                    row.createCell(6).setCellValue(hd.getTongTienDichVu());
                    row.createCell(7).setCellValue(hd.getTongTienPhong() + hd.getTongTienDichVu());
                }

                try (FileOutputStream out = new FileOutputStream(file)) {
                    workbook.write(out);
                }
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xuất file Excel thành công!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xuất Excel. Đảm bảo thư viện Apache POI (poi, poi-ooxml) đã được thêm vào project.\nLỗi: " + e.getMessage());
            }
        }
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
