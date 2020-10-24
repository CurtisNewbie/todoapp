package com.curtisnewbie.controller;

import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.exception.EventHandlerRegisteredException;
import com.curtisnewbie.util.*;
import com.curtisnewbie.callback.OnEvent;
import javafx.event.ActionEvent;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Text;

import java.util.Date;

import static com.curtisnewbie.util.TextFactory.*;

/**
 * <p>
 * A "view" object representing a to-do job
 * </p>
 *
 * @author yongjie.zhuang
 */
public class TodoJobView extends HBox {

    private static final String CHECKBOX_NAME = "DONE:";

    /**
     * {@code TodoJob} that this view represents
     */
    private final TodoJob todoJob;
    /**
     * The name of this {@code TodoJob}
     */
    private final Text nameText;
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
     * Create a TodoJobView with the given {@code name}
     *
     * @param name
     */
    public TodoJobView(String name) {
        this.todoJob = new TodoJob(name);
        this.nameText = TextFactory.getClassicText(name);
        this.nameText.wrappingWidthProperty().bind(this.widthProperty().multiply(0.7f));
        this.startDateLabel = LabelFactory.getClassicLabel(DateUtil.toDateStrSlash(new Date()));
        this.doneCb.setSelected(false);
        this.doneCb.setOnAction(this::onDoneCbActionEventHandler);
        this.getChildren().addAll(startDateLabel, MarginFactory.fixedMargin(10), wrapWithCommonPadding(nameText),
                MarginFactory.expandingMargin(), LabelFactory.getLeftPaddedLabel(CHECKBOX_NAME), doneCb);
        HBox.setHgrow(this, Priority.SOMETIMES);
    }

    /**
     * Create a TodoJobView with the given {@code todoJob}
     *
     * @param todoJob
     */
    public TodoJobView(TodoJob todoJob) {
        this.todoJob = todoJob;
        this.nameText = TextFactory.getClassicText(todoJob.getName());
        this.nameText.wrappingWidthProperty().bind(this.widthProperty().multiply(0.7f));
        this.startDateLabel = LabelFactory.getClassicLabel(DateUtil.toDateStrSlash(todoJob.getStartDate()));
        this.doneCb.setSelected(todoJob.isDone());
        this.doneCb.setOnAction(this::onDoneCbActionEventHandler);
        this.getChildren().addAll(startDateLabel, MarginFactory.fixedMargin(10), wrapWithCommonPadding(nameText),
                MarginFactory.expandingMargin(), LabelFactory.getLeftPaddedLabel(CHECKBOX_NAME), doneCb);
        HBox.setHgrow(this, Priority.SOMETIMES);
    }

    public TodoJob getTodoJob() {
        return todoJob;
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
        this.doneCbRegisteredHandler = onEvent;
    }

    private void onDoneCbActionEventHandler(ActionEvent e) {
        this.todoJob.setDone(doneCb.isSelected());
        if (doneCbRegisteredHandler != null)
            doneCbRegisteredHandler.react();
    }
}
