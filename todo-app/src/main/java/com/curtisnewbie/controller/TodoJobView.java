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
import java.time.Period;
import java.util.ConcurrentModificationException;
import java.util.Objects;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.DateUtil.toDDmmUUUUSlash;
import static com.curtisnewbie.util.LabelFactory.classicLabel;
import static com.curtisnewbie.util.LabelFactory.leftPaddedLabel;
import static com.curtisnewbie.util.MarginFactory.*;


/**
 * A "view" object representing a to-do job
 * <p>
 * This view should only be invoked inside {@link Platform#runLater(Runnable)}, as long as it's confined inside the UI
 * thread, it's thread-safe, otherwise it's not because no synchronization is used.
 * </p>
 * <p>
 * Notice that this class do check if current thread is FX's UI thread, if not, a {@link ConcurrentModificationException
 * } may be thrown
 * </p>
 *
 * @author yongjie.zhuang
 */
public class TodoJobView extends HBox {

    /**
     * Label for displaying whether to-do is finished
     */
    private final Label doneLabel;

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
     * Label that displays how much time left before the expected end date
     */
    private final Label timeLeftLabel = classicLabel(null);

    /**
     * Whether the {@code TodoJob} is finished
     */
    private final CheckBox doneCheckBox = CheckBoxFactory.getClassicCheckBox();

    /** Registered callback for {@link #doneCheckBox} */
    private OnEvent doneCheckboxRegisteredCallback;

    /** Environment configuration */
    private final Environment environment;
    private final PropertiesLoader properties = PropertiesLoader.getInstance();

    private final String DAYS = properties.getLocalizedProperty(TEXT_DAYS_KEY);
    private final String MONTHS = properties.getLocalizedProperty(TEXT_MONTHS_KEY);
    private final String YEARS = properties.getLocalizedProperty(TEXT_YEARS_KEY);
    private final String DELAYED_TEXT = properties.getLocalizedProperty(TEXT_DELAYED_KEY);
    private final String AHEAD_TEXT = properties.getLocalizedProperty(TEXT_AHEAD_KEY);
    private final String ON_TIME_TEXT = properties.getLocalizedProperty(TEXT_ON_TIME_KEY);
    private final String TODAY_TEXT = properties.getLocalizedProperty(TEXT_TODAY_KEY);


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
        this.nameText = TextFactory.getClassicText(model.getName());
        this.expectedEndDateLabel = classicLabel(toDDmmUUUUSlash(model.getExpectedEndDate()));
        if (this.model.getActualEndDate() != null) {
            this.actualEndDateLabel = classicLabel(toDDmmUUUUSlash(model.getActualEndDate()));
        } else {
            this.actualEndDateLabel = classicLabel("");
        }
        this.expectedEndDateLabel.setMinWidth(85);
        this.actualEndDateLabel.setMinWidth(85);

        // update the timeLeftLabel
        updateTimeLeftLabel();

