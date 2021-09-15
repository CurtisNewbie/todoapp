package com.curtisnewbie.dao.script;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * <p>
 * Abstract script
 * </p>
 *
 * @author yongjie.zhuang
 */
public abstract class AbstractScript implements PreInitializationScript {

    /**
     * Get database metadata
     */
    protected DatabaseMetaData getDatabaseMetaData(Connection conn) {
        try {
            return conn.getMetaData();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

}
