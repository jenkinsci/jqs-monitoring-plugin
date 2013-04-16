package org.jenkinsci.plugins.jqsmonitoring.jqscore;

import java.io.File;
import java.io.IOException;

import com.thoughtworks.xstream.mapper.CannotResolveClassException;

import net.sf.json.JSONObject;

import hudson.XmlFile;
import hudson.model.Saveable;
import hudson.model.Hudson;

/**
 * Implements the mothods needed for the local config page. Saves the
 * information in a XML-file.
 * 
 * @author yboev
 * 
 */
public class LocalConfig implements Saveable {

    private int updateFailedDaily;
    private int histogramWith;
    private int histogramHeight;

    // Enable flag variables
    private boolean allQS;
    private boolean queueJobTable;
    private boolean buildTable;
    private boolean slaveTable;
    private boolean allFailed;
    private boolean failedHistogram;

    private String allColor;
    private String okColor;
    private String blockedColor;
    private String stuckColor;

    private String histBackground;
    private String histColumn;
    private String histHourText;
    private String histNumberText;
    private String histCurrentColumn;
    private int histSorting;

    private int timeFilter_1;
    private int timeFilter_2;

    public LocalConfig() {
        try {
            boolean success = load();
            if (!success) {
                setDefaults();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() throws IOException {
        getConfigXml().write(this);
    }

    public void changeIt(JSONObject jfile) {
        this.updateFailedDaily = Integer.parseInt(jfile
                .getString("updatefaileddaily"));
        this.histogramWith = Integer.parseInt(jfile.getString("histwidth")
                .trim());
        this.histogramHeight = Integer.parseInt(jfile.getString("histheight")
                .trim());

        this.allQS = jfile.getString("allqs").trim().equals("true");
        this.queueJobTable = jfile.getString("queuejobstable").trim()
                .equals("true");
        this.buildTable = jfile.getString("buildtable").trim().equals("true");
        this.slaveTable = jfile.getString("slavetable").trim().equals("true");
        this.allFailed = jfile.getString("allfailed").trim().equals("true");
        this.failedHistogram = jfile.getString("failedhistogram").trim()
                .equals("true");

        this.allColor = jfile.getString("allColor").trim();
        this.okColor = jfile.getString("okColor").trim();
        this.blockedColor = jfile.getString("blockedColor").trim();
        this.stuckColor = jfile.getString("stuckColor").trim();

        this.histBackground = jfile.getString("histBackground").trim();
        this.histColumn = jfile.getString("histColumn").trim();
        this.histHourText = jfile.getString("histHourText").trim();
        this.histNumberText = jfile.getString("histNumberText").trim();
        this.histCurrentColumn = jfile.getString("histCurrentColumn").trim();
        this.histSorting = Integer.parseInt(jfile.getString("histSorting")
                .trim());

        this.timeFilter_1 = Integer.parseInt(jfile.getString("timeFilter_1")
                .trim());

        this.timeFilter_2 = Integer.parseInt(jfile.getString("timeFilter_2")
                .trim());

        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteConfigFile() {
        this.getConfigXml().delete();
    }

    public int getTimeFilter_1() {
        return this.timeFilter_1;
    }

    public int getTimeFilter_2() {
        return this.timeFilter_2;
    }

    public int getHistSorting() {
        return this.histSorting;
    }

    public String getHistBackground() {
        return this.histBackground;
    }

    public String getHistColumn() {
        return this.histColumn;
    }

    public String getHistHourText() {
        return this.histHourText;
    }

    public String getHistNumberText() {
        return this.histNumberText;
    }

    public String getHistCurrentColumn() {
        return this.histCurrentColumn;
    }

    public int getUpdateFailedDaily() {
        return this.updateFailedDaily;
    }

    public int getHistogramWidth() {
        return this.histogramWith;
    }

    public int getHistogramHeight() {
        return Math.max(this.histogramHeight, 1);
    }

    public boolean isQueueSlaveAllEnabled() {
        return this.allQS;
    }

    public boolean isQueueJobTableEnabled() {
        return this.queueJobTable;
    }

    public boolean isBuildTableEnabled() {
        return this.buildTable;
    }

    public boolean isSlaveTableEnabled() {
        return this.slaveTable;
    }

    public boolean isFailedAllEnabled() {
        return this.allFailed;
    }

    public boolean isFailedHistogramEnabled() {
        return this.failedHistogram;
    }

    public String getAllColor() {
        return this.allColor;
    }

    public String getOkColor() {
        return this.okColor;
    }

    public String getBlockedColor() {
        return this.blockedColor;
    }

    public String getStuckColor() {
        return this.stuckColor;
    }

    /**
     * Hardcoded default values for every configuration parameter. This is
     * loaded upon first start or if loading from previously changed
     * configuration fails.
     */
    private void setDefaults() {
        this.updateFailedDaily = 8;
        this.histogramWith = 31;
        this.histogramHeight = 290;

        this.allQS = true;
        this.queueJobTable = true;
        this.buildTable = true;
        this.slaveTable = true;
        this.allFailed = true;
        this.failedHistogram = true;

        this.allColor = "#" + "0099FF";// (Integer.toHexString(Color.BLUE.getRGB()).substring(2,
                                       // 7));
        this.okColor = "#" + "66CC00";// (Integer.toHexString(Color.GREEN.getRGB()).substring(2,
                                      // 7));
        this.blockedColor = "#" + "FF9900";// (Integer.toHexString(Color.ORANGE.getRGB()).substring(2,
                                           // 7));
        this.stuckColor = "#" + "FF0033";// (Integer.toHexString(Color.RED.getRGB()).substring(2,
                                         // 7));

        this.histBackground = "#FFFFFF";
        this.histColumn = "#000000";
        this.histHourText = "#000000";
        this.histNumberText = "#000000";
        this.histCurrentColumn = "#2E9AFE";
        this.histSorting = 3;

        this.timeFilter_1 = 30;
        this.timeFilter_2 = 60;

        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private XmlFile getConfigXml() {
        return new XmlFile(Hudson.XSTREAM, new File(Constants.file_LOCALCONFIG));
    }

    private boolean load() throws IOException {
        try {XmlFile xml = getConfigXml();
        if (xml.exists()) {
            xml.unmarshal(this); // Loads the contents of this file into an
                                 // existing object.
            return true;
        }
        } catch(CannotResolveClassException e) {
            return false;
        }
        return false;
    }
}
