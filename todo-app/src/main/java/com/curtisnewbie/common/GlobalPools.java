package com.curtisnewbie.common;

import com.curtisnewbie.controller.TodoJobListView;
import com.curtisnewbie.dao.TodoJob;

/**
 * @author yongj.zhuang
 */
public class GlobalPools {

    public static final SimplePool<TodoJob> todoJobPool = new SimplePool<>(15, TodoJob::new);

}
