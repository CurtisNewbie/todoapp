package com.curtisnewbie.controller;

import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.util.CheckBoxFactory;
import com.curtisnewbie.util.DateUtil;
import com.curtisnewbie.util.LabelFactory;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.Date;

/**
 * <p>
 * A "view" object representing a to-do job
 * </p>
 *
 * @author yongjie.zhuang
 */
public class TodoJobView extends HBox {

    private static final String CHECKBOX_NAME = "DONE:";
    private static final float WRAP_RATIO = 0.7f;

    /**
     * {@code TodoJob} that this view represents
     */
    private final TodoJob todoJob;
    /**
     * The name of this {@code TodoJob}
     */
    private final Label nameLabel;
    /**
     * The date when this {@code TodoJob} is created
     */
    private final Label startDateLabel;
    /**
     * Whether the {@code TodoJob} is finished
     */
    private final CheckBox doneCb = CheckBoxFactory.getClassicCheckBox();

    /**
     * Create a TodoJobView with the given {@code name}
     *
     * @param name
     */
    public TodoJobView(String name, Controller controller) {
        this.todoJob = new TodoJob(name);
        this.nameLabel = LabelFactory.getClassicLabel(name);
        this.nameLabel.prefWidthProperty().bind(this.widthProperty().multiply(WRAP_RATIO).subtract(15));
        this.startDateLabel = LabelFactory.getRightPaddedLabel(DateUtil.toDateStrSlash(new Date()));
        this.doneCb.setSelected(false);
        this.doneCb.setOnAction(e -> {
            this.todoJob.setDone(doneCb.isSelected());
            controller.sortListView();
        });
        this.getChildren().addAll(startDateLabel, nameLabel, expandingBox(),
                LabelFactory.getLeftPaddedLabel(CHECKBOX_NAME), doneCb);
        HBox.setHgrow(this, Priority.SOMETIMES);
    }

    /**
     * Create a TodoJobView with the given {@code todoJob}
     *
     * @param todoJob
     */
    public TodoJobView(TodoJob todoJob, Controller controller) {
        this.todoJob = todoJob;
        this.nameLabel = LabelFactory.getClassicLabel(todoJob.getName());
        this.nameLabel.prefWidthProperty().bind(this.widthProperty().multiply(WRAP_RATIO).subtract(15));
        this.startDateLabel = LabelFactory.getClassicLabel(DateUtil.toDateStrSlash(todoJob.getStartDate()));
        this.doneCb.setSelected(todoJob.isDone());
        this.doneCb.setOnAction(e -> {
            this.todoJob.setDone(doneCb.isSelected());
            controller.sortListView();
        });
        this.getChildren().addAll(startDateLabel, nameLabel, expandingBox(),
                LabelFactory.getLeftPaddedLabel(CHECKBOX_NAME), doneCb);
        HBox.setHgrow(this, Priority.SOMETIMES);
    }

    public TodoJob getTodoJob() {
        return todoJob;
    }

    public Label getStartDateLabel() {
        return startDateLabel;
    }

    public CheckBox getDoneCb() {
        return doneCb;
    }

    private HBox expandingBox(){
        HBox box = new HBox();
        HBox.setHgrow(box, Priority.SOMETIMES);
        return box;
    }
}
