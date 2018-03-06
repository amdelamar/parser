package com.ef;

/**
 * this class holds the command line argument variables.
 * @author AMD
 */
public class Arguments {

    public String startDate; // "startDate" is "yyyy-MM-dd.HH:mm:ss"
    public String duration = "hourly"; // "duration" can take only "hourly" or "daily"
    public int threshold = 0; // "threshold" can be an integer
    public String accesslog = null; // "accesslog" can be a filepath

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getAccesslog() {
        return accesslog;
    }

    public void setAccesslog(String accesslog) {
        this.accesslog = accesslog;
    }
}
