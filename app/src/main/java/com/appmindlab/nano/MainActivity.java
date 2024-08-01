package com.appmindlab.nano;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.app.backup.BackupManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;
import androidx.core.content.ContextCompat;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.core.view.GravityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputLayout;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.appmindlab.nano.R.id.fab;
import static com.appmindlab.nano.Utils.evalGlobalVariables;
import static com.appmindlab.nano.Utils.getSystemDateFormat;

@Keep
public class MainActivity extends AppCompatActivity implements View.OnClickListener, PopupMenu.OnMenuItemClickListener, NavigationView.OnNavigationItemSelectedListener {

    // Main application
    private DBApplication mApp;

    // Toolbar
    private Toolbar mToolbar;
    private int mToolBarSelectedItemId = -1;

    // Swipe Refresh
    private SwipeRefreshLayout mSwipeRefreshLayout;

    // Recycler view
    private FastScrollRecyclerView mRecyclerView;
    private CustomAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private Cursor mCursor;
    private ItemTouchHelper mItemTouchHelper;
    private TextView mEmptyView;

    // FAB
    private FloatingActionButton mFab;

    // Navigation drawer
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;
    private int mCurrentSelectedPosition = 0;

    // SQLite related
    private DataSource mDatasource;

    // Search UI
    private SearchView mSearchView;
    private String mCustomFilters;
    private String mOrderBy = Const.SORT_BY_TITLE, mOrderDirection = Const.SORT_ASC;
    private boolean mStarAtTop = false;
    private String mCriteria = null, mCriteriaIO = null;
    private long mDateFilter = -1L;

    // Status bar
    private RelativeLayout mStatusBar;
    private TextView mStatusMsg;
    private List<String> mStatusQ = new ArrayList<String>();
    private GestureDetectorCompat mGestureDetector;
    private boolean mRefreshListSafe = true;

    // Settings related
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;
    private BackupManager mBackupManager;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    // Local repo
    private String mLocalRepoPath;
    protected static CustomFileObserver mObserver = null;

    // Backup
    private Uri mBackupUri, mRestoreUri;
    private boolean mIncrementalBackup = false;
    private String mFullPath, mDirPath, mSubDirPath;
    private WorkManager mBackupWorkManager;
    private PeriodicWorkRequest mBackupWorkRequest;
    private Constraints mBackupContraints;

    // Mirror
    private Uri mMirrorUri;
    private WorkManager mMirrorWorkManager;
    private PeriodicWorkRequest mMirrorWorkRequest;
    private Constraints mMirrorConstraints;
    private boolean mMirrorSafe = true;
    private long mLastMirrored = 0;  // Last mirror time

    // External storage
    private String mCurrentStoragePath = null;

    // Auto save
    private boolean mAutoSave = true;

    // Location aware
    private boolean mLocationAware = false;

    // Theme
    private String mTheme = "day";

    // Light sensor
    private boolean mLux = false;
    private boolean mAutoThemeApplied = false;
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private SensorEventListener mLightSensorEventListener;

    // OLED
    private boolean mOled = false;

    // Lazy update
    private boolean mLazyUpdate = false;

    // Math URL
    private String mMathUrl;

    // Show hidden
    private boolean mShowHidden = false;

    // Power management
    private PowerManager mPowerManager = null;

    // Hacks
    private boolean mKeepDeletedCopies = false;
    private int mMaxSyncLogFileSize = Const.MAX_SYNC_LOG_FILE_SIZE * Const.ONE_KB;
    private int mMaxSyncLogFileAge = Const.MAX_SYNC_LOG_FILE_AGE;
    private boolean mEvalBuiltInVariables = false;
    private boolean mLowSpaceMode = false;
    private String mLocalPriorityTag = "";
    private String mRemotePriorityTag = "";
    private String mPreviewMode = Const.PREVIEW_AT_END;
    private String mCustomDateFormat = "";
    private int mProcessTextMode = Const.PROCESS_TEXT_DISABLED;
    private String mLauncherTags = "";

    // Animation
    private Animation mFadeIn, mFadeOut, mSlideUp, mSlideDown, mPushDownIn, mPushLeftIn, mPushLeftOut, mPushRightIn, mZoomIn, mBounce, mGrowFromMiddle;

    // "Share To" history
    private static String mShareToHistory = "";

    // Misc.
    protected static MainActivity main_activity = null;

    // Get theme
    protected String getAppTheme() {
        return mTheme;
    }

    // Check mirror existence
    protected boolean hasMirror() {
        boolean status;

        // Sanity check
        if ((mLocalRepoPath == null) || (mLocalRepoPath.length() == 0) || (mBackupUri == null) || mBackupUri.equals(Uri.EMPTY))
            return false;

        // Reset mirror if it's not found
        status = Utils.hasSAFSubDir(getApplicationContext(), mBackupUri, Const.MIRROR_PATH);
        if (!status)
            resetMirror();

        return status;
    }

    // Reset mirror
    protected void resetMirror() {
        // Reset uri
        mMirrorUri = null;

        // Reset last mirror timestamp
        mLastMirrored = 0L;
        if (mSharedPreferencesEditor != null) {
            mSharedPreferencesEditor.putLong(Const.MIRROR_TIMESTAMP, mLastMirrored);
            mSharedPreferencesEditor.apply();
        }
    }

    // Reset mirror timestamp
    protected void resetLastMirrored() {
        // Reset last mirror timestamp
        mLastMirrored = 0L;
        if (mSharedPreferencesEditor != null) {
            mSharedPreferencesEditor.putLong(Const.MIRROR_TIMESTAMP, mLastMirrored);
            mSharedPreferencesEditor.apply();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /////////////////////
        // Setup preferences
        /////////////////////
        loadPref();

        // Verify storage permission
        verifyStoragePermission();

        // Verify notification permission
        verifyNotificationPermission();

        ////////////////
        // Setup theme
        ////////////////
        setupTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ///////////////////////////
        // Setup main application
        ///////////////////////////
        setupApplication();

        //////////////////
        // Setup database
        //////////////////
        setupDatabase();

        //////////////////
        // Setup backup
        //////////////////
        setupBackup();

        /////////////////
        // Setup toolbar
        ////////////////
        setupToolBar();

        ////////////////////
        // Setup status bar
        ///////////////////
        setupStatusBar();

        ////////////////////
        // Setup animation
        ///////////////////
        setupAnimation();

        ///////////////////////////
        // Setup navigation drawer
        //////////////////////////
        setupNavigationDrawer(savedInstanceState);

        ///////////////////////
        // Setup recycler view
        ///////////////////////
        setupRecyclerView();

        /////////////
        // Setup FAB
        /////////////
        setupFAB();

        ///////////////////////
        // Setup file observer
        ///////////////////////
        setupFileObserver();

        ////////////////
        // Setup mirror
        ////////////////
        setupMirror(ExistingPeriodicWorkPolicy.KEEP);

        ///////////////////////
        // Setup light sensor
        ///////////////////////
        if (mLux) setupLightSensor();

        ///////////////////////
        // Apply theme
        ///////////////////////
        applyTheme();

        /////////////////////
        // Setup animation
        /////////////////////
        setupAnimation();

        //////////////////////////
        // Setup the startup list
        //////////////////////////
        setupStartupList();

        ////////////////////////////
        // Setup local find history
        ////////////////////////////
        setupLocalFindHistory();

        /////////////////////////
        // Setup process text
        /////////////////////////
        setupProcessText();

        ///////////////////
        // Handle intent
        ///////////////////
        handleIntent(getIntent());
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        mDrawerLayout.openDrawer(GravityCompat.START);
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d(Const.TAG, "nano - onRestart");

        // Mirror if applicable
        if ((hasMirror()) && (!isPowerSaveMode())) {  // Only when power saver mode is off
            if ((!isSearchActive()) || (mCriteria.equals(getDefaultCustomFilter()))) {   // Conditions added to conserve battery
                doSAFMirrorSync(Const.MIRROR_INSTANT_WORK_TAG, ExistingWorkPolicy.KEEP);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(Const.TAG, "nano - onResume");

        // Verify storage permission
        verifyStoragePermission();

        // Resume database
        resumeDatabase();

        // Reapply theme
        applyTheme();

        // Sync automatically
        doSync();

        // Self reference
        main_activity = this;

        // Setup scrapbook
        setupScrapbook();

        // Hide I/O progress bar
        hideIOProgressBar();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Const.TAG, "nano - onPause");

        // Reset auto theme application state
        mAutoThemeApplied = false;

        // Update widget
        Intent intent = new Intent(Const.ACTION_UPDATE_WIDGET);
        getApplicationContext().sendBroadcast(intent);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(Const.TAG, "nano - onStop");

        // Mirror if applicable
        if (hasMirror()) {
            // Any change since last mirroring?
            List<Long> results = mDatasource.getAllActiveRecordsIDsByLastModified(Const.SORT_BY_TITLE, Const.SORT_ASC, mLastMirrored, ">");
            if (results.size() > 0)
                doSAFMirrorSync(Const.MIRROR_INSTANT_WORK_TAG, ExistingWorkPolicy.KEEP);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(Const.TAG, "nano - onDestroy");

        // Stop observer
        if (mObserver != null)
            mObserver.stopWatching();

        main_activity = null;

        mDatasource.close();
    }

    @Override
    public void onBackPressed() {
        // Return to default filter in the context of a search
        if ((isSearchActive()) && (!mCriteria.equals(getDefaultCustomFilter())))
            doGotoDefaultCustomFilter();

        // Exit otherwise
        else
            finish();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(Const.STATE_ORDER_BY, mOrderBy);
        savedInstanceState.putString(Const.STATE_ORDER_DIRECTION, mOrderDirection);
        savedInstanceState.putString(Const.STATE_CRITERIA, mCriteria);
        savedInstanceState.putLong(Const.STATE_DATE_FILTER, mDateFilter);
        savedInstanceState.putBundle(Const.STATE_SELECTION_STATE, mAdapter.getSelectionStates());
        savedInstanceState.putInt(Const.STATE_NAVIGATION_POSITION, mCurrentSelectedPosition);

        savedInstanceState.putString(Const.STATE_FULL_PATH, mFullPath);
        savedInstanceState.putString(Const.STATE_DIR_PATH, mDirPath);
        savedInstanceState.putString(Const.STATE_SUB_DIR_PATH, mSubDirPath);

        savedInstanceState.putBoolean(Const.STATE_AUTO_THEME_APPLIED, mAutoThemeApplied);

        savedInstanceState.putBoolean(Const.STATE_MIRROR_SAFE, mMirrorSafe);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mOrderBy = savedInstanceState.getString(Const.STATE_ORDER_BY);
        mOrderDirection = savedInstanceState.getString(Const.STATE_ORDER_DIRECTION);
        setCriteria(savedInstanceState.getString(Const.STATE_CRITERIA));
        mDateFilter = savedInstanceState.getLong(Const.STATE_DATE_FILTER);
        mAdapter.setSelectionStates(savedInstanceState.getBundle(Const.STATE_SELECTION_STATE));
        mCurrentSelectedPosition = savedInstanceState.getInt(Const.STATE_NAVIGATION_POSITION);

        mFullPath = savedInstanceState.getString(Const.STATE_FULL_PATH);
        mDirPath = savedInstanceState.getString(Const.STATE_DIR_PATH);
        mSubDirPath = savedInstanceState.getString(Const.STATE_SUB_DIR_PATH);

        mAutoThemeApplied = savedInstanceState.getBoolean(Const.STATE_AUTO_THEME_APPLIED);

        mMirrorSafe = savedInstanceState.getBoolean(Const.STATE_MIRROR_SAFE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_list_menu, menu);

        // Setup search
        MenuItem item = menu.findItem(R.id.menu_search);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        mSearchView = (SearchView) item.getActionView();
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(new ComponentName(getApplicationContext(), MainActivity.class)));
        mSearchView.setIconifiedByDefault(true);
        mSearchView.setMaxWidth(Integer.MAX_VALUE);
        mSearchView.setQueryRefinementEnabled(true);
        mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
        mSearchView.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        mSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hide navigation icon
                showHideNavigationIcon(false);
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Show navigation icon
                showHideNavigationIcon(true);

                // Safe to refresh list
                mRefreshListSafe = true;

                // Issue immediately
                refreshList();

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Unsafe to refresh list
                mRefreshListSafe = false;
                return false;
            }
        });

        mSearchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                // Show navigation icon
                showHideNavigationIcon(true);

