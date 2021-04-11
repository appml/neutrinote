package com.appmindlab.nano;

import androidx.collection.LruCache;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by saelim on 8/16/2016.
 */
public class LocalReplaceHistory {
    private static int mSize = Const.LOCAL_REPLACE_CACHE_SIZE;

    private static LruCache mCache = new LruCache(mSize) {
        protected int sizeOf(String key, String value) {
            return value.length();
        }};

    // Get all values
    protected static String[] getAllValues() {
        ArrayList<String> values = new ArrayList<String>();

        Map<String, String> snapshot = mCache.snapshot();
        for (String key : snapshot.keySet()) {
            values.add(mCache.get(key).toString());
        }

        return values.toArray(new String[values.size()]);
    }

    // Add a new entry
    protected static void add(String key, String value) {
        synchronized (mCache) {
            if (mCache.get(key) == null) {
                mCache.put(key, value);
            }}
    }

    // Clear search history
    protected static void clear() {
        mCache.evictAll();
    }
}
