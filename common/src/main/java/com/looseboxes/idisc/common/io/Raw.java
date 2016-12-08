package com.looseboxes.idisc.common.io;

import android.content.Context;

import com.looseboxes.idisc.common.R;

import java.util.Map;

public class Raw extends com.bc.android.core.io.Raw {

    public static Map<String, String> get(Context context) {
        final int[] rawResourceIds = getRawResourceIds();
        return get(context, rawResourceIds);
    }

    public static Map<String, String> get(Context context, int [] rawResourceIds) {
        final String [] keys = new String[rawResourceIds.length];
        for(int i=0; i<rawResourceIds.length; i++) {
            keys[i] = getLabel(context, rawResourceIds[i]);
        }
        return get(context, keys, rawResourceIds);
    }

    public static int [] getRawResourceIdsForDefaultFeedlist() {
        final int [] rawResourceIds = new int[]{R.raw.countries_and_capitals, R.raw.nigeria_states_and_capticals, R.raw.proverbs_english, R.raw.proverbs_hausa, R.raw.proverbs_igbo, R.raw.proverbs_yoruba};
        return rawResourceIds;
    }

    public static int[] getRawResourceIds() {
        return new int[]{R.raw.countries_and_capitals, R.raw.iso2_iso3countrycodes, R.raw.nigeria_states_and_capticals, R.raw.proverbs_english, R.raw.proverbs_hausa, R.raw.proverbs_igbo, R.raw.proverbs_yoruba};
    }

    public static String getLabel(Context context, int rawResId) {
        if (rawResId == R.raw.countries_and_capitals) {
            return context.getString(R.string.msg_countries_and_capitals);
        }
        if (rawResId == R.raw.iso2_iso3countrycodes) {
            return context.getString(R.string.msg_iso2_to_iso3_countrycodes);
        }
        if (rawResId == R.raw.nigeria_states_and_capticals) {
            return context.getString(R.string.msg_nigerian_states_and_capitals);
        }
        if (rawResId == R.raw.proverbs_english) {
            return context.getString(R.string.msg_english_proverbs);
        }
        if (rawResId == R.raw.proverbs_hausa) {
            return context.getString(R.string.msg_hausa_proverbs);
        }
        if (rawResId == R.raw.proverbs_igbo) {
            return context.getString(R.string.msg_igbo_proverbs);
        }
        if (rawResId == R.raw.proverbs_yoruba) {
            return context.getString(R.string.msg_yoruba_proverbs);
        }
        throw new IllegalArgumentException();
    }
}
