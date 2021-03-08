package com.curtisnewbie.entity;

import com.curtisnewbie.util.DateUtil;
import com.fasterxml.jackson.annotation.*;

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
@JsonIgnoreProperties(ignoreUnknown = true)
public class TodoJob {

    /** primary key */
    @JsonIgnore
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
     * Create date of this job
     */
    private LocalDate startDate;

    public TodoJob() {
    }

    public TodoJob(String name) {
        this.name = name;
        this.done = false;
        this.startDate = LocalDate.now();
    }

    public TodoJob(TodoJob copied) {
        this.name = copied.name;
        this.done = copied.done;
        this.startDate = copied.startDate;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate date) {
        this.startDate = date;
    }

    @JsonSetter("startDate")
    public void startDateSerializer(long startDate) {
        this.startDate = LocalDate.ofInstant(new Date(startDate).toInstant(), ZoneId.systemDefault());
    }

    public Integer getId() {
        return id;
    }

    public TodoJob setId(Integer id) {
        this.id = id;
        return this;
    }

    @JsonGetter("startDate")
    public long startDateDeserializer() {
        return DateUtil.startTimeOf(startDate);
    }


}
