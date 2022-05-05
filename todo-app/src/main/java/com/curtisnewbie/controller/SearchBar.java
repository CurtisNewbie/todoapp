package com.curtisnewbie.controller;

import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.util.RequiresFxThread;
import com.curtisnewbie.util.FxThreadUtil;
import javafx.beans.property.DoubleProperty;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.curtisnewbie.config.PropertyConstants.TEXT_SEARCH;
import static com.curtisnewbie.util.FxThreadUtil.*;
import static com.curtisnewbie.util.LabelFactory.classicLabel;
import static com.curtisnewbie.util.MarginFactory.margin;
import static com.curtisnewbie.util.MarginFactory.padding;

/**
 * <p>Search bar</p>
 *
 * @author yongjie.zhuang
 */
@RequiresFxThread
public class SearchBar extends VBox implements Refreshable {

    private static final PropertiesLoader propertiesLoader = PropertiesLoader.getInstance();
    private final TextField searchTextField = new TextField();
    private final List<Runnable> onSearchTextFieldEnterPressed = new ArrayList<>();
    private boolean searchTextChanged = false;
    private String prevSearchText = "";
    private boolean searchOnTypeEnabled = false;

    public SearchBar() {
        checkThreadConfinement();
        searchTextField.setOnKeyReleased(e -> {
            if (!Objects.equals(e.getText(), prevSearchText)) {
                searchTextChanged = true;
                prevSearchText = e.getText();
            }

            if (searchOnTypeEnabled || e.getCode().equals(KeyCode.ENTER)) {
                for (Runnable r : onSearchTextFieldEnterPressed) {
                    r.run();
                }
            }
        });

        refresh();
    }

    /**
     * Get searchTextField's {@code prefWidthProperty()}
     */
    public DoubleProperty searchTextFieldPrefWidthProperty() {
        FxThreadUtil.checkThreadConfinement();
        return searchTextField.prefWidthProperty();
    }

    /**
     * Set runnable called when 'Enter' key is pressed for searchTextField
     */
    public void onSearchTextFieldEnterPressed(Runnable runnable) {
        FxThreadUtil.checkThreadConfinement();
        Objects.requireNonNull(runnable);
        this.onSearchTextFieldEnterPressed.add(runnable);
    }

    public TextField getSearchTextField() {
        return searchTextField;
    }

    public boolean isSearchTextChanged() {
        return searchTextChanged;
    }

    public void setSearchTextChanged(boolean searchTextChanged) {
        this.searchTextChanged = searchTextChanged;
    }

    public boolean isSearchOnTypeEnabled() {
        return searchOnTypeEnabled;
    }

    public void setSearchOnTypeEnabled(boolean searchOnTypeEnabled) {
        this.searchOnTypeEnabled = searchOnTypeEnabled;
    }

    @Override
    public void refresh() {
        checkThreadConfinement();
        this.getChildren().clear();
        final String label = propertiesLoader.getLocalizedProperty(TEXT_SEARCH);
        this.getChildren().addAll(
                margin(5),
                padding(classicLabel(label), 0, 5, 0, 5),
                margin(5),
                padding(searchTextField, 0, 5, 15, 5),
                margin(5)
        );
    }
}
