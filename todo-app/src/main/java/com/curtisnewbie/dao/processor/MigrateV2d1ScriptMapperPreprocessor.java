package com.curtisnewbie.dao.processor;

import com.curtisnewbie.dao.Mapper;
import com.curtisnewbie.dao.MapperPreprocessor;
import com.curtisnewbie.io.IOHandler;
import com.curtisnewbie.io.IOHandlerFactory;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Preprocessor that runs script to alter table DDL, and migrate data to new tables if necessary
 * <p>
 * This preprocessor is for pom version v2.1
 * </p>
 *
 * @author yongjie.zhuang
 */
public class MigrateV2d1ScriptMapperPreprocessor implements MapperPreprocessor {

    private static final Logger logger = Logger.getLogger(MigrateV2d1ScriptMapperPreprocessor.class.getName());
    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    private final String MIGRATE_V2_SCRIPT = "migrate_v2.1.sql";
    private final String TODOJOB_TABLE_NAME = "todojob";
    private final Set<String> columnsAddedInV2 = new HashSet<>(Arrays.asList(
            "expected_end_date",
            "actual_end_date"
    ));

    @Override
    public void preprocessMapper(Mapper mapper) {
        Objects.requireNonNull(mapper);

        logger.info("Checking whether we should migrate to V2.1");
        boolean needToMigrate = true;
        try {
            DatabaseMetaData meta = mapper.getDatabaseMetaData();
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
                logger.info("Migrating to V2.1");
                String script = ioHandler.readResourceAsString(MIGRATE_V2_SCRIPT);
                mapper.runScript(script);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to migrate to V2.1 DDL", e);
        }
    }

    @Override
    public boolean supports(Mapper mapper) {
        Objects.requireNonNull(mapper);
        return true;
    }
}
