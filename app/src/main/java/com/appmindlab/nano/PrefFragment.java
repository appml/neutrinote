package com.appmindlab.nano;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.widget.Toast;

import java.io.File;

/**
 * Created by saelim on 6/26/2015.
 */
public class PrefFragment extends PreferenceFragment {

    private SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(DBApplication.getAppContext());
    private SharedPreferences.Editor mSharedPreferencesEditor = mSharedPreferences.edit();
    private String mLocalRepoPath = mSharedPreferences.getString(Const.PREF_LOCAL_REPO_PATH, "");

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        // Setup local repo path picker
        Preference chooser = findPreference(Const.PREF_LOCAL_REPO_PATH);
        chooser.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (Build.VERSION.SDK_INT < 28) {
                    DirectoryChooserDialog directory_chooser =
                            new DirectoryChooserDialog(getActivity(),
                                    new DirectoryChooserDialog.ChosenDirectoryListener() {
                                        @Override
                                        public void onChosenDir(String path) {
                                            updateLocalRepoPath(path);
                                        }
                                    });

                    // Enable new folder button
                    directory_chooser.setNewFolderEnabled(true);

                    // Load directory chooser dialog for initial 'm_chosenDir' directory.
                    // The registered callback will be called upon final directory selection.
                    directory_chooser.chooseDirectory(mLocalRepoPath);
                }
                else
                    Toast.makeText(MainActivity.main_activity, mLocalRepoPath, Toast.LENGTH_SHORT).show();

                return true;
            }
        });
    }

    // Update local repo path
    protected void updateLocalRepoPath(String path) {
        try {
            // Write test
            File temp = File.createTempFile("delete", "me", new File(path));
            temp.delete();

            // Remove extra slashes
            path = Utils.cleanPath(path);

            mSharedPreferencesEditor.putString(Const.PREF_LOCAL_REPO_PATH, path);
            mSharedPreferencesEditor.commit();

            // Update main activity
            if (MainActivity.main_activity != null) {
                MainActivity.main_activity.setLocalRepoPath(path);
            }

            // Enable multi-file types by default for newly created repo
            File[] files = Utils.getFileListFromDirectory(getActivity(), new File(path), path);
            if (files.length == 0)
                Utils.writeLocalRepoFileAndTitle(getActivity(), Const.MULTI_TYPE, Const.NULL_SYM);

            // Unlock settings
            Utils.releaseWriteLock();

            Toast.makeText(getActivity(), path, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), getResources().getString(R.string.error_invalid_local_storage_path), Toast.LENGTH_SHORT).show();
        }
    }
}
