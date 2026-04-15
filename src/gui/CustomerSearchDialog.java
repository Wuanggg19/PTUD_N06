package gui;

import entity.KhachHang;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.List;

public class CustomerSearchDialog {
    private final Stage stage;
    private final KhachHangService service;
    private KhachHang selectedCustomer;

    public CustomerSearchDialog(Window owner, KhachHangService service) {
        this.service = service;
        stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Tìm khách hàng");
        stage.setResizable(false);
        stage.setScene(new Scene(buildUi(), 760, 520));
    }

    public KhachHang show() {
        stage.showAndWait();
        return selectedCustomer;
    }

    private BorderPane buildUi() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: white;");
        root.setTop(buildHeader());
        root.setCenter(buildContent());
        return root;
    }

    private VBox buildHeader() {
        VBox header = new VBox(6);
        header.setPadding(new Insets(20, 24, 18, 24));
        header.setStyle("-fx-background-color: #2c0fbd;");

        Label title = new Label("TÌM KIẾM KHÁCH HÀNG");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 17));
        title.setTextFill(Color.WHITE);

        Label sub = new Label("Tìm theo mã, tên hoặc số điện thoại rồi chọn để điền nhanh.");
        sub.setFont(Font.font("Segoe UI", 12));
        sub.setTextFill(Color.web("#d8d2ff"));

        header.getChildren().addAll(title, sub);
        return header;
    }

    private VBox buildContent() {
        List<KhachHang> customers = service.getAll();
        ObservableList<KhachHang> items = FXCollections.observableArrayList(customers);
        FilteredList<KhachHang> filtered = new FilteredList<>(items, kh -> true);

        TextField txtKeyword = new TextField();
        txtKeyword.setPromptText("Nhập mã, tên hoặc số điện thoại...");
        txtKeyword.setPrefHeight(40);
        txtKeyword.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; -fx-border-color: #dcdde1;");

        TableView<KhachHang> table = new TableView<>(filtered);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("Không tìm thấy khách hàng phù hợp"));
        VBox.setVgrow(table, Priority.ALWAYS);

        TableColumn<KhachHang, String> colMa = new TableColumn<>("Mã KH");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaKhachHang()));
        TableColumn<KhachHang, String> colTen = new TableColumn<>("Tên khách hàng");
        colTen.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getTenKhachHang()));
        TableColumn<KhachHang, String> colSdt = new TableColumn<>("Số điện thoại");
        colSdt.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getSoDienThoai()));
        TableColumn<KhachHang, String> colDiaChi = new TableColumn<>("Địa chỉ");
        colDiaChi.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getDiaChi()));
        table.getColumns().addAll(colMa, colTen, colSdt, colDiaChi);

        txtKeyword.textProperty().addListener((obs, oldValue, newValue) -> {
            String keyword = newValue == null ? "" : newValue.trim().toLowerCase();
            filtered.setPredicate(kh -> {
                if (keyword.isEmpty()) {
                    return true;
                }
                return safe(kh.getMaKhachHang()).toLowerCase().contains(keyword)
                        || safe(kh.getTenKhachHang()).toLowerCase().contains(keyword)
                        || safe(kh.getSoDienThoai()).contains(keyword);
            });
        });

        table.setRowFactory(tv -> {
            TableRow<KhachHang> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    selectedCustomer = row.getItem();
                    stage.close();
                }
            });
            return row;
        });

        Button btnCancel = new Button("Đóng");
        btnCancel.setPrefHeight(38);
        btnCancel.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 8; -fx-cursor: hand;");
        btnCancel.setOnAction(e -> stage.close());

        Button btnSelect = new Button("Chọn khách");
        btnSelect.setPrefHeight(38);
        btnSelect.setStyle("-fx-background-color: #2c0fbd; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        btnSelect.setOnAction(e -> {
            selectedCustomer = table.getSelectionModel().getSelectedItem();
            if (selectedCustomer != null) {
                stage.close();
            }
        });

        HBox actions = new HBox(10, btnCancel, btnSelect);
        actions.setAlignment(Pos.CENTER_RIGHT);

        VBox content = new VBox(16, txtKeyword, table, actions);
        content.setPadding(new Insets(18, 24, 24, 24));
        return content;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
