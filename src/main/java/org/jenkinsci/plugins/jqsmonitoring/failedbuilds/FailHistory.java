package org.jenkinsci.plugins.jqsmonitoring.failedbuilds;

import hudson.model.AbstractProject;
import hudson.model.Api;
import hudson.model.Result;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import jenkins.model.Jenkins;
import org.jenkinsci.plugins.jqsmonitoring.jqscore.Constants;
import org.jenkinsci.plugins.jqsmonitoring.jqscore.LocalConfig;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
 * TODO remove magic numbers! This class implements the information about
 *       failed builds. Keeps track of the number of failed builds for the last
 *       48 hours and shows the last 24 in a histogram.
 *
 * @author yboev
 *
 */
@ExportedBean
public class FailHistory {

    private File file_24hours;
    private int failedEnabledJobs, failedDisabledJobs;
    private LocalConfig lc;

    private static final Logger LOGGER = Logger.getLogger(FailHistory.class.getName());

    public FailHistory() {
        this.lc = new LocalConfig();
        this.createHistoryFiles();
        startRecording();
        this.createHourGraphic1();
    }

    /**
     * Returns an icon corresponding to the difference between the current
     * number of failed jobs and that from the last hour.
     *
     * @return the icon's ulr as string
     */
    public String getTrendIcon() {
        if (this.getFailedEnabledJobsCount() < this.getLastHourFailed()) {
            return Constants.LESS_FAILED_ICON;
        }
        if (this.getFailedEnabledJobsCount() > this.getLastHourFailed()) {
            return Constants.MORE_FAILED_ICON;
        }
        return Constants.EQUAL_FAILED_ICON;
    }

    public Api getApi() {
        return new Api(this);
    }

    @Exported
    public int getLastHourFailed() {
        return this.get24Hours("today")[this.getCurrentHour()];
    }

    /**
     * Returns the number of failed builds(enabled + disabled).
     *
     * @return the number of failed builds.
     */
    @Exported
    public int getAllFailedJobsCount() {
        return this.failedDisabledJobs + this.failedEnabledJobs;
    }

    /**
     * Returns the number of failed builds that are disabled.
     *
     * @return the number of disabled failed builds.
     */
    @Exported
    public int getFailedDisabledJobsCount() {
        return this.failedDisabledJobs;
    }

    /**
     * Returns the number of failed builds that are enabled.
     *
     * @return the number of enabled failed.
     */
    @Exported
    public int getFailedEnabledJobsCount() {
        return this.failedEnabledJobs;
    }

    public void updateFailedJobs() {
        this.failedEnabledJobs = 0;
        this.failedDisabledJobs = 0;
        int cannotRetrieveCompletedBuild = 0;

        for (AbstractProject<?, ?> p : Jenkins.get().getAllItems(AbstractProject.class)) {
            if (p == null) {
                cannotRetrieveCompletedBuild++;
                continue;
            }

            var lastCompletedBuild = p.getLastCompletedBuild();

            if (lastCompletedBuild == null) {
                cannotRetrieveCompletedBuild++;
                continue;
            }

            Result result = lastCompletedBuild.getResult();

            if (result == null) {
                cannotRetrieveCompletedBuild++;
                continue;
            }

            if (result.isWorseOrEqualTo(Result.FAILURE) && !(p.getParent() instanceof AbstractProject)) {
                if (p.isDisabled()) {
                    this.failedDisabledJobs++;
                } else {
                    this.failedEnabledJobs++;
                }
            }
        }

        if (cannotRetrieveCompletedBuild > 0) {
            LOGGER.info("Failed to retrieve last completed build for " + cannotRetrieveCompletedBuild + " projects.");
        }
    }

    @Exported
    public Map<Integer, Integer> getNumberOfFailedJobsLast24Hours() {
        int[] failedJobsCount = get24Hours("today");
        Map<Integer, Integer> rslt = new TreeMap<>();

        for (int i = 0; i < Constants.HOURS_PER_DAY; i++) {
            rslt.put(i, failedJobsCount[i]);
        }

        return rslt;
    }

