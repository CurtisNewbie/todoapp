package com.curtisnewbie.controller;

import com.curtisnewbie.common.GlobalPools;
import com.curtisnewbie.config.*;
import com.curtisnewbie.dao.*;
import com.curtisnewbie.util.*;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;

import java.beans.*;
import java.util.*;
import java.util.stream.Collectors;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.FxThreadUtil.*;


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
        checkThreadConfinement();

        final List<TodoJobView> copy = new ArrayList<>(listView.getItems());
        listView.getItems().clear();
        copy.forEach(GlobalPools.todoJobViewPool::returnT);

        if (list == null) return;

        listView.getItems().addAll(list.stream()
                .map(t -> GlobalPools.todoJobViewPool.borrowT().init(t, environment))
                .peek(this::prepTodoJobView)
                .collect(Collectors.toList()));
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

    private void prepTodoJobView(TodoJobView jobView) {
        if (propertyChangeListener != null) jobView.onModelChange(propertyChangeListener);
        jobView.prefWidthProperty().bind(listView.widthProperty().subtract(LISTVIEW_PADDING));
        jobView.bindTextWrappingWidthProperty(listView.widthProperty().subtract(LISTVIEW_PADDING)
                .subtract(Integer.parseInt(PropertiesLoader.getInstance().getLocalizedProperty(TODO_VIEW_TEXT_WRAP_WIDTH_KEY))));
    }

}
