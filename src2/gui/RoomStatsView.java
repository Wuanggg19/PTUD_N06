package gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.collections.FXCollections;

import connectDB.ConnectDB;
import dao.PhongDAO;
import entity.Phong;

import java.util.List;

/**
 * ================================================================
 *  THỐNG KÊ PHÒNG VIEW
 *  Chức năng: Thống kê tình trạng phòng (trống/có khách/bảo trì),
 *             tỷ lệ lấp đầy, phòng được đặt nhiều nhất.
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()              - Tải dữ liệu phòng từ DB
 *   2. updatePieChart()        - Cập nhật biểu đồ tròn tỷ lệ các trạng thái phòng
 *   3. updateOccupancyRate()   - Tính và hiển thị tỷ lệ lấp đầy (%)
 *   4. updateTopRoomsTable()   - Hiển thị top phòng được đặt nhiều nhất
 *   5. handleFilterPeriod()    - Lọc thống kê theo khoảng thời gian
 * ================================================================
 */
public class RoomStatsView {

    private static final String BG_LIGHT = "#f6f6f8";

    private Label lblTotalRooms, lblOccupied, lblVacant, lblMaintenance, lblOccupancyRate;
    private PieChart pieChart;
    private TableView<Phong> tableTopRooms;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("THỐNG KÊ PHÒNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        // --- Cards tóm tắt ---
        lblTotalRooms    = new Label("—");
        lblOccupied      = new Label("—");
        lblVacant        = new Label("—");
        lblMaintenance   = new Label("—");
        lblOccupancyRate = new Label("—%");
        lblOccupancyRate.setTextFill(Color.web("#e74c3c"));

        HBox summaryCards = new HBox(16);
        summaryCards.getChildren().addAll(
                createSummaryCard("TỔNG SỐ PHÒNG",   lblTotalRooms,    "#3498db"),
                createSummaryCard("ĐANG CÓ KHÁCH",   lblOccupied,      "#e74c3c"),
                createSummaryCard("PHÒNG TRỐNG",      lblVacant,        "#27ae60"),
                createSummaryCard("BẢO TRÌ",          lblMaintenance,   "#f39c12"),
                createSummaryCard("TỶ LỆ LẤP ĐẦY",   lblOccupancyRate, "#9b59b6")
        );
        for (Node card : summaryCards.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        // --- Layout biểu đồ + bảng ---
        HBox charts = new HBox(20);
        VBox.setVgrow(charts, Priority.ALWAYS);

        // Biểu đồ tròn trạng thái phòng
        VBox pieBox = new VBox(10);
        pieBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16;");
        HBox.setHgrow(pieBox, Priority.ALWAYS);
        Label lblPieTitle = new Label("TỶ LỆ TRẠNG THÁI PHÒNG");
        lblPieTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        pieChart = new PieChart(FXCollections.observableArrayList(
                new PieChart.Data("Đang ở (mẫu)",   30),
                new PieChart.Data("Trống (mẫu)",     55),
                new PieChart.Data("Bảo trì (mẫu)",  10),
                new PieChart.Data("Đã đặt (mẫu)",    5)
        ));
        pieChart.setTitle("Trạng thái phòng");
        pieChart.setLegendVisible(true);
        VBox.setVgrow(pieChart, Priority.ALWAYS);
        pieBox.getChildren().addAll(lblPieTitle, pieChart);

        // Bảng top phòng được đặt nhiều
        VBox tableBox = new VBox(10);
        tableBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16;");
        tableBox.setPrefWidth(350);
        Label lblTableTitle = new Label("PHÒNG ĐƯỢC ĐẶT NHIỀU NHẤT");
        lblTableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        tableTopRooms = new TableView<>();
        tableTopRooms.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableTopRooms.setPlaceholder(new Label("Chưa có dữ liệu."));
        VBox.setVgrow(tableTopRooms, Priority.ALWAYS);

        TableColumn<Phong, String> colMa   = new TableColumn<>("Mã Phòng");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaPhong()));
        TableColumn<Phong, String> colLoai = new TableColumn<>("Loại Phòng");
        colLoai.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getLoaiPhong()));
        TableColumn<Phong, String> colTT   = new TableColumn<>("Trạng Thái");
        colTT.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTrangThai()));
        tableTopRooms.getColumns().addAll(colMa, colLoai, colTT);

        Button btnRefresh = new Button("🔄  Làm mới");
        btnRefresh.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        btnRefresh.setOnAction(e -> loadData());

        tableBox.getChildren().addAll(lblTableTitle, tableTopRooms, btnRefresh);
        charts.getChildren().addAll(pieBox, tableBox);

        root.getChildren().addAll(lblTitle, summaryCards, charts);
        return root;
    }

    /**
     * TODO: Tải dữ liệu phòng từ DB và cập nhật biểu đồ + bảng.
     *       Gợi ý: Dùng PhongDAO.getAllPhong() rồi nhóm theo TrangThai
     */
    public void loadData() {
        // TODO: Implement tải dữ liệu và cập nhật UI
    }

    private VBox createSummaryCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        Label lbTitle = new Label(title);
        lbTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 11));
        lbTitle.setTextFill(Color.web("#7f8c8d"));
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        valueLabel.setTextFill(Color.web(color));
        card.getChildren().addAll(lbTitle, valueLabel);
        return card;
    }
}
