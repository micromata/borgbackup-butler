package de.micromata.borgbutler.server.rest.queue;

import de.micromata.borgbutler.BorgJob;
import de.micromata.borgbutler.jobs.AbstractJob;
import de.micromata.borgbutler.json.borg.ProgressInfo;
import de.micromata.borgbutler.server.user.UserUtils;
import lombok.Getter;
import lombok.Setter;

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

    public JsonJob() {
    }

    public JsonJob(BorgJob<?> borgJob) {
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
    }

    /**
     * Builds and sets progressText from the progressInfo object if given.
     * @return progressText
     */
    public String buildProgressText() {
        if (progressInfo == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (progressInfo.getMessage() != null) {
            sb.append(progressInfo.getMessage());
        }
        if (progressInfo.getCurrent() > 0) {
            sb.append(" (").append(UserUtils.formatNumber(progressInfo.getCurrent()));
            if (progressInfo.getTotal() > 0) {
                sb.append("/").append(UserUtils.formatNumber(progressInfo.getTotal()));
            }
            sb.append(")");
        }
        if (progressInfo.isFinished()) {
            sb.append(" (finished)");
        }
        sb.append(".");
        progressText = sb.toString();
        return progressText;
    }

}
