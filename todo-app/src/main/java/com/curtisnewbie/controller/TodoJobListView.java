package com.curtisnewbie.controller;

import com.curtisnewbie.config.*;
import com.curtisnewbie.dao.*;
import com.curtisnewbie.util.*;
import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.beans.*;
import java.util.*;

import static com.curtisnewbie.config.PropertyConstants.*;


/**
 * ListView for TodoJobView
 *
 * @author yongj.zhuang
 */
@RequiresFxThread
public class TodoJobListView extends BorderPane {

    private static final int LISTVIEW_PADDING = 55;
    private final ListView<TodoJobView> listView = new ListView<>();
    private PropertyChangeListener propertyChangeListener;

    public TodoJobListView() {
        this.setCenter(listView);
    }

    public void onModelChanged(PropertyChangeListener propertyChangeListener) {
        this.propertyChangeListener = propertyChangeListener;
    }

    public void onKeyPressed(EventHandler<KeyEvent> onKeyPressed) {
        listView.setOnKeyPressed(onKeyPressed);
    }

    /** Load Todos into ListView */
    public void clearAndLoadList(List<TodoJob> list, Environment environment) {
        listView.getItems().clear();

        if (list == null)
            return;

        list.stream()
                .map(t -> new TodoJobView(t, environment))
                .forEach(this::displayTodoJobView);
    }

    public void setContextMenu(ContextMenu contextMenu) {
        listView.setContextMenu(contextMenu);
    }

    public int getSelectedIndex() {
        return listView.getSelectionModel().getSelectedIndex();
    }

    public TodoJobView get(int index) {
        return listView.getItems().get(index);
    }

    public TodoJobView remove(int index) {
        return listView.getItems().remove(index);
    }

    public boolean isEmpty() {
        return listView.getItems().isEmpty();
    }

    // ------------------------------------ private helper methods ------------------------

    /**
     * <p>
     * Load {@code TodoJobView} into the {@code ListView}.
     * </p>
     * <p>
     * The operation of adding the jobView to the ListView is always executed in Javafx's thread
     * </p>
     */
    private void displayTodoJobView(TodoJobView jobView) {
        if (propertyChangeListener != null)
            jobView.onModelChange(propertyChangeListener);

        jobView.prefWidthProperty().bind(listView.widthProperty().subtract(LISTVIEW_PADDING));
        jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(LISTVIEW_PADDING)
                .subtract(Integer.parseInt(PropertiesLoader.getInstance().getLocalizedProperty(TODO_VIEW_TEXT_WRAP_WIDTH_KEY))));
        listView.getItems().add(jobView);
    }

}
