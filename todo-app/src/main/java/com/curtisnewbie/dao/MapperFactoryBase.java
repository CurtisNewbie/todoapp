package com.curtisnewbie.dao;

import com.curtisnewbie.dao.script.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Factory of Mapper
 *
 * @author yongjie.zhuang
 */
@Slf4j
public class MapperFactoryBase implements MapperFactory {

    private static volatile TodoJobMapper todoJobMapper = null;
    private static final String DB_NAME = "todoapp.db";
    private static final String DIR_NAME = "todo-app";
    private static final String DB_ABS_PATH;
    private static final Connection conn;

    //  will be removed after initialization
    private List<PreInitializationScript> preInitScripts = new ArrayList<>(Arrays.asList(
            new InitialiseScript()
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

    @Override
    public String getDatabaseAbsolutePath() {
        return DB_ABS_PATH;
    }

    @Override
    public CompletableFuture<TodoJobMapper> getTodoJobMapperAsync() {
        if (todoJobMapper != null) {
            return CompletableFuture.completedFuture(todoJobMapper);
        }

        return CompletableFuture.supplyAsync(() -> {
            synchronized (this) {
                if (todoJobMapper != null) return todoJobMapper;

                final SimpleScriptRunner scriptRunner = new SimpleScriptRunner();
                try {
                    for (PreInitializationScript s : preInitScripts) {
                        s.preInitialize(scriptRunner, conn);
                    }
                    preInitScripts = null;
                } catch (SQLException e) {
                    throw new IllegalStateException("Failed to run pre-initialize scripts", e);
                }
                todoJobMapper = new TodoJobMapperImpl(conn);
                return todoJobMapper;
            }
        }).exceptionally(e -> {
            throw new IllegalStateException("Failed to initialize TodoJobMapper", e);
        });
    }
}
