package com.appmindlab.nano;

/**
 * Created by saelim on 8/3/2015.
 */
public class Snapshot {
    private String mContent;
    private long mPos;
    private long mTimestamp;

    public Snapshot(String content, long pos) {
        this.mContent = content;
        this.mPos = pos;
        this.mTimestamp = System.currentTimeMillis();
    }

    protected String getContent() {
        return this.mContent;
    }

    protected long getPos() {
        return this.mPos;
    }

    protected long getTimestamp() {
        return this.mTimestamp;
    }
}
