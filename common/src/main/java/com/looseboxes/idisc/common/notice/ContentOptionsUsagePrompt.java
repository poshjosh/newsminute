package com.looseboxes.idisc.common.notice;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.util.Pref;

/**
 * Created by Josh on 9/12/2016.
 */
public class ContentOptionsUsagePrompt {

    private static boolean instanceOnDisplay;

    private final Context context;

    public ContentOptionsUsagePrompt(Context context) {
        this.context = context;
    }

    public boolean show() {
        final String dontShowAgainPrefKey = this.getClass().getName()+".dontShowAgain.boolean";
        boolean dontShowAgain = Pref.getBoolean(context, dontShowAgainPrefKey, false);
        boolean display = !instanceOnDisplay && !dontShowAgain;
        if(display) {
            try {

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

                AlertDialog.Builder bld = Popup.getInstance().getBuilder(
                        context, R.string.msg_tips, listener,
                        R.string.msg_ok, Popup.NO_RES, R.string.msg_dontshowagain);

                bld.setView(R.layout.contentoptions_usage);

                bld.create().show();

                instanceOnDisplay = true;

            } catch (Exception e) {
                Logx.getInstance().log(Popup.class, e);
            }
        }
        return display;
    }
}
