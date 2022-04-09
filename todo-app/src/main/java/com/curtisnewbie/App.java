package com.curtisnewbie;

import com.curtisnewbie.callback.OnClose;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.controller.Controller;
import com.curtisnewbie.util.ImageUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * JavaFx Application
 * </p>
 *
 * @author yongjie.zhuang
 */
public class App extends Application {

    private static final PropertiesLoader properties = PropertiesLoader.getInstance();
    public static final String VERSION = properties.getCommonProperty("app.version");
    public static final String STARTUP_TITLE = "TO-DO " + VERSION;
    private final int DEF_WIDTH = Integer.parseInt(properties.getCommonProperty("app.def.width"));
    private final int DEF_HEIGHT = Integer.parseInt(properties.getCommonProperty("app.def.height"));
    private final int MIN_WIDTH = 500;
    private final int MIN_HEIGHT = 350;

    private static Stage primaryStage;
    private static BorderPane borderPane;
    private static List<OnClose> onCloseList = new ArrayList<>();

    @Override
    public void init() throws Exception {
        borderPane = new BorderPane();
    }

    @Override
    public void start(Stage stage) throws Exception {
        App.primaryStage = stage;
        Scene s = new Scene(borderPane);
        Controller.initialize(borderPane);
        stage.setScene(s);
        stage.setTitle(STARTUP_TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setWidth(DEF_WIDTH);
        stage.setHeight(DEF_HEIGHT);
        stage.show();
        stage.setOnCloseRequest(e -> {
            // activate all registered callbacks
            App.invokesOnCloseList();
            // exit application
            System.exit(0);
        });
        stage.getIcons().add(ImageUtil.TITLE_ICON);
        System.out.println("-------------- JavaFX TODO-APP Application Up And Running ------------- ");
    }

    /**
     * Register a callback for closing certain resources for application shutdown
     *
     * @param oc callback
     */
    public static void registerOnClose(OnClose oc) {
        synchronized (onCloseList) {
            App.onCloseList.add(oc);
        }
    }

    /**
     * Invokes all registered callbacks
     */
    private static void invokesOnCloseList() {
        synchronized (onCloseList) {
            App.onCloseList.forEach(OnClose::close);
        }
    }

    public static Stage getPrimaryStage() {
        return App.primaryStage;
    }

    public static String getTitle() {
        synchronized (App.primaryStage) {
            return App.primaryStage.getTitle();
        }
    }

    public static void setTitle(String startupTitle) {
        synchronized (App.primaryStage) {
            App.primaryStage.setTitle(startupTitle);
        }
    }

    public static void main(String... args) {
        System.out.println("-------------- Initialising JavaFX TODO-APP Application --------------- ");
        launch(args);
    }
}
