package com.appmindlab.nano;

import android.content.Context;
import android.graphics.Typeface;

import java.io.File;
import java.util.HashMap;

/**
 * Created by saelim on 1/31/2016.
 */
public class FontCache {
    private static HashMap<String, Typeface> mCache = new HashMap<String, Typeface>();

    // Get from asset
    protected static Typeface getFromAsset(Context context, String ttf) {
        // Sanity check
        if (ttf.equals(Const.SYSTEM_FONT_FILE))
            return null;

        if (mCache == null)
            mCache = new HashMap<String, Typeface>();

        Typeface typeface = mCache.get(ttf);
        if (typeface == null) {
            typeface = Typeface.createFromAsset(context.getAssets(), ttf);
            mCache.put(ttf, typeface);
        }

        return typeface;
    }

    // Get from file
    protected static Typeface getFromFile(String path) {
        // Sanity check
        if (path.equals(Const.SYSTEM_FONT_PATH))
            return null;

        if (mCache == null)
            mCache = new HashMap<String, Typeface>();

        Typeface typeface = mCache.get(path);
        if (typeface == null) {
            File file = new File(path);
            if ((file.exists()) && (file.length() > 0))
                typeface = Typeface.createFromFile(file);
            else
                typeface = Typeface.SANS_SERIF;

            mCache.put(path, typeface);
        }

        return typeface;
    }

    // Clear cache
    protected static void clear() {
        mCache.clear();
    }
}
