package com.looseboxes.idisc.common.util;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;

public class AppRater {
    private final String appPackageName;

    /* renamed from: com.looseboxes.idisc.common.util.AppRater.1 */
    class AnonymousClass1 implements OnClickListener {
        final /* synthetic */ Dialog val$dialog;
        final /* synthetic */ Context val$mContext;

        AnonymousClass1(Context context, Dialog dialog) {
            this.val$mContext = context;
            this.val$dialog = dialog;
        }

        public void onClick(View v) {
            App.startPlayStoreActivity(this.val$mContext, AppRater.this.appPackageName);
            this.val$dialog.dismiss();
        }
    }

    /* renamed from: com.looseboxes.idisc.common.util.AppRater.2 */
    class AnonymousClass2 implements OnClickListener {
        final /* synthetic */ Dialog val$dialog;

        AnonymousClass2(Dialog dialog) {
            this.val$dialog = dialog;
        }

        public void onClick(View v) {
            this.val$dialog.dismiss();
        }
    }

    /* renamed from: com.looseboxes.idisc.common.util.AppRater.3 */
    class AnonymousClass3 implements OnClickListener {
        final /* synthetic */ Dialog val$dialog;
        final /* synthetic */ Editor val$editor;

        AnonymousClass3(Editor editor, Dialog dialog) {
            this.val$editor = editor;
            this.val$dialog = dialog;
        }

        public void onClick(View v) {
            if (this.val$editor != null) {
                this.val$editor.putBoolean("dontshowagain", true);
                this.val$editor.commit();
            }
            this.val$dialog.dismiss();
        }
    }

    public AppRater(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public float getDisplayFrequency() {
        return 0.33f;
    }

    public int getAppLaunchesUntilPrompt() {
        return 3;
    }

    public int getDaysUntilPrompt() {
        return 3;
    }

    public void onAppLaunch(Context mContext) {
        SharedPreferences prefs = mContext.getSharedPreferences(getClass().getName(), 0);
        if (!prefs.getBoolean("dontshowagain", false) && Math.random() >= ((double) getDisplayFrequency())) {
            Editor editor = prefs.edit();
            long launch_count = prefs.getLong("launch_count", 0) + 1;
            editor.putLong("launch_count", launch_count);
            Long date_firstLaunch = Long.valueOf(prefs.getLong("date_firstlaunch", 0));
            if (date_firstLaunch.longValue() == 0) {
                date_firstLaunch = Long.valueOf(System.currentTimeMillis());
                editor.putLong("date_firstlaunch", date_firstLaunch.longValue());
            }
            if (launch_count >= ((long) getAppLaunchesUntilPrompt()) && System.currentTimeMillis() >= date_firstLaunch.longValue() + ((long) ((((getDaysUntilPrompt() * 24) * 60) * 60) * 1000))) {
                showRateDialog(mContext, editor);
            }
            editor.commit();
        }
    }

    public void showRateDialog(Context mContext, Editor editor) {
        Dialog dialog = new Dialog(mContext);
        dialog.setTitle(getDialogTitle(mContext));
        LinearLayout ll = new LinearLayout(mContext);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setPadding(10, 10, 10, 10);
        TextView tv = new TextView(mContext);
        tv.setText(getDialogText(mContext));
        tv.setPadding(4, 0, 4, 10);
        ll.addView(tv);
        Button b1 = new Button(mContext);
        b1.setText(getOkButtonText(mContext));
        b1.setOnClickListener(new AnonymousClass1(mContext, dialog));
        ll.addView(b1);
        Button b2 = new Button(mContext);
        b2.setText(getLaterButtonText(mContext));
        b2.setOnClickListener(new AnonymousClass2(dialog));
        ll.addView(b2);
        Button b3 = new Button(mContext);
        b3.setText(getCancelButtonText(mContext));
        b3.setOnClickListener(new AnonymousClass3(editor, dialog));
        ll.addView(b3);
        dialog.setContentView(ll);
        dialog.show();
    }

    public String getDialogTitle(Context context) {
        String appLabel = App.getLabel(context);
        return context.getString(R.string.msg_rate_s, new Object[]{appLabel});
    }

    public String getDialogText(Context context) {
        String appLabel = App.getLabel(context);
        return context.getString(R.string.msg_rate_text_s, new Object[]{appLabel});
    }

    public String getOkButtonText(Context context) {
        return context.getString(R.string.msg_ok);
    }

    public String getLaterButtonText(Context context) {
        return context.getString(R.string.msg_later);
    }

    public String getCancelButtonText(Context context) {
        return context.getString(R.string.msg_never);
    }
}
