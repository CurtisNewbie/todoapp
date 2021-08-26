package com.curtisnewbie.controller;

import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.util.*;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

import static com.curtisnewbie.config.PropertyConstants.TEXT_PAGE;

/**
 * <p>
 * Pagination bar
 * </p>
 *
 * @author yongjie.zhuang
 */
@FxThreadConfinement
public class PaginationBar extends HBox {

    private Button prevPageBtn = ButtonFactory.getArrowLeftBtn();
    private Button nextPageBtn = ButtonFactory.getArrowRightBtn();
    private final Label currPageLabel = LabelFactory.classicLabel("1");
    private final PropertiesLoader properties = PropertiesLoader.getInstance();

    public PaginationBar() {
        this.setAlignment(Pos.BASELINE_RIGHT);
        this.getChildren().addAll(LabelFactory.classicLabel(properties.getLocalizedProperty(TEXT_PAGE)),
                MarginFactory.fixedMargin(10),
                currPageLabel, MarginFactory.fixedMargin(10),
                prevPageBtn, MarginFactory.fixedMargin(10),
                nextPageBtn, MarginFactory.fixedMargin(10));
    }

    public Button getPrevPageBtn() {
        return prevPageBtn;
    }

    public Button getNextPageBtn() {
        return nextPageBtn;
    }

    public void setCurrPage(int page) {
        FxThreadUtil.checkThreadConfinement();
        currPageLabel.setText(String.valueOf(page));
    }
}