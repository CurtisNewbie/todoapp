package com.curtisnewbie.dao;

import com.curtisnewbie.util.CountdownTimer;
import com.curtisnewbie.util.DateUtil;
import com.curtisnewbie.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author yongjie.zhuang
 */
@Slf4j
public final class TodoJobMapperImpl extends AbstractMapper implements TodoJobMapper {

    private static final int DEFAULT_PAGE_LIMIT = 30;

    public TodoJobMapperImpl(Connection connection) {
        super(connection);
    }

    @Override
    public TodoJob findById(int id) {
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

    @Override
    public List<TodoJob> findByPage(int page, int limit) {
        if (limit <= 0)
            throw new IllegalArgumentException("limit must be greater than 0");
        if (page <= 0)
            throw new IllegalArgumentException("page must be greater than 0");
        CountdownTimer timer = new CountdownTimer();
        timer.start();
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
            timer.stop();
            log.debug(String.format("%s found: %d records, took: %.2f milliseconds\n", stmt.toString(), result.size(), timer.getMilliSec()));
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
    public List<TodoJob> findByPage(String name, int page) {
        return findByPage(name, page, DEFAULT_PAGE_LIMIT);
    }

    @Override
    public Mono<List<TodoJob>> findByPageAsync(String name, int page) {
        return Mono.create(sink -> {
            sink.success(findByPage(name, page));
        });
    }

    @Override
    public List<TodoJob> findByPage(String name, int page, int limit) {
        if (StrUtil.isEmpty(name))
            return findByPage(page);

        if (limit <= 0)
            throw new IllegalArgumentException("limit must be greater than 0");
        if (page <= 0)
            throw new IllegalArgumentException("page must be greater than 0");
        CountdownTimer timer = new CountdownTimer();
        timer.start();
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
            timer.stop();
            log.debug(String.format("%s found: %d records, took: %.2f milliseconds\n", stmt.toString(), result.size(), timer.getMilliSec()));
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }

    }

    @Override
    public List<TodoJob> findAll() {
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

    @Override
    public List<TodoJob> findBetweenDates(LocalDate startDate, LocalDate endDate) {
        CountdownTimer timer = new CountdownTimer();
        timer.start();
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
            timer.stop();
            log.debug(String.format("%s found: %d records, took: %.2f milliseconds\n", stmt.toString(), result.size(), timer.getMilliSec()));
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public List<TodoJob> findBetweenDates(String name, LocalDate startDate, LocalDate endDate) {
        if (StrUtil.isEmpty(name))
            return findBetweenDates(startDate, endDate);

        CountdownTimer timer = new CountdownTimer();
        timer.start();
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
            timer.stop();
            log.debug(String.format("%s found: %d records, took: %.2f milliseconds\n", stmt.toString(), result.size(), timer.getMilliSec()));
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Mono<List<TodoJob>> findBetweenDatesAsync(String name, LocalDate startDate, LocalDate endDate) {
        return Mono.create(sink -> {
            sink.success(findBetweenDates(name, startDate, endDate));
        });
    }

    @Override
    public LocalDate findEarliestDate() {
        CountdownTimer timer = new CountdownTimer();
        timer.start();
        try (Statement stmt = connection.createStatement();) {
            ResultSet rs = stmt.executeQuery("SELECT expected_end_date FROM todojob ORDER BY expected_end_date ASC LIMIT 1");
            if (rs.next()) {
                timer.stop();
                log.debug(String.format("Find earliest date took: %.2f milliseconds\n", timer.getMilliSec()));
                return DateUtil.localDateOf(rs.getDate(1).getTime());
            }
            return null;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Mono<LocalDate> findEarliestDateAsync() {
        return Mono.create(sink -> {
            LocalDate ld = findEarliestDate();
            if (ld == null)
                ld = LocalDate.now();
            sink.success(ld);
        });
    }

    @Override
    public LocalDate findLatestDate() {
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

    @Override
    public Mono<LocalDate> findLatestDateAsync() {
        return Mono.create(sink -> {
            LocalDate ld = findLatestDate();
            if (ld == null)
                ld = LocalDate.now();
            sink.success(ld);
        });
    }

    @Override
    public int updateById(TodoJob todoJob) {
        Objects.requireNonNull(todoJob);
        Objects.requireNonNull(todoJob.getId());
        CountdownTimer timer = new CountdownTimer();
        timer.start();
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE todojob SET name = ?, is_done = ?, expected_end_date = ?, actual_end_date = ? WHERE id = ?")) {
            stmt.setString(1, todoJob.getName());
            stmt.setBoolean(2, todoJob.isDone());
            stmt.setDate(3, new java.sql.Date(DateUtil.startTimeOf(todoJob.getExpectedEndDate())));
            stmt.setDate(4, todoJob.getActualEndDate() != null ?
                    new java.sql.Date(DateUtil.startTimeOf(todoJob.getActualEndDate())) : null);
            stmt.setInt(5, todoJob.getId());
            int res = stmt.executeUpdate();
            timer.stop();
            log.debug(String.format("Update took %.2f milliseconds", timer.getMilliSec()));
            return res;
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Mono<Boolean> updateByIdAsync(TodoJob todoJob) {
        return Mono.create(sink -> {
            sink.success(this.updateById(todoJob) > 0);
        });
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
    public Mono<Boolean> deleteByIdAsync(int id) {
        return Mono.create(sink -> {
            sink.success(deleteById(id) > 0);
        });
    }

    @Override
    public Integer insert(TodoJob todoJob) {
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

    @Override
    public Mono<Integer> insertAsync(TodoJob todoJob) {
        return Mono.create(sink -> {
            Integer id = insert(todoJob);
            if (id == null)
                sink.error(new SQLException("Unable to insert todo"));
            sink.success(id);
        });
    }

    @Override
    public int countRows() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM todojob")) {
            ResultSet rs = stmt.executeQuery();
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
            ResultSet rs = stmt.executeQuery();
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
