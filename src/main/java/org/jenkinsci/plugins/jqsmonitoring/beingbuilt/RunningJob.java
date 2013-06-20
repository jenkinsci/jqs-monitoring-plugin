package org.jenkinsci.plugins.jqsmonitoring.beingbuilt;


import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.plugins.jqsmonitoring.jqscore.LocalConfig;


import hudson.model.Executor;
import hudson.model.Slave;

/**
 * Class representing a job that has been running.
 * @author yboev
 *
 */
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
    
    /**
     * Returns the slave url on which this job runs.
     * 
     * @return The url as String.
     */
    public String getSlaveUrl() {
        return this.slave.getComputer().getUrl();
    }

    /**
     * Returns the job url.
     * 
     * @return the url as String.
     */
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
    public String getName() {
        return this.name;
    }

    /**
     * Returns the slave.
     * @return slave on which this job has been running.
     */
    public Slave getSlave() {
        return this.slave;
    }

    /**
     * Returns the time this job as been running. Returns it as a string that is human readable.
     * @return elapsed time in h m s format. where h can be > 24.
     */
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
    public String getRemainingTime() {
        return this.remainingTime;
    }
    
    /**
     * Returns a list of jobs being build for too long.
     * @param slaves
     *            list of all slaves
     * @return returns these jobs that have been being built for more than a
     *         certain period of time. The period of time is determined by
     *         TimeFilter_1 in the configuration.
     */
    public static ArrayList<RunningJob> getJobsRunningTooLong(ArrayList<Slave> slaves) {
        LocalConfig lc = new LocalConfig();
        final ArrayList<RunningJob> runningJobs = new ArrayList<RunningJob>();
        if (slaves == null || slaves.size() == 0) {
            return null;
        }
        for (final Iterator<Slave> i = slaves.iterator(); i.hasNext();) {
            final Slave s = i.next();
            final List<Executor> executors = s.toComputer().getExecutors();
            if (executors == null || executors.size() == 0) {
                continue;
            }
            for (final Iterator<Executor> p = executors.iterator(); p.hasNext();) {
                final Executor exec = p.next();
                if (exec != null
                        && exec.getCurrentExecutable() != null
                        && TimeUnit.MINUTES.toMillis(lc.getTimeFilter_1()) <= exec
                                .getElapsedTime()) {
                    runningJobs.add(new RunningJob(lc, exec, s));
                }
            }
        }
        if (runningJobs.size() == 0) {
            return null;
        }
        return runningJobs;
    }
    
}