    @Exported
    public int getAverage24Hours() {
        final int[] today = this.get24Hours("today");
        int sum = 0;
        for (int i = 0; i < Constants.HOURS_PER_DAY; i++) {
            sum += today[i];
        }
        return sum / Constants.HOURS_PER_DAY;
    }

    @Exported
    public int getMinimum24Hours() {
        final int[] today = this.get24Hours("today");
        int min = this.getLastHourFailed();
        for (int i = 0; i < Constants.HOURS_PER_DAY; i++) {
            if (min > today[i]) {
                min = today[i];
            }
        }
        return min;
    }

    @Exported
    public int getMaximum24Hours() {
        final int[] today = this.get24Hours("today");
        int max = 0;
        for (int i = 0; i < Constants.HOURS_PER_DAY; i++) {
            if (max < today[i]) {
                max = today[i];
            }
        }
        return max;
    }

    public String getTrendIconColor() {
        if (getTrendIcon() == Constants.LESS_FAILED_ICON) {
            return Constants.LESS_FAILED_COLOR;
        }
        if (getTrendIcon() == Constants.MORE_FAILED_ICON) {
            return Constants.MORE_FAILED_COLOR;
        }
        return null;
    }

    @Exported
    public String getTrendIconMessage() {
        if (getTrendIcon() == Constants.LESS_FAILED_ICON) {
            return this.getLastHourFailed() - this.failedEnabledJobs + " less failed";
        }
        if (getTrendIcon() == Constants.MORE_FAILED_ICON) {
            return this.failedEnabledJobs - this.getLastHourFailed() + " more failed";
        }
        return null;
    }

