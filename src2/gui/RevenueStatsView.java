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

import connectDB.ConnectDB;
import dao.HoaDonDAO;
import entity.HoaDon;

import java.util.List;

/**
 * ================================================================
 *  THỐNG KÊ DOANH THU VIEW
 *  Chức năng: Thống kê và biểu đồ doanh thu theo ngày/tuần/tháng/năm.
 * ================================================================
 *
 *  TODO: Implement các chức năng sau:
 *   1. loadData()           - Tải dữ liệu hóa đơn từ DB
 *   2. handleFilterPeriod() - Lọc và tổng hợp doanh thu theo kỳ (ngày/tháng/năm)
 *   3. updateChart()        - Cập nhật biểu đồ cột/đường với dữ liệu đã tổng hợp
 *   4. updateSummaryCards() - Cập nhật các ô tóm tắt (tổng doanh thu, so sánh kỳ trước...)
 *   5. handleExport()       - Xuất báo cáo ra Excel/PDF (tùy chọn)
 * ================================================================
 */
public class RevenueStatsView {

    private static final String BG_LIGHT = "#f6f6f8";

    private Label lblTotalRevenue, lblTotalInvoices, lblAvgRevenue;
    private BarChart<String, Number> barChart;
    private DatePicker dpFrom, dpTo;
    private ComboBox<String> cboPeriod;

    public Node createView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        // --- Header ---
        Label lblTitle = new Label("THỐNG KÊ DOANH THU");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));

        // --- Bộ lọc ---
        HBox filterBar = new HBox(14);
        filterBar.setAlignment(Pos.CENTER_LEFT);
        filterBar.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 12 16;");

        cboPeriod = new ComboBox<>();
        cboPeriod.setItems(javafx.collections.FXCollections.observableArrayList("Theo ngày", "Theo tháng", "Theo năm"));
        cboPeriod.setValue("Theo tháng");
        cboPeriod.setPrefHeight(34);

        Label lblFrom = new Label("Từ:");
        dpFrom = new DatePicker();
        dpFrom.setPromptText("Ngày bắt đầu");
        Label lblTo = new Label("Đến:");
        dpTo = new DatePicker();
        dpTo.setPromptText("Ngày kết thúc");

        Button btnFilter = new Button("📊  Thống kê");
        btnFilter.setPrefHeight(34);
        btnFilter.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnFilter.setTextFill(Color.WHITE);
        btnFilter.setStyle("-fx-background-color: #2c0fbd; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 16;");
        btnFilter.setOnAction(e -> handleFilterPeriod());

        Button btnExport = new Button("📤  Xuất báo cáo");
        btnExport.setPrefHeight(34);
        btnExport.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        btnExport.setTextFill(Color.WHITE);
        btnExport.setStyle("-fx-background-color: #16a085; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 0 16;");
        btnExport.setOnAction(e -> handleExport());

        filterBar.getChildren().addAll(new Label("Kỳ:"), cboPeriod, lblFrom, dpFrom, lblTo, dpTo, btnFilter, btnExport);

        // --- Cards tóm tắt ---
        lblTotalRevenue = new Label("—");
        lblTotalInvoices = new Label("—");
        lblAvgRevenue = new Label("—");

        HBox summaryCards = new HBox(20);
        summaryCards.getChildren().addAll(
                createSummaryCard("TỔNG DOANH THU", lblTotalRevenue, "#e74c3c"),
                createSummaryCard("SỐ HÓA ĐƠN", lblTotalInvoices, "#3498db"),
                createSummaryCard("DOANH THU TRUNG BÌNH", lblAvgRevenue, "#f39c12")
        );
        for (Node card : summaryCards.getChildren()) {
            HBox.setHgrow(card, Priority.ALWAYS);
        }

        // --- Biểu đồ cột ---
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Thời gian");
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Doanh thu (VNĐ)");

        barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Doanh thu theo thời gian");
        barChart.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        barChart.setPrefHeight(350);
        VBox.setVgrow(barChart, Priority.ALWAYS);

        // Dữ liệu mẫu để xem giao diện
        XYChart.Series<String, Number> sampleSeries = new XYChart.Series<>();
        sampleSeries.setName("Doanh thu (mẫu)");
        sampleSeries.getData().add(new XYChart.Data<>("Tháng 1", 15000000));
        sampleSeries.getData().add(new XYChart.Data<>("Tháng 2", 22000000));
        sampleSeries.getData().add(new XYChart.Data<>("Tháng 3", 18000000));
        sampleSeries.getData().add(new XYChart.Data<>("Tháng 4", 30000000));
        barChart.getData().add(sampleSeries);

        root.getChildren().addAll(lblTitle, filterBar, summaryCards, barChart);
        return root;
    }

    /**
     * TODO: Tải dữ liệu hóa đơn từ DB và cập nhật biểu đồ + cards tóm tắt.
     */
    public void loadData() {
        // TODO: Gọi handleFilterPeriod() với khoảng thời gian mặc định
    }

    /**
     * TODO: Lọc, tổng hợp doanh thu theo kỳ đã chọn (ngày/tháng/năm).
     *       Cập nhật barChart và 3 summary cards.
     */
    private void handleFilterPeriod() {
        // TODO: Implement thống kê doanh thu
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Thông báo");
        a.setHeaderText(null);
        a.setContentText("Chức năng thống kê doanh thu đang được phát triển.");
        a.showAndWait();
    }

    /**
     * TODO: Xuất báo cáo doanh thu ra file Excel hoặc PDF.
     */
    private void handleExport() {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Thông báo");
        a.setHeaderText(null);
        a.setContentText("Chức năng xuất báo cáo đang được phát triển.");
        a.showAndWait();
    }

    private VBox createSummaryCard(String title, Label valueLabel, String color) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(18));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        Label lbTitle = new Label(title);
        lbTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        lbTitle.setTextFill(Color.web("#7f8c8d"));
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web(color));
        card.getChildren().addAll(lbTitle, valueLabel);
        return card;
    }
}
