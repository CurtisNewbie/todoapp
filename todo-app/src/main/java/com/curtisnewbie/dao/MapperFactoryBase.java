package com.curtisnewbie.dao;

import com.curtisnewbie.dao.processor.InitialiseScriptMapperPreprocessor;
import com.curtisnewbie.dao.processor.MigrateV2d1ScriptMapperPreprocessor;
import reactor.core.publisher.Mono;

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
public class MapperFactoryBase implements MapperFactory {

    private static final String DB_NAME = "todoapp.db";
    private static final String DIR_NAME = "todo-app";
    private static final String DB_ABS_PATH;
    private static final Connection conn;

    private final List<MapperPreprocessor> mapperPreprocessors = new ArrayList<>(Arrays.asList(
            new MigrateV2d1ScriptMapperPreprocessor(),
            new InitialiseScriptMapperPreprocessor()
    ));

    static {
        try {
            String baseDir = System.getProperty("user.home") + File.separator + DIR_NAME;
            new File(baseDir).mkdirs();
            DB_ABS_PATH = baseDir + File.separator + DB_NAME;
            conn = DriverManager.getConnection("jdbc:sqlite:" + DB_ABS_PATH);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    public MapperFactoryBase() {
    }

    @Override
    public String getDatabaseAbsolutePath() {
        return DB_ABS_PATH;
    }

    @Override
    public TodoJobMapper getNewTodoJobMapper() {
        TodoJobMapper todoJobMapper = new TodoJobMapperImpl(conn);
        applyPreProcessing(todoJobMapper);
        return todoJobMapper;
    }

    @Override
    public Mono<TodoJobMapper> getNewTodoJobMapperAsync() {
        return Mono.create(sink -> {
            sink.success(getNewTodoJobMapper());
        });
    }

    private void applyPreProcessing(Mapper m) {
        for (MapperPreprocessor p : mapperPreprocessors) {
            if (p.supports(m)) {
                p.preprocessMapper(m);
            }
        }
    }
}
