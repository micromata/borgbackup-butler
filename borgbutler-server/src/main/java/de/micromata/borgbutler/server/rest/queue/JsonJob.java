package de.micromata.borgbutler.server.rest.queue;

import de.micromata.borgbutler.BorgJob;
import de.micromata.borgbutler.jobs.AbstractJob;
import de.micromata.borgbutler.json.borg.ProgressMessage;
import de.micromata.borgbutler.server.user.UserUtils;
import lombok.Getter;
import lombok.Setter;

public class JsonJob {
    @Getter
    @Setter
    private boolean cancelledRequested;
    @Getter
    @Setter
    private AbstractJob.Status status;
    @Getter
    @Setter
    private String title;
    @Getter
    private String description;
    @Getter
    @Setter
    private String progressText;
    @Setter
    @Getter
    private ProgressMessage progressMessage;
    @Getter
    private String commandLineAsString;

    public JsonJob() {
    }

    public JsonJob(BorgJob<?> borgJob) {
        this.cancelledRequested = borgJob.isCancelledRequested();
        this.status = borgJob.getStatus();
        this.title = borgJob.getTitle();
        ProgressMessage progressMessage = borgJob.getProgressMessage();
        if (progressMessage != null) {
            this.progressMessage = progressMessage;
            this.progressText = progressMessageToString();
        }
        this.commandLineAsString = borgJob.getCommandLineAsString();
        this.description = borgJob.getDescription();
    }

    public String progressMessageToString() {
        if (progressMessage == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        if (progressMessage.getMessage()!= null) {
            sb.append(progressMessage.getMessage());
        }
        if (progressMessage.getCurrent() > 0) {
            sb.append(" (").append(UserUtils.formatNumber(progressMessage.getCurrent()));
            if (progressMessage.getTotal() > 0) {
                sb.append("/").append(UserUtils.formatNumber(progressMessage.getTotal()));
            }
            sb.append(")");
        }
        if (progressMessage.isFinished()) {
            sb.append(" (finished)");
        }
        sb.append(".");
        return sb.toString();
    }

}
