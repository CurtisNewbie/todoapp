package com.curtisnewbie.dao;

import com.curtisnewbie.entity.TodoJob;
import com.curtisnewbie.util.DateUtil;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
                job.setStartDate(DateUtil.localDateOf(rs.getDate(4)));
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
    public LocalDate findEarliestDate() {
        return null;
    }

    @Override
    public LocalDate findLatestDate() {
        return null;
    }

    @Override
    public TodoJob updateById(TodoJob todoJob) {
        return null;
    }

    @Override
    public TodoJob deleteById(int id) {
        return null;
    }
}
