package com.curtisnewbie.dao;

import com.curtisnewbie.common.Cleanable;
import com.curtisnewbie.util.DateUtil;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;

import java.time.*;
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
public class TodoJob implements Cleanable {

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

    public void withName(String name) {
        this.name = name;
        this.done = false;
        this.expectedEndDate = LocalDate.now();
        this.actualEndDate = null;
    }

    @JsonGetter("expectedEndDate")
    public long expectedEndDateSerializer() {
        return DateUtil.startTimeOf(expectedEndDate);
    }

    @JsonSetter("expectedEndDate")
    public void expectedEndDateDeserializer(long expectedEndDate) {
        this.expectedEndDate = Instant.ofEpochMilli(expectedEndDate).atZone(ZoneId.systemDefault()).toLocalDate();
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
            this.expectedEndDate = Instant.ofEpochMilli(actualEndDate).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    @Override
    public String toString() {
        return "TodoJob{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", done=" + done +
                ", expectedEndDate=" + expectedEndDate +
                ", actualEndDate=" + actualEndDate +
                '}';
    }

    @Override
    public void clean() {
        this.id = null;
        this.name = null;
        this.done = false;
        this.expectedEndDate = null;
        this.actualEndDate = null;
    }
}
