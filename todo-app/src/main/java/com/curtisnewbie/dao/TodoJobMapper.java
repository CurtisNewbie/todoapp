package com.curtisnewbie.dao;

import com.curtisnewbie.entity.TodoJob;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yongjie.zhuang
 */
public interface TodoJobMapper {

    TodoJob findById(int id) throws SQLException;

    List<TodoJob> findByPage(int page, int limit) throws SQLException;

    List<TodoJob> findBetweenDates(LocalDate startDate, LocalDate endDate) throws SQLException;

    LocalDate findEarliestDate();

    LocalDate findLatestDate();

    TodoJob updateById(TodoJob todoJob);

    TodoJob deleteById(int id);
}
