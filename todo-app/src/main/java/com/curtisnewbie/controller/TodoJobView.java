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

import static com.curtisnewbie.util.DateUtil.toMMddUUUUSlash;
import static com.curtisnewbie.util.LabelFactory.classicLabel;
import static com.curtisnewbie.util.LabelFactory.leftPaddedLabel;
import static com.curtisnewbie.util.MarginFactory.*;

// TODO fix the overly complicated or say problematic synchronization, model and view are mixed in a weired way

/**
 * A "view" object representing a to-do job
 *
 * @author yongjie.zhuang
 */
public class TodoJobView extends HBox {
    public static final int WIDTH_OTHER_THAN_TEXT = 290;
    private final Object mutex = new Object();
    private final String checkboxName;
    private final Label doneLabel;

    /**
     * The id of the todojob
     */
    private final Integer idOfTodoJob;

    /**
     * The name of this {@code TodoJob}
     */
    private final Text nameText;

    /**
     * Expected end date
     */
    private LocalDate expectedEndDate;

    /**
     * Expected end date in {@link Label}
     */
    private final Label expectedEndDateLabel;

    /**
     * Actual end date
     */
    private LocalDate actualEndDate;

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
        this.environment = environment;
        this.idOfTodoJob = todoJob.getId();
        this.checkboxName = PropertiesLoader.getInstance().get(PropertyConstants.TEXT_DONE_PREFIX, environment.getLanguage());
        this.doneLabel = new Label();
        this.nameText = TextFactory.getClassicText(todoJob.getName());
        this.expectedEndDateLabel = classicLabel(toMMddUUUUSlash(todoJob.getExpectedEndDate()));
        this.expectedEndDate = todoJob.getExpectedEndDate();
        this.actualEndDate = todoJob.getActualEndDate();
        if (actualEndDate != null) {
            this.actualEndDateLabel = classicLabel(toMMddUUUUSlash(todoJob.getActualEndDate()));
        } else {
            this.actualEndDateLabel = classicLabel("");
            this.actualEndDateLabel.prefWidthProperty().bind(this.expectedEndDateLabel.widthProperty());
        }
        this.doneCheckBox.setSelected(todoJob.isDone());
        this.doneCheckBox.setOnAction(this::onCheckboxSelected);
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

    /**
     * Set the {@link #expectedEndDate} as well as updating the label
     */
    public void setExpectedEndDate(LocalDate date) {
        Objects.requireNonNull(date, "Expected End Date should never be null");
        synchronized (mutex) {
            this.expectedEndDate = date;
        }
        Platform.runLater(() -> {
            this.expectedEndDateLabel.setText(toMMddUUUUSlash(date));
        });
    }

    /**
     * Set the {@link #actualEndDate} as well as updating the label
     */
    public void setActualEndDate(LocalDate date) {
        synchronized (mutex) {
            this.actualEndDate = date;
        }
        Platform.runLater(() -> {
            if (date != null) {
                this.actualEndDateLabel.setText(toMMddUUUUSlash(date));
            } else {
                this.actualEndDateLabel.setText("");
                this.actualEndDateLabel.prefWidthProperty().bind(this.expectedEndDateLabel.widthProperty());
            }
        });
    }

    /**
     * Bind the wrapping width of text
     */
    public void bindTextWrappingWidthProperty(final DoubleBinding binding) {
        synchronized (mutex) {
            nameText.wrappingWidthProperty().bind(binding);
        }
    }

    public LocalDate getExpectedEndDate() {
        synchronized (mutex) {
            return this.expectedEndDate;
        }
    }

    public LocalDate getActualEndDate() {
        synchronized (mutex) {
            return this.actualEndDate;
        }
    }

    public void setName(String txt) {
        synchronized (mutex) {
            nameText.setText(txt);
        }
    }

    public String getName() {
        synchronized (mutex) {
            return nameText.getText();
        }
    }

    public boolean isCheckboxSelected() {
        synchronized (mutex) {
            return doneCheckBox.isSelected();
        }
    }

    public void setCheckboxSelected(boolean isSelected) {
        synchronized (mutex) {
            doneCheckBox.setSelected(isSelected);
        }
    }

    /**
     * Get copy of current {@code TodoJobView} in forms of a new {@code TodoJob}
     *
     * @return todoJob
     */
    public TodoJob createTodoJobCopy() {
        TodoJob copy = new TodoJob();
        copy.setId(idOfTodoJob);
        synchronized (mutex) {
            copy.setName(nameText.getText());
            copy.setDone(doneCheckBox.isSelected());
            copy.setExpectedEndDate(this.expectedEndDate);
            copy.setActualEndDate(this.actualEndDate);
        }
        return copy;
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
        boolean isSelected = isCheckboxSelected();
        setActualEndDate(isSelected ? LocalDate.now() : null);
        updateGraphicOnJobStatus(isSelected);
        // this callback cannot be changed, no need to synchronise
        if (doneCheckboxRegisteredCallback != null)
            doneCheckboxRegisteredCallback.react();
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
        synchronized (mutex) {
            if (!doneCheckBox.isDisable())
                doneCheckBox.setDisable(true);
        }
    }

    public Integer getIdOfTodoJob() {
        return idOfTodoJob;
    }
}
