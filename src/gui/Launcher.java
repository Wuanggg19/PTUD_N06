package gui;

import javafx.application.Application;

public final class Launcher {
    private Launcher() {
    }

    public static void main(String[] args) {
        Application.launch(StartUpGui.class, args);
    }
}
