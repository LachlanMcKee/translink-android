package com.lach.common.data.provider;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.lach.common.log.Log;

public class ContactAddressExtractor {
    private static final String TAG = "ContactAddressExtractor";

    private static final String[] projection = {
            ContactsContract.CommonDataKinds.StructuredPostal.STREET,
            ContactsContract.CommonDataKinds.StructuredPostal.CITY
    };

    public String getContactsAddress(Context context, Uri contactUri) {
        Cursor cursor;

        try {
            cursor = context.getContentResolver().query(contactUri, projection, null, null, null);
        } catch (Exception ex) {
            Log.error(TAG, "Error obtaining contacts content", ex);
            return null;
        }

        // The contacts query may not work, we should safe-guard against this possibility.
        if (cursor == null) {
            return null;
        }

        try {
            if (cursor.moveToFirst()) {
                int streetIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
                int cityIndex = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.CITY);

                cursor.moveToFirst();
                String street = cursor.getString(streetIndex);
                String suburb = cursor.getString(cityIndex);

                String address = street;
                if (suburb != null) {
                    address += ", " + suburb;
                }

                return address;
            }
        } finally {
            cursor.close();
        }

        return null;
    }

}
