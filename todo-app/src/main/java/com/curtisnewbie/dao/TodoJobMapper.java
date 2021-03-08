package com.curtisnewbie.dao;

import com.curtisnewbie.entity.TodoJob;

import java.time.LocalDate;
import java.util.List;

/**
 * @author yongjie.zhuang
 */
public interface TodoJobMapper {

    List<TodoJob> findById(int id);

    List<TodoJob> findByPage(int page, int limit);

    List<TodoJob> findBetweenDates(LocalDate startDate, LocalDate endDate);

    LocalDate findEarliestDate();

    LocalDate findLatestDate();

    TodoJob updateById(TodoJob todoJob);

    TodoJob deleteById(int id);
}
