package com.curtisnewbie.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;

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
        TodoJobView jobView = new TodoJobView("Feels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels " +
                "gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels gucciFeels " +
                "gucci");
        listView.getItems().add((TodoJobView) jobView);
        jobView.prefWidthProperty().bind(listView.widthProperty());
        listView.getItems().add(new TodoJobView("Feels gucci"));
        listView.getItems().add(new TodoJobView("Feels gucci"));
        listView.getItems().add(new TodoJobView("Feels gucci"));
    }
}
