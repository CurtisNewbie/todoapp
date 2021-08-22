package com.curtisnewbie.dao.processor;

import com.curtisnewbie.dao.Mapper;
import com.curtisnewbie.dao.MapperPreprocessor;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Objects;

/**
 * Preprocessor that runs script to initialise tables if necessary
 *
 * @author yongjie.zhuang
 */
@Slf4j
public class InitialiseScriptMapperPreprocessor implements MapperPreprocessor {

    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    private final String INIT_SCRIPT = "init.sql";

    @Override
    public void preprocessMapper(Mapper mapper) {
        Objects.requireNonNull(mapper);
        log.info("Attempt to run initialization script for tables that are not yet created");
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
