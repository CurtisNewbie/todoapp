package com.curtisnewbie.io;

import com.curtisnewbie.config.Config;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.exception.FailureToLoadException;

import java.io.File;
import java.util.List;

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
     * @param savePath path to where the job-list is saved
     * @return a list of {@code TodoJob}
     */
    List<TodoJob> loadTodoJob(String savePath) throws FailureToLoadException;

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
     * Save the job list to file (that can be loaded later on) in a synchronous way
     *
     * @param jobs     job-list
     * @param savePath path to where the job-list is saved
     */
    void writeTodoJobSync(List<TodoJob> jobs, String savePath);

    /**
     * Save the job list to file (that can be loaded later on) in an asynchronous way
     *
     * @param jobs     job-list
     * @param savePath path to where the job-list is saved
     */
    void writeTodoJobAsync(List<TodoJob> jobs, String savePath);

    /**
     * Export job list (in a human-readable form) to file
     *
     * @param jobs job-list
     * @param file file that the jobs exported to
     * @param lang the language to use
     */
    void exportTodoJob(List<TodoJob> jobs, File file, Language lang);

    /**
     * Get Conf file path
     */
    String getConfPath();
}
