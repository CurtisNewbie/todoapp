package com.curtisnewbie.dao;

import com.curtisnewbie.util.StrUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;

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

    @Override
    public void runScript(String script) {
        if (StrUtil.isEmpty(script)) {
            throw new IllegalArgumentException("Script is empty or null, unable to execute it.");
        }
        try (Statement stmt = connection.createStatement();) {
            stmt.execute(script);
        } catch (SQLException e) {
            throw new IllegalStateException("Unable to run script: " + script, e);
        }
    }

    @Override
    public DatabaseMetaData getDatabaseMetaData() {
        try {
            return connection.getMetaData();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

}
