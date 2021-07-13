package com.curtisnewbie.dao.processor;

import com.curtisnewbie.dao.Mapper;
import com.curtisnewbie.dao.MapperPreprocessor;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerFactory;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * Preprocessor that runs script to initialise tables if necessary
 *
 * @author yongjie.zhuang
 */
public class InitialiseScriptMapperPreprocessor implements MapperPreprocessor {

    private static final Logger logger = Logger.getLogger(InitialiseScriptMapperPreprocessor.class.getName());
    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    private final String INIT_SCRIPT = "init.sql";

    @Override
    public void preprocessMapper(Mapper mapper) {
        Objects.requireNonNull(mapper);
        logger.info("Attempt to run initialization script for tables that are not yet created");
        try {
            String script = ioHandler.readResourceAsString(INIT_SCRIPT);
            mapper.runScript(script);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to run tables' initialization script: " + INIT_SCRIPT, e);
        }
    }

    @Override
    public boolean supports(Mapper mapper) {
        Objects.requireNonNull(mapper);
        // support all kinds of Mapper
        return true;
    }
}
