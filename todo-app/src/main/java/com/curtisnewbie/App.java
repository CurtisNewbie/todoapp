package com.curtisnewbie;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.InputStream;

/**
 * <p>
 * JavaFx Application
 * </p>
 *
 * @author yongjie.zhuang
 */
public class App extends Application {

    private static final String TITLE = "TO-DO";
    private static final String FXML_FILE = "ui.fxml";
    private final int MIN_WIDTH = 400;
    private final int MIN_HEIGHT = 500;

    private static Stage primaryStage;
    private static Parent parent;

    @Override
    public void init() throws Exception {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(FXML_FILE)) {
            FXMLLoader loader = new FXMLLoader();
            App.parent = loader.load(in);
        }
    }

    @Override
    public void start(Stage stage) throws Exception {
        App.primaryStage = stage;
        Scene s = new Scene(parent);
        stage.setScene(s);
        primaryStage.setTitle(TITLE);
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
    }

    public static void main(String... args) {
        System.out.println("-------------- Initialising JavaFX TODO-APP Application --------------- ");
        launch(args);
        System.out.println("-------------- JavaFX TODO-APP Application Up And Running ------------- ");
    }
}
