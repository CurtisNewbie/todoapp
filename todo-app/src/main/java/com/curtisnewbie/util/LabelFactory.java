package com.curtisnewbie.util;

import com.curtisnewbie.common.GlobalPools;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Border;

import static com.curtisnewbie.util.MarginFactory.cachedInsets;
import static javafx.scene.layout.Region.USE_COMPUTED_SIZE;

/**
 * <p>
 * Factory class to produce custom {@code Label}
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class LabelFactory {

    private LabelFactory() {
    }

    public static void returnLabel(Label label) {
        // merely an attempt to reset the label so that we may reuse it
        label.setGraphic(null);
        label.setText(null);
        label.setMinWidth(USE_COMPUTED_SIZE);
        label.setPadding(cachedInsets(0, 0, 0, 0));
        label.textProperty().unbind();
        GlobalPools.labelPool.returnT(label);
    }

    public static Label classicLabel(String text) {
        Label label = baseLabel(text);
        label.setPadding(cachedInsets(3, 2, 3, 2));
        return label;
    }

    public static Label leftPaddedLabel(String text) {
        Label label = baseLabel(text);
        label.setPadding(cachedInsets(3, 2, 3, 15));
        return label;
    }


    public static Label rightPaddedLabel(String text) {
        Label label = baseLabel(text);
        label.setPadding(cachedInsets(3, 15, 3, 2));
        return label;
    }

    private static Label baseLabel(String text) {
        Label label = GlobalPools.labelPool.borrowT();
        if (text != null) label.setText(text);
        label.setWrapText(false);
        label.setBorder(Border.EMPTY);
        label.setAlignment(Pos.BASELINE_CENTER);
        return label;
    }

}
