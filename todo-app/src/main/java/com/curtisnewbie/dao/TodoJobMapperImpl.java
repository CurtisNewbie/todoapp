package com.curtisnewbie.dao;

import com.curtisnewbie.entity.TodoJob;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yongjie.zhuang
 */
public final class TodoJobMapperImpl implements TodoJobMapper {

    private final Connection connection;

    public TodoJobMapperImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<TodoJob> findById(int id) {
        return null;
    }

    @Override
    public List<TodoJob> findByPage(int page, int limit) {
        return null;
    }

    @Override
    public List<TodoJob> findBetweenDates(LocalDate startDate, LocalDate endDate) {
        return null;
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
