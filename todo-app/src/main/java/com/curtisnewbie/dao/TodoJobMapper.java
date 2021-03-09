package com.curtisnewbie.dao;

import com.curtisnewbie.entity.TodoJob;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yongjie.zhuang
 */
public interface TodoJobMapper {

    TodoJob findById(int id);

    List<TodoJob> findByPage(int page, int limit);

    List<TodoJob> findBetweenDates(LocalDate startDate, LocalDate endDate);

    LocalDate findEarliestDate();

    LocalDate findLatestDate();

    /**
     * Update record
     *
     * @return number of rows affected
     * @throws SQLException
     */
    int updateById(TodoJob todoJob) ;

    /**
     * Delete record
     *
     * @return number of rows affected
     */
    int deleteById(int id);

    /**
     * Insert record
     *
     * @return primary key
     */
    Integer insert(TodoJob todoJob);
}
