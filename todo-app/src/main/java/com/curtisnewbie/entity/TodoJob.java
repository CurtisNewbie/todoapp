package com.curtisnewbie.entity;

import com.curtisnewbie.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * <p>
 * Class that represents a "to-do" job
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoJob {

    /** primary key */
    private Integer id;

    /**
     * Name of the job
     */
    private String name;

    /**
     * Indicate where the job is done
     */
    private boolean done;

    /**
     * Expected end date of this job
     */
    private LocalDate expectedEndDate;

    /**
     * Actual end date
     */
    private LocalDate actualEndDate;

    public TodoJob() {

    }

    public TodoJob(String name) {
        this.name = name;
        this.done = false;
        this.expectedEndDate = LocalDate.now();
        this.actualEndDate = null;
    }

    public TodoJob(TodoJob copied) {
        this.id = copied.id;
        this.name = copied.name;
        this.done = copied.done;
        this.expectedEndDate = copied.expectedEndDate;
        this.actualEndDate = copied.actualEndDate;
    }

    @JsonGetter("expectedEndDate")
    public long expectedEndDateSerializer() {
        return DateUtil.startTimeOf(expectedEndDate);
    }

    @JsonSetter("expectedEndDate")
    public void expectedEndDateDeserializer(long expectedEndDate) {
        this.expectedEndDate = LocalDate.ofInstant(new Date(expectedEndDate).toInstant(), ZoneId.systemDefault());
    }

    @JsonGetter("actualEndDate")
    public long actualEndDateSerializer() {
        return DateUtil.startTimeOf(actualEndDate);
    }

    @JsonSetter("actualEndDate")
    public void actualEndDateDeserializer(Long actualEndDate) {
        if (actualEndDate == null)
            this.expectedEndDate = null;
        else
            this.expectedEndDate = LocalDate.ofInstant(new Date(actualEndDate).toInstant(), ZoneId.systemDefault());
    }
}
