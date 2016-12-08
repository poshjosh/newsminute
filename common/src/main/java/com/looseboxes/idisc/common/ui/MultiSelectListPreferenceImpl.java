package com.looseboxes.idisc.common.ui;

import android.content.Context;
import android.preference.MultiSelectListPreference;

/**
 * Created by Josh on 9/12/2016.
 */
public class MultiSelectListPreferenceImpl extends MultiSelectListPreference {

    public MultiSelectListPreferenceImpl(
            Context context, int idRes,
            int titleRes, int summaryRes, int dialogTitleRes,
            CharSequence[] values) {

        this(context, idRes, titleRes, summaryRes, dialogTitleRes, values, values);
    }

    public MultiSelectListPreferenceImpl(
            Context context, int idRes,
            int titleRes, int summaryRes, int dialogTitleRes,
            CharSequence[] entries, CharSequence[] entryValues) {

        this(context, context.getString(idRes), titleRes, summaryRes, dialogTitleRes, entries, entryValues);
    }

    public MultiSelectListPreferenceImpl(
            Context context, String key,
            int titleRes, int summaryRes, int dialogTitleRes,
            CharSequence[] values) {

        this(context, key, titleRes, summaryRes, dialogTitleRes, values, values);
    }

    public MultiSelectListPreferenceImpl(
            Context context, String key,
            int titleRes, int summaryRes, int dialogTitleRes,
            CharSequence[] entries, CharSequence[] entryValues) {

        super(context);

        this.setKey(key);
        this.setTitle(titleRes);
        this.setSummary(summaryRes);
        this.setDialogTitle(dialogTitleRes);
        this.setEntries(entries);
        this.setEntryValues(entryValues);
    }

    public void show() {
        this.showDialog(null);
    }
}
