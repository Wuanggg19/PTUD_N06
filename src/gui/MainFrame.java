package gui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import javafx.stage.Stage;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.NhanVien;

public class MainFrame extends JFrame {

    private static final String PRIMARY_COLOR      = "#2c0fbd";
    private static final String SIDEBAR_HOVER      = "#3d21c9";
    private static final String ACTIVE_COLOR       = "#5839ff";
    private static final String BG_LIGHT           = "#f6f6f8";
    private static final String GROUP_HEADER_COLOR = "rgba(255,255,255,0.13)";

    private static final String FONT_STYLE   = "-fx-font-size: 14px; -fx-font-family: 'Inter'; ";
    private static final String COMMON_STYLE = "-fx-cursor: hand; -fx-background-radius: 6; -fx-alignment: center-left; ";

    private final JFXPanel jfxPanel  = new JFXPanel();
    private final List<Button>          menuButtons = new ArrayList<>();
    private final Map<String, StackPane> cardMap    = new HashMap<>();

    // ===== Views đã hoàn thiện =====
    private DashboardView      dashboardView;
    private RoomView           roomView;
    private RoomManagementView roomManagementView;
    private BookingView        bookingView;
    private CheckoutView       checkoutView;
    private CustomerView       customerView;

    // ===== Skeleton Views (cần implement) =====
    private BookingListView    bookingListView;
    private CheckInView        checkInView;
    private ServiceView        serviceView;
    private PricingView        pricingView;
    private PaymentView        paymentView;
    private InvoiceView        invoiceView;
    private RevenueStatsView   revenueStatsView;
    private RoomStatsView      roomStatsView;
    private CustomerStatsView  customerStatsView;
    private AccountView        accountView;
    private ChangePasswordView changePasswordView;

    private Label    lblClock;
    private Timeline clockTimeline;
    private final NhanVien loggedInNhanVien;

    public MainFrame(NhanVien loggedInNhanVien) {
        this.loggedInNhanVien = loggedInNhanVien;
        setTitle("Hệ Thống Quản Lý Khách Sạn - Nhóm 06");
        setSize(1400, 870);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        add(jfxPanel, BorderLayout.CENTER);

        Platform.runLater(() -> {
            try {
                javafx.scene.Parent root = createFxUI();
                jfxPanel.setScene(new Scene(root, 1400, 870));
                startClockFx();
                System.out.println("DEBUG: JavaFX UI Initialized Successfully.");
            } catch (Throwable t) {
                System.err.println("FATAL UI ERROR: Failed to create JavaFX UI.");
                t.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(this,
                        "Lỗi khởi tạo giao diện: " + t.getMessage(),
                        "Lỗi Hệ Thống", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void showDashboardWithDelay() {
        if (!menuButtons.isEmpty()) {
            setButtonActive(menuButtons.get(0));
            // Force show Dashboard immediately if possible
            Platform.runLater(() -> {
                try { 
                    showCard("Dashboard");
                    System.out.println("DEBUG: Auto-showing Dashboard...");
                } catch (Exception e) { e.printStackTrace(); }
            });
        }
    }

    private javafx.scene.Parent createFxUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        String role = (loggedInNhanVien != null) ? loggedInNhanVien.getVaiTro() : "User";
        String name = (loggedInNhanVien != null) ? loggedInNhanVien.getTenNhanVien() : "Nhân viên";

        // ===== SIDEBAR =====
        VBox sidebar = new VBox();
        sidebar.setPrefWidth(270);
        sidebar.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");

        // --- Logo & User info ---
        VBox top = new VBox(6);
        top.setPadding(new Insets(28, 20, 18, 20));
        Label lblLogo = new Label("🏨  HOTEL SYSTEM");
        lblLogo.setTextFill(javafx.scene.paint.Color.WHITE);
        lblLogo.setFont(javafx.scene.text.Font.font("Inter", javafx.scene.text.FontWeight.BOLD, 20));

        Label lblRole = new Label(name + "  ·  " + role);
        lblRole.setTextFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.65));
        lblRole.setFont(javafx.scene.text.Font.font("Inter", 12));

        lblClock = new Label();
        lblClock.setTextFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.85));
        lblClock.setFont(Font.font("Inter", javafx.scene.text.FontWeight.BOLD, 13));
        top.getChildren().addAll(lblLogo, lblRole, lblClock);

        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.15);");

        // --- Menu theo cấu trúc yêu cầu ---
        VBox menu = new VBox(3);
        menu.setPadding(new Insets(12, 8, 12, 8));
        javafx.scene.layout.VBox.setVgrow(menu, javafx.scene.layout.Priority.ALWAYS);

