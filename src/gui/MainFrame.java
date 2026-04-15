package gui;

import entity.NhanVien;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainFrame extends JFrame {

    private static final String PRIMARY_COLOR = "#2c0fbd";
    private static final String SIDEBAR_HOVER = "#3d21c9";
    private static final String ACTIVE_COLOR = "#5839ff";
    private static final String BG_LIGHT = "#f6f6f8";
    private static final String GROUP_HEADER_COLOR = "rgba(255,255,255,0.13)";

    private static final String FONT_STYLE = "-fx-font-size: 14px; -fx-font-family: 'Inter'; ";
    private static final String COMMON_STYLE = "-fx-cursor: hand; -fx-background-radius: 6; -fx-alignment: center-left; ";

    private final JFXPanel jfxPanel = new JFXPanel();
    private final List<Button> menuButtons = new ArrayList<>();
    private final Map<String, StackPane> cardMap = new HashMap<>();
    private final Set<String> initializedCards = new HashSet<>();
    private final NhanVien loggedInNhanVien;

    private StackPane cardsContainer;

    private DashboardView dashboardView;
    private RoomView roomView;
    private RoomManagementView roomManagementView;
    private BookingView bookingView;
    private CheckoutView checkoutView;
    private CustomerView customerView;
    private BookingListView bookingListView;
    private CheckInView checkInView;
    private ServiceView serviceView;
    private PricingView pricingView;
    private PaymentView paymentView;
    private InvoiceView invoiceView;
    private RevenueStatsView revenueStatsView;
    private RoomStatsView roomStatsView;
    private CustomerStatsView customerStatsView;
    private AccountView accountView;
    private ChangePasswordView changePasswordView;

    private Label lblClock;
    private Timeline clockTimeline;

    public MainFrame(NhanVien loggedInNhanVien) {
        this.loggedInNhanVien = loggedInNhanVien;
        setTitle("He thong Quan Ly Khach San - Nhom 06");
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
            } catch (Throwable t) {
                t.printStackTrace();
                javax.swing.JOptionPane.showMessageDialog(
                        this,
                        "Loi khoi tao giao dien: " + t.getMessage(),
                        "Loi He Thong",
                        javax.swing.JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }

    private javafx.scene.Parent createFxUI() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BG_LIGHT + ";");

        String role = loggedInNhanVien != null ? loggedInNhanVien.getVaiTro() : "User";
        String name = loggedInNhanVien != null ? loggedInNhanVien.getTenNhanVien() : "Nhan vien";

        VBox sidebar = new VBox();
        sidebar.setPrefWidth(270);
        sidebar.setStyle("-fx-background-color: " + PRIMARY_COLOR + ";");

        VBox top = new VBox(6);
        top.setPadding(new Insets(28, 20, 18, 20));
        Label lblLogo = new Label("HOTEL SYSTEM");
        lblLogo.setTextFill(javafx.scene.paint.Color.WHITE);
        lblLogo.setFont(Font.font("Inter", FontWeight.BOLD, 20));

        Label lblRole = new Label(name + "  ·  " + role);
        lblRole.setTextFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.65));
        lblRole.setFont(Font.font("Inter", 12));

        lblClock = new Label();
        lblClock.setTextFill(javafx.scene.paint.Color.rgb(255, 255, 255, 0.85));
        lblClock.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        top.getChildren().addAll(lblLogo, lblRole, lblClock);

        javafx.scene.control.Separator sep = new javafx.scene.control.Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.15);");

        VBox menu = new VBox(3);
        menu.setPadding(new Insets(12, 8, 12, 8));
        VBox.setVgrow(menu, javafx.scene.layout.Priority.ALWAYS);

        menu.getChildren().add(createMenuButton("Tong quan", "Dashboard"));
        menu.getChildren().add(createGroupSection("QUAN LY DAT PHONG", true,
                new String[]{"Dat phong", "Booking"},
                new String[]{"Danh sach dat phong", "BookingList"},
                new String[]{"Check-in", "CheckIn"},
                new String[]{"Check-out", "Checkout"}));
        menu.getChildren().add(createGroupSection("QUAN LY KHACH SAN", false,
                new String[]{"Phong", "Room"},
                new String[]{"Loai phong", "RoomMgmt"},
                new String[]{"Dich vu", "Service"},
                new String[]{"Bang gia", "Pricing"}));
        menu.getChildren().add(createGroupSection("KHACH HANG", false,
                new String[]{"Khach hang", "Customer"}));
        menu.getChildren().add(createGroupSection("INVOICE", false,
                new String[]{"Thanh toan", "Payment"},
                new String[]{"Hoa don", "Invoice"}));
        menu.getChildren().add(createGroupSection("BAO CAO", false,
                new String[]{"Thong ke doanh thu", "RevenueStats"},
                new String[]{"Thong ke phong", "RoomStats"},
                new String[]{"Thong ke khach hang", "CustomerStats"}));
        menu.getChildren().add(createGroupSection("HE THONG", false,
                new String[]{"Tai khoan", "Account"},
                new String[]{"Doi mat khau", "ChangePassword"}));

        VBox bottom = new VBox(16);
        bottom.setPadding(new Insets(16, 16, 20, 16));
        bottom.setAlignment(Pos.CENTER);
        Button btnLogout = new Button("Dang xuat");
        btnLogout.setPrefSize(238, 44);
        btnLogout.setFont(Font.font("Inter", FontWeight.BOLD, 13));
        String logoutStyle = "-fx-background-color: rgba(255,255,255,0.15); -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-alignment: center-left;";
        btnLogout.setStyle(logoutStyle);
        btnLogout.setOnMouseEntered(e -> btnLogout.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 8; -fx-font-weight: bold; -fx-alignment: center-left;"));
        btnLogout.setOnMouseExited(e -> btnLogout.setStyle(logoutStyle));
        btnLogout.setOnAction(e -> handleLogout());
        bottom.getChildren().add(btnLogout);

        sidebar.getChildren().addAll(top, sep, menu, bottom);

        cardsContainer = new StackPane();
        initializeViews();

        root.setLeft(sidebar);
        root.setCenter(cardsContainer);
        showCard("Dashboard");
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
        header.setFont(Font.font("Inter", FontWeight.BOLD, 12));
        String headerNormal = "-fx-background-color: " + GROUP_HEADER_COLOR + "; "
                + "-fx-text-fill: rgba(255,255,255,0.85); -fx-cursor: hand; "
                + "-fx-background-radius: 8; -fx-alignment: center-left; -fx-padding: 0 0 0 12;";
        String headerHover = "-fx-background-color: rgba(255,255,255,0.22); "
                + "-fx-text-fill: white; -fx-cursor: hand; "
                + "-fx-background-radius: 8; -fx-alignment: center-left; -fx-padding: 0 0 0 12;";
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

    private void initializeViews() {
        ensureCardInitialized("Dashboard");
    }

    private void ensureCardInitialized(String cardName) {
        if (initializedCards.contains(cardName)) {
            return;
        }

        StackPane pane;
        try {
            pane = new StackPane(createViewNode(cardName));
        } catch (Throwable t) {
            t.printStackTrace();
            pane = createPlaceholderCard("Canh bao: " + cardName, "Khoi tao view that bai.");
        }

        pane.setVisible(false);
        pane.setManaged(false);
        cardsContainer.getChildren().add(pane);
        cardMap.put(cardName, pane);
        initializedCards.add(cardName);
    }

    private javafx.scene.Node createViewNode(String cardName) {
        return switch (cardName) {
            case "Dashboard" -> {
                if (dashboardView == null) {
                    dashboardView = new DashboardView();
                }
                yield dashboardView.createView();
            }
            case "Room" -> {
                if (roomView == null) {
                    roomView = new RoomView();
                    roomView.setOnBookingRequest(room -> {
                        showCard("Booking");
                        if (bookingView != null) {
                            bookingView.preSelectRoom(room);
                        }
                    });
                }
                yield roomView.createView();
            }
            case "RoomMgmt" -> {
                if (roomManagementView == null) {
                    roomManagementView = new RoomManagementView();
                }
                yield roomManagementView.createView();
            }
            case "Booking" -> {
                if (bookingView == null) {
                    bookingView = new BookingView(loggedInNhanVien);
                }
                yield bookingView.createView();
            }
            case "Checkout" -> {
                if (checkoutView == null) {
                    checkoutView = new CheckoutView(loggedInNhanVien);
                }
                yield checkoutView.createView();
            }
            case "Customer" -> {
                if (customerView == null) {
                    customerView = new CustomerView();
                }
                yield customerView.createView();
            }
            case "BookingList" -> {
                if (bookingListView == null) {
                    bookingListView = new BookingListView();
                }
                yield bookingListView.createView();
            }
            case "CheckIn" -> {
                if (checkInView == null) {
                    checkInView = new CheckInView();
                }
                yield checkInView.createView();
            }
            case "Service" -> {
                if (serviceView == null) {
                    serviceView = new ServiceView();
                }
                yield serviceView.createView();
            }
            case "Pricing" -> {
                if (pricingView == null) {
                    pricingView = new PricingView();
                }
                yield pricingView.createView();
            }
            case "Payment" -> {
                if (paymentView == null) {
                    paymentView = new PaymentView();
                }
                yield paymentView.createView();
            }
            case "Invoice" -> {
                if (invoiceView == null) {
                    invoiceView = new InvoiceView();
                }
                yield invoiceView.createView();
            }
            case "RevenueStats" -> {
                if (revenueStatsView == null) {
                    revenueStatsView = new RevenueStatsView();
                }
                yield revenueStatsView.createView();
            }
            case "RoomStats" -> {
                if (roomStatsView == null) {
                    roomStatsView = new RoomStatsView();
                }
                yield roomStatsView.createView();
            }
            case "CustomerStats" -> {
                if (customerStatsView == null) {
                    customerStatsView = new CustomerStatsView();
                }
                yield customerStatsView.createView();
            }
            case "Account" -> {
                if (accountView == null) {
                    accountView = new AccountView();
                }
                yield accountView.createView();
            }
            case "ChangePassword" -> {
                if (changePasswordView == null) {
                    changePasswordView = new ChangePasswordView(loggedInNhanVien);
                }
                yield changePasswordView.createView();
            }
            default -> createPlaceholderCard("Khong ho tro", "Khong tim thay view tuong ung.");
        };
    }

    private void showCard(String cardName) {
        ensureCardInitialized(cardName);

        cardMap.forEach((name, pane) -> {
            boolean active = name.equals(cardName);
            pane.setVisible(active);
            pane.setManaged(active);
        });

        try {
            switch (cardName) {
                case "Dashboard" -> {
                    if (dashboardView != null) dashboardView.refreshData();
                }
                case "Room" -> {
                    if (roomView != null) roomView.loadData();
                }
                case "RoomMgmt" -> {
                    if (roomManagementView != null) roomManagementView.loadData();
                }
                case "Booking" -> {
                    if (bookingView != null) bookingView.loadPhongTrong();
                }
                case "BookingList" -> {
                    if (bookingListView != null) bookingListView.loadData();
                }
                case "CheckIn" -> {
                    if (checkInView != null) checkInView.loadData();
                }
                case "Checkout" -> {
                    if (checkoutView != null) checkoutView.loadData();
                }
                case "Customer" -> {
                    if (customerView != null) customerView.loadData();
                }
                case "Service" -> {
                    if (serviceView != null) serviceView.loadData();
                }
                case "Pricing" -> {
                    if (pricingView != null) pricingView.loadData();
                }
                case "Payment" -> {
                    if (paymentView != null) paymentView.loadData();
                }
                case "Invoice" -> {
                    if (invoiceView != null) invoiceView.loadData();
                }
                case "Account" -> {
                    if (accountView != null) accountView.loadData();
                }
                default -> {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        String normalStyle = FONT_STYLE + COMMON_STYLE
                + "-fx-background-color: transparent; -fx-text-fill: rgb(210, 218, 236); -fx-font-weight: normal;";
        btn.setStyle(normalStyle);

        btn.setOnMouseEntered(e -> {
            if (!ACTIVE_COLOR.equals(btn.getProperties().get("state"))) {
                btn.setStyle(FONT_STYLE + COMMON_STYLE
                        + "-fx-background-color: " + SIDEBAR_HOVER + "; -fx-text-fill: white; -fx-font-weight: normal;");
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
                btn.setStyle(FONT_STYLE + COMMON_STYLE
                        + "-fx-background-color: " + ACTIVE_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold;");
                btn.getProperties().put("state", ACTIVE_COLOR);
            } else {
                btn.setStyle(FONT_STYLE + COMMON_STYLE
                        + "-fx-background-color: transparent; -fx-text-fill: rgb(210, 218, 236); -fx-font-weight: normal;");
                btn.getProperties().remove("state");
            }
        }
    }

    private void handleLogout() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Ban co muon dang xuat?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Xac nhan dang xuat");
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(type -> {
            if (type == ButtonType.YES) {
                Platform.runLater(() -> {
                    Stage stage = new Stage();
                    LoginFrame login = new LoginFrame();
                    Scene scene = login.createScene(stage);
                    stage.setScene(scene);
                    stage.show();
                    SwingUtilities.invokeLater(this::dispose);
                });
            }
        });
    }

    private StackPane createPlaceholderCard(String title, String subtitle) {
        VBox box = new VBox(16);
        box.setAlignment(Pos.CENTER);
        Label lbl = new Label(title);
        lbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        lbl.setTextFill(javafx.scene.paint.Color.web("#bdc3c7"));
        Label lbl2 = new Label(subtitle);
        lbl2.setFont(Font.font("Segoe UI", 16));
        lbl2.setTextFill(javafx.scene.paint.Color.web("#d5d8dc"));
        box.getChildren().addAll(lbl, lbl2);
        StackPane sp = new StackPane(box);
        sp.setStyle("-fx-background-color: " + BG_LIGHT + ";");
        return sp;
    }

    private void startClockFx() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss  dd/MM/yyyy");
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (lblClock != null) {
                lblClock.setText(sdf.format(new Date()));
            }
        }));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }
}
