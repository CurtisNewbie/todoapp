package com.curtisnewbie.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
    private Date startDate;

    public TodoJob() {
    }

    public TodoJob(String name) {
        this.name = name;
        this.done = false;
        this.startDate = new Date();
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
}
