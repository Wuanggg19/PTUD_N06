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
import dao.KhachHangDAO;
import entity.KhachHang;

import java.util.List;

/**
 * ================================================================
 *  THỐNG KÊ KHÁCH HÀNG VIEW
 *  Chức năng: Thống kê số lượng khách hàng, khách hàng thân thiết,
 *             tần suất đặt phòng, nguồn khách...
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()              - Tải dữ liệu khách hàng từ DB
 *   2. updateSummaryCards()    - Cập nhật tổng khách, khách mới, khách thân thiết
 *   3. updateTopCustomerTable()- Bảng top khách hàng đặt phòng nhiều nhất
 *   4. updateBarChart()        - Biểu đồ số lượng khách theo tháng
 *   5. handleSearch()          - Tìm kiếm / lọc khách hàng
 * ================================================================
 */
public class CustomerStatsView {

    private static final String BG_LIGHT = "#f6f6f8";

    private Label lblTotal, lblNewMonth, lblFrequent;
    private TableView<KhachHang> tableTop;
    private BarChart<String, Number> barChart;
    private TextField txtSearch;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("THỐNG KÊ KHÁCH HÀNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        // --- Summary Cards ---
        lblTotal     = new Label("—");
        lblNewMonth  = new Label("—");
        lblFrequent  = new Label("—");

        HBox summaryCards = new HBox(20);
        summaryCards.getChildren().addAll(
                createSummaryCard("TỔNG KHÁCH HÀNG",       lblTotal,    "#3498db"),
                createSummaryCard("KHÁCH MỚI THÁNG NÀY",   lblNewMonth, "#27ae60"),
                createSummaryCard("KHÁCH THÂN THIẾT (≥5 lần)", lblFrequent, "#9b59b6")
        );
        for (Node c : summaryCards.getChildren()) HBox.setHgrow(c, Priority.ALWAYS);

        // --- Layout chính ---
        HBox mainContent = new HBox(20);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // Bảng top khách
        VBox leftBox = new VBox(12);
        leftBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16;");
        HBox.setHgrow(leftBox, Priority.ALWAYS);
        Label lblTableTitle = new Label("TOP KHÁCH HÀNG ĐẶT NHIỀU NHẤT");
        lblTableTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        txtSearch = new TextField();
        txtSearch.setPromptText("🔍  Tìm khách hàng...");
        txtSearch.setPrefHeight(34);
        txtSearch.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dce1e7;");

        tableTop = new TableView<>();
        tableTop.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableTop.setPlaceholder(new Label("Chưa có dữ liệu."));
        VBox.setVgrow(tableTop, Priority.ALWAYS);

        TableColumn<KhachHang, String> colMa  = new TableColumn<>("Mã KH");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaKhachHang()));
        TableColumn<KhachHang, String> colTen = new TableColumn<>("Tên Khách Hàng");
        colTen.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTenKhachHang()));
        TableColumn<KhachHang, String> colSDT = new TableColumn<>("Số Điện Thoại");
        colSDT.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSoDienThoai()));
        TableColumn<KhachHang, String> colDiaChi = new TableColumn<>("Địa Chỉ");
        colDiaChi.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDiaChi()));

        tableTop.getColumns().addAll(colMa, colTen, colSDT, colDiaChi);

        Button btnRefresh = new Button("🔄  Làm mới");
        btnRefresh.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-background-radius: 8; -fx-cursor: hand; -fx-font-weight: bold;");
        btnRefresh.setOnAction(e -> loadData());

        leftBox.getChildren().addAll(lblTableTitle, txtSearch, tableTop, btnRefresh);

        // Biểu đồ cột khách theo tháng
        VBox rightBox = new VBox(12);
        rightBox.setPrefWidth(380);
        rightBox.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16;");
        Label lblChartTitle = new Label("SỐ LƯỢNG KHÁCH THEO THÁNG");
        lblChartTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Số khách");
        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Khách theo tháng");
        barChart.setLegendVisible(false);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        // Dữ liệu mẫu
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.getData().addAll(
                new XYChart.Data<>("T1", 12), new XYChart.Data<>("T2", 18),
                new XYChart.Data<>("T3", 15), new XYChart.Data<>("T4", 22),
                new XYChart.Data<>("T5", 20), new XYChart.Data<>("T6", 30)
        );
        barChart.getData().add(series);
        rightBox.getChildren().addAll(lblChartTitle, barChart);

        mainContent.getChildren().addAll(leftBox, rightBox);
        root.getChildren().addAll(lblTitle, summaryCards, mainContent);
        return root;
    }

    /**
     * TODO: Tải danh sách khách hàng và cập nhật bảng + biểu đồ.
     *       Gợi ý: Dùng KhachHangDAO.getAllKhachHang()
     */
    public void loadData() {
        // TODO: Implement tải dữ liệu khách hàng
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
