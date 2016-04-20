package com.looseboxes.idisc.common.asynctasks;

import com.bc.util.JsonFormat;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.User;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager;
import com.looseboxes.idisc.common.util.PreferenceFeedsManager.PreferenceType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class UpdateUserFeedPreferences extends DefaultReadTask<Object> {
    private final PreferenceType preferenceType;
    private final String prefix;
    private final List updateValues;

    public UpdateUserFeedPreferences(PreferenceType preferenceType, String prefix, List userFeedPrefs) {
        this.preferenceType = preferenceType;
        this.prefix = prefix;
        this.updateValues = userFeedPrefs;
        Map<String, String> params = new HashMap(userFeedPrefs.size() + 15, 0.75f);
        params.put(getRequestParameterName(), new JsonFormat().toJSONString(userFeedPrefs));
        User.getInstance().addParameters(getContext(), params);
        setOutputParameters(params);
    }

    private String getRequestParameterName() {
        PreferenceFeedsManager.validatePrefix(this.prefix);
        String output;
        switch(preferenceType) {
// DO NOT CHANGE THESE WITHOUT CHANGING THE SERVER CODE... as this format is expected by the server
            case bookmarks:
                output = "com.looseboxes.idisc."+prefix.toLowerCase()+"bookmarkfeedids.feedids"; break;
            case favorites:
                output = "com.looseboxes.idisc."+prefix.toLowerCase()+"favoritefeedids.feedids"; break;
            default:
                throw new IllegalArgumentException("Expected any of: "+
                        Arrays.toString(PreferenceFeedsManager.PreferenceType.values())+", found: "+preferenceType);
        }
        return output;
    }

    public String getOutputKey() {
        return FileIO.getUserFeedPreferenceKey(this.prefix, this.preferenceType);
    }

    public boolean isRemote() {
        return true;
    }

    public String getErrorMessage() {
        return getContext().getString(R.string.err_loading_userpreferences);
    }

    public String getLocalFilename() {
        throw new UnsupportedOperationException("Not supported.");
    }

    public PreferenceType getPreferenceType() {
        return this.preferenceType;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public List getUpdateValues() {
        return this.updateValues;
    }
}
