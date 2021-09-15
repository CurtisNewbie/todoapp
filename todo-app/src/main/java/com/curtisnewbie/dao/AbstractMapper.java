package com.curtisnewbie.dao;

import java.sql.Connection;

/**
 * Abstract implementation of Mapper
 *
 * @author yongjie.zhuang
 */
public class AbstractMapper implements Mapper {

    protected final Connection connection;

    public AbstractMapper(Connection connection) {
        this.connection = connection;
    }

}
