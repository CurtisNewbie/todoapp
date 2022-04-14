package com.curtisnewbie.dao;

import com.curtisnewbie.dao.script.*;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Factory of Mapper
 *
 * @author yongjie.zhuang
 */
@Slf4j
public class MapperFactoryBase implements MapperFactory {

    private static final String DB_NAME = "todoapp.db";
    private static final String DIR_NAME = "todo-app";
    private static final String DB_ABS_PATH;
    private static final Connection conn;

    //  will be removed after initialization
    private List<PreInitializationScript> preInitScripts = new ArrayList<>(Arrays.asList(
            new InitialiseScript()
    ));
    //  will be removed after initialization
    private ScriptRunner scriptRunner = new SimpleScriptRunner();

    /** Whether the initialize scripts are finished */
    private final AtomicBoolean isInitialized = new AtomicBoolean();

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
        runPreInitializeScriptAsync();
    }

    private void runPreInitializeScriptAsync() {
        CompletableFuture.runAsync(() -> {
            try {
                for (PreInitializationScript s : preInitScripts) {
                    s.preInitialize(scriptRunner, conn);
                }
            } catch (SQLException e) {
                throw new IllegalStateException("Failed to run pre-initialize scripts", e);
            } finally {
                isInitialized.set(true);
                _doPostConstruct();
            }
        }).exceptionally(e -> {
            log.error("Failed to run pre-initialize scripts", e);
            return null;
        });
    }

    @Override
    public String getDatabaseAbsolutePath() {
        return DB_ABS_PATH;
    }

    @Override
    public CompletableFuture<TodoJobMapper> getNewTodoJobMapperAsync() {
        return CompletableFuture.supplyAsync(() -> {
            while (!isInitialized())
                ; // what till the scripts are executed

            return new TodoJobMapperImpl(conn);
        });
    }

    /**
     * Whether the Mapper Factory is fully initialized
     */
    private boolean isInitialized() {
        return isInitialized.get();
    }

    private void _doPostConstruct() {
        preInitScripts = null;
        scriptRunner = null;
    }
}
