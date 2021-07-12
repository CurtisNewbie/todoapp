package com.curtisnewbie.dao;

import java.sql.DatabaseMetaData;

/**
 * A Mapper
 *
 * @author yongjie.zhuang
 */
public interface Mapper {

    /**
     * Execute script
     *
     * @param script script
     */
    void runScript(String script);

    /**
     * Get database's metadata
     */
    DatabaseMetaData getDatabaseMetaData();

}
