package com.curtisnewbie.controller;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * <p>
 * An input dialog using TextArea. This is to solve the single-line input limitation in {@link
 * javafx.scene.control.TextInputDialog}.
 * </p>
 * <p>
 * Note that this is almost a pure copy of TextInputDialog with little modification.
 * </p>
 *
 * @author yongjie.zhuang
 */
public class TxtAreaDialog extends Dialog<String> {

    public static final double MAX_WIDTH = 350;
    private final GridPane grid;
    private final Label label;
    private final TextArea textArea;
    private final String defaultValue;

    /**
     * Create TxtAreaDialog with "" as default value
     */
    public TxtAreaDialog() {
        this("");
    }

    /**
     * Create TxtAreaDialog with {@code defValue} as default value
     */
    public TxtAreaDialog(String defValue) {
        final DialogPane dialogPane = getDialogPane();
        this.textArea = new TextArea(defValue);
        this.textArea.setMaxWidth(MAX_WIDTH);
        this.textArea.setWrapText(true);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setFillWidth(textArea, true);

        // -- label
        label = createContentLabel(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        this.defaultValue = defValue;

        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        this.grid.add(label, 0, 0);
        this.grid.add(textArea, 1, 0);
        dialogPane.setContent(grid);

        dialogPane.contentTextProperty().addListener(o -> {
            Platform.runLater(() -> textArea.requestFocus());
        });

        setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? textArea.getText() : null;
        });

    }

    static Label createContentLabel(String text) {
        Label label = new Label(text);
        label.setMaxWidth(Double.MAX_VALUE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.getStyleClass().add("content");
        label.setWrapText(true);
        label.setPrefWidth(360);
        return label;
    }
}
