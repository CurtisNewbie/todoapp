package com.curtisnewbie.controller;

import com.curtisnewbie.config.*;
import com.curtisnewbie.util.*;
import javafx.application.*;
import javafx.beans.property.*;
import javafx.scene.control.*;
import javafx.scene.control.TextArea;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.util.function.*;

import static com.curtisnewbie.config.PropertyConstants.*;

/**
 * Bar that provides TextField to quickly create a new TO-DO
 *
 * @author yongj.zhuang
 */
@FxThreadConfinement
public class QuickTodoBar extends VBox {

    private final TextField textField = new TextField();
    private Consumer<String> onEnter;

    public QuickTodoBar() {
        textField.setOnKeyReleased(e -> {

            if (e.getCode().equals(KeyCode.ENTER) && (e.isControlDown() || e.isAltDown() || e.isMetaDown())) {
                Platform.runLater(() -> {
                    final String name = textField.getText();
                    if (StrUtil.isEmpty(name))
                        return;

                    textField.clear();

                    if (onEnter != null)
                        onEnter.accept(name);
                });
            }
        });

        this.getChildren().addAll(
                MarginFactory.fixedMargin(5),
                LabelFactory.classicLabel(PropertiesLoader.getInstance().getLocalizedProperty(TEXT_QUICK_TODO)),
                MarginFactory.fixedMargin(5),
                textField,
                MarginFactory.fixedMargin(5)
        );
    }

    public void setOnEnter(Consumer<String> onEnter) {
        this.onEnter = onEnter;
    }

    /**
     * Get textField's {@code prefWidthProperty()}
     */
    public DoubleProperty textFieldPrefWidthProperty() {
        FxThreadUtil.checkThreadConfinement();
        return textField.prefWidthProperty();
    }
}
