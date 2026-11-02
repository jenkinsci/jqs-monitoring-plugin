package org.jenkinsci.plugins.jqsmonitoring.waithistory;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean
public class WaitHistoryEntry {

    private long queueId;
    private String status;
    private String name;
    private String causeOfBlockage;
    private String url;
    private long waitTimeMillis;
    private long startedAt;

    public WaitHistoryEntry() {}

    public WaitHistoryEntry(
            long queueId,
            String status,
            String name,
            String causeOfBlockage,
            String url,
            long waitTimeMillis,
            long startedAt) {
        this.queueId = queueId;
        this.status = status;
        this.name = name;
        this.causeOfBlockage = causeOfBlockage;
        this.url = url;
        this.waitTimeMillis = waitTimeMillis;
        this.startedAt = startedAt;
    }

    @Exported
    public long getQueueId() {
        return queueId;
    }

    @Exported
    public String getStatus() {
        return status;
    }

    @Exported
    public String getName() {
        return name;
    }

    @Exported
    public String getCauseOfBlockage() {
        return causeOfBlockage;
    }

    @Exported
    public String getUrl() {
        return url;
    }

    @Exported
    public long getWaitTimeMillis() {
        return waitTimeMillis;
    }

    @Exported
    public String getWaitTime() {
        long seconds = waitTimeMillis / 1000;
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        if (days > 0) {
            return days + "d " + hours + "h " + minutes + "m " + remainingSeconds + "s";
        }

        if (hours > 0) {
            return hours + "h " + minutes + "m " + remainingSeconds + "s";
        }

        if (minutes > 0) {
            return minutes + "m " + remainingSeconds + "s";
        }

        return remainingSeconds + "s";
    }

    @Exported
    public long getStartedAt() {
        return startedAt;
    }

    void setStatus(String status) {
        this.status = status;
    }
}
