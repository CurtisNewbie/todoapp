package com.curtisnewbie.entity;

import lombok.Data;
import lombok.NonNull;

import java.util.Date;

/**
 * <p>
 * Class that represents a "to-do" job
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
public class TodoJob {

    private String name;

    private boolean done;

    private Date startDate;

    private Date endDate;
}
