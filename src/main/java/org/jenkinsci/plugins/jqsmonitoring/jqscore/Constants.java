package org.jenkinsci.plugins.jqsmonitoring.jqscore;

import hudson.model.Hudson;

import java.awt.Color;
import java.io.File;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

public final class Constants {

    private Constants() {
        // holds constants
    }
    
    private static final Logger LOGGER = Logger.getLogger(Constants.class
            .getName());

    public static final String rootURL = getHudsonAddr();
    public static final String URL = "/jqs-monitoring";
    public static final String DISPLAYNAME = "JQS Monitoring";
    public static final String ICONS = "plugin/jqs-monitoring/icons/";

    public static final String MENUICONURL = ICONS + "user-desktop.png";

    public static final long UPATE_TIME_SMALL = 0;
    public static final long UPATE_TIME_MED = 1000 * 60 * 10;
    public static final long UPATE_TIME_BIG = 1000 * 60 * 30;

    
    public static final String NOWTAG_NAME = "now";
    public static final String MEDTAG_NAME = "10 min";
    public static final String BIGTAG_NAME = "30 min";

    //Different statuses of a queue jobs
    public static final String STATUS0 = "overall";
    public static final String STATUS1 = "ok";
    public static final String STATUS2 = "blocked";
    public static final String STATUS3 = "stuck";

    public static final String okIcon = ICONS + "job_ok.png";
    public static final String okBlocked = ICONS + "job_blocked.png";
    public static final String okStuck = ICONS + "job_stuck.png";

    public static final String arrow_up_gray = ICONS + "gray_up.png";
    public static final String arrow_down_gray = ICONS + "gray_down.png";
    public static final String equal_gray = ICONS + "equal.png";

    // public static final int GRAPHIC_MULT = 35;
    // public static final int GRAPHIC_HEIGHT = 300;
    // public static final int GRAPHIC_WIDTH = 24 * GRAPHIC_MULT;
    public static final int PIXELSPERSYMBOL = 7;

    public static final long REFRESH_RATE = 1000 * 60 * 60; // an hour

    public static final String BASE = Constants.getHome()
            + "userContent/jqs-monitoring-data/";
    public static final String FAILED_JOB_GRAPHIC_1_URL = rootURL
            + "userContent/jqs-monitoring-data/graphic1.jpg";
    public static final String FAILED_JOB_GRAPHIC_1 = BASE + "graphic1.jpg";
    public static final String file_longterm = BASE + "longterm";
    public static final String file_24hours = BASE + "24hours";
    public static final String file_tmp = BASE + "tmp";
    
    public static final int HOURS_PER_DAY = 24;

    public static final String LESS_FAILED_ICON = ICONS
            + "arrow_down_32x32.png";
    public static final String MORE_FAILED_ICON = ICONS
            + "red_arrow_up_32x32.png";
    public static final String EQUAL_FAILED_ICON = null; //ICONS + "red_equal_24x32.png";
    public static final String LESS_FAILED_COLOR = "green";
    public static final String MORE_FAILED_COLOR = "red";
    
    
    public static final Color GRAPHIC_1_BGCOLOR = Color.WHITE;
    public static final Color GRAPHIC_1_COLOR = Color.BLACK;

    public static final String file_LOCALCONFIG = BASE + "jqs_localconf.xml";
    
    public static final String SLAVE_OFFLINE_COLOR = "#" + (Integer.toHexString(Color.RED.getRGB()).substring(2, 7));
    public static final String SLAVE_NOEXECUTORS_COLOR = "#" + (Integer.toHexString(Color.ORANGE.getRGB()).substring(2, 7));
    public static final String SLAVE_OK_COLOR = "#66CC00";

    private static String getHome() {
        String s = Jenkins.getInstance().getRootDir().getPath();
        if (s == null) {
            s = System.getProperty("JENKINS_HOME");
        }
        if (s == null) {
            s = System.getProperty("HUDSON_HOME");
        }
        if (s == null) {
            s = (new File(System.getProperty("user.dir"))).getParent();
        }
        if (s != null && !s.endsWith("/"))
            s = s + "/";
        if (s != null && !s.startsWith("/"))
            s = "/" + s;
        return s;
    }

    private static String getHudsonAddr() {
        String s;
        try {
            s = Hudson.getInstance().getRootUrlFromRequest();
        } catch (NullPointerException e) {
            s = Hudson.getInstance().getRootUrl();
        }
        if (s == null)
            s = Jenkins.getInstance().getRootUrl();
        if (s != null && !s.endsWith("/"))
            s = s + "/";
        return s;
    }
}
