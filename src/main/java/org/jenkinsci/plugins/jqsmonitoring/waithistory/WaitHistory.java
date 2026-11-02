package org.jenkinsci.plugins.jqsmonitoring.waithistory;

import hudson.Extension;
import hudson.ExtensionList;
import hudson.XmlFile;
import hudson.model.Queue;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.queue.CauseOfBlockage;
import hudson.model.queue.QueueListener;
import hudson.util.XStream2;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;

@Extension
public class WaitHistory extends QueueListener {

    private static final Logger LOGGER = Logger.getLogger(WaitHistory.class.getName());

    private final XmlFile storage;
    private final List<WaitHistoryEntry> entries = new ArrayList<>();

    public WaitHistory() {
        File file = new File(
                Jenkins.get().getRootDir(),
                "jqs-monitoring-wait-history.xml");

        this.storage = new XmlFile(new XStream2(), file);
        load();
    }

    public static WaitHistory getInstance() {
        return ExtensionList.lookupSingleton(WaitHistory.class);
    }

    @Override
    public void onLeft(Queue.LeftItem item) {
        if (item.isCancelled()) {
            return;
        }

        Queue.Executable executable = item.getExecutable();

        if (!(executable instanceof Run<?, ?>)) {
            return;
        }

        Run<?, ?> run = (Run<?, ?>) executable;

        long startedAt = run.getStartTimeInMillis();
        long waitTime = Math.max(0, startedAt - item.getInQueueSince());

        CauseOfBlockage blockage = item.getCauseOfBlockage();

        String cause = blockage != null
                ? blockage.getShortDescription()
                : item.getWhy();

        if (cause == null || cause.isBlank()) {
            cause = "No blockage";
        }

        String name = run.getParent().getFullDisplayName()
                + " #" + run.getNumber();

        WaitHistoryEntry entry = new WaitHistoryEntry(
                item.getId(),
                "BUILDING",
                name,
                cause,
                run.getUrl(),
                waitTime,
                startedAt);

        synchronized (this) {
            entries.add(entry);
            save();
        }
    }

    public synchronized void complete(Run<?, ?> run) {
        long queueId = run.getQueueId();
        Result result = run.getResult();

        for (WaitHistoryEntry entry : entries) {
            if (entry.getQueueId() == queueId) {
                entry.setStatus(
                        result != null ? result.toString() : "UNKNOWN");

                save();
                return;
            }
        }

        LOGGER.log(
                Level.FINE,
                "No wait history entry found for queue ID {0}",
                queueId);
    }

    public synchronized List<WaitHistoryEntry> getEntries() {
        List<WaitHistoryEntry> result = new ArrayList<>(entries);

        result.sort(
                Comparator.comparingLong(WaitHistoryEntry::getStartedAt)
                        .reversed());

        return result;
    }

    private synchronized void load() {
        if (!storage.exists()) {
            return;
        }

        try {
            HistoryData data = (HistoryData) storage.read();

            if (data != null && data.entries != null) {
                entries.addAll(data.entries);
            }
        } catch (IOException | ClassCastException e) {
            LOGGER.log(Level.WARNING, "Could not load wait history", e);
        }
    }

    private synchronized void save() {
        try {
            storage.write(new HistoryData(new ArrayList<>(entries)));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Could not save wait history", e);
        }
    }

    public static class HistoryData {

        public List<WaitHistoryEntry> entries = new ArrayList<>();

        public HistoryData() {}

        public HistoryData(List<WaitHistoryEntry> entries) {
            this.entries = entries;
        }
    }
}