package com.curtisnewbie.controller;

import com.curtisnewbie.callback.OnEvent;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.config.PropertyConstants;
import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.exception.EventHandlerAlreadyRegisteredException;
import com.curtisnewbie.util.*;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import java.time.LocalDate;

import static com.curtisnewbie.util.MarginFactory.wrapWithCommonPadding;

/**
 * <p>
 * A "view" object representing a to-do job
 * </p>
 *
 * @author yongjie.zhuang
 */
public class TodoJobView extends HBox {
    public static final int WIDTH_FOR_LABELS = 170;
    private final String checkboxName;
    private final Label doneLabel;
    private final Object mutex = new Object();
    /**
     * The name of this {@code TodoJob}
     */
    private final Text nameText;
    /**
     * Start Date in milliseconds since EPOCH
     */
    private LocalDate startDate;
    /**
     * The date when this {@code TodoJob} is created
     */
    private final Label startDateLabel;
    /**
     * Whether the {@code TodoJob} is finished
     */
    private final CheckBox doneCb = CheckBoxFactory.getClassicCheckBox();

    private OnEvent doneCbRegisteredHandler;

    /**
     * Create a TodoJobView with the given {@code todoJob}
     *
     * @param todoJob
     */
    public TodoJobView(TodoJob todoJob, Language lang) {
        this.checkboxName = PropertiesLoader.getInstance().get(PropertyConstants.TEXT_DONE_PREFIX, lang);
        this.doneLabel = new Label();
        this.nameText = TextFactory.getClassicText(todoJob.getName());
        this.startDateLabel = LabelFactory.getClassicLabel(DateUtil.toMMddUUUUSlash(todoJob.getStartDate()));
        this.startDate = todoJob.getStartDate();
        this.doneCb.setSelected(todoJob.isDone());
        this.doneCb.setOnAction(this::onDoneCbActionEventHandler);
        this.getChildren()
                .addAll(doneLabel, MarginFactory.fixedMargin(3), startDateLabel, MarginFactory.fixedMargin(10), wrapWithCommonPadding(nameText),
                        MarginFactory.expandingMargin(), LabelFactory.getLeftPaddedLabel(checkboxName), doneCb);
        HBox.setHgrow(this, Priority.SOMETIMES);
        updateGraphicForJobState(todoJob.isDone());
        this.requestFocus();
    }

    public void setStartDate(LocalDate date) {
        synchronized (mutex) {
            this.startDate = date;
            this.startDateLabel.setText(DateUtil.toMMddUUUUSlash(date));
        }
    }

    public LocalDate getStartDate() {
        synchronized (mutex) {
            return this.startDate;
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

    public boolean isSelected() {
        synchronized (mutex) {
            return doneCb.isSelected();
        }
    }

    /**
     * Retrieves information of current {@code TodoJobView} and put them in a new {@code TodoJob}
     *
     * @return todoJob
     */
    public TodoJob createTodoJobCopy() {
        TodoJob copy = new TodoJob();
        copy.setName(getName());
        copy.setDone(isSelected());
        copy.setStartDate(getStartDate());
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
    public void regDoneCbEventHandler(OnEvent onEvent) {
        synchronized (mutex) {
            if (doneCbRegisteredHandler != null)
                throw new EventHandlerAlreadyRegisteredException();
            this.doneCbRegisteredHandler = onEvent;
        }
    }

    private void onDoneCbActionEventHandler(ActionEvent e) {
        synchronized (mutex) {
            updateGraphicForJobState(doneCb.isSelected());
            if (doneCbRegisteredHandler != null)
                doneCbRegisteredHandler.react();
        }
    }

    private void updateGraphicForJobState(boolean isDone) {
        Platform.runLater(() -> {
            this.doneLabel.setGraphic(isDone ? ShapeFactory.greenCircle() : ShapeFactory.redCircle());
            this.nameText.setStrikethrough(isDone);
        });
    }

    /** make the internal checkbox uneditable */
    public final void freeze() {
        synchronized (mutex) {
            if (!doneCb.isDisable())
                doneCb.setDisable(true);
        }
    }
}
