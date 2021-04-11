package com.appmindlab.nano;

import java.util.Date;

/**
 * Created by saelim on 6/24/2015.
 */
public class DBEntry {
    private long mId;
    private String mTitle;
    private String mContent;

    private int mStar;
    private int mDeleted;

    private Date mCreated;
    private Date mModified;
    private Date mAccessed;

    private String mMetadata;
    private long mPos;
    private String mPasscode;
    private double mLatitude;
    private double mLongitude;
    private double mDistance;

    private long mSize;

    public DBEntry() {
        Date now = new Date();

        this.mId = -1L;
        this.mTitle = "";
        this.mContent = "";

        this.mStar = 0;
        this.mDeleted = 0;

        this.mCreated = now;
        this.mModified = now;
        this.mAccessed = now;

        this.mMetadata = "";
        this.mPos = -1;
        this.mPasscode = "";
        this.mLatitude = -1;
        this.mLongitude = -1;
        this.mDistance = -1;

        this.mSize = -1;
    }

    public long getId() {return this.mId;}
    public void setId(long id) {this.mId = id;}

    public String getTitle() {return this.mTitle;}
    public void setTitle(String title) {this.mTitle = title;}

    public String getContent() {return this.mContent;}
    public void setContent(String content) {this.mContent = content;}

    public int getStar() {return this.mStar;}
    public void setStar(int star) {this.mStar = star;}

    public int getDeleted() {return this.mDeleted;}
    public void setDeleted(int deleted) {this.mDeleted = deleted;}

    public Date getCreated() { return this.mCreated;}
    public void setCreated(Date date) {this.mCreated = date;}

    public Date getModified() {
        return this.mModified;
    }
    public void setModified(Date date) {
        this.mModified = date;
    }

    public Date getAccessed() {
        return this.mAccessed;
    }
    public void setAccessed(Date date) {
        this.mAccessed = date;
    }

    public String getMetadata() {
        return this.mMetadata;
    }
    public void setMetadata(String metadata) {
        this.mMetadata = metadata;
    }

    public long getPos() {
        return this.mPos;
    }
    public void setPos(long pos) {
        this.mPos = pos;
    }

    public double getLatitude() {
        return this.mLatitude;
    }
    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public double getLongitude() {
        return this.mLongitude;
    }
    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    public void setDistance(double distance) {
        this.mDistance = distance;
    }
    public double getDistance() {
        return this.mDistance;
    }

    public void setSize(long size) {
        this.mSize = size;
    }
    public long getSize() {
        return this.mSize;
    }

    public String getPasscode() {
        return this.mPasscode;
    }
    public void setPasscode(String passcode) {
        this.mPasscode = passcode;
    }
}
