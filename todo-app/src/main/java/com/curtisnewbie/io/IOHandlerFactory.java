package com.curtisnewbie.io;

/**
 * Factory of {@link IOHandler}
 *
 * @author yongjie.zhuang
 */
public final class IOHandlerFactory {
    private static final IOHandler ioHandler = new IOHandlerImpl();

    private IOHandlerFactory() {

    }

    public static IOHandler getIOHandler() {
        return ioHandler;
    }
}
