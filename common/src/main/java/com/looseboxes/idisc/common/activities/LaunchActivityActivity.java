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
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import java.util.Arrays;
import java.util.List;

public class LaunchActivityActivity extends Activity {
    private Button okButton;
    private Class<? extends Activity> selectedActivityClass;
    private Button skipButton;

    /* renamed from: com.looseboxes.idisc.common.activities.LaunchActivityActivity.3 */
    class AnonymousClass3 implements OnItemSelectedListener {
        final /* synthetic */ Object[] val$values;

        AnonymousClass3(Object[] objArr) {
            this.val$values = objArr;
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
            try {
                LaunchActivityActivity.this.selectedActivityClass = (Class) this.val$values[position];
            } catch (Exception e) {
                Logx.log(getClass(), e);
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
                    Popup.alert(LaunchActivityActivity.this, "You did not select any activity to launch");
                } catch (Exception e) {
                    Logx.log(getClass(), e);
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

        Object[] values = new Object[]{DisplayLinkActivity.class, InfoActivity.class, WelcomeActivity.class, WelcomeOptionsActivity.class, LoginActivity.class, SignupActivity.class, ForgotPasswordActivity.class};

        String[] entries = new String[values.length];

        int i = 0;
        for(Object value:values) {
            entries[i++] = value.toString();
        }

        initSpinner(spinner, entries);

        spinner.setOnItemSelectedListener(getLogTagSpinnerListener(values));
    }

    private void initSpinner(Spinner spinner, String[] entries) {

        List list = Arrays.asList(entries);

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, list
        );

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
    }

    private OnItemSelectedListener getLogTagSpinnerListener(Object[] values) {
        return new AnonymousClass3(values);
    }
}
