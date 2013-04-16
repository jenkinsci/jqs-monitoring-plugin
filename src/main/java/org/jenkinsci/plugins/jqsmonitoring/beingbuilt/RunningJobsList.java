package org.jenkinsci.plugins.jqsmonitoring.beingbuilt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.jenkinsci.plugins.jqsmonitoring.jqscore.LocalConfig;

import hudson.model.Executor;
import hudson.model.Slave;


/**
 * Class used for retrieving a list of the currently running jobs.
 * @author yboev
 *
 */
public class RunningJobsList {

    /**
     * Reference to the configuration. Needed for make a new instance of a Job.
     */
    private LocalConfig lc;

    /**
     * Constructor.
     */
    public RunningJobsList() {
        this.lc = new LocalConfig();
    }

    /**
     * Returns a list of jobs being build for too long.
     * @param slaves
     *            list of all slaves
     * @return returns these jobs that have been being built for more than a
     *         certain period of time. The period of time is determined by
     *         TimeFilter_1 in the configuration.
     */
    public ArrayList<RunningJob> getJobsRunningTooLong(ArrayList<Slave> slaves) {

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
