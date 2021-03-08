package com.curtisnewbie.dao;

import java.sql.Connection;

/**
 * @author yongjie.zhuang
 */
public final class TodoJobMapperImpl implements TodoJobMapper {

    private final Connection connection;

    public TodoJobMapperImpl(Connection connection) {
        this.connection = connection;
    }
}
