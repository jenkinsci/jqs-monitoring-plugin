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
import javax.servlet.ServletOutputStream;
import jenkins.model.Jenkins;
import org.joda.time.DateTime;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;
import java.util.List;

/**
 * This {@link RootAction} provides relevant system information in an easily parseable format.
 * <dd>
 * </dd>
 * @author Mirko Friedenhagen
 */
@Extension
public class SystemRootAction implements RootAction {

    final Jenkins instance;

    public SystemRootAction() {
        this(Jenkins.getInstance());
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
        response.getOutputStream().println("SUMMARY: OK -- " + String.valueOf(new DateTime()));
    }

    public void doCheck(StaplerRequest request, StaplerResponse response) throws IOException {
        response.setContentType("text/plain");
        final List<Node> nodes = instance.getNodes();
        int buildableItems = instance.getQueue().countBuildableItems();
        final ServletOutputStream out = response.getOutputStream();
        out.println("SUMMARY: OK -- " + String.valueOf(new DateTime()));
        out.println("NODES: OK -- " + String.valueOf(nodes));
        out.println("BUILD-QUEUE: OK -- " + String.valueOf(buildableItems) + " items in queue");
    }

    public void doHealth(StaplerRequest request, StaplerResponse response) throws IOException {
        doCheck(request, response);
    }
}
