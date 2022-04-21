package com.curtisnewbie.dao;

import com.curtisnewbie.util.DateUtil;
import com.curtisnewbie.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

/**
 * @author yongjie.zhuang
 */
@Slf4j
public final class TodoJobMapperImpl extends AbstractMapper implements TodoJobMapper {

    private static final int DEFAULT_PAGE_LIMIT = 15;

    public TodoJobMapperImpl(Connection connection) {
        super(connection);
    }

    @Override
    public CompletableFuture<List<TodoJob>> findByPageAsync(String name, int page) {
        return CompletableFuture.supplyAsync(() -> findByPage(name, page));
    }

    @Override
    public CompletableFuture<List<TodoJob>> findBetweenDatesAsync(String name, LocalDate startDate, LocalDate endDate) {
        return CompletableFuture.supplyAsync(() -> findBetweenDates(name, startDate, endDate));
    }

    @Override
    public CompletableFuture<LocalDate> findEarliestDateAsync() {
        return CompletableFuture.supplyAsync(() -> {
            LocalDate ld = findEarliestDate();
            if (ld == null)
                ld = LocalDate.now();
            return ld;
        });
    }

    @Override
    public CompletableFuture<LocalDate> findLatestDateAsync() {
        return CompletableFuture.supplyAsync(() -> {
            LocalDate ld = findLatestDate();
            if (ld == null)
                ld = LocalDate.now();
            return ld;
        });
    }

    @Override
    public CompletableFuture<Boolean> updateByIdAsync(TodoJob todoJob) {
        return CompletableFuture.supplyAsync(() -> this.updateById(todoJob) > 0);
    }

    @Override
    public CompletableFuture<Boolean> deleteByIdAsync(int id) {
        return CompletableFuture.supplyAsync(() -> deleteById(id) > 0);
    }

    @Override
    public CompletableFuture<Integer> insertAsync(TodoJob todoJob) {
        return CompletableFuture.supplyAsync(() -> insert(todoJob));
    }

    // ------------------------------ helper methods -------------------

