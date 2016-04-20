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
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.io.FileIO;
import com.looseboxes.idisc.common.notice.Popup;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ViewLogFilesActivity extends Activity {
    private Button okButton;
    private String selectedFilename;
    private Object selectedItem;
    private Spinner spinner;

    /* renamed from: com.looseboxes.idisc.common.activities.ViewLogFilesActivity.1 */
    class AnonymousClass1 implements OnItemSelectedListener {
        final /* synthetic */ String[] val$entries;
        final /* synthetic */ String[] val$values;

        AnonymousClass1(String[] strArr, String[] strArr2) {
            this.val$entries = strArr;
            this.val$values = strArr2;
        }

        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            try {
                Object item = parent.getItemAtPosition(position);
                for (int i = 0; i < this.val$entries.length; i++) {
                    if (this.val$entries[i].equals(item.toString())) {
                        Logx.debug(getClass(), "Found selection:\n" + this.val$entries[i]);
                        ViewLogFilesActivity.this.selectedFilename = this.val$values[i];
                        ViewLogFilesActivity.this.selectedItem = item;
                        return;
                    }
                }
            } catch (Exception e) {
                Logx.log(getClass(), e);
            }
        }

        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        Logx.log(Log.VERBOSE, getClass(), "#onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewlogfiles);
        this.spinner = (Spinner) findViewById(R.id.viewlogfiles_spinner);
        List<File> files = getFiles();
        String[] entries = getEntries(files);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter(
                this, android.R.layout.simple_spinner_item, new ArrayList(Arrays.asList(entries)));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.spinner.setAdapter(dataAdapter);
        String[] values = getValues(files);
        String str = (values == null || values.length == 0) ? null : values[0];
        this.selectedFilename = str;
        this.spinner.setOnItemSelectedListener(new AnonymousClass1(entries, values));
        this.okButton = (Button) findViewById(R.id.viewlogfiles_okbutton);
        this.okButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    Logx.debug(getClass(), "Selected filename:\n" + ViewLogFilesActivity.this.selectedFilename);
                    if (ViewLogFilesActivity.this.selectedFilename != null) {
                        File file = Logx.getLogFile(ViewLogFilesActivity.this.selectedFilename);
                        if (file == null || !file.exists()) {
                            Popup.show(ViewLogFilesActivity.this, "Does not exist:\n" + ViewLogFilesActivity.this.selectedFilename, 1);
                            return;
                        }
                        String contents = FileIO.readFile(file);
                        if (contents == null || contents.isEmpty()) {
                            Popup.show(ViewLogFilesActivity.this, (Object) "Selected log file is empty", 1);
                        } else {
                            ((TextView) ((ScrollView) ViewLogFilesActivity.this.findViewById(R.id.viewlogfiles_scrollview)).getChildAt(0)).setText(contents);
                        }
                    }
                } catch (Exception e) {
                    Logx.log(getClass(), e);
                }
            }
        });
    }

    private java.util.List<java.io.File> getFiles() {
        int logFileCount = App.getPropertiesManager(this).getInt(PropertiesManager.PropertyName.logFileCount);
        List<File> files = new LinkedList<>(); // Linked list very important
        for(int i=0; i<logFileCount; i++) {
            File file = Logx.getLogFile(i);
            if(file == null || !file.exists()) {
                break;
            }
            files.add(file);
        }
        Logx.debug(this.getClass(), "Files: {0}", files);
        return files;
    }

    private String[] getEntries(List<File> files) {
        String[] entries = new String[files.size()];
        for (int i = 0; i < entries.length; i++) {
            entries[i] = ((File) files.get(i)).getName();
        }
        Logx.debug(getClass(), "Spinner entries: {0}", Arrays.toString(entries));
        return entries;
    }

    private String[] getValues(List<File> files) {
        String[] values = new String[files.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = ((File) files.get(i)).getName();
        }
        Logx.debug(getClass(), "Spinner values: {0}", Arrays.toString(values));
        return values;
    }
}
