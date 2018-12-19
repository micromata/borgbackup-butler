package de.micromata.borgbutler;

import de.micromata.borgbutler.config.BorgRepoConfig;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * Represents a command to be queued for executing.
 */
public class BorgCommand {
    private Logger log = LoggerFactory.getLogger(BorgCommand.class);

    /**
     * The current Thread assigned to this command sets the state and reads for the state {@link State#TERMINATE_SIGNAL_RECEIVED}.
     * If received, the thread will be terminated without executing the borg command.
     */
    public enum State {
        /**
         * This command is waiting in the queue.
         */
        WAITING_IN_QUEUE,
        /**
         * This command is running (beeing executed).
         */
        RUNNING,
        /**
         * This command should not be terminated before starting.
         */
        TERMINATE_SIGNAL_RECEIVED}

    public enum ResultStatus {
        OK, ERROR
    }

    @Getter
    @Setter
    private File workingDir;
    @Getter
    private String[] args;
    @Getter
    private String[] params;
    @Getter
    @Setter
    private BorgRepoConfig repoConfig;
    @Getter
    @Setter
    private String command;
    @Getter
    @Setter
    private String archive;
    @Getter
    private State state;
    /**
     * For displaying and information purposes for the user only, when browsing the current command queue.
     */
    @Setter
    @Getter
    private String description;
    /**
     * The result of the call will be written to this String.
     */
    @Getter
    @Setter(AccessLevel.PACKAGE)
    private String response;

    @Getter
    @Setter(AccessLevel.PACKAGE)
    private ResultStatus resultStatus;

    /**
     * Requests the thread handling this command to terminate / cancel this action.
     */
    public void terminate() {
        this.state = State.TERMINATE_SIGNAL_RECEIVED;
    }

    BorgCommand setArgs(String... args) {
        this.args = args;
        return this;
    }

    BorgCommand setParams(String... params) {
        this.params = params;
        return this;
    }

    String getRepoArchive() {
        if (archive == null) {
            return repoConfig.getRepo();
        }
        return repoConfig.getRepo() + "::" + archive;
    }

    /**
     *
     * @return Abbreviated response e. g. for logging an error.
     */
    public String getAbbreviatedResponse() {
        return StringUtils.abbreviate(response, 1000);
    }
}