        this.doneCheckBox.setSelected(model.isDone());
        this.doneCheckBox.setOnAction(this::onDoneCheckBoxSelected);
        String checkboxName = properties.getLocalizedProperty(PropertyConstants.TEXT_DONE_KEY);
        Objects.requireNonNull(checkboxName);
        this.getChildren()
                .addAll(doneLabel,
                        fixedMargin(3),
                        expectedEndDateLabel,
                        fixedMargin(3),
                        actualEndDateLabel,
                        fixedMargin(20),
                        wrapWithCommonPadding(nameText),
                        fixedMargin(2),
                        timeLeftLabel,
                        fixedMargin(2),
                        expandingMargin(),
                        leftPaddedLabel(checkboxName),
                        doneCheckBox);
        HBox.setHgrow(this, Priority.SOMETIMES);
        updateGraphicOnJobStatus(model.isDone());
        this.requestFocus();
    }

    /**
     * Set the expectedEndDate being displayed
     *
     * @param date nullable LocalDate
     */
    public void setExpectedEndDate(LocalDate date) {
        Objects.requireNonNull(date, "Expected End Date should never be null");
        this.model.setExpectedEndDate(date);
        this.expectedEndDateLabel.setText(toDDmmUUUUSlash(date));
        if (!this.model.isDone())
            updateTimeLeftLabel();
        else
            emptyTimeLeftLabel();
    }

    /**
     * Set the actualEndDate being displayed
     *
     * @param date nullable LocalDate
     */
    public void setActualEndDate(LocalDate date) {
        checkThreadConfinement();
        this.model.setActualEndDate(date);
        if (date != null) {
            this.actualEndDateLabel.setText(toDDmmUUUUSlash(date));
        } else {
            this.actualEndDateLabel.setText("");
        }
    }

    /**
     * Bind the wrapping width of text
     */
    public void bindTextWrappingWidthProperty(final DoubleBinding binding) {
        checkThreadConfinement();
        Objects.requireNonNull(binding);
        nameText.wrappingWidthProperty().bind(binding);
    }

    /**
     * Set the text/name being displayed
     */
    public void setName(String name) {
        checkThreadConfinement();
        Objects.requireNonNull(name);
        this.model.setName(name);
        nameText.setText(name);
    }

    /**
     * Get copy of current {@code TodoJobView} in forms of a new {@code TodoJob}
     *
     * @return todoJob
     */
    public TodoJob createTodoJobCopy() {
        checkThreadConfinement();
        return new TodoJob(model);
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
        checkThreadConfinement();
        Objects.requireNonNull(onEvent);
        if (doneCheckboxRegisteredCallback != null)
            throw new EventHandlerAlreadyRegisteredException();
        this.doneCheckboxRegisteredCallback = onEvent;
    }

    private void onDoneCheckBoxSelected(ActionEvent e) {
        checkThreadConfinement();
        final boolean isTaskDone = ((CheckBox) e.getTarget()).isSelected();
        model.setDone(isTaskDone);
        setActualEndDate(isTaskDone ? LocalDate.now() : null);
        if (isTaskDone)
            emptyTimeLeftLabel();
        else
            updateTimeLeftLabel();
        updateGraphicOnJobStatus(isTaskDone);
        if (doneCheckboxRegisteredCallback != null)
            doneCheckboxRegisteredCallback.react();
    }

    /** Update graphic based on job's status */
    private void updateGraphicOnJobStatus(boolean isJobFinished) {
        this.doneLabel.setGraphic(isJobFinished ? ShapeFactory.greenCircle() : ShapeFactory.redCircle());
        if (environment.isStrikethroughEffectEnabled()) {
            this.nameText.setStrikethrough(isJobFinished);
        }
    }

    /** make the internal checkbox not editable */
    public void freeze() {
        checkThreadConfinement();
        doneCheckBox.setDisable(true);
    }

    /** Get todojob's id, may be null */
    public Integer getTodoJobId() {
        checkThreadConfinement();
        return model.getId();
    }

    private void updateTimeLeftLabel() {
        boolean isDone = model.isDone();
        LocalDate begin = isDone ? model.getActualEndDate() : LocalDate.now();
        LocalDate end = model.getExpectedEndDate();
        Period period = Period.between(begin, end);

        int d = period.getDays();
        int m = period.getMonths();
        int y = period.getYears();

        boolean isDelayed = false;
        // only display positive values
        if (d < 0 || m < 0 || y < 0) {
            isDelayed = true;
        }
        // 0 days left
        if (d == 0 && m == 0 && y == 0) {
            if (isDone) // finished, display nothing
                this.timeLeftLabel.setText(ON_TIME_TEXT);
            else  // not yet finished, display 0 days
                this.timeLeftLabel.setText(TODAY_TEXT);
            return;
        }

        // either it's delayed, or its' finished ahead of time
        String s = "";
        if (y != 0) {
            s += Math.abs(y) + " " + YEARS;
        } else if (m != 0) {
            s += Math.abs(m) + " " + MONTHS + " ";
        } else if (d != 0) {
            s += Math.abs(d) + " " + DAYS + " ";
        }
        // only display 'ahead' text when we indeed finished the task
        if (model.isDone() && !isDelayed) {
            s = s + " (" + AHEAD_TEXT + ")";
        } else
            s = isDelayed ? s + " (" + DELAYED_TEXT + ")" : s;
        this.timeLeftLabel.setText(s);
    }

    private void emptyTimeLeftLabel() {
        this.timeLeftLabel.setText("");
    }

    /**
     * Check if the current thread is FX's UI thread
     *
     * @throws ConcurrentModificationException if current thread is not FX's UI thread
     */
    private void checkThreadConfinement() {
        if (!Platform.isFxApplicationThread())
            throw new ConcurrentModificationException(TodoJobView.class.getName() + " should only be used inside UI thread");
    }
}
