package gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class StartUpGui extends Application {

    @Override
    public void start(Stage primaryStage) {
        // JavaFX sẽ vẫn chạy ngay cả khi đóng Stage login, vì `MainFrame` đang dùng
        // JFXPanel (Swing + JavaFX).
        Platform.setImplicitExit(false);

        primaryStage.setTitle("Đăng nhập - Hệ Thống Quản Lý Khách Sạn");
        LoginFrame login = new LoginFrame();
        Scene scene = login.createScene(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.setMinWidth(450);
        primaryStage.setMinHeight(320);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