        // 🏠 Trang chủ / Tổng quan
        menu.getChildren().add(createMenuButton("🏠  Tổng quan", "Dashboard"));

        // 🛎 Tiếp tân - Chức năng chính: Sơ đồ, Đặt phòng, Trả phòng
        menu.getChildren().add(createGroupSection("🛎  LỄ TÂN", true,
                new String[]{"Sơ đồ phòng",        "Room"},
                new String[]{"Đặt phòng mới",      "Booking"},
                new String[]{"Trả phòng",           "Checkout"},
                new String[]{"Danh sách đặt phòng", "BookingList"},
                new String[]{"Check-in",            "CheckIn"}));

        // 🛏 Quản lý khách sạn & Dịch vụ
        menu.getChildren().add(createGroupSection("🛏  QUẢN LÝ", false,
                new String[]{"Danh mục phòng",      "RoomMgmt"},
                new String[]{"Quản lý dịch vụ",     "Service"},
                new String[]{"Quản lý khách hàng",   "Customer"},
                new String[]{"Bảng giá niêm yết",    "Pricing"}));

        // 💰 Tài chính & Hóa đơn
        menu.getChildren().add(createGroupSection("💰  TÀI CHÍNH", false,
                new String[]{"Thanh toán", "Payment"},
                new String[]{"Hóa đơn",   "Invoice"}));

        // 📊 Thống kê & Báo cáo
        menu.getChildren().add(createGroupSection("📊  THỐNG KÊ", false,
                new String[]{"Thống kê doanh thu",   "RevenueStats"},
                new String[]{"Thống kê phòng",       "RoomStats"},
                new String[]{"Thống kê khách hàng",  "CustomerStats"}));

        // ⚙ Hệ thống
        menu.getChildren().add(createGroupSection("⚙  HỆ THỐNG", false,
                new String[]{"Tài khoản",      "Account"},
                new String[]{"Đổi mật khẩu",  "ChangePassword"}));

        // --- Nút Đăng xuất ---
        VBox bottom = new VBox(16);
        bottom.setPadding(new Insets(16, 16, 20, 16));
        bottom.setAlignment(Pos.CENTER);
        Button btnLogout = new Button("  🚪  Đăng xuất");
        btnLogout.setPrefSize(238, 44);
        btnLogout.setFont(javafx.scene.text.Font.font("Inter", javafx.scene.text.FontWeight.BOLD, 13));
        String logoutStyle = "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-alignment: center-left;";
        btnLogout.setStyle(logoutStyle);
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-weight: bold; -fx-alignment: center-left;"));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(logoutStyle));
        btnLogout.setOnAction(e -> handleLogout());
        bottom.getChildren().add(btnLogout);

        sidebar.getChildren().addAll(top, sep, menu, bottom);

        // ===== NỘI DUNG CHÍNH =====
        StackPane cards = new StackPane();
        initializeViews(cards);

        root.setLeft(sidebar);
        root.setCenter(cards);

