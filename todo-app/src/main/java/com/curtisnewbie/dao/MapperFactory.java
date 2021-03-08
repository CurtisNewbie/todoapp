package com.curtisnewbie.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Factory of Mapper
 *
 * @author yongjie.zhuang
 */
public final class MapperFactory {

    private static final String DB_NAME = "todoapp.db";
    private static final String DIR_NAME = "todo-app";
    private static final MapperFactory INSTANCE = new MapperFactory();

    private Connection conn;

    private MapperFactory() {
        try {
            String baseDir = System.getProperty("user.home") + File.separator + DIR_NAME;
            new File(baseDir).mkdirs();
            conn = DriverManager.getConnection("jdbc:sqlite:" + baseDir + File.separator + DB_NAME);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public static MapperFactory getFactory() {
        return INSTANCE;
    }

    public TodoJobMapper getTodoJobMapper() {
        return new TodoJobMapperImpl(this.conn);
    }
}
