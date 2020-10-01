package com.curtisnewbie.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * <p>
 * Controller for UI (fxml)
 * </p>
 *
 * @author yongjie.zhuang
 */
public class Controller implements Initializable {

    @FXML
    private ListView<TodoJobView> listView;

    /**
     * Add {@code TodoJobView} into the {@code ListView}
     *
     * @param jobView
     */
    public void addTodoJobView(TodoJobView jobView) {
        jobView.prefWidthProperty().bind(listView.widthProperty());
        listView.getItems().add(jobView);
    }

    /**
     * Add new {@code TodoJobView} into the {@code ListView}
     *
     * @param jobName
     */
    public void addTodoJobView(String jobName) {
        TodoJobView jobView = new TodoJobView(jobName);
        addTodoJobView(jobView);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TodoJobView jobView = new TodoJobView("Feels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels " +
                "gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels " + "gucci");
        addTodoJobView(jobView);
        addTodoJobView("Todo job 1");
        addTodoJobView("Todo job 2");
        addTodoJobView("Todo job 3");
    }
}
