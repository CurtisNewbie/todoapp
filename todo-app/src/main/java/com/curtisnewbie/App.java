package com.curtisnewbie;

import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.controller.Controller;
import com.curtisnewbie.util.ImageUtil;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * JavaFx Application
 * </p>
 *
 * @author yongjie.zhuang
 */
@Slf4j
public class App extends Application {

    private static final PropertiesLoader properties = PropertiesLoader.getInstance(); // can only use common props
    public static final String VERSION = properties.getCommonProperty("app.version");
    public static final String STARTUP_TITLE = "TO-DO " + VERSION;
    private static final int MIN_WIDTH = 500;
    private static final int MIN_HEIGHT = 350;

    private final int DEF_WIDTH = Integer.parseInt(properties.getCommonProperty("app.def.width"));
    private final int DEF_HEIGHT = Integer.parseInt(properties.getCommonProperty("app.def.height"));

    private static Stage primaryStage;
    private static BorderPane borderPane;
    private static final List<Runnable> onCloseList = new CopyOnWriteArrayList<>();

    @Override
    public void init() throws Exception {
        borderPane = new BorderPane();
    }

    @Override
    public void start(Stage stage) throws Exception {
        App.primaryStage = stage;

        final Scene s = new Scene(borderPane);

        // controller
        Controller.initialize(borderPane);

        // primary stage
        stage.setScene(s);
        stage.setTitle(STARTUP_TITLE);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.setWidth(DEF_WIDTH);
        stage.setHeight(DEF_HEIGHT);
        stage.getIcons().add(ImageUtil.load(ImageUtil.ICON_IMG_NAME));

        // activate all registered callbacks on close
        stage.setOnCloseRequest(e -> {
            App.invokesOnCloseList();
            System.exit(0);
        });

        // display stage
        stage.show();
    }

    /**
     * Register a callback for closing certain resources for application shutdown
     *
     * @param oc callback
     */
    public static void registerOnClose(Runnable oc) {
        App.onCloseList.add(oc);
    }

    /**
     * Invokes all registered callbacks
     */
    private static void invokesOnCloseList() {
        App.onCloseList.forEach(r -> {
            try {
                r.run();
            } catch (Throwable t) {
                log.error("Failed to execute onClose callback", t);
            }
        });
    }

    /** Get primary stage */
    public synchronized static Stage getPrimaryStage() {
        return App.primaryStage;
    }

    /** Get title on primary stage */
    public synchronized static String getTitle() {
        return App.primaryStage.getTitle();
    }

    /** Set title on primary stage */
    public synchronized static void setTitle(String startupTitle) {
        App.primaryStage.setTitle(startupTitle);
    }

    public static void main(String... args) {
        launch(args);
    }
}
