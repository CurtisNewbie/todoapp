package com.curtisnewbie.io;

import com.curtisnewbie.config.Config;
import com.curtisnewbie.dao.TodoJob;
import com.curtisnewbie.exception.FailureToLoadException;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Function;

/**
 * <p>
 * Class that handles I/O related operations.
 * </p>
 *
 * @author yongjie.zhuang
 */
public interface IOHandler {

    /**
     * Load a list of {@code TodoJob} from disk
     *
     * @param file where the job-list is saved
     * @return a list of {@code TodoJob}
     */
    List<TodoJob> loadTodoJob(File file) throws FailureToLoadException;

    /**
     * Generate the configuration file if not exists
     */
    void generateConfIfNotExists();

    /**
     * Attempt to write (or overwrite) config file in an asynchronous way
     */
    void writeConfigAsync(Config config);

    /**
     * Load {@code Config} from file
     *
     * @return configuration entity
     */
    Config readConfig();

    /**
     * Writer content to file
     *
     * @param file file
     */
    void writeObjectsAsync(String content, File file);

    /**
     * Get Conf file path
     */
    String getConfPath();

    /**
     * Read file in resources/ folder as string
     *
     * @param relPath relative path under resources/ folder
     * @throws IOException when file is not found or some other I/O related error occurred while reading the file
     */
    String readResourceAsString(String relPath) throws IOException;
}
