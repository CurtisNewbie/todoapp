package com.curtisnewbie.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * Simple implementation of {@link ScriptRunner}
 *
 * @author yongjie.zhuang
 */
public class SimpleScriptRunner implements ScriptRunner {

    private static final Logger logger = Logger.getLogger(SimpleScriptRunner.class.getName());
    private final String COMMENT_PREFIX = "--";
    private final String SPACE = " ";

    @Override
    public void runScript(Connection connection, String script) throws SQLException {
        if (script == null || script.isEmpty())
            return;

        String[] seg = script.split("\\n");
        StringBuilder sb = new StringBuilder();
        for (String s : seg) {
            s = extractNonCommentedPart(s.trim());
            if (s.isEmpty())
                continue;
            else
                sb.append(SPACE).append(s);
        }

        String[] commands = sb.toString().split(";");
        for (String c : commands) {
            doExecute(connection, c);
        }
    }

    void doExecute(Connection connection, String command) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            logger.info("Do execute: \n" + command + "\n");
            stmt.execute(command);
        }
    }

    String extractNonCommentedPart(String s) {
        int index = s.indexOf(COMMENT_PREFIX);
        if (index == -1)
            return s;
        if (index == 0)
            return "";
        else
            return s.substring(0, index);
    }
}
