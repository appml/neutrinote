package com.appmindlab.nano;

import android.content.Context;
import android.content.Intent;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DynamicShortcutHelper {

    public static void updateDynamicShortcut(Context context, Long id, String title, String shortcutId) {
        List<ShortcutInfoCompat> shortcuts = new ArrayList<>();
        Set<String> categories = new HashSet<>();

        // Category for dynamic shortcut
        categories.add(Const.DYNAMIC_SHORTCUT_CATEGORY);

        // View note intent
        Intent intent = new Intent(context, DisplayDBEntry.class);
        intent.setAction(Const.ACTION_VIEW_ENTRY);
        intent.putExtra(Const.EXTRA_ID, id);

        // Build the shortcut
        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(context, shortcutId)
                .setShortLabel(title.length() > Const.DYNAMIC_SHORTCUT_LABEL_LEN ? title.substring(0, Const.DYNAMIC_SHORTCUT_LABEL_LEN) + "..." : title)
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher))
                .setIntent(intent)
                .setCategories(categories)
                .setLongLived(true)
                .build();

        shortcuts.add(shortcut);

        // Add shortcut if id does not exist, replace otherwise
        ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts);
    }
}
