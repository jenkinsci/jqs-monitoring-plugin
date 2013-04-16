package org.jenkinsci.plugins.jqsmonitoring.beingbuilt;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.plugins.jqsmonitoring.jqscore.LocalConfig;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;


import hudson.model.Api;
import hudson.model.Executor;
import hudson.model.Slave;

/**
 * Class representing a job that has been running.
 * @author yboev
 *
 */
@ExportedBean
public class RunningJob {

    /**
     * name of the job.
     */
    private String name;
    /**
     * slave on which the job runs.
     */
    private Slave slave;
    /**
     * Elapsed time running.
     */
    private long runningTime;
    /**
     * Remaining time till finish.
     */
    private String remainingTime;
    /**
     * Color. Depends on the time which the job has been running, determined in
     * constructor method.
     */
    private Color color;
    /**
     * The url of this job.
     */
    private String jobUrl;

    /**
     * Constructor
     * 
     * @param lc
     *            local configuration.
     * @param executor
     *            the executor.
     * @param slave
     *            the slave.
     */
    public RunningJob(LocalConfig lc, Executor executor, Slave slave) {
        this.name = executor.getCurrentExecutable() + "";
        this.slave = slave;
        this.runningTime = executor.getElapsedTime();
        this.remainingTime = executor.getEstimatedRemainingTime();
        this.jobUrl = executor.getCurrentExecutable().getParent().getOwnerTask()
                .getUrl();
        if (TimeUnit.MINUTES.toMillis(lc.getTimeFilter_2()) >= this.runningTime) {
            this.color = Color.ORANGE;
        }
        if (TimeUnit.MINUTES.toMillis(lc.getTimeFilter_2()) < this.runningTime) {
            this.color = Color.RED;
        }
    }
    
    public Api getApi() {
        return new Api(this);
    }

    
    /**
     * Returns the slave url on which this job runs.
     * 
     * @return The url as String.
     */
    @Exported
    public String getSlaveUrl() {
        return this.slave.getComputer().getUrl();
    }

    /**
     * Returns the job url.
     * 
     * @return the url as String.
     */
    @Exported
    public String getJobUrl() {
        return this.jobUrl;
    }
    
    /**
     * Returns the color.
     * @return the color as string. Format: #******.
     */
    public String getColor() {
        return "#" + (Integer.toHexString(this.color.getRGB()).substring(2, 7));
    }

    /**
     * Returns the name of the job.
     * @return the name as string
     */
    @Exported
    public String getName() {
        return this.name;
    }

    /**
     * Returns the slave.
     * @return slave on which this job has been running.
     */
    @Exported
    public Slave getSlave() {
        return this.slave;
    }

    /**
     * Returns the time this job as been running. Returns it as a string that is human readable.
     * @return elapsed time in h m s format. where h can be > 24.
     */
    @Exported
    public String getRunningTime() {
        long hours, mins, secs, time;
        time = this.runningTime;
        hours = (byte) TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(hours);
        mins = (byte) TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(mins);
        secs = (byte) TimeUnit.MILLISECONDS.toSeconds(time);

        return hours + "h " + mins + "m " + secs + "s";
    }

    /**
     * return the remaining time as string that is human readable.
     * @return the remaining time. it will return N/A if this could not be determined.
     */
    @Exported
    public String getRemainingTime() {
        return this.remainingTime;
    }
    
}
