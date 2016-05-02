package com.looseboxes.idisc.common.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.io.image.ImageManager;
import com.looseboxes.idisc.common.io.image.PicassoImageManager;
import com.looseboxes.idisc.common.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.Util;
import com.squareup.picasso.RequestCreator;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;

public abstract class AbstractArrayAdapter extends ArrayAdapter<JSONObject> {
    private ImageManager _t_accessViaGetter;
    private final List<JSONObject> feedDisplayArray;
    private final int feedListIconHeight;
    private final int feedListIconWidth;
    private final int len_short;
    private final int len_xshort;

    private Drawable imageViewPlaceholder;

    static class ViewHolder {
        ImageView imageView;
        TextView infoView;
        TextView textView;

        ViewHolder() {
        }
    }

    protected abstract String getAuthor(JSONObject jSONObject);

    protected abstract String getHeading(JSONObject jSONObject);

    protected abstract Long getId(JSONObject jSONObject);

    protected abstract String getImageUrl(JSONObject jSONObject);

    protected abstract String getInfo(JSONObject jSONObject);

    protected abstract String getUrl(JSONObject jSONObject);

    @TargetApi(21)
    public AbstractArrayAdapter(Context context) {
        this(context, new ArrayList());
    }

    @TargetApi(21)
    public AbstractArrayAdapter(Context context, List<JSONObject> arr) {
        super(context, R.layout.listrow, R.id.listrow_text, arr);
        this.feedDisplayArray = arr;
        Resources res = context.getResources();
        float widthDp = res.getDimension(R.dimen.iconImageWidth);
        float heightDp = res.getDimension(R.dimen.iconImageHeight);
        this.feedListIconWidth = (int) Util.dipToPixels(context, widthDp);
        this.feedListIconHeight = (int) Util.dipToPixels(context, heightDp);
        Logx.debug(getClass(), "Width. dp: {0}, px: {1}", Float.valueOf(widthDp), Integer.valueOf(this.feedListIconWidth));
        PropertiesManager pm = App.getPropertiesManager(context);
        this.len_xshort = pm.getInt(PropertyName.textLengthXShort);
        this.len_short = pm.getInt(PropertyName.textLengthShort);
        try {
            if (App.isAcceptableVersion(context, 21)) {
                imageViewPlaceholder = context.getResources().getDrawable(R.drawable.placeholder, this.getContext().getTheme());
            } else {
                imageViewPlaceholder = context.getResources().getDrawable(R.drawable.placeholder);
            }
        }catch (Exception e) {
            Logx.log(this.getClass(), e);
        }
    }

    public long getItemId(int position) {
        Long feedId = getId((JSONObject) super.getItem(position));
        return feedId == null ? -1 : feedId.longValue();
    }

    @TargetApi(16)
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        String authorFavicon;

        final Context context = this.getContext();

        if (convertView == null) {
            convertView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listrow, parent, false);
            holder = new ViewHolder();
            holder.imageView = (ImageView) convertView.findViewById(R.id.listrow_icon);
            holder.textView = (TextView) convertView.findViewById(R.id.listrow_text);
            holder.infoView = (TextView) convertView.findViewById(R.id.listrow_options);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imageView.setMinimumWidth(this.feedListIconWidth);
        holder.imageView.setMinimumHeight(this.feedListIconHeight);

        JSONObject jsonData = (JSONObject) getItem(position);

        holder.textView.setText(getHeading(jsonData));
        holder.infoView.setText(getInfo(jsonData));

        if ("iganta".equalsIgnoreCase(getAuthor(jsonData))) {
            authorFavicon = getAuthorFile("favicon.ico", jsonData);
        } else {
            authorFavicon = null;
        }

        String imageUrl = getImageUrl(jsonData);

        if (holder.imageView != null) {

            holder.imageView.setImageDrawable(imageViewPlaceholder);

            RequestCreator rc = null;
            ImageManager im = getImageManager();
            if (!(authorFavicon == null || authorFavicon.isEmpty())) {
                rc = im.from(authorFavicon, this.feedListIconWidth, this.feedListIconHeight);
            }
            if (!(rc != null || imageUrl == null || imageUrl.isEmpty())) {
                rc = im.from(imageUrl, this.feedListIconWidth, this.feedListIconHeight);
            }
            if (rc != null) {
                rc.error(R.drawable.brokendownload).placeholder(R.drawable.placeholder).into(holder.imageView);
            }
        }
        return convertView;
    }

    private ImageManager getImageManager() {
        if (this._t_accessViaGetter == null) {
            this._t_accessViaGetter = new PicassoImageManager(getContext());
        }
        return this._t_accessViaGetter;
    }

    public String getAuthorFile(String fname, JSONObject jsonData) {
        if (getAuthor(jsonData) != null) {
            String urlStr = getUrl(jsonData);
            if (urlStr != null) {
                try {
                    URL url = new URL(urlStr);
                    return new URL(url.getProtocol(), url.getHost(), fname).toExternalForm();
                } catch (MalformedURLException e) {
                    Logx.log(getClass(), e);
                }
            }
        }
        return null;
    }

    public int getFeedListIconWidth() {
        return this.feedListIconWidth;
    }

    public int getFeedListIconHeight() {
        return this.feedListIconHeight;
    }

    public int getLen_xshort() {
        return this.len_xshort;
    }

    public int getLen_short() {
        return this.len_short;
    }

    public List<JSONObject> getFeedDisplayArray() {
        return this.feedDisplayArray;
    }
}
