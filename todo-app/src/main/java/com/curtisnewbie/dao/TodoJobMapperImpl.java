package com.curtisnewbie.dao;

import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.util.CountdownTimer;
import com.curtisnewbie.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * @author yongjie.zhuang
 */
public final class TodoJobMapperImpl implements TodoJobMapper {

    private static final Logger logger = Logger.getLogger(TodoJobMapperImpl.class.getName());
    private static final int DEFAULT_PAGE_LIMIT = 30;
    private final Connection connection;

    public TodoJobMapperImpl(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
        createIndexesIfNotExists();
    }

    private void createTableIfNotExists() {
        String initTableSql = "CREATE TABLE IF NOT EXISTS todojob (\n" +
                "    id INTEGER PRIMARY KEY AUTOINCREMENT, -- \"Primary key\"\n" +
                "    name VARCHAR(255) NOT NULL,\n" +
                "    is_done TINYINT NOT NULL, -- \"1-true, 0-false\"\n" +
                "    start_date DATE NOT NULL\n" +
                ")";
        try (Statement stmt = connection.createStatement();) {
            stmt.executeUpdate(initTableSql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private void createIndexesIfNotExists() {
        String initIdxSql = "CREATE INDEX IF NOT EXISTS sort_idx ON todojob (is_done ASC, start_date DESC)";
        try (Statement stmt = connection.createStatement();) {
            stmt.executeUpdate(initIdxSql);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public TodoJob findById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id, name, is_done, start_date FROM todojob WHERE id = ?");) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                var job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setStartDate(DateUtil.localDateOf(rs.getDate(4)));
                return job;
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<TodoJob> findByPage(int page, int limit) {
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be greater than 0");
        if (page <= 0)
            throw new IllegalArgumentException("page must be greater than 0");
        CountdownTimer timer = new CountdownTimer();
        timer.start();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id, name, is_done, start_date FROM todojob " +
                "ORDER BY is_done ASC, start_date DESC LIMIT ? OFFSET ?");) {
            stmt.setInt(1, limit);
            stmt.setInt(2, (page - 1) * limit);
            ResultSet rs = stmt.executeQuery();
            List<TodoJob> result = new ArrayList<>();
            while (rs.next()) {
                var job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setStartDate(DateUtil.localDateOf(rs.getDate(4).getTime()));
                result.add(job);
            }
            timer.stop();
            logger.info(String.format("Found: %d records, took: %.2f milliseconds\n", result.size(), timer.getMilliSec()));
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<TodoJob> findByPage(int page) {
        return findByPage(page, DEFAULT_PAGE_LIMIT);
    }

    @Override
    public List<TodoJob> findAll() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id, name, is_done, start_date FROM todojob " +
                "ORDER BY start_date DESC, is_done ASC");) {
            ResultSet rs = stmt.executeQuery();
            List<TodoJob> result = new ArrayList<>();
            while (rs.next()) {
                var job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setStartDate(DateUtil.localDateOf(rs.getDate(4).getTime()));
                result.add(job);
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<TodoJob> findBetweenDates(LocalDate startDate, LocalDate endDate) {
        CountdownTimer timer = new CountdownTimer();
        timer.start();
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id, name, is_done, start_date FROM todojob " +
                "WHERE start_date BETWEEN ? AND ? ORDER BY start_date DESC, is_done ASC");) {
            stmt.setDate(1, new java.sql.Date(DateUtil.startTimeOf(startDate)));
            stmt.setDate(2, new java.sql.Date(DateUtil.startTimeOf(endDate)));
            ResultSet rs = stmt.executeQuery();
            List<TodoJob> result = new ArrayList<>();
            while (rs.next()) {
                var job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setStartDate(DateUtil.localDateOf(rs.getDate(4).getTime()));
                result.add(job);
            }
            timer.stop();
            logger.info(String.format("Found: %d records, took: %.2f milliseconds\n", result.size(), timer.getMilliSec()));
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public LocalDate findEarliestDate() {
        CountdownTimer timer = new CountdownTimer();
        timer.start();
        try (Statement stmt = connection.createStatement();) {
            var rs = stmt.executeQuery("SELECT start_date FROM todojob ORDER BY start_date ASC LIMIT 1");
            if (rs.next()) {
                timer.stop();
                logger.info(String.format("Find earliest date took: %.2f milliseconds\n", timer.getMilliSec()));
                return DateUtil.localDateOf(rs.getDate(1).getTime());
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public LocalDate findLatestDate() {
        try (Statement stmt = connection.createStatement();) {
            var rs = stmt.executeQuery("SELECT start_date FROM todojob ORDER BY start_date DESC LIMIT 1");
            if (rs.next()) {
                return DateUtil.localDateOf(rs.getDate(1).getTime());
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int updateById(TodoJob todoJob) {
        Objects.requireNonNull(todoJob);
        Objects.requireNonNull(todoJob.getId());
        CountdownTimer timer = new CountdownTimer();
        timer.start();
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE todojob SET name = ?, is_done = ?, start_date = ? WHERE id = ?")) {
            stmt.setString(1, todoJob.getName());
            stmt.setBoolean(2, todoJob.isDone());
            stmt.setDate(3, new java.sql.Date(DateUtil.startTimeOf(todoJob.getStartDate())));
            stmt.setInt(4, todoJob.getId());
            int res = stmt.executeUpdate();
            timer.stop();
            logger.info(String.format("Update took %.2f milliseconds", timer.getMilliSec()));
            return res;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int deleteById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM todojob WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Integer insert(TodoJob todoJob) {
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO todojob (name, is_done, start_date) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, todoJob.getName());
            stmt.setBoolean(2, todoJob.isDone());
            stmt.setDate(3, new java.sql.Date(DateUtil.startTimeOf(todoJob.getStartDate())));
            int c = stmt.executeUpdate();
            if (c > 0) {
                try (var rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public int countRows() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM todojob")) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public boolean hasRecord() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM todojob LIMIT 1")) {
            var rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }
}
