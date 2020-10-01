package com.curtisnewbie.controller;

import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.util.CheckBoxFactory;
import com.curtisnewbie.util.DateUtil;
import com.curtisnewbie.util.LabelFactory;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

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
    private static final float WRAP_RATIO = 0.6f;

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
    public TodoJobView(String name) {
        this.todoJob = new TodoJob();
        this.nameLabel = LabelFactory.getClassicLabel(name);
        this.nameLabel.prefWidthProperty().bind(this.widthProperty().multiply(WRAP_RATIO));
        this.startDateLabel = LabelFactory.getClassicLabel(DateUtil.toDateStr(new Date()));
        this.doneCb.setSelected(false);
        this.getChildren().addAll(startDateLabel, nameLabel, LabelFactory.getLeftPaddedLabel(CHECKBOX_NAME), doneCb);
    }

    /**
     * Create a TodoJobView with the given {@code todoJob}
     *
     * @param todoJob
     */
    public TodoJobView(TodoJob todoJob) {
        this.todoJob = todoJob;
        this.nameLabel = LabelFactory.getClassicLabel(todoJob.getName());
        this.nameLabel.prefWidthProperty().bind(this.widthProperty().multiply(WRAP_RATIO));
        this.startDateLabel = LabelFactory.getClassicLabel(DateUtil.toDateStr(todoJob.getStartDate()));
        this.doneCb.setSelected(todoJob.isDone());
        this.getChildren().addAll(startDateLabel, nameLabel, LabelFactory.getLeftPaddedLabel(CHECKBOX_NAME), doneCb);
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
}
