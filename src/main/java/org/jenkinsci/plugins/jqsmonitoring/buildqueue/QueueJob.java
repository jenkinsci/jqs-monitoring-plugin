package org.jenkinsci.plugins.jqsmonitoring.buildqueue;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.jenkinsci.plugins.jqsmonitoring.jqscore.Constants;
import org.jenkinsci.plugins.jqsmonitoring.jqscore.LocalConfig;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;


import hudson.model.Api;
import hudson.model.Queue;
import hudson.model.queue.CauseOfBlockage;

/**
 * Stores attributes and functions of a queue job. Implements the interface
 * Comparable for sorting the jobs currently waiting in the queue according to
 * their wait time.
 * 
 * @author yboev
 * 
 */
@ExportedBean
public class QueueJob implements Comparable<QueueJob> {
    private String name;
    private String status;
    private CauseOfBlockage causeOfBlockage;
    private long hours;
    private long mins;
    private long secs;
    private long days;
    private long time_ms;
    private String color;
    private String url;

    private static final Logger LOGGER = Logger.getLogger(QueueJob.class
            .getName());

    /**
     * Constructor.
     * 
     * @param item
     *            the job(item) waiting in the queue
     * @param currentTime
     *            the current time on which all items have been retrieved
     */
    public QueueJob(Queue.BuildableItem item, long currentTime) {

        this.name = item.task.getDisplayName();
        // calulates the time files of a given job from a single number in
        // miliseons
        this.time_ms = currentTime - (item.buildableStartMilliseconds);
        long time = this.time_ms;
        this.days = TimeUnit.MILLISECONDS.toDays(time);
        time -= TimeUnit.DAYS.toMillis(this.days);
        this.hours = TimeUnit.MILLISECONDS.toHours(time);
        time -= TimeUnit.HOURS.toMillis(this.hours);
        this.mins = TimeUnit.MILLISECONDS.toMinutes(time);
        time -= TimeUnit.MINUTES.toMillis(this.mins);
        this.secs = TimeUnit.MILLISECONDS.toSeconds(time);
        this.url = item.task.getUrl();

        try {
            this.causeOfBlockage = item.getCauseOfBlockage();
        } catch (NullPointerException e) {
            LOGGER.info("Exception :(");
        }

        // sets the color of a job, according to its status
        final LocalConfig lc = new LocalConfig();
        this.status = Constants.STATUS1;
        this.color = lc.getOkColor();
        if (item.isBlocked()) {
            this.status = Constants.STATUS2;
            this.color = lc.getBlockedColor();
        }
        if (item.isStuck()) {
            this.status = Constants.STATUS3;
            this.color = lc.getStuckColor();
        }
    }
    
    /**
     * REST API
     * 
     * @return the api.
     */
    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns the status of this queue job. Options: "ok", "blocked", "stuck"
     * 
     * @return the status
     */
    @Exported
    public String getStatus() {
        return this.status;
    }

    /**
     * Returns ID of the waiting job.
     * 
     * @return the ID
     */
    @Exported
    public String getName() {
        return this.name;
    }

    /**
     * Color Returns the time which a job has spend in the queue waiting as a
     * formate String.
     * 
     * @return the formated String
     */
    @Exported
    public String getTime() {
        if (days == 0) {
            return hours + "h " + mins + "m " + secs + "s ";
        }
        return days + "d " + hours + "h " + mins + "m " + secs + "s ";
    }

    /**
     * Returns why the job is waiting in the queue.
     */
    @Exported
    public String getCauseOfBlockage() {
        if (this.causeOfBlockage != null) {
            return this.causeOfBlockage.getShortDescription().toString();
        }
        return "unknown(to fix)";
    }

    /**
     * Color to be used for this job.
     * 
     * @return the color as String
     */
    @Exported
    public String getColor() {
        // LOGGER.info("Color is:" + "#" +
        // Integer.toHexString(this.color.getRGB()));
        return this.color; // "#" +
                           // (Integer.toHexString(this.color.getRGB()).substring(2,
                           // 7));
    }

    /**
     * Implementation of the comparing method.
     */
    public int compareTo(QueueJob qj) {
        if (this.status == qj.status) {
            if (qj.time_ms > this.time_ms) {
                return -1;
            } else {
                return 1;
            }
        }
        if (this.status == Constants.STATUS3) {
            return 1;
        }
        if (this.status == Constants.STATUS1) {
            return -1;
        }
        if (this.status == Constants.STATUS2 && qj.status == Constants.STATUS1) {
            return 1;
        } else {
            return -1;
        }
    }

    /**
     * Returns the icon for this queue job.
     * 
     * @return address of the icon
     */
    @Exported
    public String getIcon() {
        if (this.status == Constants.STATUS1) {
            return Constants.okIcon;
        }
        if (this.status == Constants.STATUS2) {
            return Constants.okBlocked;
        }
        if (this.status == Constants.STATUS3) {
            return Constants.okStuck;
        }
        return "";
    }
    
    /**
     * Returns the url for this queue job.
     * @return the url as string.
     */
    @Exported
    public String getUrl() {
        return this.url;
    }
}
