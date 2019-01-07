package de.micromata.borgbutler.server.rest.queue;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.micromata.borgbutler.BorgJob;
import de.micromata.borgbutler.jobs.AbstractJob;
import de.micromata.borgbutler.json.borg.ProgressInfo;
import de.micromata.borgbutler.server.user.UserUtils;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class JsonJob {
    @Getter
    @Setter
    private boolean cancellationRequested;
    @Getter
    @Setter
    private AbstractJob.Status status;
    @Getter
    @Setter
    private String title;
    @Getter
    @Setter
    private String description;
    @Getter
    @Setter
    private String progressText;
    @Getter
    @Setter
    private ProgressInfo progressInfo;
    @Getter
    @Setter
    private String commandLineAsString;
    @Getter
    @Setter
    private long uniqueJobNumber;
    @Getter
    @Setter
    private String[] environmentVariables;

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
}
