package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.jsonview.Comment;
import com.looseboxes.idisc.common.jsonview.InstallationNames;
import com.looseboxes.idisc.common.util.Logx;
import org.json.simple.JSONObject;

public class CommentListAdapter extends AbstractArrayAdapter {
    private final Comment output;

    public CommentListAdapter(Context context) {
        this(context, new Comment());
    }

    public CommentListAdapter(Context context, Comment output) {
        super(context);
        this.output = output;
        this.output.setDisplayDateAsTimeElapsed(true);
    }

    protected Long getId(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return (Long) this.output.getCommentid();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = super.getView(position, convertView, parent);
        TextView textView = (TextView) rowView.findViewById(R.id.listrow_text);
        try {
            if (App.getId(getContext()).equals(getOutput().getInstallationId().get(InstallationNames.installationkey).toString())) {
                textView.setTypeface(Typeface.DEFAULT, 3);
            }
        } catch (Exception e) {
            Logx.log(getClass(), e);
        }
        return rowView;
    }

    protected String getHeading(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getText(getContext().getString(R.string.err), getLen_short());
    }

    protected String getInfo(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getOptions(getContext().getString(R.string.err), getLen_xshort());
    }

    protected String getAuthor(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return this.output.getAuthor();
    }

    protected String getImageUrl(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return null;
    }

    protected String getUrl(JSONObject jsonData) {
        this.output.setJsonData(jsonData);
        return null;
    }

    public Comment getOutput() {
        return this.output;
    }
}
