package org.jenkinsci.plugins.jqsmonitoring.jqscore;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;

import hudson.Extension;
import hudson.model.RootAction;
import hudson.model.Api;
import hudson.model.Hudson;

import org.jenkinsci.plugins.jqsmonitoring.beingbuilt.RunningJob;
import org.jenkinsci.plugins.jqsmonitoring.buildqueue.BuildQueue;
import org.jenkinsci.plugins.jqsmonitoring.failedbuilds.FailHistory;
import org.jenkinsci.plugins.jqsmonitoring.slaves.SlavesHolder;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Main class of the Plugin. Extension Point.
 * 
 * @author yboev
 * 
 */
@ExportedBean
@Extension
public class JQSMonitoring implements RootAction {

    /*
     * Lists of all buildable jobs in queue, slaves and failed jobs.
     */
    private BuildQueue buildQueue;
    private FailHistory failedHistory;
    private LocalConfig lc;

    /**
     * Constructor
     */
    public JQSMonitoring() {
        this.buildQueue = new BuildQueue();
        this.failedHistory = new FailHistory();
    }

    /**
     * REST API.
     * 
     * @return new api instance.
     */
    public Api getApi() {
        return new Api(this);
    }

    /**
     * Returns the display name for this plugin.
     */
    @Exported
    public String getDisplayName() {
        if (this.hasPermission()) {
            return Constants.DISPLAYNAME;
        }
        return null;
    }

    /**
     * Returns the icon url for this plugin.
     * 
     * @return the icon file name.
     */
    @Exported
    public String getIconFileName() {
        if (this.hasPermission()) {
            return "/" + Constants.MENUICONURL;
        }
        return null;
    }

    /**
     * Returns the url for this plugin.
     * 
     * @return the url name.
     */
    @Exported
    public String getUrlName() {
        return Constants.URL;
    }

    /**
     * Refreshes and returns the instance of BuildQueue.
     * 
     * @return the BuildQueue instance
     */
    @Exported(inline=true)
    public BuildQueue getBuildQueue() {
        this.buildQueue.refresh();
        return this.buildQueue;
    }

    /**
     * Returns the root url for this plugin.
     * 
     * @return the url.
     */
    @Exported
    public String getRootURL() {
        // LOGGER.info(Constants.rootURL);
        return Constants.rootURL;
    }

    /**
     * Returns the icons url for this plugin.
     * 
     * @return the url where icons can be found.
     */
    @Exported
    public String getIconsURL() {
        return Constants.ICONS;
    }

    /**
     * Returns the number of all jobs in Jenkins/Hudson.
     * 
     * @return the jobs count
     */
    @Exported
    public int getNumberOfAllJobs() {
        return Hudson.getInstance().getItems().size();
    }

    /**
     * Returns a list of jobs that have been running for too long.
     * 
     * @return list with the jobs.
     */
    @Exported(inline=true)
    public ArrayList<RunningJob> getJobsRunningTooLong() {
        return RunningJob.getJobsRunningTooLong(SlavesHolder.getInstance()
                .getSlaves());
    }

    /*
     * @Exported public ArrayList<String> getTestArrayListString() {
     * ArrayList<String> s = new ArrayList<String>(); s.add("TestString1");
     * s.add("ANDFIAEUEHFOEUFHU$HFWE"); return s; }
     */

    /**
     * Returns the instance of FailHistory.
     * 
     * @return the instance
     */
    @Exported(inline=true)
    public FailHistory getFailHistory() {
        return this.failedHistory;
    }

    /**
     * Returns the address of the failed-jobs histogram.
     * 
     * @return the address
     */
    @Exported
    public String getFailedBuildHistogram() {
        return Constants.FAILED_JOB_GRAPHIC_1_URL;
    }

    /**
     * Accepts the parameters from the local configuration page and passes them
     * on to LocalConfig class.
     * 
     * @param req
     *            Request
     * @param rsp
     *            Response
     * @throws IOException
     *             input/ouput
     * @throws ServletException
     */
    public void doConfigSubmit(StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {
        if (!this.hasPermission()) {
            return;
        }
        this.lc = new LocalConfig();
        lc.changeIt(req.getSubmittedForm());
        this.refreshFailGraphic();
        rsp.sendRedirect(".");
    }

    /**
     * Sets the default values for the configuration.
     * 
     * @param req
     *            Request
     * @param rsp
     *            Response
     * @throws IOException
     * @throws ServletException
     */
    public void doSetDefaults(StaplerRequest req, StaplerResponse rsp)
            throws IOException, ServletException {
        if (!this.hasPermission()) {
            return;
        }
        this.lc = new LocalConfig();
        lc.deleteConfigFile();
        this.refresh();
        rsp.sendRedirect("./config");
    }

    /**
     * Returns an instance of the configuration.
     * 
     * @return a configuration instance.
     */
    public LocalConfig getLocalConfig() {
        return new LocalConfig();
    }

    /**
     * Returns an instance of the SlavesHolder.
     * 
     * @return the instance.
     */
    @Exported(inline=true)
    public SlavesHolder getSlavesHolder() {
        return SlavesHolder.getInstance();
    }

    /**
     * Checks if the user has ADMINISTER permission.
     * 
     * @return
     */
    private boolean hasPermission() {
        return Hudson.getInstance().hasPermission(Hudson.ADMINISTER);
    }

    /**
     * Refresh method.
     */
    private void refresh() {
        this.buildQueue.refresh();
    }

    /**
     * Refreshes the graphic with the failed builds. Used to generate a new one
     * when changes has been made to the configuration such as color change,
     * width or height change etc. so that these can be seen right away.
     */
    private void refreshFailGraphic() {
        this.failedHistory.createHourGraphic1();
    }

}
