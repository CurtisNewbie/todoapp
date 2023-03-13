package com.curtisnewbie.common;

import com.curtisnewbie.controller.TodoJobView;
import com.curtisnewbie.dao.TodoJob;
import javafx.scene.control.Label;
import javafx.scene.text.Text;

/**
 * @author yongj.zhuang
 */
public class GlobalPools {

    public static final SimplePool<TodoJob> todoJobPool = new SimplePool<>("todoJobPool", 0, TodoJob::new);
    public static final SimplePool<TodoJobView> todoJobViewPool = new SimplePool<>("todoJobViewPool", 0, TodoJobView::new);
    public static final SimplePool<Label> labelPool = new SimplePool<>("labelPool", 0, Label::new);
    public static final SimplePool<Text> textPool= new SimplePool<>("textPool", 0, Text::new);

}
