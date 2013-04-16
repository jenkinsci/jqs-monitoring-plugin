//DISABLED FEATURE
//package org.jenkinsci.plugins.jqsmonitoring.buildqueue;
//
///**
// * Saves information about the number of jobs in the queue for 3 time marks so
// * that a trend for the queue can be shown.
// * 
// * @author yboev
// * 
// */
//public class ChangeTag {
//
//    private String name;
//    private long updateTime;
//    private long lastUpdate;
//    private int jobs;
//    private int diff;
//
//    public ChangeTag(String name, long time) {
//        this.name = name;
//        this.updateTime = time;
//        this.lastUpdate = System.currentTimeMillis();
//        this.diff = 0;
//    }
//
//    public int getDiff() {
//        return this.diff;
//    }
//
//    public String getName() {
//        return this.name;
//    }
//
//    public int getJobs() {
//        return this.jobs;
//    }
//
//    public void update(int count, long time) {
//        if (this.needUpdate(time)) {
//            this.diff = count - this.jobs;
//            this.jobs = count;
//            this.lastUpdate = time;
//        }
//    }
//
//    private boolean needUpdate(long time) {
//        if (this.lastUpdate == -1 || time - this.lastUpdate > this.updateTime)
//            return true;
//        return false;
//    }
//
//}
