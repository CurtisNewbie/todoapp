package com.curtisnewbie.io;

import com.curtisnewbie.config.Config;
import com.curtisnewbie.entity.TodoJob;

import java.io.File;
import java.util.List;

/**
 * <p>
 * Class that handles I/O related operations.
 * </p>
 *
 * @author zhuangyongj
 */
public interface IOHandler {

    /**
     * Load a list of {@code TodoJob} from disk
     *
     * @param savePath path to where the job-list is saved
     * @return a list of {@code TodoJob}
     */
    List<TodoJob> loadTodoJob(String savePath);

    /**
     * Generate the configuration file if not exists
     */
    void generateConfIfNotExists();

    /**
     * Load {@code Config} from file
     *
     * @return configuration entity
     */
    Config readConfig();

    /**
     * Save the job list to file (that can be loaded later on)
     *
     * @param jobs     job-list
     * @param savePath path to where the job-list is saved
     */
    void writeTodoJob(List<TodoJob> jobs, String savePath);

    /**
     * Export job list (in a human-readable form) to file.
     *
     * @param jobs job-list
     * @param file file that the jobs exported to
     */
    void exportTodoJob(List<TodoJob> jobs, File file);

    /**
     * Get Conf file path
     */
    String getConfPath();
}
