package com.curtisnewbie.util;

import com.curtisnewbie.App;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.concurrent.TimeUnit;

import static com.curtisnewbie.util.MarginFactory.padding;


/**
 * Util for Toast
 *
 * @author yongj.zhuang
 */
public final class ToastUtil {

    /**
     * Toast
     */
    @RunInFxThread
    public static void toast(final String msg) {
        toast(msg, TimeUnit.SECONDS.toMillis(5));
    }

    /**
     * Toast
     */
    @RunInFxThread
    public static void toast(final String msg, final long durationInMilli) {
        Platform.runLater(() -> {
            final Stage toastStage = new Stage();
            toastStage.setResizable(false);
            toastStage.initOwner(App.getPrimaryStage());

            final Text text = TextFactory.baseText(msg);
            final StackPane root = new StackPane(padding(text, 30, 50, 30, 50));
            final Scene scene = new Scene(root);
            toastStage.setScene(scene);
            toastStage.show();

            final FadeTransition inTransition = new FadeTransition(new Duration(400), toastStage.getScene().getRoot());
            inTransition.setFromValue(0.0);
            inTransition.setToValue(1);

            final FadeTransition outTransition = new FadeTransition(new Duration(400), toastStage.getScene().getRoot());
            outTransition.setFromValue(1.0);
            outTransition.setToValue(0);

            final PauseTransition pauseTransition = new PauseTransition(new Duration(durationInMilli));
            final SequentialTransition mainTransition = new SequentialTransition(inTransition, pauseTransition, outTransition);
            mainTransition.setOnFinished(ae -> toastStage.close());
            mainTransition.play();
        });
    }
}
