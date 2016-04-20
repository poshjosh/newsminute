package com.looseboxes.idisc.common.util;

import android.content.Context;
import com.looseboxes.idisc.common.asynctasks.Addfeedhit;
import com.looseboxes.idisc.common.asynctasks.Addfeedhits;
import java.util.List;
import java.util.Map;

public class FeedhitManager {
    private static Map<Long, Integer> _hc;
    private static List<String> _hits;

    public static void addFeedhit(Context context, Long feedId) {
        if (Util.isNetworkConnectedOrConnecting(context)) {
            List<String> cachedHits = getCachedHits(context, true);
            if (cachedHits == null || cachedHits.isEmpty()) {
                Addfeedhit hitupdater = new Addfeedhit(context);
                hitupdater.setFeedid(feedId);
                hitupdater.setHittime(System.currentTimeMillis());
                hitupdater.execute();
                return;
            }
            cachedHits.add(toCacheFormat(feedId, System.currentTimeMillis()));
            Addfeedhits hitupdater2 = new Addfeedhits(context);
            hitupdater2.setHits(cachedHits);
            hitupdater2.execute();
            return;
        }
        addToCache(context, feedId, System.currentTimeMillis());
    }

    public static int getHitcount(Context context, Long feedid) {
        Integer hc;
        if (feedid != null) {
            Map<Long, Integer> map = getHitcounts(context, false);
            hc = (map == null || map.isEmpty()) ? null : (Integer) map.get(feedid);
        } else {
            hc = null;
        }
        return hc == null ? 0 : hc.intValue();
    }

    public static String toCacheFormat(Long feedid, long hittime) {
        return Long.toString(feedid.longValue()) + "," + hittime;
    }

    public static boolean addToCache(Context context, Long feedid, long hittime) {
        return getCachedHits(context, true).add(toCacheFormat(feedid, hittime));
    }

    public static List<String> getCachedHits(Context context, boolean create) {
        if (_hits == null && create) {
            _hits = new CachedList(context, Addfeedhit.class.getName() + ".hits.list");
        }
        return _hits;
    }

    public static Map<Long, Integer> getHitcounts(Context context, boolean create) {
        if (_hc == null && create && context != null) {
            _hc = new CachedMap(context, Addfeedhit.class.getName() + ".hitcounts.map");
        }
        return _hc;
    }
}
