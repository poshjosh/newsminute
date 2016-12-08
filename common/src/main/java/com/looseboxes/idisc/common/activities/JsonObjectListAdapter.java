package com.looseboxes.idisc.common.activities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bc.android.core.util.Util;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.R;
import com.bc.android.core.io.image.PicassoImageManager;
import com.bc.android.core.io.image.PicassoImageManagerImpl;
import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.util.PropertiesManager;
import com.looseboxes.idisc.common.util.PropertiesManager.PropertyName;
import com.looseboxes.idisc.common.util.NewsminuteUtil;
import com.squareup.picasso.RequestCreator;

import org.json.simple.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonObjectListAdapter extends ArrayAdapter<JSONObject> {

    private final int len_short;
    private final int len_xshort;
    private final int feedListIconHeight;
    private final int feedListIconWidth;
    private final List<JSONObject> feedDisplayArray;

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

    public JsonObjectListAdapter(Context context) {
        this(context, new ArrayList());
    }

    public JsonObjectListAdapter(Context context, List<JSONObject> arr) {
        super(context, R.layout.listrow, R.id.listrow_text, arr);
        this.feedDisplayArray = arr;
        Resources res = context.getResources();
        float widthDp = res.getDimension(R.dimen.iconImageWidth);
        float heightDp = res.getDimension(R.dimen.iconImageHeight);
        this.feedListIconWidth = (int) NewsminuteUtil.dipToPixels(context, widthDp);
        this.feedListIconHeight = (int) NewsminuteUtil.dipToPixels(context, heightDp);
        Logx.getInstance().debug(getClass(), "Width. dp: {0}, px: {1}", Float.valueOf(widthDp), Integer.valueOf(this.feedListIconWidth));
        PropertiesManager pm = App.getPropertiesManager(context);
        this.len_xshort = pm.getInt(PropertyName.textLengthXShort);
        this.len_short = pm.getInt(PropertyName.textLengthShort);
        imageViewPlaceholder = Util.getDrawable(this.getContext(), R.drawable.placeholder);
    }

    @Override
    public long getItemId(int position) {
        final JSONObject itemAtPosition = super.getItem(position);
        Long feedId = itemAtPosition == null ? -1L : getId(itemAtPosition);
        return feedId == null ? -1L : feedId;
    }

    @Override
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
            PicassoImageManager im = getImageManager();
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

    private PicassoImageManager _t_accessViaGetter;
    private PicassoImageManager getImageManager() {
        if (this._t_accessViaGetter == null) {
            this._t_accessViaGetter = new PicassoImageManagerImpl(getContext());
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
                    Logx.getInstance().log(getClass(), e);
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

    public int getInfoLength() {
        return this.len_xshort;
    }

    public int getHeadingLength() {
        return this.len_short;
    }

    public List<JSONObject> getFeedDisplayArray() {
        return this.feedDisplayArray;
    }
}
