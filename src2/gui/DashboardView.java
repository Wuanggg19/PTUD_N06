package gui;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import connectDB.ConnectDB;
import dao.PhongDAO;
import dao.HoaDonDAO;
import dao.PhieuDatPhongDAO;
import entity.Phong;
import entity.HoaDon;
import entity.PhieuDatPhong;

public class DashboardView {

    private final String BG_LIGHT = "#f6f6f8";
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    
    private Label valTotal, valOcc, valVac, valRev, lblTitle;
    private TableView<PhieuDatPhong> tvRecent;
    private TableView<Phong> tvVacant;

    public javafx.scene.Node createView() {
        System.out.println("DEBUG: Creating DashboardView UI...");
        javafx.scene.layout.VBox root = new javafx.scene.layout.VBox(30);
        root.setPadding(new javafx.geometry.Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // Header Dashboard
        javafx.scene.layout.HBox header = new javafx.scene.layout.HBox();
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        lblTitle = new javafx.scene.control.Label("TỔNG QUAN HỆ THỐNG");
        lblTitle.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 28));
        lblTitle.setTextFill(javafx.scene.paint.Color.web("#2c3e50"));
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button btnRefresh = new Button("Làm mới dữ liệu");
        btnRefresh.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 15 8 15;");
        btnRefresh.setOnAction(e -> refreshData());
        
        header.getChildren().addAll(lblTitle, spacer, btnRefresh);

        valTotal = new Label("0");
        valOcc = new Label("0");
        valVac = new Label("0");
        valRev = new Label("0 VNĐ");

        GridPane grid = new GridPane();
        grid.setHgap(25); grid.setVgap(16);
        for (int i = 0; i < 4; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setHgrow(Priority.ALWAYS);
            cc.setPercentWidth(25);
            grid.getColumnConstraints().add(cc);
        }

        grid.add(createStatCardFx("TỔNG SỐ PHÒNG", valTotal, "Phòng khách sạn", "#3498db"), 0, 0);
        grid.add(createStatCardFx("ĐANG CÓ KHÁCH", valOcc, "Phòng đang ở", "#e74c3c"), 1, 0);
        grid.add(createStatCardFx("PHÒNG TRỐNG", valVac, "Sẵn sàng đón khách", "#2ecc71"), 2, 0);
        grid.add(createStatCardFx("DOANH THU HÔM NAY", valRev, "Tổng tiền thu về", "#9b59b6"), 3, 0);

        HBox tablesBox = new HBox(25);
        VBox.setVgrow(tablesBox, Priority.ALWAYS);

        VBox pnlRecent = createTableContainer("PHÒNG VỪA ĐẶT (GẦN ĐÂY)");
        tvRecent = new TableView<>();
        tvRecent.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupRecentTable();
        pnlRecent.getChildren().add(tvRecent);
        HBox.setHgrow(pnlRecent, Priority.ALWAYS);

        VBox pnlVacant = createTableContainer("DANH SÁCH PHÒNG ĐANG TRỐNG");
        tvVacant = new TableView<>();
        tvVacant.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        setupVacantTable();
        pnlVacant.getChildren().add(tvVacant);
        HBox.setHgrow(pnlVacant, Priority.ALWAYS);

        tablesBox.getChildren().addAll(pnlRecent, pnlVacant);
        root.getChildren().addAll(header, grid, tablesBox);

        return root;
    }

    private void setupRecentTable() {
        TableColumn<PhieuDatPhong, String> colMaDP = new TableColumn<>("Mã Phiếu");
        colMaDP.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMaDatPhong()));
        TableColumn<PhieuDatPhong, String> colKhach = new TableColumn<>("Khách Hàng");
        colKhach.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getKhachHang().getTenKhachHang()));
        TableColumn<PhieuDatPhong, String> colNgay = new TableColumn<>("Ngày Đặt");
        colNgay.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getNgayDat().toString()));
        tvRecent.getColumns().addAll(colMaDP, colKhach, colNgay);
    }

    private void setupVacantTable() {
        TableColumn<Phong, String> colMaP = new TableColumn<>("Mã Phòng");
        colMaP.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getMaPhong()));
        TableColumn<Phong, String> colLoai = new TableColumn<>("Loại Phòng");
        colLoai.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLoaiPhong()));
        TableColumn<Phong, String> colGia = new TableColumn<>("Giá Phòng");
        colGia.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(df.format(data.getValue().getGiaPhong())));
        tvVacant.getColumns().addAll(colMaP, colLoai, colGia);
    }

    public void refreshData() {
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    PhongDAO phongDAO = new PhongDAO();
                    HoaDonDAO hoaDonDAO = new HoaDonDAO();
                    PhieuDatPhongDAO pdpDAO = new PhieuDatPhongDAO();
    
                    List<Phong> dsP = phongDAO.getAllPhong();
                    List<HoaDon> dsHD = hoaDonDAO.getAllHoaDon();
                    List<PhieuDatPhong> recent = pdpDAO.getAllPhieuDatPhong();
                    
                    List<PhieuDatPhong> recentSorted = (recent != null) ? recent.stream()
                        .sorted((p1, p2) -> p2.getNgayDat().compareTo(p1.getNgayDat()))
                        .limit(10).collect(Collectors.toList()) : new ArrayList<>();
    
                    long occupiedCount = dsP.stream().filter(p -> "Đang ở".equals(p.getTrangThai()) || "DaNhanPhong".equals(p.getTrangThai())).count();
                    long vacantCount = dsP.stream().filter(p -> "Trống".equals(p.getTrangThai())).count();
                    double totalRevenue = dsHD.stream()
                        .filter(hd -> hd.getNgayLap().toLocalDate().equals(LocalDate.now()))
                        .mapToDouble(hd -> hd.getTongTienPhong() + hd.getTongTienDichVu()).sum();
    
                    List<Phong> vacantRooms = dsP.stream()
                        .filter(p -> "Trống".equals(p.getTrangThai()))
                        .limit(10).collect(Collectors.toList());
    
                    javafx.application.Platform.runLater(() -> {
                        valTotal.setText(String.valueOf(dsP.size()));
                        valOcc.setText(String.valueOf(occupiedCount));
                        valVac.setText(String.valueOf(vacantCount));
                        valRev.setText(df.format(totalRevenue));
                        tvRecent.getItems().setAll(recentSorted);
                        tvVacant.getItems().setAll(vacantRooms);
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        new Thread(task).start();
    }

    private VBox createTableContainer(String title) {
        VBox vb = new VBox(10);
        Label lbl = new Label(title);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        lbl.setTextFill(Color.web("#34495e"));
        vb.getChildren().add(lbl);
        return vb;
    }

    private VBox createStatCardFx(String title, Label valueLabel, String subtitle, String accentHex) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        Label lbTitle = new Label(title);
        lbTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        lbTitle.setTextFill(Color.web("#7f8c8d"));
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        valueLabel.setTextFill(Color.web("#2c3e50"));
        Label lbSub = new Label(subtitle);
        lbSub.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
        lbSub.setTextFill(Color.web("#bdc3c7"));
        HBox accent = new HBox();
        accent.setPrefWidth(5);
        accent.setStyle("-fx-background-color: " + accentHex + "; -fx-background-radius: 5;");
        HBox content = new HBox(15, accent, new VBox(5, lbTitle, valueLabel, lbSub));
        card.getChildren().add(content);
        return card;
    }
}
