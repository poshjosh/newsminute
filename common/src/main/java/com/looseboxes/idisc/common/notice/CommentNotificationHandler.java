package com.looseboxes.idisc.common.notice;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Context;

import com.bc.android.core.notice.NotificationHandler;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.activities.DisplayCommentActivity;
import com.looseboxes.idisc.common.activities.DisplayCommentsActivity;
import com.looseboxes.idisc.common.jsonview.Notice;

import org.json.simple.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by Josh on 12/3/2016.
 */
public class CommentNotificationHandler extends NotificationHandlerImpl {

    public static final int COMMENT_NOTICE_ID = 2;

    public CommentNotificationHandler(Context context) {
        super(context);
    }

    @TargetApi(16)
    public boolean showCommentNotice(List<Map<String, Object>> notices) {
        if (!isCompatibleAndroidVersion()) {
            return false;
        }
        if (notices == null || notices.isEmpty()) {
            return false;
        }
        Notice noticeView = new Notice();
        try {
            String summary;
            int noticesCount = notices.size();
            String title = getTitle();
            if (noticesCount == 1) {
                noticeView.setJsonData((JSONObject)notices.get(0));
                summary = noticeView.getSummary(this.getContext());
            } else {
                summary = getGroupSummary(noticeView, notices);
            }
            Notification.Builder builder = getBuilder(this.getNoticeiconResourceid(), null, title, summary);
            if (noticesCount > 1) {
                Notification.InboxStyle inboxStyle = new Notification.InboxStyle();
                int i = 0;
                int repliesCount = 0;
                for (Map<String, Object> notice : notices) {
                    noticeView.setJsonData((JSONObject) notice);
                    int n = noticeView.getReplies().size();
                    summary = noticeView.getSummary(this.getContext());
                    inboxStyle.addLine(summary);
                    repliesCount += n;
                    i++;
                    if (i == 5) {
                        break;
                    }
                }
                inboxStyle.setSummaryText(getContext().getString(R.string.msg_d_repliestoyourcomment, new Object[]{Integer.valueOf(repliesCount)}));
                builder.setStyle(inboxStyle);
            }
            final Class targetActivity = noticesCount == 1 ? DisplayCommentActivity.class : DisplayCommentsActivity.class;
            return showNotification(COMMENT_NOTICE_ID, builder, targetActivity, null, null);
        } catch (Exception e) {
            Logx.getInstance().log(getClass(), e);
            return false;
        }
    }

    private String getGroupSummary(Notice noticeView, List<Map<String, Object>> notices) {
        int commentCount = notices.size();
        int totalReplies = 0;
        for (Map<String, Object> notice : notices) {
            noticeView.setJsonData((JSONObject)notice);
            totalReplies += noticeView.getReplies().size();
        }
        return getContext().getString(R.string.msg_d_repliesto_d_ofyourcomments, new Object[]{Integer.valueOf(totalReplies), Integer.valueOf(commentCount)});
    }
}
