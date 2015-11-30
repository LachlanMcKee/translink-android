package com.lach.translink.data.location;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @deprecated
 */
public class ResolvedLocationList extends ArrayList<ResolvedLocation> implements Serializable {

    private static final long serialVersionUID = 1L;
    private ResolvedLocation lastPinned = null;

    @Override
    public boolean add(ResolvedLocation object) {
        int index = 0;

        for (ResolvedLocation other : this) {

            if (object.displayAddress.compareToIgnoreCase(other.displayAddress) > 1) {
                index++;
            }

            if (object.pinned) {
                index = index - unpinnedCount();

                if (lastPinned != null) {
                    if (object.displayAddress.compareToIgnoreCase(
                            lastPinned.displayAddress) > 1) {
                        index++;
                    }
                }

                lastPinned = object;
            }

        }

        if (index < 0) {
            index = 0;
        }

        add(index, object);
        return true;
    }

    public void sort() {
        Collections.sort(this, new Comparator<ResolvedLocation>() {

            @Override
            public int compare(ResolvedLocation object1, ResolvedLocation object2) {
                return object1.displayAddress.compareToIgnoreCase(object2.displayAddress);
            }

        });
    }

    private int unpinnedCount() {
        int counter = 0;
        for (ResolvedLocation location : this) {
            if (!location.pinned) {
                counter++;
            }
        }
        return counter;
    }

}
