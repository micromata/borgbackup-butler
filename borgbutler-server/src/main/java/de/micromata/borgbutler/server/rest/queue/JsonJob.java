package de.micromata.borgbutler.server.rest.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.BorgJob;
import de.micromata.borgbutler.jobs.AbstractJob;
import de.micromata.borgbutler.json.borg.ProgressInfo;
import de.micromata.borgbutler.server.user.UserUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class JsonJob {
    private boolean cancellationRequested;
    private AbstractJob.Status status;
    private String title;
    private String description;
    private String progressText;
    private ProgressInfo progressInfo;
    private String commandLineAsString;
    private long uniqueJobNumber;
    private String[] environmentVariables;
    private String createTime;
    private String startTime;
    private String stopTime;

    public JsonJob() {
    }

    public JsonJob(BorgJob<?> borgJob) {
        this.uniqueJobNumber = borgJob.getUniqueJobNumber();
        this.cancellationRequested = borgJob.isCancellationRequested();
        this.status = borgJob.getStatus();
        this.title = borgJob.getTitle();
        ProgressInfo progressInfo = borgJob.getProgressInfo();
        if (progressInfo != null) {
            this.progressInfo = progressInfo;
            buildProgressText();
        }
        this.commandLineAsString = borgJob.getCommandLineAsString();
        this.description = borgJob.getDescription();
        environmentVariables = borgJob.getCommand().getRepoConfig().getEnvironmentVariables();
        this.createTime = borgJob.getCreateTime();
        this.startTime = borgJob.getStartTime();
        this.stopTime = borgJob.getStopTime();
    }

    /**
     * Builds and sets progressText from the progressInfo object if given.
     *
     * @return progressText
     */
    public String buildProgressText() {
        if (progressInfo == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (progressInfo.getCurrent() > 0) {
            if (StringUtils.indexOf(progressInfo.getMessage(), '%') < 0) {
                // No percentage given by borg, try to create an own one:
                short percentage = getProgressPercent();
                if (percentage >= 0) {
                    sb.append(percentage).append("%");
                }
            }
            sb.append(" (");
            if ("extract".equals(progressInfo.getMsgid())) {
                sb.append(FileUtils.byteCountToDisplaySize(progressInfo.getCurrent()));
            } else {
                sb.append(UserUtils.formatNumber(progressInfo.getCurrent()));
            }
            if (progressInfo.getTotal() > 0) {
                sb.append("/");
                if ("extract".equals(progressInfo.getMsgid())) {
                    sb.append(FileUtils.byteCountToDisplaySize(progressInfo.getTotal()));
                } else {

                    sb.append(UserUtils.formatNumber(progressInfo.getTotal()));
                }
            }
            sb.append("): ");
        }
        if (progressInfo.getMessage() != null) {
            sb.append(progressInfo.getMessage());
        }
        if (progressInfo.isFinished()) {
            sb.append(" (finished)");
        }
        progressText = sb.toString();
        return progressText;
    }

    /**
     * If current and total of {@link ProgressInfo} is available, this value is given, otherwise this value is -1.
     */
    @JsonProperty
    public short getProgressPercent() {
        if (progressInfo == null || progressInfo.getTotal() <= 0) {
            return -1;
        }
        long value = 100 * progressInfo.getCurrent() / progressInfo.getTotal();
        if (value < 0) {
            return 0;
        }
        if (value >= 100) {
            return 100;
        }
        return (short) value;
    }

    public boolean isCancellationRequested() {
        return this.cancellationRequested;
    }

    public AbstractJob.Status getStatus() {
        return this.status;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getProgressText() {
        return this.progressText;
    }

    public ProgressInfo getProgressInfo() {
        return this.progressInfo;
    }

    public String getCommandLineAsString() {
        return this.commandLineAsString;
    }

    public long getUniqueJobNumber() {
        return this.uniqueJobNumber;
    }

    public String[] getEnvironmentVariables() {
        return this.environmentVariables;
    }

    public String getCreateTime() {
        return this.createTime;
    }

    public String getStartTime() {
        return this.startTime;
    }

    public String getStopTime() {
        return this.stopTime;
    }

    public JsonJob setCancellationRequested(boolean cancellationRequested) {
        this.cancellationRequested = cancellationRequested;
        return this;
    }

    public JsonJob setStatus(AbstractJob.Status status) {
        this.status = status;
        return this;
    }

    public JsonJob setTitle(String title) {
        this.title = title;
        return this;
    }

    public JsonJob setDescription(String description) {
        this.description = description;
        return this;
    }

    public JsonJob setProgressText(String progressText) {
        this.progressText = progressText;
        return this;
    }

    public JsonJob setProgressInfo(ProgressInfo progressInfo) {
        this.progressInfo = progressInfo;
        return this;
    }

    public JsonJob setCommandLineAsString(String commandLineAsString) {
        this.commandLineAsString = commandLineAsString;
        return this;
    }

    public JsonJob setUniqueJobNumber(long uniqueJobNumber) {
        this.uniqueJobNumber = uniqueJobNumber;
        return this;
    }

    public JsonJob setEnvironmentVariables(String[] environmentVariables) {
        this.environmentVariables = environmentVariables;
        return this;
    }

    public JsonJob setCreateTime(String createTime) {
        this.createTime = createTime;
        return this;
    }

    public JsonJob setStartTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public JsonJob setStopTime(String stopTime) {
        this.stopTime = stopTime;
        return this;
    }
}
