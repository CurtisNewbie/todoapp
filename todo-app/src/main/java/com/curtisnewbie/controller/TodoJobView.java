package com.curtisnewbie.controller;

import com.curtisnewbie.common.Cleanable;
import com.curtisnewbie.common.GlobalPools;
import com.curtisnewbie.config.*;
import com.curtisnewbie.dao.TodoJob;
import com.curtisnewbie.util.*;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.time.LocalDate;
import java.time.Period;
import java.util.ConcurrentModificationException;
import java.util.Objects;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.DateUtil.toDDmmUUUUSlash;
import static com.curtisnewbie.util.FxThreadUtil.checkThreadConfinement;
import static com.curtisnewbie.util.LabelFactory.classicLabel;
import static com.curtisnewbie.util.LabelFactory.leftPaddedLabel;
import static com.curtisnewbie.util.MarginFactory.*;
import static com.curtisnewbie.util.TextFactory.*;


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
@RequiresFxThread
public class TodoJobView extends HBox implements Cleanable {
    private static final PropertiesLoader properties = PropertiesLoader.getInstance();

    /** Label for displaying whether to-do is finished */
    private Label doneLabel;

    /** Model inside this view */
    private TodoJob model;

    /** The name of this {@code TodoJob} */
    private Text nameText;

    /** Expected end date in {@link Label} */
    private Label expectedEndDateLabel;

    /** Actual end date in {@link Label} */
    private Label actualEndDateLabel;

    /** Label that displays how much time left before the expected end date */
    private Label timeLeftLabel;

    /** Whether the {@code TodoJob} is finished */
    private CheckBox doneCheckBox;

    /** Property change listener support */
    private PropertyChangeSupport pcs;

    /** Environment configuration */
    private Environment environment;

    private String DAYS;
    private String MONTHS;
    private String YEARS;
    private String DELAYED_TEXT;
    private String AHEAD_TEXT;
    private String ON_TIME_TEXT;
    private String TODAY_TEXT;

    public TodoJobView() {}

    public TodoJobView init(TodoJob todoJob, Environment environment) {
        checkThreadConfinement();
        Objects.requireNonNull(todoJob);
        Objects.requireNonNull(environment);

        DAYS = properties.getLocalizedProperty(TEXT_DAYS_KEY);
        MONTHS = properties.getLocalizedProperty(TEXT_MONTHS_KEY);
        YEARS = properties.getLocalizedProperty(TEXT_YEARS_KEY);
        DELAYED_TEXT = properties.getLocalizedProperty(TEXT_DELAYED_KEY);
        AHEAD_TEXT = properties.getLocalizedProperty(TEXT_AHEAD_KEY);
        ON_TIME_TEXT = properties.getLocalizedProperty(TEXT_ON_TIME_KEY);
        TODAY_TEXT = properties.getLocalizedProperty(TEXT_TODAY_KEY);

        this.pcs = new PropertyChangeSupport(this);
        this.environment = environment;
        this.model = todoJob;
        this.doneLabel = GlobalPools.labelPool.borrowT();
        final String displayedName = environment.isSpecialTagHidden() ? Tag.EXCL.escape(model.getName()) : model.getName();
        this.nameText = getClassicText(displayedName);
        this.timeLeftLabel = classicLabel(null);
        this.doneCheckBox = CheckBoxFactory.getClassicCheckBox();
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
                        margin(3),
                        expectedEndDateLabel,
                        margin(3),
                        actualEndDateLabel,
                        margin(20),
                        wrapWithCommonPadding(nameText),
                        margin(2),
                        timeLeftLabel,
                        margin(2),
                        fillingMargin(),
                        leftPaddedLabel(checkboxName),
                        doneCheckBox);
        HBox.setHgrow(this, Priority.SOMETIMES);
        updateGraphicOnJobStatus(model.isDone());
        this.requestFocus();
        return this;
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
     * Get name
     */
    public String getName() {
        return this.model.getName();
    }

    /**
     * Get copy of current {@code TodoJobView} in forms of a new {@code TodoJob}
     *
     * @return todoJob
     */
    public TodoJob createTodoJobCopy() {
        checkThreadConfinement();
        final TodoJob todoJob = GlobalPools.todoJobPool.borrowT();
        todoJob.setId(model.getId());
        todoJob.setName(model.getName());
        todoJob.setDone(model.isDone());
        todoJob.setExpectedEndDate(model.getExpectedEndDate());
        todoJob.setActualEndDate(model.getActualEndDate());
        return todoJob;
    }

    /**
     * Register a {@link PropertyChangeListener} for a model's change
     * <p>
     * The event value object (e.g., {@link PropertyChangeEvent#getNewValue()}) is {@link TodoJob}
     * </p>
     */
    public void onModelChange(PropertyChangeListener pcl) {
        pcs.addPropertyChangeListener(Event.MODEL_CHANGE.getValue(), pcl);
    }

    private void onDoneCheckBoxSelected(ActionEvent e) {
        TodoJob oldModel = createTodoJobCopy();

        checkThreadConfinement();
        final boolean isTaskDone = ((CheckBox) e.getTarget()).isSelected();
        model.setDone(isTaskDone);
        setActualEndDate(isTaskDone ? LocalDate.now() : null);
        if (isTaskDone)
            emptyTimeLeftLabel();
        else
            updateTimeLeftLabel();
        updateGraphicOnJobStatus(isTaskDone);

        pcs.firePropertyChange(Event.MODEL_CHANGE.getValue(), oldModel, model);
    }

    /** Update graphic based on job's status */
    private void updateGraphicOnJobStatus(boolean isJobFinished) {
        if (isJobFinished)
            this.doneLabel.setGraphic(ShapeFactory.greenCircle());
        else if (isDelayed())
            this.doneLabel.setGraphic(ShapeFactory.redCircle());
        else
            this.doneLabel.setGraphic(ShapeFactory.orangeCircle());

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

    private boolean isDelayed() {
        boolean isDone = model.isDone();
        if (isDone)
            return false;

        LocalDate begin = isDone ? model.getActualEndDate() : LocalDate.now();
        LocalDate end = model.getExpectedEndDate();

        return Period.between(begin, end).isNegative();
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
        } else {
            s = isDelayed ? s + " (" + DELAYED_TEXT + ")" : s;
        }
        this.timeLeftLabel.setText(s);
    }

    private void emptyTimeLeftLabel() {
        this.timeLeftLabel.setText("");
    }

    @Override
    public void clean() {
        this.getChildren().clear();

        Label tmpLabel = this.doneLabel;
        this.doneLabel = null;
        LabelFactory.returnLabel(tmpLabel);

        tmpLabel = this.expectedEndDateLabel;
        this.expectedEndDateLabel = null;
        LabelFactory.returnLabel(tmpLabel);

        tmpLabel = this.actualEndDateLabel;
        this.actualEndDateLabel = null;
        LabelFactory.returnLabel(tmpLabel);

        tmpLabel = this.timeLeftLabel;
        this.timeLeftLabel = null;
        LabelFactory.returnLabel(tmpLabel);

        TodoJob tmpTodo = this.model;
        this.model = null;
        GlobalPools.todoJobPool.returnT(tmpTodo);

        Text tmpText = this.nameText;
        this.nameText = null;
        TextFactory.returnText(tmpText);

        this.doneCheckBox = null;
        this.pcs = null;
        this.environment = null;
    }

    private enum Event {

        /** Event for model's change */
        MODEL_CHANGE("model");

        private final String s;

        Event(String s) {
            this.s = s;
        }

        public String getValue() {
            return s;
        }
    }

    public TodoJob getModel() {
        return this.model;
    }
}
