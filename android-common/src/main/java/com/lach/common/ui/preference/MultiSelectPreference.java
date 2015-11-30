package com.lach.common.ui.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.lach.common.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author declanshanaghy http://blog.350nice.com/wp/archives/240 MultiChoice Preference Widget for
 *         Android
 * @contributor matiboy Added support for check all/none and custom separator defined in XML.
 * IMPORTANT: The following attributes MUST be defined (probably inside attr.xml) for
 * the code to even compile <declare-styleable name="MultiSelectPreference"> <attr
 * format="string" name="checkAll" /> <attr format="string" name="separator" />
 * </declare-styleable> Whether you decide to then use those attributes is up to you.
 */
public class MultiSelectPreference extends EnhancedListPreference {
    private final String separator;
    private boolean isValueMandatory;

    private static final String DEFAULT_SEPARATOR = "~";
    private boolean[] mClickedDialogEntryIndices;

    // Constructor
    public MultiSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        String preferenceSeparator;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MultiSelectPreference);
        try {
            preferenceSeparator = typedArray.getString(R.styleable.MultiSelectPreference_separator);
            isValueMandatory = typedArray.getBoolean(R.styleable.MultiSelectPreference_value_mandatory, false);
        } finally {
            typedArray.recycle();
        }

        if (preferenceSeparator != null) {
            separator = preferenceSeparator;
        } else {
            separator = DEFAULT_SEPARATOR;
        }
        // Initialize the array of boolean to the same size as number of entries
        mClickedDialogEntryIndices = new boolean[getEntries().length];
    }

    @Override
    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
        // Initialize the array of boolean to the same size as number of entries
        mClickedDialogEntryIndices = new boolean[entries.length];
    }

    @Override
    protected void onPrepareDialogBuilder(android.support.v7.app.AlertDialog.Builder builder) {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();
        if (entries == null || entryValues == null || entries.length != entryValues.length) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        restoreCheckedEntries();
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean val) {
                        mClickedDialogEntryIndices[which] = val;
                    }
                });
    }

    @Override
    protected void onDialogCreated(final android.support.v7.app.AlertDialog dialog) {
        if (!isValueMandatory) {
            // If the value isn't mandatory, don't add this listener.
            return;
        }

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean valueSelected = false;

                        for (boolean mClickedDialogEntryIndice : mClickedDialogEntryIndices) {
                            if (mClickedDialogEntryIndice) {
                                valueSelected = true;
                                break;
                            }
                        }

                        if (valueSelected) {
                            MultiSelectPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                            dialog.dismiss();
                        } else {
                            Toast.makeText(getContext(), "You must select at least one value", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private String[] parseStoredValue(CharSequence val) {
        if ("".equals(val)) {
            return null;
        } else {
            String saveData = val.toString();
            return saveData.split(separator);
        }
    }

    private void restoreCheckedEntries() {
        CharSequence[] entryValues = getEntryValues();

        // Explode the string read in sharedpreferences
        String[] vals = parseStoredValue(getValue());

        if (vals != null) {
            List<String> valuesList = Arrays.asList(vals);
            for (int i = 0; i < entryValues.length; i++) {
                mClickedDialogEntryIndices[i] = valuesList.contains(entryValues[i].toString());
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // super.onDialogClosed(positiveResult);
        ArrayList<String> values = new ArrayList<>();

        if (positiveResult) {
            CharSequence[] entryValues = getEntryValues();
            if (entryValues != null) {
                for (int i = 0; i < entryValues.length; i++) {
                    if (mClickedDialogEntryIndices[i]) {
                        values.add((String) entryValues[i]);
                    }
                }

                if (callChangeListener(values)) {
                    setValue(join(values, separator));
                }
            }
        }
    }

    @Override
    protected void updateSummary(String value) {
        boolean summaryAssigned = false;
        if (value != null) {
            String[] values = parseStoredValue(value);

            if (values != null) {
                setSummary(join(Arrays.asList(values), ", "));
                summaryAssigned = true;
            }
        }
        if (!summaryAssigned) {
            setSummary("None selected");
        }
    }

    // Credits to kurellajunior on this post http://snippets.dzone.com/posts/show/91
    private static String join(Iterable<?> pColl, String separator) {
        Iterator<?> oIter;
        if (pColl == null || (!(oIter = pColl.iterator()).hasNext()))
            return "";
        StringBuilder oBuilder = new StringBuilder(String.valueOf(oIter.next()));
        while (oIter.hasNext())
            oBuilder.append(separator).append(oIter.next());
        return oBuilder.toString();
    }

}
