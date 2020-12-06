package com.curtisnewbie.controller;

import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.exception.EventHandlerRegisteredException;
import com.curtisnewbie.util.*;
import com.curtisnewbie.callback.OnEvent;
import javafx.application.Platform;
import javafx.beans.binding.DoubleBinding;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import java.util.Date;

import static com.curtisnewbie.util.MarginFactory.wrapWithCommonPadding;

/**
 * <p>
 * A "view" object representing a to-do job
 * </p>
 *
 * @author yongjie.zhuang
 */
public class TodoJobView extends HBox {

    private static final String CHECKBOX_NAME = "DONE:";
    public static final int WIDTH_FOR_LABELS = 170;

    private final Label doneLabel;
    /**
     * The name of this {@code TodoJob}
     */
    private final Text nameText;
    /**
     * Start Date in milliseconds since EPOCH
     */
    private long startDate;
    /**
     * The date when this {@code TodoJob} is created
     */
    private final Label startDateLabel;
    /**
     * Whether the {@code TodoJob} is finished
     */
    private final CheckBox doneCb = CheckBoxFactory.getClassicCheckBox();

    private volatile OnEvent doneCbRegisteredHandler;

    /**
     * Create a TodoJobView with the given {@code todoJob}
     *
     * @param todoJob
     */
    public TodoJobView(TodoJob todoJob) {
        this.doneLabel = new Label();
        updateDoneLabelGraphic(todoJob.isDone());
        this.nameText = TextFactory.getClassicText(todoJob.getName());
        this.startDateLabel = LabelFactory.getClassicLabel(DateUtil.toDateStrSlash(todoJob.getStartDate()));
        this.startDate = todoJob.getStartDate().getTime();
        this.doneCb.setSelected(todoJob.isDone());
        this.doneCb.setOnAction(this::onDoneCbActionEventHandler);
        this.getChildren()
                .addAll(doneLabel, MarginFactory.fixedMargin(3), startDateLabel, MarginFactory.fixedMargin(10), wrapWithCommonPadding(nameText),
                        MarginFactory.expandingMargin(), LabelFactory.getLeftPaddedLabel(CHECKBOX_NAME), doneCb);
        HBox.setHgrow(this, Priority.SOMETIMES);
        this.requestFocus();
    }

    public void updateDate(Date date) {
        this.startDate = date.getTime();
        this.startDateLabel.setText(DateUtil.toDateStrSlash(date));
    }

    /**
     * Bind the wrapping width of text
     */
    public void bindTextWrappingWidthProperty(final DoubleBinding binding) {
        nameText.wrappingWidthProperty().bind(binding);
    }

    public void setName(String txt) {
        this.nameText.setText(txt);
    }

    public String getName() {
        return this.nameText.getText();
    }

    public long getStartDate() {
        return this.startDate;
    }

    /**
     * Retrieves information of current {@code TodoJobView} and put them in a new {@code TodoJob}
     *
     * @return todoJob
     */
    public TodoJob createTodoJobCopy() {
        TodoJob copy = new TodoJob();
        copy.setName(nameText.getText());
        copy.setDone(doneCb.isSelected());
        copy.setStartDate(new Date(startDate));
        return copy;
    }

    /**
     * <p>
     * Register an event handler for the "done" check box
     * </p>
     *
     * @param onEvent
     * @throws EventHandlerRegisteredException if this method is invoked for multiple times for the same object
     */
    public void regDoneCbEventHandler(OnEvent onEvent) {
        if (this.doneCbRegisteredHandler != null)
            throw new EventHandlerRegisteredException();
        synchronized (this) {
            this.doneCbRegisteredHandler = onEvent;
        }
    }

    private void onDoneCbActionEventHandler(ActionEvent e) {
        updateDoneLabelGraphic(doneCb.isSelected());
        if (doneCbRegisteredHandler != null)
            doneCbRegisteredHandler.react();
    }

    private void updateDoneLabelGraphic(boolean isDone) {
        Platform.runLater(() -> {
            this.doneLabel.setGraphic(isDone ? ShapeFactory.greenCircle() : ShapeFactory.redCircle());
        });
    }
}
