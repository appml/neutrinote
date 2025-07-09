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

public class DirectShareHelper {

    public static void updateRecentNoteShortcut(Context context, Long id, String title) {
        List<ShortcutInfoCompat> shortcuts = new ArrayList<>();
        Set<String> categories = new HashSet<>();

        Intent intent = new Intent(context, DisplayDBEntry.class);
        intent.setAction(Const.ACTION_VIEW_ENTRY);
        intent.putExtra(Const.EXTRA_ID, id);  // Pass note ID to open the correct note

        categories.add("android.intent.category.DEFAULT");

        ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(context, Const.DYNAMIC_SHORTCUT_PREFIX + id)
                .setShortLabel(title.length() > 20 ? title.substring(0, 20) : title)  // Short label
                .setIcon(IconCompat.createWithResource(context, R.drawable.ic_launcher))
                .setIntent(intent)
                .setCategories(categories)
                .build();

        shortcuts.add(shortcut);
        ShortcutManagerCompat.addDynamicShortcuts(context, shortcuts);
    }
}