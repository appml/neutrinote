package com.appmindlab.nano;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

/**
 * Created by saelim on 6/26/2015.
 */
public class SetPreferenceActivity extends AppCompatActivity {
    // Settings related
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    // Theme related
    private String mTheme = "day";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Setup preferences
        loadPref();

        // Setup theme
        setupTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new PrefFragment()).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Setup theme
    private void setupTheme() {
        // Determine the theme to use
        String mode = Const.NULL_SYM;

        if (mTheme.equals(Const.SYSTEM_THEME)) {
            int flags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (flags == Configuration.UI_MODE_NIGHT_YES) {
                mode = Const.NIGHT_THEME;
            }
            else {
                mode = Const.DAY_THEME;
            }
        }

        if ((mTheme.equals(Const.DAY_THEME)) || (mode.equals(Const.DAY_THEME))) {
            setTheme(R.style.AppSettingsThemeDay);
            Utils.setStatusBarColor(getWindow(), ContextCompat.getColor(this, R.color.colorPrimary));
        }
        else {
            setTheme(R.style.AppSettingsTheme);
        }
    }

    // Load preferences
    private void loadPref() {
        try {
            // Retrieve preference values
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mTheme = mSharedPreferences.getString(Const.PREF_THEME, Const.DEFAULT_THEME);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                // Implementation
                if (key.equals(Const.PREF_THEME)) {
                    mTheme = prefs.getString(key, Const.DAY_THEME);

                    // Recreate activity to reload theme
                    recreate();
                }
            }
        };

        mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }
}
