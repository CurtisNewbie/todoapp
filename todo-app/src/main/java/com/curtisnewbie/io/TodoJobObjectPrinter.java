package com.curtisnewbie.io;

import com.curtisnewbie.config.Environment;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.config.PropertyConstants;
import com.curtisnewbie.dao.TodoJob;

import static com.curtisnewbie.util.DateUtil.toDDmmUUUUSlash;

/**
 * ObjectPrinter for {@link TodoJob}
 *
 * @author yongjie.zhuang
 */
public class TodoJobObjectPrinter implements ObjectPrinter<TodoJob> {

    private static final int ENG_WIDTH = 13;
    private static final int CN_WIDTH = 5;

    private PropertiesLoader propertiesLoader;
    private Environment environment;

    public TodoJobObjectPrinter(PropertiesLoader propertiesLoader, Environment environment) {
        this.propertiesLoader = propertiesLoader;
        this.environment = environment;
    }

    @Override
    public String printObject(TodoJob todoJob) {
        String done = propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_DONE_KEY);
        String inProgress = propertiesLoader.getLocalizedProperty(PropertyConstants.TEXT_IN_PROGRESS_KEY);
        String status = todoJob.isDone() ? done : inProgress;
        int width = environment.getLanguage().equals(Language.ENG) ? ENG_WIDTH : CN_WIDTH;
        return String.format("%-" + width + "s Expected: %s - Actual: %s \n\t%s\n",
                "[" + status + "]",
                toDDmmUUUUSlash(todoJob.getExpectedEndDate()),
                todoJob.getActualEndDate() != null ? toDDmmUUUUSlash(todoJob.getActualEndDate()) : "__/__/____",
                formatContent(todoJob.getName()));
    }

    private static String formatContent(String content) {
        return content.replaceAll("\\n", "\n\t") + "\n";
    }
}