    /**
     * Calculates when the periodic update should start for the first time. That
     * is the next possible round hour at 00 min and 0 seconds.
     *
     * @return the date of the start
     */
    private Date calcStartingPoint() {
        final Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + 1) % 24);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        // LOGGER.info(c.toString());
        return c.getTime();
    }

    /**
     * Returns the current hour.
     *
     * @return the current hour as int.
     */
    private int getCurrentHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    private void startRecording() {

        new Timer()
                .schedule(
                        new TimerTask() {

                            @Override
                            public void run() {
                                LOGGER.info("Gathering information about failed builds...");
                                updateFailedJobs();
                                LOGGER.info("Writing information about failed builds...");
                                writeData();
                            }
                        },
                        this.calcStartingPoint(),
                        Constants.REFRESH_RATE);
    }

    /**
     * What should be written every hour.
     */
    private void writeData() {
        write24Hours();
    }

    /**
     * Rewrites the 24-hour change file, by adding information for the current
     * hour. Called every hour.
     */
    private void write24Hours() {
        // retrieve the number of failed jobs for today

        final int[] today = this.get24Hours("today");
        final int[] yesterday = this.get24Hours("yesterday");

        // update both for the current hours
        // yesterday = today, today = current number of failed jobs
        yesterday[this.getCurrentHour()] = today[this.getCurrentHour()];
        today[this.getCurrentHour()] = this.failedEnabledJobs;

        // write changes to tmp file
        BufferedWriter bw = null;
        File tmp = null;

        try {
            tmp = new File(Constants.file_tmp);
            bw = new BufferedWriter(new FileWriter(tmp.getPath(), true));
            for (int i = 0; i < Constants.HOURS_PER_DAY; i++) {
                bw.write(i + " " + today[i] + " " + yesterday[i]);
                bw.newLine();
            }
            bw.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bw != null) {
                try {
                    bw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // rename tmp to the old(outdated) file
        if (tmp != null) {
            try {
                Files.move(tmp.toPath(), this.file_24hours.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                LOGGER.warning("Could not update failed-build history: " + e.getMessage());
            }
        }
        LOGGER.info("Information written successfully. Writing image...");
        this.createHourGraphic1();
        LOGGER.info("Image written successfully!");
    }

    /**
     * Creates a Histogram that shows the number of failed jobs for every hour.
     * This method is called every hour.
     *
     */
    public void createHourGraphic1() {
        this.lc = new LocalConfig();

        Color background = decodeColor(lc.getHistBackground(), Color.WHITE);
        Color columnColor = decodeColor(lc.getHistColumn(), Color.BLACK);
        Color currentColumnColor = decodeColor(lc.getHistCurrentColumn(), Color.DARK_GRAY);
        Color hoursColor = decodeColor(lc.getHistHourText(), Color.BLACK);
        Color countColor = decodeColor(lc.getHistNumberText(), Color.BLACK);

        final int widthMultiplicator = this.lc.getHistogramWidth();
        final int histWidth = Constants.HOURS_PER_DAY * widthMultiplicator;
        final int histHeight = this.lc.getHistogramHeight();

        final BufferedImage image = new BufferedImage(histWidth, histHeight, BufferedImage.TYPE_INT_RGB);

        final Graphics g = image.getGraphics();
        final Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(background);
        g2d.fillRect(0, 0, histWidth, histHeight);

        final int[] hours = this.get24Hours("today");

        double heightMultiplicator =
                (double) (histHeight - widthMultiplicator * 2) / (double) Math.max(hours[Constants.HOURS_PER_DAY], 1);

        createFailedJobsImage(
                background,
                columnColor,
                currentColumnColor,
                hoursColor,
                countColor,
                widthMultiplicator,
                histHeight,
                g2d,
                hours,
                heightMultiplicator);

        saveFailedGraphic(image);

        g.dispose();
    }

    private Color decodeColor(String value, Color fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }

        try {
            return Color.decode(value);
        } catch (NumberFormatException e) {
            LOGGER.warning("Invalid histogram color: " + value);
            return fallback;
        }
    }

    private void createFailedJobsImage(
            Color background,
            Color columnColor,
            Color currentColumnColor,
            Color hoursColor,
            Color countColor,
            final int widthMultiplicator,
            final int histHeight,
            final Graphics2D g2d,
            final int[] hours,
            double heightMultiplicator) {
        // holds the current hour being added to the histogram
        int i = this.getHistogramStartingHour(lc.getHistSorting());
        // holds the position of this hour from left to right
        int j = 0;
        do {

            // draws a rectangle according to the number of failed builds for
            // the current hour
            if (i == this.getCurrentHour()) {
                g2d.setColor(currentColumnColor);
            } else {
                g2d.setColor(columnColor);
            }

            g2d.fillRect(
                    0 + j * widthMultiplicator,
                    histHeight - widthMultiplicator - (int) (hours[i] * heightMultiplicator),
                    widthMultiplicator,
                    (int) (hours[i] * heightMultiplicator));
            // draws a gap between the rectangles
            g2d.setColor(background);
            g2d.drawRect(
                    0 + j * widthMultiplicator,
                    histHeight - widthMultiplicator - (int) (hours[i] * heightMultiplicator),
                    widthMultiplicator,
                    (int) (hours[i] * heightMultiplicator));

            // puts hours on the x-axis
            // Font font = new Font(g2d.getFont().getName(), Font.PLAIN, 14);
            // g2d.setFont(font);
            g2d.setColor(hoursColor);
            g2d.drawString(
                    i + "h",
                    this.getStartingStringPos(Math.max(i, 1) * 10, widthMultiplicator) + j * widthMultiplicator,
                    histHeight - (int) (widthMultiplicator / 1.7));

            // puts the number of failed build on top of each rectangle
            // font = new Font(g2d.getFont().getName(), Font.PLAIN, 16);
            // g2d.setFont(font);
            g2d.setColor(countColor);
            g2d.drawString(
                    hours[i] == -1 ? "x" : hours[i] + "",
                    this.getStartingStringPos(hours[i], widthMultiplicator) + j * widthMultiplicator,
                    histHeight
                            - widthMultiplicator
                            - (int) ((hours[i] == -1 ? 0 : hours[i]) * heightMultiplicator)
                            - widthMultiplicator / 4);
            i = (i + 1) % Constants.HOURS_PER_DAY;
            j++;
        } while (i != this.getHistogramStartingHour(lc.getHistSorting()));
    }

    private void saveFailedGraphic(final BufferedImage image) {
        final File graphic1File = new File(Constants.FAILED_JOB_GRAPHIC_1);

        try {
            File parent = graphic1File.getParentFile();

            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                LOGGER.warning("Could not create histogram directory: " + parent.getAbsolutePath());
                return;
            }

            if (!graphic1File.exists() && !graphic1File.createNewFile()) {
                LOGGER.warning("Could not create histogram file: " + graphic1File.getAbsolutePath());
                return;
            }

            ImageIO.write(image, "jpg", graphic1File);
        } catch (IOException e) {
            LOGGER.warning("Could not save failed-build histogram: " + e.getMessage());
        }
    }

    /**
     * Calculates the histogram starting hour according to the configuration.
     *
     * @param sorting
     *            what type of sorting has been chosen in the conf.
     */
    private int getHistogramStartingHour(int sorting) {
        if (sorting == 2) {
            return this.getCurrentHour();
        }
        if (sorting == 3) {
            return (this.getCurrentHour() + 1) % Constants.HOURS_PER_DAY;
        }
        return 0;
    }

    /**
     * calculates the starting position for a string in the histogram. Used to
     * determine the position of the hour strings and the count strings.
     *
     * @param i
     *            for which hour we are calculating.
     * @param w
     *            multiplicator for the current hisogram.
     * @return position of the string as int.
     */
    private int getStartingStringPos(int i, int w) {
        String s = ((i == -1) ? "x" : i + "");
        return Math.max(1, ((w - Constants.PIXELSPERSYMBOL * s.length()) / 2));
    }

    /**
     * Returns the content of the file that stores info about the last 24 hours.
     *
     * @param s
     *            if parameter is "today" then the info returned is for the last
     *            24 hours, if not then the info is for the period 24-48 hours
     * @return array with 24 elements containing the number of failed jobs for
     *         every hour
     */
    private int[] get24Hours(String s) {
        final int[] hours = new int[Constants.HOURS_PER_DAY + 1];
        Arrays.fill(hours, -1);

        if (this.file_24hours == null) {
            return hours;
        }

        final int valueIndex = "today".equals(s) ? 1 : 2;

        try (BufferedReader br = Files.newBufferedReader(this.file_24hours.toPath(), StandardCharsets.UTF_8)) {

            String current;

            while ((current = br.readLine()) != null) {
                final String[] tokens = current.trim().split("\\s+");

                if (tokens.length <= valueIndex) {
                    continue;
                }

                try {
                    final int index = Integer.parseInt(tokens[0]);
                    final int value = Integer.parseInt(tokens[valueIndex]);

                    if (index >= 0 && index < Constants.HOURS_PER_DAY) {
                        hours[index] = value;
                        hours[Constants.HOURS_PER_DAY] = Math.max(hours[Constants.HOURS_PER_DAY], value);
                    }
                } catch (NumberFormatException e) {
                    LOGGER.warning("Ignoring invalid entry in failed-build history: " + current);
                }
            }
        } catch (IOException e) {
            LOGGER.warning("Could not read failed-build history: " + e.getMessage());
        }

        return hours;
    }

    /**
     * Creates the history files if they do not exist.
     */
    private void createHistoryFiles() {
        this.file_24hours = new File(Constants.file_24hours);

        try {
            if (!this.file_24hours.exists() && !this.file_24hours.createNewFile()) {
                LOGGER.warning("Could not create failed-build history file: " + this.file_24hours.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.warning("Could not create failed-build history file: " + e.getMessage());
        }
    }
}
