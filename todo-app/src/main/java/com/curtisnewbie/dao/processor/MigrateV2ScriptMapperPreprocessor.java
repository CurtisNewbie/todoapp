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

/**
 * Preprocessor that runs script to alter table DDL, and migrate data to new tables if necessary
 * <p>
 * This preprocessor is for pom version v2.0
 * </p>
 *
 * @author yongjie.zhuang
 */
public class MigrateV2ScriptMapperPreprocessor implements MapperPreprocessor {

    private final IOHandler ioHandler = IOHandlerFactory.getIOHandler();
    private final String MIGRATE_V2_SCRIPT = "migrate_v2.sql";
    private final String TODOJOB_TABLE_NAME = "todojob";
    private final Set<String> columnsAddedInV2 = new HashSet<>(Arrays.asList(
            "expected_end_date",
            "actual_end_date"
    ));

    @Override
    public void preprocessMapper(Mapper mapper) {
        Objects.requireNonNull(mapper);

        boolean needToMigrate = true;
        try {
            DatabaseMetaData meta = mapper.getDatabaseMetaData();
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
                mapper.runScript(ioHandler.readResourceAsString(MIGRATE_V2_SCRIPT));
            }
        } catch (Exception e) {
            throw new IllegalStateException("Unable to migrate to V2.0 DDL", e);
        }
    }

    @Override
    public boolean supports(Mapper mapper) {
        Objects.requireNonNull(mapper);
        return true;
    }
}
