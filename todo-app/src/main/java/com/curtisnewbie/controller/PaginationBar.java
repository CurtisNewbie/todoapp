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
@RequiresFxThread
public class PaginationBar extends HBox {

    private final Button prevPageBtn = ButtonFactory.getArrowLeftBtn();
    private final Button nextPageBtn = ButtonFactory.getArrowRightBtn();
    private final Label currPageLabel = LabelFactory.classicLabel("1");

    public PaginationBar() {
        this.setAlignment(Pos.BASELINE_RIGHT);
        PropertiesLoader properties = PropertiesLoader.getInstance();
        this.getChildren().addAll(LabelFactory.classicLabel(properties.getLocalizedProperty(TEXT_PAGE)),
                MarginFactory.margin(10),
                currPageLabel, MarginFactory.margin(10),
                prevPageBtn, MarginFactory.margin(10),
                nextPageBtn, MarginFactory.margin(10));
    }

    public PaginationBar(int page){
        this();
        setCurrPage(page);
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
