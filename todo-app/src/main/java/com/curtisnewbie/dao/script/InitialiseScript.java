package com.curtisnewbie.dao.script;

import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Preprocessor that runs script to initialise tables if necessary
 *
 * @author yongjie.zhuang
 */
@Slf4j
public class InitialiseScript extends AbstractScript implements PreInitializationScript {

    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    private static final String INIT_SCRIPT = "init.sql";

    @Override
    public void preInitialize(ScriptRunner runner, Connection conn) throws SQLException {
        log.info("Attempt to run initialization script for tables that are not yet created");
        try {
            String script = ioHandler.readResourceAsString(INIT_SCRIPT);
            runner.runScript(conn, script);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to run tables' initialization script: " + INIT_SCRIPT, e);
        }
    }
}
