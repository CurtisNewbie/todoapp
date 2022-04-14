package com.curtisnewbie.util;

import com.curtisnewbie.*;
import javafx.animation.*;
import javafx.scene.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.text.*;
import javafx.stage.*;
import javafx.util.Duration;

import java.util.concurrent.*;

import static com.curtisnewbie.util.MarginFactory.*;


/**
 * Util for Toast
 *
 * @author yongj.zhuang
 */
public final class ToastUtil {

    /**
     * Toast
     */
    public static void toast(final String msg) {
        toast(msg, TimeUnit.SECONDS.toMillis(5));
    }

    /**
     * Toast
     */
    public static void toast(final String msg, final long durationInMilli) {
        final Stage toastStage = new Stage();
        toastStage.setResizable(false);
        toastStage.initOwner(App.getPrimaryStage());

        final Text text = new Text(msg);
        final StackPane root = new StackPane(padding(text, 30, 50, 30, 50));
        final Scene scene = new Scene(root);
        toastStage.setScene(scene);
        toastStage.show();

        final FadeTransition inTransition = new FadeTransition(new Duration(300), toastStage.getScene().getRoot());
        inTransition.setFromValue(0.0);
        inTransition.setToValue(1);

        final FadeTransition outTransition = new FadeTransition(new Duration(300), toastStage.getScene().getRoot());
        outTransition.setFromValue(1.0);
        outTransition.setToValue(0);

        final PauseTransition pauseTransition = new PauseTransition(new Duration(durationInMilli));
        final SequentialTransition mainTransition = new SequentialTransition(inTransition, pauseTransition, outTransition);
        mainTransition.setOnFinished(ae -> toastStage.close());
        mainTransition.play();
    }
}
