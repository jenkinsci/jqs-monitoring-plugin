package org.jenkinsci.plugins.jqsmonitoring.slaves;

import hudson.model.Api;
import hudson.model.Executor;
import hudson.model.Hudson;
import hudson.model.Slave;
import hudson.slaves.ComputerListener;
import java.util.ArrayList;
import java.util.Comparator;
import org.jenkinsci.plugins.jqsmonitoring.jqscore.Constants;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * Holds all information about the slaves in the current jenkins instance.
 *
 * @author yboev
 *
 */
@ExportedBean
public final class SlavesHolder extends ComputerListener {

    /**
     * List of all jenkins slaves.
     */
    private ArrayList<Slave> slaves;
    /**
     * Singleton instance.
     */
    private static SlavesHolder instance;

    /**
     * Constructor method. Called only from getInstance().
     */
    @SuppressWarnings("deprecation")
    private SlavesHolder() {
        this.slaves = new ArrayList<Slave>(Hudson.getInstance().getSlaves());
        sort();
        // Adding ATExtension did no rigister my class, that is why this method
        // is called.
        this.register();
    }

    /**
     * Return the instance for this class(Singleton).
     *
     * @return the instance.
     */
    public static SlavesHolder getInstance() {
        if (instance == null) {
            instance = new SlavesHolder();
        }
        return instance;
    }

    /**
     * REST API.
     *
     * @return the api.
     */
    public Api getApi() {
        return new Api(this);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onConfigurationChange() {
        this.slaves = new ArrayList<Slave>(Hudson.getInstance().getSlaves());
        sort();
    }

    /**
     * List with all slaves.
     *
     * @return the list.
     */
    @Exported
    public ArrayList<Slave> getSlaves() {
        return this.slaves;
    }

    /**
     * The number of slaves.
     *
     * @return the number as int.
     */
    @Exported
    public int getNumberOfSlaves() {
        if (this.slaves == null) {
            return 0;
        }
        return this.slaves.size();
    }

    /**
     * Returns the url for a certain slave.
     *
     * @param slave
     *            the slave.
     * @return the url of the given slave.
     */
    public String getUrl(Slave slave) {
        if (slave == null) {
            return "NoLastBuild";
        }

        var computer = slave.getComputer();

        if (computer == null) {
            return "NoLastBuild";
        }

        var builds = computer.getBuilds();

        if (builds == null) {
            return "NoLastBuild";
        }

        var lastBuild = builds.getLastBuild();

        if (lastBuild == null) {
            return "NoLastBuild";
        }

        return lastBuild.getUrl();
    }

    /**
     * Return the last build for a given slave.
     *
     * @param slave
     *            the slave.
     * @return the last build of the given slave as String.
     */
    public String getLastBuild(Slave slave) {
        if (slave == null) {
            return null;
        }

        var computer = slave.getComputer();

        if (computer == null) {
            return null;
        }

        var builds = computer.getBuilds();

        if (builds == null) {
            return null;
        }

        var lastBuild = builds.getLastBuild();

        if (lastBuild == null) {
            return null;
        }

        return lastBuild.getFullDisplayName();
    }

    /**
     * Return number of online slaves.
     *
     * @return number of online slaves as int.
     */
    @Exported
    public int getNumberOfOnlineSlaves() {
        int count = 0;

        for (Slave s : slaves) {
            if (s == null) {
                continue;
            }

            var computer = s.getComputer();

            if (computer != null && computer.isOnline()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Return number of offline slaves.
     *
     * @return number of offline slaves as int.
     */
    @Exported
    public int getNumberOfOfflineSlaves() {
        int count = 0;

        for (Slave s : slaves) {
            if (s == null) {
                continue;
            }

            var computer = s.getComputer();

            if (computer == null || computer.isOffline()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Return the color assotiated with this slave . red - offline orange - all
     * executors busy green - free executor
     *
     * @param s
     *            the slave.
     * @return the color as String.
     */
    public String getColor(Slave s) {
        if (s == null) {
            return Constants.SLAVE_OFFLINE_COLOR;
        }

        var computer = s.toComputer();

        if (computer == null || computer.isOffline()) {
            return Constants.SLAVE_OFFLINE_COLOR;
        }

        if (this.areAllExecutorsBusy(s)) {
            return Constants.SLAVE_NOEXECUTORS_COLOR;
        }

        return Constants.SLAVE_OK_COLOR;
    }

    /**
     * Sort method for the slaves list.
     */
    private void sort() {
        final Comparator<Slave> c = Comparator.comparing(Slave::getNodeName);
        this.slaves.sort(c);
    }

    /**
     * Checks if all executors of a slave are busy.
     *
     * @param s
     *            the slave, which executors are being checked.
     * @return true - all busy, false otherwise.
     */
    private boolean areAllExecutorsBusy(Slave s) {
        if (s == null) {
            return false;
        }

        var computer = s.getComputer();

        if (computer == null) {
            return false;
        }

        int count = 0;

        for (Executor executor : computer.getExecutors()) {
            if (!executor.isBusy()) {
                count++;
            }
        }

        return count == 0;
    }
}
