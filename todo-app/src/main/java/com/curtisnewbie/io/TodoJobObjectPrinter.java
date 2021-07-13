package com.curtisnewbie.io;

import com.curtisnewbie.config.Environment;
import com.curtisnewbie.config.Language;
import com.curtisnewbie.config.PropertiesLoader;
import com.curtisnewbie.config.PropertyConstants;
import com.curtisnewbie.dao.TodoJob;

import static com.curtisnewbie.util.DateUtil.toMMddUUUUSlash;

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
        String done = PropertiesLoader.getInstance().get(PropertyConstants.TEXT_DONE_PREFIX, environment.getLanguage());
        String inProgress = PropertiesLoader.getInstance().get(PropertyConstants.TEXT_IN_PROGRESS_PREFIX, environment.getLanguage());
        String status = todoJob.isDone() ? done : inProgress;
        int width = environment.getLanguage().equals(Language.ENG) ? 13 : 5;
        return String.format("%-" + width + "s %s-%s '%s'\n",
                "[" + status + "]",
                toMMddUUUUSlash(todoJob.getExpectedEndDate()),
                todoJob.getActualEndDate() != null ? toMMddUUUUSlash(todoJob.getActualEndDate()) : "__/__/____",
                formatName(todoJob.getName()));
    }

    private static String formatName(String name) {
        return name.replaceAll("\\n", "\n  ");
    }
}
