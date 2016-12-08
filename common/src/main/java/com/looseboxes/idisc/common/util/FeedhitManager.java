package com.looseboxes.idisc.common.util;

import android.content.Context;
import android.util.Log;

import com.bc.android.core.io.IOWrapper;
import com.bc.android.core.util.IntArrayLimitedSize;
import com.bc.android.core.util.Logx;
import com.bc.android.core.util.SparseIntArrayForLongsLimitedSize;
import com.bc.android.core.util.SparseIntArrayLimitedSize;
import com.bc.util.IntegerArray;
import com.looseboxes.idisc.common.App;
import com.looseboxes.idisc.common.asynctasks.Addfeedhit;
import com.looseboxes.idisc.common.asynctasks.Addfeedhits;

import org.json.simple.JSONValue;

import java.util.Map;
import java.util.Set;

public class FeedhitManager {

    private SparseIntArrayForLongsLimitedSize feedIdToHittime;
    private SparseIntArrayLimitedSize feedIdToHitcount;
    private IntArrayLimitedSize readFeedids;
    private IntArrayLimitedSize readNoticeFeedids;

    private Context context;

    private IOWrapper ioWrapper;

    public FeedhitManager(Context context) {
        this.context = context;
        this.ioWrapper = new IOWrapper<>(context, null);
        this.ioWrapper.setUseCache(false);
        feedIdToHittime = (SparseIntArrayForLongsLimitedSize) loadSparseIntArray(context, getFeedhitsName(), true);
        feedIdToHitcount = loadSparseIntArray(context, getHitcountsName(), false);
        final int initial = 5;
        readFeedids = loadIntArray(context, getReadFeedidsName(), initial,
                App.getMemoryLimitedInt(context, PropertiesManager.PropertyName.readFeedBufferSize, 10));
        readNoticeFeedids = loadIntArray(context, getReadNoticeFeedidsName(), initial,
                App.getMemoryLimitedInt(context, PropertiesManager.PropertyName.readNoticeBufferSize, 5));
    }

    public void destroy() {

        saveSparseIntArray(context, getFeedhitsName(), feedIdToHittime);
        feedIdToHittime.clear();
        feedIdToHittime = null;

        saveSparseIntArray(context, getHitcountsName(), feedIdToHitcount);
        feedIdToHitcount.clear();
        feedIdToHitcount = null;

        saveIntArray(context, getReadFeedidsName(), readFeedids);
        readFeedids.clear();
        readFeedids = null;

        saveIntArray(context, getReadNoticeFeedidsName(), readNoticeFeedids);
        readNoticeFeedids.clear();
        readNoticeFeedids = null;

        ioWrapper = null;
        context = null;
    }

    public boolean isRead(Long feedid) {
        return isReadFeed(feedid) || isReadNotice(feedid);
    }

    public boolean addReadFeed(Long feedid) {
        return this.add(this.readFeedids, feedid);
    }

    public boolean isReadFeed(Long feedid) {
        return this.contains(readFeedids, feedid);
    }

    public boolean addReadNotice(Long feedid) {
        return this.add(this.readNoticeFeedids, feedid);
    }

    public boolean isReadNotice(Long feedid) {
        return this.contains(this.readNoticeFeedids, feedid);
    }

    public boolean add(IntegerArray ia, Long feedid) {
        final int toAdd = feedid.intValue();
        if(!ia.contains(toAdd)) {
            ia.add(feedid.intValue());
            return true;
        }else{
            return false;
        }
    }

    public boolean contains(IntegerArray ia, Long feedid) {
        return ia.contains(feedid.intValue());
    }

