package com.curtisnewbie.dao;

import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * @author yongjie.zhuang
 */
public interface TodoJobMapper extends Mapper {

    TodoJob findById(int id);

    List<TodoJob> findByPage(int page, int limit);

    List<TodoJob> findByPage(int page);

    List<TodoJob> findByPage(String name, int page);

    List<TodoJob> findByPage(String name, int page, int limit);

    List<TodoJob> findAll();

    List<TodoJob> findBetweenDates(LocalDate startDate, LocalDate endDate);

    List<TodoJob> findBetweenDates(String name, LocalDate startDate, LocalDate endDate);

    Mono<List<TodoJob>> findBetweenDatesAsync(String name, LocalDate startDate, LocalDate endDate);

    LocalDate findEarliestDate();

    /**
     * If there is no 'earliest data', now is returned
     */
    Mono<LocalDate> findEarliestDateAsync();

    LocalDate findLatestDate();

    /**
     * If there is no 'latest data', now is returned
     */
    Mono<LocalDate> findLatestDateAsync();

    /**
     * Update record
     *
     * @return number of rows affected
     * @throws SQLException
     */
    int updateById(TodoJob todoJob);

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

    /** Count total number of rows */
    int countRows();

    /** Check if current DB has any record */
    boolean hasRecord();
}
