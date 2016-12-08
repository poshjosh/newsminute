package com.looseboxes.idisc.common.io;

import android.content.Context;

import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds;
import com.looseboxes.idisc.common.preferencefeed.Preferencefeeds.PreferenceType;
import com.looseboxes.idisc.common.util.AliasesManager.AliasType;

import java.io.File;
import java.util.Arrays;

public class FileIO extends com.bc.android.core.io.FileIO {

    public static File getExternalPublicFile(Context context, String fname) {
        return getExternalPublicFile(context, fname, true);
    }

    public static File getExternalPublicFile(Context context, String fname, boolean nomedia) {
        return getExternalPublicFile(context, getExternalPublicDirName(), fname, nomedia);
    }

    public static File getExternalPublicDir(boolean nomedia) {
        return getExternalPublicStorageDir(getExternalPublicDirName(), nomedia);
    }

    public static String getExternalPublicDirName() {
        return "donot_delete_newsminte";
    }

    public static String getLastFeedNotificationTimeFilename() {
        return "com.looseboxes.idisc.common.FeedLoadService.lastNotificationTime.long";
    }

    public static String getEmailsFilename() {
        return "com.looseboxes.idisc.common.User.emails.list";
    }

    public static String getUserDetailsFilename() {
        return "com.looseboxes.idisc.common.User.details.map";
    }

    public static String getInstallationIdFilename() {
        return "com.looseboxes.idisc.common.app_installationid.string";
    }

    public static String getLastInstallationTimeFilename() {
        return "com.looseboxes.idisc.common.app_last_installationtime.long";
    }

    public static String getFirstInstallationTimeFilename() {
        return "com.looseboxes.idisc.common.app_first_installationtime.long";
    }

    public static String getFeedsFilename() {
        return "com.looseboxes.idisc.common." + getFeedskey() + ".json";
    }

    public static String getCommentNotificationsFilename() {
        return "com.looseboxes.idisc.common.commentNotifications.json";
    }

    public static String getFeedsFilename(PreferenceType preferenceType) {
        switch (preferenceType) {
            case bookmarks:
                return "com.looseboxes.idisc.common.bookmark" + getFeedskey() + ".json";
            case favorites:
                return "com.looseboxes.idisc.common.favorite" + getFeedskey() + ".json";
            default:
                throw new IllegalArgumentException("Expected any of: " + Arrays.toString(PreferenceType.values()) + ", found: " + preferenceType);
        }
    }

    public static String getSearchfeedskey() {
        return "selectfeeds";
    }

    public static String getFeedskey() {
        return "feeds";
    }

    public static String getSyncFeedidskey(PreferenceType preferenceType) {
        switch (preferenceType) {
            case bookmarks:
                return "syncbookmarks";
            case favorites:
                return "syncfavorites";
            default:
                throw new IllegalArgumentException("Expected any of: " + Arrays.toString(PreferenceType.values()) + ", found: " + preferenceType);
        }
    }

    public static String getFeedskey(PreferenceType preferenceType) {
        switch (preferenceType) {
            case bookmarks:
                return "getbookmarkfeeds";
            case favorites:
                return "getfavoritefeeds";
            default:
                throw new IllegalArgumentException("Expected any of: " + Arrays.toString(PreferenceType.values()) + ", found: " + preferenceType);
        }
    }

    public static String getCommentskey() {
        return "comments";
    }

    public static String getPreferenceFeedidsFilename(PreferenceType prefType) {
        return "com.looseboxes.idisc.common.userpreferences." + prefType.name().toLowerCase() + ".feedids.json";
    }

    public static String getUserFeedPreferenceKey(String prefix, PreferenceType prefType) {
        Preferencefeeds.validatePrefix(prefix);
        switch (prefType) {
            case bookmarks:
                return prefix.toLowerCase() + "bookmarkfeedids";
            case favorites:
                return prefix.toLowerCase() + "favoritefeedids";
            default:
                throw new IllegalArgumentException("Expected any of: " + Arrays.toString(PreferenceType.values()) + ", found: " + prefType);
        }
    }

    public static String getAliasesKey(AliasType aliasesAliasType) {
        return "aliasesfor" + aliasesAliasType.name().toLowerCase();
    }

    public static String getAppPropertiesKey() {
        return "appproperties";
    }
}
