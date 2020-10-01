package com.curtisnewbie.controller;

import com.curtisnewbie.App;
import com.curtisnewbie.config.Config;
import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerImpl;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
        // save the whole to-do list on app shutdown
        App.registerOnClose(() -> {
            List<TodoJob> list = listView.getItems().stream().map(TodoJobView::getTodoJob).collect(Collectors.toList());
            ioHandler.writeTodoJob(list, config.getSavePath());
        });
        // register a ContextMenu for the ListView
        listView.setContextMenu(createCtxMenu());
    }

    /**
     * Sort the {@code ListView} based on 1. whether they are finished and 2. their create date
     */
    private void sortListView() {
        listView.getItems().sort((a, b) -> {
            int res = Boolean.compare(a.getTodoJob().isDone(), b.getTodoJob().isDone());
            if (res != 0)
                return res;
            else
                return a.getTodoJob().getStartDate().compareTo(b.getTodoJob().getStartDate());
        });
    }

    private JobCtxMenu createCtxMenu() {
        JobCtxMenu ctxMenu = new JobCtxMenu();
        ctxMenu.addMenuItem("Delete", e -> {
            int selected = listView.getSelectionModel().getSelectedIndex();
            listView.getItems().remove(selected);
        });
        ctxMenu.addMenuItem("Add", e -> {
            Platform.runLater(() -> {
                TextInputDialog dialog = new TextInputDialog("NewJob");
                dialog.setHeaderText("Add New TODO");
                dialog.setTitle("Add a new TODO");
                dialog.setContentText("Enter the name of the TODO:");
                Optional<String> result = dialog.showAndWait();
                if (!result.isEmpty() && !result.get().isBlank()) {
                    addTodoJobView(result.get());
                    sortListView();
                }
            });
        });
        return ctxMenu;
    }
}
