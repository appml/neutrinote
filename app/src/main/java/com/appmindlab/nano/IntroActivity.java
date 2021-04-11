package com.appmindlab.nano;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;

import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by saelim on 9/10/2015.
 */
public class IntroActivity extends AppIntro {
    private SharedPreferences mSharedPreferences;
    private String mLocalRepoPath;

    @Override
    public void init(Bundle bundle) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mLocalRepoPath = mSharedPreferences.getString(Const.PREF_LOCAL_REPO_PATH, "");

        if (Build.VERSION.SDK_INT >= 23) {
            if ((mLocalRepoPath.length() > 0) || (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
                launchMainActivity();
        }
        else {
            if (mLocalRepoPath.length() > 0)
                launchMainActivity();
        }

        // Intro screens
        AppIntroFragment slide1 = AppIntroFragment.newInstance(getResources().getString(R.string.intro_getting_started), getResources().getString(R.string.intro_getting_started_desc), R.drawable.intro_notepad, Color.parseColor("#0277BD"));
        AppIntroFragment slide2 = AppIntroFragment.newInstance(getResources().getString(R.string.intro_local_repo), getResources().getString(R.string.intro_local_repo_desc), R.drawable.intro_brick, Color.parseColor("#0277BD"));

        addSlide(slide1);
        addSlide(slide2);
    }

    @Override
    public void onSkipPressed() {
        launchMainActivity();
    }

    @Override
    public void onDonePressed() {
        launchMainActivity();
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