    public void addFeedhit(final Long feedId) {
        if (NewsminuteUtil.isNetworkConnectedOrConnecting(context)) {
            if (feedIdToHittime.size() == 0) {
                Addfeedhit single = new Addfeedhit(context, feedId, System.currentTimeMillis()){
                    public void onSuccess(Object download) {
                        FeedhitManager.this.putHitcount(feedId, Integer.valueOf(download.toString()));
                        Logx.getInstance().log(Log.DEBUG, getClass(), "Successfully updated hitcount");
                    }

                };
                single.execute();
            }else {
                feedIdToHittime.put(feedId.intValue(), System.currentTimeMillis());
                Addfeedhits multiple = new Addfeedhits(context, feedIdToHittime){
                    public void onSuccess(Object download) {

                        FeedhitManager.this.clearFeedhits();

                        try {
                            Map map = (Map) JSONValue.parseWithException(download.toString());
                            FeedhitManager.this.putHitcounts(map);
                        } catch (Exception e) {
                            Logx.getInstance().log(getClass(), e);
                        }
                    }
                };
                multiple.execute();
            }
        }else {
            feedIdToHittime.put(feedId.intValue(), System.currentTimeMillis());
        }
        Logx.getInstance().debug(this.getClass(), "Feedhits: {0}\nHitcounts: {1}", feedIdToHittime, feedIdToHitcount);
    }

    public void clearFeedhits() {
        feedIdToHittime.clear();
        saveSparseIntArray(context, getFeedhitsName(), feedIdToHittime);
    }

    public void clearHitcounts() {
        feedIdToHitcount.clear();
        saveSparseIntArray(context, getHitcountsName(), feedIdToHitcount);
    }

    public void putHitcounts(Map feedidsToHitcounts) {
        Set keys = feedidsToHitcounts.keySet();
        for(Object key : keys) {
            Object val = feedidsToHitcounts.get(key);
            feedIdToHitcount.put(getInt(key), getInt(val));
        }
    }

    private int getInt(Object oval) {
        try{
            return ((Long)oval).intValue();
        }catch(ClassCastException e0) {
            try {
                return ((Integer) oval).intValue();
            }catch(ClassCastException e1) {
                return Integer.parseInt(oval.toString());
            }
        }
    }

    public void putHitcount(Long feedid, int hitcount) {
        feedIdToHitcount.put(feedid.intValue(), hitcount);
    }

    public int getHitcount(Long feedid, int valueIfNone) {
        return feedIdToHitcount.get(feedid.intValue(), valueIfNone);
    }

    private SparseIntArrayLimitedSize loadSparseIntArray(Context context, String name, boolean forLongs) {

        ioWrapper.setContext(context);
        ioWrapper.setFilename(name);

        SparseIntArrayLimitedSize output = (SparseIntArrayLimitedSize)ioWrapper.getTarget();

        if(output == null) {
            final int limit = App.getMemoryLimitedInt(context, PropertiesManager.PropertyName.feedhitBufferSize, 10);
            output = forLongs ?
                    new SparseIntArrayForLongsLimitedSize(5, limit) :
                    new SparseIntArrayLimitedSize(5, limit);
        }

        return output;
    }

    private void saveSparseIntArray(Context context, String name, SparseIntArrayLimitedSize toSave) {

        ioWrapper.setContext(context);
        ioWrapper.setFilename(name);

        if(toSave == null || toSave.size() < 1) {
            ioWrapper.setTarget(toSave);
        }else {
            ioWrapper.setTarget(null);
        }
    }

    private IntArrayLimitedSize loadIntArray(Context context, String name, int initial, int limit) {

        ioWrapper.setContext(context);
        ioWrapper.setFilename(name);

        IntArrayLimitedSize output = (IntArrayLimitedSize)ioWrapper.getTarget();

        if(output == null) {
            output = new IntArrayLimitedSize(initial, limit);
        }

        return output;
    }

    private void saveIntArray(Context context, String name, IntArrayLimitedSize toSave) {

        ioWrapper.setContext(context);
        ioWrapper.setFilename(name);

        if(toSave == null || toSave.size() < 1) {
            ioWrapper.setTarget(toSave);
        }else {
            ioWrapper.setTarget(null);
        }
    }

    private final String getFeedhitsName() {
        return "com.looseboxes.idisc.common.User.feedidToHittime.sparseintarray";
    }

    private final String getHitcountsName() {
        return "com.looseboxes.idisc.common.User.feedidToHitcount.sparseintarray";
    }

    private static String getReadFeedidsName() {
        return "com.looseboxes.idisc.common.User.readFeedids.intarray";
    }

    private static String getReadNoticeFeedidsName() {
        return "com.looseboxes.idisc.common.User.readNoticeFeedids.intarray";
    }
}