    private Integer insert(TodoJob todoJob) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO todojob (name, is_done, expected_end_date) VALUES (?,?,?)", Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, todoJob.getName());
            stmt.setBoolean(2, todoJob.isDone());
            stmt.setDate(3, new java.sql.Date(DateUtil.startTimeOf(todoJob.getExpectedEndDate())));
            int c = stmt.executeUpdate();
            if (c > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
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

    private int deleteById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM todojob WHERE id = ?")) {
            stmt.setInt(1, id);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private int updateById(TodoJob todoJob) {
        Objects.requireNonNull(todoJob);
        Objects.requireNonNull(todoJob.getId());
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE todojob SET name = ?, is_done = ?, expected_end_date = ?, actual_end_date = ? WHERE id = ?")) {
            stmt.setString(1, todoJob.getName());
            stmt.setBoolean(2, todoJob.isDone());
            stmt.setDate(3, new java.sql.Date(DateUtil.startTimeOf(todoJob.getExpectedEndDate())));
            stmt.setDate(4, todoJob.getActualEndDate() != null ?
                    new java.sql.Date(DateUtil.startTimeOf(todoJob.getActualEndDate())) : null);
            stmt.setInt(5, todoJob.getId());
            int res = stmt.executeUpdate();
            return res;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private LocalDate findLatestDate() {
        try (Statement stmt = connection.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT expected_end_date FROM todojob ORDER BY expected_end_date DESC LIMIT 1");
            if (rs.next()) {
                return DateUtil.localDateOf(rs.getDate(1).getTime());
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private LocalDate findEarliestDate() {
        try (Statement stmt = connection.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT expected_end_date FROM todojob ORDER BY expected_end_date ASC LIMIT 1");
            if (rs.next()) {
                return DateUtil.localDateOf(rs.getDate(1).getTime());
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private TodoJob findById(int id) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name, is_done, expected_end_date, actual_end_date FROM todojob WHERE id = ?"
        );) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                TodoJob job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setExpectedEndDate(DateUtil.localDateOf(rs.getDate(4).getTime()));
                job.setActualEndDate(rs.getDate(5) != null ? DateUtil.localDateOf(rs.getDate(5).getTime()) : null);
                return job;
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<TodoJob> findByPage(int page, int limit) {
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be greater than 0");
        if (page <= 0)
            throw new IllegalArgumentException("page must be greater than 0");
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name, is_done, expected_end_date, actual_end_date FROM todojob " +
                        "ORDER BY is_done ASC, actual_end_date DESC, expected_end_date ASC LIMIT ? OFFSET ?");) {
            stmt.setInt(1, limit);
            stmt.setInt(2, (page - 1) * limit);
            ResultSet rs = stmt.executeQuery();
            List<TodoJob> result = new ArrayList<>();
            while (rs.next()) {
                TodoJob job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setExpectedEndDate(DateUtil.localDateOf(rs.getDate(4).getTime()));
                job.setActualEndDate(rs.getDate(5) != null ? DateUtil.localDateOf(rs.getDate(5).getTime()) : null);
                result.add(job);
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<TodoJob> findByPage(int page) {
        return findByPage(page, DEFAULT_PAGE_LIMIT);
    }

    private List<TodoJob> findByPage(String name, int page) {
        return findByPage(name, page, DEFAULT_PAGE_LIMIT);
    }

    private List<TodoJob> findByPage(String name, int page, int limit) {
        if (StrUtil.isEmpty(name))
            return findByPage(page);

        if (limit <= 0)
            throw new IllegalArgumentException("limit must be greater than 0");
        if (page <= 0)
            throw new IllegalArgumentException("page must be greater than 0");
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name, is_done, expected_end_date, actual_end_date FROM todojob " +
                        "WHERE name LIKE ? " +
                        "ORDER BY is_done ASC, actual_end_date DESC, expected_end_date ASC LIMIT ? OFFSET ?");) {

            stmt.setString(1, "%" + name + "%");
            stmt.setInt(2, limit);
            stmt.setInt(3, (page - 1) * limit);
            ResultSet rs = stmt.executeQuery();
            List<TodoJob> result = new ArrayList<>();
            while (rs.next()) {
                TodoJob job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setExpectedEndDate(DateUtil.localDateOf(rs.getDate(4).getTime()));
                job.setActualEndDate(rs.getDate(5) != null ? DateUtil.localDateOf(rs.getDate(5).getTime()) : null);
                result.add(job);
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

    }

    private List<TodoJob> findAll() {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name, is_done, expected_end_date, actual_end_date FROM todojob " +
                        "ORDER BY expected_end_date DESC, is_done ASC");) {
            ResultSet rs = stmt.executeQuery();
            List<TodoJob> result = new ArrayList<>();
            while (rs.next()) {
                TodoJob job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setExpectedEndDate(DateUtil.localDateOf(rs.getDate(4).getTime()));
                job.setActualEndDate(rs.getDate(5) != null ? DateUtil.localDateOf(rs.getDate(5).getTime()) : null);
                result.add(job);
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<TodoJob> findBetweenDates(LocalDate startDate, LocalDate endDate) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name, is_done, expected_end_date, actual_end_date FROM todojob " +
                        "WHERE expected_end_date BETWEEN ? AND ? ORDER BY expected_end_date DESC, is_done ASC");) {
            stmt.setDate(1, new java.sql.Date(DateUtil.startTimeOf(startDate)));
            stmt.setDate(2, new java.sql.Date(DateUtil.startTimeOf(endDate)));
            ResultSet rs = stmt.executeQuery();
            List<TodoJob> result = new ArrayList<>();
            while (rs.next()) {
                TodoJob job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setExpectedEndDate(DateUtil.localDateOf(rs.getDate(4).getTime()));
                job.setActualEndDate(rs.getDate(5) != null ? DateUtil.localDateOf(rs.getDate(5).getTime()) : null);
                result.add(job);
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    private List<TodoJob> findBetweenDates(String name, LocalDate startDate, LocalDate endDate) {
        if (StrUtil.isEmpty(name))
            return findBetweenDates(startDate, endDate);

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, name, is_done, expected_end_date, actual_end_date FROM todojob " +
                        " WHERE (expected_end_date BETWEEN ? AND ?) AND name LIKE ? " +
                        " ORDER BY expected_end_date DESC, is_done ASC");) {
            stmt.setDate(1, new java.sql.Date(DateUtil.startTimeOf(startDate)));
            stmt.setDate(2, new java.sql.Date(DateUtil.startTimeOf(endDate)));
            stmt.setString(3, "%" + name + "%");
            ResultSet rs = stmt.executeQuery();
            List<TodoJob> result = new ArrayList<>();
            while (rs.next()) {
                TodoJob job = new TodoJob();
                job.setId(rs.getInt(1));
                job.setName(rs.getString(2));
                job.setDone(rs.getBoolean(3));
                job.setExpectedEndDate(DateUtil.localDateOf(rs.getDate(4).getTime()));
                job.setActualEndDate(rs.getDate(5) != null ? DateUtil.localDateOf(rs.getDate(5).getTime()) : null);
                result.add(job);
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

}
