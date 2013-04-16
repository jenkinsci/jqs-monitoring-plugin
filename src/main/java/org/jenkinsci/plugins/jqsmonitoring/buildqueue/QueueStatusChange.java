//DISABLED FEATURE
////package org.jenkinsci.plugins.jqsmonitoring.buildqueue;
//
//import org.jenkinsci.plugins.jqsmonitoring.jqscore.Constants;
//import org.jenkinsci.plugins.jqsmonitoring.jqscore.LocalConfig;
//
///**
// * Tracks the changes in the number of jobs in the queue.
// * 
// * @author yboev
// * 
// */
//public class QueueStatusChange {
//    private String name;
//    private ChangeTag nowTag;
//    private ChangeTag medTag;
//    private ChangeTag bigTag;
//
//    public QueueStatusChange(String name) {
//        this.name = name;
//        this.nowTag = new ChangeTag(Constants.NOWTAG_NAME,
//                Constants.UPATE_TIME_SMALL);
//        this.medTag = new ChangeTag(Constants.MEDTAG_NAME,
//                Constants.UPATE_TIME_MED);
//        this.bigTag = new ChangeTag(Constants.BIGTAG_NAME,
//                Constants.UPATE_TIME_BIG);
//    }
//
//    public String getName() {
//        return this.name;
//    }
//
//    public void update(int count) {
//        long time = System.currentTimeMillis();
//        this.nowTag.update(count, time);
//        this.medTag.update(count, time);
//        this.bigTag.update(count, time);
//    }
//
//    public ChangeTag getNowTag() {
//        return this.nowTag;
//    }
//
//    public ChangeTag getMedTag() {
//        return this.medTag;
//    }
//
//    public ChangeTag getBigTag() {
//        return this.bigTag;
//    }
//
//    public String getColor() {
//        LocalConfig lc = new LocalConfig();
//        if (this.name == Constants.STATUS0)
//            return lc.getAllColor();
//        if (this.name == Constants.STATUS1)
//            return lc.getOkColor();
//        if (this.name == Constants.STATUS2)
//            return lc.getBlockedColor();
//        if (this.name == Constants.STATUS3)
//            return lc.getStuckColor();////package org.jenkinsci.plugins.jqsmonitoring.buildqueue;
//
//import org.jenkinsci.plugins.jqsmonitoring.jqscore.Constants;
//import org.jenkinsci.plugins.jqsmonitoring.jqscore.LocalConfig;
//
///**
// * Tracks the changes in the number of jobs in the queue.
// * 
// * @author yboev
// * 
// */
//public class QueueStatusChange {
//    private String name;
//    private ChangeTag nowTag;
//    private ChangeTag medTag;
//    private ChangeTag bigTag;
//
//    public QueueStatusChange(String name) {
//        this.name = name;
//        this.nowTag = new ChangeTag(Constants.NOWTAG_NAME,
//                Constants.UPATE_TIME_SMALL);
//        this.medTag = new ChangeTag(Constants.MEDTAG_NAME,
//                Constants.UPATE_TIME_MED);
//        this.bigTag = new ChangeTag(Constants.BIGTAG_NAME,
//                Constants.UPATE_TIME_BIG);
//    }
//
//    public String getName() {
//        return this.name;
//    }
//
//    public void update(int count) {
//        long time = System.currentTimeMillis();
//        this.nowTag.update(count, time);
//        this.medTag.update(count, time);
//        this.bigTag.update(count, time);
//    }
//
//    public ChangeTag getNowTag() {
//        return this.nowTag;
//    }
//
//    public ChangeTag getMedTag() {
//        return this.medTag;
//    }
//
//    public ChangeTag getBigTag() {
//        return this.bigTag;
//    }
//
//    public String getColor() {
//        LocalConfig lc = new LocalConfig();
//        if (this.name == Constants.STATUS0)
//            return lc.getAllColor();
//        if (this.name == Constants.STATUS1)
//            return lc.getOkColor();
//        if (this.name == Constants.STATUS2)
//            return lc.getBlockedColor();
//        if (this.name == Constants.STATUS3)
//            return lc.getStuckColor();
//        return "black";
//    }
//
//    public String getNowTagImage() {
//        if (this.nowTag.getDiff() > 0)
//            return Constants.arrow_up_gray;
//        if (this.nowTag.getDiff() < 0)
//            return Constants.arrow_down_gray;
//        return Constants.equal_gray;
//    }
//
//    public String getMedTagImage() {
//        if (this.nowTag.getJobs() > this.medTag.getJobs())
//            return Constants.arrow_up_gray;
//        if (this.nowTag.getJobs() < this.medTag.getJobs())
//            return Constants.arrow_down_gray;
//        return Constants.equal_gray;
//
//    }
//
//    public String getBigTagImage() {
//        if (this.nowTag.getJobs() > this.bigTag.getJobs())
//            return Constants.arrow_up_gray;
//        if (this.nowTag.getJobs() < this.bigTag.getJobs())
//            return Constants.arrow_down_gray;
//        return Constants.equal_gray;
//
//    }
//}

//        return "black";
//    }
//
//    public String getNowTagImage() {
//        if (this.nowTag.getDiff() > 0)
//            return Constants.arrow_up_gray;
//        if (this.nowTag.getDiff() < 0)
//            return Constants.arrow_down_gray;
//        return Constants.equal_gray;
//    }
//
//    public String getMedTagImage() {
//        if (this.nowTag.getJobs() > this.medTag.getJobs())
//            return Constants.arrow_up_gray;
//        if (this.nowTag.getJobs() < this.medTag.getJobs())
//            return Constants.arrow_down_gray;
//        return Constants.equal_gray;
//
//    }
//
//    public String getBigTagImage() {
//        if (this.nowTag.getJobs() > this.bigTag.getJobs())
//            return Constants.arrow_up_gray;
//        if (this.nowTag.getJobs() < this.bigTag.getJobs())
//            return Constants.arrow_down_gray;
//        return Constants.equal_gray;
//
//    }
//}
