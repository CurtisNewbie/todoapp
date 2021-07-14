package com.curtisnewbie.dao;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Runner of scripts
 *
 * @author yongjie.zhuang
 */
public interface ScriptRunner {

    /**
     * Run script using the given connection
     *
     * @param connection connection
     * @param script     script (that may contains multiple statements)
     */
    void runScript(Connection connection, String script) throws SQLException;
}
