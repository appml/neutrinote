package com.appmindlab.nano;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by saelim on 8/1/2015.
 */
public class HitParcelable implements Parcelable {
    int pos;

    public HitParcelable(int pos) {
        this.pos = pos;
    }

    private HitParcelable(Parcel in) {
        pos = in.readInt();
    }

    public int describeContents() {
        return 0;
    }

    protected int val() {
        return pos;
    }

    @Override
    public String toString() {
        return Integer.toString(pos);
    }

    public static final Parcelable.Creator<HitParcelable> CREATOR = new Parcelable.Creator<HitParcelable>() {
        public HitParcelable createFromParcel(Parcel in) {
            return new HitParcelable(in);
        }

        public HitParcelable[] newArray(int size) {
            return new HitParcelable[size];
        }
    };

    @Override
    public void writeToParcel(Parcel out, int flags) {
        // TODO Auto-generated method stub
        out.writeInt(pos);
    }
}
