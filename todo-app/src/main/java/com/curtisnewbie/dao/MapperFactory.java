package com.curtisnewbie.dao;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory of Mapper
 *
 * @author yongjie.zhuang
 */
public final class MapperFactory {

    private static final String DB_NAME = "todoapp.db";
    private static final String DIR_NAME = "todo-app";
    private static final List<MapperPreprocessor> mapperPreprocessors = new ArrayList<>(Arrays.asList(
            new MigrateV2ScriptMapperPreprocessor(),
            new InitialiseScriptMapperPreprocessor()
    ));
    private static final Connection conn;

    static {
        try {
            String baseDir = System.getProperty("user.home") + File.separator + DIR_NAME;
            new File(baseDir).mkdirs();
            conn = DriverManager.getConnection("jdbc:sqlite:" + baseDir + File.separator + DB_NAME);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get a new mapper
     */
    public static TodoJobMapper getNewTodoJobMapper() {
        TodoJobMapper todoJobMapper = new TodoJobMapperImpl(conn);
        applyPreProcessing(todoJobMapper);
        return todoJobMapper;
    }

    private static void applyPreProcessing(Mapper m) {
        for (MapperPreprocessor p : mapperPreprocessors) {
            if (p.supports(m)) {
                p.preprocessMapper(m);
            }
        }
    }
}
