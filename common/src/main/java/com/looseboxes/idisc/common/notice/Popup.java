package com.looseboxes.idisc.common.notice;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.Pref;

public class Popup {

    public static void alert(Context context, String message, OnClickListener okListener) {
        try {
            Builder bld = new Builder(new ContextThemeWrapper(context, R.style.DialogAppTheme));
            bld.setMessage((CharSequence) message);
            bld.setPositiveButton((CharSequence) "Yes", okListener);
            bld.setNegativeButton((CharSequence) "No", null);
            bld.create().show();
        } catch (Exception e) {
            Logx.log(Popup.class, e);
        }
    }

    private static boolean instanceOnDisplay;
    public static boolean promptContentoptionsUsage(final Context context) {
        final String dontShowAgainPrefKey = Popup.class.getName()+".ContentoptionsUsage.Dontshowagain.boolean";
        boolean dontShowAgain = Pref.getBoolean(context, dontShowAgainPrefKey, false);
        boolean display = !instanceOnDisplay && !dontShowAgain;
        if(display) {
            try {
                Builder bld = new Builder(new ContextThemeWrapper(context, R.style.DialogAppTheme));

                bld.setView(R.layout.contentoptions_usage);

                Dialog.OnClickListener listener = new Dialog.OnClickListener(){
                    /**
                     * This method will be invoked when a button in the dialog is clicked.
                     * @param dialog The dialog that received the click.
                     * @param which  The button that was clicked (e.g.
                     *               {@link DialogInterface#BUTTON1}) or the position
                     */
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch(which) {
                            case Dialog.BUTTON_POSITIVE:
                                break;
                            case Dialog.BUTTON_NEGATIVE:
                                Pref.setBoolean(context, dontShowAgainPrefKey, true);
                                break;
                            default:
                        }
                        instanceOnDisplay = false;
                        dialog.dismiss();
                    }
                };
                bld.setMessage("Tips");
                bld.setPositiveButton("OK", listener);
                bld.setNegativeButton("Don't show again", listener);
                bld.create().show();
                instanceOnDisplay = true;
            } catch (Exception e) {
                Logx.log(Popup.class, e);
            }
        }
        return display;
    }

    public static void alert(Context context, String message) {
        try {
            Builder bld = new Builder(new ContextThemeWrapper(context, R.style.DialogAppTheme));
            bld.setMessage((CharSequence) message);
            bld.setNeutralButton((CharSequence) "OK", null);
            bld.create().show();
        } catch (Exception e) {
            Logx.log(Popup.class, e);
        }
    }

    public static void show(View v, int msg_resource, int length) {
        if (v != null) {
            show(v, v.getContext().getString(msg_resource), length);
        }
    }

    public static void show(View v, Object msg, int length) {
        if (v != null) {
            show(v.getContext(), msg == null ? "null" : msg.toString(), length);
        }
    }

    public static void show(Context context, int msg_resource, int length) {
        show(context, context.getString(msg_resource), length);
    }

    public static void show(Context context, Object msg, int length) {
        show(LayoutInflater.from(context), null, context, msg, length);
    }

    public static void show(Activity a, int msg_resource, int length) {
        show(a, a.getApplicationContext().getString(msg_resource), length);
    }

    public static void show(Activity a, Object msg, int length) {
        show(a.getLayoutInflater(), (ViewGroup) a.findViewById(R.id.toast_layout_root), a.getApplicationContext(), msg, length);
    }

    private static void show(LayoutInflater inflater, ViewGroup root, Context context, Object msg, int length) {
        try {
            View layout = inflater.inflate(R.layout.toast_layout, root);
            ((TextView) layout.findViewById(R.id.toast_layout_text)).setText(msg == null ? "null" : msg.toString());
            Toast toast = new Toast(context);
            toast.setDuration(length);
            toast.setView(layout);
            toast.show();
        } catch (Exception e) {
            Logx.log(Popup.class, e);
        }
    }
}
