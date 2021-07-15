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

    private PropertiesLoader propertiesLoader;
    private Environment environment;

    public TodoJobObjectPrinter(PropertiesLoader propertiesLoader, Environment environment) {
        this.propertiesLoader = propertiesLoader;
        this.environment = environment;
    }

    @Override
    public String printObject(TodoJob todoJob) {
        String done = PropertiesLoader.getInstance().getLocalizedProperty(PropertyConstants.TEXT_DONE_KEY);
        String inProgress = PropertiesLoader.getInstance().getLocalizedProperty(PropertyConstants.TEXT_IN_PROGRESS_KEY);
        String status = todoJob.isDone() ? done : inProgress;
        int width = environment.getLanguage().equals(Language.ENG) ? 13 : 5;
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
