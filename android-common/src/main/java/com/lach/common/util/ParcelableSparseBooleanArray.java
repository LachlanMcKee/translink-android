package com.lach.common.util;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseBooleanArray;

public class ParcelableSparseBooleanArray extends SparseBooleanArray implements Parcelable {

    public ParcelableSparseBooleanArray() {
        super();
    }

    public ParcelableSparseBooleanArray(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        int size = size();
        int[] keys = new int[size];
        boolean[] vals = new boolean[size];

        for (int i = 0; i < size; i++) {
            keys[i] = keyAt(i);
            vals[i] = valueAt(i);
        }

        parcel.writeInt(size);
        parcel.writeIntArray(keys);
        parcel.writeBooleanArray(vals);
    }

    public static final Creator<ParcelableSparseBooleanArray> CREATOR = new Creator<ParcelableSparseBooleanArray>() {
        @Override
        public ParcelableSparseBooleanArray createFromParcel(Parcel parcel) {
            int size = parcel.readInt();

            int[] keys = new int[size];
            parcel.readIntArray(keys);

            boolean[] vals = new boolean[size];
            parcel.readBooleanArray(vals);

            ParcelableSparseBooleanArray array = new ParcelableSparseBooleanArray(size);
            for (int i = 0; i < size; i++) {
                array.put(keys[i], vals[i]);
            }

            return array;
        }

        @Override
        public ParcelableSparseBooleanArray[] newArray(int size) {
            return new ParcelableSparseBooleanArray[size];
        }
    };
}