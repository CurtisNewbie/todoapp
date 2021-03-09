package com.curtisnewbie.dao;

import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author yongjie.zhuang
 */
public final class TodoJobMapperImpl implements TodoJobMapper {

    private final Connection connection;

    public TodoJobMapperImpl(Connection connection) {
        this.connection = connection;
        createTableIfNotExists();
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

    @Override
    public TodoJob findById(int id) throws SQLException {
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
        }
    }

    @Override
    public List<TodoJob> findByPage(int page, int limit) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id, name, is_done, start_date FROM todojob LIMIT ? OFFSET ?");) {
            stmt.setInt(1, limit);
            stmt.setInt(2, page > 0 ? (page - 1) * limit : 0);
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
        }
    }

    @Override
    public List<TodoJob> findBetweenDates(LocalDate startDate, LocalDate endDate) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id, name, is_done, start_date FROM todojob " +
                "WHERE start_date BETWEEN ? AND ?");) {
            stmt.setDate(1, new java.sql.Date(DateUtil.startTimeOf(startDate)));
            stmt.setDate(2, new java.sql.Date(DateUtil.startTimeOf(endDate)));
            ResultSet rs = stmt.executeQuery();
            List<TodoJob> result = new ArrayList<>();
            while (rs.next()) {
                var job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setStartDate(DateUtil.localDateOf(rs.getDate(4)));
                result.add(job);
            }
            return result;
        }
    }

    @Override
    public LocalDate findEarliestDate() throws SQLException {
        try (Statement stmt = connection.createStatement();) {
            var rs = stmt.executeQuery("SELECT start_date FROM todojob LIMIT 1 ORDER BY start_date ASC");
            if (rs.next()) {
                return DateUtil.localDateOf(rs.getDate(1).getTime());
            }
        }
        return null;
    }

    @Override
    public LocalDate findLatestDate() throws SQLException {
        try (Statement stmt = connection.createStatement();) {
            var rs = stmt.executeQuery("SELECT start_date FROM todojob LIMIT 1 ORDER BY start_date DESC");
            if (rs.next()) {
                return DateUtil.localDateOf(rs.getDate(1).getTime());
            }
        }
        return null;
    }

    @Override
    public int updateById(TodoJob todoJob) {
        Objects.requireNonNull(todoJob);
        Objects.requireNonNull(todoJob.getId());

        try (PreparedStatement stmt = connection.prepareStatement("UPDATE todojob SET name, is_done, start_date WHERE id = ? LIMIT 1")) {
            stmt.setInt(1, todoJob.getId());
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int deleteById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM todojob WHERE id = ? LIMIT 1")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
