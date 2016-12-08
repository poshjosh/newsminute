package com.looseboxes.idisc.common.jsonview;

import android.util.Log;

import com.bc.android.core.util.Logx;

import org.json.simple.JSONObject;

import java.util.Date;
import java.util.Map;

public class Comment extends JsonView {

    public Comment() {
    }

    public Comment(JSONObject source) {
        super(source);
    }

    public String getText(String defaultValue) {
        String subj = getCommentSubject();
        if (subj == null) {
            return getCommentText();
        }
        StringBuilder builder = new StringBuilder();
        builder.append(subj);
        builder.append("\n---\n");
        builder.append(getCommentText());
        return builder.toString();
    }

    public String getOptions(String defaultValue, int maxLength) {
        String author;
        String dateStr = getDatecreated() == null ? null : getDatecreated().toString();
        if (!(dateStr == null || dateStr.isEmpty())) {
            dateStr = getDateDisplay(dateStr);
        }
        String BY = " by ";
        int otherLength = (dateStr == null ? 0 : dateStr.length()) + " by ".length();
        if (otherLength > maxLength) {
            author = "";
        } else {
            author = getAuthor();
            int required = maxLength - otherLength;
            if ((author == null ? 0 : author.length()) > required) {
                String trailing = "...";
                int tlen = "...".length();
                if (author == null || required <= tlen) {
                    author = "";
                } else {
                    author = author.substring(0, required - tlen) + "...";
                }
            }
        }
        StringBuilder builder = new StringBuilder();
        if (!(dateStr == null || dateStr.isEmpty())) {
            builder.append(dateStr);
        }
        if (!(author == null || author.isEmpty())) {
            builder.append(" by ").append(author);
        }
        return builder.toString();
    }

    public String getAuthor() {
        return getScreenName();
    }

    public String getScreenName() {
        Map installationData = this.getInstallationId();
        Logx.getInstance().log(Log.VERBOSE, getClass(), "Installation data: {0}", installationData);
        Object val = installationData.get(InstallationNames.screenname);
        return val == null ? null : val.toString();
    }

    public Object getCommentid() {
        return getJsonData().get(CommentNames.commentid);
    }

    public void setCommentid(Object commentid) {
        getJsonData().put(CommentNames.commentid, commentid);
    }

    public Object getRepliedto() {
        return getJsonData().get(CommentNames.repliedto);
    }

    public void setRepliedto(Object repliedto) {
        getJsonData().put(CommentNames.repliedto, repliedto);
    }

    public String getCommentSubject() {
        return (String) getJsonData().get(CommentNames.commentSubject);
    }

    public void setCommentSubject(String commentSubject) {
        getJsonData().put(CommentNames.commentSubject, commentSubject);
    }

    public String getCommentText() {
        return (String) getJsonData().get(CommentNames.commentText);
    }

    public void setCommentText(String commentText) {
        getJsonData().put(CommentNames.commentText, commentText);
    }

    public Object getDatecreated() {
        return getJsonData().get(FeedNames.datecreated);
    }

    public void setDatecreated(Date datecreated) {
        getJsonData().put(FeedNames.datecreated, datecreated);
    }

    public Object getFeedid() {
        return getJsonData().get(FeedhitNames.feedid);
    }

    public void setFeedid(Object feedid) {
        getJsonData().put(FeedhitNames.feedid, feedid);
    }

    public JSONObject getInstallationId() {
        return (JSONObject) getJsonData().get(InstallationNames.installationid);
    }

    public void setInstallationId(JSONObject installationid) {
        getJsonData().put(InstallationNames.installationid, installationid);
    }
}
