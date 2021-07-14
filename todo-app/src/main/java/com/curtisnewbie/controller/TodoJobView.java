package com.curtisnewbie.controller;

import com.curtisnewbie.callback.OnEvent;
import com.curtisnewbie.config.Environment;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.config.PropertyConstants;
import com.curtisnewbie.dao.TodoJob;
import com.curtisnewbie.exception.EventHandlerAlreadyRegisteredException;
import com.curtisnewbie.util.CheckBoxFactory;
import com.curtisnewbie.util.ShapeFactory;
import com.curtisnewbie.util.TextFactory;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import java.time.LocalDate;
import java.util.Objects;

import static com.curtisnewbie.util.DateUtil.toDDmmUUUUSlash;
import static com.curtisnewbie.util.DateUtil.toMMddUUUUSlash;
import static com.curtisnewbie.util.LabelFactory.classicLabel;
import static com.curtisnewbie.util.LabelFactory.leftPaddedLabel;
import static com.curtisnewbie.util.MarginFactory.*;


/**
 * A "view" object representing a to-do job
 *
 * @author yongjie.zhuang
 */
public class TodoJobView extends HBox {

    public static final int WIDTH_OTHER_THAN_TEXT = 290;

    private final Label doneLabel;

    private final Object mutex = new Object();

    /**
     * Model inside this view
     */
    private TodoJob model;

    /**
     * The name of this {@code TodoJob}
     */
    private final Text nameText;

    /**
     * Expected end date in {@link Label}
     */
    private final Label expectedEndDateLabel;

    /**
     * Actual end date in {@link Label}
     */
    private final Label actualEndDateLabel;

    /**
     * Whether the {@code TodoJob} is finished
     */
    private final CheckBox doneCheckBox = CheckBoxFactory.getClassicCheckBox();

    /** Registered callback for {@link #doneCheckBox} */
    private volatile OnEvent doneCheckboxRegisteredCallback;

    /** Environment configuration */
    private final Environment environment;

    /**
     * Create a TodoJobView with the given {@code todoJob}
     *
     * @param todoJob
     */
    public TodoJobView(TodoJob todoJob, Environment environment) {
        Objects.requireNonNull(todoJob);
        Objects.requireNonNull(environment);

        this.environment = environment;
        this.model = new TodoJob(todoJob);
        this.doneLabel = new Label();
        this.nameText = TextFactory.getClassicText(todoJob.getName());
        this.expectedEndDateLabel = classicLabel(toDDmmUUUUSlash(todoJob.getExpectedEndDate()));
        if (this.model.getActualEndDate() != null) {
            this.actualEndDateLabel = classicLabel(toDDmmUUUUSlash(todoJob.getActualEndDate()));
        } else {
            this.actualEndDateLabel = classicLabel("");
            this.actualEndDateLabel.prefWidthProperty().bind(this.expectedEndDateLabel.widthProperty());
        }
        this.doneCheckBox.setSelected(todoJob.isDone());
        this.doneCheckBox.setOnAction(this::onCheckboxSelected);
        String checkboxName = PropertiesLoader.getInstance().get(PropertyConstants.TEXT_DONE_PREFIX, environment.getLanguage());
        Objects.requireNonNull(checkboxName);
        this.getChildren()
                .addAll(doneLabel,
                        fixedMargin(3),
                        expectedEndDateLabel,
                        fixedMargin(3),
                        actualEndDateLabel,
                        fixedMargin(20),
                        wrapWithCommonPadding(nameText),
                        expandingMargin(),
                        leftPaddedLabel(checkboxName),
                        doneCheckBox);
        HBox.setHgrow(this, Priority.SOMETIMES);
        updateGraphicOnJobStatus(todoJob.isDone());
        this.requestFocus();
    }

    public void setExpectedEndDate(LocalDate date) {
        Objects.requireNonNull(date, "Expected End Date should never be null");

        synchronized (model) {
            this.model.setExpectedEndDate(date);
            Platform.runLater(() -> {
                this.expectedEndDateLabel.setText(toDDmmUUUUSlash(date));
            });
        }
    }

    public void setActualEndDate(LocalDate date) {
        synchronized (model) {
            this.model.setActualEndDate(date);
            Platform.runLater(() -> {
                if (date != null) {
                    this.actualEndDateLabel.setText(toDDmmUUUUSlash(date));
                } else {
                    this.actualEndDateLabel.setText("");
                    this.actualEndDateLabel.prefWidthProperty().bind(this.expectedEndDateLabel.widthProperty());
                }
            });
        }
    }

    /**
     * Bind the wrapping width of text
     */
    public void bindTextWrappingWidthProperty(final DoubleBinding binding) {
        synchronized (mutex) {
            nameText.wrappingWidthProperty().bind(binding);
        }
    }

    public void setName(String name) {
        synchronized (model) {
            this.model.setName(name);
            Platform.runLater(() -> {
                nameText.setText(name);
            });
        }
    }

    public boolean isDone() {
        synchronized (model) {
            return this.model.isDone();
        }
    }

    /**
     * Get copy of current {@code TodoJobView} in forms of a new {@code TodoJob}
     *
     * @return todoJob
     */
    public TodoJob createTodoJobCopy() {
        synchronized (model) {
            return new TodoJob(model);
        }
    }

    /**
     * <p>
     * Register an event handler for the "done" check box
     * </p>
     *
     * @param onEvent
     * @throws EventHandlerAlreadyRegisteredException if this method is invoked for multiple times for the same object
     */
    public void registerCheckboxEventHandler(OnEvent onEvent) {
        synchronized (mutex) {
            if (doneCheckboxRegisteredCallback != null)
                throw new EventHandlerAlreadyRegisteredException();
            this.doneCheckboxRegisteredCallback = onEvent;
        }
    }

    private void onCheckboxSelected(ActionEvent e) {
        synchronized (model) {
            final boolean isTaskDone = ((CheckBox) e.getTarget()).isSelected();
            model.setDone(isTaskDone);
            setActualEndDate(isTaskDone ? LocalDate.now() : null);
            updateGraphicOnJobStatus(isTaskDone);
            if (doneCheckboxRegisteredCallback != null)
                doneCheckboxRegisteredCallback.react();
        }
    }

    /** Update graphic based on job's status */
    private void updateGraphicOnJobStatus(boolean isJobFinished) {
        Platform.runLater(() -> {
            this.doneLabel.setGraphic(isJobFinished ? ShapeFactory.greenCircle() : ShapeFactory.redCircle());
            if (environment.isStrikethroughEffectEnabled()) {
                this.nameText.setStrikethrough(isJobFinished);
            }
        });
    }

    /** make the internal checkbox not editable */
    public final void freeze() {
        Platform.runLater(() -> {
            doneCheckBox.setDisable(true);
        });
    }

    public Integer getTodoJobId() {
        synchronized (model) {
            return model.getId();
        }
    }
}
