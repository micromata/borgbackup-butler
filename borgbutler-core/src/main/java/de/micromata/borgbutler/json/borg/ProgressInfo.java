package de.micromata.borgbutler.json.borg;

/**
 * Output of borg option <tt>--progress</tt>.
 * See https://borgbackup.readthedocs.io/en/stable/internals/frontends.html,
 */
public class ProgressInfo implements Cloneable {
    // {"message": "Calculating statistics...   0%", "current": 1, "total": 2497, "info": null, "operation": 1, "msgid": null, "type": "progress_percent", "finished": false, "time": 1546640510.116256}
    /**
     * e. g. Calculating statistics...   5%
     */
    private String message;
    /**
     * Current counter of total.
     */
    private long current;
    private long total;
    /**
     * Array that describes the current item, may be null, contents depend on msgid.
     */
    private String[] info;
    /**
     * unique, opaque integer ID of the operation.
     */
    private int operation;
    private String msgid;
    /**
     * e. g. progress_percent
     */
    private String type;
    private boolean finished;
    /**
     * Unix timestamp (float).
     */
    private double time;

    public ProgressInfo incrementCurrent() {
        ++current;
        return this;
    }

    @Override
    public ProgressInfo clone() {
        ProgressInfo clone = null;
        try {
            clone = (ProgressInfo) super.clone();
        } catch (CloneNotSupportedException ex) {
            throw new UnsupportedOperationException(this.getClass().getCanonicalName() + " isn't cloneable: " + ex.getMessage(), ex);
        }
        return clone;
    }

    public String getMessage() {
        return this.message;
    }

    public long getCurrent() {
        return this.current;
    }

    public long getTotal() {
        return this.total;
    }

    public String[] getInfo() {
        return this.info;
    }

    public int getOperation() {
        return this.operation;
    }

    public String getMsgid() {
        return this.msgid;
    }

    public String getType() {
        return this.type;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public double getTime() {
        return this.time;
    }

    public ProgressInfo setMessage(String message) {
        this.message = message;
        return this;
    }

    public ProgressInfo setCurrent(long current) {
        this.current = current;
        return this;
    }

    public ProgressInfo setTotal(long total) {
        this.total = total;
        return this;
    }
}
