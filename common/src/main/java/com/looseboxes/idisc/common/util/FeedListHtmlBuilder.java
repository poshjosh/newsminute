package com.looseboxes.idisc.common.util;

import android.content.Context;
import android.net.Uri;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.jsonview.Feed;

import org.json.simple.JSONObject;

import java.util.Collection;

/**
 * Created by poshjosh on 5/7/2016.
 */
public class FeedListHtmlBuilder {

    private final Feed feed;

    private final Context context;

    public FeedListHtmlBuilder(Context context) {
        this.feed = new Feed();
        this.context = context;
    }

    public void buildHtml(Collection<JSONObject> data, StringBuilder builder) {
        builder.append("<html><head><title>Auto Generated</title></head><body>");
        this.build(data, builder);
        builder.append("</body></html>");
    }

    public void build(Collection<JSONObject> data, StringBuilder builder) {
        builder.append("<table>");
        for(JSONObject json:data) {
            this.buildRow(json, builder);
        }
        builder.append("</table>");
    }

    public void buildRow(JSONObject data, StringBuilder builder) {
        feed.setJsonData(data);
        builder.append("<tr>");
        for(int i=0; i<2; i++) {
            this.buildCell(data, builder, i);
        }
        builder.append("</tr>");
    }

    public void buildCell(JSONObject data, StringBuilder builder, int pos) {
        feed.setJsonData(data);
        String style = this.getCellStyle(pos);
        if(style == null) {
            builder.append("<td>");
        }else{
            builder.append("<td style=\""+style+"\">");
        }
        this.appendCellContent(builder, pos);
        builder.append("</td>");
    }

    public String getCellStyle(int pos) {
        switch(pos) {
            case 0:
                return ("style=\"width:72px; height:72px;\"");
            case 1:
                return ("style=\"height:72px; font-size:20px\"");
            default:
                throw new UnsupportedOperationException("Cell position: "+pos);
        }
    }

    private void appendCellContent(StringBuilder builder, int pos) {
        builder.append("<a style=\"text-decoration:none;\" href=\"").append(feed.getLocalUrl()).append("\">");
        switch(pos) {
            case 0:
                String imageSrc = feed.getImageUrl();
                if(imageSrc == null) {
                    imageSrc = feed.getSiteIconurl();
                    if(imageSrc == null) {
                        try {
                            Uri uri = NewsminuteUtil.getUriToResource(context, R.drawable.placeholder);
                            imageSrc = uri.toString();
                            Logx.getInstance().debug(this.getClass(), "Image src: {0}", imageSrc);
                        }catch(Exception e) {
                            Logx.getInstance().log(this.getClass(), e);
                        }
                    }
                }
                builder.append("<img width=\"64\" height=\"64\" src=\"").append(imageSrc).append("\"/>");
                break;
            case 1:
                builder.append(feed.getHeading("No title"));
                break;
            default:
                throw new UnsupportedOperationException("Cell position: "+pos);
        }
        builder.append("</a>");
    }
}
/**
 *
 public int getImageWidthPx() {
 return 40;
 }
 public int getImageHeightPx() {
 return 40;
 }
 public int getCellWidthPx() {
 return 44;
 }
 public int getCellHeightPx() {
 return 44;
 }

 *
 *
*/
