package com.looseboxes.idisc.common.io;

import android.content.Context;

import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.Logx;

import java.util.HashMap;
import java.util.Map;

public class Raw {
    public static Map get(Context context) {
        int[] rawResourceIds = getRawResourceIds();
        Map m = new HashMap(rawResourceIds.length, 1.0f);
        StreamReader sr = new StreamReader();
        for (int rawResourceId : rawResourceIds) {
            String contents = get(context, sr, rawResourceId);
            if (contents != null) {
                m.put(getLabel(context, rawResourceId), contents);
            }
        }
        return m;
    }

    public static String get(Context context, int rawResId) {
        return get(context, new StreamReader(), rawResId);
    }

    public static String get(Context context, StreamReader sr, int rawResId) {
        try {
            return sr.readContents(context.getResources().openRawResource(rawResId));
        } catch (Exception e) {
            Logx.log(Raw.class, e);
            return null;
        }
    }

    public static int[] getRawResourceIds() {
        return new int[]{R.raw.countries_and_capitals, R.raw.nigeria_states_and_capticals, R.raw.proverbs_english, R.raw.proverbs_hausa, R.raw.proverbs_igbo, R.raw.proverbs_yoruba};
    }

    public static String getLabel(Context context, int rawResId) {
        if (rawResId == R.raw.countries_and_capitals) {
            return "Countries and Capitals";
        }
        if (rawResId == R.raw.nigeria_states_and_capticals) {
            return "Nigerian States and Capitals";
        }
        if (rawResId == R.raw.proverbs_english) {
            return "English Proverbs";
        }
        if (rawResId == R.raw.proverbs_hausa) {
            return "Hausa Proverbs";
        }
        if (rawResId == R.raw.proverbs_igbo) {
            return "Igbo Proverbs";
        }
        if (rawResId == R.raw.proverbs_yoruba) {
            return "Yoruba Proverbs";
        }
        throw new IllegalArgumentException();
    }
}
