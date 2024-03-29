package com.curtisnewbie.controller;

import com.curtisnewbie.util.RequiresFxThread;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

import static com.curtisnewbie.util.FxThreadUtil.*;

/**
 * <p>
 * Convenient extension of {@code ContextMenu}
 * </p>
 *
 * @author yongjie.zhuang
 */
@RequiresFxThread
public class CnvCtxMenu extends ContextMenu {

    /**
     * Add new {@code MenuItem} with the given {@code itemName}, and register it with the given {@code EventHandler}
     *
     * @param itemName name of {@code MenuItem}
     * @param eh       EventHandler
     * @return current {@code JobCtxMenu}, this is convenient for method chaining
     */
    public CnvCtxMenu addMenuItem(String itemName, EventHandler<ActionEvent> eh) {
        checkThreadConfinement();
        MenuItem menuItem = new MenuItem(itemName);
        menuItem.setOnAction(eh);
        this.getItems().add(menuItem);
        return this;
    }

    /**
     * Remove all {@MenuItem} registered
     */
    public void clearMenuItems() {
        this.getItems().clear();
    }
}
