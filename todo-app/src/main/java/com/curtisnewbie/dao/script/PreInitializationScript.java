package com.curtisnewbie.dao.script;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * <p>
 * Script that will be executed before any mapper is created
 * </p>
 *
 * @author yongjie.zhuang
 */
public interface PreInitializationScript {

    void preInitialize(ScriptRunner runner, Connection connection) throws SQLException;

}
