package com.curtisnewbie.controller;

import com.curtisnewbie.dao.TodoJob;
import com.curtisnewbie.util.DialogUtil;
import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import java.time.LocalDate;
import java.util.Optional;

import static com.curtisnewbie.util.LabelFactory.classicLabel;
import static com.curtisnewbie.util.MarginFactory.wrapWithPadding;

/**
 * Dialog for TodoJob
 *
 * @author yongjie.zhuang
 */
public class TodoJobDialog extends Dialog<TodoJob> {

    public static final double MAX_WIDTH = 350;
    private final GridPane grid;
    private final DialogType type;
    private Label contextLabel;
    private TextArea textArea;
    private DatePicker expectedEndDatePicker = new DatePicker();
    private LocalDate expectedEndDate;
    private DatePicker actualEndDatePicker;
    private LocalDate actualEndDate;
    private Optional<TodoJob> optionalTodoJob;

    /**
     * Create a TodoJobDialog for creating new to-do or updating existing to-do
     * <p>
     * When the dialog is used to update an existing to-do, the parameter {@code todoJob} is mandatory, and thus cannot
     * be empty, note that a dialog is used for only one to-do, so only one {@link TodoJob} is needed.
     * </p>
     *
     * @param type    type of the dialog
     * @param todoJob optional todoJob (mandatory for updating todojob)
     */
    public TodoJobDialog(DialogType type, TodoJob todoJob) {
        this.type = type;
        this.optionalTodoJob = Optional.ofNullable(todoJob);
        if (type == DialogType.UPDATE_TODO_JOB && !optionalTodoJob.isPresent())
            throw new IllegalArgumentException("Parameter todoJob is mandatory when updating an existing record");

        final DialogPane dialogPane = getDialogPane();
        setupContentLabel(dialogPane);

        if (optionalTodoJob.isPresent())
            setupTextArea(optionalTodoJob.get().getName());
        else
            setupTextArea("");

        if (optionalTodoJob.isPresent()) {
            this.expectedEndDate = optionalTodoJob.get().getExpectedEndDate();
            this.expectedEndDatePicker.setValue(expectedEndDate);

            // if we are updating an todojob & the actualEndDate is present
            if (shouldDisplayActualEndDatePicker()) {
                this.actualEndDatePicker = new DatePicker();
                this.actualEndDate = optionalTodoJob.get().getActualEndDate();
                this.actualEndDatePicker.setValue(actualEndDate);
            }
        }

        // setup grid
        this.grid = new GridPane();
        setupGrid(dialogPane);
        // setup dialog pane
        setupDialogPane(dialogPane);
        // register event handler for the datePickers
        registerDatePickerEventListener();
        // setup result converter
        setupResultConverter();

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

    private void registerDatePickerEventListener() {
        expectedEndDatePicker.setOnAction(actionEvent -> {
            expectedEndDate = expectedEndDatePicker.getValue();
        });
        if (shouldDisplayActualEndDatePicker()) {
            actualEndDatePicker.setOnAction(actionEvent -> {
                actualEndDate = actualEndDatePicker.getValue();
            });
        }
    }

    private void setupGrid(DialogPane dialogPane) {
        this.grid.setHgap(10);
        this.grid.setMaxWidth(Double.MAX_VALUE);
        this.grid.setAlignment(Pos.CENTER_LEFT);
        this.grid.add(contextLabel, 0, 0);
        this.grid.add(wrapWithPadding(classicLabel("Expected End Date:"), new Insets(1, 2, 5, 2)),
                1, 1);
        this.grid.add(wrapWithPadding(expectedEndDatePicker, new Insets(1, 2, 5, 2)),
                2, 1);

        if (shouldDisplayActualEndDatePicker()) {
            this.grid.add(wrapWithPadding(classicLabel("Actual End Date:"), new Insets(1, 2, 5, 2)),
                    1, 2);
            this.grid.add(wrapWithPadding(actualEndDatePicker, new Insets(1, 2, 5, 2)),
                    2, 2);
            this.grid.add(textArea, 1, 3);
        } else {
            this.grid.add(textArea, 1, 2);
        }
        dialogPane.setContent(grid);
    }

    private void setupDialogPane(DialogPane dialogPane) {
        dialogPane.contentTextProperty().addListener(o -> {
            Platform.runLater(() -> textArea.requestFocus());
        });

        setTitle(ControlResources.getString("Dialog.confirm.title"));
        dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
        dialogPane.getStyleClass().add("text-input-dialog");
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    }

    private void setupResultConverter() {
        setResultConverter((dialogButton) -> {
            ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
            if (data == ButtonBar.ButtonData.OK_DONE) {
                var todoJob = new TodoJob(textArea.getText().trim());
                todoJob.setExpectedEndDate(expectedEndDate != null ? expectedEndDate : LocalDate.now());
                if (shouldDisplayActualEndDatePicker()) {
                    todoJob.setActualEndDate(actualEndDate);
                }
                return todoJob;
            } else {
                return null;
            }
        });
    }

    private void setupTextArea(String defaultText) {
        if (defaultText == null)
            defaultText = "";
        this.textArea = new TextArea(defaultText);
        this.textArea.setMaxWidth(MAX_WIDTH);
        this.textArea.setWrapText(true);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane.setFillWidth(textArea, true);
    }

    private void setupContentLabel(DialogPane dialogPane) {
        contextLabel = createContentLabel(dialogPane.getContentText());
        contextLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        contextLabel.textProperty().bind(dialogPane.contentTextProperty());
    }

    private boolean shouldDisplayActualEndDatePicker() {
        return type == DialogType.UPDATE_TODO_JOB
                && this.optionalTodoJob.get().isDone();
    }

    public enum DialogType {
        ADD_TODO_JOB,
        UPDATE_TODO_JOB
    }
}
