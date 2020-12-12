package com.curtisnewbie.controller;

import com.curtisnewbie.entity.TodoJob;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDate;

import static com.curtisnewbie.util.MarginFactory.wrapWithPadding;

/**
 * Dialog for TodoJob
 *
 * @author yongjie.zhuang
 */
public class TodoJobDialog extends Dialog<TodoJob> {

    public static final double MAX_WIDTH = 350;
    private final GridPane grid;
    private final Label label;
    private final TextArea textArea;
    private final DatePicker datePicker;
    private final String defaultValue;
    private LocalDate localDate;

    /**
     * Create TodoJobDialog with "" as default text and current date as {@code TodoJob}'s createDate
     */
    public TodoJobDialog() {
        this("", LocalDate.now());
    }

    /**
     * Create TodoJobDialog with {@code defValue} as default value and {@code date} (in milliseconds) as the createDate
     * of the {@code TodoJob}
     */
    public TodoJobDialog(String defValue, LocalDate date) {
        final DialogPane dialogPane = getDialogPane();
        this.datePicker = new DatePicker();
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
        this.localDate = date;
        this.datePicker.setValue(localDate);

        this.grid = new GridPane();
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        this.grid.add(label, 0, 0);
        this.grid.add(wrapWithPadding(datePicker, new Insets(1, 2, 5, 2)), 1, 1);
        this.grid.add(textArea, 1, 2);
        dialogPane.setContent(grid);

        dialogPane.contentTextProperty().addListener(o -> {
            Platform.runLater(() -> textArea.requestFocus());
        });

        setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        datePicker.setOnAction(actionEvent -> {
            localDate = datePicker.getValue();
        });

        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            if (data == ButtonBar.ButtonData.OK_DONE) {
                var todoJob = new TodoJob(textArea.getText().trim());
                if (localDate == null || LocalDate.now().isEqual(localDate)) {
                    todoJob.setStartDate(LocalDate.now());
                } else {
                    todoJob.setStartDate(localDate);
                }
                return todoJob;
            } else {
                return null;
            }
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
