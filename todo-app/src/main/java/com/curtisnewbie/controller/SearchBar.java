package com.curtisnewbie.controller;

import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.util.FxThreadConfinement;
import com.curtisnewbie.util.FxThreadUtil;
import com.curtisnewbie.util.LabelFactory;
import com.curtisnewbie.util.MarginFactory;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.curtisnewbie.config.PropertyConstants.TEXT_SEARCH;

/**
 * <p>Search bar</p>
 *
 * @author yongjie.zhuang
 */
@FxThreadConfinement
public class SearchBar extends HBox {

    private final TextField searchTextField = new TextField();
    private final PropertiesLoader properties = PropertiesLoader.getInstance();
    private final List<Runnable> onSearchTextFieldEnterPressed = new ArrayList<>();
    private boolean searchTextChanged = false;
    private String prevSearchText = "";

    public SearchBar() {
        searchTextField.setOnKeyReleased(e -> {
            if (!Objects.equals(e.getText(), prevSearchText)) {
                searchTextChanged = true;
                prevSearchText = e.getText();
            }

            if (e.getCode().equals(KeyCode.ENTER)) {
                for (Runnable r : onSearchTextFieldEnterPressed) {
                    r.run();
                }
            }
        });

        this.setAlignment(Pos.BASELINE_RIGHT);
        this.getChildren().addAll(
                MarginFactory.fixedMargin(20),
                LabelFactory.classicLabel(properties.getLocalizedProperty(TEXT_SEARCH)),
                MarginFactory.fixedMargin(10),
                searchTextField,
                MarginFactory.expandingMargin(),
                MarginFactory.fixedMargin(10)
        );
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
}
