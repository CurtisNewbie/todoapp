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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        listView.getItems().add(new TodoJobView("Feels gucci"));
        listView.getItems().add(new TodoJobView("Feels gucci"));
        listView.getItems().add(new TodoJobView("Feels gucci"));
        listView.getItems().add(new TodoJobView("Feels gucci"));
    }
}
