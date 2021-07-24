package com.curtisnewbie.controller;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;

/**
 * <p>
 * Convenient extension of {@code ContextMenu}
 * </p>
 *
 * @author yongjie.zhuang
 */
public class CnvCtxMenu extends ContextMenu {

    /**
     * Add new {@code MenuItem} with the given {@code itemName}, and register it with the given {@code EventHandler}
     *
     * @param itemName name of {@code MenuItem}
     * @param eh       EventHandler
     * @return current {@code JobCtxMenu}, this is convenient for method chaining
     */
    public CnvCtxMenu addMenuItem(String itemName, EventHandler<ActionEvent> eh) {
        var menuItem = new MenuItem(itemName);
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
