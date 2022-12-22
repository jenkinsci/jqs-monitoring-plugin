/*
 * The MIT License
 *
 * Copyright 2014 Mirko Friedenhagen.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.jqsmonitoring.jqscore;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import hudson.Extension;
import hudson.model.Node;
import hudson.model.RootAction;
import javax.servlet.ServletOutputStream;
import jenkins.model.Jenkins;
import org.joda.time.DateTime;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import org.jenkinsci.plugins.jqsmonitoring.beingbuilt.RunningJob;
import org.jenkinsci.plugins.jqsmonitoring.failedbuilds.FailHistory;

/**
 * This {@link RootAction} provides relevant system information in an easily parseable format.
 *
 * @author Mirko Friedenhagen
 */
@Extension
public class SystemRootAction implements RootAction {

    final Jenkins instance;

    public SystemRootAction() {
        this(Jenkins.get());
    }

    SystemRootAction(Jenkins instance) {
        this.instance = instance;
    }

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "/-system";
    }

    public void doAlive(StaplerRequest request, StaplerResponse response) throws IOException {
        response.setContentType("text/plain");
        response.getOutputStream().println(String.format(Locale.ENGLISH, "SUMMARY: OK -- TIME: %s", String.valueOf(new DateTime())));
    }

    public void doCheck(StaplerRequest request, StaplerResponse response) throws IOException {
        response.setContentType("text/plain");
        final JQSMonitoring jqsMonitoring = new JQSMonitoring();
        Collection<String> nodes = Collections2.transform(instance.getNodes(), Node::getDisplayName);
        int buildableItems = instance.getQueue().countBuildableItems();
        final ServletOutputStream out = response.getOutputStream();
        final ArrayList<RunningJob> jobsRunningTooLong = jqsMonitoring.getJobsRunningTooLong();
        final FailHistory failHistory = jqsMonitoring.getFailHistory();
        final int average24Hours = failHistory.getAverage24Hours();
        final int lastHourFailed = failHistory.getLastHourFailed();
        final int maximum24Hours = failHistory.getMaximum24Hours();
        final String failedStatus;
        int oks = 2;
        int warnings = 0;
        int failures = 0;
        if (lastHourFailed > maximum24Hours) {
            failures += 1;
            failedStatus = String.format(Locale.ENGLISH, "FAILURE -- Count of job failures above maximum of the last 24h, lasth:%s, avg24h:%s, max24h:%s", lastHourFailed, average24Hours, maximum24Hours);
        } else if (lastHourFailed > average24Hours) {
            warnings += 1;
            failedStatus = String.format(Locale.ENGLISH, "WARNING -- Count of job failures above average of last 24h, lasth:%s, avg24h:%s, max24h:%s", lastHourFailed, average24Hours, maximum24Hours);

        } else {
            oks += 1;
            failedStatus = String.format(Locale.ENGLISH, "OK -- Count of job failures normal, lasth:%s, avg24h:%s, max24h:%s", lastHourFailed, average24Hours, maximum24Hours);
        }
        if (!jobsRunningTooLong.isEmpty()) {
            failures += 1;
        } else {
            oks +=1;
        }
        final String status = failures > 0 ? "WARNING" : "OK";
        out.println(String.format(Locale.ENGLISH, "SUMMARY: %s -- OK: %s, WARNING: %s, FAILURE: %s, TIME: %s",
                status, oks, warnings, failures, String.valueOf(new DateTime())));
        out.println("NODES: OK -- " + String.valueOf(nodes));
        out.println("BUILD-QUEUE: OK -- " + String.valueOf(buildableItems) + " items in queue");
        if (!jobsRunningTooLong.isEmpty()) {
            out.println("JOBS_RUNNING_TO_LONG: FAILURE -- " + String.valueOf(jobsRunningTooLong));
        } else {
            out.println("JOBS_RUNNING_TO_LONG: OK -- None found!");
        }
        out.println(String.format(Locale.ENGLISH, "FAILING_JOBS: %s", failedStatus));
        
    }

    public void doHealth(StaplerRequest request, StaplerResponse response) throws IOException {
        doCheck(request, response);
    }
}
