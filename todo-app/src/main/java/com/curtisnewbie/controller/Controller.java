package com.curtisnewbie.controller;

import com.curtisnewbie.config.Config;
import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerImpl;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

    private Config config;

    private final IOHandler ioHandler = new IOHandlerImpl();

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
        config = ioHandler.readConfig();
        // load previous job list if exists
        for (TodoJob j : ioHandler.loadTodoJob(config.getSavePath())) {
            addTodoJobView(new TodoJobView(j));
        }
    }
}