                // Safe to refresh list
                mRefreshListSafe = true;
                return false;
            }
        });

        mSearchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                // Show navigation icon
                showHideNavigationIcon(true);

                // Safe to refresh list
                mRefreshListSafe = true;
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                Cursor cursor = (Cursor) mSearchView.getSuggestionsAdapter().getItem(
                        position);
                int idx = cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1);
                String selected = cursor.getString(idx);

                mSearchView.setQuery(selected, false);
                mSearchView.clearFocus();

                // Show navigation icon
                showHideNavigationIcon(true);

                // Safe to refresh list
                mRefreshListSafe = true;

                // Issue immediately
                refreshList();

                return false;
            }
        });

        // Setup suggestions
        int id = R.id.search_src_text;
        final AutoCompleteTextView text_view = mSearchView.findViewById(id);
        final View dropdown_anchor = mSearchView.findViewById(text_view.getDropDownAnchor());

        if (dropdown_anchor != null) {
            dropdown_anchor.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v,
                                           int left,
                                           int top,
                                           int right,
                                           int bottom,
                                           int oldLeft,
                                           int oldTop,
                                           int oldRight,
                                           int oldBottom) {
                    // Set width
                    int screen_width = MainActivity.main_activity.getResources().getDisplayMetrics().widthPixels;
                    text_view.setDropDownWidth(screen_width);
                }
            });
        }

        // Setup mirror menu
        item = menu.findItem(R.id.menu_mirror_files);
        if (hasMirror()) {
            item.setVisible(true);
        } else {
            item.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Toggle sort icon
        MenuItem item = menu.findItem(R.id.menu_sort);
        boolean reverse = false;

        if (mOrderBy.equals(Const.SORT_BY_TITLE)) {
            if (mOrderDirection.equals(Const.SORT_DESC))
                reverse = true;
        } else if (mOrderBy.equals(Const.SORT_BY_MODIFIED) || mOrderBy.equals(Const.SORT_BY_ACCESSED)) {
            if (mOrderDirection.equals(Const.SORT_ASC))
                reverse = true;
        }

        if (reverse)
            item.setIcon(ContextCompat.getDrawable(DBApplication.getAppContext(), R.drawable.ic_rsort_anim_vector));
        else
            item.setIcon(ContextCompat.getDrawable(DBApplication.getAppContext(), R.drawable.ic_sort_anim_vector));

        // Animate selected item
        if (mToolBarSelectedItemId == R.id.menu_sort) {
            item = menu.findItem(mToolBarSelectedItemId);
            ((AnimatedVectorDrawable) item.getIcon()).start();

            // Reset selected item
            mToolBarSelectedItemId = -1;
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Const.REQUEST_CODE_STORAGE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();

                // Initial
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, this.getResources().getString(R.string.status_storage_permission_granted), Toast.LENGTH_LONG).show();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, this.getResources().getString(R.string.status_storage_permission_denied), Toast.LENGTH_LONG).show();
                }
            }
            break;
            case Const.REQUEST_CODE_LOCATION_PERMISSION: {
                Map<String, Integer> perms = new HashMap<String, Integer>();

                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, this.getResources().getString(R.string.status_location_permission_granted), Toast.LENGTH_LONG).show();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, this.getResources().getString(R.string.status_location_permission_denied), Toast.LENGTH_LONG).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Show sort popup
    private void showSortPopup() {
        View view = findViewById(R.id.menu_sort);

        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        Menu menu = popup.getMenu();

        // Setup the popup
        inflater.inflate(R.menu.sort_menu, menu);
        popup.setOnMenuItemClickListener(this);

        // Setup checkboxes
        MenuItem sort_by_title = menu.findItem(R.id.menu_sort_by_title);
        MenuItem sort_by_date = menu.findItem(R.id.menu_sort_by_date);

        if (mOrderBy.equals(Const.SORT_BY_TITLE)) {
            sort_by_title.setChecked(true);
            sort_by_date.setChecked(false);
        } else {
            sort_by_title.setChecked(false);
            sort_by_date.setChecked(true);
        }

        // Show the popup
        popup.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // Update currenlty selected item
        mToolBarSelectedItemId = itemId;

        if (itemId == R.id.menu_sort)
            // Note: assuming sort menu icon is visible, otherwise an exception will be thrown
            showSortPopup();

        else if (itemId == R.id.menu_advanced_search)
            handleAdvancedSearch();

        else if (itemId == R.id.menu_import_files)
            handleSAFImport();

        else if (itemId == R.id.menu_export_files)
            handleSAFExport();

        else if (itemId == R.id.menu_mirror_files)
            handleSAFMirror();

        else if (itemId == R.id.menu_clear_search_history)
            doClearSearchHistory();

        else if (itemId == R.id.menu_clear_location_data)
            doClearLocationData();

        else if (itemId == android.R.id.home)
            onBackPressed();

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if ((resultCode == Activity.RESULT_OK) && (requestCode == Const.REQUEST_CODE_PICK_BACKUP_URI)) {
            // Persist uri permission
            mBackupUri = intent.getData();
            ContentResolver resolver = DBApplication.getAppContext().getContentResolver();
            int perms = Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION;

            resolver.takePersistableUriPermission(mBackupUri, perms);

            // Update preference
            mSharedPreferencesEditor.putString(Const.PREF_BACKUP_URI, mBackupUri.toString());
            mSharedPreferencesEditor.commit();

            // Setup mirror
            setupMirror(ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE);

            // Schedule backup
            scheduleBackup();
        } else if ((resultCode == Activity.RESULT_OK) && (requestCode == Const.REQUEST_CODE_PICK_RESTORE_URI)) {
            // Set restore uri and launch import
            mRestoreUri = intent.getData();
            doSAFImport();
        } else {
            // To make it simple, always re-load Preference setting.
            loadPref();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    // Setup main application
    private void setupApplication() {
        mApp = (DBApplication) getApplication();

        // Self reference
        main_activity = this;
    }

    // Setup backup
    private void setupBackup() {
        mBackupManager = new BackupManager(this);
    }

    // Verify databas
    protected void verifyDatabase() {
        try {
            Thread t = new Thread() {
                public void run() {
                    resumeDatabase();
                    mDatasource.removeDuplicateRecords(Utils.fileNameAsTitle(getApplicationContext()));
                }
            };
            t.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // Setup database
    private void setupDatabase() {
        // Get all entries from the database
        mDatasource = new DataSource();
        mDatasource.open();

        // Set filter
        if (mShowHidden)
            mDatasource.setFilter(Const.SHOW_PATTERN);
        else
            mDatasource.setFilter(Const.HIDE_PATTERN);

        // Set order by prefix
        if (mStarAtTop)
            mDatasource.setOrderByPrefix(Const.SORT_BY_STAR + " " + Const.SORT_DESC);

        // Set adapter's datasource
        if (mAdapter != null)
            mAdapter.setDatasource(mDatasource);
    }

    // Resume database
    private void resumeDatabase() {
        if ((mDatasource == null) || (!mDatasource.isOpen())) {
            setupDatabase();

            // Mark list stale (adapter also needs a new cursor)
            mCriteriaIO = Const.INVALID_SYM;
        }
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

        if ((mTheme.equals(Const.NIGHT_THEME)) || (mode.equals(Const.NIGHT_THEME)))
            mOled = true;

        if (mOled)
            setTheme(R.style.AppThemeOled);
        else if (mLux)
            setTheme(R.style.AppThemeLux);
        else if ((mTheme.equals(Const.DAY_THEME)) || (mode.equals(Const.DAY_THEME)))
            setTheme(R.style.AppThemeDay);
        else
            setTheme(R.style.AppTheme);
    }

    // Setup the toolbar
    private void setupToolBar() {
        mToolbar = findViewById(R.id.main_toolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
            mToolbar.setNavigationIcon(R.drawable.ic_menu_vector);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!mSearchView.isIconified())
                        mSearchView.setIconified(true);
                    else
                        mDrawerLayout.openDrawer(Gravity.LEFT);
                }
            });
        }
    }

    // Show hide navigation icon
    private void showHideNavigationIcon(boolean visible) {
        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        if (mToolbar != null) {
            if (visible) {
                mToolbar.setNavigationIcon(R.drawable.ic_menu_vector);
            } else {
                mToolbar.setNavigationIcon(R.drawable.ic_back_vector);
            }
        }
    }

    // Setup status bar
    private void setupStatusBar() {
        mStatusBar = (RelativeLayout) findViewById(R.id.status_bar);

        // Set gesture detector
        mGestureDetector = new GestureDetectorCompat(this, new MainStatusGestureListener());
        mStatusBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        mStatusMsg = (TextView) findViewById(R.id.status_msg);
    }

    // Setup animation
    protected void setupAnimation() {
        mBounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        mSlideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        mSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        mPushDownIn = AnimationUtils.loadAnimation(this, R.anim.push_down_in);
        mPushLeftIn = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
        mPushLeftOut = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
        mPushRightIn = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
        mZoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mGrowFromMiddle = AnimationUtils.loadAnimation(this, R.anim.grow_from_middle);
    }

    // Setup the recycler view
    private void setupRecyclerView() {
        ///////////////////////
        // Setup recycler view
        ///////////////////////
        mRecyclerView = (FastScrollRecyclerView) findViewById(R.id.recycler_view);
        mEmptyView = (TextView) findViewById(R.id.empty_view);

        // Use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Specify an adapter (see also next example)
        mAdapter = new CustomAdapter(mCursor, this, mDatasource);
        mRecyclerView.setAdapter(mAdapter);

        // Setup swiping
        /* Note: disabled for now
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder viewHolder1) {
                final int fromPos = viewHolder.getAdapterPosition();
                final int toPos = viewHolder1.getAdapterPosition();

                // move item in `fromPos` to `toPos` in adapter.
                return true;// true if moved, false otherwise
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                int position = viewHolder.getAdapterPosition();

                // Remove from database
                mCursor.moveToPosition(position);
                mUndoCandidate = mDatasource.cursorToRecord(mCursor).getId();
                mDatasource.markRecordDeletedById(mUndoCandidate, 1);

                // Remove swiped item from list and notify the RecyclerView
                // mAdapter.notifyItemRemoved(position);

                // Reload the cursor and notify the list
                refreshList();
            }
        };

        mItemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        */

        // Setup swipe refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.swipe_refresh_color_1, R.color.swipe_refresh_color_2, R.color.swipe_refresh_color_3);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Send a sync request
                Utils.sendSyncRequest(getApplicationContext(), mLocalRepoPath, mBackupUri);

                if (!Uri.EMPTY.equals(mBackupUri)) {
                    DocumentFile dir = DocumentFile.fromTreeUri(getApplicationContext(), mBackupUri);
                    if (Utils.getSAFSubDirUri(getApplicationContext(), mBackupUri, Const.NOOP_FILE) != null)
                        Utils.exportToSAFFile(getApplicationContext(), mLocalRepoPath + "/", Const.NOOP_FILE, dir);
                }

                // Mirror if applicable
                if (hasMirror())
                    doSAFMirrorSync(Const.MIRROR_INSTANT_WORK_TAG, ExistingWorkPolicy.KEEP);

                mSwipeRefreshLayout.setRefreshing(false);
                refreshList();
            }
        });
    }

    // Setup FAB
    private void setupFAB() {
        mFab = (FloatingActionButton) findViewById(fab);

        // Set handler
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doAdd();
            }
        });
    }

    // Setup navigation drawer
    private void setupNavigationDrawer(Bundle savedInstanceState) {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                try {
                    // Get version
                    TextView header = (TextView) findViewById(R.id.drawer_header_view);
                    String str = getResources().getString(R.string.app_name) + " v" + Utils.getVersion(main_activity);

                    header.setTypeface(Typeface.createFromAsset(getAssets(), "RobotoSlab-Regular.ttf"));
                    header.setText(str);
                    header.setVisibility(View.VISIBLE);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                // Safe to refresh list
                mRefreshListSafe = true;
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                // Do not remember the selected item
                mNavigationView.getMenu().getItem(mCurrentSelectedPosition).setChecked(false);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mNavigationView = (NavigationView) findViewById(R.id.navigation);
        mNavigationView.setNavigationItemSelectedListener(this);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(Const.STATE_NAVIGATION_POSITION);
        }
    }

    // Setup file observer
    protected void setupFileObserver() {
        // Stop existing observer
        if (mObserver != null)
            mObserver.stopWatching();

        mObserver = new CustomFileObserver(mLocalRepoPath);
        mObserver.startWatching();
    }

    // Setup mirror
    protected void setupMirror(ExistingPeriodicWorkPolicy policy) {
        if (hasMirror()) {
            // Setup uri
            DocumentFile dir = DocumentFile.fromTreeUri(getApplicationContext(), mBackupUri);
            DocumentFile dest_dir = Utils.getSAFSubDir(getApplicationContext(), dir, Const.MIRROR_PATH);
            mMirrorUri = dest_dir.getUri();

            // Schedule mirror
            scheduleMirror(policy);

            // Do a quick mirroring
            doSAFMirrorSync(Const.MIRROR_INSTANT_WORK_TAG, ExistingWorkPolicy.REPLACE);
        }
        else {
            // Cancel mirror
            cancelMirror();

            // Reset last mirror timestamp
            resetMirror();
        }
    }

    // Setup startup list
    protected void setupStartupList() {
        if ((mCustomFilters != null) && (mCustomFilters.length() > 0)) {
            String[] temp = mCustomFilters.split(";");
            if (temp.length > 0)
                setCriteria(temp[0]);
        }
    }

    // Setup local find history
    protected void setupLocalFindHistory() {
        LocalFindHistory.add(Const.YESTERDAY_VAR, Const.YESTERDAY_VAR);
        LocalFindHistory.add(Const.TODAY_VAR, Const.TODAY_VAR);
        LocalFindHistory.add(Const.TOMORROW_VAR, Const.TOMORROW_VAR);
        LocalFindHistory.add(Const.CLIPBOARD_VAR, Const.CLIPBOARD_VAR);
    }

    // Setup light sensor
    protected void setupLightSensor() {
        // Get light sensor
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // Setup sensor event listener
        mLightSensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if ((!mAutoThemeApplied) && (sensorEvent.sensor.getType() == Sensor.TYPE_LIGHT)) {
                    float light_level = sensorEvent.values[0];

                    // Set light level
                    Utils.setLightLevel(light_level);

                    // If natural light is detected
                    if (light_level > Const.LIGHT_LEVEL_THRESHOLD_NATURAL_LIGHT) {
                        if (!mTheme.equals(Const.DAY_THEME)) {
                            mSharedPreferencesEditor.putString(Const.PREF_THEME, Const.DAY_THEME);
                            mSharedPreferencesEditor.commit();
                        }
                    } else {
                        if (mTheme.equals(Const.DAY_THEME)) {
                            mSharedPreferencesEditor.putString(Const.PREF_THEME, Const.NIGHT_THEME);
                            mSharedPreferencesEditor.commit();
                        }
                    }

                    // Remember auto theme application status
                    mAutoThemeApplied = true;
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };

        // Register event listener
        if (mLightSensor != null) {
            mSensorManager.registerListener(
                    mLightSensorEventListener,
                    mLightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // Schedule backup
    protected void scheduleBackup() {
        // Sanity check
        if (!mIncrementalBackup) return;

        mBackupWorkManager = WorkManager.getInstance(getApplicationContext());

        // Build constraints
        mBackupContraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build();

        // Build request
        mBackupWorkRequest = new PeriodicWorkRequest.Builder(
                BackupWorker.class,
                Const.AUTO_BACKUP_FREQ,
                TimeUnit.HOURS)
                .setConstraints(mBackupContraints)
                .build();

        // Add to the queue
        // Note: if an incomplete job exists, cancel and reenqueue
        mBackupWorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
                Const.BACKUP_WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                mBackupWorkRequest);

        Log.d(Const.TAG, "nano - Backup job scheduled");
    }

    // Cancel backup
    protected void cancelBackup() {
        if (mBackupWorkManager != null) {
            mBackupWorkManager.cancelUniqueWork(Const.BACKUP_WORK_NAME);
            Log.d(Const.TAG, "nano - Backup job cancelled");
        }
    }

    // Schedule mirror
    protected void scheduleMirror(ExistingPeriodicWorkPolicy policy) {
        // Sanity check
        if (!hasMirror()) return;

        mMirrorWorkManager = WorkManager.getInstance(getApplicationContext());

        // Build constraints
        mMirrorConstraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build();

        // Build request
        mMirrorWorkRequest = new PeriodicWorkRequest.Builder(
                MirrorWorker.class,
                Const.AUTO_MIRROR_FREQ,
                TimeUnit.HOURS)
                .addTag(Const.MIRROR_WORK_TAG)
                .setConstraints(mMirrorConstraints)
                .build();

        // Add to the queue
        mMirrorWorkManager.getInstance(getApplicationContext()).enqueueUniquePeriodicWork(
                Const.MIRROR_WORK_NAME,
                policy,
                mMirrorWorkRequest);

        Log.d(Const.TAG, "nano - Mirror job scheduled");
    }

    // Cancel mirror
    protected void cancelMirror() {
        if (mMirrorWorkManager != null) {
            mMirrorWorkManager.cancelUniqueWork(Const.MIRROR_WORK_NAME);
            Log.d(Const.TAG, "nano - Mirror job cancelled");
        }
    }

    // Scroll to top
    protected void scrollToTop() {
        if (mRecyclerView != null) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    // Apply theme
    protected void applyTheme() {
        try {
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

            if ((mTheme.equals(Const.NIGHT_THEME)) || (mode.equals(Const.NIGHT_THEME))) {
                mStatusMsg.setTextColor(ContextCompat.getColor(this, R.color.status_bar_text_night));
                mStatusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.status_bar_night));
                mSwipeRefreshLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.swipe_refresh_background_night));
                mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.swipe_refresh_night));
            } else if ((mTheme.equals(Const.DARK_THEME)) || (mode.equals(Const.DARK_THEME))) {
                mStatusMsg.setTextColor(ContextCompat.getColor(this, R.color.status_bar_text_dark));
                mStatusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.status_bar_dark));
                mSwipeRefreshLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.swipe_refresh_background_dark));
                mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.swipe_refresh_dark));
            } else {
                mStatusMsg.setTextColor(ContextCompat.getColor(this, R.color.status_bar_text_day));
                mStatusBar.setBackgroundColor(ContextCompat.getColor(this, R.color.status_bar_day));
                mSwipeRefreshLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.swipe_refresh_background_day));
                mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(ContextCompat.getColor(this, R.color.swipe_refresh_day));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Setup process text
    protected void setupProcessText() {
        PackageManager pm = getApplicationContext().getPackageManager();
        ComponentName comp = new ComponentName(getPackageName(), getPackageName() + Const.PROCESS_TEXT_ACTIVITY_NAME);

        if (mProcessTextMode == Const.PROCESS_TEXT_DISABLED) {
            pm.setComponentEnabledSetting(
                    comp,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }
        else {
            pm.setComponentEnabledSetting(
                    comp,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }

    // Setup scrapbook
    protected void setupScrapbook() {
        Intent intent = new Intent(getApplicationContext(), NotificationReceiver.class);
        intent.setAction(Const.ACTION_UPDATE_SCRAPBOOK);
        sendBroadcast(intent);
    }

    // Handle intent
    protected void handleIntent(Intent intent) {
        if (Const.ACTION_VIEW_ENTRY.equals(intent.getAction())) {
            long id = intent.getLongExtra(Const.EXTRA_ID, -1);

            Intent launchIntent = new Intent(this, DisplayDBEntry.class);
            launchIntent.putExtra(Const.EXTRA_ID, id);

            startActivity(launchIntent);
        } else if (Const.ACTION_ADD_ENTRY.equals(intent.getAction())) {
            Intent launchIntent = new Intent(this, DisplayDBEntry.class);
            launchIntent.putExtra(Const.EXTRA_ID, -1);

            startActivity(launchIntent);
        } else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            setCriteria(intent.getStringExtra(SearchManager.QUERY).trim());
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
            suggestions.saveRecentQuery(mCriteria, null);

            // Update search history on file if available
            updateSearchHistory(true);

            // Notify change (useful only when a search was issued from a current instance of MainActivity)
            if (mAdapter != null) {
                mAdapter.changeCursor(mCursor);
                mAdapter.notifyDataSetChanged();
            }
        } else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            if (intent.getType() != null)
                viewFile(intent);
            else
                viewLink(intent);
        } else if ((Intent.ACTION_SEND.equals(intent.getAction())) && (intent.getType() != null)) {
            if (Const.PLAIN_TEXT_TYPE.equals(intent.getType())) {
                String str = intent.getStringExtra(Intent.EXTRA_TEXT);
                if (!TextUtils.isEmpty(str))
                    handleShareTo(str);
            }
        } else if (Intent.ACTION_PROCESS_TEXT.equals(intent.getAction())) {
            if (mProcessTextMode == Const.PROCESS_TEXT_PASTE) {
                String str = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
                if (!TextUtils.isEmpty(str))
                    handleShareTo(str);
            }
            else if (mProcessTextMode == Const.PROCESS_TEXT_SEARCH) {
                String str = intent.getCharSequenceExtra(Intent.EXTRA_PROCESS_TEXT).toString();
                setCriteria(str.trim());
                SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                        SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                suggestions.saveRecentQuery(mCriteria, null);

                // Update search history on file if available
                updateSearchHistory(true);

                // Notify change (useful only when a search was issued from a current instance of MainActivity)
                if (mAdapter != null) {
                    mAdapter.changeCursor(mCursor);
                    mAdapter.notifyDataSetChanged();
                }
            }
        } else if ((Const.ACTION_AUTO_SEND.equals(intent.getAction())) && (intent.getType() != null)) {
            if (Const.PLAIN_TEXT_TYPE.equals(intent.getType())) {
                doVoiceMemo(intent);
            }
        } else
            refreshList();
    }

    // View a note
    protected void viewNote(long id) {
        Intent launchIntent = new Intent(this, DisplayDBEntry.class);
        launchIntent.putExtra(Const.EXTRA_ID, id);

        startActivity(launchIntent);
    }

    // View a file
    private void viewFile(Intent launchIntent) {
        // Intent related
        Uri uri = launchIntent.getData();
        Intent intent;

        // File related
        String path = uri.getEncodedPath(), title, content, line;

        try {
            path = URLDecoder.decode(path, "UTF-8");
            File file = new File(path);
            String file_name = file.getName();

            // Get title
            if (Utils.fileNameAsTitle(this))
                title = file_name;
            else
                title = file_name.substring(0, file_name.lastIndexOf('.'));

            // Show progress bar
            showIOProgressBar(getResources().getString(R.string.status_opening) + "\"" + title + "\" " + Const.ELLIPSIS_SYM);

            // Sanity check
            FileInputStream in = new FileInputStream(file);

            // Create intent
            intent = new Intent(this, DisplayDBEntry.class);
            intent.putExtra(Const.EXTRA_ID, -1);
            intent.putExtra(Const.EXTRA_CRITERIA, "");
            intent.putExtra(Const.EXTRA_TITLE, title);
            intent.putExtra(Const.EXTRA_FILEPATH, path);

            // Send the intent
            startActivity(intent);

        } catch (FileNotFoundException e) {
            e.printStackTrace();

            // Show progress bar
            showIOProgressBar(getResources().getString(R.string.status_opening) + Const.ELLIPSIS_SYM);

            // Create intent
            intent = new Intent(this, DisplayDBEntry.class);
            intent.putExtra(Const.EXTRA_ID, -1);
            intent.putExtra(Const.EXTRA_CRITERIA, "");
            intent.putExtra(Const.EXTRA_URI, uri.toString());

            // Send the intent
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close cursor
            // Note: should I close else where?
            if (mCursor != null)
                mCursor.close();
        }
    }

    // View a link
    private void viewLink(Intent launchIntent) {
        try {
            // Intent related
            Uri uri = launchIntent.getData();
            List<String> params = uri.getPathSegments();

            // Sanity check
            if (params.size() == 0)
                return;

            // Get title
            String title = params.get(0);
            String criteria;

            // Get criteria
            criteria = uri.getQueryParameter(Const.CUSTOM_SCHEME_SEARCH_OP);

            // Get record
            ArrayList<DBEntry> results = mDatasource.getContentlessRecordByTitle(title);
            if (results.size() == 1) {

                // Create intent
                Intent intent = new Intent(this, DisplayDBEntry.class);
                intent.putExtra(Const.EXTRA_ID, results.get(0).getId());
                intent.putExtra(Const.EXTRA_CRITERIA, criteria);

                // Send the intent
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close cursor
            // Note: should I close else where?
            if (mCursor != null)
                mCursor.close();
        }
    }

    // Check network connection
    protected boolean isConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    // Check power saving mode
    protected boolean isPowerSaveMode() {
        try {
            if (mPowerManager == null)
                mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);

            return mPowerManager.isPowerSaveMode();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Reset criteria
    private void clearSearch() {
        setCriteria(null);
    }

    // Check whether a search is going on
    protected boolean isSearchActive() {
        return ((mCriteria != null) && (mCriteria.length() > 0));
    }

    // Align FAB
    protected void alignFAB(int id) {
        try {
            // Adjust FAB position
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) mFab.getLayoutParams();
            params.setAnchorId(id);
            mFab.setLayoutParams(params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Show/hide recycler view
    protected void showHideRecyclerView() {
        if (mCursor.getCount() == 0) {
            mSwipeRefreshLayout.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
            mEmptyView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearSearch();
                    refreshList();
                }
            });

            // Re-align FAB when ready to edit
            if ((mLocalRepoPath != null) && (mLocalRepoPath.length() > 0))
                alignFAB(R.id.empty_view);
        } else {
            mSwipeRefreshLayout.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

            // Re-align FAB when ready to edit
            if ((mLocalRepoPath != null) && (mLocalRepoPath.length() > 0))
                alignFAB(R.id.swipe_refresh);
        }
    }

    // Perform list transition
    protected void transitionList() {
        updateStatus(null, mGrowFromMiddle);
    }

    // Refresh list
    protected /* synchronized */ void refreshList() {
        // Sanity check
        if (!mRefreshListSafe) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new RefreshListTask().executeOnExecutor(CustomAsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new RefreshListTask().execute();
        }
    }

    // Refresh list task
    private class RefreshListTask extends CustomAsyncTask<Void, Integer, Long> {
        @Override
        protected Long doInBackground(Void... params) {
            long totalSize = 0;
            String criteria = mCriteria;

            try {
                // Update the main list
                if (!isSearchActive()) {
                    mCursor = mDatasource.getAllActiveRecordsCursor(mOrderBy, mOrderDirection, mPreviewMode);
                } else {
                    // Evaluate built-in variables
                    if (mEvalBuiltInVariables)
                        criteria = evalVariables(mCriteria);

                    if ((mCriteria.equals(Const.MODIFIED_AFTER_FILTER)) && (mDateFilter > 0))
                        mCursor = mDatasource.getAllActiveRecordsByLastModifiedCursor(mOrderBy, mOrderDirection, mDateFilter, ">", mPreviewMode);

                    else if ((mCriteria.equals(Const.ACCESSED_AFTER_FILTER)) && (mDateFilter > 0))
                        mCursor = mDatasource.getAllActiveRecordsByLastAccessedCursor(mOrderBy, mOrderDirection, mDateFilter, ">", mPreviewMode);

                    else if (mCriteria.equals(Const.MODIFIED_NEARBY_FILTER))
                        mCursor = mDatasource.getAllActiveRecordsModifiedNearbyCursor(mOrderBy, mOrderDirection, Const.NEARBY, mPreviewMode);

                    else if (mCriteria.startsWith(Const.METADATAONLY)) {
                        mCursor = mDatasource.getAllActiveRecordsByMetadataCursor(criteria.substring(Const.METADATAONLY.length()), mOrderBy, mOrderDirection, mPreviewMode);
                    } else if (mCriteria.startsWith(Const.METADATAREGONLY)) {
                        mCursor = mDatasource.getAllActiveRecordsByMetadataRegCursor(criteria.substring(Const.METADATAREGONLY.length()), mOrderBy, mOrderDirection, mPreviewMode);
                    } else {
                        mCursor = mDatasource.searchRecordsCursor(criteria, mOrderBy, mOrderDirection, mPreviewMode);
                    }
                }
            } catch (Exception e) {
                Log.d(Const.TAG, "nano - RefreshListTask: caught an exception");
                e.printStackTrace();
            }
            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            // Unsafe to refresh list
            mRefreshListSafe = false;

            // Make sure the database is open
            resumeDatabase();

            // Show progress
            showSwipeRefresh();
        }

        @Override
        protected void onPostExecute(Long result) {
            if (mAdapter != null) {
                mAdapter.changeCursor(mCursor);
                mAdapter.notifyDataSetChanged();
            }

            showHideRecyclerView();

            // Update status
            updateStatus(null, null);

            // Hide progress
            hideSwipeRefresh();

            // Update menu
            invalidateOptionsMenu();

            // Safe to refresh list
            mRefreshListSafe = true;
        }

        @Override
        protected void onCancelled() {
            Log.i(Const.TAG, "RefreshListTask: onCancelled");

            // Hide progress
            hideIOProgressBar();

            // Update menu
            invalidateOptionsMenu();
        }
    }

    // Refresh list with delay
    protected void refreshListDelayed(int delay) {
        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshList();
            }
        }, delay);
    }

    // Add status
    protected void addStatus(String status) {
        // Sanity check
        if (mStatusQ == null)
            mStatusQ = new ArrayList<String>();

        mStatusQ.add(status);
    }

    // Show status
    private void updateStatus(String status, Animation animation) {
        String count_status;
        int count = 0;

        if (mStatusQ.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : mStatusQ) {
                sb.append(s);
                sb.append(Const.DELIM_SYM);
            }

            status = sb.toString();

            mStatusQ.clear();

            // Add animated effect
            animation = mPushDownIn;
        } else if ((status == null) || (status.length() == 0)) {
            // Default status
            if (mCursor != null)
                count = mCursor.getCount();

            if (count == 0)
                count_status = getResources().getString(R.string.status_no_note);

            else
                count_status = Integer.toString(count) + getResources().getString(R.string.status_count);

            if ((mCriteria != null) && (mCriteria.length() > 0))
                if ((mCriteria.equals(Const.MODIFIED_AFTER_FILTER)) || (mCriteria.equals(Const.ACCESSED_AFTER_FILTER)))
                    status = Utils.convertCriteriaToStatus(getApplicationContext(), mCriteria, mDateFilter) + ": " + count_status;

                else if (mCriteria.equals(Const.MODIFIED_NEARBY_FILTER))
                    status = Utils.convertCriteriaToStatus(getApplicationContext(), mCriteria, -1L) + count_status;

                else
                    status = mCriteria + ": " + count_status;

            else
                status = count_status;
        }

        if (animation != null)
            mStatusMsg.startAnimation(animation);

        mStatusMsg.setText(status);
    }

    // Get order by
    protected String getOrderBy() {
        return mOrderBy;
    }

    // Get criteria
    protected String getCriteria() {
        return mCriteria;
    }

    // Set criteria
    protected void setCriteria(String criteria) {
        mCriteria = criteria;
        mCriteriaIO = null;
    }

    // Evaluate variables
    private String evalVariables(String str) {
        // Evaluate global variables
        str = evalGlobalVariables(getApplicationContext(), str, mCustomDateFormat, null, false);
        return str;
    }

    // Check to see if note list reflects I/O updates
    protected boolean isStaleList(boolean updated) {
        // Always refresh if no criteria
        if ((mCriteria == null) || (mCriteria.equals(Const.ALL_SYM)))
            return true;

        if (!mCriteria.equals(mCriteriaIO)) {
            mCriteriaIO = mCriteria;
            return true;
        }

        return updated;
    }

    // Do sort
    private void doSort(String orderBy, String direction1, String direction2) {
        if ((mOrderBy.equals(orderBy) && mOrderDirection.equals(direction1)))
            mOrderDirection = direction2;
        else
            mOrderDirection = direction1;

        mOrderBy = orderBy;

        // Update preferences
        mSharedPreferencesEditor.putString(Const.PREF_ORDER_BY, mOrderBy);
        mSharedPreferencesEditor.putString(Const.PREF_ORDER_BY_DIRECTION, mOrderDirection);
        mSharedPreferencesEditor.commit();

        // Refresh list
        refreshList();
        scrollToTop();
    }

    // Sync database
    private void doSync() {
        doRescanLocalRepo();
    }

    // Do rescan local repo
    protected void doRescanLocalRepo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new RescanLocalRepoTask().executeOnExecutor(CustomAsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new RescanLocalRepoTask().execute();
        }
    }

    // Rescan local repo
    private class RescanLocalRepoTask extends CustomAsyncTask<Void, Integer, Long> {
        private ProgressBar progressBar;
        private int count = 0;
        private int incr;
        private File files[];
        private boolean updated = false;

        @Override
        protected Long doInBackground(Void... params) {
            long totalSize = 0;
            String title;
            List<DBEntry> results, results_temp;
            DBEntry entry;
            long id;

            try {
                Log.d(Const.TAG, "nano - RescanLocalRepoTask in progress...");

                // Make sure the database is open
                resumeDatabase();

                // Remove files that are missing remotely
                results = mDatasource.getAllSimpleRecords(DBHelper.COLUMN_TITLE, Const.SORT_ASC);

                for (int i = 0; i < results.size(); i++) {
                    entry = results.get(i);
                    title = entry.getTitle();

                    if ((title == null) || (title.length() == 0) || (!Utils.fileExists(getApplicationContext(), mLocalRepoPath, title))) {
                        id = entry.getId();

                        // Update file if record does not have priority
                        if ((mLocalPriorityTag != null) && (mLocalPriorityTag.length() > 0) && (entry.getMetadata().contains(mLocalPriorityTag))) {
                            // Do nothing
                        } else {
                            // Keep deleted copies
                            if ((mKeepDeletedCopies) && (title != null) && (title.length() > 0) && (!title.matches(Const.CONFLICT_PATTERN))) {
                                results_temp = mDatasource.getRecordById(id);
                                if ((results_temp.size() > 0) && (results_temp.get(0).getSize() > 0)) {
                                    if (mMirrorUri != null)
                                        Utils.writeSpecialSAFFile(getApplicationContext(), mMirrorUri, Const.TRASH_PATH, Utils.makeDeletedTitle(results_temp.get(0).getTitle()), results_temp.get(0).getContent());
                                    else
                                        Utils.writeLocalRepoFile(getApplicationContext(), Const.TRASH_PATH, Utils.makeDeletedTitle(results_temp.get(0).getTitle()), results_temp.get(0).getContent());
                                }
                            }

                            // Delete record
                            mDatasource.deleteRecordById(id);

                            // Mark updated
                            updated = true;

                            // Status
                            if ((title != null) && (title.length() > 0) && (!Utils.isHiddenFile(title)))
                                mStatusQ.add(title + getResources().getString(R.string.status_deleted_remotely));

                            // Append to sync history
                            Utils.appendSyncLogFile(getApplicationContext(), mLocalRepoPath, title, Const.REV_FILE_REMOVED_SYM + title, mMaxSyncLogFileSize, mMaxSyncLogFileAge);
                        }
                    }

                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }

                // Import file changes
                // Note: if mirror exists, database records are updated by mirror directly
                if (!hasMirror()) {
                    for (int i = 0; i < files.length; i++) {
                        // Move to oversize folder
                        if (files[i].length() > Const.MAX_FILE_SIZE) {
                            Utils.moveFile(getApplicationContext(), mLocalRepoPath + "/", files[i].getName(), mLocalRepoPath + "/" + Const.IMPORT_ERROR_PATH + "/");
                            continue;
                        }

                        // Handle empty file
                        if (files[i].length() == 0) {
                            title = Utils.getTitleFromFileName(getApplicationContext(), files[i]);
                            results = mDatasource.getRecordByTitle(title, DBHelper.COLUMN_MODIFIED, Const.SORT_DESC);  // Most recent record if duplicates exist
                            if (results.size() > 0) {
                                entry = results.get(0);

                                // Files should not be blank if record is not blank, so attempt to repair
                                if (entry.getSize() > 0) {
                                    Utils.writeLocalRepoFile(getApplicationContext(), entry.getTitle(), entry.getContent());
                                    continue;
                                }
                            }
                        }

                        updated = importLocalRepoFile(files[i]);

                        // Update the notification progress bar
                        ++count;

                        // Update the notification progress bar
                        incr = (int) ((i / (float) count) * 100);

                        // Update the on screen progress bar
                        publishProgress(incr);

                        // Escape early if cancel() is called
                        if (isCancelled()) break;
                    }
                }

                // Remove files that are marked deleted
                results = mDatasource.getAllDeletedRecords(DBHelper.COLUMN_TITLE, Const.SORT_ASC);
                for (int i = 0; i < results.size(); i++) {
                    entry = results.get(i);
                    title = entry.getTitle();

                    // Delete database record
                    id = entry.getId();

                    // Sanity check
                    if (title.equals(Const.MULTI_TYPE)) {    // Forbid the reversal of multi file type settings from app UI
                        mDatasource.markRecordDeletedById(id, 0);
                        mStatusQ.add(title + getResources().getString(R.string.status_locked));
                        continue;
                    }

                    // Keep deleted copies
                    if ((mKeepDeletedCopies) && (!title.matches(Const.CONFLICT_PATTERN))) {
                        results_temp = mDatasource.getRecordById(id);
                        if ((results_temp.size() > 0) && (results_temp.get(0).getSize() > 0)) {
                            if (mMirrorUri != null)
                                Utils.writeSpecialSAFFile(getApplicationContext(), mMirrorUri, Const.TRASH_PATH, Utils.makeDeletedTitle(results_temp.get(0).getTitle()), results_temp.get(0).getContent());
                            else
                                Utils.writeLocalRepoFile(getApplicationContext(), Const.TRASH_PATH, Utils.makeDeletedTitle(results_temp.get(0).getTitle()), results_temp.get(0).getContent());
                        }
                    }

                    mDatasource.deleteRecordById(id);

                    // Delete file
                    if (mDatasource.getBasicRecordByTitle(title).size() == 0) {
                        Utils.deleteFile(getApplicationContext(), mLocalRepoPath, title);
                    }

                    if (hasMirror())
                        Utils.deleteSAFSubDirFile(getApplicationContext(), mBackupUri, Const.MIRROR_PATH, title);

                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }
            } catch (Exception e) {
                Log.d(Const.TAG, "nano - RescanLocalRepoTask: caught an exception");
                e.printStackTrace();
            }

            // Log.d(Const.TAG, "nano - RescanLocalRepoTask completed.");

            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            updateStatus(getResources().getString(R.string.status_rescanning), null);

            // If local repo is not set
            if ((mLocalRepoPath == null) || (mLocalRepoPath.length() == 0)) {
                updateStatus(getResources().getString(R.string.error_empty_local_repo_path), mBounce);

                cancel(true);
            } else {
                // Make subdirectory if not available yet
                File dir = new File(mLocalRepoPath);
                if (dir.isDirectory()) {
                    files = Utils.getFileListFromDirectory(getApplicationContext(), dir, mLocalRepoPath);
                } else {
                    updateStatus(Utils.cleanPath(mLocalRepoPath) + " " + getResources().getString(R.string.error_invalid_local_storage_path), mBounce);
                    cancel(true);
                }
            }

            // Show the progress bar
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Long result) {

            // Hide the progress bar when completed
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);

            // Disable swipe refresh
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);

            // Refresh the list if needed
            // Note: mandatory if mirror is used since the update is between mirror and database bypassing local repo
            if ((isStaleList(updated)) || (hasMirror())) {
                refreshList();
            } else
                updateStatus("", null);

            // Update widget
            Intent intent = new Intent(Const.ACTION_UPDATE_WIDGET);
            getApplicationContext().sendBroadcast(intent);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update the regular progress bar
            super.onProgressUpdate(progress[0]);
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onCancelled() {
            Log.i("RescanLocalRepoTask", "onCancelled");
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }

    }

    // Do import local repo
    protected void doImportLocalRepo() {
        // Sanity check
        if (hasMirror()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new ImportLocalRepoTask().executeOnExecutor(CustomAsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new ImportLocalRepoTask().execute();
        }
    }

    // Import local repo changes task
    private class ImportLocalRepoTask extends CustomAsyncTask<Void, Integer, Long> {
        private int count = 0;
        private int incr;
        private File files[];
        private ArrayList<File> updated_files = new ArrayList<>();

        @Override
        protected Long doInBackground(Void... params) {
            long totalSize = 0;
            String title;
            List<DBEntry> results, results_temp;
            DBEntry entry;
            long id;

            try {
                // Make sure the database is open
                resumeDatabase();

                // Remove files that are missing remotely
                results = mDatasource.getAllBasicRecords(DBHelper.COLUMN_TITLE, Const.SORT_ASC);
                for (int i = 0; i < results.size(); i++) {
                    entry = results.get(i);
                    title = entry.getTitle();

                    if (!Utils.fileExists(getApplicationContext(), mLocalRepoPath, title)) {
                        id = entry.getId();

                        // Keep deleted copies
                        if ((mKeepDeletedCopies) && (!title.matches(Const.CONFLICT_PATTERN))) {
                            results_temp = mDatasource.getRecordById(id);
                            if ((results_temp.size() > 0) && (results_temp.get(0).getSize() > 0)) {
                                if (mMirrorUri != null)
                                    Utils.writeSpecialSAFFile(getApplicationContext(), mMirrorUri, Const.TRASH_PATH, Utils.makeDeletedTitle(results_temp.get(0).getTitle()), results_temp.get(0).getContent());
                                else
                                    Utils.writeLocalRepoFile(getApplicationContext(), Const.TRASH_PATH, Utils.makeDeletedTitle(results_temp.get(0).getTitle()), results_temp.get(0).getContent());
                            }
                        }

                        mDatasource.deleteRecordById(id);
                    }

                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }

                // Import file changes
                for (int i = 0; i < files.length; i++) {
                    // Move to oversize folder
                    if (files[i].length() > Const.MAX_FILE_SIZE) {
                        Utils.moveFile(getApplicationContext(), mLocalRepoPath + "/", files[i].getName(), mLocalRepoPath + "/" + Const.IMPORT_ERROR_PATH + "/");
                        continue;
                    }

                    if (importLocalRepoFile(files[i]))
                        updated_files.add(files[i]);

                    // Update the notification progress bar
                    ++count;
                    incr = (int) ((i / (float) count) * 100);

                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }
            } catch (Exception e) {
                Log.e(Const.TAG, "Import local repo task: caught an exception");
                e.printStackTrace();
            }
            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            Log.i(Const.TAG, "ImportLocalRepoTask: onPreExecute");

            // Make subdirectory if not available yet
            File dir = new File(mLocalRepoPath);
            if (dir.isDirectory()) {
                files = Utils.getFileListFromDirectory(getApplicationContext(), dir, mLocalRepoPath);
            } else {
                Log.i(Const.TAG, "ImportLocalRepoTask: local repo is not a directory");
                cancel(true);
            }
        }

        @Override
        protected void onPostExecute(Long result) {
            Log.i(Const.TAG, "ImportLocalRepoTask: onPostExecute");

            // Refresh the list
            refreshList();

            // Disable swipe refresh
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);

            // Notify editor
            if (DisplayDBEntry.display_dbentry != null) {
                for (int i = 0; i < updated_files.size(); i++)
                    DisplayDBEntry.display_dbentry.notifyChange(updated_files.get(i));
            }
        }

        @Override
        protected void onCancelled() {
            Log.i(Const.TAG, "ImportLocalRepoTask: onCancelled");

            // Disable swipe refresh
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);

            updateStatus(getResources().getString(R.string.status_canceled), mBounce);
        }
    }

    // Do import local repo file
    protected void doImportLocalRepoFile(File file) {
        // Sanity check
        if (hasMirror()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new ImportLocalRepoFileTask(file).executeOnExecutor(CustomAsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new ImportLocalRepoFileTask(file).execute();
        }
    }

    // Import monitored file changes task
    private class ImportLocalRepoFileTask extends CustomAsyncTask<Void, Integer, Long> {
        private File file;

        public ImportLocalRepoFileTask(File file) {
            this.file = file;
        }

        @Override
        protected Long doInBackground(Void... params) {
            long totalSize = 0;

            try {
                if (file.length() > Const.MAX_FILE_SIZE)
                    // Move to oversize folder
                    Utils.moveFile(getApplicationContext(), mLocalRepoPath + "/", file.getName(), mLocalRepoPath + "/" + Const.IMPORT_ERROR_PATH + "/");
                else
                    importLocalRepoFile(file);
            } catch (Exception e) {
                Log.e(Const.TAG, "ImportLocalRepoFileTask: Caught an exception");
                e.printStackTrace();
            }
            return totalSize;
        }

        @Override
        protected void onPreExecute() {

            // Make subdirectory if not available yet
            File dir = new File(mLocalRepoPath);
            if (!dir.isDirectory()) {
                Log.i(Const.TAG, "ImportLocalRepoFileTask: local repo is not a directory");
                cancel(true);
            }
        }

        @Override
        protected void onPostExecute(Long result) {

            // Refresh the list
            refreshList();

            // Disable swipe refresh
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);

            // Notify editor
            if (DisplayDBEntry.display_dbentry != null)
                DisplayDBEntry.display_dbentry.notifyChange(file);
        }

        @Override
        protected void onCancelled() {
            // Disable swipe refresh
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    // Recover metadata
    protected void recoverMetadata(DBEntry entry) {
        List<DBEntry> results;
        String app_data_file, title, content;

        String[] rows, columns;
        int count;

        // Determine app data file name
        app_data_file = Utils.makeFileName(getApplicationContext(), Const.APP_DATA_FILE);

        results = mDatasource.getRecordByTitle(app_data_file);
        if (results.size() == 1) {
            content = results.get(0).getContent();

            // Sanity check
            if (!content.contains(entry.getTitle() + Const.SUBDELIMITER))
                return;

            rows = content.split(Const.DELIMITER);
            count = rows.length;

            // For each record
            for (int i = 0; i < count; i++) {
                if (rows[i].length() == 0)
                    continue;

                try {
                    columns = rows[i].split(Const.SUBDELIMITER);

                    // Try to recover metadata
                    title = columns[0];
                    if (title.equals(entry.getTitle())) {
                        // Update star, metadata
                        mDatasource.updateRecord(entry.getId(),
                                Integer.parseInt(columns[1]),
                                columns[3]);

                        return;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Import local repo file
    protected boolean importLocalRepoFile(File file) {
        boolean updated = false;

        try {
            String title, content;
            Date modified;
            Calendar calendar;
            DBEntry entry = null;
            long id = -1;
            int count;
            boolean has_local_priority = false;
            boolean has_remote_priority = false;

            ////////////////////////////////////
            // Make sure the database is open //
            ////////////////////////////////////
            resumeDatabase();

            title = Utils.getTitleFromFileName(this, file);

            List<DBEntry> records = mDatasource.getSimpleRecordByTitle(title, DBHelper.COLUMN_MODIFIED, Const.SORT_DESC);  // Most recent record if duplicates exist
            count = records.size();

            if (count > 0) {
                ////////////////////////////////////
                // Get to the latest local update //
                ////////////////////////////////////

                // Default
                entry = records.get(0);
                id = entry.getId();

                // When local priority tag is defined
                if ((mLocalPriorityTag != null) && (mLocalPriorityTag.length() > 0)) {
                    for (int i = 0; i < count; i++) {
                        if (records.get(i).getMetadata().contains(mLocalPriorityTag)) {
                            entry = records.get(i);
                            id = entry.getId();
                            has_local_priority = true;
                            break;
                        }
                    }
                }
                // When remote priority tag is defined
                else if ((mRemotePriorityTag != null) && (mRemotePriorityTag.length() > 0)) {
                    for (int i = 0; i < count; i++) {
                        if (records.get(i).getMetadata().contains(mRemotePriorityTag)) {
                            entry = records.get(i);
                            id = entry.getId();
                            has_remote_priority = true;
                            break;
                        }
                    }
                }

                ///////////////////////////////////////////////////////////////
                // Read in file when either:                                 //
                // 1. Changes have been detected based on modified time, or; //
                // 2. Duplicate database records exist.                      //
                // 3. Has remote priority tag.                               //
                ///////////////////////////////////////////////////////////////
                if ((entry.getModified().getTime() < file.lastModified()) || (count > 1) || (has_remote_priority)) {

                    // Get data from the file
                    content = Utils.readFile(file);
                    modified = new Date(file.lastModified());

                    // Reduce the chance of reading in the previous version (based on heuristics)
                    if (content.length() < entry.getSize()) {    // Usually previous version has a smaller size
                        calendar = Calendar.getInstance();
                        calendar.setTime(entry.getModified());
                        calendar.add(Calendar.MINUTE, Const.SYNC_NOISE_INTERVAL);

                        // Also unlikely that a remote new version follows right after local version
                        if ((file.lastModified() < calendar.getTimeInMillis()) && (mKeepDeletedCopies)) {
                            // Save local copy in trash before updating it
                            if (mMirrorUri != null)
                                Utils.writeSpecialSAFFile(getApplicationContext(), mMirrorUri, Const.TRASH_PATH, Utils.makeDeletedTitle(entry.getTitle()), entry.getContent());
                            else
                                Utils.writeLocalRepoFile(getApplicationContext(), Const.TRASH_PATH, Utils.makeDeletedTitle(entry.getTitle()), entry.getContent());
                        }
                    }

                    // Update
                    if (content.length() > 0) {
                        // Update file if record has priority
                        if (has_local_priority) {
                            // Do nothing
                        } else {
                            // Update record if record is not protected
                            mDatasource.updateRecordContent(entry.getId(), content, modified);
                            title = entry.getTitle();
                            if ((title != null) && (title.length() > 0) && (!Utils.isHiddenFile(title)))
                                mStatusQ.add(title + getResources().getString(R.string.status_updated_remotely));
                            updated = true;

                            // Append to sync history
                            Utils.appendSyncLogFile(getApplicationContext(), mLocalRepoPath, title, Utils.getRevisionSummaryStr(getApplicationContext(), entry.getContent(), content), mMaxSyncLogFileSize, mMaxSyncLogFileAge);
                        }
                    }
                }

                /////////////////////////////
                // Purge duplicate records //
                /////////////////////////////
                if (count > 1) {
                    for (int i = 0; i < count; i++) {
                        entry = records.get(i);
                        if (entry.getId() != id) {
                            // Delete duplicates from the database only
                            mDatasource.deleteRecord(entry);
                        }
                    }
                }
            } else {
                // Get data from the file
                content = Utils.readFile(file);
                modified = new Date(file.lastModified());

                // Create new
                if (content.length() > 0) {
                    entry = mDatasource.createRecord(title, content, 0, modified, false);
                    recoverMetadata(entry);
                    if ((title != null) && (title.length() > 0) && (!Utils.isHiddenFile(title)))
                        mStatusQ.add(title + getResources().getString(R.string.status_added_remotely));
                    updated = true;
                }
            }
        } catch (Exception e) {
            Log.i(Const.TAG, "importLocalRepoFile: failed");
            e.printStackTrace();
        }

        return updated;
    }

    // Handle SAF backup
    private void handleSAFExport() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title
        if (!Utils.checkMultiWindowMode(this))
            builder.setTitle(R.string.dialog_sd_backup_title);

        builder.setPositiveButton(R.string.dialog_sd_backup_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doSAFBackupRequest(Const.BACKUP_INSTANT_WORK_TAG);
                scheduleBackup();
                return;
            }
        });
        builder.setNegativeButton(R.string.dialog_sd_backup_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                return; // Do nothing
            }
        });

        // Get the AlertDialog from create()
        final AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();
    }

    // Handle SAF import
    private void handleSAFImport() {
        // Pick restore tree
        openDocumentTree(Const.REQUEST_CODE_PICK_RESTORE_URI);
    }

    // Handle SAF mirror
    private void handleSAFMirror() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title
        if (!Utils.checkMultiWindowMode(this))
            builder.setMessage(R.string.dialog_mirror_message).setTitle(R.string.dialog_mirror_title);

        builder.setPositiveButton(R.string.dialog_mirror_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doSAFMirrorTo();
                return;
            }
        });
        builder.setNegativeButton(R.string.dialog_mirror_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doSAFMirrorFrom();
                return;
            }
        });
        builder.setNeutralButton(R.string.dialog_mirror_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                resetLastMirrored();
                doSAFMirrorSync(Const.MIRROR_ONETIME_WORK_TAG, ExistingWorkPolicy.REPLACE);
                return;
            }
        });

        // Get the AlertDialog from create()
        final AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Do SAF import
    protected void doSAFImport() {
        // Sanity check
        if (!mMirrorSafe) {
            showSwipeRefresh();
        }

        if (mRestoreUri != null)
            new ImportSAFTask().execute();
        else
            updateStatus(getResources().getString(R.string.error_no_writable_external_storage), mBounce);
    }

    // Do SAF backup
    protected void doSAFBackup() {
        // Sanity check
        if (!mMirrorSafe) {
            showSwipeRefresh();
        }

        if (mBackupUri != null)
            new BackupSAFTask().execute();
        else
            updateStatus(getResources().getString(R.string.error_no_writable_external_storage), mBounce);
    }

    // Do SAF backup request
    private void doSAFBackupRequest(String tag) {
        // Sanity check
        if (!mMirrorSafe) {
            showSwipeRefresh();
        }

        // Show progress
        showIOProgressBar(null);

        mBackupWorkManager = WorkManager.getInstance(getApplicationContext());

        if (mBackupUri != null) {
            // Build constraints
            Constraints constraints = new Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .setRequiresStorageNotLow(true)
                    .build();

            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder
                    (BackupWorker.class)
                    .setConstraints(constraints)
                    .addTag(tag)
                    .build();

            mBackupWorkManager.enqueueUniqueWork(
                    Const.BACKUP_ONETIME_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    request);

            Log.d(Const.TAG, "nano - Backup job requested");
        }
        else
            updateStatus(getResources().getString(R.string.error_no_writable_external_storage), mBounce);
    }

    // Do SAF mirror to
    protected void doSAFMirrorTo() {
        // Prepare backup path
        mSubDirPath = Const.MIRROR_PATH;

        doSAFBackup();

        // Save time stamp
        Date now = new Date();
        mSharedPreferencesEditor.putLong(Const.MIRROR_TIMESTAMP, now.getTime());
        mSharedPreferencesEditor.apply();
    }

    // Do SAF mirror from
    protected void doSAFMirrorFrom() {
        // Prepare restore path
        mSubDirPath = Const.MIRROR_PATH;
        mRestoreUri = mMirrorUri;

        doSAFImport();
    }

    // Do SAF mirror sync
    private void doSAFMirrorSync(String tag, ExistingWorkPolicy policy) {
        // Sanity check
        if (!hasMirror()) return;

        // Show progress
        showIOProgressBar(null);

        mMirrorWorkManager = WorkManager.getInstance(getApplicationContext());

        // Build constraints
        Constraints constraints = new Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .setRequiresStorageNotLow(true)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder
                (MirrorWorker.class)
                .setConstraints(constraints)
                .addTag(tag)
                .build();

        mMirrorWorkManager.enqueueUniqueWork(
                Const.MIRROR_ONETIME_WORK_NAME,
                policy,
                request);

        // Update widget
        Intent intent = new Intent(Const.ACTION_UPDATE_WIDGET);
        getApplicationContext().sendBroadcast(intent);

        Log.d(Const.TAG, "nano - Mirror job requested");
    }

    // SAF Import task
    private class ImportSAFTask extends CustomAsyncTask<Void, Integer, Long> {
        private ProgressBar progressBar;
        private String status = "";
        private int i = 0, count = 0;
        private int incr;
        private DocumentFile dir, dest_dir, attachment_dir, font_dir, log_dir;

        @Override
        protected Long doInBackground(Void... params) {
            long totalSize = 0;
            boolean overwrite = true;

            Log.i(Const.TAG, "nano - ImportSAFTask: doInBackground");
            try {
                if (mRestoreUri == mMirrorUri) {
                    dir = DocumentFile.fromTreeUri(getApplicationContext(), mBackupUri);
                    dest_dir = Utils.getSAFSubDir(getApplicationContext(), dir, mSubDirPath);
                    overwrite = false;
                }
                else {
                    dest_dir = DocumentFile.fromTreeUri(getApplicationContext(), mRestoreUri);
                }

                for (DocumentFile file : dest_dir.listFiles()) {
                    // Sanity check
                    if (file.isDirectory()) continue;
                    if (Arrays.asList(Const.RESERVED_FOLDER_NAMES).contains(file.getName())) {
                        // Notes with reserved folder names need to be removed
                        file.delete();
                        continue;
                    }

                    importSAFFile(file, overwrite);

                    // Update the notification progress bar
                    ++count;
                    incr = (int) ((i++ / (float) count) * 100);

                    // Update the on screen progress bar
                    publishProgress(incr);

                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }

                // Restore attachments
                attachment_dir = dest_dir.findFile(Const.ATTACHMENT_PATH);
                status += Utils.importFromSAFFolder(getApplicationContext(), attachment_dir, mLocalRepoPath + "/" + Const.ATTACHMENT_PATH, true);

                // Restore fonts
                font_dir = dest_dir.findFile(Const.CUSTOM_FONTS_PATH);
                status += Utils.importFromSAFFolder(getApplicationContext(), font_dir, mLocalRepoPath + "/" + Const.CUSTOM_FONTS_PATH, true);
            } catch (Exception e) {
                Log.i(Const.TAG, "nano - ImportSAFTask: Caught an exception");
                e.printStackTrace();
            }
            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            Log.i(Const.TAG, "nano - ImportSAFTask: onPreExecute");

            // Set mirror status
            mMirrorSafe = false;

            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Long result) {
            Log.i(Const.TAG, "nano - ImportSAFTask: onPostExecute");

            // Refresh the list
            refreshList();

            // Hide the regular progress bar when completed
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);

            // Disable swipe refresh
            if (mSwipeRefreshLayout != null)
                mSwipeRefreshLayout.setRefreshing(false);

            // Status
            status += count + getResources().getString(R.string.status_imported_count) + mCurrentStoragePath;
            updateStatus(status, mPushRightIn);

            // Set mirror status
            mMirrorSafe = true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update the regular progress bar
            super.onProgressUpdate(progress[0]);
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onCancelled() {
            Log.i(Const.TAG, "nano - ImportSAFTask: onCancelled");
            updateStatus(getResources().getString(R.string.status_canceled), mBounce);

            // Set mirror status
            mMirrorSafe = true;
        }
    }

    // Import a file from SAF
    protected void importSAFFile(DocumentFile file, boolean overwrite) {
        try {
            String title, content, line;
            StringBuilder buf = new StringBuilder();
            Calendar cal;
            Date modified;
            DBEntry entry;
            BufferedReader reader;
            FileInputStream in;

            title = Utils.getTitleFromDocumentFileName(getApplicationContext(), file);

            // Sanity check
            if ((title == null) || (title.length() == 0)) return;

            List<DBEntry> records = mDatasource.getRecordByTitle(title);

            // Get data from the file
            content = Utils.readFromSAFFile(getApplicationContext(), file);
            modified = new Date(file.lastModified());

            if (records.size() > 0) {
                // Update existing
                entry = records.get(0);

                if (!overwrite) {
                    // Log.d(Const.TAG, "nano - importSAFFile: checking " + title + " ... entry.getModified: " + entry.getModified() + ", file.lastModified: " + new Date(file.lastModified()));
                    if ((entry.getModified().after(modified)) || (entry.getModified().equals(modified)))  return;
                }

                mDatasource.updateRecord(entry.getId(), entry.getTitle(), content, entry.getStar(), modified, true, entry.getTitle());
            } else {
                // Create new
                mDatasource.createRecord(title, content, 0, modified, true);
            }

            Log.d(Const.TAG, "nano - importSAFFile: " + title + " processed.");
        } catch (Exception e) {
            Log.d(Const.TAG, "nano - importSAFFile: failed");
            e.printStackTrace();
        }
    }

    // SAF Backup task
    private class BackupSAFTask extends CustomAsyncTask<Void, Integer, Long> {
        private ProgressBar progressBar;
        private String status = "";
        private int count;
        private int incr;
        private DocumentFile dir, dest_dir, attachment_dir, font_dir, log_dir;

        @Override
        protected Long doInBackground(Void... params) {
            long totalSize = 0;

            try {
                List<Long> results;

                if (mSubDirPath == Const.MIRROR_PATH) {
                    // Retrieve records modified after last mirror
                    results = mDatasource.getAllActiveRecordsIDsByLastModified(Const.SORT_BY_TITLE, Const.SORT_ASC, mLastMirrored, ">");
                }
                else
                    results = mDatasource.getAllActiveRecordsIDs(DBHelper.COLUMN_MODIFIED, Const.SORT_DESC);

                count = results.size();

                dir = DocumentFile.fromTreeUri(getApplicationContext(), mBackupUri);
                dest_dir = Utils.getSAFSubDir(getApplicationContext(), dir, mSubDirPath);

                for (int i = 0; i < count; i++) {
                    exportSAFFile(dest_dir, results.get(i));

                    // Update the notification progress bar
                    incr = (int) ((i / (float) count) * 100);

                    // Update the on screen progress bar
                    publishProgress(incr);

                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }

                // Backup attachments
                attachment_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, Const.ATTACHMENT_PATH);
                Utils.exportToSAFFolder(getApplicationContext(), new File(mLocalRepoPath + "/" + Const.ATTACHMENT_PATH), attachment_dir, true);

                // Backup fonts
                font_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, Const.CUSTOM_FONTS_PATH);
                Utils.exportToSAFFolder(getApplicationContext(), new File(mLocalRepoPath + "/" + Const.CUSTOM_FONTS_PATH), font_dir, true);

                // Backup multitype file
                if (Utils.fileExists(getApplicationContext(), mLocalRepoPath, Const.MULTI_TYPE))
                    Utils.exportToSAFFile(getApplicationContext(), mLocalRepoPath + "/", Const.MULTI_TYPE, dest_dir);

                // Backup sync log
                // Note: comment out to delegate to mirror to handle
                /*
                if (Utils.fileExists(getApplicationContext(), mLocalRepoPath, Const.SYNC_LOG_FILE)) {
                    Utils.exportToSAFFile(getApplicationContext(), mLocalRepoPath + "/", Const.SYNC_LOG_FILE, dest_dir);

                    // Move log folder to backup
                    // Note: no need to delete source copy as that's managed by purging; destination copy can be manually removed by user
                    log_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, Const.LOG_PATH);
                    Utils.moveToSAFFolder(getApplicationContext(), mBackupUri, new File(mLocalRepoPath + "/" + Const.LOG_PATH), log_dir, false, false);
                }

                // Backup import errors by moving import error folder to backup
                // Note: need to remove source copy as its content will keep growing; destination copy can be manually removed by user
                Utils.moveToSAFFolder(getApplicationContext(), mBackupUri, new File(mLocalRepoPath + "/" + Const.IMPORT_ERROR_PATH), dest_dir, true, false);
                 */
            } catch (Exception e) {
                e.printStackTrace();
            }
            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            Log.i(Const.TAG, "nano - BackupSAFTask: onPreExecute");

            // Set mirror status
            mMirrorSafe = false;

            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Long result) {
            Log.i(Const.TAG, "nano - BackupSAFTask: onPostExecute");

            // Hide the regular progress bar when completed
            progressBar = (ProgressBar) findViewById(R.id.progress_bar);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);

            // Status
            status = count + getResources().getString(R.string.status_notes_exported);
            updateStatus(status, mPushRightIn);

            // Set mirror status
            mMirrorSafe = true;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update the regular progress bar
            super.onProgressUpdate(progress[0]);
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onCancelled() {
            // Set mirror status
            mMirrorSafe = true;
        }
    }

    // Export a file to SAF
    protected void exportSAFFile(DocumentFile dir, Long id) {
        try {
            String title, content;
            Date lastModified;
            DBEntry entry;

            // Get content
            List<DBEntry> results = mDatasource.getRecordById(id);

            if (results.size() > 0) {
                entry = results.get(0);
                title = entry.getTitle();

                // Sanity check
                if (Arrays.asList(Const.RESERVED_FOLDER_NAMES).contains(title)) {
                    // Notes with reserved folder names need to be removed
                    mDatasource.markRecordDeletedById(entry.getId(), 1);
                    return;
                }

                content = entry.getContent();
                lastModified = entry.getModified();

                Utils.writeSAFFile(getApplicationContext(), dir, title, content, lastModified);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Do settings
    private void doSettings() {
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, SetPreferenceActivity.class);
        startActivityForResult(intent, 0);
    }

    // Do long tap status
    protected void doLongTapStatus() {
        if (mStatusMsg.getText().toString().contains(getResources().getString(R.string.status_updated_remotely))) {
            Calendar cal = Calendar.getInstance();

            // Load recently modified items
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1 * Const.RECENCY);    // Load reasonable recent items
            doModifiedFilter(cal.getTime().getTime());

            transitionList();
        }
    }

    // Do double tap status
    protected void doDoubleTapStatus() {
        if (mCriteria == null) {
            doGotoDefaultCustomFilter();
            return;
        }

        if ((mCriteria.equals(Const.MODIFIED_AFTER_FILTER)) || (mCriteria.equals(Const.ACCESSED_AFTER_FILTER)))
            doGotoDefaultDateFilter();
        else
            doGotoDefaultCustomFilter();
    }

    // Do swipe status
    protected void doSwipeStatus(boolean next) {
        if (mCriteria == null) {
            doGotoPrevNextCustomFilter(next);
            return;
        }

        if ((mCriteria.equals(Const.MODIFIED_AFTER_FILTER)) || (mCriteria.equals(Const.ACCESSED_AFTER_FILTER)))
            doGotoPrevNextDateFilter(next);
        else
            doGotoPrevNextCustomFilter(next);
    }

    // Go to default date filter
    protected void doGotoDefaultDateFilter() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        cal.add(Calendar.DAY_OF_YEAR, -1);    // Subtract a day

        // Update date filter
        mDateFilter = cal.getTime().getTime();

        // Refresh list
        refreshList();

        // Update status
        updateStatus(null, mFadeIn);

        // Start animation
        mRecyclerView.startAnimation(mGrowFromMiddle);

        // Scroll to list top
        scrollToTop();
    }

    // Go to previous or next date filter
    protected void doGotoPrevNextDateFilter(boolean next) {
        long filter = mDateFilter;

        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(filter));

        if (next)
            cal.add(Calendar.DAY_OF_YEAR, 1);    // Add a day
        else
            cal.add(Calendar.DAY_OF_YEAR, -1);    // Subtract a day

        // Update date filter
        mDateFilter = cal.getTime().getTime();

        // Refresh list
        refreshList();

        // Update status
        if (next) {
            updateStatus(null, mPushLeftIn);
            mRecyclerView.startAnimation(mPushLeftIn);
        } else {
            updateStatus(null, mPushRightIn);
            mRecyclerView.startAnimation(mPushRightIn);
        }

        // Scroll to list top
        scrollToTop();
    }

    // Get default custom filter
    protected String getDefaultCustomFilter() {
        if ((mCustomFilters != null) && (mCustomFilters.length() > 0)) {
            String[] temp = mCustomFilters.split(";");
            if (temp.length > 0)
                return temp[0];
        }

        return Const.ALL_SYM;
    }

    // Go to default custom filter
    protected void doGotoDefaultCustomFilter() {
        // Setup the list of filters
        String criteria;

        // Resume default filter
        criteria = getDefaultCustomFilter();

        if (criteria != null) {
            // Refresh list
            setCriteria(criteria);

            if (mCriteria.equals(Const.ALL_SYM))
                setCriteria(null);

            refreshList();

            // Update status
            updateStatus(null, mFadeIn);

            // Start animation
            mRecyclerView.startAnimation(mGrowFromMiddle);

            // Scroll to list top
            scrollToTop();
        }
    }

    // Go to previous or next custom filter
    protected void doGotoPrevNextCustomFilter(boolean next) {
        // Setup the list of filters
        String filters = mCustomFilters;
        String criteria;

        String[] items = filters.split(";");

        // Determine current filter
        criteria = mCriteria;
        if (criteria == null)
            criteria = Const.ALL_SYM;


        int idx = Arrays.asList(items).indexOf(criteria);

        // Determine the previous or next filter
        if (idx >= 0) {
            // Boundaries
            if (idx == 0) {
                if (next)
                    idx = idx + 1;
                else
                    idx = items.length - 1;
            } else if (idx == items.length - 1) {
                if (next)
                    idx = 0;
                else
                    idx = idx - 1;
            } else {
                if (next)
                    idx = idx + 1;
                else
                    idx = idx - 1;
            }

            criteria = items[idx];
        } else
            criteria = null;

        if (criteria != null) {
            // Refresh list
            setCriteria(criteria);

            if (mCriteria.equals(Const.ALL_SYM))
                setCriteria(null);

            refreshList();

            // Update status
            if (next) {
                updateStatus(null, mPushLeftIn);
                mRecyclerView.startAnimation(mPushLeftIn);
            } else {
                updateStatus(null, mPushRightIn);
                mRecyclerView.startAnimation(mPushRightIn);
            }

            // Scroll to list top
            scrollToTop();
        }
    }

    // Handle custom filters
    private void handleCustomFilters() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title
        if (!Utils.checkMultiWindowMode(this))
            builder.setMessage(R.string.dialog_custom_filters_message).setTitle(R.string.dialog_custom_filters_title);

        // Setup the list of filters
        String filters = mCustomFilters;

        // Set view
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        builder.setView(view);

        // Setup values
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numbers);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        final String[] items = filters.split(";");
        int idx;

        if (mCriteria == null)
            idx = Arrays.asList(items).indexOf(Const.ALL_SYM);
        else
            idx = Arrays.asList(items).indexOf(mCriteria);

        if (items.length > 0) {
            picker.setMinValue(0);
            picker.setMaxValue(items.length - 1);
            picker.setDisplayedValues(items);

            if (idx >= 0)
                picker.setValue(idx);

            picker.setWrapSelectorWheel(true);  // Wrap around
        }

        // Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_custom_filters_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                setCriteria(items[picker.getValue()]);
                if (mCriteria.equals(Const.ALL_SYM))
                    setCriteria(null);

                refreshList();

                // Scroll to list top
                scrollToTop();

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_custom_filters_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
                return;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.show();

        // Show the dialog
        dialog.show();
    }

    // Handle metadata browse
    private void handleMetadataBrowse() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_metadata_search_message).setTitle(R.string.dialog_metadata_search_title);

        // Add a spinner for metadata browsing
        LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.metadata_list, null);

        final Spinner spinner = (Spinner) layout.findViewById(R.id.metadata_item);

        // Setup autocomlete
        String[] tags = mDatasource.getUniqueMetadata();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, tags);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        // Set default value
        if (tags.length > 0)
            spinner.setSelection(0);

        builder.setView(layout);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_metadata_search_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    String metadata = spinner.getSelectedItem().toString();
                    setCriteria(Const.METADATAONLY + metadata);

                    // Update search history on file if available
                    updateSearchHistory(true);

                    refreshList();

                    // Scroll to list top
                    scrollToTop();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_metadata_search_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
                return;
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.show();

        // 4. Show the dialog
        dialog.show();
    }

    // Handle metadata search
    private void handleMetadataSearch() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_metadata_search_message).setTitle(R.string.dialog_metadata_search_title);

        // Set up text boxes
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add an edit field for metadata search
        final AutoCompleteTextView search_str = new AutoCompleteTextView(this);
        search_str.setImeOptions(EditorInfo.IME_ACTION_SEARCH | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        search_str.setTextColor(ContextCompat.getColor(this, R.color.theme_control_activated));
        search_str.requestFocus();

        // Setup autocomlete
        String[] tags = mDatasource.getUniqueMetadata();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, R.layout.dropdown_list_item, tags);
        search_str.setAdapter(adapter);

        // Glob checkbox
        final SwitchCompat glob = new SwitchCompat(this);
        glob.setText(getResources().getString(R.string.message_glob));

        // Prepare criteria
        if (mCriteria != null) {
            if (mCriteria.startsWith(Const.METADATAREGONLY)) {
                setCriteria(mCriteria);
                search_str.setText(mCriteria.substring(Const.METADATAREGONLY.length()));
                glob.setChecked(true);
            } else if (mCriteria.startsWith(Const.METADATAONLY)) {
                setCriteria(mCriteria);
                search_str.setText(mCriteria.substring(Const.METADATAONLY.length()));
            } else if (Utils.isPreset(mCriteria))
                search_str.setText("");
        }

        search_str.setHint(getResources().getString(R.string.hint_search));
        search_str.setSingleLine();
        search_str.selectAll();

        layout.addView(search_str);
        layout.addView(glob);

        builder.setView(layout);

        // Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_metadata_search_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                boolean use_glob = glob.isChecked();

                // User clicked OK button
                String metadata = search_str.getText().toString().trim();

                // For highlights
                if (use_glob)
                    setCriteria(Const.METADATAREGONLY + metadata);

                else
                    setCriteria(Const.METADATAONLY + metadata);

                // Update search history on file if available
                updateSearchHistory(true);

                refreshList();

                // Scroll to list top
                scrollToTop();

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_metadata_search_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
                return;
            }
        });

        builder.setNeutralButton(R.string.dialog_metadata_search_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                handleMetadataBrowse();
                return;
            }
        });

        // Get the AlertDialog from create()
        final AlertDialog dialog = builder.show();

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        search_str.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    // User clicked OK button
                    boolean use_glob = glob.isChecked();

                    // User clicked OK button
                    String metadata = search_str.getText().toString().trim();

                    // For highlights
                    if (use_glob)
                        setCriteria(Const.METADATAREGONLY + metadata);

                    else
                        setCriteria(Const.METADATAONLY + metadata);

                    // Dismiss dialog
                    dialog.dismiss();

                    refreshList();

                    // Scroll to list top
                    scrollToTop();
                    return true;
                }
                return false;
            }
        });
    }

    // Handle share to
    private synchronized void handleShareTo(String str) {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.dialog_share_to_title);

        // Set up text boxes
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add an edit field for metadata search
        final AutoCompleteTextView search_str = new AutoCompleteTextView(this);
        search_str.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Setup autocomlete
        String[] tags = mDatasource.getAllActiveRecordsTitles(mOrderBy, mOrderDirection);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, R.layout.dropdown_list_item, tags);
        search_str.setAdapter(adapter);
        search_str.requestFocus();

        layout.addView(search_str);

        builder.setView(layout);

        // Set default value
        search_str.setText(mShareToHistory);

        search_str.setHint(getResources().getString(R.string.hint_share_to));
        search_str.setTextColor(ContextCompat.getColor(this, R.color.theme_control_activated));
        search_str.setSingleLine();

        // Select all for easy correction
        search_str.setSelectAllOnFocus(true);

        // Shared content
        final String shared_content = str;

        // Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_share_to_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                String title = search_str.getText().toString().trim();
                Long temp_id = -1L;

                // Check if the record already exists
                ArrayList<DBEntry> results = mDatasource.getRecordByTitle(title);
                if (results.size() == 1) {
                    temp_id = results.get(0).getId();
                    mShareToHistory = title;    // Update history
                } else
                    title = Utils.makeFileName(getApplicationContext(), title);

                // Launch editor
                Intent intent = new Intent(main_activity, DisplayDBEntry.class);
                intent.putExtra(Const.EXTRA_ID, temp_id);
                intent.putExtra(Const.EXTRA_TITLE, title);
                intent.putExtra(Const.EXTRA_SHARED_CONTENT, shared_content);

                // Send the intent
                main_activity.startActivity(intent);

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_share_to_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Do nothing
                return;
            }
        });

        // Get the AlertDialog from create()
        final AlertDialog dialog = builder.show();

        // Show the dialog
        dialog.show();

        // Add some animation
        search_str.startAnimation(mZoomIn);

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        search_str.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    // User clicked OK button
                    String title = search_str.getText().toString().trim();
                    Long temp_id = -1L;

                    // Check if the record already exists
                    ArrayList<DBEntry> results = mDatasource.getRecordByTitle(title);
                    if (results.size() == 1) {
                        temp_id = results.get(0).getId();
                        mShareToHistory = title;    // Update history
                    } else
                        title = Utils.makeFileName(getApplicationContext(), title);

                    // Launch editor
                    Intent intent = new Intent(main_activity, DisplayDBEntry.class);
                    intent.putExtra(Const.EXTRA_ID, temp_id);
                    intent.putExtra(Const.EXTRA_TITLE, title);
                    intent.putExtra(Const.EXTRA_SHARED_CONTENT, shared_content);

                    // Send the intent
                    main_activity.startActivity(intent);

                    // Dismiss dialog
                    dialog.dismiss();

                    return true;
                }
                return false;
            }
        });
    }

    // Show the modified date picker dialog
    public void showModifiedDatePickerDialog() {
        DatePickerModifiedFilterFragment fragment = new DatePickerModifiedFilterFragment();
        fragment.show(this.getSupportFragmentManager(), Const.DATE_PICKER_MODIFIED_FILTER_FRAGMENT_TAG);
    }

    // Show the accessed date picker dialog
    public void showAccessedDatePickerDialog() {
        DatePickerAccessedFilterFragment fragment = new DatePickerAccessedFilterFragment();
        fragment.show(this.getSupportFragmentManager(), Const.DATE_PICKER_ACCESSED_FILTER_FRAGMENT_TAG);
    }

    // Filter by modified date after a certain date
    protected void doModifiedFilter(long filter) {
        setCriteria(Const.MODIFIED_AFTER_FILTER);
        mDateFilter = filter;

        mOrderBy = Const.SORT_BY_MODIFIED;
        mOrderDirection = Const.SORT_DESC;

        mCursor = mDatasource.getAllActiveRecordsByLastModifiedCursor(mOrderBy, mOrderDirection, mDateFilter, ">", mPreviewMode);

        mAdapter.changeCursor(mCursor);
        mAdapter.notifyDataSetChanged();

        showHideRecyclerView();

        // Update status
        updateStatus(null, null);

        // Scroll to list top
        scrollToTop();

        // Update menu
        invalidateOptionsMenu();
    }

    // Filter by accessed after a certain date
    protected void doAccessedFilter(long filter) {
        setCriteria(Const.ACCESSED_AFTER_FILTER);
        mDateFilter = filter;

        mOrderBy = Const.SORT_BY_ACCESSED;
        mOrderDirection = Const.SORT_DESC;

        mCursor = mDatasource.getAllActiveRecordsByLastAccessedCursor(mOrderBy, mOrderDirection, mDateFilter, ">", mPreviewMode);

        mAdapter.changeCursor(mCursor);
        mAdapter.notifyDataSetChanged();

        showHideRecyclerView();

        // Update status
        updateStatus(null, null);

        // Scroll to list top
        scrollToTop();

        // Update menu
        invalidateOptionsMenu();
    }

    // Filter by modified at a nearby location
    protected void doModifiedNearbyFilter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new SearchNearbyTask().executeOnExecutor(CustomAsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new SearchNearbyTask().execute();
        }
    }

    // Search nearby task
    private class SearchNearbyTask extends CustomAsyncTask<Void, Integer, Long> {
        ProgressBar progressBar;

        @Override
        protected Long doInBackground(Void... arg0) {
            long totalSize = 0;

            try {
                // Compute distance for all records
                Location here = Utils.getLocation(getApplicationContext());
                ArrayList<DBEntry> results = mDatasource.getAllActiveContentlessRecords(mOrderBy, mOrderDirection);
                DBEntry entry;
                for (int i = 0; i < results.size(); i++) {
                    entry = results.get(i);
                    mDatasource.updateRecordDistance(entry.getId(), Utils.getDistance(here, entry.getLatitude(), entry.getLongitude()));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            // Show the progress bar
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.VISIBLE);

            setCriteria(Const.MODIFIED_NEARBY_FILTER);
            mOrderBy = Const.SORT_BY_TITLE;
            mOrderDirection = Const.SORT_ASC;
        }

        @Override
        protected void onPostExecute(Long result) {
            // Hide the progress bar when completed
            progressBar.setVisibility(View.GONE);

            // Refresh the list
            refreshList();

            // Scroll to list top
            scrollToTop();

            // Update status
            updateStatus(null, null);
        }

        @Override
        protected void onCancelled() {

        }
    }

    // Add new note
    private void doAdd() {
        Intent intent = new Intent(getApplication(), DisplayDBEntry.class);
        intent.putExtra(Const.EXTRA_ID, -1L);

        // Send the intent
        startActivity(intent);
    }

    // Handle advanced search
    private void handleAdvancedSearch() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set title
        if (!Utils.checkMultiWindowMode(this))
            builder.setTitle(R.string.dialog_boolean_search_title);

        // Add an edit field for search text
        TextInputLayout layout = (TextInputLayout) LayoutInflater.from(this).inflate(R.layout.floating_hint_input, null);
        String hint = getResources().getString(R.string.dialog_boolean_search_message);
        layout.setHint(hint);

        final EditText search_str = (EditText) layout.findViewById(R.id.input_str);
        search_str.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        search_str.setTextColor(ContextCompat.getColor(this, R.color.theme_control_activated));
        search_str.setSingleLine();
        search_str.requestFocus();

        // Set up glob
        final SwitchCompat glob = new SwitchCompat(this);
        glob.setText(getResources().getString(R.string.message_glob));

        // Prepare criteria
        String temp = "";
        if ((mCriteria != null) && (mCriteria.length() > 0)) {
            if (mCriteria.startsWith(Const.ANDGQUERY)) {
                temp = mCriteria.substring(Const.ANDGQUERY.length());
                glob.setChecked(true);
            } else if (mCriteria.startsWith(Const.ANDQUERY))
                temp = mCriteria.substring(Const.ANDQUERY.length());

            else if (mCriteria.startsWith(Const.ORGQUERY)) {
                temp = mCriteria.substring(Const.ORGQUERY.length());
                glob.setChecked(true);
            } else if (mCriteria.startsWith(Const.ORQUERY))
                temp = mCriteria.substring(Const.ORQUERY.length());

            else
                temp = mCriteria;

            // Non-boolean criteria
            if (Utils.isPreset(temp))
                search_str.setText("");
            else
                search_str.setText(temp);
        }

        search_str.selectAll();
        layout.addView(glob);
        builder.setView(layout);

        // Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_boolean_search_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                boolean use_glob = glob.isChecked();
                String mode, qry;

                if (use_glob)
                    mode = Const.ANDGQUERY;
                else
                    mode = Const.ANDQUERY;

                qry = search_str.getText().toString().trim();
                if ((Utils.validateBooleanSearchCriteria(qry)) || (use_glob))
                    qry = mode + qry;

                doSearch(qry);

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_boolean_search_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        builder.setNeutralButton(R.string.dialog_boolean_search_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                boolean use_glob = glob.isChecked();
                String mode, qry;

                if (use_glob)
                    mode = Const.ORGQUERY;
                else
                    mode = Const.ORQUERY;

                qry = search_str.getText().toString().trim();
                if ((Utils.validateBooleanSearchCriteria(qry)) || (use_glob))
                    qry = mode + qry;

                doSearch(qry);

                return;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.show();

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // Safe to refresh list
        mRefreshListSafe = true;
    }

    // Do search
    protected void doSearch(String qry) {
        setCriteria(qry);

        // Update search history
        updateSearchHistory(true);

        // Refresh list with the new criteria
        refreshList();

        // Scroll to list top
        scrollToTop();
    }

    // Do clear search history
    protected void doClearSearchHistory() {
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
        suggestions.clearHistory();

        // Reset search history on file if available
        updateSearchHistory(false);

        // Update status
        updateStatus(getResources().getString(R.string.status_search_history_cleared), mPushRightIn);
    }

    // Do clear location data
    protected void doClearLocationData() {
        mDatasource.clearAllCoordinates();

        // Update status
        updateStatus(getResources().getString(R.string.status_location_data_cleared), mPushRightIn);
    }

    // Help
    private void doHelp() {
        Uri uri = Uri.parse(Const.HELP_URL);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }

    // Handle about
    private void handleAbout() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        String message = "By App Mind\n\n";
        message += Const.BEAR_SYM + "\n\n";
        message += Const.OFFICIAL_URL + "\n\n";

        message += getResources().getString(R.string.message_auto_backup_log) + mSharedPreferences.getString(Const.AUTO_BACKUP_LOG, getResources().getString(R.string.message_auto_backup_log_empty));
        message += "\n\n";
        message += getResources().getString(R.string.message_auto_mirror_log) + mSharedPreferences.getString(Const.AUTO_MIRROR_LOG, getResources().getString(R.string.message_auto_mirror_log_empty));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {    // For pre Android Oreo devices only
            if ((Utils.hasPackage(this, Const.CONNECTOR_PACKAGE)) || (Utils.hasPackage(this, Const.CONNECTORPLUS_PACKAGE))) {
                message += Const.BLANK_LINE;
                message += getResources().getString(R.string.message_sync_log) + mSharedPreferences.getString(Const.SYNC_LOG, getResources().getString(R.string.message_sync_log_empty));
            }
        }

        message += Const.BLANK_LINE;
        message += getResources().getString(R.string.message_storage_usage) + Utils.readableFileSize(mDatasource.getSize());

        message += Const.HORIZONTAL_LINE;
        message += "Chinese (Simplified) translation kindly provided by: 王子菠萝油, updated by: TyanBoot";

        message += Const.HORIZONTAL_LINE;
        message += "Japanese translation kindly provided by: gnuhead-chieb";

        message += Const.HORIZONTAL_LINE;
        message += "Japanese manual translation kindly provided by: Hiroyuki Sekihara";

        message += Const.HORIZONTAL_LINE;
        message += "About TextDrawable:\n\n";
        message += "The MIT License (MIT)\n\nCopyright (c) 2014, Amulya Khare\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";

        message += Const.HORIZONTAL_LINE;
        message += "About RecyclerView-FastScroll:\n\n";
        message += "Copyright (C) 2016 Tim Malseed\n\nLicensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About DirectoryChooserDialog:\n\n";
        message += "Gregory Shpitalnik\n\nLicensed under The Code Project Open License (CPOL) 1.02";

        message += Const.HORIZONTAL_LINE;
        message += "About Line Awesome:\n\n";
        message += "Licence: https://icons8.com/good-boy-license";

        message += Const.HORIZONTAL_LINE;
        message += "About Roboto Slab:\n\n";
        message += "Copyright Christian Robertson\n\nLicensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About Droid Sans Mono:\n\n";
        message += "Copyright Steve Matteson\n\nLicensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About Guava:\n\n";
        message += "Copyright 2009 Google Inc.\n\nLicensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About java-diff-utils:\n\n";
        message += "Copyright Dmitry Naumenko\n\nLicensed under the Apache License, Version 1.1 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-1.1\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About Apache Commons IO:\n\n";
        message += "Licensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About Prettytime:\n\n";
        message += "Copyright [2011] Lincoln Baxter, III \n\nLicensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";
        message += "\n\nThis product includes software developed at OCPSoft (https://ocpsoft.org/).";

        message += Const.HORIZONTAL_LINE;
        message += "About Prettify:\n\n";
        message += "Copyright (C) 2006 Google Inc.\n\nLicensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About Tomorrow Theme:\n\n";
        message += "The MIT License (MIT)\n\nCopyright (c) 2013\n\nChris Kempson,  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";

        message += Const.HORIZONTAL_LINE;
        message += "About jQuery:\n\n";
        message += "Copyright jQuery Foundation and other contributors, https://jquery.org/\n\nThis software consists of voluntary contributions made by many individuals. For exact contribution history, see the revision history available at https://github.com/jquery/jquery\n\nThe following license applies to all parts of this software except as documented below:\n\n====\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the\n" +
                "\"Software\"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED \"AS IS\", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF\n" +
                "MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";

        message += Const.HORIZONTAL_LINE;
        message += "About MathJax:\n\n";
        message += "Copyright (c) 2009-2013 The MathJax Consortium.\n\nLicensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About Math.js:\n\n";
        message += "Copyright (c) 2013-2015 Jos de Jong wjosdejong@gmail.com.\n\nLicensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttp://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About PageDown:\n\n";
        message += "A javascript port of Markdown, as used on Stack Overflow.\n\nLargely based on showdown.js by John Fraser (Attacklab).\n\n";
        message += "Original Markdown Copyright (c) 2004-2005 John Gruber <https://daringfireball.net/projects/markdown/>\n\n";
        message += "Original Showdown code copyright (c) 2007 John Fraser\n\n";
        message += "Modifications and bugfixes (c) 2009 Dana Robinson\nModifications and bugfixes (c) 2009-2013 Stack Exchange Inc.\n\n";
        message += "Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\n";
        message += "The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\n";
        message += "THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";

        message += Const.HORIZONTAL_LINE;
        message += "About PageDown-Extra:\n\n";
        message += "Javascript Markdown Extra Extensions for Pagedown\nCopyright (c) 2012-2013 Justin McManus\n\n";
        message += "PHP Markdown & Extra\n";
        message += "Copyright (c) 2004-2013 Michel Fortin\n";
        message += "All rights reserved.\n\n";
        message += "Original Markdown\n";
        message += "Copyright (c) 2004-2006 John Gruber\n";
        message += "All rights reserved.\n\n";
        message += "Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n\n";
        message += "Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.\n\n";
        message += "Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.\n\n";
        message += "Neither the name \"PHP Markdown\" nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.\n\n";
        message += "This software is provided by the copyright holders and contributors 'as is' and any express or implied warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no event shall the copyright owner or contributors be liable for any direct, indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the possibility of such damage.";

        message += Const.HORIZONTAL_LINE;
        message += "About Vue.js:\n\n";
        message += "The MIT License (MIT)\n\nCopyright (c) 2014-2020 Evan You\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";

        message += Const.HORIZONTAL_LINE;
        message += "About Brython:\n\n";
        message += "Copyright (c) 2012, Pierre Quentel pierre.quentel@gmail.com\n\n";
        message += "All rights reserved.\n\n";
        message += "Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n\n";
        message += "Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.\n" +
                "Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.\n" +
                "Neither the name of the <ORGANIZATION> nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.\n\n";
        message += "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS \"AS IS\" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.\n";

        message += Const.HORIZONTAL_LINE;
        message += "About Mermaid:\n\n";
        message += "The MIT License (MIT)\n\nCopyright (c) 2014 - 2021 Knut Sveidqvist\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";

        message += Const.HORIZONTAL_LINE;
        message += "About Typograms:\n\n";
        message += "google/typograms is licensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        message += Const.HORIZONTAL_LINE;
        message += "About leaflet.bouncemarker:\n\n";
        message += "The MIT License (MIT)\n\nCopyright (c) 2014 Maxime Hadjinlian\n\nPermission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 'Software'), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:\n\nThe above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.\n\nTHE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.";

        message += Const.HORIZONTAL_LINE;
        message += "About small-n-flat:\n\n";
        message += "Copyright Pao Media\n\nCC0 1.0 Universal";

        message += Const.HORIZONTAL_LINE;
        message += "About AppIntro:\n\n";
        message += "Copyright Paolo Rotolo\n\nLicensed under the Apache License, Version 2.0 (the \"License\");  you may not use this file except in compliance with the License.  You may obtain a copy of the License at\n\nhttps://www.apache.org/licenses/LICENSE-2.0\n\nUnless required by applicable law or agreed to in writing, software distributed under the License is distributed on an \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.";

        // Linkify the message
        final SpannableString message_linkified = new SpannableString(message);
        Linkify.addLinks(message_linkified, Linkify.WEB_URLS);

        builder.setMessage(message_linkified).setTitle(R.string.dialog_about_title);

        builder.setPositiveButton(R.string.dialog_about_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.show();

        // Center the text
        Typeface font = FontCache.getFromAsset(this, "RobotoMono-Light.ttf");
        TextView text = (TextView) dialog.findViewById(android.R.id.message);
        text.setGravity(Gravity.CENTER);
        text.setTextSize(10);
        text.setTypeface(font);
        text.setMovementMethod(LinkMovementMethod.getInstance());

        // Show the dialog
        dialog.show();
    }

    // Check local repo path
    private synchronized void checkLocalRepoPath() {
        if (((mLocalRepoPath == null) || (mLocalRepoPath.length() == 0)) && (!Utils.checkWriteLock())) {
            // Lock settings
            Utils.acquireWriteLock();

            // Send to settings
            if (Build.VERSION.SDK_INT < 28)
                handleEmptyLocalRepoPath();
            else if (Build.VERSION.SDK_INT < 33)
                handleEmptyLocalRepoPathSimplified();
            else
                // Permission no longer required, defaulted to scope storage
                setDefaultLocalRepoPath();
        }
    }

    // Check backup uri
    private void checkBackupUri() {
        if ((mIncrementalBackup) && (mBackupUri.toString().length() == 0))
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_empty_backup_uri), Toast.LENGTH_LONG).show();
    }

    // Check math url
    private void checkMathUrl() {
        if (mMathUrl.equals(Const.UNSET_URL))
            handleUnsetMathUrl();
    }

    // Verify storage permission
    private void verifyStoragePermission() {
        if ((Build.VERSION.SDK_INT >= 23) && (Build.VERSION.SDK_INT < 29)) {
            // Handle runtime permissions
            if (!((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)))
                getStoragePermission(getApplicationContext());
        }
    }

    // Verify notification permission
    private void verifyNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            // Handle runtime permissions
            if (!((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)))
                getNotificationPermission(getApplicationContext());
        }
    }

    // Handle empty local repo path
    private void handleEmptyLocalRepoPath() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_empty_local_repo_message).setTitle(R.string.dialog_empty_local_repo_title);

        builder.setPositiveButton(R.string.dialog_empty_local_repo_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (Build.VERSION.SDK_INT >= 23) {
                    // Handle runtime permissions
                    if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
                        doSettings();
                    else
                        getStoragePermission(getApplicationContext());
                } else {
                    // User clicked OK button
                    doSettings();
                }
            }
        });
        builder.setNegativeButton(R.string.dialog_empty_local_repo_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (Build.VERSION.SDK_INT >= 23) {
                    // Handle runtime permissions
                    if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
                        setDefaultLocalRepoPath();
                    else
                        getStoragePermission(getApplicationContext());
                } else {
                    // User clicked OK button
                    setDefaultLocalRepoPath();
                }

                // Reset status
                updateStatus(null, null);
            }
        });
        builder.setNeutralButton(R.string.dialog_empty_local_repo_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Unlock settings
                Utils.releaseWriteLock();

                finish();
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Handle empty local repo path simplified version (default to app-specific folder)
    private void handleEmptyLocalRepoPathSimplified() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_empty_local_repo_message).setTitle(R.string.dialog_empty_local_repo_title);

        builder.setPositiveButton(R.string.dialog_empty_local_repo_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (Build.VERSION.SDK_INT >= 23) {
                    // Handle runtime permissions
                    if ((ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
                        setDefaultLocalRepoPath();
                    else
                        getStoragePermission(getApplicationContext());
                } else {
                    // User clicked OK button
                    setDefaultLocalRepoPath();
                }

                // Reset status
                updateStatus(null, null);
            }
        });
        builder.setNegativeButton(R.string.dialog_empty_local_repo_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Unlock settings
                Utils.releaseWriteLock();

                finish();
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Handle unset math url
    private void handleUnsetMathUrl() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_unset_math_url_message).setTitle(R.string.dialog_unset_math_url_title);

        builder.setPositiveButton(R.string.dialog_unset_math_url_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mMathUrl = getResources().getString(R.string.pref_math_url_default);

                mSharedPreferencesEditor.putString(Const.PREF_MATH_URL, mMathUrl);
                mSharedPreferencesEditor.commit();
            }
        });
        builder.setNegativeButton(R.string.dialog_unset_math_url_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mMathUrl = Const.HTTPS_SYM;

                mSharedPreferencesEditor.putString(Const.PREF_MATH_URL, mMathUrl);
                mSharedPreferencesEditor.commit();
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Voice memo
    void doVoiceMemo(Intent intent) {
        SimpleDateFormat sdf = new SimpleDateFormat(Const.DIRPATH_DATE_FORMAT, Locale.getDefault());
        Date now = new Date();

        String title = Utils.makeFileName(getApplicationContext(), Const.VOICE_MEMO_TITLE);
        String content = intent.getStringExtra(Intent.EXTRA_TEXT);

        if ((title != null) && (content != null)) {
            try {
                // Add/update record
                DBEntry entry;

                // Insert time stamp
                String temp = Const.NEWLINE + getSystemDateFormat(getApplicationContext(), Locale.getDefault()).format(now) + Utils.getSystemTimeFormat(getApplicationContext(), Locale.getDefault()).format(now);
                temp += Const.NEWLINE + Const.NEWLINE + content;
                temp += Const.NEWLINE + Const.LINE_SYM + Const.NEWLINE;
                content = temp;

                ArrayList<DBEntry> results = mDatasource.getRecordByTitle(title);
                if (results.size() == 1) {
                    entry = results.get(0);

                    StringBuilder sb = new StringBuilder();
                    sb.append(entry.getContent());
                    sb.append(content);

                    mDatasource.updateRecord(entry.getId(), entry.getTitle(), sb.toString(), entry.getStar(), now, true, entry.getTitle());
                    Toast.makeText(getApplicationContext(), title + getResources().getString(R.string.status_updated_remotely), Toast.LENGTH_SHORT).show();
                } else {
                    entry = mDatasource.createRecord(title, content, 0, now, true);
                    Toast.makeText(getApplicationContext(), title + getResources().getString(R.string.status_added_remotely), Toast.LENGTH_SHORT).show();
                }

                // Store location
                if (mLocationAware) {
                    Location location = Utils.getLocation(getApplicationContext());
                    mDatasource.updateRecordCoordinates(entry.getId(), location.getLatitude(), location.getLongitude());
                }

                // Go right to voice memo
                setCriteria(Const.TITLEONLY + title);

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), title + getResources().getString(R.string.error_create_path), Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Show I/O progress
    protected void showIOProgressBar(String status) {
        updateStatus(status, null);
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
    }

    // Hide I/O progress
    protected void hideIOProgressBar() {
        ProgressBar progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
        progressBar.setVisibility(View.GONE);
    }

    // Show swipe refresh
    protected void showSwipeRefresh() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(true);
    }

    // Hide swipe refresh
    protected void hideSwipeRefresh() {
        if (mSwipeRefreshLayout != null)
            mSwipeRefreshLayout.setRefreshing(false);
    }

    // Popup menu handling
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_sort_by_title:
                doSort(Const.SORT_BY_TITLE, Const.SORT_ASC, Const.SORT_DESC);
                return true;
            case R.id.menu_sort_by_date:
                doSort(Const.SORT_BY_MODIFIED, Const.SORT_DESC, Const.SORT_ASC);
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // menuItem.setChecked(true);

        switch (menuItem.getItemId()) {
            case R.id.menu_custom_filters:
                mCurrentSelectedPosition = 0;
                mDrawerLayout.closeDrawers();
                handleCustomFilters();
                return true;
            case R.id.menu_modified_after:
                mCurrentSelectedPosition = 1;
                mDrawerLayout.closeDrawers();
                showModifiedDatePickerDialog();
                return true;
            case R.id.menu_accessed_recently:
                mCurrentSelectedPosition = 2;
                mDrawerLayout.closeDrawers();
                showAccessedDatePickerDialog();
                return true;
            case R.id.menu_modified_nearby:
                mCurrentSelectedPosition = 3;
                mDrawerLayout.closeDrawers();
                doModifiedNearbyFilter();
                return true;
            case R.id.menu_metadata:
                mCurrentSelectedPosition = 4;
                handleMetadataSearch();
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.menu_backup_app_data:
                mDrawerLayout.closeDrawers();
                doAppDataBackup();
                return true;
            case R.id.menu_restore_app_data:
                mDrawerLayout.closeDrawers();
                doAppDataRestore();
                return true;
            case R.id.menu_about:
                mDrawerLayout.closeDrawers();
                handleAbout();
                return true;
            case R.id.menu_help:
                mDrawerLayout.closeDrawers();
                doHelp();
                return true;
            case R.id.menu_settings:
                mDrawerLayout.closeDrawers();
                doSettings();
                return true;
        }

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_F:
                if (event.isCtrlPressed()) {
                    if (mSearchView.isIconified()) {
                        mSearchView.setIconified(false);
                        mSearchView.requestFocus();
                    }
                }
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    // Update search history
    protected void updateSearchHistory(boolean append) {
        final boolean append_mode = append;

        try {
            Thread t = new Thread() {
                public void run() {
                    // Update search history on file
                    DBEntry entry;
                    List<DBEntry> results;
                    String content, search_term, search_history_file;
                    int overflow;

                    // Determine search history file name
                    search_history_file = Utils.makeFileName(getApplicationContext(), Const.SEARCH_HISTORY_FILE);

                    results = mDatasource.getRecordByTitle(search_history_file);
                    if (results.size() == 1) {
                        entry = results.get(0);
                        if (append_mode) {
                            search_term = mCriteria + Const.DELIMITER;
                            content = entry.getContent();

                            // Automatic size control for search history
                            if (content.length() > Const.MAX_SEARCH_HISTORY_SIZE) {
                                // Discard the oldest search history
                                overflow = content.indexOf(Const.DELIMITER, content.length() - Const.SAFE_SEARCH_HISTORY_SIZE);
                                content = content.substring(overflow);
                            }

                            // Remove previous appearance of search term to keep it recent
                            if (content.contains(Const.DELIMITER + search_term))
                                content = content.replaceAll(Const.DELIMITER + Pattern.quote(search_term), Const.DELIMITER);

                            content = content + search_term;
                        } else
                            content = "";

                        mDatasource.updateRecord(entry.getId(), search_history_file, content, entry.getStar(), new Date(), true, search_history_file);
                    }

                    // Update recent suggestions
                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(getApplicationContext(), SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);
                    suggestions.saveRecentQuery(mCriteria, null);
                }
            };
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Restore search history
    protected void restoreSearchHistory() {
        SearchRecentSuggestions suggestions;
        List<DBEntry> results;
        String content, search_history_file;
        String[] rows;
        int count;

        try {
            // Determine search history file name
            search_history_file = Utils.makeFileName(getApplicationContext(), Const.SEARCH_HISTORY_FILE);
            results = mDatasource.getRecordByTitle(search_history_file);
            if (results.size() == 1) {
                // Update search history
                suggestions = new SearchRecentSuggestions(this, SuggestionProvider.AUTHORITY, SuggestionProvider.MODE);

                content = results.get(0).getContent();

                rows = content.split(Const.DELIMITER);
                count = rows.length;

                // For each history entry
                for (int i = 0; i < count; i++) {
                    if (rows[i].length() == 0)
                        continue;

                    suggestions.saveRecentQuery(rows[i], null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Refresh dynamic shortcuts
    protected void refreshDynamicShortcuts() {
        // Reset shortcuts
        ShortcutManagerCompat.removeAllDynamicShortcuts(getApplicationContext());

        // Sanity check
        if (mLauncherTags.length() == 0)
            return;

        String parts[] = mLauncherTags.split(Const.LAUNCHER_TAG_DELIM);
        Intent intent;
        ShortcutInfoCompat shortcut;

        for (int i = 0; i < parts.length; i++) {
            // Create shortcuts
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setAction(Intent.ACTION_SEARCH);
            intent.putExtra(SearchManager.QUERY, parts[i]);

            shortcut = new ShortcutInfoCompat.Builder(getApplicationContext(), parts[i])
                    .setShortLabel(parts[i])
                    .setIcon(IconCompat.createWithResource(getApplicationContext(), R.drawable.ic_launcher))
                    .setIntent(intent)
                    .build();

            ShortcutManagerCompat.pushDynamicShortcut(getApplicationContext(), shortcut);
        }
    }

    // Do app data restore
    protected void doAppDataRestore() {
        try {
            new RestoreAppDataTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Restore app data task
    private class RestoreAppDataTask extends CustomAsyncTask<Void, Integer, Long> {
        private ProgressBar progressBar;
        private String status;

        @Override
        protected Long doInBackground(Void... arg0) {
            long totalSize = 0;
            List<DBEntry> results;
            DBEntry entry;
            String title, content, app_data_file, app_settings_file;
            String[] rows, columns;
            int count;

            // 1. Restore app data
            app_data_file = Utils.makeFileName(getApplicationContext(), Const.APP_DATA_FILE);
            app_settings_file = Utils.makeFileName(getApplicationContext(), Const.APP_SETTINGS_FILE);

            results = mDatasource.getRecordByTitle(app_data_file);
            if (results.size() == 1) {
                content = results.get(0).getContent();

                rows = content.split(Const.DELIMITER);
                count = rows.length;

                // For each record
                for (int i = 0; i < count; i++) {
                    if (rows[i].length() == 0)
                        continue;

                    try {
                        columns = rows[i].split(Const.SUBDELIMITER);

                        // Try to recover metadata
                        if ((Utils.fileNameAsTitle(getApplicationContext())) && (!columns[0].contains(".")))
                            // From without file extension to with file extension
                            title = columns[0] + ".txt";
                        else if ((!Utils.fileNameAsTitle(getApplicationContext())) && (columns[0].contains(".")))
                            // From with file extension to without file extension
                            title = columns[0].substring(0, columns[0].lastIndexOf("."));
                        else
                            title = columns[0];

                        results = mDatasource.getContentlessRecordByTitle(title);
                        if (results.size() == 1) {
                            entry = results.get(0);

                            // Update star, pos, metadata, accessed time, created time, modified time, latitude, longitude
                            mDatasource.updateRecord(entry.getId(),
                                    Integer.parseInt(columns[1]),
                                    Integer.parseInt(columns[2]),
                                    columns[3],
                                    Long.parseLong(columns[4]),
                                    Long.parseLong(columns[7]),
                                    Long.parseLong(columns[8]),
                                    Double.parseDouble(columns[5]),
                                    Double.parseDouble(columns[6]));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    publishProgress((int) ((i / (float) count) * 100));

                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }

                // 2. Restore settings
                results = mDatasource.getRecordByTitle(app_settings_file);
                if (results.size() == 1) {
                    content = results.get(0).getContent();

                    rows = content.split(Const.DELIMITER);
                    count = rows.length;

                    // For each settings
                    for (int i = 0; i < count; i++) {
                        if (rows[i].length() == 0)
                            continue;

                        try {
                            if (rows[i].contains(Const.SETTINGS_DELIMITER)) {
                                String[] parts = rows[i].split(Pattern.quote(Const.SETTINGS_DELIMITER));

                                // Skip log
                                if ((parts[0].equals(Const.AUTO_BACKUP_LOG)) || (parts[0].equals(Const.SYNC_LOG)))
                                    continue;

                                // Skip local repository path (should only be set via UI)
                                if (parts[0].equals(Const.PREF_LOCAL_REPO_PATH))
                                    continue;

                                if (Arrays.asList(Const.BOOL_PREFS).contains(parts[0])) {
                                    mSharedPreferencesEditor.putBoolean(parts[0], Boolean.parseBoolean(parts[1]));
                                    mSharedPreferencesEditor.commit();
                                } else if (Arrays.asList(Const.ALL_PREFS).contains(parts[0])) {
                                    mSharedPreferencesEditor.putString(parts[0], parts[1]);
                                    mSharedPreferencesEditor.commit();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        publishProgress((int) ((i / (float) count) * 100));

                        // Escape early if cancel() is called
                        if (isCancelled()) break;
                    }

                    // Backup the settings
                    mBackupManager.dataChanged();
                }

                // 3. Restore search history if available
                restoreSearchHistory();

                // 4. Restore dynamic shortcuts
                refreshDynamicShortcuts();

                status = getResources().getString(R.string.status_app_data_restored);
            } else
                status = getResources().getString(R.string.error_app_data_not_found);

            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            updateStatus(getResources().getString(R.string.status_app_data_restoring), null);

            // Show the progress bar
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update the progress bar
            super.onProgressUpdate(progress[0]);
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Long result) {
            refreshList();
            updateStatus(status, mBounce);

            // Hide the progress bar when completed
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);

            main_activity.onRestoreAppDataTaskFinished();
        }

        @Override
        protected void onCancelled() {
            // Hide the progress bar when completed
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    // Finish up app data restoration
    public void onRestoreAppDataTaskFinished() {
        // Reload preferences
        loadPref();

        // schedule backup
        if (mIncrementalBackup)
            scheduleBackup();
        else
            cancelBackup();
    }

    // Do app data backup
    protected void doAppDataBackup() {
        try {
            new BackupAppDataTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Backup app data task
    private class BackupAppDataTask extends CustomAsyncTask<Void, Integer, Long> {
        private ProgressBar progressBar;

        @Override
        protected Long doInBackground(Void... arg0) {
            long totalSize = 0;
            List<DBEntry> results;
            DBEntry entry;
            String content = "", app_data_file, app_settings_file;
            int count;

            // 1. Remove app data conflicts
            mDatasource.removeAppDataConflicts();

            // 2. Backup metadata
            results = mDatasource.getAllActiveContentlessRecords(mOrderBy, mOrderDirection);
            count = results.size();

            // Fill up the metadata string
            for (int i = 0; i < count; i++) {
                entry = results.get(i);
                content = content + entry.getTitle() + Const.SUBDELIMITER + entry.getStar() + Const.SUBDELIMITER + entry.getPos() + Const.SUBDELIMITER + entry.getMetadata() + Const.SUBDELIMITER + entry.getAccessed().getTime() + Const.SUBDELIMITER + entry.getLatitude() + Const.SUBDELIMITER + entry.getLongitude();
                content = content + Const.SUBDELIMITER + entry.getCreated().getTime() + Const.SUBDELIMITER + entry.getModified().getTime();
                content = content + Const.SUBDELIMITER + Const.SUBDELIMITER + Const.DELIMITER;
                publishProgress((int) ((i / (float) count) * 100));

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            // Save to metadata file
            app_data_file = Utils.makeFileName(getApplicationContext(), Const.APP_DATA_FILE);
            app_settings_file = Utils.makeFileName(getApplicationContext(), Const.APP_SETTINGS_FILE);

            results = mDatasource.getRecordByTitle(app_data_file);

            if (results.size() == 1) {
                entry = results.get(0);
                mDatasource.updateRecord(entry.getId(), app_data_file, content, entry.getStar(), null, true, app_data_file);
            } else if (results.size() == 0) {
                mDatasource.createRecord(app_data_file, content, 0, null, true);
            }

            // 3. Backup settings
            Map<String, ?> prefs = new TreeMap<>(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getAll());
            String key, value;
            content = "";
            for (Map.Entry<String, ?> pref : prefs.entrySet()) {
                key = pref.getKey();
                value = pref.getValue().toString();

                // Skip log
                if ((key.equals(Const.AUTO_BACKUP_LOG)) || (key.equals(Const.SYNC_LOG)) || (!key.startsWith(Const.PACKAGE)))
                    continue;

                // Skip local repository path (should only be saved via UI)
                if (key.equals(Const.PREF_LOCAL_REPO_PATH))
                    continue;

                if (Arrays.asList(Const.ALL_PREFS).contains(key))
                    content += key + Const.SETTINGS_DELIMITER + value + Const.DELIMITER;
            }

            // Save to settings file
            results = mDatasource.getRecordByTitle(app_settings_file);
            if (results.size() == 1) {
                entry = results.get(0);
                mDatasource.updateRecord(entry.getId(), app_settings_file, content, entry.getStar(), null, true, app_settings_file);
            } else if (results.size() == 0) {
                mDatasource.createRecord(app_settings_file, content, 0, null, true);
            }

            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            // Update status
            updateStatus(getResources().getString(R.string.status_app_data_backing_up), null);

            // Show the progress bar
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update the progress bar
            super.onProgressUpdate(progress[0]);
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Long result) {
            // Update status
            updateStatus(getResources().getString(R.string.status_app_data_backup), mPushRightIn);

            // Hide the progress bar when completed
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }

        @Override
        protected void onCancelled() {
            // Hide the progress bar when completed
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);

            // Update status
            updateStatus(getResources().getString(R.string.status_canceled), mBounce);
        }
    }

    // Do basic app data backup
    protected void doBasicAppDataBackup() {
        try {
            new BackupBasicAppDataTask().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Backup basic app data task
    private class BackupBasicAppDataTask extends CustomAsyncTask<Void, Integer, Long> {
        private ProgressBar progressBar;

        @Override
        protected Long doInBackground(Void... arg0) {
            long totalSize = 0;
            List<DBEntry> results;
            DBEntry entry;
            String content = "", app_data_file;
            int count;

            // Backup metadata
            results = mDatasource.getAllActiveContentlessRecords(mOrderBy, mOrderDirection);
            count = results.size();

            // Fill up the metadata string
            for (int i = 0; i < count; i++) {
                entry = results.get(i);
                content = content + entry.getTitle() + Const.SUBDELIMITER + entry.getStar() + Const.SUBDELIMITER + entry.getPos() + Const.SUBDELIMITER + entry.getMetadata() + Const.SUBDELIMITER + entry.getAccessed().getTime() + Const.SUBDELIMITER + entry.getLatitude() + Const.SUBDELIMITER + entry.getLongitude();
                content = content + Const.SUBDELIMITER + entry.getCreated().getTime() + Const.SUBDELIMITER + entry.getModified().getTime();
                content = content + Const.SUBDELIMITER + Const.SUBDELIMITER + Const.DELIMITER;
                publishProgress((int) ((i / (float) count) * 100));

                // Escape early if cancel() is called
                if (isCancelled()) break;
            }

            // Save to metadata file
            app_data_file = Utils.makeFileName(getApplicationContext(), Const.APP_DATA_FILE);
            results = mDatasource.getRecordByTitle(app_data_file);
            if (results.size() == 1) {
                entry = results.get(0);
                mDatasource.updateRecord(entry.getId(), app_data_file, content, entry.getStar(), null, true, app_data_file);
            } else if (results.size() == 0) {
                mDatasource.createRecord(app_data_file, content, 0, null, true);
            }

            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            // Show the progress bar
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Update the progress bar
            super.onProgressUpdate(progress[0]);
            progressBar.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Long result) {
            // Hide the progress bar when completed
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }

        @Override
        protected void onCancelled() {
            // Hide the progress bar when completed
            progressBar = (ProgressBar) findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);
            progressBar.setProgress(0);
        }
    }

    // Get incremental backup state
    protected boolean getIncrementalBackup() {
        return mIncrementalBackup;
    }

    // Set incremental backup state
    protected void setmIncrementalBackup(boolean incrementalBackup) {
        mIncrementalBackup = incrementalBackup;
    }

    // Get local repo path
    protected String getLocalRepoPath() {
        return mLocalRepoPath;
    }

    // Set local repo path
    protected void setLocalRepoPath(String localRepoPath) {
        mLocalRepoPath = localRepoPath;
    }

    // Set default local repo path
    protected void setDefaultLocalRepoPath() {
        try {
            String dir = Utils.getAppPathRemovableStorage(getApplicationContext());
            String path = new File(dir).getCanonicalPath();
            setLocalRepoPath(path);

            mSharedPreferencesEditor.putString(Const.PREF_LOCAL_REPO_PATH, path);
            mSharedPreferencesEditor.commit();

            // Enable multi-file types by default
            Utils.writeLocalRepoFileAndTitle(getApplicationContext(), Const.MULTI_TYPE, Const.NULL_SYM);

            // Create intro note
            Utils.writeLocalRepoFileAndTitle(getApplicationContext(), Const.INTRO_NOTE_FILE, Const.INTRO_NOTE_CONTENT);

            Toast.makeText(getApplicationContext(), path, Toast.LENGTH_LONG).show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Load preferences
    private void loadPref() {
        try {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mSharedPreferencesEditor = mSharedPreferences.edit();

            // Retrieve preference values
            mLocalRepoPath = mSharedPreferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
            mBackupUri = Uri.parse(mSharedPreferences.getString(Const.PREF_BACKUP_URI, ""));
            mIncrementalBackup = mSharedPreferences.getBoolean(Const.PREF_INCREMENTAL_BACKUP, false);
            mAutoSave = mSharedPreferences.getBoolean(Const.PREF_AUTO_SAVE, true);
            mLocationAware = mSharedPreferences.getBoolean(Const.PREF_LOCATION_AWARE, false);
            mCustomFilters = mSharedPreferences.getString(Const.PREF_CUSTOM_FILTERS, getResources().getString(R.string.pref_custom_filter_default));
            mLazyUpdate = mSharedPreferences.getBoolean(Const.PREF_LAZY_UPDATE, false);
            mMathUrl = mSharedPreferences.getString(Const.PREF_MATH_URL, Const.UNSET_URL);
            mOrderBy = mSharedPreferences.getString(Const.PREF_ORDER_BY, Const.SORT_BY_TITLE);
            mOrderDirection = mSharedPreferences.getString(Const.PREF_ORDER_BY_DIRECTION, Const.SORT_ASC);
            mStarAtTop = mSharedPreferences.getBoolean(Const.PREF_STAR_AT_TOP, false);
            mTheme = mSharedPreferences.getString(Const.PREF_THEME, Const.DEFAULT_THEME);
            mLux = mSharedPreferences.getBoolean(Const.PREF_LUX, false);
            mShowHidden = mSharedPreferences.getBoolean(Const.PREF_SHOW_HIDDEN, false);

            // Last mirrored time
            mLastMirrored = mSharedPreferences.getLong(Const.MIRROR_TIMESTAMP, 0);

            // Hacks
            mOled = mSharedPreferences.getBoolean(Const.PREF_OLED, false);
            mEvalBuiltInVariables = mSharedPreferences.getBoolean(Const.PREF_EVAL_BUILT_IN_VARIALBES, false);
            mLowSpaceMode = mSharedPreferences.getBoolean(Const.PREF_LOW_SPACE_MODE, false);
            mLocalPriorityTag = mSharedPreferences.getString(Const.PREF_LOCAL_PRIORITY_TAG, "");
            mRemotePriorityTag = mSharedPreferences.getString(Const.PREF_REMOTE_PRIORITY_TAG, "");
            mPreviewMode = mSharedPreferences.getString(Const.PREF_PREVIEW_MODE, Const.PREVIEW_AT_END);
            mCustomDateFormat = mSharedPreferences.getString(Const.PREF_CUSTOM_DATE_FORMAT, "");
            mKeepDeletedCopies = mSharedPreferences.getBoolean(Const.PREF_KEEP_DELETED_COPIES, true);
            mProcessTextMode = Integer.valueOf(mSharedPreferences.getString(Const.PREF_PROCESS_TEXT_MODE, String.valueOf(Const.PROCESS_TEXT_DISABLED)));
            mMaxSyncLogFileSize = Integer.valueOf(mSharedPreferences.getString(Const.PREF_MAX_SYNC_LOG_FILE_SIZE, String.valueOf(Const.MAX_SYNC_LOG_FILE_SIZE))) * Const.ONE_KB;
            mMaxSyncLogFileAge = Integer.valueOf(mSharedPreferences.getString(Const.PREF_MAX_SYNC_LOG_FILE_AGE, String.valueOf(Const.MAX_SYNC_LOG_FILE_AGE)));
            mLauncherTags = mSharedPreferences.getString(Const.PREF_LAUNCHER_TAGS, "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Sanity check
        checkLocalRepoPath();
        checkBackupUri();
        checkMathUrl();

        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                // Implementation
                if (key.equals(Const.PREF_INCREMENTAL_BACKUP)) {
                    mIncrementalBackup = prefs.getBoolean(key, false);

                    mSharedPreferencesEditor.putBoolean(key, mIncrementalBackup);
                    mSharedPreferencesEditor.commit();

                    // Schedule / cancel backup
                    if (mIncrementalBackup) {
                        // Request permission of backup uri
                        openDocumentTree(Const.REQUEST_CODE_PICK_BACKUP_URI);
                    } else {
                        cancelBackup();

                        // Reset backup uri
                        mSharedPreferencesEditor.putString(Const.PREF_BACKUP_URI, Const.NULL_SYM);
                        mSharedPreferencesEditor.commit();
                    }
                } else if (key.equals(Const.PREF_AUTO_SAVE)) {
                    mAutoSave = prefs.getBoolean(key, true);

                    mSharedPreferencesEditor.putBoolean(key, mAutoSave);
                    mSharedPreferencesEditor.commit();
                } else if (key.equals(Const.PREF_LOCATION_AWARE)) {
                    mLocationAware = prefs.getBoolean(key, false);

                    mSharedPreferencesEditor.putBoolean(key, mLocationAware);
                    mSharedPreferencesEditor.commit();

                    // Handle runtime permissions
                    if ((Build.VERSION.SDK_INT >= 23) && (mLocationAware))
                        getLocationPermission(getApplicationContext());
                } else if (key.equals(Const.PREF_CUSTOM_FILTERS)) {
                    // Validation
                    String temp = prefs.getString(key, getResources().getString(R.string.pref_custom_filter_default));
                    if (temp.length() > 0) {
                        try {
                            String[] dummy = temp.split(";");
                            mSharedPreferencesEditor.putString(key, temp);
                            mSharedPreferencesEditor.commit();

                            mCustomFilters = temp;
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_invalid_filters), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (key.equals(Const.PREF_THEME)) {
                    mTheme = prefs.getString(key, Const.DAY_THEME);

                    mSharedPreferencesEditor.putString(key, mTheme);
                    mSharedPreferencesEditor.commit();

                    // Recreate activity to reload theme
                    recreate();

                    // Set main list background color
                    refreshList();

                    // Apply theme
                    applyTheme();
                } else if (key.equals(Const.PREF_LUX)) {
                    boolean lux_temp = mLux;

                    mLux = prefs.getBoolean(key, false);

                    mSharedPreferencesEditor.putBoolean(key, mLux);
                    mSharedPreferencesEditor.commit();

                    // Set main list background color
                    refreshList();

                    // Apply theme
                    applyTheme();

                    // Prompt restart only when setting has been changed
                    if (lux_temp != mLux)
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.status_restart_app), Toast.LENGTH_LONG).show();
                } else if (key.equals(Const.PREF_LAZY_UPDATE)) {
                    mLazyUpdate = prefs.getBoolean(key, false);

                    mSharedPreferencesEditor.putBoolean(key, mLazyUpdate);
                    mSharedPreferencesEditor.commit();
                } else if (key.equals(Const.PREF_MATH_URL)) {
                    mMathUrl = prefs.getString(key, getResources().getString(R.string.pref_math_url_default));

                    // Only allow HTTPS
                    if (!Utils.isHTTPS(mMathUrl)) {
                        mMathUrl = getResources().getString(R.string.pref_math_url_default);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_server), Toast.LENGTH_SHORT).show();
                    }

                    mSharedPreferencesEditor.putString(key, mMathUrl);
                    mSharedPreferencesEditor.commit();
                } else if (key.equals(Const.PREF_SHOW_HIDDEN)) {
                    mShowHidden = prefs.getBoolean(key, false);

                    // Update filter
                    if (mShowHidden)
                        mDatasource.setFilter(Const.SHOW_PATTERN);
                    else
                        mDatasource.setFilter(Const.HIDE_PATTERN);

                    mSharedPreferencesEditor.putBoolean(key, mShowHidden);
                    mSharedPreferencesEditor.commit();

                    // Refresh list
                    refreshList();
                } else if (key.equals(Const.MIRROR_TIMESTAMP)) {
                    // Hide progress
                    hideIOProgressBar();

                    // Refresh list
                    refreshList();

                    // Update widget
                    Intent intent = new Intent(Const.ACTION_UPDATE_WIDGET);
                    getApplicationContext().sendBroadcast(intent);
                }

                // Backup the settings
                mBackupManager.dataChanged();
            }
        };

        mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    //////////////////////
    // Permission
    //////////////////////

    // Get storage permissions
    protected void getStoragePermission(Context context) {
        final List<String> permissions = new ArrayList<String>();
        List<String> messages = new ArrayList<String>();

        if (!Utils.addPermissionForced(this, permissions, Manifest.permission.READ_EXTERNAL_STORAGE))
            messages.add(getResources().getString(R.string.rationale_storage_read_permission));

        if (!Utils.addPermissionForced(this, permissions, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            messages.add(getResources().getString(R.string.rationale_storage_write_permission));

        if (permissions.size() > 0) {
            if (messages.size() > 0) {
                // Need Rationale
                String message = "";
                for (int i = 0; i < messages.size(); i++)
                    message = message + Const.NEWLINE + messages.get(i);

                Utils.showMessageOKCancel(this, message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, permissions.toArray(new String[permissions.size()]), Const.REQUEST_CODE_STORAGE_PERMISSIONS);
                            }
                        });
                return;
            }
        }
    }

    // Get location permission
    protected void getLocationPermission(Context context) {
        final List<String> permissions = new ArrayList<String>();
        List<String> messages = new ArrayList<String>();

        if (!Utils.addPermission(this, permissions, Manifest.permission.ACCESS_FINE_LOCATION))
            messages.add(getResources().getString(R.string.rationale_location_permission));

        if (!Utils.addPermission(this, permissions, Manifest.permission.ACCESS_COARSE_LOCATION))
            messages.add(getResources().getString(R.string.rationale_coarse_location_permission));

        if (permissions.size() > 0) {
            if (messages.size() > 0) {
                // Need Rationale
                String message = "";
                for (int i = 0; i < messages.size(); i++)
                    message = message + Const.NEWLINE + messages.get(i);

                Utils.showMessageOKCancel(this, message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, permissions.toArray(new String[permissions.size()]), Const.REQUEST_CODE_LOCATION_PERMISSION);
                            }
                        });
                return;
            }
        }
    }

    // Get notification permission
    protected void getNotificationPermission(Context context) {
        final List<String> permissions = new ArrayList<String>();
        List<String> messages = new ArrayList<String>();

        if (!Utils.addPermission(this, permissions, Manifest.permission.POST_NOTIFICATIONS))
            messages.add(getResources().getString(R.string.rationale_post_notifications_permission));

        if (permissions.size() > 0) {
            if (messages.size() > 0) {
                // Need Rationale
                String message = "";
                for (int i = 0; i < messages.size(); i++)
                    message = message + Const.NEWLINE + messages.get(i);

                Utils.showMessageOKCancel(this, message,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, permissions.toArray(new String[permissions.size()]), Const.REQUEST_CODE_NOTIFICATION_PERMISSION);
                            }
                        });
                return;
            }
        }
    }

    // Storage access framework support
    // Open SAF document tree
    protected void openDocumentTree(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        startActivityForResult(intent, requestCode);
    }
}

