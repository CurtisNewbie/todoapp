package com.curtisnewbie.controller;

import com.curtisnewbie.util.DialogUtil;
import com.curtisnewbie.util.RequiresFxThread;
import com.curtisnewbie.util.MarginFactory;
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
@RequiresFxThread
public class TxtAreaDialog extends Dialog<String> {

    public static final double MAX_WIDTH = 500;
    private final TextArea textArea;

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
        final Label label = createContentLabel(dialogPane.getContentText());
        label.setPrefWidth(Region.USE_COMPUTED_SIZE);
        label.textProperty().bind(dialogPane.contentTextProperty());

        final GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setMaxWidth(Double.MAX_VALUE);
        grid.setAlignment(Pos.CENTER_LEFT);
        grid.add(MarginFactory.wrapWithCommonPadding(label), 0, 0);
        grid.add(MarginFactory.wrapWithCommonPadding(textArea), 0, 1);
        dialogPane.setContent(grid);

        dialogPane.contentTextProperty().addListener(o -> Platform.runLater(textArea::requestFocus));

        setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            return data == ButtonBar.ButtonData.OK_DONE ? textArea.getText() : null;
        });
        DialogUtil.disableHeader(this);
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
