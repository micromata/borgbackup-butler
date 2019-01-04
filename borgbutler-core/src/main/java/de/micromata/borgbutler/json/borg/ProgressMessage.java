package de.micromata.borgbutler.json.borg;

import lombok.Getter;
import lombok.Setter;

/**
 * Output of borg option <tt>--progress</tt>.
 * See https://borgbackup.readthedocs.io/en/stable/internals/frontends.html,
 */
public class ProgressMessage implements Cloneable {
    // {"message": "Calculating statistics...   0%", "current": 1, "total": 2497, "info": null, "operation": 1, "msgid": null, "type": "progress_percent", "finished": false, "time": 1546640510.116256}
    /**
     * e. g. Calculating statistics...   5%
     */
    @Getter
    @Setter
    private String message;
    /**
     * Current counter of total.
     */
    @Getter
    @Setter
    private long current;
    @Getter
    private long total;
    /**
     * Array that describes the current item, may be null, contents depend on msgid.
     */
    @Getter
    private String[] info;
    /**
     * unique, opaque integer ID of the operation.
     */
    @Getter
    private int operation;
    @Getter
    private int msgid;
    /**
     * e. g. progress_percent
     */
    @Getter
    private String type;
    @Getter
    private boolean finished;
    /**
     * Unix timestamp (float).
     */
    @Getter
    private double time;

    public ProgressMessage incrementCurrent() {
        ++current;
        return this;
    }

    @Override
    public ProgressMessage clone() {
        ProgressMessage clone = null;
        try {
            clone = (ProgressMessage) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " isn't cloneable: " + ex.getMessage(), ex);
        }
        return clone;
    }
}
