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

    Mono<List<TodoJob>> findByPageAsync(String name, int page);

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
     * Update record
     *
     * @return is record updated or not
     * @throws SQLException
     */
    Mono<Boolean> updateByIdAsync(TodoJob todoJob);

    /**
     * Delete record
     *
     * @return number of rows affected
     */
    int deleteById(int id);

    /**
     * Delete record
     *
     * @return is record deleted or not
     */
    Mono<Boolean> deleteByIdAsync(int id);

    /**
     * Insert record
     *
     * @return primary key
     */
    Integer insert(TodoJob todoJob);

    /**
     * <p>
     * Insert record
     * </p>
     * <p>
     * id is always returned when the operation succeeds, if not, an exception is returned that can be caught in
     * onError
     * </p>
     *
     * @return primary key
     */
    Mono<Integer> insertAsync(TodoJob todoJob);

    /** Count total number of rows */
    int countRows();

    /** Check if current DB has any record */
    boolean hasRecord();
}
