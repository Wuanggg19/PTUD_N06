package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import dao.BangGiaDetailDAO;
import dao.BangGiaHeaderDAO;
import dao.PhieuDatPhongDAO;
import dao.PhongDAO;
import entity.BangGiaDetail;
import entity.BangGiaHeader;
import entity.ChiTietPhieuDat;
import entity.KhachHang;
import entity.NhanVien;
import entity.PhieuDatPhong;
import entity.Phong;
import util.BookingStatus;

public class BookingView {

    private static final String BG_LIGHT = "#f6f6f8";
    private static final String PRIMARY_COLOR = "#2c0fbd";
    private static final String SUCCESS_COLOR = "#27ae60";
    private static final String DANGER_COLOR = "#e74c3c";
    private static final String CARD_STYLE =
            "-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 12;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);";

    // Step 1 fields
    private TextField txtSdt, txtTen, txtDiaChi;
    private RadioButton rbNam, rbNu;
    private DatePicker dpNgayNhan, dpNgayTra;
    private Spinner<Integer> spGioNhan, spPhutNhan, spGioTra, spPhutTra;
    private CheckBox chkQuickCheckin;

    // Step 2 fields
    private TableView<Phong> tvPhongTrong;
    private VBox selectedRoomsContainer;
    private final List<Phong> selectedRooms = new ArrayList<>();

    private TextField txtFilterMa;
    private ComboBox<String> cbFilterTang, cbFilterLoai, cbFilterGiuong, cbFilterSucChua;

    private ObservableList<Phong> allPhongTrong = FXCollections.observableArrayList();
    private FilteredList<Phong> filteredPhong;

    // Wizard state
    private StackPane wizardPane;
    private Node step1Panel, step2Panel;

    // Services
    private final NhanVien loggedInNhanVien;
    private final KhachHangService khService = new KhachHangService();
    private final PhieuDatPhongDAO pdpDAO = new PhieuDatPhongDAO();
    private final BangGiaHeaderDAO bangGiaHeaderDAO = new BangGiaHeaderDAO();
    private final BangGiaDetailDAO bangGiaDetailDAO = new BangGiaDetailDAO();

    // Auto-pricing state
    private BangGiaHeader bangGiaHienTai = null;
    private Label lblBangGiaInfo;

    private KhachHang currentKH = null;
    private Task<BangGiaHeader> bangGiaTask = null;
    private Task<KhachHang> searchTask = null;
    private boolean isLoading = false;
    private boolean isAutoFilling = false;

    public BookingView(NhanVien nhanVien) {
        this.loggedInNhanVien = nhanVien;
    }

    public Node createView() {
        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");
        VBox.setVgrow(root, Priority.ALWAYS);

        // Header
        HBox header = new HBox();
        header.setPadding(new Insets(25, 30, 15, 30));
        header.setAlignment(Pos.CENTER_LEFT);
        Label lblTitle = new Label("LẬP PHIẾU ĐẶT PHÒNG");
        lblTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        lblTitle.setTextFill(Color.web("#2c3e50"));
        header.getChildren().add(lblTitle);

        // Wizard breadcrumb indicator
        HBox breadcrumb = buildBreadcrumb();
        breadcrumb.setPadding(new Insets(0, 30, 15, 30));

        step1Panel = createStep1Panel();
        step2Panel = createStep2Panel();
        step2Panel.setVisible(false);
        step2Panel.setManaged(false);

        wizardPane = new StackPane(step1Panel, step2Panel);
        VBox.setVgrow(wizardPane, Priority.ALWAYS);

        root.getChildren().addAll(header, breadcrumb, wizardPane);

        loadPhongTrong();
        lookupBangGia();
        return root;
    }

    // ─── Breadcrumb ──────────────────────────────────────────────────────────

    private Label lblStep1Crumb, lblStep2Crumb;

    private HBox buildBreadcrumb() {
        lblStep1Crumb = new Label("① Thông tin & Thời gian");
        lblStep1Crumb.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

        Label lblArrow = new Label("  ›  ");
        lblArrow.setTextFill(Color.web("#aaa"));

        lblStep2Crumb = new Label("② Chọn phòng & Hoàn tất");
        lblStep2Crumb.setFont(Font.font("Segoe UI", 13));
        lblStep2Crumb.setTextFill(Color.web("#aaa"));

        HBox box = new HBox(4, lblStep1Crumb, lblArrow, lblStep2Crumb);
        box.setAlignment(Pos.CENTER_LEFT);
        updateBreadcrumb(1);
        return box;
    }

    private void updateBreadcrumb(int activeStep) {
        if (activeStep == 1) {
            lblStep1Crumb.setTextFill(Color.web(PRIMARY_COLOR));
            lblStep2Crumb.setTextFill(Color.web("#aaa"));
            lblStep2Crumb.setFont(Font.font("Segoe UI", 13));
            lblStep1Crumb.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        } else {
            lblStep1Crumb.setTextFill(Color.web("#aaa"));
            lblStep1Crumb.setFont(Font.font("Segoe UI", 13));
            lblStep2Crumb.setTextFill(Color.web(PRIMARY_COLOR));
            lblStep2Crumb.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        }
    }

    // ─── Step 1 Panel ────────────────────────────────────────────────────────

