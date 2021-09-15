package com.curtisnewbie.dao.script;

import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerFactory;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Preprocessor that runs script to alter table DDL, and migrate data to new tables if necessary
 * <p>
 * This preprocessor is for pom version v2.1
 * </p>
 *
 * @author yongjie.zhuang
 */
@Slf4j
public class MigrateV2d1Script extends AbstractScript implements PreInitializationScript {

    private static final String MIGRATE_V2_SCRIPT = "migrate_v2.1.sql";
    private static final String TODOJOB_TABLE_NAME = "todojob";
    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    private final Set<String> columnsAddedInV2 = new HashSet<>(Arrays.asList(
            "expected_end_date",
            "actual_end_date"
    ));

    @Override
    public void preInitialize(ScriptRunner runner, Connection conn) throws SQLException {

        log.info("Checking whether we should migrate to V2.1");
        boolean needToMigrate = true;
        try {
            DatabaseMetaData meta = getDatabaseMetaData(conn);
            // check do we have the table at all
            boolean hasTable = false;
            ResultSet tables = meta.getTables(null, null, TODOJOB_TABLE_NAME, null);
            while (tables.next()) {
                String tableName = tables.getString(3);
                if (tableName.equals(TODOJOB_TABLE_NAME)) {
                    hasTable = true;
                }
            }
            if (!hasTable)
                return;

            ResultSet columns = meta.getColumns(null, null, TODOJOB_TABLE_NAME, null);
            while (columns.next()) {
                String colName = columns.getString(4);
                // figure out whether we need to update the DDL by checking the column names
                if (columnsAddedInV2.contains(colName)) {
                    needToMigrate = false;
                    break;
                }
            }
            if (needToMigrate) {
                log.info("Migrating to V2.1");
                String script = ioHandler.readResourceAsString(MIGRATE_V2_SCRIPT);
                runner.runScript(conn, script);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to migrate to V2.1 DDL", e);
        }
    }
}