        showDashboardWithDelay();
        return root;
    }

    private VBox createGroupSection(String groupTitle, boolean startExpanded, String[]... items) {
        VBox section = new VBox(0);

        VBox subItems = new VBox(2);
        subItems.setPadding(new Insets(2, 0, 4, 18));
        subItems.setVisible(startExpanded);
        subItems.setManaged(startExpanded);

        for (String[] item : items) {
            subItems.getChildren().add(createMenuButton("· " + item[0], item[1]));
        }

        boolean[] expanded = {startExpanded};
        Button header = new Button((startExpanded ? "▼  " : "▶  ") + groupTitle);
        header.setPrefWidth(254);
        header.setMinHeight(40);
        header.setMaxWidth(Double.MAX_VALUE);
        header.setFont(Font.font("Inter", javafx.scene.text.FontWeight.BOLD, 12));
        String headerNormal = "-fx-background-color: " + GROUP_HEADER_COLOR + "; " +
                "-fx-text-fill: rgba(255,255,255,0.85); -fx-cursor: hand; " +
                "-fx-background-radius: 8; -fx-alignment: center-left; -fx-padding: 0 0 0 12;";
        String headerHover = "-fx-background-color: rgba(255,255,255,0.22); " +
                "-fx-text-fill: white; -fx-cursor: hand; " +
                "-fx-background-radius: 8; -fx-alignment: center-left; -fx-padding: 0 0 0 12;";
        header.setStyle(headerNormal);
        header.setOnMouseEntered(e -> header.setStyle(headerHover));
        header.setOnMouseExited(e -> header.setStyle(headerNormal));
        header.setOnAction(e -> {
            expanded[0] = !expanded[0];
            subItems.setVisible(expanded[0]);
            subItems.setManaged(expanded[0]);
            header.setText((expanded[0] ? "▼  " : "▶  ") + groupTitle);
        });

        section.getChildren().addAll(header, subItems);
        return section;
    }

    private void initializeViews(StackPane cards) {
        try {
            System.out.println("DEBUG: Starting View Initialization...");

            // --- Khởi tạo views đã hoàn thiện ---
            dashboardView      = new DashboardView();
            roomView           = new RoomView();
            roomManagementView = new RoomManagementView();
            bookingView        = new BookingView(loggedInNhanVien);
            checkoutView       = new CheckoutView(loggedInNhanVien);
            customerView       = new CustomerView();

            // --- Khởi tạo skeleton views ---
            bookingListView    = new BookingListView();
            checkInView        = new CheckInView();
            serviceView        = new ServiceView();
            pricingView        = new PricingView();
            paymentView        = new PaymentView();
            invoiceView        = new InvoiceView();
            revenueStatsView   = new RevenueStatsView();
            roomStatsView      = new RoomStatsView();
            customerStatsView  = new CustomerStatsView();
            accountView        = new AccountView();
            changePasswordView = new ChangePasswordView(loggedInNhanVien);

            // --- Cài đặt callback từ roomView ---
            if (roomView != null) {
                roomView.setOnBookingRequest(p -> {
                    showCard("Booking");
                    if (bookingView != null) {
                        // Implement pre-select room logic if needed
                    }
                });
            }

            // --- Đăng ký views đã hoàn thiện ---
            safeAddCard(cards, "Dashboard",  dashboardView.createView());
            safeAddCard(cards, "Room",       roomView.createView());
            safeAddCard(cards, "RoomMgmt",   roomManagementView.createView());
            safeAddCard(cards, "Booking",    bookingView.createView());
            safeAddCard(cards, "Checkout",   checkoutView.createView());
            safeAddCard(cards, "Customer",   customerView.createView());

            // --- Đăng ký skeleton views ---
            safeAddCard(cards, "BookingList",    bookingListView.createView());
            safeAddCard(cards, "CheckIn",        checkInView.createView());
            safeAddCard(cards, "Service",        serviceView.createView());
            safeAddCard(cards, "Pricing",        pricingView.createView());
            safeAddCard(cards, "Payment",        paymentView.createView());
            safeAddCard(cards, "Invoice",        invoiceView.createView());
            safeAddCard(cards, "RevenueStats",   revenueStatsView.createView());
            safeAddCard(cards, "RoomStats",      roomStatsView.createView());
            safeAddCard(cards, "CustomerStats",  customerStatsView.createView());
            safeAddCard(cards, "Account",        accountView.createView());
            safeAddCard(cards, "ChangePassword", changePasswordView.createView());

            // Ẩn tất cả lúc đầu
            for (StackPane p : cardMap.values()) {
                p.setVisible(false);
                p.setManaged(false);
                cards.getChildren().add(p);
            }

            System.out.println("DEBUG: View Initialization Completed. Cards: " + cardMap.keySet());
        } catch (Throwable t) {
            System.err.println("CRITICAL ERROR: Major failure in view initialization.");
            t.printStackTrace();
        }
    }

    /** Thêm card an toàn: nếu createView() ném lỗi thì thay bằng placeholder. */
    private void safeAddCard(StackPane cards, String key, javafx.scene.Node node) {
        try {
            cardMap.put(key, wrapInStackPane(node));
        } catch (Throwable t) {
            System.err.println("ERROR: View [" + key + "] failed to create. Using placeholder.");
            t.printStackTrace();
            cardMap.put(key, createPlaceholderCard("⚠  " + key, "Lỗi khởi tạo view. Kiểm tra console."));
        }
    }

    private StackPane wrapInStackPane(javafx.scene.Node node) {
        return new StackPane(node);
    }

    private void showCard(String cardName) {
        cardMap.forEach((name, pane) -> {
            boolean active = name.equals(cardName);
            pane.setVisible(active);
            pane.setManaged(active);
        });

        // Gọi refresh khi chuyển màn
        try {
            switch (cardName) {
                case "Dashboard"    -> { if (dashboardView     != null) dashboardView.refreshData(); }
                case "Room"         -> { if (roomView          != null) roomView.loadData(); }
                case "RoomMgmt"     -> { if (roomManagementView!= null) roomManagementView.loadData(); }
                case "Booking"      -> { if (bookingView       != null) bookingView.loadPhongTrong(); }
                case "BookingList"  -> { if (bookingListView   != null) bookingListView.loadData(); }
                case "CheckIn"      -> { if (checkInView       != null) checkInView.loadData(); }
                case "Checkout"     -> { if (checkoutView      != null) checkoutView.loadData(); }
                case "Customer"     -> { if (customerView      != null) customerView.loadData(); }
                case "Service"      -> { if (serviceView       != null) serviceView.loadData(); }
                case "Pricing"      -> { if (pricingView       != null) pricingView.loadData(); }
                case "Payment"      -> { if (paymentView       != null) paymentView.loadData(); }
                case "Invoice"      -> { if (invoiceView       != null) invoiceView.loadData(); }
                case "Account"      -> { if (accountView       != null) accountView.loadData(); }
            }
        } catch (Exception e) {
            System.err.println("WARNING: Failed to refresh data for card: " + cardName);
            e.printStackTrace();
        }

        // Highlight nút active
        for (Button btn : menuButtons) {
            if (cardName.equals(btn.getUserData())) {
                setButtonActive(btn);
            }
        }
    }

    private Button createMenuButton(String text, String cardName) {
        Button btn = new Button("  " + text);
        btn.setPrefWidth(238);
        btn.setMinHeight(38);
        btn.setUserData(cardName);

        String normalStyle = FONT_STYLE + COMMON_STYLE +
                "-fx-background-color: transparent; -fx-text-fill: rgb(210, 218, 236); -fx-font-weight: normal;";
        btn.setStyle(normalStyle);

        btn.setOnMouseEntered(e -> {
            if (!ACTIVE_COLOR.equals(btn.getProperties().get("state"))) {
                btn.setStyle(FONT_STYLE + COMMON_STYLE +
                        "-fx-background-color: " + SIDEBAR_HOVER + "; -fx-text-fill: white; -fx-font-weight: normal;");
            }
        });
        btn.setOnMouseExited(e -> {
            if (!ACTIVE_COLOR.equals(btn.getProperties().get("state"))) {
                btn.setStyle(normalStyle);
            }
        });
        btn.setOnAction(e -> {
            setButtonActive(btn);
            showCard(cardName);
        });

        menuButtons.add(btn);
        return btn;
    }

    private void setButtonActive(Button selectedBtn) {
        for (Button btn : menuButtons) {
            if (btn == selectedBtn) {
                btn.setStyle(FONT_STYLE + COMMON_STYLE +
                        "-fx-background-color: " + ACTIVE_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.getProperties().put("state", ACTIVE_COLOR);
            } else {
                btn.setStyle(FONT_STYLE + COMMON_STYLE +
                        "-fx-background-color: transparent; -fx-text-fill: rgb(210, 218, 236); -fx-font-weight: normal;");
                btn.getProperties().remove("state");
            }
        }
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có muốn đăng xuất?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xác nhận đăng xuất");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(type -> {
            if (type == javafx.scene.control.ButtonType.YES) {
                javafx.application.Platform.runLater(() -> {
                    javafx.stage.Stage stage = new javafx.stage.Stage();
                    LoginFrame login = new LoginFrame();
                    javafx.scene.Scene scene = login.createScene(stage);
                    stage.setScene(scene);
                    stage.show();
                    javax.swing.SwingUtilities.invokeLater(this::dispose);
                });
            }
        });
    }

    private StackPane createPlaceholderCard(String title, String subtitle) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        Label lbl = new Label(title);
        lbl.setFont(javafx.scene.text.Font.font("Segoe UI", javafx.scene.text.FontWeight.BOLD, 28));
        lbl.setTextFill(javafx.scene.paint.Color.web("#bdc3c7"));
        Label lbl2 = new Label(subtitle);
        lbl2.setFont(javafx.scene.text.Font.font("Segoe UI", 16));
        lbl2.setTextFill(javafx.scene.paint.Color.web("#d5d8dc"));
        box.getChildren().addAll(lbl, lbl2);
        StackPane sp = new StackPane(box);
        sp.setStyle("-fx-background-color: " + BG_LIGHT + ";");
        return sp;
    }

    private void startClockFx() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm:ss  dd/MM/yyyy");
        clockTimeline = new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
            if (lblClock != null) lblClock.setText(sdf.format(new java.util.Date()));
        }));
        clockTimeline.setCycleCount(javafx.animation.Timeline.INDEFINITE);
        clockTimeline.play();
    }
}
