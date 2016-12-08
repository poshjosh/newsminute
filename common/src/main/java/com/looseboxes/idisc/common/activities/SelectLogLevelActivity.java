package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.asynctasks.FeedDownloadTask;
import com.looseboxes.idisc.common.handlers.FeedListDisplayHandler;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.jsonview.AuthuserNames;
import com.looseboxes.idisc.common.listeners.AbstractContentOptionsButtonListener;
import com.looseboxes.idisc.common.notice.FeedNotificationHandler;
import com.bc.android.core.notice.Popup;
import com.looseboxes.idisc.common.service.DownloadService;
import com.looseboxes.idisc.common.util.AliasesManager;
import com.bc.android.core.util.Logx;
import java.util.Arrays;

public class SelectLogLevelActivity extends Activity {
    private static final String LOG_TAG_NONE = "CLEAR LOG TAGS";
    private Button okButton;
    private int selectedLogPriority;
    private String selectedLogTag;
    private Button skipButton;

    /* renamed from: com.looseboxes.idisc.common.activities.SelectLogLevelActivity.3 */
    class AnonymousClass3 implements OnItemSelectedListener {
        final /* synthetic */ Object[] val$values;

        AnonymousClass3(Object[] objArr) {
            this.val$values = objArr;
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            try {
                SelectLogLevelActivity.this.selectedLogPriority = Integer.parseInt(this.val$values[position].toString());
            } catch (Exception e) {
                Logx.getInstance().log(getClass(), e);
            }
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    /* renamed from: com.looseboxes.idisc.common.activities.SelectLogLevelActivity.4 */
    class AnonymousClass4 implements OnItemSelectedListener {
        final /* synthetic */ Object[] val$values;

        AnonymousClass4(Object[] objArr) {
            this.val$values = objArr;
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            try {
                SelectLogLevelActivity.this.selectedLogTag = this.val$values[position].toString();
            } catch (Exception e) {
                Logx.getInstance().log(getClass(), e);
            }
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selectloglevel);
        initLogLevelSpinner((Spinner) findViewById(R.id.selectloglevel_loglevelspinner));
        initLogTagSpinner((Spinner) findViewById(R.id.selectloglevel_logtagspinner));
        this.okButton = (Button) findViewById(R.id.selectloglevel_okbutton);
        this.okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    if (SelectLogLevelActivity.this.selectedLogPriority != -1) {
                        Logx.getInstance().setLogPriority(SelectLogLevelActivity.this.selectedLogPriority);
                        Popup.getInstance().show(SelectLogLevelActivity.this, "Log priority updated to: " + Logx.getInstance().getLogPriorityString(SelectLogLevelActivity.this.selectedLogPriority), 0);
                    }
                    if (SelectLogLevelActivity.this.selectedLogTag == null) {
                        return;
                    }
                    if (SelectLogLevelActivity.this.selectedLogTag.equals(SelectLogLevelActivity.LOG_TAG_NONE)) {
                        Logx.getInstance().clearTagsToAccept();
                        return;
                    }
                    Logx.getInstance().addTagToAccept(SelectLogLevelActivity.this.selectedLogTag);
                    Popup.getInstance().show(SelectLogLevelActivity.this, "Added log tag to accept: " + SelectLogLevelActivity.this.selectedLogTag, 1);
                } catch (Exception e) {
                    Logx.getInstance().log(getClass(), e);
                }
            }
        });
        this.skipButton = (Button) findViewById(R.id.selectloglevel_skipbutton);
        this.skipButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                SelectLogLevelActivity.this.finish();
            }
        });
    }

    private void initLogTagSpinner(Spinner spinner) {
        Object[] values = new Object[]{LOG_TAG_NONE, FeedDownloadTask.class.getPackage().getName(), FeedListDisplayHandler.class.getPackage().getName(), FileIO.class.getPackage().getName(), AuthuserNames.class.getPackage().getName(), AbstractContentOptionsButtonListener.class.getPackage().getName(), FeedNotificationHandler.class.getPackage().getName(), DownloadService.class.getPackage().getName(), AliasesManager.class.getPackage().getName(), AboutUsActivity.class.getPackage().getName()};
        initSpinner(spinner, values, values);
        spinner.setOnItemSelectedListener(getLogTagSpinnerListener(values));
    }

    private void initLogLevelSpinner(Spinner spinner) {
        Integer[] values = new Integer[]{Log.VERBOSE, Log.DEBUG, Log.INFO, Log.WARN, Log.ERROR, Log.ASSERT};
        String[] entries = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            entries[i] = Logx.getInstance().getLogPriorityString(values[i].intValue());
        }
        initSpinner(spinner, entries, values);
        spinner.setOnItemSelectedListener(getLogLevelSpinnerListener(values));
    }

    private void initSpinner(Spinner spinner, Object[] entries, Object[] values) {
        ArrayAdapter<String> dataAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, Arrays.asList(entries));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
    }

    private OnItemSelectedListener getLogLevelSpinnerListener(Object[] values) {
        return new AnonymousClass3(values);
    }

    private OnItemSelectedListener getLogTagSpinnerListener(Object[] values) {
        return new AnonymousClass4(values);
    }
}
