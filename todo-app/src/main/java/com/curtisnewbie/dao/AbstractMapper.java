package com.curtisnewbie.dao;

import com.curtisnewbie.util.StrUtil;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Abstract implementation of Mapper
 *
 * @author yongjie.zhuang
 */
public class AbstractMapper implements Mapper {

    private final ScriptRunner scriptRunner = new SimpleScriptRunner();
    protected final Connection connection;

    public AbstractMapper(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void runScript(String script) {
        if (StrUtil.isEmpty(script)) {
            throw new IllegalArgumentException("Script is empty or null, unable to execute it.");
        }
        try {
            scriptRunner.runScript(connection, script);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
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
