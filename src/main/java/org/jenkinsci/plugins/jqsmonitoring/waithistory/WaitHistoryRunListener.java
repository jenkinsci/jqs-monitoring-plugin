package org.jenkinsci.plugins.jqsmonitoring.waithistory;

import hudson.Extension;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.model.listeners.RunListener;

@Extension
public class WaitHistoryRunListener extends RunListener<Run<?, ?>> {

    @Override
    public void onCompleted(Run<?, ?> run, TaskListener listener) {
        WaitHistory history = WaitHistory.getInstance();

        if (history != null) {
            history.complete(run);
        }
    }
}
