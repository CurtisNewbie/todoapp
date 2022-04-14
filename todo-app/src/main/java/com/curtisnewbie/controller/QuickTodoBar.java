package com.curtisnewbie.controller;

import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.util.*;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.util.function.Consumer;

import static com.curtisnewbie.config.PropertyConstants.TEXT_QUICK_TODO;
import static com.curtisnewbie.config.PropertyConstants.TEXT_QUICK_TODO_HELP;
import static com.curtisnewbie.util.LabelFactory.classicLabel;
import static com.curtisnewbie.util.MarginFactory.margin;
import static com.curtisnewbie.util.MarginFactory.padding;
import static com.curtisnewbie.util.TooltipUtil.tooltip;

/**
 * Bar that provides TextField to quickly create a new TO-DO
 *
 * @author yongj.zhuang
 */
@RequiresFxThread
public class QuickTodoBar extends VBox {

    private final TextField textField = new TextField();
    private Consumer<String> onEnter;

    public QuickTodoBar() {
        tooltip(this, PropertiesLoader.getInstance().getLocalizedProperty(TEXT_QUICK_TODO_HELP));

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

        final String label = PropertiesLoader.getInstance().getLocalizedProperty(TEXT_QUICK_TODO);
        this.getChildren().addAll(
                margin(5),
                MarginFactory.padding(classicLabel(label), 0, 5, 0, 5),
                margin(5),
                padding(textField, new Insets(0, 5, 5, 5)),
                margin(5)
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
