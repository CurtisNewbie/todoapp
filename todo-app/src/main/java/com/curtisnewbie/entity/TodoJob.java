package com.curtisnewbie.entity;

import java.util.Date;

/**
 * <p>
 * Class that represents a "to-do" job
 * </p>
 *
 * @author yongjie.zhuang
 */
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

    /**
     * End date of this job
     */
    private Date endDate;

    public TodoJob() {
    }

    public TodoJob(String name) {
        this.name = name;
        this.done = false;
        this.startDate = new Date();
        this.endDate = new Date();
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

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public String toString() {
        return "TodoJob{" + "name='" + name + '\'' + ", done=" + done + ", startDate=" + startDate + ", endDate=" + endDate + '}';
    }
}
