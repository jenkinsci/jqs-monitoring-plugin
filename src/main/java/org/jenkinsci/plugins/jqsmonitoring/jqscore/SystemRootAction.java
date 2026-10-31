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

import hudson.Extension;
import hudson.model.Node;
import hudson.model.RootAction;
import jakarta.servlet.ServletOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.jqsmonitoring.beingbuilt.RunningJob;
import org.jenkinsci.plugins.jqsmonitoring.failedbuilds.FailHistory;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.StaplerResponse2;

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

    public void doAlive(StaplerRequest2 request, StaplerResponse2 response) throws IOException {
        response.setContentType("text/plain");
        response.getOutputStream()
                .println(String.format(Locale.ENGLISH, "SUMMARY: OK -- TIME: %s", OffsetDateTime.now()));
    }

    public void doCheck(StaplerRequest2 request, StaplerResponse2 response) throws IOException {
        response.setContentType("text/plain");
        final JQSMonitoring jqsMonitoring = new JQSMonitoring();

        Collection<String> nodes =
                instance.getNodes().stream().map(Node::getDisplayName).collect(Collectors.toList());

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
            failedStatus = String.format(
                    Locale.ENGLISH,
                    "FAILURE -- Count of job failures above maximum of the last 24h, lasth:%s, avg24h:%s, max24h:%s",
                    lastHourFailed,
                    average24Hours,
                    maximum24Hours);
        } else if (lastHourFailed > average24Hours) {
            warnings += 1;
            failedStatus = String.format(
                    Locale.ENGLISH,
                    "WARNING -- Count of job failures above average of last 24h, lasth:%s, avg24h:%s, max24h:%s",
                    lastHourFailed,
                    average24Hours,
                    maximum24Hours);
        } else {
            oks += 1;
            failedStatus = String.format(
                    Locale.ENGLISH,
                    "OK -- Count of job failures normal, lasth:%s, avg24h:%s, max24h:%s",
                    lastHourFailed,
                    average24Hours,
                    maximum24Hours);
        }

        if (!jobsRunningTooLong.isEmpty()) {
            failures += 1;
        } else {
            oks += 1;
        }

        final String status = failures > 0 ? "WARNING" : "OK";

        out.println(String.format(
                Locale.ENGLISH,
                "SUMMARY: %s -- OK: %s, WARNING: %s, FAILURE: %s, TIME: %s",
                status,
                oks,
                warnings,
                failures,
                OffsetDateTime.now()));

        out.println("NODES: OK -- " + nodes);
        out.println("BUILD-QUEUE: OK -- " + buildableItems + " items in queue");

        if (!jobsRunningTooLong.isEmpty()) {
            out.println("JOBS_RUNNING_TO_LONG: FAILURE -- " + jobsRunningTooLong);
        } else {
            out.println("JOBS_RUNNING_TO_LONG: OK -- None found!");
        }

        out.println(String.format(Locale.ENGLISH, "FAILING_JOBS: %s", failedStatus));
    }

    public void doHealth(StaplerRequest2 request, StaplerResponse2 response) throws IOException {
        doCheck(request, response);
    }
}
