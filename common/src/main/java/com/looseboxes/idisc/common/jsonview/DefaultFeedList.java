package com.looseboxes.idisc.common.jsonview;

import android.app.Activity;
import android.content.Context;

import com.bc.android.core.util.Logx;
import com.looseboxes.idisc.common.DefaultApplication;
import com.looseboxes.idisc.common.R;
import com.looseboxes.idisc.common.io.Raw;

import org.json.simple.JSONObject;

import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Josh on 8/15/2016.
 */
public final class DefaultFeedList extends AbstractList<JSONObject> {

    private final JSONObject [] elements;

    public DefaultFeedList(Activity activity) {

        this((DefaultApplication)activity.getApplication());
    }

    public DefaultFeedList(DefaultApplication app) {

        this(app, app.getSharedContext().getFeedidsService().getFeedids().getDefaultFeedidStart(),
                app.getSharedContext().getFeedidsService().getFeedids().getDefaultFeedidEnd());
    }

    public DefaultFeedList(Context context, long startId, long endId) {

        this(context, context.getString(R.string.app_label) + "-local-default-feed-list", startId, endId);
    }

    public DefaultFeedList(Context context, Object categories, long startId, long endId) {

        final Map map = Raw.get(context, Raw.getRawResourceIdsForDefaultFeedlist());

        final Set titles = map.keySet();

        final List<JSONObject> list = new LinkedList<>();

        long id = startId;

        for (Object title : titles) {

            if(id == endId) {
                break;
            }

            try {

                list.add(new DefaultJsonObject(context, id, title, map.get(title), categories));

                ++id;

            } catch (Exception e) {
                Logx.getInstance().log(this.getClass(), e);
            }
        }

        this.elements = list.toArray(new JSONObject[0]);
    }

    /**
     * Returns the element at the specified location in this list.
     *
     * @param location the index of the element to return.
     * @return the element at the specified index.
     * @throws IndexOutOfBoundsException if {@code location < 0 || location >= size()}
     */
    @Override
    public JSONObject get(int location) {
        return elements[location];
    }

    /**
     * Returns a count of how many objects this {@code Collection} contains.
     * <p/>
     * In this class this method is declared abstract and has to be implemented
     * by concrete {@code Collection} implementations.
     *
     * @return how many objects this {@code Collection} contains, or {@code Integer.MAX_VALUE}
     * if there are more than {@code Integer.MAX_VALUE} elements in this
     * {@code Collection}.
     */
    @Override
    public int size() {
        return elements.length;
    }
}