    private Node createStep1Panel() {
        VBox panel = new VBox(0);
        panel.setPadding(new Insets(0, 30, 30, 30));
        VBox.setVgrow(panel, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: " + BG_LIGHT + ";");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox content = new VBox(20);
        content.setPadding(new Insets(5));

        // -- Customer info card --
        VBox khCard = new VBox(16);
        khCard.setStyle(CARD_STYLE);

        HBox khHeader = new HBox(10);
        khHeader.setAlignment(Pos.CENTER_LEFT);
        Label lblKH = new Label("1. THÔNG TIN KHÁCH HÀNG");
        lblKH.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        Region khSpacer = new Region();
        HBox.setHgrow(khSpacer, Priority.ALWAYS);
        Button btnSearchKH = new Button("Tìm khách hàng");
        btnSearchKH.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-cursor: hand; -fx-background-radius: 6; -fx-padding: 6 16 6 16;");
        btnSearchKH.setOnAction(e -> showCustomerSearchPopup());
        khHeader.getChildren().addAll(lblKH, khSpacer, btnSearchKH);

        txtSdt = createStyledTextField("Nhập số điện thoại khách...");
        txtTen = createStyledTextField("Họ và tên khách hàng");
        txtDiaChi = createStyledTextField("Địa chỉ (Tỉnh/Thành phố)");

        txtSdt.textProperty().addListener((obs, oldVal, newVal) -> {
            if (isAutoFilling) return;
            resetStyle(txtSdt);
            if (newVal.length() < 10) {
                currentKH = null;
                txtTen.clear();
                txtDiaChi.clear();
                txtTen.setEditable(true);
            } else {
                searchKhachHang(newVal);
            }
        });
        txtTen.textProperty().addListener((o, v, n) -> resetStyle(txtTen));
        txtDiaChi.textProperty().addListener((o, v, n) -> resetStyle(txtDiaChi));

        ToggleGroup groupGT = new ToggleGroup();
        rbNam = new RadioButton("Nam");
        rbNam.setToggleGroup(groupGT);
        rbNam.setSelected(true);
        rbNu = new RadioButton("Nữ");
        rbNu.setToggleGroup(groupGT);
        HBox gtBox = new HBox(20, new Label("Giới tính:"), rbNam, rbNu);
        gtBox.setAlignment(Pos.CENTER_LEFT);

        txtSdt.setOnAction(e -> txtTen.requestFocus());
        txtTen.setOnAction(e -> txtDiaChi.requestFocus());

        khCard.getChildren().addAll(khHeader,
                fieldRow("Số điện thoại:", txtSdt),
                fieldRow("Họ và tên:", txtTen),
                fieldRow("Địa chỉ:", txtDiaChi),
                gtBox);

        // -- Date card --
        VBox dateCard = new VBox(16);
        dateCard.setStyle(CARD_STYLE);

        Label lblTime = new Label("2. THỜI GIAN ĐẶT PHÒNG");
        lblTime.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        dpNgayNhan = new DatePicker(LocalDate.now());
        dpNgayNhan.setPrefHeight(40);
        dpNgayNhan.setMaxWidth(Double.MAX_VALUE);
        dpNgayTra = new DatePicker(LocalDate.now().plusDays(1));
        dpNgayTra.setPrefHeight(40);
        dpNgayTra.setMaxWidth(Double.MAX_VALUE);

        dpNgayNhan.setOnAction(e -> {
            resetStyle(dpNgayNhan);
            onDatesChanged();
        });
        dpNgayTra.setOnAction(e -> {
            resetStyle(dpNgayTra);
            onDatesChanged();
        });

        spGioNhan = createTimeSpinner(23, 14);
        spPhutNhan = createTimeSpinner(59, 0);
        spGioTra = createTimeSpinner(23, 12);
        spPhutTra = createTimeSpinner(59, 0);

        HBox dateRow = new HBox(20);

        VBox nhanBox = new VBox(6, new Label("Ngày nhận phòng:"), dpNgayNhan);
        HBox timeNhanBox = new HBox(5, new Label("Lúc:"), spGioNhan, new Label(":"), spPhutNhan);
        timeNhanBox.setAlignment(Pos.CENTER_LEFT);
        nhanBox.getChildren().add(timeNhanBox);
        HBox.setHgrow(nhanBox, Priority.ALWAYS);

        VBox traBox = new VBox(6, new Label("Ngày trả dự kiến:"), dpNgayTra);
        HBox timeTraBox = new HBox(5, new Label("Lúc:"), spGioTra, new Label(":"), spPhutTra);
        timeTraBox.setAlignment(Pos.CENTER_LEFT);
        traBox.getChildren().add(timeTraBox);
        HBox.setHgrow(traBox, Priority.ALWAYS);

        dateRow.getChildren().addAll(nhanBox, traBox);

        chkQuickCheckin = new CheckBox("Nhận phòng nhanh (Khách đã đến)");
        chkQuickCheckin.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
        chkQuickCheckin.setPadding(new Insets(5, 0, 5, 0));
        chkQuickCheckin.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                dpNgayNhan.setValue(LocalDate.now());
                dpNgayNhan.setDisable(true);
                // Không gọi onDatesChanged() ở đây vì setValue(LocalDate.now())
                // đã tự động kích hoạt ActionEvent của dpNgayNhan rồi.
            } else {
                dpNgayNhan.setDisable(false);
            }
        });

        lblBangGiaInfo = new Label("Bảng giá: Đang tải...");
        lblBangGiaInfo.setFont(Font.font("Segoe UI", 12));
        lblBangGiaInfo.setTextFill(Color.web("#7f8c8d"));
        lblBangGiaInfo.setStyle("-fx-background-color: #f0f4ff; -fx-padding: 6 12; -fx-background-radius: 6;");

        dateCard.getChildren().addAll(lblTime, dateRow, chkQuickCheckin, lblBangGiaInfo);

        // -- Next button --
        Button btnNext = new Button("TIẾP THEO  (ENTER)  →");
        btnNext.setPrefHeight(50);
        btnNext.setMaxWidth(Double.MAX_VALUE);
        btnNext.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 15; -fx-cursor: hand; -fx-background-radius: 10;");
        btnNext.setOnAction(e -> goToStep2());
        btnNext.setDefaultButton(true);

        HBox nextRow = new HBox();
        nextRow.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox.setHgrow(btnNext, Priority.ALWAYS);
        nextRow.getChildren().add(btnNext);

        content.getChildren().addAll(khCard, dateCard, nextRow);
        scroll.setContent(content);
        panel.getChildren().add(scroll);
        return panel;
    }

    private HBox fieldRow(String label, Control field) {
        Label lbl = new Label(label);
        lbl.setMinWidth(140);
        lbl.setFont(Font.font("Segoe UI", 13));
        HBox row = new HBox(12, lbl, field);
        row.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(field, Priority.ALWAYS);
        return row;
    }

    private VBox labeledFilter(String label, Control field) {
        Label lbl = new Label(label);
        lbl.setFont(Font.font("Segoe UI", 11));
        lbl.setTextFill(Color.web("#888"));
        VBox box = new VBox(3, lbl, field);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    // ─── Step 2 Panel ────────────────────────────────────────────────────────

    private Node createStep2Panel() {
        VBox panel = new VBox(20);
        panel.setPadding(new Insets(0, 30, 30, 30));
        VBox.setVgrow(panel, Priority.ALWAYS);

        // Back button row
        Button btnBack = new Button("←  QUAY LẠI");
        btnBack.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY_COLOR + ";" +
                "-fx-font-weight: bold; -fx-font-size: 13; -fx-cursor: hand; -fx-border-color: " + PRIMARY_COLOR + ";" +
                "-fx-border-radius: 6; -fx-background-radius: 6; -fx-padding: 6 14 6 14;");
        btnBack.setOnAction(e -> goToStep1());

        HBox backRow = new HBox(btnBack);
        backRow.setAlignment(Pos.CENTER_LEFT);

        // Main content: two columns
        HBox mainContent = new HBox(20);
        VBox.setVgrow(mainContent, Priority.ALWAYS);

        // Left: available rooms
        VBox roomBox = new VBox(12);
        roomBox.setStyle(CARD_STYLE);
        VBox.setVgrow(roomBox, Priority.ALWAYS);
        HBox.setHgrow(roomBox, Priority.ALWAYS);

        Label lblRoom = new Label("CHỌN PHÒNG ĐANG TRỐNG");
        lblRoom.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        HBox filterBar = buildFilterBar();

        filteredPhong = new FilteredList<>(allPhongTrong, p -> true);

        tvPhongTrong = new TableView<>(filteredPhong);
        tvPhongTrong.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tvPhongTrong.setPlaceholder(new Label("Không có phòng trống phù hợp"));
        VBox.setVgrow(tvPhongTrong, Priority.ALWAYS);

        TableColumn<Phong, String> colMa = new TableColumn<>("Mã Phòng");
        colMa.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getMaPhong()));

        TableColumn<Phong, String> colLoai = new TableColumn<>("Loại");
        colLoai.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getLoaiPhong()));

        TableColumn<Phong, String> colGiuong = new TableColumn<>("Giường");
        colGiuong.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.valueOf(d.getValue().getSoGiuong())));

        TableColumn<Phong, String> colSucChua = new TableColumn<>("Sức chứa");
        colSucChua.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                d.getValue().getSucChua() + " người"));

        TableColumn<Phong, String> colGia = new TableColumn<>("Giá/Ngày");
        colGia.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(
                String.format("%,.0f VNĐ", getDonGiaNgay(d.getValue()))));

        tvPhongTrong.getColumns().addAll(colMa, colLoai, colGiuong, colSucChua, colGia);

        tvPhongTrong.setRowFactory(tv -> {
            TableRow<Phong> row = new TableRow<Phong>() {
                @Override
                protected void updateItem(Phong item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setStyle("");
                    } else if (selectedRooms.contains(item)) {
                        setStyle("-fx-background-color: #dce8ff; -fx-text-background-color: #1a1a2e;");
                    } else {
                        setStyle("");
                    }
                }
            };
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    Phong p = row.getItem();
                    if (selectedRooms.contains(p)) {
                        selectedRooms.remove(p);
                    } else {
                        selectedRooms.add(p);
                    }
                    tvPhongTrong.refresh();
                    renderSelectedRooms();
                }
            });
            return row;
        });

        roomBox.getChildren().addAll(lblRoom, filterBar, tvPhongTrong);

        // Right: selected rooms + action buttons
        VBox selBox = new VBox(14);
        selBox.setStyle(CARD_STYLE);
        selBox.setPrefWidth(340);
        selBox.setMinWidth(300);
        VBox.setVgrow(selBox, Priority.ALWAYS);

        Label lblSel = new Label("PHÒNG ĐÃ CHỌN");
        lblSel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));

        selectedRoomsContainer = new VBox(8);
        ScrollPane spSelected = new ScrollPane(selectedRoomsContainer);
        spSelected.setFitToWidth(true);
        spSelected.setStyle("-fx-background-color: transparent; -fx-background: #f9f9fb;");
        VBox.setVgrow(spSelected, Priority.ALWAYS);

        Button btnConfirm = new Button("XÁC NHẬN HOÀN TẤT");
        btnConfirm.setPrefHeight(50);
        btnConfirm.setMaxWidth(Double.MAX_VALUE);
        btnConfirm.setStyle("-fx-background-color: " + PRIMARY_COLOR + "; -fx-text-fill: white;" +
                "-fx-font-weight: bold; -fx-font-size: 15; -fx-cursor: hand; -fx-background-radius: 8;");
        btnConfirm.setOnAction(e -> {
            String status = chkQuickCheckin.isSelected()
                    ? BookingStatus.DA_NHAN_PHONG.getCode()
                    : BookingStatus.CHO_XAC_NHAN.getCode();
            handleBooking(status);
        });

        Button btnReset = new Button("LÀM MỚI");
        btnReset.setPrefHeight(40);
        btnReset.setMaxWidth(Double.MAX_VALUE);
        btnReset.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold;" +
                "-fx-font-size: 13; -fx-cursor: hand; -fx-background-radius: 8;");
        btnReset.setOnAction(e -> clearAllAndReset());

        selBox.getChildren().addAll(lblSel, spSelected, btnConfirm, btnReset);

        mainContent.getChildren().addAll(roomBox, selBox);
        panel.getChildren().addAll(backRow, mainContent);
        return panel;
    }

    // ─── Filter Bar ──────────────────────────────────────────────────────────

    private static final String ALL_OPTION = "Tất cả";

    private HBox buildFilterBar() {
        txtFilterMa = new TextField();
        txtFilterMa.setPromptText("Tìm mã phòng...");
        txtFilterMa.setPrefHeight(36);
        txtFilterMa.setPrefWidth(130);
        txtFilterMa.setStyle("-fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");

        cbFilterTang = new ComboBox<>();
        cbFilterTang.setPrefHeight(36);
        cbFilterTang.setPrefWidth(110);

        cbFilterLoai = new ComboBox<>();
        cbFilterLoai.setPrefHeight(36);
        cbFilterLoai.setPrefWidth(130);

        cbFilterGiuong = new ComboBox<>();
        cbFilterGiuong.setPrefHeight(36);
        cbFilterGiuong.setPrefWidth(115);

        cbFilterSucChua = new ComboBox<>();
        cbFilterSucChua.setPrefHeight(36);
        cbFilterSucChua.setPrefWidth(115);

        Button btnClearFilter = new Button("Xóa lọc");
        btnClearFilter.setPrefHeight(36);
        btnClearFilter.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #555; -fx-cursor: hand;" +
                "-fx-background-radius: 6; -fx-font-size: 12;");
        btnClearFilter.setOnAction(e -> clearFilters());

        // Real-time filter listeners
        txtFilterMa.textProperty().addListener((obs, o, n) -> applyFilters());
        cbFilterTang.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbFilterLoai.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbFilterGiuong.valueProperty().addListener((obs, o, n) -> applyFilters());
        cbFilterSucChua.valueProperty().addListener((obs, o, n) -> applyFilters());

        HBox bar = new HBox(12,
                labeledFilter("Mã phòng:", txtFilterMa),
                labeledFilter("Tầng:", cbFilterTang),
                labeledFilter("Loại phòng:", cbFilterLoai),
                labeledFilter("Số giường:", cbFilterGiuong),
                labeledFilter("Sức chứa:", cbFilterSucChua),
                btnClearFilter);
        bar.setAlignment(Pos.CENTER_LEFT);
        bar.setPadding(new Insets(6, 0, 6, 0));
        return bar;
    }

    private boolean isAll(String val) {
        return val == null || val.isEmpty() || ALL_OPTION.equals(val);
    }

    private void applyFilters() {
        if (filteredPhong == null) return;
        String maTxt = txtFilterMa.getText().trim().toLowerCase();
        String tang = cbFilterTang.getValue();
        String loai = cbFilterLoai.getValue();
        String giuong = cbFilterGiuong.getValue();
        String sucChua = cbFilterSucChua.getValue();

        filteredPhong.setPredicate(p -> {
            if (!maTxt.isEmpty() && !p.getMaPhong().toLowerCase().contains(maTxt)) return false;
            if (!isAll(tang) && !tang.equals(extractTang(p.getMaPhong()))) return false;
            if (!isAll(loai) && !loai.equalsIgnoreCase(p.getLoaiPhong())) return false;
            if (!isAll(giuong)) {
                try {
                    if (p.getSoGiuong() != Integer.parseInt(giuong.replaceAll("[^0-9]", ""))) return false;
                } catch (NumberFormatException ignored) {}
            }
            if (!isAll(sucChua)) {
                try {
                    if (p.getSucChua() != Integer.parseInt(sucChua.replaceAll("[^0-9]", ""))) return false;
                } catch (NumberFormatException ignored) {}
            }
            return true;
        });
    }

    private void clearFilters() {
        txtFilterMa.clear();
        cbFilterTang.setValue(ALL_OPTION);
        cbFilterLoai.setValue(ALL_OPTION);
        cbFilterGiuong.setValue(ALL_OPTION);
        cbFilterSucChua.setValue(ALL_OPTION);
    }

    private String extractTang(String maPhong) {
        // Mã phòng dạng P101 => tầng 1, P201 => tầng 2
        if (maPhong == null || maPhong.length() < 2) return "?";
        try {
            String digits = maPhong.replaceAll("[^0-9]", "");
            if (digits.length() >= 3) {
                return "Tầng " + digits.charAt(0);
            }
        } catch (Exception ignored) {}
        return "?";
    }

    private void populateFilterCombos(List<Phong> list) {
        String selTang   = cbFilterTang.getValue();
        String selLoai   = cbFilterLoai.getValue();
        String selGiuong = cbFilterGiuong.getValue();
        String selSucChua = cbFilterSucChua.getValue();

        // Tầng
        List<String> tangs = new ArrayList<>();
        tangs.add(ALL_OPTION);
        list.stream().map(p -> extractTang(p.getMaPhong())).distinct().sorted().forEach(tangs::add);
        cbFilterTang.getItems().setAll(tangs);
        cbFilterTang.setValue(tangs.contains(selTang) ? selTang : ALL_OPTION);

        // Loại phòng
        List<String> loais = new ArrayList<>();
        loais.add(ALL_OPTION);
        list.stream().map(Phong::getLoaiPhong).distinct().sorted().forEach(loais::add);
        cbFilterLoai.getItems().setAll(loais);
        cbFilterLoai.setValue(loais.contains(selLoai) ? selLoai : ALL_OPTION);

        // Số giường
        List<String> giuongs = new ArrayList<>();
        giuongs.add(ALL_OPTION);
        list.stream().map(p -> p.getSoGiuong() + " giường").distinct().sorted().forEach(giuongs::add);
        cbFilterGiuong.getItems().setAll(giuongs);
        cbFilterGiuong.setValue(giuongs.contains(selGiuong) ? selGiuong : ALL_OPTION);

        // Sức chứa
        List<String> sucChuas = new ArrayList<>();
        sucChuas.add(ALL_OPTION);
        list.stream().map(p -> p.getSucChua() + " người").distinct().sorted().forEach(sucChuas::add);
        cbFilterSucChua.getItems().setAll(sucChuas);
        cbFilterSucChua.setValue(sucChuas.contains(selSucChua) ? selSucChua : ALL_OPTION);
    }

    // ─── Wizard Navigation ───────────────────────────────────────────────────

    private void goToStep2() {
        if (!validateStep1()) return;

        step1Panel.setVisible(false);
        step1Panel.setManaged(false);
        step2Panel.setVisible(true);
        step2Panel.setManaged(true);
        updateBreadcrumb(2);
        renderSelectedRooms();
    }

    private void goToStep1() {
        step2Panel.setVisible(false);
        step2Panel.setManaged(false);
        step1Panel.setVisible(true);
        step1Panel.setManaged(true);
        updateBreadcrumb(1);
    }

    // ─── Auto-pricing & Date change ──────────────────────────────────────────

    /** Gọi khi ngày nhận/trả thay đổi: xóa phòng đã chọn + tìm bảng giá mới. */
    private void onDatesChanged() {
        // Ràng buộc: xóa sạch phòng đã chọn để đảm bảo tính khả dụng theo ngày mới
        if (!selectedRooms.isEmpty()) {
            selectedRooms.clear();
            if (tvPhongTrong != null) tvPhongTrong.refresh();
            if (selectedRoomsContainer != null) renderSelectedRooms();
        }

        loadPhongTrong();
        lookupBangGia();
    }

    /** Tự động tìm bảng giá hiệu lực theo ngày nhận. */
    private void lookupBangGia() {
        LocalDate ngayNhan = dpNgayNhan.getValue();
        if (ngayNhan == null) return;

        // Hủy task cũ nếu đang chạy để tránh xung đột kết nối (Race Condition)
        if (bangGiaTask != null && bangGiaTask.isRunning()) {
            bangGiaTask.cancel();
        }

        bangGiaTask = new Task<BangGiaHeader>() {
            @Override
            protected BangGiaHeader call() {
                return bangGiaHeaderDAO.getBangGiaTheoNgay(ngayNhan);
            }
        };
        bangGiaTask.setOnSucceeded(e -> {
            bangGiaHienTai = bangGiaTask.getValue();
            if (lblBangGiaInfo != null) {
                if (bangGiaHienTai != null) {
                    lblBangGiaInfo.setText("Bảng giá áp dụng: " + bangGiaHienTai.getTenBangGia()
                            + "  (" + bangGiaHienTai.getMaBangGia() + ")");
                    lblBangGiaInfo.setStyle("-fx-background-color: #e8f8f0; -fx-padding: 6 12; -fx-background-radius: 6;");
                    lblBangGiaInfo.setTextFill(Color.web("#1e8449"));
                } else {
                    lblBangGiaInfo.setText("Không có bảng giá hiệu lực — dùng giá gốc phòng.");
                    lblBangGiaInfo.setStyle("-fx-background-color: #fef9e7; -fx-padding: 6 12; -fx-background-radius: 6;");
                    lblBangGiaInfo.setTextFill(Color.web("#b7770d"));
                }
            }
            // Refresh giá trong bảng phòng
            if (tvPhongTrong != null) tvPhongTrong.refresh();
            renderSelectedRooms();
        });
        new Thread(bangGiaTask).start();
    }

    /** Lấy đơn giá 1 ngày theo bảng giá đang hiệu lực. */
    private double getDonGiaNgay(Phong p) {
        if (p == null) return 0;

        // 1. Tìm bảng giá áp dụng
        LocalDate nhan = (dpNgayNhan != null) ? dpNgayNhan.getValue() : LocalDate.now();
        BangGiaHeader bg = bangGiaHeaderDAO.getBangGiaTheoNgay(nhan);
        if (bg == null) return p.getGiaPhong(); // Fallback cuối cùng là giá trong bảng Phòng nếu DB trống rỗng

        // 2. Lấy chi tiết đơn giá ngày
        BangGiaDetail ct = bangGiaDetailDAO.getDetailByBangGiaAndLoai(bg.getMaBangGia(), p.getMaLoai());
        if (ct == null) return p.getGiaPhong();

        // Ưu tiên giá Lễ -> Cuối tuần -> Ngày thường
        if (bg.isDacBiet() && (bg.getLoaiNgay().contains("LE") || bg.getLoaiNgay().contains("TET"))) {
            if (ct.getGiaLe() > 0) return ct.getGiaLe();
        }
        int dayOfWeek = nhan.getDayOfWeek().getValue();
        if ((dayOfWeek == 6 || dayOfWeek == 7) && ct.getGiaCuoiTuan() > 0) {
            return ct.getGiaCuoiTuan();
        }

        return ct.getGiaTheoNgay() > 0 ? ct.getGiaTheoNgay() : p.getGiaPhong();
    }

    /** Lấy tổng giá áp dụng cho phòng (tổng tiền ở n ngày/giờ). */
    private double getGiaApDung(Phong p) {
        if (p == null) return 0;

        LocalDate nhan = (dpNgayNhan != null) ? dpNgayNhan.getValue() : LocalDate.now();
        LocalDate tra = (dpNgayTra != null) ? dpNgayTra.getValue() : LocalDate.now().plusDays(1);
        int hIn = (spGioNhan != null) ? spGioNhan.getValue() : 14;
        int mIn = (spPhutNhan != null) ? spPhutNhan.getValue() : 0;
        int hOut = (spGioTra != null) ? spGioTra.getValue() : 12;
        int mOut = (spPhutTra != null) ? spPhutTra.getValue() : 0;

        LocalDateTime start = nhan.atTime(hIn, mIn);
        LocalDateTime end = tra.atTime(hOut, mOut);

        // Sử dụng bộ máy tính giá thông minh
        double price = bangGiaDetailDAO.calculateStayPrice(p.getMaLoai(), start, end);

        // NẾU GIÁ BẰNG 0 (do chưa cấu hình bảng giá), LẤY GIÁ MẶC ĐỊNH TRONG BẢNG PHÒNG
        if (price <= 0) {
            long days = java.time.Duration.between(start, end).toDays();
            if (days <= 0) days = 1;
            return p.getGiaPhong() * days;
        }

        return price;
    }

    // ─── Validation ──────────────────────────────────────────────────────────

    private boolean validateStep1() {
        List<String> errors = new ArrayList<>();
        Control firstError = null;

        String sdt = txtSdt.getText().trim();
        if (sdt.isEmpty() || !sdt.matches("^0\\d{9,10}$")) {
            errors.add("- Số điện thoại không hợp lệ (phải bắt đầu bằng 0, 10-11 số).");
            setInvalidStyle(txtSdt);
            if (firstError == null) firstError = txtSdt;
        }

        if (txtTen.getText().trim().isEmpty()) {
            errors.add("- Vui lòng nhập tên khách hàng.");
            setInvalidStyle(txtTen);
            if (firstError == null) firstError = txtTen;
        }

        if (txtDiaChi.getText().trim().isEmpty()) {
            errors.add("- Vui lòng nhập địa chỉ.");
            setInvalidStyle(txtDiaChi);
            if (firstError == null) firstError = txtDiaChi;
        }

        LocalDate nhan = dpNgayNhan.getValue();
        LocalDate tra = dpNgayTra.getValue();
        int hIn = spGioNhan.getValue();
        int mIn = spPhutNhan.getValue();
        int hOut = spGioTra.getValue();
        int mOut = spPhutTra.getValue();

        LocalDateTime start = nhan.atTime(hIn, mIn);
        LocalDateTime end = tra.atTime(hOut, mOut);

        if (nhan == null) {
            errors.add("- Vui lòng chọn ngày nhận phòng.");
            setInvalidStyle(dpNgayNhan);
            if (firstError == null) firstError = dpNgayNhan;
        }

        if (tra == null || !end.isAfter(start)) {
            errors.add("- Thời gian trả phòng phải sau thời gian nhận phòng.");
            setInvalidStyle(dpNgayTra);
            if (firstError == null) firstError = dpNgayTra;
        }

        if (!errors.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Thông tin chưa hợp lệ");
            alert.setHeaderText("Vui lòng kiểm tra lại " + errors.size() + " mục:");
            alert.setContentText(String.join("\n", errors));
            final Control target = firstError;
            alert.showAndWait().ifPresent(r -> { if (target != null) target.requestFocus(); });
            return false;
        }
        return true;
    }

    // ─── Booking Logic ───────────────────────────────────────────────────────

    private void handleBooking(String trangThai) {
        if (selectedRooms.isEmpty()) {
            tvPhongTrong.setStyle("-fx-border-color: " + DANGER_COLOR + "; -fx-border-width: 2px;");
            showAlert("Thiếu thông tin", "Vui lòng chọn ít nhất 1 phòng trước khi xác nhận.");
            return;
        }
        tvPhongTrong.setStyle("");

        LocalDate nhan = dpNgayNhan.getValue();
        LocalDate tra = dpNgayTra.getValue();

        Task<Boolean> saveTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                if (currentKH == null || "NEW".equals(currentKH.getMaKhachHang())) {
                    String maKH = khService.generateNextId();
                    currentKH = new KhachHang(maKH, txtTen.getText(), txtDiaChi.getText(),
                            rbNam.isSelected(), txtSdt.getText());
                    khService.create(currentKH);
                } else {
                    currentKH.setTenKhachHang(txtTen.getText());
                    currentKH.setDiaChi(txtDiaChi.getText());
                    currentKH.setGioiTinh(rbNam.isSelected());
                    khService.update(currentKH);
                }

                String maDP = pdpDAO.generateNextId();
                PhieuDatPhong pdp = new PhieuDatPhong(maDP, LocalDateTime.now(), trangThai,
                        loggedInNhanVien, currentKH);

                int hIn = spGioNhan.getValue();
                int mIn = spPhutNhan.getValue();
                int hOut = spGioTra.getValue();
                int mOut = spPhutTra.getValue();

                List<ChiTietPhieuDat> dsCT = new ArrayList<>();
                for (Phong p : selectedRooms) {
                    dsCT.add(new ChiTietPhieuDat(
                            pdp, p, getGiaApDung(p),
                            LocalDateTime.of(nhan, LocalTime.of(hIn, mIn)),
                            LocalDateTime.of(tra, LocalTime.of(hOut, mOut))));
                }
                return pdpDAO.datPhong(pdp, dsCT);
            }
        };

        saveTask.setOnSucceeded(e -> {
            if (Boolean.TRUE.equals(saveTask.getValue())) {
                String msg = BookingStatus.DA_NHAN_PHONG.getCode().equals(trangThai)
                        ? "Check-in thành công! Khách đã nhận phòng."
                        : "Đã tạo phiếu đặt phòng thành công!";
                showAlert("Thành công", msg);
                clearAllAndReset();
            } else {
                showAlert("Lỗi", "Không thể lưu dữ liệu vào cơ sở dữ liệu!");
            }
        });

        saveTask.setOnFailed(ev -> showAlert("Lỗi hệ thống",
                "Có lỗi xảy ra: " + saveTask.getException().getMessage()));

        new Thread(saveTask).start();
    }

    // ─── Room Loading ────────────────────────────────────────────────────────

    public void loadPhongTrong() {
        if (isLoading) return;
        isLoading = true;

        LocalDate nhan = (dpNgayNhan != null) ? dpNgayNhan.getValue() : LocalDate.now();
        LocalDate tra = (dpNgayTra != null) ? dpNgayTra.getValue() : LocalDate.now().plusDays(1);

        int hIn = (spGioNhan != null) ? spGioNhan.getValue() : 14;
        int mIn = (spPhutNhan != null) ? spPhutNhan.getValue() : 0;
        int hOut = (spGioTra != null) ? spGioTra.getValue() : 12;
        int mOut = (spPhutTra != null) ? spPhutTra.getValue() : 0;

        final LocalDateTime start = (nhan != null) ? nhan.atTime(hIn, mIn) : LocalDateTime.now();
        final LocalDateTime end = (tra != null) ? tra.atTime(hOut, mOut) : LocalDateTime.now().plusDays(1);

        Task<List<Phong>> task = new Task<List<Phong>>() {
            @Override
            protected List<Phong> call() throws Exception {
                return new PhongDAO().findAvailableRooms(start, end);
            }
        };

        task.setOnSucceeded(e -> {
            isLoading = false;
            List<Phong> list = task.getValue();
            if (list != null) {
                allPhongTrong.setAll(list);
                if (filteredPhong != null) {
                    populateFilterCombos(list);
                    applyFilters();
                }
                if (tvPhongTrong != null) tvPhongTrong.refresh();
            }
        });

        task.setOnFailed(ev -> {
            isLoading = false;
            Throwable ex = task.getException();
            if (ex != null) ex.printStackTrace();
        });

        new Thread(task).start();
    }

    // ─── Selected Rooms Render ────────────────────────────────────────────────

    private void renderSelectedRooms() {
        selectedRoomsContainer.getChildren().clear();

        if (selectedRooms.isEmpty()) {
            Label hint = new Label("Chưa chọn phòng nào.\nNhấp vào phòng bên trái để chọn.");
            hint.setTextFill(Color.web("#aaa"));
            hint.setFont(Font.font("Segoe UI", 13));
            hint.setWrapText(true);
            selectedRoomsContainer.getChildren().add(hint);
            return;
        }

        for (Phong p : selectedRooms) {
            HBox card = new HBox(12);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(10, 16, 10, 16));
            card.setStyle("-fx-background-color: #f0f4ff; -fx-background-radius: 8;" +
                    "-fx-border-color: #c5d3f7; -fx-border-radius: 8;");

            VBox info = new VBox(2);
            Label lblMa = new Label(p.getMaPhong() + " - " + p.getLoaiPhong());
            lblMa.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));

            double total = getGiaApDung(p);
            Label lblPriceTotal = new Label("Tổng cộng: " + String.format("%,.0f VNĐ", total));
            lblPriceTotal.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
            lblPriceTotal.setTextFill(Color.web(PRIMARY_COLOR));

            Label lblInfo = new Label(p.getSoGiuong() + " giường  •  " + p.getSucChua() + " người");
            lblInfo.setTextFill(Color.GRAY);
            lblInfo.setFont(Font.font("Segoe UI", 12));

            info.getChildren().addAll(lblMa, lblPriceTotal, lblInfo);
            HBox.setHgrow(info, Priority.ALWAYS);

            Button btnRemove = new Button("✕");
            btnRemove.setMinWidth(28);
            btnRemove.setMinHeight(28);
            btnRemove.setStyle("-fx-background-color: " + DANGER_COLOR + "; -fx-text-fill: white;" +
                    "-fx-font-weight: bold; -fx-background-radius: 14; -fx-cursor: hand; -fx-padding: 0;");
            btnRemove.setVisible(false);
            card.setOnMouseEntered(e -> btnRemove.setVisible(true));
            card.setOnMouseExited(e -> btnRemove.setVisible(false));
            btnRemove.setOnAction(e -> {
                selectedRooms.remove(p);
                tvPhongTrong.refresh();
                renderSelectedRooms();
            });

            card.getChildren().addAll(info, btnRemove);
            selectedRoomsContainer.getChildren().add(card);
        }
    }

    // ─── Customer Search ─────────────────────────────────────────────────────

    private void searchKhachHang(String sdt) {
        if (searchTask != null && searchTask.isRunning()) searchTask.cancel();

        searchTask = new Task<KhachHang>() {
            @Override
            protected KhachHang call() throws Exception {
                return khService.getAll().stream()
                        .filter(kh -> kh.getSoDienThoai().equals(sdt))
                        .findFirst().orElse(null);
            }
        };

        searchTask.setOnSucceeded(e -> {
            if (!sdt.equals(txtSdt.getText())) return;
            currentKH = searchTask.getValue();
            if (currentKH != null) {
                txtTen.setText(currentKH.getTenKhachHang());
                txtDiaChi.setText(currentKH.getDiaChi());
                if (currentKH.isGioiTinh()) rbNam.setSelected(true);
                else rbNu.setSelected(true);
                txtTen.setEditable(true);
            } else {
                txtTen.clear();
                txtDiaChi.clear();
                txtTen.setEditable(true);
            }
        });

        new Thread(searchTask).start();
    }

    private void showCustomerSearchPopup() {
        try {
            javafx.stage.Window owner = txtSdt.getScene().getWindow();
            CustomerSearchDialog dialog = new CustomerSearchDialog(owner, khService);
            KhachHang selected = dialog.show();
            if (selected != null) {
                isAutoFilling = true;
                currentKH = selected;
                txtSdt.setText(selected.getSoDienThoai());
                txtTen.setText(selected.getTenKhachHang());
                txtDiaChi.setText(selected.getDiaChi());
                if (selected.isGioiTinh()) rbNam.setSelected(true);
                else rbNu.setSelected(true);

                txtSdt.setStyle("-fx-background-radius: 6; -fx-border-color: #27ae60;" +
                        "-fx-border-width: 2px; -fx-border-radius: 6;");
                javafx.animation.PauseTransition pause =
                        new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
                pause.setOnFinished(e -> { resetStyle(txtSdt); isAutoFilling = false; });
                pause.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Không thể mở cửa sổ tìm kiếm!\nChi tiết: " + e);
        }
    }

    // ─── Public API ──────────────────────────────────────────────────────────

    public void preSelectRoom(Phong p) {
        clearAllAndReset();
        loadPhongTrong();
        javafx.application.Platform.runLater(() -> {
            if (p != null) {
                selectedRooms.add(p);
                renderSelectedRooms();
                if (tvPhongTrong != null) tvPhongTrong.refresh();
            }
        });
    }

    public void preFillCustomer(KhachHang kh) {
        clearAllAndReset();
        loadPhongTrong();
        javafx.application.Platform.runLater(() -> {
            if (kh != null) {
                currentKH = kh;
                txtSdt.setText(kh.getSoDienThoai());
                txtTen.setText(kh.getTenKhachHang());
                txtDiaChi.setText(kh.getDiaChi());
                if (kh.isGioiTinh()) rbNam.setSelected(true);
                else rbNu.setSelected(true);
            }
        });
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void clearAllAndReset() {
        txtSdt.clear();
        txtTen.clear();
        txtDiaChi.clear();
        resetStyle(txtSdt);
        resetStyle(txtTen);
        resetStyle(txtDiaChi);
        resetStyle(dpNgayNhan);
        resetStyle(dpNgayTra);
        dpNgayNhan.setValue(LocalDate.now());
        dpNgayTra.setValue(LocalDate.now().plusDays(1));
        selectedRooms.clear();
        currentKH = null;
        renderSelectedRooms();
        if (tvPhongTrong != null) {
            tvPhongTrong.setStyle("");
            tvPhongTrong.refresh();
        }
        clearFilters();
        goToStep1();
        loadPhongTrong();
        lookupBangGia();
    }

    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefHeight(40);
        tf.setStyle("-fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
        return tf;
    }

    private void resetStyle(Control c) {
        c.setStyle("-fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");
    }

    private Spinner<Integer> createTimeSpinner(int max, int current) {
        Spinner<Integer> spinner = new Spinner<>(0, max, current);
        spinner.setEditable(true);
        spinner.setPrefHeight(40);
        spinner.setPrefWidth(70);
        spinner.setStyle("-fx-background-radius: 6; -fx-border-color: #dcdde1; -fx-border-radius: 6;");

        // Cập nhật khi giá trị thay đổi
        spinner.valueProperty().addListener((obs, oldV, newV) -> onDatesChanged());

        // Hỗ trợ nhập liệu trực tiếp từ bàn phím
        spinner.getEditor().textProperty().addListener((obs, oldV, newVal) -> {
            if (!newVal.matches("\\d*")) {
                spinner.getEditor().setText(oldV);
            }
        });

        return spinner;
    }

    private void setInvalidStyle(Control c) {
        c.setStyle("-fx-background-radius: 6; -fx-border-color: " + DANGER_COLOR +
                "; -fx-border-radius: 6; -fx-border-width: 2px;");
    }

    private void showAlert(String title, String content) {
        Alert.AlertType type = title.contains("Lỗi") || title.contains("Thiếu")
                ? Alert.AlertType.ERROR
                : Alert.AlertType.INFORMATION;
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
