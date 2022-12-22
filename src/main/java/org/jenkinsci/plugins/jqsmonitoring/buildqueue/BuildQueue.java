package org.jenkinsci.plugins.jqsmonitoring.buildqueue;

import hudson.model.Api;
import hudson.model.Hudson;
import hudson.model.Queue;
import hudson.model.Queue.BuildableItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * This class implements a BuildQueue and contains information about all queue
 * jobs.
 * 
 * @author yboev
 * 
 */
@ExportedBean
public class BuildQueue {

    /**
     * List of queue jobs.
     */
    private ArrayList<QueueJob> jobs;

    // private ArrayList<QueueStatusChange> changes;

    /**
     * Constructor method.
     */
    public BuildQueue() {
        retrieveQueueJobs();
    }

    /**
     * REST API.
     * 
     * @return the api.
     */
    public Api getApi() {
        return new Api(this);
    }

    /**
     * Return the number of queue jobs.
     * 
     * @return the number of jobs waiting as int.
     */
    @Exported
    public int getNumberOfJobs() {
        if (this.jobs == null) {
            return 0;
        }
        return this.jobs.size();
    }

    /**
     * Returns a list of all jobs in the queue.
     * 
     * @return the list
     */
    @Exported(inline=true)
    public ArrayList<QueueJob> getQueueJobs() {
        return this.jobs;
    }

    /**
     * Refresh method for this class.
     */
    public void refresh() {
        this.retrieveQueueJobs();
    }

    /**
     * Creates and fills the list of queue jobs.
     */
    private void retrieveQueueJobs() {
        final List<Queue.BuildableItem> list = Jenkins.get().getQueue()
                .getBuildableItems();
        this.jobs = new ArrayList<QueueJob>();
        final long currentTime = System.currentTimeMillis();
        for (BuildableItem buildableItem : list) {
            this.jobs.add(new QueueJob(buildableItem, currentTime));
        }
        Collections.sort(jobs);
        Collections.reverse(jobs);
    }
}
