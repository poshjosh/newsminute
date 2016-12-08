package com.looseboxes.idisc.common.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import com.looseboxes.idisc.common.R;
import com.bc.android.core.notice.Popup;
import com.bc.android.core.util.Logx;

import java.util.Arrays;
import java.util.List;

public class LaunchActivityActivity extends Activity {
    private Button okButton;
    private Class<? extends Activity> selectedActivityClass;
    private Button skipButton;

    class ActivityListSpinnerListener implements OnItemSelectedListener {
        final Object[] val$values;

        ActivityListSpinnerListener(Object[] objArr) {
            this.val$values = objArr;
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            try {
                LaunchActivityActivity.this.selectedActivityClass = (Class) this.val$values[position];
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
        initLogTagSpinner((Spinner) findViewById(R.id.selectloglevel_logtagspinner));
        this.okButton = (Button) findViewById(R.id.selectloglevel_okbutton);
        this.okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    if (LaunchActivityActivity.this.selectedActivityClass != null) {
                        LaunchActivityActivity.this.startActivity(new Intent(LaunchActivityActivity.this, LaunchActivityActivity.this.selectedActivityClass));
                        return;
                    }
                    Popup.getInstance().alert(LaunchActivityActivity.this,
                            "You did not select any activity to launch", null,
                            LaunchActivityActivity.this.getString(R.string.msg_ok));
                } catch (Exception e) {
                    Logx.getInstance().log(getClass(), e);
                }
            }
        });
        this.skipButton = (Button) findViewById(R.id.selectloglevel_skipbutton);
        this.skipButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                LaunchActivityActivity.this.finish();
            }
        });
    }

    private void initLogTagSpinner(Spinner spinner) {


        Object[] values = new Object[]{SumbitnewsActivity.class, UserprofileActivity.class, DisplayCommentActivity.class, DisplayCommentsActivity.class, DisplayLinkActivity.class, InfoActivity.class, WelcomeActivity.class, WelcomeOptionsActivity.class, LoginActivity.class, SignupActivity.class, ForgotPasswordActivity.class};

        String[] entries = new String[values.length];

        int i = 0;
        for(Object value:values) {
            entries[i++] = ((Class)value).getSimpleName();
        }

        initSpinner(spinner, entries);

        spinner.setOnItemSelectedListener(new ActivityListSpinnerListener(values));
    }

    private void initSpinner(Spinner spinner, String[] entries) {

        List list = Arrays.asList(entries);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, list
        );

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
    }
}
