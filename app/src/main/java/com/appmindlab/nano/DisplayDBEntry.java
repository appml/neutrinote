package com.appmindlab.nano;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.provider.CalendarContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.BackgroundColorSpan;
import android.text.util.Linkify;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.webkit.JavascriptInterface;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Magnifier;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Keep;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.view.GestureDetectorCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.base.CaseFormat;
import com.google.common.base.Strings;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.appmindlab.nano.R.layout.canvas;
import static com.appmindlab.nano.Utils.makeFileName;

/**
 * Created by saelim on 6/24/2015.
 */
@Keep
public class DisplayDBEntry extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener, MarkdownSymbolFragment.OnMarkdownSymbolSelectedListener, EditToolFragment.OnEditToolFragmentSelectedListener, LocalFindFragment.OnLocalFindSelectedListener, LocalReplaceFragment.OnLocalReplaceSelectedListener {

    // Linear layout
    private LinearLayout mEditor;
    private CoordinatorLayout mEditorCompact;

    // Immersive mode
    private View mDecorView;
    private boolean mImmersiveMode = false;
    private boolean mStopped = false;
    private Handler mImmersiveModeHandler = null;
    private Runnable mImmersiveModeRunnable = null;

    // Markdown view
    protected boolean mMarkdownAnchorActive = false;
    protected boolean mMarkdownRendered = false;
    private int mMarkdownAnchorPos = -1;

    // Toolbar
    private Toolbar mToolBar;
    private int mToolBarSelectedItemId = -1;

    // Database
    private DataSource mDatasource;
    private long mId = -1L;

    // Main list related
    private MainActivity mActivity;

    // Content
    private EditText mTitle, mContent, mCurrentEditText;
    private String mTitleSaved, mContentSaved, mTitleAtOpen, mContentAtOpen;
    private long mPosAtOpen, mPosAtClear = -1;
    private RelativeLayout mTitleBar;
    private boolean mTitleBarVisible = true;
    private boolean mToolBarVisible = true;
    private boolean mEditToolFragmentVisible = true;
    private int mStar;
    private Date mCreated, mModified, mAccessed;
    private String mMetadata = "";
    private ScrollView mScrollView;
    private WebView mMarkdownView;
    private boolean mMarkdownMode = false;
    private String mMarkdownFontFamily = "";
    private String mMarkdownMargin = "";
    private int mMarkdownCacheMode = WebSettings.LOAD_DEFAULT;
    private String mSharedContent = "";
    private long mAnchorPos = -1;
    private boolean mChanged = false;
    private Uri mUri;
    // Magnifier
    private Magnifier mMarkdownMagnifier;
    private float[] mMarkdownMagnifierCoord = new float[2];

    // Status bar
    private TextView mStatusBar;
    private GestureDetectorCompat mEditStatusGestureDetector, mEditContentGestureDetector;
    private List<String> mStatusQ = new ArrayList<String>();

    // Content
    private ScaleGestureDetector mScaleDetector;

    // Snack bar
    private View.OnClickListener mSnackbarOnClickListener;

    // Clipboard
    private ClipboardManager mClipboard;
    private boolean mClipboardMonitor = false;
    private int mClipboardSize;

    // Canvas
    private int mCanvasForeground;
    private Drawable mCanvasBackground;
    private String mCanvasStrokes;
    private char mCanvasStroke = Const.CANVAS_OFF;

    // In note navigation
    private int mInNoteLastIdx = -1;

    // Shared preferences
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor mSharedPreferencesEditor;
    private String mLocalRepoPath;
    private Uri mBackupUri;
    private String mTheme;
    private boolean mLux, mOled;
    private String mFontFamily = Const.DEFAULT_FONT_FAMILY, mFontSize = Const.DEFAULT_FONT_SIZE, mMargin = Const.DEFAULT_MARGIN;
    private String mFontSizeList = Const.DEFAULT_FONT_SIZE_LIST;
    private boolean mFontScaled = false;
    private String mMarginList = Const.DEFAULT_MARGIN_LIST;
    private String mMathUrl;
    private boolean mParsePython;
    private boolean mParseVue;
    private boolean mParseAlpine;
    private boolean mParseMermaid;

    private boolean mParseTypograms;
    private boolean mAutoSave;
    private boolean mCopyAttachmentsToRepo;
    private SharedPreferences.OnSharedPreferenceChangeListener mListener;

    // Light sensor
    private boolean mAutoThemeApplied = false;
    private SensorManager mSensorManager;
    private Sensor mLightSensor;
    private SensorEventListener mLightSensorEventListener;

    // Hacks
    private boolean mKeepDeletedCopies = false;
    private boolean mOpenInMarkdown = false;
    private String mNewNoteFileType = Const.NEW_NOTE_FILE_TYPE;
    private String mMarkdownTrigger = "";
    private String mSafeModeTag = "";
    private String mAutoToolBarTag = "";
    private String mLinkifyTrigger = "";
    private boolean mShowToolBar = true;
    private boolean mCompactToolBar = false;
    private boolean mLaTeXSingleDollar = true;
    private boolean mAppendCustomStyle = false;
    private String mIndentChar = Const.INDENTATION;
    private String mCustomDateFormat = "";
    private String mCustomTimeFormat = "";
    private boolean mMarkdownLocalCache = true;
    private boolean mEvalBuiltInVariables = false;
    private int mWorkingSetSize = Const.WORKING_SET_SIZE;
    private boolean mLabMode = false;

    // Local find and replace
    private AutoCompleteTextView mLocalFind, mLocalReplace;
    private String mCriteria;
    private SpannableStringBuilder mSpann;
    private ArrayList<HitParcelable> mHits = new ArrayList<HitParcelable>();
    private int mHitIdx = -1;
    private long mCurPos = -1, mNextPos = 0;

    // Auto save
    private int mAutoSaveInterval = Const.AUTO_SAVE_INTERVAL;
    private Handler mAutoSaveHandler = null;
    private Runnable mAutoSaveRunnable = null;
    private boolean mAutoSaveSafe = true;

    // Location
    private boolean mLocationAware = false;

    // Snapshots
    private LinkedList mUndo, mRedo;
    private boolean mSnapshotSafe = true;

    // Animation
    private Animation mFadeIn, mFadeOut, mSlideUp, mSlideDown, mPushDownIn, mPushUpIn, mPushLeftIn, mPushLeftOut, mPushRightIn, mPushRightOut, mZoomIn, mBounce, mRotateCenter;

    // Custom fonts
    private HashMap mCustomFonts;

    // Reload
    private boolean mReloadSafe = true;

    // Image
    private Uri mTmpImageUri = null;

    // Misc.
    protected static DisplayDBEntry display_dbentry;

    // Get content
    protected EditText getContent() {
        return mContent;
    }

    // Get current position
    protected int getContentPos() {
        return mContent.getSelectionStart();
    }

    // Get theme
    protected String getAppTheme() {
        return mTheme;
    }

    // Return tool bar visibility
    protected boolean getToolBarVisible() { return mToolBarVisible; }
    protected void setToolBarVisible(boolean visible) { mToolBarVisible = visible; }

    // Return Markdown mode
    protected boolean isMarkdownMode() {
        return mMarkdownMode;
    }

    // Return Markdown render state
    protected boolean isMarkdownRendered() {
        return mMarkdownRendered;
    }
    protected void setMarkdownRendered(boolean rendered) { mMarkdownRendered = rendered; }

    // Return coordinator layout
    protected View getCoordinatorLayout() {
        if (mCompactToolBar)
            return findViewById(R.id.editor);
        else
            return findViewById(R.id.coordinator);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(Const.TAG, "nano - onCreate");

        /////////////////////
        // Setup preferences
        /////////////////////
        loadPref();

        // Setup theme
        setupTheme();

        super.onCreate(savedInstanceState);

        // Setup view
        setupView(savedInstanceState);

        // Self reference
        display_dbentry = this;

        // Setup database
        setupDatabase();

        // Set main activity
        mActivity = MainActivity.main_activity;

        // Setup toolbar
        setupToolBar();

        // Setup status
        setupStatusBar();

        // Setup animation
        setupAnimation();

        // Setup immersive mode
        setupImmersiveMode();

        // Setup custom fonts
        setupCustomFonts();

        ///////////////////////
        // Setup light sensor
        ///////////////////////
        if (mLux) setupLightSensor();

        // Setup editor
        setupEditor();

        // Setup markdown view
        setupMarkdownView();

        // Setup snapshots
        setupSnapshots();

        // Setup shared content
        setupSharedContent();

        // Setup back pressed callback
        setupBackPressedCallback();

        // Apply hacks
        applyHacks();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(Const.TAG, "nano - onPause");

        // Update access time and edit position
        try {
            doSaveAccessTime();
            doSavePos();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Stop auto save
        if (mAutoSaveHandler != null) {
            mAutoSaveHandler.removeCallbacksAndMessages(null);
            mAutoSaveHandler = null;
        }

        // Remember scaled font size
        if (mFontScaled) {
            mSharedPreferencesEditor.putString(Const.PREF_FONT_SIZE, mFontSize);
            mSharedPreferencesEditor.commit();
        }

        // Reset auto theme application state
        mAutoThemeApplied = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(Const.TAG, "nano - onStop");

        // Stopped
        mStopped = true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        Log.d(Const.TAG, "nano - onRestart");

        // Force show edit tool fragment
        forceShowEditToolFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(Const.TAG, "nano - onResume");

        // Reset stop state
        mStopped = false;

        // Self reference (no need to increment count for it is possible to resume more than once)
        display_dbentry = this;

        // Resume database
        resumeDatabase();

        // Reapply theme
        applyTheme();

        // Show/hide title
        showHideTitle(mTitleBarVisible);

        // Show/hide toolbar
        if (mShowToolBar)
            mShowToolBar = !((mAutoToolBarTag.length() > 0) && (mMetadata.contains(mAutoToolBarTag)));

        if ((!mShowToolBar) && (!mImmersiveMode))
            showHideToolBar(mToolBarVisible);

        // Title toggle as default selected item
        mToolBarSelectedItemId = R.id.menu_toggle_title;

        // Resume immersive mode if needed
        if (mImmersiveMode) {
            enterImmersiveMode();
        }

        // Set up editor if needed
        if ((mTitle == null) || (mContent == null)) {
            setupEditor();
        }

        // Resume change detection
        toggleChanges();

        // Setup autosave
        setupAutoSave();

        // Show clipboard change if appropriate
        if (mClipboardMonitor) {
            if (mClipboardSize != Utils.getClipboardText(getApplicationContext(), mClipboard, -1, true).length())
                handleShowClipboard();

            // Deactivate clipboard monitor
            mClipboardMonitor = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop auto save
        if (mAutoSaveHandler != null) {
            mAutoSaveHandler.removeCallbacksAndMessages(null);
            mAutoSaveHandler = null;
        }

        // Remove self reference
        if (display_dbentry == this)
            display_dbentry = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Log.d(Const.TAG, "nano - onSaveInstanceState");

        // If content length is within limit, preserve instance state
        if (mContent.getText().toString().length() <= Const.INSTANCE_SAFE_CONTENT_LEN) {
            super.onSaveInstanceState(savedInstanceState);

            // Auto save if so needed
            if ((mChanged) && (mAutoSave) && (mAutoSaveSafe))
                doSave(false, false);
        }
        else if (mChanged) {    // Otherwise save any changes (the state that we most care)
            if ((mAutoSave) && (mAutoSaveSafe))
                doSave(false, false);
            else
                handleSave();
        }

        savedInstanceState.putLong(Const.STATE_ID, mId);
        savedInstanceState.putString(Const.STATE_CRITERIA, mCriteria);
        savedInstanceState.putParcelableArrayList(Const.STATE_HITS, mHits);
        savedInstanceState.putInt(Const.STATE_HIT_INDEX, mHitIdx);
        savedInstanceState.putLong(Const.STATE_NEXT_POS, mNextPos);
        savedInstanceState.putLong(Const.STATE_ANCHOR_POS, mAnchorPos);
        savedInstanceState.putInt(Const.STATE_MARKDOWN_ANCHOR_POS, mMarkdownAnchorPos);
        savedInstanceState.putBoolean(Const.STATE_TITLE_BAR_VISIBLE, mTitleBarVisible);
        savedInstanceState.putBoolean(Const.STATE_TOOL_BAR_VISIBLE, mToolBarVisible);
        savedInstanceState.putBoolean(Const.STATE_COMPACT_TOOLBAR, mCompactToolBar);
        savedInstanceState.putBoolean(Const.STATE_EDIT_TOOL_FRAGMENT_VISIBLE, mEditToolFragmentVisible);
        savedInstanceState.putBoolean(Const.STATE_MARKDOWN_MODE, mMarkdownMode);
        savedInstanceState.putBoolean(Const.STATE_IMMERSIVE_MODE, mImmersiveMode);
        savedInstanceState.putBoolean(Const.STATE_CHANGED, mChanged);
        savedInstanceState.putBoolean(Const.STATE_AUTOSAVE_SAFE, mAutoSaveSafe);
        savedInstanceState.putBoolean(Const.STATE_SNAPSHOT_SAFE, mSnapshotSafe);
        savedInstanceState.putBoolean(Const.STATE_RELOAD_SAFE, mReloadSafe);
        savedInstanceState.putChar(Const.STATE_CANVAS_STROKE, mCanvasStroke);
        savedInstanceState.putBoolean(Const.STATE_AUTO_THEME_APPLIED, mAutoThemeApplied);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(Const.TAG, "nano - onRestoreInstanceState");

        // If content length is within limit
        if (mContent.getText().toString().length() <= Const.INSTANCE_SAFE_CONTENT_LEN) {
            try {
                super.onRestoreInstanceState(savedInstanceState);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        mId = savedInstanceState.getLong(Const.STATE_ID);
        mCriteria = savedInstanceState.getString(Const.STATE_CRITERIA);
        mHits = savedInstanceState.getParcelableArrayList(Const.STATE_HITS);
        mHitIdx = savedInstanceState.getInt(Const.STATE_HIT_INDEX);
        mNextPos = savedInstanceState.getLong(Const.STATE_NEXT_POS);
        mAnchorPos = savedInstanceState.getLong(Const.STATE_ANCHOR_POS);
        mMarkdownAnchorPos = savedInstanceState.getInt(Const.STATE_MARKDOWN_ANCHOR_POS);
        mTitleBarVisible = savedInstanceState.getBoolean(Const.STATE_TITLE_BAR_VISIBLE);
        mToolBarVisible = savedInstanceState.getBoolean(Const.STATE_TOOL_BAR_VISIBLE);
        mCompactToolBar = savedInstanceState.getBoolean(Const.STATE_COMPACT_TOOLBAR);
        mEditToolFragmentVisible = savedInstanceState.getBoolean(Const.STATE_EDIT_TOOL_FRAGMENT_VISIBLE);
        mMarkdownMode = savedInstanceState.getBoolean(Const.STATE_MARKDOWN_MODE);
        mImmersiveMode = savedInstanceState.getBoolean(Const.STATE_IMMERSIVE_MODE);
        mChanged = savedInstanceState.getBoolean(Const.STATE_CHANGED);
        mAutoSaveSafe = savedInstanceState.getBoolean(Const.STATE_AUTOSAVE_SAFE);
        mSnapshotSafe = savedInstanceState.getBoolean(Const.STATE_SNAPSHOT_SAFE);
        mReloadSafe = savedInstanceState.getBoolean(Const.STATE_RELOAD_SAFE);
        mCanvasStroke = savedInstanceState.getChar(Const.STATE_CANVAS_STROKE);
        mAutoThemeApplied = savedInstanceState.getBoolean(Const.STATE_AUTO_THEME_APPLIED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_dbentry_menu, menu);

        // Title toggle
        MenuItem item = menu.findItem(R.id.menu_toggle_title);

        if (mTitleBarVisible) {
            item.setIcon(ContextCompat.getDrawable(DBApplication.getAppContext(), R.drawable.ic_keyboard_arrow_up_anim_vector));
        } else {
            item.setIcon(ContextCompat.getDrawable(DBApplication.getAppContext(), R.drawable.ic_keyboard_arrow_down_anim_vector));
        }

        if (mMarkdownMode) {
            toggleMarkdownViewMenu(menu, false);
        } else {
            toggleMarkdownViewMenu(menu, true);
        }

        if (mImmersiveMode)
            enterImmersiveMode();

        // Show hide metadata
        item = menu.findItem(R.id.menu_metadata);
        if (mId == -1) {
            item.setVisible(false);
        } else {
            item.setVisible(true);
        }

        // Animate selected item
        if ((mToolBarSelectedItemId == R.id.menu_markdown_view) || (mToolBarSelectedItemId == R.id.menu_toggle_title))
        {
            item = menu.findItem(mToolBarSelectedItemId);
            ((AnimatedVectorDrawable) item.getIcon()).start();

            // Reset selected item
            mToolBarSelectedItemId = -1;
        }

        // Return true to display menu
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        // Update currenlty selected item
        mToolBarSelectedItemId = itemId;

        if (itemId == R.id.menu_style) {
            showStylePopup(findViewById(R.id.menu_style));
            return true;
        } else if (itemId == R.id.menu_markdown_view) {
            toggleMarkdownView();
            return true;
        } else if (itemId == R.id.menu_toggle_title) {
            showHideTitle(!mTitleBarVisible);
            return true;
        } else if (itemId == R.id.menu_save) {
            handleSave();
            return true;
        } else if (itemId == R.id.menu_revert) {
            handleRevert();
            return true;
        } else if (itemId == R.id.menu_metadata) {
            handleMetadata();
            return true;
        } else if (itemId == R.id.menu_edit_tools) {
            showEditToolFragment();
            return true;
        } else if (itemId == R.id.menu_encrypt_decrypt) {
            handleEncryptDecrypt();
            return true;
        } else if (itemId == R.id.menu_paste_calendar) {
            doPasteCalendar();
            return true;
        } else if (itemId == R.id.menu_full_screen) {
            enterImmersiveMode();
            return true;
        } else if (itemId == R.id.menu_clear_cache) {
            doClearCache();
            return true;
        } else if (itemId == R.id.menu_export) {
            handleExport();
            return true;
        } else if (itemId == R.id.menu_print) {
            if (mMarkdownMode)
                createWebPrintJob(mMarkdownView);
            else
                doExportHTML(true);
            return true;
        } else if (itemId == R.id.menu_share) {
            doShare();
            return true;
        } else if (itemId == android.R.id.home) {
            handleHome();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Const.REQUEST_CODE_CAMERA_PERMISSION: {
                Map<String, Integer> perms = new HashMap<String, Integer>();

                // Initial
                perms.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);

                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(DisplayDBEntry.this, this.getResources().getString(R.string.status_camera_permission_granted), Toast.LENGTH_LONG).show();
                } else {
                    // Permission Denied
                    Toast.makeText(DisplayDBEntry.this, this.getResources().getString(R.string.status_camera_permission_denied), Toast.LENGTH_LONG).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // Insert image link
    protected void insertImageLink(Uri uri, String label) {
        try {
            String link, file_name, path;
            InputStream in;
            SimpleDateFormat sdf = new SimpleDateFormat(Const.DIRPATH_FINE_DATE_FORMAT, Locale.getDefault());

            // Convert uri to path
            path = Utils.uri2Path(getApplicationContext(), uri);

            // Add to repo
            if ((mCopyAttachmentsToRepo) && (!path.startsWith(mLocalRepoPath + "/"))) {
                // Obtain input stream
                in = getContentResolver().openInputStream(uri);

                // Generate file name
                file_name = sdf.format(new Date()) + ".jpg";

                // Validate the name for cloud sync
                if (!Utils.validateTitle(file_name)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_invalid_title), Snackbar.LENGTH_SHORT);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                    return;
                }

                // Write to file
                if (Utils.writeFile(this, in, mLocalRepoPath + "/" + Const.ATTACHMENT_PATH + "/", file_name))
                    path = Const.ATTACHMENT_PATH + "/" + file_name;

                // Update mirror if applicable
                if (hasMirror()) {
                    updateMirror(Const.ATTACHMENT_PATH, file_name);
                }
            }

            // Uri-encode the last portion of the path
            path = Utils.encodePathFileName(path);

            // Build the link
            if (path.length() > 0) {
                link = "![" + label + "](" + path + ")" + Const.NEWLINE + Const.NEWLINE;
                Utils.insert(mContent, link);
            } else {
                Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_invalid_local_storage_path), Snackbar.LENGTH_LONG).setAction(getResources().getString(R.string.snack_bar_button_done), mSnackbarOnClickListener);
                Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                snackbar.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_LONG).setAction(getResources().getString(R.string.snack_bar_button_done), mSnackbarOnClickListener);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }

    // Process activity result
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Const.REQUEST_CODE_INSERT_GALLERY_IMAGE && resultCode == RESULT_OK) {    // Insert image
            Uri uri;
            String label = Utils.getCurrentSelection(mContent);
            ClipData clip_data;

            // Sanity check
            if (data == null) return;

            if (data.getClipData() != null) {
                clip_data = data.getClipData();

                for (int i = 0; i < clip_data.getItemCount(); i++) {
                    ClipData.Item item = clip_data.getItemAt(i);
                    uri = item.getUri();
                    insertImageLink(uri, label);
                }
            } else if (data.getData() != null) {
                uri = data.getData();
                insertImageLink(uri, label);
            }
        }
        if (requestCode == Const.REQUEST_CODE_INSERT_CAMERA_IMAGE && resultCode == RESULT_OK) {
            String link, file_name, src, dest, path;
            String label = Utils.getCurrentSelection(mContent);
            SimpleDateFormat sdf = new SimpleDateFormat(Const.DIRPATH_FINE_DATE_FORMAT, Locale.getDefault());

            try {
                // Generate file name
                file_name = sdf.format(new Date()) + ".jpg";

                // Create output directory if it does not exist
                Utils.makeFolder(mLocalRepoPath + "/" + Const.ATTACHMENT_PATH);

                src = mLocalRepoPath + "/" + Const.TMP_PATH + "/" + Const.TMP_IMAGE;
                dest = mLocalRepoPath + "/" + Const.ATTACHMENT_PATH + "/" + file_name;

                if (Utils.copyFile(this, src, dest)) {
                    path = Const.ATTACHMENT_PATH + "/" + file_name;

                    // Build the link
                    if (path.length() > 0) {
                        link = "![" + label + "](" + path + ")" + Const.NEWLINE + Const.NEWLINE;
                        Utils.insert(mContent, link);
                    } else {
                        Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_invalid_local_storage_path), Snackbar.LENGTH_LONG).setAction(getResources().getString(R.string.snack_bar_button_done), mSnackbarOnClickListener);
                        Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                        snackbar.show();
                    }

                    // Update mirror if applicable
                    if (hasMirror()) {
                        updateMirror(Const.ATTACHMENT_PATH, file_name);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_LONG).setAction(getResources().getString(R.string.snack_bar_button_done), mSnackbarOnClickListener);
                Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                snackbar.show();
            }
        } else {
            // Barcode scanner (default)
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (result != null) {
                Utils.insert(mContent, result.getContents() + Const.SPACE_CHAR);
            }
        }
    }

    // Setup database
    private void setupDatabase() {
        // Get all entries from the database
        mDatasource = new DataSource();
        mDatasource.open();
    }

    // Resume database
    private void resumeDatabase() {
        // Sanity check
        if ((mDatasource == null) || (!mDatasource.isOpen())) {
            setupDatabase();
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

    // Setup toolbar
    protected void setupToolBar() {
        mToolBar = findViewById(R.id.entry_toolbar);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    // Setup status bar
    @SuppressLint("ClickableViewAccessibility")
    protected void setupStatusBar() {
        mStatusBar = findViewById(R.id.status_bar);

        // Set gesture detector
        mEditStatusGestureDetector = new GestureDetectorCompat(this, new EditStatusGestureListener());
        mStatusBar.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                mEditStatusGestureDetector.onTouchEvent(motionEvent);
                return true;
            }
        });

        // Set clipboard listener
        mClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mClipboard.addPrimaryClipChangedListener(new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                updateStatus(null, mPushLeftIn);
            }
        });

        // Setup snackbar
        mSnackbarOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        };
    }

    // Setup animation
    protected void setupAnimation() {
        mBounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
        mRotateCenter = AnimationUtils.loadAnimation(this, R.anim.rotate_center);
        mPushDownIn = AnimationUtils.loadAnimation(this, R.anim.push_down_in);
        mPushUpIn = AnimationUtils.loadAnimation(this, R.anim.push_up_in);
        mPushLeftIn = AnimationUtils.loadAnimation(this, R.anim.push_left_in);
        mPushLeftOut = AnimationUtils.loadAnimation(this, R.anim.push_left_out);
        mPushRightIn = AnimationUtils.loadAnimation(this, R.anim.push_right_in);
        mPushRightOut = AnimationUtils.loadAnimation(this, R.anim.push_right_out);
        mZoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        mFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        mFadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        mFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTitleBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        mSlideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        mSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        mSlideUp.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                mTitleBar.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    // Setup immersive mode
    protected void setupImmersiveMode() {
        mDecorView = getWindow().getDecorView();
        mDecorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                // Sanity check
                if (!mImmersiveMode)
                    return;

                // Ignore if triggered by a turning off of the screen
                if (mStopped)
                    return;

                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == View.VISIBLE) {
                    Log.d(Const.TAG, "nano - Exiting full screen");

                    // Exit immersive mode
                    exitImmersiveMode();
                } else {
                    Log.d(Const.TAG, "nano - Entering full screen");
                }
            }
        });
    }

    // Exit immersive mode
    private void exitImmersiveMode() {
        // Handle system UI
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        else {
            mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        // Show title
        showHideTitle(true);

        // Handle edit view
        if (!mMarkdownMode) {
            try {
                // Show edit tools
                showEditToolFragment();

                // Show keyboard
                Utils.showKeyboardAfterImmersiveMode(getApplicationContext(), mTitle, mContent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Show action bar
        getSupportActionBar().show();

        // Reset the state
        mImmersiveMode = false;
    }

    // Enter immersive mode
    private void enterImmersiveMode() {
        // Handle system UI
        int config = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE;

        // Note: for older Android versions
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.R) || (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE))
            config = config | View.SYSTEM_UI_FLAG_FULLSCREEN;

        mDecorView.setSystemUiVisibility(config);

        // Hide title
        showHideTitle(false);

        if (!mMarkdownMode) {
            try {
                // Hide keyboard
                Utils.hideKeyboard(getApplicationContext(), mTitle, mContent);

                // Close all fragments
                closeAllFragments();

                // Hack: avoid getting focus
                mTitle.requestFocus();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Hide action bar
        getSupportActionBar().hide();

        // Remember the state
        // Note: for older Android versions
        if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.R) || (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)) {
            mImmersiveMode = true;
        }
        else {
            mImmersiveModeHandler = new Handler();
            mImmersiveModeRunnable = new Runnable() {
                public void run() {
                    mImmersiveMode = true;
                }
            };
            mImmersiveModeHandler.postDelayed(mImmersiveModeRunnable, Const.IMMERSIVE_MODE_DELAY);
        }
    }

    // Setup view
    protected synchronized void setupView(Bundle savedInstanceState) {
        // Show hide tool bar
        // Three cases:
        // I. If auto save is on and it is safe to do so, free to switch toolbar view.
        // II. If auto save is not or it is not safe to do so, plus already in compact toolbar view, retain compact toolbar view.
        // III. Non-compact toolbar is the default.
        boolean allowToolBarSwitch = mAutoSave;
        boolean fromCompactToolBar = false;

        // Peek saved state
        if (savedInstanceState != null) {
            allowToolBarSwitch &= savedInstanceState.getBoolean(Const.STATE_AUTOSAVE_SAFE);  // Read-only
            fromCompactToolBar = savedInstanceState.getBoolean(Const.STATE_COMPACT_TOOLBAR);   // Writable
        }

        // Alter state
        if ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) && ((allowToolBarSwitch) || (fromCompactToolBar)))
            mCompactToolBar = true;
        else if ((getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) && (!allowToolBarSwitch) && (fromCompactToolBar))
            mCompactToolBar = true;

        // Render
        if (mCompactToolBar)
            setContentView(R.layout.activity_display_dbentry_compact);
        else
            setContentView(R.layout.activity_display_dbentry);

        // Persist state change
        if ((savedInstanceState != null) && (fromCompactToolBar != mCompactToolBar)) {
            savedInstanceState.putBoolean(Const.STATE_COMPACT_TOOLBAR, mCompactToolBar);
        }
    }
    
    // Setup back pressed callback
    protected void setupBackPressedCallback() {
        // Back press callback
        OnBackPressedCallback back_press_callback = new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                // Handle the back button event
                handleHome();
            }
        };
        this.getOnBackPressedDispatcher().addCallback(this, back_press_callback);
    }

    // Setup editor
    protected void setupEditor() {
        // Get the message from the intent
        Intent intent = getIntent();
        mId = intent.getLongExtra(Const.EXTRA_ID, -1L);
        mCriteria = intent.getStringExtra(Const.EXTRA_CRITERIA);

        // Setup editor
        if (mCompactToolBar)
            mEditorCompact = findViewById(R.id.editor);
        else
            mEditor = findViewById(R.id.editor);

        mScrollView = findViewById(R.id.edit_scrollview);

        mTitleBar = findViewById(R.id.title_bar);
        mTitle = findViewById(R.id.edit_title);
        mTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    mCurrentEditText = mTitle;
                }
            }
        });

        mTitle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Handle done event
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mContent.requestFocus();
                }
                return false;
            }
        });

        mTitle.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!mChanged) {
                    mChanged = true;
                    toggleChanges();

                    // Reset Markdown render state
                    setMarkdownRendered(false);
                }

                // Safe for undo
                if (mSnapshotSafe)
                    updateUndo();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        mTitle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Reset auto save timer
                if (mAutoSaveHandler != null) {
                    mAutoSaveHandler.removeCallbacks(mAutoSaveRunnable);
                    mAutoSaveHandler.postDelayed(mAutoSaveRunnable, Const.AUTO_SAVE_BACKOFF * Const.ONE_SECOND);
                }

                return false;
            }
        });

        mContent = findViewById(R.id.edit_content);

        // Note: call here since XML does not work
        mContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        mContent.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    updateStatus(mMetadata, mFadeIn);
                    mCurrentEditText = mContent;
                }
            }
        });

        mContent.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                if (!mChanged) {
                    mChanged = true;
                    toggleChanges();

                    // Reset Markdown render state
                    setMarkdownRendered(false);
                }

                // Safe for undo
                if (mSnapshotSafe)
                    updateUndo();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Set gesture detector
        mEditContentGestureDetector = new GestureDetectorCompat(this, new ContentGestureListener());

        mContent.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Reset auto save timer
                if (mAutoSaveHandler != null) {
                    mAutoSaveHandler.removeCallbacks(mAutoSaveRunnable);
                    mAutoSaveHandler.postDelayed(mAutoSaveRunnable, Const.AUTO_SAVE_BACKOFF * Const.ONE_SECOND);
                }

                // Show/hide tool bar
                if ((!mShowToolBar) && (!mImmersiveMode)) {
                    mEditContentGestureDetector.onTouchEvent(motionEvent);
                }

                // Pass event to scale detector
                mScaleDetector.onTouchEvent(motionEvent);

                return false;
            }
        });

        // Set scale detector
        mScaleDetector = new ScaleGestureDetector(getApplicationContext(), new ContentScaleGestureListener());

        // Existing record
        if (mId > 0) {
            ArrayList<DBEntry> results = mDatasource.getRecordById(mId);
            if (results.size() == 1) {
                mTitleSaved = results.get(0).getTitle();
                mContentSaved = results.get(0).getContent();
                mStar = results.get(0).getStar();
                mMetadata = results.get(0).getMetadata();
                mCreated = results.get(0).getCreated();
                mModified = results.get(0).getModified();
                mAccessed = results.get(0).getAccessed();

                mTitle.setText(mTitleSaved);
                mContent.setText(mContentSaved);

                // Set criteria
                if ((mCriteria != null) && (mCriteria.length() > 1) && (Utils.validateLocalFindCriteria(mCriteria))) {
                    doFind();
                }
                else
                    mCriteria = null;

                // Set focus
                long pos = results.get(0).getPos();
                if ((pos < 0) || (pos > mContent.getText().length())) {
                    pos = mContent.getText().length() - 1;
                    if (pos < 0)
                        pos = 0;
                }

                // Best effort
                try {
                    mContent.setSelection((int) pos);
                    mContent.requestFocus();
                }
                catch (Exception e) {
                    Log.d(Const.TAG, "nano - setupEditor: caught an exception");
                    e.printStackTrace();
                }

                // Update access time
                doSaveAccessTime();

                // Set status
                updateStatus(mMetadata, mPushDownIn);
            }
        } else {
            // Either a new note, open from a file or uri
            if (intent.getStringExtra(Const.EXTRA_URI) != null) {
                // Read from uri
                mUri = Uri.parse(intent.getStringExtra(Const.EXTRA_URI));

                mTitle.setText(Utils.getTitleFromUri(getApplicationContext(), mUri));
                mContent.setText(Utils.getContentFromUri(getApplicationContext(), mUri));

                // File name not changable
                mTitle.setEnabled(false);
            }
            else if (intent.getStringExtra(Const.EXTRA_FILEPATH) != null) {
                // Read from file
                mTitle.setText(intent.getStringExtra(Const.EXTRA_TITLE));
                mContent.setText(Utils.readFile(new File(intent.getStringExtra(Const.EXTRA_FILEPATH))));
            }
            else if (intent.getStringExtra(Const.EXTRA_TITLE) != null) {
                // Suggested title of a new note
                mTitle.setText(intent.getStringExtra(Const.EXTRA_TITLE));
            }

            // Set to empty strings for change detection
            mTitleSaved = "";
            mContentSaved = "";
            mModified = new Date();

            // Show keyboard for blank notes
            if (mTitle.getText().length() == 0) {
                mTitle.setText(mDatasource.getNextNewNoteTitle(getApplicationContext()));

                // Select all for easy correction
                mTitle.setSelectAllOnFocus(true);

                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                mContent.requestFocus();
            }

            // Set status
            updateStatus(Const.EMPTY_SYM, null);

            // Disable autofill
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                getWindow().getDecorView().setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
            }
        }

        // Initialize change detection
        mChanged = false;
        toggleChanges();

        // Store first state
        mTitleAtOpen = mTitle.getText().toString();
        mContentAtOpen = mContent.getText().toString();
        mPosAtOpen = mContent.getSelectionStart();

        // Show edit tools
        showEditToolFragment();

        // Apply theme
        applyTheme();
        applyFontFamily();
        applyFontSize();
        applyMargin();
    }

    // Setup shared content
    private void setupSharedContent() {
        Intent intent = getIntent();
        String content = mContent.getText().toString();
        int pos = 0;

        mSharedContent = intent.getStringExtra(Const.EXTRA_SHARED_CONTENT);

        // Append shared content
        if ((mSharedContent != null) && (mSharedContent.length() > 0)) {

            // Safe for undo
            if (mSnapshotSafe)
                updateUndo();

            if ((content != null) && (content.length() > 0)) {
                pos = content.length() + 2;
                content = content + Const.NEWLINE + Const.NEWLINE + mSharedContent + Const.NEWLINE;
            }
            else
                content = mSharedContent;

            mContent.setText(content);

            // Best effort
            try {
                mContent.setSelection(pos);
                mContent.requestFocus();
            }
            catch (Exception e) {
                Log.d(Const.TAG, "nano - setupShareContent: caught an exception");
                e.printStackTrace();
            }
        }
    }

    // Setup markdown view
    private void setupMarkdownView() {
        mMarkdownView = findViewById(R.id.markdown_view);

        // Enable HTML5 database
        WebSettings settings = mMarkdownView.getSettings();
        settings.setAllowFileAccess(true);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setCacheMode(mMarkdownCacheMode);

        // Register a new JavaScript interface called "appm"
        settings.setJavaScriptEnabled(true);
        mMarkdownView.addJavascriptInterface(new EditViewJavaScriptInterface(this), Const.JAVASCRIPT_INTERFACE);
        mMarkdownView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);

                // Update scroll bar
                if (mMarkdownAnchorActive) {
                    // Scroll to anchor
                    doMarkdownViewScrollAnchor();

                    // Deactivate anchor
                    mMarkdownAnchorActive = false;
                }

                // Set Markdown render state
                if (!mChanged)
                    setMarkdownRendered(true);

                // Hide the progress bar
                ProgressBar progressBar = findViewById(R.id.io_progress_bar);
                progressBar.setVisibility(View.GONE);

                // Setup reflow
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript("neutriNote_utils.init(document);" , null);
                    }
                }, 100);
            }

            boolean scaleChangedRunnablePending = false;

            @Override
            public void onScaleChanged(final WebView webView, final float oldScale, final float newScale) {
                // Text reflow
                if (scaleChangedRunnablePending) return;
                scaleChangedRunnablePending = true;

                webView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        webView.evaluateJavascript("neutriNote_utils.updateScale(" + oldScale + "," + newScale + ");", null);
                        scaleChangedRunnablePending = false;
                    }
                }, 2);
            }

            // Handle URL loading
            protected boolean handleUrlLoading(WebView view, String url) {
                if (url != null) {
                    Intent intent = new Intent();
                    String path, mime;
                    Uri uri;

                    if (url.startsWith(Const.PREFIX_FILE)) {
                        // Relative path will have asset folder as prefix when sent from WebView, so replace it with local repo path
                        // Note: always divert access to the asset folder as well
                        if (url.startsWith(Const.PREFIX_ASSET))
                            url = url.replace(Const.PREFIX_ASSET, Const.PREFIX_FILE + mLocalRepoPath + "/");

                        // Strip file uri prefix
                        path = url.substring(Const.PREFIX_FILE.length());

                        // Uri-decode a path's file name
                        path = Utils.decodePathFileNameWithPrefix(mLocalRepoPath, path);

                        mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(url));

                        uri = FileProvider.getUriForFile(display_dbentry,
                                Utils.mPackageName + ".provider",
                                new File(path));

                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, mime);
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(url));
                    }

                    view.getContext().startActivity(intent);
                    return true;
                } else {
                    return false;
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrlLoading(view, url);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                return handleUrlLoading(view, url);
            }
        });

        // Enable hardware acceleration
        mMarkdownView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        // Handle visibility change such as entering multi-window mode
        // Source: https://stackoverflow.com/a/32778292
        mMarkdownView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if ((mMarkdownMode) && (mMarkdownView.getVisibility() == View.GONE)) {
                    showHideMarkdown(mMarkdownMode);
                }
            }}
        );

        mMarkdownView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // Show/hide tool bar
                if ((!mShowToolBar) && (!mImmersiveMode)) {
                    mEditContentGestureDetector.onTouchEvent(motionEvent);
                }

                // Handle magnifier events
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    if (mMarkdownMagnifier == null) {
                        mMarkdownMagnifier = new Magnifier.Builder(mMarkdownView).build();
                        mMarkdownMagnifier.show(mMarkdownView.getWidth() / 2, mMarkdownView.getHeight() / 2);
                    }

                    switch (motionEvent.getActionMasked()) {
                        case MotionEvent.ACTION_DOWN:
                            // Fall through.
                        case MotionEvent.ACTION_MOVE: {
                            final int[] coord = new int[2];
                            view.getLocationOnScreen(coord);
                            mMarkdownMagnifier.show(motionEvent.getRawX() - coord[0],
                                    motionEvent.getRawY() - coord[1]);
                            break;
                        }
                        case MotionEvent.ACTION_CANCEL:
                            // Fall through.
                        case MotionEvent.ACTION_UP: {
                            mMarkdownMagnifier.dismiss();
                        }
                    }
                }

                return false;
            }
        });
    }

    // Setup custom fonts
    private void setupCustomFonts() {
        Thread t = new Thread() {
            public void run() {
                String fonts_file;

                // Load font configuration
                fonts_file = makeFileName(getApplicationContext(), Const.CUSTOM_FONTS_FILE);
                ArrayList<DBEntry> results = mDatasource.getRecordByTitle(fonts_file);
                if (results.size() == 1) {
                    // Initialize
                    mCustomFonts = new HashMap();

                    // Build a list of custom fonts, each separated by a blank line
                    DBEntry entry = results.get(0);
                    String[] fonts = entry.getContent().split(Const.BLANK_LINE);
                    for (int i = 0; i < fonts.length; i++) {
                        // Skip if commented out
                        if (fonts[i].startsWith(Const.COMMENT_SYM))
                            continue;

                        // Load parameters, each separated by a newline
                        String[] params = fonts[i].split(Const.NEWLINE);

                        // Sanity check
                        if (params.length != 4) continue;

                        // Verify file existence
                        if (!params[0].equals(Const.SYSTEM_FONT_NAME)) {
                            File file = new File(mLocalRepoPath + "/" + Const.CUSTOM_FONTS_PATH + "/" + params[1]);
                            if (!file.exists()) continue;
                        }

                        // Save the configuration
                        mCustomFonts.put(params[0], new CustomFont(params[0], params[1], params[2], params[3]));
                    }
                }
            };
        };

        t.start();
    }

    // Setup snapshots
    private void setupSnapshots() {
        Thread t = new Thread() {
            public void run() {
                try {
                    // Return if no undo allowed
                    if (Const.MAX_SNAPSHOTS == 0)
                        return;

                    mUndo = new LinkedList();
                    mRedo = new LinkedList();

                    // Save version 0
                    mUndo.add(new Snapshot(mContent.getText().toString(), (int) mContent.getSelectionStart()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };

        t.start();
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
                    }
                    else {
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
        if (mLightSensor != null){
            mSensorManager.registerListener(
                    mLightSensorEventListener,
                    mLightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    // Update undo
    private void updateUndo() {
        Thread t = new Thread() {
            public void run() {
                try {
                    // Return if no undo allowed
                    if (Const.MAX_SNAPSHOTS == 0) return;

                    // Recreate a snapshot list if needed
                    if (mUndo == null)
                        mUndo = new LinkedList();

                    // Truncate the set of snapshots
                    while (mUndo.size() > Const.MAX_SNAPSHOTS)
                        mUndo.removeFirst();

                    mUndo.add(new Snapshot(mContent.getText().toString(), (int) mContent.getSelectionStart()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };

        t.start();
    }

    // Update redo
    private void updateRedo() {
        Thread t = new Thread() {
            public void run() {
                try {
                    // Return if no undo allowed
                    if (Const.MAX_SNAPSHOTS == 0) return;

                    // Recreate a snapshot list if needed
                    if (mRedo == null)
                        mRedo = new LinkedList();

                    // Truncate the set of snapshots
                    while (mRedo.size() > Const.MAX_SNAPSHOTS)
                        mRedo.removeFirst();

                    // Add new snapshot
                    mRedo.add(new Snapshot(mContent.getText().toString(), mContent.getSelectionStart()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            };
        };

        t.start();
    }

    // Do undo
    private void doUndo() {
        try {
            // Not safe
            mSnapshotSafe = false;
            mAutoSaveSafe = false;

            if (mUndo.size() > 0) {
                Snapshot snapshot = (Snapshot) mUndo.removeLast();
                String status;
                PrettyTime pretty_time = new PrettyTime(DBApplication.getAppContext().getResources().getConfiguration().locale);

                if (snapshot != null) {
                    // Make the most recent action redoable before making any change
                    updateRedo();

                    // Clear search hits
                    if (mHitIdx != -1)
                        doClearSearch();

                    // Update UI
                    Utils.replaceDifference(mContent, snapshot.getContent());
                    mContent.setSelection((int) snapshot.getPos());
                }

                status = mUndo.size() + getResources().getString(R.string.status_changes_left);
                status += "\n" + getResources().getString(R.string.status_back_to) + pretty_time.format(new Date(snapshot.getTimestamp()));

                updateStatus(status, null);
            } else {
                updateStatus(getResources().getString(R.string.error_undo), mBounce);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_undo), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mSnapshotSafe = true;
            mAutoSaveSafe = true;
        }
    }

    // Do redo
    private void doRedo() {
        try {
            // Not safe
            mSnapshotSafe = false;
            mAutoSaveSafe = false;

            if (mRedo.size() > 0) {
                Snapshot snapshot = (Snapshot) mRedo.removeLast();
                String status;
                PrettyTime pretty_time = new PrettyTime(DBApplication.getAppContext().getResources().getConfiguration().locale);

                if (snapshot != null) {
                    // Make the most recent action undoable before making any change
                    updateUndo();

                    // Clear search hits
                    if (mHitIdx != -1)
                        doClearSearch();

                    // Update UI
                    Utils.replaceDifference(mContent, snapshot.getContent());
                    mContent.setSelection((int) snapshot.getPos());
                }

                status = mRedo.size() + getResources().getString(R.string.status_changes_left);
                status += "\n" + getResources().getString(R.string.status_back_to) + pretty_time.format(new Date(snapshot.getTimestamp()));

                updateStatus(status, null);
            } else {
                updateStatus(getResources().getString(R.string.error_redo), mBounce);
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.error_redo), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mSnapshotSafe = true;
            mAutoSaveSafe = true;
        }
    }

    // Setup auto save
    private void setupAutoSave() {
        if (!mAutoSave)
            return;

        if (mAutoSaveInterval < 0)
            return;

        if (mAutoSaveHandler == null) {
            mAutoSaveHandler = new Handler(Looper.getMainLooper());
            mAutoSaveRunnable = () -> {
                mAutoSaveHandler.postDelayed(mAutoSaveRunnable, mAutoSaveInterval * Const.ONE_SECOND);

                /* Skip when a new record has not been given a title
                if ((mId < 0) && (mTitle.getText().toString().length() == 0))
                    return;
                */

                if ((mAutoSaveSafe) && (mChanged)) {
                    doSave(false, false);
                }
            };
            mAutoSaveHandler.postDelayed(mAutoSaveRunnable, mAutoSaveInterval * Const.AUTO_SAVE_BACKOFF * Const.ONE_SECOND);
        }
    }

    // Close top fragment
    private void closeTopFragment() {
        FragmentManager manager = getSupportFragmentManager();

        if (manager.getBackStackEntryCount() > 0)
            manager.popBackStack();
    }

    // Close all fragment
    private void closeAllFragments() {
        FragmentManager manager = getSupportFragmentManager();

        for (int i = manager.getBackStackEntryCount(); i > 0; i--)
            manager.popBackStack();
    }

    // Show local find tool fragment
    private void showLocalFindFragment() {
        LocalFindFragment fragment = new LocalFindFragment();

        // No argument for now
        Bundle args = new Bundle();
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.addToBackStack(Const.LOCAL_FIND_FRAGMENT_TAG);
        transaction.replace(R.id.fragment_content, fragment);

        transaction.commit();
    }

    // Handle show shortcut
    private void handleShowShortcuts() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.dialog_show_shortcuts_title);

        // Create a list of shortcuts
        final String shortcuts_file;
        shortcuts_file = Utils.makeFileName(this, Const.SHORTCUTS_FILE);

        List<DBEntry> results = mDatasource.getRecordByTitle(shortcuts_file);
        DBEntry entry;
        StringBuilder sb = new StringBuilder();
        String message = getResources().getString(R.string.dialog_show_shortcuts_not_found);

        if (results.size() == 1) {
            entry = results.get(0);
            sb.append(entry.getContent().trim());
            sb.append(Const.NEWLINE);

            if (sb.length() > 1)
                message = sb.toString();
        }

        builder.setMessage(message);

        builder.setPositiveButton(R.string.dialog_show_shortcuts_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.show();

        // Center the text
        Typeface font = FontCache.getFromAsset(this, "RobotoMono-Light.ttf");
        TextView text = (TextView) dialog.findViewById(android.R.id.message);
        text.setTextSize(11);
        text.setTypeface(font);
        text.setTextIsSelectable(true);
        text.startAnimation(mFadeIn);

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    // Handle add shortcut
    private void handleAddShortcut(final String shortcut) {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        // builder.setTitle(R.string.dialog_add_shortcut_title);

        // Add an edit field for expanded text
        TextInputLayout view = (TextInputLayout) LayoutInflater.from(this).inflate(R.layout.floating_hint_input, null);
        String hint = "\"" + shortcut + "\"" + getResources().getString(R.string.dialog_add_shortcut_hint);
        view.setHint(hint);
        builder.setView(view);

        final EditText expanded_str = (EditText) view.findViewById(R.id.input_str);
        expanded_str.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        expanded_str.setTextColor(ContextCompat.getColor(this, R.color.theme_control_activated));
        expanded_str.setSingleLine();

        final String shortcuts_file;
        shortcuts_file = Utils.makeFileName(this, Const.SHORTCUTS_FILE);

        builder.setPositiveButton(R.string.dialog_add_shortcut_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                List<DBEntry> results = mDatasource.getRecordByTitle(shortcuts_file);
                DBEntry entry;
                StringBuilder sb = new StringBuilder();
                String expanded = expanded_str.getText().toString().replaceAll("\\r\\n|\\r|\\n", " ");

                // Append to the shortcuts file
                if ((results.size() == 1) && (expanded.length() > 0)) {
                    entry = results.get(0);
                    sb.append(entry.getContent().trim());
                    sb.append(Const.NEWLINE);
                    sb.append(shortcut + "|" + expanded);

                    // Update the definition file
                    mDatasource.updateRecord(entry.getId(), entry.getTitle(), sb.toString(), entry.getStar(), new Date(), true, entry.getTitle());

                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.status_shortcut_added), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_no_shortcuts), Snackbar.LENGTH_LONG).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_add_shortcut_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return; // Do nothing
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Evaluate custom variables
    private String evalCustomVariables(String[] shortcuts, String shortcut) {
        String expanded = shortcut;

        // Get expansion
        // Note: only basic shortcuts are allowed for use as custom variables
        for (int i = 0; i < shortcuts.length; i++) {
            // Ignore comments
            if (shortcuts[i].startsWith(Const.COMMENT_SYM))
                continue;

            if (shortcuts[i].toLowerCase(Locale.getDefault()).startsWith(shortcut.toLowerCase(Locale.getDefault()) + Const.SHORTCUTS_DELIMITER)) {
                expanded = shortcuts[i].substring(shortcut.length() + 1);
                break;
            }
        }

        return expanded;
    }

    // Evaluate variables
    private String evalVariables(String str, boolean escape) {
        try {
            SimpleDateFormat sdf = Utils.getDateFormat(getApplicationContext(), mCustomDateFormat);

            // Sanity check
            if ((str == null) || (str.length() == 0))
                return "";

            // Evaluate note specific variables
            if ((mTitle != null) && (str.contains(Const.TITLE_VAR)))
                str = str.replaceAll(Const.TITLE_VAR, mTitle.getText().toString());

            if ((mCreated != null) && (str.contains(Const.CREATED_VAR)))
                str = str.replaceAll(Const.CREATED_VAR, sdf.format(mCreated));

            if ((mModified != null) && (str.contains(Const.MODIFIED_VAR)))
                str = str.replaceAll(Const.MODIFIED_VAR, sdf.format(mModified));

            if ((mAccessed != null) && (str.contains(Const.ACCESSED_VAR)))
                str = str.replaceAll(Const.ACCESSED_VAR, sdf.format(mAccessed));

            if (str.contains(Const.CUSTOM_VAR_PREFIX)) {
                String[] shortcuts = loadShortcuts();
                String pattern, expanded;
                int start, end;

                if (shortcuts != null) {
                    start = str.indexOf(Const.CUSTOM_VAR_PREFIX);
                    end = str.indexOf(Const.EMPTY_SYM, start);

                    if (end > 0)
                        pattern = str.substring(start + Const.CUSTOM_VAR_PREFIX.length(), end);
                    else
                        pattern = str.substring(start + Const.CUSTOM_VAR_PREFIX.length());

                    expanded = evalCustomVariables(shortcuts, pattern);
                    str = str.replaceAll(Const.CUSTOM_VAR_PREFIX + pattern, expanded);
                }
            }

            // Evaluate global variables
            str = Utils.evalGlobalVariables(getApplicationContext(), str, mCustomDateFormat, mCustomTimeFormat, escape);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            return str;
        }
    }

    // Load shortcuts
    private String[] loadShortcuts() {
        final String shortcuts_file;

        // Sanity check
        resumeDatabase();

        shortcuts_file = Utils.makeFileName(getApplicationContext(), Const.SHORTCUTS_FILE);
        List<DBEntry> results = mDatasource.getRecordByTitle(shortcuts_file);

        if (results.size() == 1)
            return results.get(0).getContent().split(Const.NEWLINE);
        else
            // Otherwise initialize the shortcut file
            mDatasource.createRecord(shortcuts_file, "", 0, null, true);

        return null;
    }

    // Expand the shortcut
    private void expandShortcut(String[] shortcuts) {
        int start = Math.min(mContent.getSelectionStart(), mContent.getSelectionEnd());
        int end = Math.max(mContent.getSelectionStart(), mContent.getSelectionEnd());

        String expanded = null;
        String shortcut, shortcut_with_space = null, extra = null;
        String[] params;
        String[] items;

        // Determine the word
        if (start < end) {
            shortcut = mContent.getText().toString().substring(start, end).trim();
            shortcut_with_space = mContent.getText().toString().substring(start, end).trim();
        }
        else
            shortcut = Utils.getCurrentWord(mContent, start, true);

        // Also try to extract symbols
        if (shortcut.length() == 0)
            shortcut = Utils.getCurrentSnippet(mContent, start, true);

        // Sanity check
        if (shortcut.length() == 0) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.warn_no_selected_word), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            return;
        }

        // If more than a word in the shortcut, the extra words are parameters
        params = shortcut.split("\\s+");
        if (params.length > 1) {
            extra = shortcut;
            shortcut = params[0];
            extra = extra.substring(shortcut.length());
        }

        // Get expansion
        for (int i = 0; i < shortcuts.length; i++) {
            // Ignore comments
            if (shortcuts[i].startsWith(Const.COMMENT_SYM))
                continue;

            if (shortcuts[i].toLowerCase(Locale.getDefault()).startsWith(shortcut.toLowerCase(Locale.getDefault()) + Const.SHORTCUTS_DELIMITER)) {
                expanded = shortcuts[i].substring(shortcut.length() + 1);
                break;
            }

            // Consider shortcuts with spaces as well
            if (shortcut_with_space != null) {
                if (shortcuts[i].toLowerCase(Locale.getDefault()).startsWith(shortcut_with_space.toLowerCase(Locale.getDefault()) + Const.SHORTCUTS_DELIMITER)) {
                    shortcut = shortcut_with_space;
                    expanded = shortcuts[i].substring(shortcut.length() + 1);
                    break;
                }
            }
        }

        if (expanded != null) {
            if (expanded.startsWith(Const.WEBSERVICE_SYM)) {    // Web service
                // Setup request
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = Utils.toHTTPS(expanded.substring(Const.WEBSERVICE_SYM.length()).trim());

                // Handle parameters if available
                if (extra != null) {
                    params = extra.split(Const.SHORTCUTS_PARAMS_DELIMITER);
                    params = Utils.encodeStringArray(params);

                    if (expanded.contains(Const.PARAMETER_SYM)) {
                        // Replace parameter placeholders
                        url = Utils.replacePattern(url, Const.PARAMETER_SYM, params);
                    } else {
                        // Append parameters
                        for (int i = 0; i < params.length; i++) {
                            url += params[i].trim();

                            if (i < (params.length - 1))
                                url += "&";
                        }
                    }
                }

                // Request a string response from the provided URL.
                StringRequest request = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Utils.insert(mContent, response);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT);
                                Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                                snackbar.show();
                            }
                        });

                // Add the request to the RequestQueue.
                queue.add(request);
            }
            else if (expanded.startsWith(Const.WEBSERVICE_JSON_SYM)) {    // Web service (JSON)
                // Setup request
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = Utils.toHTTPS(expanded.substring(Const.WEBSERVICE_JSON_SYM.length()).trim());

                // Handle parameters if available
                if (extra != null) {
                    try {
                        // Create JSON object
                        JSONObject json = new JSONObject(extra.replaceAll("\"", "\\\""));

                        // Request a JSON response from the provided URL.
                        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, json,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        try {
                                            Utils.insert(mContent, response.toString(mIndentChar.length()));
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT);
                                            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                                            snackbar.show();
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT);
                                        Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                                        snackbar.show();
                                    }
                                });

                        // Add the request to the RequestQueue.
                        queue.add(request);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT);
                        Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                        snackbar.show();
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
            }
            else if (expanded.startsWith(Const.SHELL_SYM)) {    // Shell command
                // Handle parameters if available
                if (extra != null) {
                    params = extra.split(Const.SHORTCUTS_PARAMS_DELIMITER);
                    params = Utils.cleanStringArray(params);

                    if (expanded.contains(Const.PARAMETER_SYM)) {
                        // Replace parameter placeholders
                        expanded = Utils.replacePattern(expanded, Const.PARAMETER_SYM, params);
                    }
                    else {
                        // Append parameters
                        for (int i = 0; i < params.length; i++) {
                            expanded += " " + params[i];
                        }
                    }
                }

                ShellExecuter exe = new ShellExecuter();
                expanded = exe.Executer(expanded.substring(Const.SHELL_SYM.length()));

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.insert(mContent, expanded);
            }
            else if (expanded.startsWith(Const.REPLACE_SYM)) {    // Replace command
                if (extra != null) {
                    expanded = expanded.substring(Const.REPLACE_SYM.length()).trim();

                    // Split the expanded string into 2 parts:
                    // 1. Old pattern
                    // 2. New pattern
                    params = expanded.split(Const.SHORTCUTS_PATTERN_DELIMITER);
                    params = Utils.cleanStringArray(params);
                    if (params.length == 2)
                        expanded = extra.replaceAll(params[0], params[1]).trim();
                    else
                        expanded = null;
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.insert(mContent, expanded);
            }
            else if (expanded.startsWith(Const.SPLIT_SYM)) {    // Split command
                if (extra != null) {
                    expanded = expanded.substring(Const.SPLIT_SYM.length()).trim();

                    if (expanded.length() > 0) {
                        items = extra.split(expanded);
                        expanded = TextUtils.join(Const.NEWLINE, items).trim();
                    }
                    else
                        expanded = null;
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.insert(mContent, expanded);
            }
            else if (expanded.startsWith(Const.REMOVE_SYM)) {    // Remove command
                if (extra != null) {
                    expanded = expanded.substring(Const.REMOVE_SYM.length()).trim();

                    if (expanded.length() > 0)
                        expanded = extra.replaceAll(expanded, "").trim();
                    else
                        expanded = null;
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.insert(mContent, expanded);
            }
            else if (expanded.startsWith(Const.LINEBREAK_SYM)) {    // Insert line break command
                if (extra != null) {
                    expanded = expanded.substring(Const.LINEBREAK_SYM.length()).trim();

                    // Replace occurrences of old pattern by line breaks
                    params = expanded.split(Const.SHORTCUTS_PATTERN_DELIMITER);
                    params = Utils.cleanStringArray(params);
                    if (params.length == 1)
                        expanded = extra.replaceAll(params[0], System.getProperty("line.separator")).trim();
                    else
                        expanded = null;
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.insert(mContent, expanded);
            } else if (expanded.startsWith(Const.TRIM_SYM)) {    // Trim command
                if (extra != null)
                    expanded = extra.trim();
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.REMOVE_ZERO_WIDTH_SPACE_SYM)) {    // Trim command
                if (extra != null)
                    try {
                        expanded = Utils.removeZeroWidthSpaces(extra.trim());
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.SORT_SYM)) {    // Sort command
                if (extra != null) {
                    try {
                        expanded = Utils.sort(extra);
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.REVERSE_SORT_SYM)) {    // Reverse sort command
                if (extra != null) {
                    try {
                        expanded = Utils.rsort(extra);
                    } catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                } else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.ENCODE_SYM)) {    // Encode command
                if (extra != null) {
                    try {
                        expanded = Base64.encodeToString(extra.getBytes("UTF-8"), Base64.DEFAULT);
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.info_cancel_encrypt), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.DECODE_SYM)) {    // Decode command
                if (extra != null)
                    expanded = new String(Base64.decode(extra, Base64.DEFAULT));
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.CAMEL2SNAKE_SYM)) {    // Camel to snake command
                if (extra != null)
                    expanded = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, extra);
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.SNAKE2CAMEL_SYM)) {    // Snake to camel command
                if (extra != null)
                    expanded = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, extra);
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.STRIP_HTML_SYM)) {    // Strip HTML command
                if (extra != null) {
                    try {
                        expanded = Utils.stripHtml(extra);
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.TAG_EXPAND_SYM)) {    // Tag expand command
                if (extra != null) {
                    try {
                        expanded = Utils.tagExpand(extra);
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.replaceString(mContent, start, end, expanded);
            }
            else if (expanded.startsWith(Const.MORPH_SYM)) {    // Morph command
                if ((extra != null) && (extra.length() <= Const.MAX_EXTRA_LEN)) {
                    // Get pattern
                    expanded = expanded.substring(Const.MORPH_SYM.length()).trim();

                    // Remove unsafe specifiers
                    expanded = expanded.replace("%n", "");

                    // Prepare value
                    try {
                        extra = extra.trim();
                        if (Utils.isNumeric(extra)) {    // Numeric
                            if (Utils.isDecimalNumber(extra))
                                expanded = String.format(Locale.getDefault(), expanded, Double.parseDouble(extra));
                            else
                                expanded = String.format(Locale.getDefault(), expanded, Integer.parseInt(extra));
                        } else if (expanded.contains("t") || expanded.contains("T")) {    // Date
                            DateFormat date_format;

                            date_format = android.text.format.DateFormat.getDateFormat(getApplicationContext());
                            expanded = String.format(Locale.getDefault(), expanded, date_format.parse(extra));
                        } else
                            expanded = String.format(Locale.getDefault(), expanded, extra);
                    } catch (Exception e) {
                        e.printStackTrace();
                        expanded = null;
                    }
                } else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                else
                    Utils.insert(mContent, expanded);
            }
            else if (expanded.startsWith(Const.LAUNCH_SYM)) {    // Launch command
                // Retrieve package name
                expanded = expanded.substring(Const.LAUNCH_SYM.length()).trim();

                if (expanded != null) {
                    // Launch the package
                    if (!Utils.launchPackage(getApplicationContext(), expanded)) {
                        Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                        Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                        snackbar.show();
                    }
                } else {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
            }
            else if (expanded.startsWith(Const.FUNNEL_SYM)) {    // Funnel command
                // Show file picker
                handleFunnel();
            }
            else if (expanded.startsWith(Const.NEEDLE_SYM)) {    // Needle command
                if (extra != null) {
                    // Dynamic criteria
                    try {
                        expanded = extra.trim();
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                }
                else
                    // Pre-defined criteria
                    expanded = expanded.substring(Const.NEEDLE_SYM.length()).trim();

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                } else {
                    // "Consume" the command
                    if (extra == null)
                        Utils.insert(mContent, Const.NULL_SYM);
                    else
                        Utils.insert(mContent, extra.trim());

                    doSearchNote(expanded);
                }
            }
            else if (expanded.startsWith(Const.OVERRIDE_SYM)) {    // Command to temporarily override settings
                // Retrieve settings
                expanded = expanded.substring(Const.OVERRIDE_SYM.length()).trim();

                if (expanded.equals(Const.PREF_SHOW_TOOLBAR)) {
                    // Toogle toolbar mode
                    mShowToolBar = !mShowToolBar;

                    // Show tool bar
                    showHideToolBar(true);

                    // "Consume" the command
                    Utils.insert(mContent, Const.NULL_SYM);

                    // Show confirmation
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), expanded + ": " + mShowToolBar, Snackbar.LENGTH_LONG).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
            }
            else if (expanded.startsWith(Const.SYNC_SYM)) {    // Command to send a sync request
                // Send a sync request
                Utils.sendSyncRequest(getApplicationContext(), mLocalRepoPath, mBackupUri);

                // "Consume" the command
                Utils.insert(mContent, Const.NULL_SYM);
            }
            else if (expanded.startsWith(Const.CREATE_NOTE_LINK_SYM)) {
                String title = mTitle.getText().toString();
                String criteria = null;

                if (extra != null) {
                    criteria = extra;
                }

                String link = Const.BLANK_LINE + Utils.createNoteLink(title, criteria);
                Utils.setClipboardText(getApplicationContext(), mClipboard, Const.CREATE_NOTE_LINK_SYM, link);

                // Restore the parameter
                Utils.insert(mContent, extra.trim());

                // Show confirmation
                Toast.makeText(getApplicationContext(), Const.CLIPBOARD_SYM + Const.LINK_SYM + Const.SPACE_CHAR + link, Toast.LENGTH_SHORT).show();
            }
            else if (expanded.startsWith(Const.CLI_EVAL_JS_SNIPPET_SYM)) {    // Evaluate a JavaScript snippet
                if (extra != null) {
                    try {
                        Utils.cliEvalJS(getApplicationContext(), this, getCoordinatorLayout(), mContent, extra, Const.CLI_EVAL_JS_TIMEOUT, true);
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
            }
            else if (expanded.startsWith(Const.CLI_EVAL_JS_INTERACTIVE_SYM)) {    // Evaluate JavaScript calls interactively
                if (extra != null) {
                    try {
                        Utils.cliEvalJS(getApplicationContext(), this, getCoordinatorLayout(), mContent, extra, Const.CLI_EVAL_JS_TIMEOUT, false);
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
            }
            else if (expanded.startsWith(Const.CLI_EVAL_JS_INLINE_SYM)) {    // Evaluate inline based JavaScript
                if (extra != null) {
                    try {
                        // Retrieve function declaration
                        expanded = expanded.substring(Const.CLI_EVAL_JS_INLINE_SYM.length()).trim();
                        Utils.cliEvalJS(getApplicationContext(), this, getCoordinatorLayout(), mContent, expanded + extra, Const.CLI_EVAL_JS_TIMEOUT, true);
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }

            }
            else if (expanded.startsWith(Const.CLI_EVAL_JS_FILE_SYM)) {    // Evaluate file based JavaScript
                if (extra != null) {
                    try {
                        // Retrieve function declaration
                        expanded = expanded.substring(Const.CLI_EVAL_JS_FILE_SYM.length()).trim();

                        // Load Javascript
                        List<DBEntry> results = mDatasource.getRecordByTitle(expanded);
                        DBEntry entry;
                        StringBuilder sb = new StringBuilder();

                        if (results.size() == 1) {
                            entry = results.get(0);
                            sb.append(entry.getContent().trim());

                            String script = sb.toString();
                            if (!script.endsWith(";"))
                                script += ";";

                            Utils.cliEvalJS(getApplicationContext(), this, getCoordinatorLayout(), mContent, script + extra, Const.CLI_EVAL_JS_TIMEOUT*2, true);
                        }
                        else
                            expanded = null;
                    }
                    catch (Exception e) {
                        expanded = null;
                        e.printStackTrace();
                    }
                }
                else
                    expanded = null;

                if ((expanded == null) || (expanded.length() == 0)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }

            }
            else {
                // Apply newlines
                expanded = expanded.trim().replaceAll(Const.NEWLINE_ENTITY, System.getProperty("line.separator"));

                // Handle parameters if available
                if (extra != null) {
                    params = extra.split(Const.SHORTCUTS_PARAMS_DELIMITER);
                    params = Utils.cleanStringArray(params);

                    if (expanded.contains(Const.PARAMETER_SYM)) {
                        // Replace parameter placeholders
                        expanded = Utils.replacePattern(expanded, Const.PARAMETER_SYM, params);
                    }
                }

                // Evaluate built-in variables
                if (mEvalBuiltInVariables)
                    expanded = evalVariables(expanded, false);

                Utils.insert(mContent, expanded);
            }
        }
        else {
            // Restore selection range
            mContent.setSelection(start, end);
            handleAddShortcut(shortcut);
        }
    }

    // Do text expansion
    private void doTextExpansion() {
        final String shortcuts_file;

        if (Utils.fileNameAsTitle(this))
            shortcuts_file = Const.SHORTCUTS_FILE + ".txt";
        else
            shortcuts_file = Const.SHORTCUTS_FILE;

        // Sanity check
        if (mTitleSaved.equals(shortcuts_file)) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.warn_shortcut_disabled), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            return;
        }

        String[] shortcuts = loadShortcuts();

        if (shortcuts != null) {
            // Prepare for undo
            updateUndo();

            // Expand shortcut
            expandShortcut(shortcuts);
        }
    }

    // Show local find tool fragment
    private void showLocalReplaceFragment() {
        // Sanity check
        if ((mHits == null) || (mHits.size() == 0)) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.status_no_search_conducted), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.snack_bar_button_ok), mSnackbarOnClickListener);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            return;
        }

        // Safe for undo
        if (mSnapshotSafe)
            updateUndo();

        LocalReplaceFragment fragment = new LocalReplaceFragment();

        // No argument for now
        Bundle args = new Bundle();
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.addToBackStack(Const.LOCAL_REPLACE_FRAGMENT_TAG);
        transaction.replace(R.id.fragment_content, fragment);

        transaction.commit();
    }

    // Force show edit tool fragment
    private void forceShowEditToolFragment() {
        FrameLayout frame_layout = findViewById(R.id.fragment_content);

        if (frame_layout.getVisibility() != View.VISIBLE)
            Utils.hideKeyboard(getApplicationContext(), mTitle, mContent);
    }

    // Show markdown symbol fragment
    private void showMarkdownSymbolFragment() {
        MarkdownSymbolFragment fragment = new MarkdownSymbolFragment();

        // No argument for now
        Bundle args = new Bundle();
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FrameLayout frame_layout = findViewById(R.id.fragment_content);
        frame_layout.startAnimation(mFadeIn);

        transaction.addToBackStack(Const.MAKRDOWN_SYMBOL_FRAGMENT_TAG);
        transaction.replace(R.id.fragment_content, fragment);

        transaction.commit();

        // Set focus
        mCurrentEditText.requestFocus();
    }


    // Show edit tool fragment
    private void showEditToolFragment() {
        EditToolFragment fragment = new EditToolFragment();

        closeTopFragment();

        // No argument for now
        Bundle args = new Bundle();
        fragment.setArguments(args);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FrameLayout frame_layout = findViewById(R.id.fragment_content);
        frame_layout.startAnimation(mPushUpIn);

        transaction.addToBackStack(Const.EDIT_TOOL_FRAGMENT_TAG);
        transaction.replace(R.id.fragment_content, fragment);

        transaction.commit();

        // Refresh keyboard if edit tool fragment is "missing"
        if (mEditToolFragmentVisible) {
            Utils.hideKeyboard(getApplicationContext(), mTitle, mContent);
            Utils.showKeyboard(getApplicationContext(), mTitle, mContent);
        }
        else
            mEditToolFragmentVisible = true;
    }

    // Show the date picker fragment
    public void showDatePickerCalendarViewFragment() {
        DatePickerCalendarViewFragment fragment = new DatePickerCalendarViewFragment();
        fragment.show(this.getSupportFragmentManager(), Const.DATE_PICKER_CALENDAR_VIEW_FRAGMENT_TAG);
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
                mTitle.setTextColor(ContextCompat.getColor(this, R.color.edit_title_bar_text_night));
                mTitle.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_title_bar_night));
                mContent.setTextColor(Utils.getWhiteColor(this, R.color.edit_content_night, mLux));
                mContent.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_content_background_night));

                if (mCompactToolBar)
                    mEditorCompact.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_title_bar_night));
                else
                    mEditor.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_title_bar_night));

                mCanvasForeground = ContextCompat.getColor(this, R.color.canvas_foreground_night);
                mCanvasBackground = ContextCompat.getDrawable(this, R.drawable.canvas_night);
            } else if ((mTheme.equals(Const.DARK_THEME)) || (mode.equals(Const.DARK_THEME))) {
                mTitle.setTextColor(ContextCompat.getColor(this, R.color.edit_title_bar_text_dark));
                mTitle.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_title_bar_dark));
                mContent.setTextColor(Utils.getWhiteColor(this, R.color.edit_content_night, mLux));
                mContent.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_content_background_dark));

                if (mCompactToolBar)
                    mEditorCompact.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_title_bar_dark));
                else
                    mEditor.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_title_bar_dark));

                mCanvasForeground = ContextCompat.getColor(this, R.color.canvas_foreground_dark);
                mCanvasBackground = ContextCompat.getDrawable(this, R.drawable.canvas_dark);
            } else {
                mTitle.setTextColor(ContextCompat.getColor(this, R.color.edit_title_bar_text_day));
                mTitle.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_title_bar_day));
                mContent.setTextColor(ContextCompat.getColor(this, R.color.edit_content_day));
                mContent.setBackgroundColor(Utils.getWhiteColor(this, R.color.edit_content_background_day, mLux));

                if (mCompactToolBar)
                    mEditorCompact.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_title_bar_day));
                else
                    mEditor.setBackgroundColor(ContextCompat.getColor(this, R.color.edit_title_bar_day));

                mCanvasForeground = ContextCompat.getColor(this, R.color.canvas_foreground_day);
                mCanvasBackground = ContextCompat.getDrawable(this, R.drawable.canvas_day);
            }

            // Reset markdown render state
            setMarkdownRendered(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Set font family
    protected void setFontFamily(TextView view, String fontFamily) {
        boolean found = false;

        if (mFontFamily.equals(Const.SYSTEM_FONT_NAME)) {
            mContent.setTypeface(Typeface.DEFAULT);
            mMarkdownFontFamily = Const.NULL_SYM;
            found = true;
        } else if (fontFamily.equals("Monospace")) {
            view.setTypeface(Typeface.MONOSPACE);
            found = true;
        } else if (fontFamily.equals("Sans Serif")) {
            view.setTypeface(Typeface.SANS_SERIF);
            found = true;
        } else if (fontFamily.equals("Serif")) {
            view.setTypeface(Typeface.SERIF);
            found = true;
        } else if (fontFamily.equals("Roboto Light")) {
            view.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            found = true;
        } else if (fontFamily.equals("Roboto Medium")) {
            view.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            found = true;
        } else if (fontFamily.equals("Roboto Condensed Light")) {
            view.setTypeface(FontCache.getFromAsset(this, "RobotoCondensed-Light.ttf"));
            found = true;
        } else if (fontFamily.equals("Roboto Condensed Regular")) {
            view.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
            found = true;
        } else if (fontFamily.equals("Roboto Mono Light")) {
            view.setTypeface(FontCache.getFromAsset(this, "RobotoMono-Light.ttf"));
            found = true;
        } else if (fontFamily.equals("Roboto Mono Regular")) {
            view.setTypeface(FontCache.getFromAsset(this, "RobotoMono-Regular.ttf"));
            found = true;
        } else if (fontFamily.equals("Roboto Slab Light")) {
            view.setTypeface(FontCache.getFromAsset(this, "RobotoSlab-Light.ttf"));
            found = true;
        } else if (fontFamily.equals("Roboto Slab Regular")) {
            view.setTypeface(FontCache.getFromAsset(this, "RobotoSlab-Regular.ttf"));
            found = true;
        } else if (fontFamily.equals("Roboto Slab Bold")) {
            view.setTypeface(FontCache.getFromAsset(this, "RobotoSlab-Bold.ttf"));
            found = true;
        } else if (mCustomFonts != null) {
            try {
                CustomFont font = (CustomFont) mCustomFonts.get(fontFamily);
                if (font != null) {
                    String path = mLocalRepoPath + "/" + Const.CUSTOM_FONTS_PATH + "/" + font.getPath();
                    view.setTypeface(FontCache.getFromFile(path));
                    found = true;
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Default
        if (!found) {
            view.setTypeface(Typeface.SANS_SERIF);
        }
    }

    // Apply font family
    protected void applyFontFamily() {
        boolean found = false;

        if (mFontFamily.equals(Const.SYSTEM_FONT_NAME)) {
            mContent.setTypeface(Typeface.DEFAULT);
            mMarkdownFontFamily = "<style>div#content{font-size: " + mFontSize + "px}</style>";
            found = true;
        } else if (mFontFamily.equals("Roboto Condensed Light")) {
            mContent.setTypeface(FontCache.getFromAsset(this, "RobotoCondensed-Light.ttf"));
            mMarkdownFontFamily = "<style>@font-face { font-family: 'Roboto Condensed Light'; src: url('file:///android_asset/RobotoCondensed-Light.ttf') } div#content{font-family: 'Roboto Condensed Light'; font-weight: 300; font-size: " + mFontSize + "px}</style>";
            found = true;
        } else if (mFontFamily.equals("Roboto Mono Light")) {
            mContent.setTypeface(FontCache.getFromAsset(this, "RobotoMono-Light.ttf"));
            mMarkdownFontFamily = "<style>@font-face { font-family: 'Roboto Mono Light'; src: url('file:///android_asset/RobotoMono-Light.ttf') } div#content{font-family: 'Roboto Mono Light'; font-weight: 300; font-size: " + mFontSize + "px}</style>";
            found = true;
        } else if (mFontFamily.equals("Roboto Mono Regular")) {
            mContent.setTypeface(FontCache.getFromAsset(this, "RobotoMono-Regular.ttf"));
            mMarkdownFontFamily = "<style>@font-face { font-family: 'Roboto Mono'; src: url('file:///android_asset/RobotoMono-Regular.ttf') } div#content{font-family: 'Roboto Mono'; font-size: " + mFontSize + "px}</style>";
            found = true;
        } else if (mFontFamily.equals("Roboto Slab Light")) {
            mContent.setTypeface(FontCache.getFromAsset(this, "RobotoSlab-Light.ttf"));
            mMarkdownFontFamily = "<style>@font-face { font-family: 'Roboto Slab Light'; src: url('file:///android_asset/RobotoSlab-Light.ttf') } div#content{font-family: 'Roboto Slab Light'; font-weight: 300; font-size: " + mFontSize + "px}</style>";
            found = true;
        } else if (mFontFamily.equals("Roboto Slab Regular")) {
            mContent.setTypeface(FontCache.getFromAsset(this, "RobotoSlab-Regular.ttf"));
            mMarkdownFontFamily = "<style>@font-face { font-family: 'Roboto Slab Regular'; src: url('file:///android_asset/RobotoSlab-Regular.ttf') } div#content{font-family: 'Roboto Slab'; font-size: " + mFontSize + "px}</style>";
            found = true;
        } else if (mFontFamily.equals("Roboto Slab Bold")) {
            mContent.setTypeface(FontCache.getFromAsset(this, "RobotoSlab-Bold.ttf"));
            mMarkdownFontFamily = "<style>@font-face { font-family: 'Roboto Slab Bold'; src: url('file:///android_asset/RobotoSlab-Bold.ttf') } div#content{font-family: 'Roboto Slab Bold'; font-weight: 700; font-size: " + mFontSize + "px}</style>";
            found = true;
        } else if (mCustomFonts != null) {
            CustomFont font = (CustomFont) mCustomFonts.get(mFontFamily);
            if (font != null) {
                String path = mLocalRepoPath + "/" + Const.CUSTOM_FONTS_PATH + "/" + font.getPath();
                mContent.setTypeface(FontCache.getFromFile(path));
                mMarkdownFontFamily = font.getUrl();
                mMarkdownFontFamily += "<style>div#content{" + font.getCSS() + " font-size: " + mFontSize + "px}</style>";
                found = true;
            }
        }

        // Default
        if (!found) {
            mContent.setTypeface(FontCache.getFromAsset(this, "RobotoMono-Regular.ttf"));
            mMarkdownFontFamily = "<style>@font-face { font-family: 'Roboto Mono'; src: url('file:///android_asset/RobotoMono-Regular.ttf') } div#content{font-family: 'Roboto Mono'; font-size: " + mFontSize + "px}</style>";
        }

        // Reset markdown render state
        setMarkdownRendered(false);
    }

    // Apply font size
    protected void applyFontSize() {
        if ((Utils.pxToSp(this, mContent.getTextSize())) != Integer.parseInt(mFontSize)) {
            mContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Integer.parseInt(mFontSize));

            // Reset markdown render state
            setMarkdownRendered(false);
        }
    }

    // Scale font size
    // Scale font size
    protected void scaleFontSize(float factor, int pos) {
        String font_size = String.valueOf(Math.round(Integer.parseInt(mFontSize) * factor));

        if (!mFontSize.equals(font_size)) {  // Only when size has changed
            mFontSize = font_size;

            // Mark font scaled
            mFontScaled = true;

            // Set status
            updateStatus(new DecimalFormat("##.##").format(factor) + "x / " + mFontSize + "pt", mFadeIn);

            // Apply font size
            applyFontSize();
            applyFontFamily();

            // Resume content position
            if (pos > 0) {
                mAnchorPos = pos;
                mContent.setSelection(pos);
            }
        }
    }

    // Apply margin
    protected void applyMargin() {
        int padding = Utils.toDP(this, Integer.parseInt(mMargin));

        // mTitle.setPadding(padding, Utils.toDP(this, 5), Utils.toDP(this, 120), Utils.toDP(this, 2));
        // mStatusBar.setPadding(0, Utils.toDP(this, 2), padding, Utils.toDP(this, 2));  // mixed with layout values
        mContent.setPadding(padding, padding, padding, padding);

        mMarkdownMargin = "<style>body {margin-left:" + mMargin + "px; margin-right:" + mMargin + "px;}</style>";

        // Reset markdown render state
        setMarkdownRendered(false);
    }

    // Apply hacks
    private void applyHacks() {
        // Open in markdown view if so specified
        if ((mOpenInMarkdown) || (mMarkdownTrigger.length() > 0) && (mMetadata.contains(mMarkdownTrigger)))
            toggleMarkdownView();

        // Linkify
        applyLinkify();

        // Set math cache mode
        if (!mMarkdownLocalCache)
            mMarkdownCacheMode = WebSettings.LOAD_NO_CACHE;
    }

    // Apply linkify
    private void applyLinkify() {
        if ((mLinkifyTrigger.length() > 0) && (mMetadata.contains(mLinkifyTrigger))) {
            if (mContent.getText().toString().length() < Const.MAX_LINKIFY_FILE_SIZE) {    // Avoid lags from linkifying a large file
                mContent.setLinksClickable(true);
                mContent.setAutoLinkMask(Linkify.ALL);
                mContent.setMovementMethod(LinkMovementMethod.getInstance());
                mContent.setLinkTextColor(ContextCompat.getColor(this, R.color.linkify_color));
                Linkify.addLinks(mContent, Linkify.ALL);
            }
        }
    }

    // Show style popup
    private void showStylePopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        Menu menu = popup.getMenu();

        // Setup the popup
        inflater.inflate(R.menu.style_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(this);

        popup.show();
    }

    // Add status
    protected void addStatus(String status) {
        // Sanity check
        if (mStatusQ == null)
            mStatusQ = new ArrayList<String>();

        mStatusQ.add(status);
    }

    // Update status
    protected void updateStatus(String status, Animation animation) {
        if (mStatusBar.getVisibility() == View.GONE)
            return;  // do nothing

        if (mStatusQ.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : mStatusQ) {
                sb.append(s);
                sb.append(Const.NEWLINE);

                // Reload if this note has been updated
                if (s.equals(mTitleSaved + getApplicationContext().getResources().getString(R.string.status_updated_remotely)))
                    notifyChange(mTitleSaved);
            }

            status = sb.toString();

            mStatusQ.clear();

            // Add animated effect
            animation = mPushDownIn;
        }

        if (status == null) {
            // Provide basic statistics
            status = Utils.makeClipboardStatus(getApplicationContext(), mClipboard, Const.CLIPBOARD_PREVIEW_LEN, false);
        }

        if (animation != null)
            mStatusBar.startAnimation(animation);

        mStatusBar.setText(status);
    }

    // Show statistics
    protected void showStat() {
        // Provide basic statistics
        String content;

        // Get selection
        content = Utils.getCurrentSelection(mContent);
        if (content.length() == 0)
            content = mContent.getText().toString();    // Otherwise whole content

        long cur_line = Utils.getCurrentCursorLine(mContent);
        long word_count = Utils.countWords(content);
        String file_size = Utils.readableFileSize((long) content.length());

        String status = getResources().getString(R.string.word_count) + ": " + Utils.injectComma(word_count) + "  ";
        status += getResources().getString(R.string.file_size) + ": " + file_size + "  ";

        if (cur_line > 0)
            status += getResources().getString(R.string.line_number) + ": " + Utils.injectComma(cur_line);

        // Show statistics
        Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), status, Snackbar.LENGTH_LONG);
        Utils.anchorSnackbar(snackbar, R.id.fragment_content);
        snackbar.setAction(getResources().getString(R.string.snack_bar_button_done), mSnackbarOnClickListener).show();

        // Show clipboard
        updateStatus(null, mSlideDown);
    }

    // Show details
    protected void showDetails() {
        List<DBEntry> records;
        DBEntry entry;

        String title, preview;
        String status = mStatusBar.getText().toString();
        int end;

        // Show more details for remote updates
        if (status.endsWith(getResources().getString(R.string.status_updated_remotely))) {
            // Extract title
            end = status.length() - getResources().getString(R.string.status_updated_remotely).length();
            title = status.substring(0, end).trim();

            // Sanity check
            if (title.equals(mTitleSaved)) return;

            // Get content preview
            records = mDatasource.getRecordPreviewByTitle(title);
            if (records.size() > 0) {
                entry = records.get(0);
                preview = Utils.subStringWordBoundary(entry.getContent(), 1, Const.PREVIEW_LEN).replaceAll("\\s+", " ");

                // Show preview
                Snackbar snackbar = Utils.makeSnackbar(this, getCoordinatorLayout(), preview, 12);
                Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                snackbar.show();
            }
        }

        // Show more details of clipboard contents
        else if (Utils.makeClipboardStatus(getApplicationContext(), mClipboard, Const.CLIPBOARD_PREVIEW_LEN, false).equals(mStatusBar.getText()))
            handleShowClipboard();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d(Const.TAG, "nano -- onKeyDown");

            // Ignore if in immersive mode
            if (mImmersiveMode) {
                exitImmersiveMode();
                return false;
            }

            handleHome();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_D:
                if (event.isCtrlPressed())
                    doDefine();
                return true;
            case KeyEvent.KEYCODE_W:
                if (event.isCtrlPressed())
                    doWebSearch(false);
                return true;
            case KeyEvent.KEYCODE_M:
                if (event.isCtrlPressed())
                    toggleMarkdownView();
                return true;
            case KeyEvent.KEYCODE_F:
                if (event.isCtrlPressed())
                    showLocalFindFragment();
                return true;
            case KeyEvent.KEYCODE_H:
                if (event.isCtrlPressed())
                    showLocalReplaceFragment();
                return true;
            case KeyEvent.KEYCODE_F3:
                doGotoMatch(1, false);
                return true;
            case KeyEvent.KEYCODE_S:
                if (event.isCtrlPressed())
                    doSave(false, false);
                return true;
            case KeyEvent.KEYCODE_Z:
                if (event.isCtrlPressed()) {
                    if (event.isShiftPressed())
                        doRedo();
                    else
                        doUndo();
                }
                return true;
            case KeyEvent.KEYCODE_I:
                if (event.isCtrlPressed())
                    if (event.isShiftPressed())
                        Utils.unIndent(mCurrentEditText, mIndentChar);
                    else
                        Utils.insertMarkdownSymbolAutoIndent(mCurrentEditText, mIndentChar, mIndentChar);
                return true;
            case KeyEvent.KEYCODE_ENTER:
                Utils.insertMarkdownSymbolAutoIndent(mCurrentEditText, Const.NULL_SYM, mIndentChar);
                return true;
            case KeyEvent.KEYCODE_LEFT_BRACKET:
                if (event.isCtrlPressed())
                    Utils.unIndent(mCurrentEditText, mIndentChar);
                return true;
            case KeyEvent.KEYCODE_RIGHT_BRACKET:
                if (event.isCtrlPressed())
                    Utils.insertMarkdownSymbolAutoIndent(mCurrentEditText, mIndentChar, mIndentChar);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                if (event.isCtrlPressed())
                    doGoTo(true);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (event.isCtrlPressed())
                    doGoTo(false);
                return true;
            case KeyEvent.KEYCODE_EQUALS:
                if (event.isCtrlPressed())
                    doCalculate();
                return true;
            case KeyEvent.KEYCODE_TAB:
                if (event.isCtrlPressed())
                    doTextExpansion();
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    /////////////////
    // Save related
    ////////////////

    // Leave the editor
    private void leave() {
        // Remember editor position in any case
        if (mId > 0)
            doSavePos();

        finish();
    }

    // Notify change
    protected void notifyChange(File file) {
        String title;

        if (Utils.fileNameAsTitle(this))
            title = file.getName();
        else
            title = Utils.stripExtension("txt", file.getName());

        // Update status
        if (!Utils.isHiddenFile(title))
            updateStatus(title + getResources().getString(R.string.status_updated_remotely), null);

        notifyChange(title);
    }

    // Notify change
    protected void notifyChange(String title) {
        try {
            // Reload content if needed
            if ((mReloadSafe) && (title.equals(mTitleSaved))) {
                if (mId > 0) {
                    ArrayList<DBEntry> results = mDatasource.getRecordById(mId);
                    Date now = new Date();
                    if (results.size() == 1) {
                        // Backoff to avoid notifications from local updates
                        if ((now.getTime() - mModified.getTime()) > Const.BACKOFF) {
                            if (!results.get(0).getContent().equals(mContent.getText().toString()))
                                handleReload();
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Handle reload
    private void handleReload() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_reload_message).setTitle(R.string.dialog_reload_title);

        builder.setPositiveButton(R.string.dialog_reload_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                doReload();

                // Allow auto save
                mAutoSaveSafe = true;

                // Allow reload
                mReloadSafe = true;
            }
        });
        builder.setNegativeButton(R.string.dialog_reload_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                // Allow auto save
                mAutoSaveSafe = true;

                // Allow reload
                mReloadSafe = true;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                // Allow auto save
                mAutoSaveSafe = true;

                // Allow reload
                mReloadSafe = true;
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                // Allow auto save
                mAutoSaveSafe = true;

                // Allow reload
                mReloadSafe = true;
            }
        });

        // Disallow auto save
        mAutoSaveSafe = false;

        // Disallow reload
        mReloadSafe = false;

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Reload the record
    private void doReload() {
        if (mId > 0) {
            ArrayList<DBEntry> results = mDatasource.getRecordById(mId);
            String status, content;
            long old_size, new_size;

            if (results.size() == 1) {
                mTitleSaved = results.get(0).getTitle();
                mContentSaved = results.get(0).getContent();
                mStar = results.get(0).getStar();
                mMetadata = results.get(0).getMetadata();
                mModified = results.get(0).getModified();

                content = mContent.getText().toString();

                old_size = content.length();
                new_size = mContentSaved.length();

                if (new_size > old_size)
                    status = Const.SIZE_UP_SYM + Utils.readableFileSize(new_size - old_size);

                else if (new_size < old_size)
                    status = Const.SIZE_DOWN_SYM + Utils.readableFileSize(old_size - new_size);

                else
                    status = mMetadata;

                mTitle.setText(mTitleSaved);
                mContent.setText(mContentSaved);

                // Set criteria
                mCriteria = null;

                // Set focus
                mContent.setSelection((int) mContentSaved.length() - 1);
                mContent.requestFocus();

                // Update access time
                doSaveAccessTime();

                // Reset markdown render state
                setMarkdownRendered(false);

                // Reload markdown if needed
                if (mMarkdownMode)
                    showHideMarkdown(mMarkdownMode);

                // Set status
                updateStatus(status, mSlideDown);

                // Show revision summary
                if (mLabMode)
                    handleRevisionSummary(content, mContentSaved);
            }
        }
    }

    // Handle revision summary
    private void handleRevisionSummary(String original, String revised) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        WebView webview = new WebView(this);

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);                           // For accessibility
        settings.setBuiltInZoomControls(true);                         // Allow zoom
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(false);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);    // Offline cache

        // Build source
        String source = Const.REVISION_CSS + Utils.getRevisionSummary(this, original, revised);

        // Load revision
        webview.loadDataWithBaseURL(Const.PREFIX_FILE + mLocalRepoPath + "/", source, "text/html", "utf-8", null);
        webview.setWebViewClient(new WebViewClient() {});

        builder.setView(webview);
        builder.setPositiveButton(DBApplication.getAppContext().getResources().getString(R.string.dialog_revision_summary_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Add some animations
        webview.startAnimation(mSlideDown);
    }

    // Handle save
    private void handleSave() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_save_message).setTitle(R.string.dialog_save_title);

        builder.setPositiveButton(R.string.dialog_save_quit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                doSave(false, true);

                // Allow auto save
                mAutoSaveSafe = true;
            }
        });
        builder.setNegativeButton(R.string.dialog_save_edit, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doSave(false, false);

                // Allow auto save
                mAutoSaveSafe = true;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                // Allow auto save
                mAutoSaveSafe = true;
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                // Allow auto save
                mAutoSaveSafe = true;
            }
        });

        // Disallow auto save
        mAutoSaveSafe = false;

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Save the record
    private void doSave(boolean overwrite, boolean exit) {
        String title = mTitle.getText().toString();
        String content = mContent.getText().toString();
        DBEntry entry;

        // Title is missing
        if (title.length() == 0) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_empty_title), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            showHideTitle(true);
            return;
        }

        if (!Utils.validateTitle(title)) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_invalid_title), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            showHideTitle(true);
            return;
        }

        if (!Utils.validateFileSize(content)) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_invalid_file_size), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            return;
        }

        // In case of direct uri edit, skip database update
        if (mUri != null) {
            Utils.writeContentToUri(getApplicationContext(), mUri, content);
            return;
        }

        // Save a copy of the last saved version if the note content is blank
        if ((mKeepDeletedCopies) && ((content == null) || (content.length() == 0))) {
            if ((mContentSaved != null) && (mContentSaved.length() > 0)) {
                Utils.writeSpecialSAFFile(getApplicationContext(), mBackupUri, Const.TRASH_PATH, Utils.makeDeletedTitle(title), mContentSaved);
            }
            else if ((mContentAtOpen != null) && (mContentAtOpen.length() > 0)) {
                Utils.writeSpecialSAFFile(getApplicationContext(), mBackupUri, Const.TRASH_PATH, Utils.makeDeletedTitle(title), mContentAtOpen);

            }
        }

        if (mId > 0) {
            mId = mDatasource.updateRecord(mId, title, content, mStar, null, true, mTitleSaved);
            // Purge old title from mirror in the event of a title change, updated title will be added by next mirroring
            if ((!title.equals(mTitleSaved)) && (hasMirror())) {
                Utils.deleteSAFSubDirFile(getApplicationContext(), mBackupUri, Const.MIRROR_PATH, mTitleSaved);
            }
        }
        else {
            ArrayList<DBEntry> results = mDatasource.getRecordByTitle(title);
            if (results.size() > 0) {
                entry = results.get(0);
                if (!overwrite) {
                    handleDuplicates(exit);
                    return;
                } else {
                    mId = entry.getId();
                    mId = mDatasource.updateRecord(mId, title, content, mStar, null, true, mTitleSaved);
                }
            } else {
                // Append default file extension if one is not specified
                if ((Utils.fileNameAsTitle(this)) && (!title.contains(".")))
                    title += mNewNoteFileType;

                entry = mDatasource.createRecord(title, content, 0, null, true);
                mId = entry.getId();

                // Update that intent that started this activity
                Intent intent = getIntent();
                intent.putExtra(Const.EXTRA_ID, mId);
            }
        }

        // Update other information
        doSavePos();
        if (mLocationAware) {
            Location location = Utils.getLocation(getApplicationContext());
            if (location != null)
                mDatasource.updateRecordCoordinates(mId, location.getLatitude(), location.getLongitude());
        }

        // Update app data if needed
        if (!mTitleSaved.equals(title))
            mActivity.doBasicAppDataBackup();

        // Handle exit
        if (exit)
            leave();

        // Updated saved content
        mTitleSaved = title;
        mContentSaved = content;

        // Toggle changes status
        mChanged = false;
        toggleChanges();

        // Linkify
        applyLinkify();

        // Update modified time
        ArrayList<DBEntry> results = mDatasource.getRecordById(mId);
        if (results.size() > 0) {
            mModified = results.get(0).getModified();
        }
    }

    // Save record position
    private void doSavePos() {
        if (mId > 0)
            mDatasource.updateRecordPos(mId, mContent.getSelectionStart());
    }

    // Save access time
    private void doSaveAccessTime() {
        if (mId > 0)
            mDatasource.updateRecordAccessTime(mId);
    }

    // Do revert
    private void doRevert() {
        mTitle.setText(mTitleAtOpen);
        mContent.setText(mContentAtOpen);
        mContent.setSelection((int) mPosAtOpen);

        // Reset criteria
        mCriteria = null;
        mHitIdx = -1;
        mHits.clear();

        // Reset markdown render state
        setMarkdownRendered(false);

        // Update status
        updateStatus(mTitleAtOpen + getResources().getString(R.string.status_reverted), mZoomIn);
    }

    // Handle metadata
    private void handleMetadata() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        String msg = DBApplication.getAppContext().getResources().getString(R.string.dialog_metadata_message);
        builder.setMessage(msg).setTitle(R.string.dialog_metadata_title);

        // Set up text boxes
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add an edit field for metadata search
        final AutoCompleteTextView metadata_str = new AutoCompleteTextView(mActivity);

        // Setup auto complete
        String[] tags = mDatasource.getUniqueMetadata();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, R.layout.dropdown_list_item, tags);
        metadata_str.setAdapter(adapter);

        metadata_str.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        metadata_str.setText(mMetadata);
        metadata_str.requestFocus();

        // Set star checkbox
        final SwitchCompat star = new SwitchCompat(this);
        star.setThumbDrawable(getResources().getDrawable(R.drawable.ic_star_vector));
        star.setChecked(mStar == 1);

        layout.addView(metadata_str);
        layout.addView(star);
        layout.setGravity(Gravity.CENTER);

        builder.setView(layout);

        builder.setPositiveButton(R.string.dialog_metadata_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String metadata = metadata_str.getText().toString().trim().replaceAll("\\r\\n|\\r|\\n|" + Const.DELIMITER + "|" + Const.SUBDELIMITER, " ");
                boolean apply_star = star.isChecked();

                if (apply_star)
                    doSetMetadataStar(mId, metadata, 1);
                else
                    doSetMetadataStar(mId, metadata, 0);

                // Reset Markdown render state
                setMarkdownRendered(false);

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_metadata_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return; // Do nothing
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    // Set metadata
    protected void doSetMetadata(long id, String metadata) {
        long count = mDatasource.updateRecordMetadata(id, metadata);

        if (count > 0) {
            mMetadata = metadata;
            updateStatus(mMetadata, mSlideDown);
        } else {
            updateStatus(getResources().getString(R.string.status_no_change), mBounce);
        }

        // Backup basic app data
        mActivity.doBasicAppDataBackup();

        // Show/hide toolbar
        if (mShowToolBar)
            mShowToolBar = !((mAutoToolBarTag.length() > 0) && (mMetadata.contains(mAutoToolBarTag)));

        // Linkify
        applyLinkify();
    }

    // Set metadata and star
    protected void doSetMetadataStar(long id, String metadata, int star) {
        long count = mDatasource.updateRecordMetadata(id, metadata);

        // Update metadata
        if (count > 0) {
            mMetadata = metadata;
            updateStatus(mMetadata, mSlideDown);
        } else {
            updateStatus(getResources().getString(R.string.status_no_change), mBounce);
        }

        // Toggle star
        mDatasource.updateRecordStarStatus(id, star);
        mStar = star;

        // Backup basic app data
        mActivity.doBasicAppDataBackup();

        // Show/hide toolbar
        if (mShowToolBar)
            mShowToolBar = !((mAutoToolBarTag.length() > 0) && (mMetadata.contains(mAutoToolBarTag)));

        // Linkify
        applyLinkify();
    }

    // Handle revert
    private void handleRevert() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        String msg = getResources().getString(R.string.dialog_revert_message);

        // Some info about the revert
        msg += "\n\n";
        msg += Utils.countWords(mContentAtOpen) + getResources().getString(R.string.status_word_count);
        msg += Const.REVERT_SYM;
        msg += Utils.countWords(mContent.getText().toString()) + getResources().getString(R.string.status_word_count);

        builder.setMessage(msg).setTitle(R.string.dialog_revert_title);

        builder.setPositiveButton(R.string.dialog_revert_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Revert
                doRevert();
                mAutoSaveSafe = true;
            }
        });
        builder.setNegativeButton(R.string.dialog_revert_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Allow auto save
                mAutoSaveSafe = true;
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mAutoSaveSafe = true;
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                // Allow auto save
                mAutoSaveSafe = true;
            }
        });

        // Disallow auto save
        mAutoSaveSafe = false;

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Handle duplicates
    private void handleDuplicates(boolean exit) {

        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_duplicates_message).setTitle(R.string.dialog_duplicates_title);

        final boolean exit_mode = exit;

        builder.setPositiveButton(R.string.dialog_duplicates_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Save into the existing record
                doSave(true, exit_mode);
                mAutoSaveSafe = true;
                return;
            }
        });
        builder.setNeutralButton(R.string.dialog_duplicates_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Discard content
                if (exit_mode)
                    leave();
                return;
            }
        });
        builder.setNegativeButton(R.string.dialog_duplicates_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mAutoSaveSafe = true;
                return;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mAutoSaveSafe = true;
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                mAutoSaveSafe = true;
            }
        });

        // Disallow auto save
        mAutoSaveSafe = false;

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Handle home button
    private void handleHome() {
        String title = mTitle.getText().toString();
        String content = mContent.getText().toString();

        // Return if no changes
        if ((title.equals(mTitleSaved)) && (content.equals(mContentSaved))) {
            leave();
            return;
        }

        // Save and exit if auto save is on
        if ((mAutoSave) && (mAutoSaveSafe)) {
            doSave(false, true);
            return;
        }

        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        String msg = getResources().getString(R.string.dialog_home_message);

        builder.setMessage(msg).setTitle(R.string.dialog_home_title);

        builder.setPositiveButton(R.string.dialog_home_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doSave(false, true);
            }
        });
        builder.setNegativeButton(R.string.dialog_home_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                leave();
            }
        });

        //  Get the AlertDialog from create()
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
    }

    // Handle encrypt decrypt
    private void handleEncryptDecrypt() {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_encrypt_decrypt_message).setTitle(R.string.dialog_encrypt_decrypt_title);
        builder.setPositiveButton(R.string.dialog_encrypt_decrypt_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new EncryptTask(DisplayDBEntry.this).executeOnExecutor(CustomAsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new EncryptTask(DisplayDBEntry.this).execute();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            }
        });

        builder.setNeutralButton(R.string.dialog_encrypt_decrypt_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        new DecryptTask(DisplayDBEntry.this).executeOnExecutor(CustomAsyncTask.THREAD_POOL_EXECUTOR);
                    } else {
                        new DecryptTask(DisplayDBEntry.this).execute();
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_encrypt_decrypt_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return; // Do nothing
            }
        });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
            }
        });

        // 4. Show the dialog
        dialog.show();

        // 5. Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Handle export
    private void handleExport() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setMessage(R.string.dialog_export_message).setTitle(R.string.dialog_export_title);

        // Set view
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        builder.setView(view);

        // Setup values
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numbers);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        final String[] items = Const.EXPORT_TYPES;

        if (items.length > 0) {
            picker.setMinValue(0);
            picker.setMaxValue(items.length - 1);
            picker.setDisplayedValues(items);
            picker.setWrapSelectorWheel(true);  // Wrap around
        }

        // Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_export_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                String export_type = items[picker.getValue()];
                if (export_type.equals(Const.FILE_EXTENSION_HTML))
                    doExportHTML(false);

                else if (export_type.equals(Const.FILE_EXTENSION_MARKDOWN))
                    doExportMarkdown();

                // Allow auto save
                mAutoSaveSafe = true;

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_export_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Allow auto save
                mAutoSaveSafe = true;

                // Do nothing
                return;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.show();

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mAutoSaveSafe = true;
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                mAutoSaveSafe = true;
            }
        });

        // Disallow auto save
        mAutoSaveSafe = false;

        // Show the dialog
        dialog.show();
    }

    // Encrypt task
    private class EncryptTask extends CustomAsyncTask<Void, Integer, Long> {
        private DisplayDBEntry activity;
        private String temp;

        public EncryptTask(DisplayDBEntry activity) {
            this.activity = activity;
        }

        @Override
        protected Long doInBackground(Void... arg0) {
            long totalSize = 0;

            try {
                temp = Base64.encodeToString(temp.getBytes("UTF-8"), Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            // Get content
            if (mContent.getSelectionStart() < mContent.getSelectionEnd())
                temp = Utils.getCurrentSelection(mContent);    // Get selected text
            else
                temp = mContent.getText().toString();

            // Save cursor position
            doSavePos();

            // Show the progress bar
            updateStatus(getResources().getString(R.string.status_encrypting), null);
            ProgressBar progressBar = findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Long result) {

            // Hide the progress bar when completed
            ProgressBar progressBar = findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);

            // Reset markdown render state
            setMarkdownRendered(false);

            // Call back
            activity.onEncryptTaskFinished(temp);
        }

        @Override
        protected void onCancelled() {
        }
    }

    // Finish up encryption
    public void onEncryptTaskFinished(String temp) {
        // Turn off suggestions
        mContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

        if (temp.length() > 0) {
            if (mContent.getSelectionStart() < mContent.getSelectionEnd())
                Utils.replaceSelection(mContent, temp);    // Replace selected text
            else
                mContent.setText(temp);

            // Clear search hits
            doClearSearch();

            updateStatus(getResources().getString(R.string.status_encrypted), mFadeIn);
        } else {
            updateStatus(getResources().getString(R.string.info_cancel_encrypt), mBounce);
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.info_cancel_encrypt), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }

        // Allow auto save
        mAutoSaveSafe = true;

        // Show toolbar always to avoid data corruption from accidental taps
        mShowToolBar = true;
    }

    // Decrypt task
    private class DecryptTask extends CustomAsyncTask<Void, Integer, Long> {
        private DisplayDBEntry activity;
        private String content;
        private byte[] temp = new byte[0];

        public DecryptTask(DisplayDBEntry activity) {
            this.activity = activity;
        }

        @Override
        protected Long doInBackground(Void... arg0) {
            long totalSize = 0;

            try {
                temp = Base64.decode(content, Base64.DEFAULT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            // Disallow auto save
            mAutoSaveSafe = false;

            // Get content
            if (mContent.getSelectionStart() < mContent.getSelectionEnd())
                content = Utils.getCurrentSelection(mContent);    // Get selected text
            else
                content = mContent.getText().toString();

            // Show the progress bar
            updateStatus(getResources().getString(R.string.status_decrypting), mFadeIn);
            ProgressBar progressBar = findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Long result) {
            // Hide the progress bar when completed
            ProgressBar progressBar = findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);

            // Call back
            try {
                activity.onDecryptTaskFinished(new String(temp, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        @Override
        protected void onCancelled() {

        }
    }

    // Finish up decryption
    public void onDecryptTaskFinished(String temp) {
        // Turn on suggestions
        mContent.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);

        try {
            if (temp.length() > 0) {
                if (mContent.getSelectionStart() < mContent.getSelectionEnd())
                    Utils.replaceSelection(mContent, temp);    // Replace selected text
                else {
                    mContent.setText(temp);
                    mContent.setSelection(temp.length());  // Set cursor
                }

                updateStatus(getResources().getString(R.string.status_decrypted), mFadeIn);
            } else {
                updateStatus(getResources().getString(R.string.info_cancel_decrypt), mBounce);
                Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.info_cancel_decrypt), Snackbar.LENGTH_SHORT);
                Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                snackbar.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Disable auto save
        mAutoSaveSafe = false;

        // Show toolbar always to avoid data corruption from accidental taps
        mShowToolBar = true;
    }

    // Do OpenKeyChain encrypt
    private void doOKCEncrypt() {
        try {
            // Get content
            String content;
            int start, end;

            if (mContent.getSelectionStart() < mContent.getSelectionEnd())
                content = Utils.getCurrentSelection(mContent);    // Get selected text
            else {
                content = mContent.getText().toString();

                // Pre-select the snippet to be replaced
                start = 0;
                end = content.length();
                mContent.setSelection(start, end);
            }

            // Disable auto save
            mAutoSaveSafe = false;

            String size = Utils.readableFileSize((long) content.length());
            Toast.makeText(this, size, Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(Const.OKC_ENCRYPT_TEXT);
            intent.putExtra(Const.OKC_ENCRYPT_EXTRA_TEXT, content);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            updateStatus(getResources().getString(R.string.info_cancel_encrypt), mBounce);
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.info_cancel_encrypt), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }

    // Do OpenKeyChain decrypt
    private void doOKCDecrypt() {
        try {
            // Get content
            String content;
            int start, end, current;

            if (mContent.getSelectionStart() < mContent.getSelectionEnd())
                content = Utils.getCurrentSelection(mContent);    // Get selected text
            else
                content = Utils.getEnclosedSnippet(mContent, Const.OKC_BEGIN_SYM, Const.OKC_END_SYM);

            // Disable auto save
            mAutoSaveSafe = false;

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setClassName(Const.OKC_PACKAGE_NAME, Const.OKC_DECRYPT_ACTIVITY);
            intent.setType(Const.PLAIN_TEXT_TYPE);
            intent.putExtra(intent.EXTRA_TEXT, content);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            updateStatus(getResources().getString(R.string.info_cancel_decrypt), mBounce);
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.info_cancel_decrypt), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }

    // Do OCR
    private void doOCR() {
        if (!Utils.launchPackage(getApplicationContext(), Const.OCR_PACKAGE_NAME)) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT).setAction(getResources().getString(R.string.button_ok), mSnackbarOnClickListener);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }

    // Insert location stamp
    private void doInsertLocationStamp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            new InsertLocationStampTask(DisplayDBEntry.this).executeOnExecutor(CustomAsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            new InsertLocationStampTask(DisplayDBEntry.this).execute();
        }
    }

    // Insert location stamp task
    private class InsertLocationStampTask extends CustomAsyncTask<Void, Integer, Long> {
        private DisplayDBEntry activity;
        private String temp;
        private int pos;

        public InsertLocationStampTask(DisplayDBEntry activity) {
            this.activity = activity;
        }

        @Override
        protected Long doInBackground(Void... arg0) {
            long totalSize = 0;

            try {
                temp = Utils.getAddress(activity);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            // Remember editor position
            pos = mContent.getSelectionStart();

            // Show the progress bar
            ProgressBar progressBar = findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Long result) {
            // Hide the progress bar when completed
            ProgressBar progressBar = findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);

            // Insert location
            onInsertLocationStampTaskFinished(temp, pos);
        }

        @Override
        protected void onCancelled() {
        }
    }

    // Finish up insert location stamp
    public void onInsertLocationStampTaskFinished(String temp, int pos) {
        if ((temp != null) && (temp.length() > 0)) {
            if (temp.startsWith(getResources().getString(R.string.error_location_unknown))) {
                Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_location_unknown), Snackbar.LENGTH_SHORT);
                Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                snackbar.show();
            }
            else {
                // Best effort returning to the last editing position
                try {
                    mContent.setSelection(pos);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Utils.insert(mContent, temp);
            }
        }
        else {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_network), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }

    // Handle font family
    private void handleFontFamily() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        final TextView title = new TextView(this);
        title.setText(R.string.dialog_font_family_title);
        title.setTextSize(Const.DIALOG_TITLE_SIZE);
        title.setPadding(Const.DIALOG_PADDING, Const.DIALOG_PADDING, Const.DIALOG_PADDING, Const.DIALOG_PADDING);
        setFontFamily(title, mFontFamily);
        builder.setCustomTitle(title);

        // Set view
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        builder.setView(view);

        // Setup values
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numbers);

        // Build a list of available fonts
        List<String> fonts = new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.font_family_values)));
        if (mCustomFonts != null) {
            Iterator iterator = mCustomFonts.entrySet().iterator();
            while (iterator.hasNext()) {
                HashMap.Entry pair = (HashMap.Entry) iterator.next();
                fonts.add(pair.getKey().toString());
            }
        }

        // Sort the list
        Collections.sort(fonts, new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }
        });

        // Set up the picker
        final String[] items = fonts.toArray(new String[fonts.size()]);
        int pos = fonts.indexOf(mFontFamily);

        if (items.length > 0) {
            picker.setMinValue(0);
            picker.setMaxValue(items.length - 1);
            picker.setDisplayedValues(items);

            if (pos > 0)
                picker.setValue(pos);

            picker.setWrapSelectorWheel(true);  // Wrap around
        }

        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                setFontFamily(title, items[picker.getValue()]);
            }
        });

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_font_family_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                mFontFamily = items[picker.getValue()];

                mSharedPreferencesEditor.putString(Const.PREF_FONT_FAMILY, mFontFamily);
                mSharedPreferencesEditor.commit();

                applyFontFamily();

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_font_family_cancel, new DialogInterface.OnClickListener() {
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

    // Handle font size
    private void handleFontSize() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.dialog_font_size_title);

        // Set view
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        builder.setView(view);

        // Setup values
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numbers);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        final String[] items = mFontSizeList.split(";");
        int pos = Arrays.asList(items).indexOf(String.valueOf(mFontSize));

        if (items.length > 0) {
            picker.setMinValue(0);
            picker.setMaxValue(items.length - 1);
            picker.setDisplayedValues(items);

            if (pos > 0)
                picker.setValue(pos);

            picker.setWrapSelectorWheel(true);  // Wrap around
        }

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_font_size_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                mFontSize = items[picker.getValue()];

                mSharedPreferencesEditor.putString(Const.PREF_FONT_SIZE, mFontSize);
                mSharedPreferencesEditor.commit();

                applyFontSize();
                applyFontFamily();

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_font_size_cancel, new DialogInterface.OnClickListener() {
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

    // Handle margin
    private void handleMargin() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.dialog_margin_title);

        // Set view
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        builder.setView(view);

        // Setup values
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numbers);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        final String[] items = mMarginList.split(";");
        int pos = Arrays.asList(items).indexOf(String.valueOf(mMargin));

        if (items.length > 0) {
            picker.setMinValue(0);
            picker.setMaxValue(items.length - 1);
            picker.setDisplayedValues(items);

            if (pos > 0)
                picker.setValue(pos);

            picker.setWrapSelectorWheel(true);  // Wrap around
        }

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_margin_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                mMargin = items[picker.getValue()];

                mSharedPreferencesEditor.putString(Const.PREF_MARGIN, mMargin);
                mSharedPreferencesEditor.commit();

                applyMargin();

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_margin_cancel, new DialogInterface.OnClickListener() {
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

    // Handle theme
    private void handleTheme() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final String[] names = getResources().getStringArray(R.array.pref_theme_names);
        final String[] values = getResources().getStringArray(R.array.pref_theme_values);

        // Get selected index
        int idx = Arrays.asList(values).indexOf(mTheme);

        builder.setSingleChoiceItems(names,
                idx,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        mTheme = values[item];

                        // Save theme settings
                        mSharedPreferencesEditor.putString(Const.PREF_THEME, mTheme);
                        mSharedPreferencesEditor.commit();

                        // Recreate activity to reload theme
                        // Note: recreate activity only when there is no unsaved changes or markdown magnifier is inactive
                        if ((mChanged) || (mMarkdownMagnifier != null)) {
                            applyTheme();

                            // Reset magnifier
                            mMarkdownMagnifier = null;
                        }
                        else {
                            doSavePos();
                            recreate();
                        }

                        dialog.dismiss();
                    }
                });

        // 3. Get the AlertDialog from create()
        AlertDialog dialog = builder.show();

        // 4. Show the dialog
        dialog.show();
    }

    // Paste into calendar
    private void doPasteCalendar() {
        String temp;

        // Get selected text, fallback to clipboard if not available
        temp = Utils.getCurrentSelection(mContent);
        if (temp.length() == 0)
            temp = Utils.getClipboardText(getApplicationContext(), mClipboard, -1, false);

        // Sanity check
        if (temp.length() == 0) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.status_empty_clipboard), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            return;
        }

        String title = mTitle.getText().toString();
        String link = Const.BLANK_LINE + Utils.createNoteLinkFromClipboard(getApplicationContext(), title, mClipboard);

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.Events.TITLE, title)
                .putExtra(CalendarContract.Events.DESCRIPTION, temp + link);

        if (Utils.isIntentAvailable(this, intent))
            startActivity(intent);
        else {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_no_calendar_app), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }

    // Do share
    private void doShare() {
        try {
            String title = mTitle.getText().toString();
            String content;
            Intent shareIntent;

            // Get text to share
            content = Utils.getCurrentSelection(mContent);
            if (content.length() == 0)
                content = mContent.getText().toString();

            // The intent does not have a URI, so declare the "text/plain" MIME type
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(Const.PLAIN_TEXT_TYPE);
            shareIntent.putExtra(Intent.EXTRA_TEXT, title + "\n\n" + content);

            startActivity(shareIntent);
        }
        catch (Exception e) {
            e.printStackTrace();
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_unexpected), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();        }
    }

    // Do clear cache
    private void doClearCache() {
        // Clear markdown view cache
        if (mMarkdownView != null) {
            mMarkdownView.clearCache(true);
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.info_cache_cleared), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }

        // Clear font cache
        FontCache.clear();

        // Clear search history
        LocalFindHistory.clear();

        // Clear replace history
        LocalReplaceHistory.clear();

        // Reset markdown render state
        setMarkdownRendered(false);
    }

    // Toggle change status
    protected void toggleChanges() {
        if (mChanged) {
            mStatusBar.setTextColor(ContextCompat.getColor(this, R.color.unsaved_color));
            mStatusBar.setTypeface(null, Typeface.ITALIC);

            // Reset auto save timer
            if (mAutoSaveHandler != null) {
                mAutoSaveHandler.removeCallbacks(mAutoSaveRunnable);
                mAutoSaveHandler.postDelayed(mAutoSaveRunnable, mAutoSaveInterval * Const.ONE_SECOND);
            }
        } else {
            mStatusBar.setTextColor(ContextCompat.getColor(this, R.color.edit_status_bar));
            mStatusBar.setTypeface(null, Typeface.NORMAL);
        }
    }

    // Show/hide title
    protected void showHideTitle(boolean visible) {
        if (visible) {
            mStatusBar.startAnimation(mPushDownIn);
            mTitle.setVisibility(View.VISIBLE);
            mTitleBar.setVisibility(View.VISIBLE);
            mTitleBarVisible = true;
        } else {
            mStatusBar.startAnimation(mSlideUp);
            mTitle.setVisibility(View.GONE);
            mTitleBar.setVisibility(View.GONE);
            mTitleBarVisible = false;
        }

        // Refresh the menu
        invalidateOptionsMenu();
    }

    // Show/hide tool bar
    protected void showHideToolBar(boolean visible) {
        try {
            if (visible) {
                mToolBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                mToolBar.setVisibility(View.VISIBLE);

                mTitleBar.animate().translationY(0).setInterpolator(new DecelerateInterpolator()).start();
                mTitleBar.setVisibility(View.VISIBLE);
            } else {
                mToolBar.animate().translationY(-mToolBar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                mToolBar.setVisibility(View.GONE);

                mTitleBar.animate().translationY(-mTitleBar.getBottom()).setInterpolator(new AccelerateInterpolator()).start();
                mTitleBar.setVisibility(View.GONE);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_style_font_family:
                handleFontFamily();
                return true;
            case R.id.menu_style_font_size:
                handleFontSize();
                return true;
            case R.id.menu_style_margin:
                handleMargin();
                return true;
            case R.id.menu_style_theme:
                handleTheme();
                return true;
            default:
                return false;
        }
    }

    /////////////////////
    // Fragment related
    /////////////////////
    @Override
    public void showHelp(View view) {
        int id = view.getId();

        if (id == R.id.button_text_expand)
            handleShowShortcuts();

        Toast.makeText(this, view.getContentDescription(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkdownSymbolSelected(int id) {
        if (id == (R.id.button_close)) {
            closeTopFragment();
        } else if (id == (R.id.button_indent)) {
            Utils.insertMarkdownSymbolAutoIndent(mCurrentEditText, mIndentChar, mIndentChar);
        } else if (id == (R.id.button_unindent)) {
            Utils.unIndent(mCurrentEditText, mIndentChar);
        } else if (id == (R.id.button_hash)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, "#");
        } else if (id == (R.id.button_asterisk)) {
            Utils.insertMarkdownSymbolPairOrMultiLine(mCurrentEditText, "*", "*", "*", mIndentChar);
        } else if (id == (R.id.button_grave_accent)) {
            Utils.insertMarkdownSymbolPair(mCurrentEditText, "`", "`", "`", null);
        } else if (id == (R.id.button_quotation)) {
            Utils.insertMarkdownSymbolPair(mCurrentEditText, "\"", "\"", "\"", null);
        } else if (id == (R.id.button_plus)) {
            Utils.fillMarkdownSymbolOrMultiLine(mCurrentEditText, "+", mIndentChar);
        } else if (id == (R.id.button_dash)) {
            Utils.fillMarkdownSymbolOrMultiLine(mCurrentEditText, "-", mIndentChar);
        } else if (id == (R.id.button_equal)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, "=");
        } else if (id == (R.id.button_vertical)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, "|");
        } else if (id == (R.id.button_backslash)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, "\\");
        } else if (id == (R.id.button_slash)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, "/");
        } else if (id == (R.id.button_colon)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, ":");
        } else if (id == (R.id.button_semicolon)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, ";");
        } else if (id == (R.id.button_bracket_left)) {
            Utils.insertMarkdownSymbolPair(mCurrentEditText, "(", ")", "(", null);
        } else if (id == (R.id.button_bracket_right)) {
            Utils.insertMarkdownSymbolPair(mCurrentEditText, "(", ")", ")", null);
        } else if (id == (R.id.button_square_bracket_left)) {
            Utils.insertMarkdownSymbolPair(mCurrentEditText, "[", "]", "[", null);
        } else if (id == (R.id.button_square_bracket_right)) {
            Utils.insertMarkdownSymbolPair(mCurrentEditText, "[", "]", "]", null);
        } else if (id == (R.id.button_bracket_less)) {
            Utils.insertMarkdownSymbolPair(mCurrentEditText, "<", ">", "<", null);
        } else if (id == (R.id.button_bracket_greater)) {
            Utils.insertMarkdownSymbolPairOrMultiLine(mCurrentEditText, "<", ">", ">", null);
        } else if (id == (R.id.button_curly_bracket_left)) {
            Utils.insertMarkdownSymbolPair(mCurrentEditText, "{", "}", "{", null);
        } else if (id == (R.id.button_curly_bracket_right)) {
            Utils.insertMarkdownSymbolPair(mCurrentEditText, "{", "}", "}", null);
        } else if (id == (R.id.button_underscore)) {
            Utils.fillMarkdownSymbol(mCurrentEditText, "_", null);
        } else if (id == (R.id.button_dollar)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, "$");
        } else if (id == (R.id.button_bang)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, "!");
        } else if (id == (R.id.button_question)) {
            Utils.insertMarkdownSymbol(mCurrentEditText, "?");
        }
    }

    @Override
    public void onEditToolSelected(int id) {
        if (id == R.id.button_close) {
            mEditToolFragmentVisible = false;
            closeTopFragment();
        } else if (id == R.id.button_save) {
            if ((mAutoSave) && (mAutoSaveSafe))
                doSave(false, false);
            else
                handleSave();
        } else if (id == R.id.button_undo) {
            doUndo();
        } else if (id == R.id.button_redo) {
            doRedo();
        } else if (id == R.id.button_markdown) {
            showMarkdownSymbolFragment();
        } else if (id == R.id.button_timestamp) {
            Utils.insert(mContent, Utils.getDateFormat(this, mCustomDateFormat).format(new Date()) + Const.SPACE_CHAR + Utils.getTimeFormat(this, mCustomTimeFormat).format(new Date()));
        } else if (id == R.id.button_datestamp) {
            showDatePickerCalendarViewFragment();
        } else if (id == R.id.button_local_find) {
            if ((mCriteria != null) && (mHits.size() > 0))
                handleHitsNavigation();
            else
                showLocalFindFragment();
        } else if (id == R.id.button_locationstamp) {
            doInsertLocationStamp();
        } else if (id == R.id.button_text_expand) {
            doTextExpansion();
        } else if (id == R.id.button_draw) {
            handleDraw();
        } else if (id == R.id.button_top) {
            doGoTo(true);
        } else if (id == R.id.button_bottom) {
            doGoTo(false);
        } else if (id == R.id.button_local_replace) {
            showLocalReplaceFragment();
        } else if (id == R.id.button_barcode) {
            handleInsertBarcode();
        } else if (id == R.id.button_image) {
            handleInsertImage();
        } else if (id == R.id.button_ocr) {
            doOCR();
        } else if (id == R.id.button_define) {
            doDefine();
        } else if (id == R.id.button_calculate) {
            doCalculate();
        } else if (id == R.id.button_web_search) {
            doWebSearch(false);
        } else if (id == R.id.button_encrypt) {
            doOKCEncrypt();
        } else if (id == R.id.button_decrypt) {
            doOKCDecrypt();
        }
    }

    @Override
    public void onLocalReplaceSelected(int id) {
        if (id == (R.id.button_close)) {
            closeTopFragment();
            mContent.requestFocus();
            mNextPos = 0;    // Reset next position
        } else if (id == (R.id.button_replace)) {
            doReplaceAllOrNext();
        } else if (id == (R.id.button_next)) {
            doReplaceNext(false);
        }
    }

    // Replace all or next
    private void doReplaceAllOrNext() {
        // If this is the first replace and the number of hits exceeds threshold, let users choose to replace all
        if ((mHits.size() > Const.NUM_HITS_THRESHOLD) && (mNextPos <= 0))
            handleReplaceAll();

        else
            doReplaceNext(true);
    }

    // Handle replace all
    private void handleReplaceAll() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        builder.setTitle(R.string.dialog_replace_all_title);

        builder.setMessage("");

        builder.setPositiveButton(R.string.dialog_replace_all_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Replace all
                doReplaceAll();
                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_replace_all_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Replace next
                doReplaceNext(true);
                return;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.show();

        // Show the dialog
        dialog.show();

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    // Replace all
    public void doReplaceAll() {
        String content = mContent.getText().toString();
        String pattern, status;

        mLocalReplace = findViewById(R.id.edit_local_replace);
        pattern = mLocalReplace.getText().toString();        // Unprocessed pattern

        // Add to history
        LocalReplaceHistory.add(pattern, pattern);

        if ((pattern != null) && (pattern.length() > 0) && (content.length() > 0)) {
            mContent.setText(content.replaceAll(mCriteria, pattern));

            // Update status
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.status_all_replaced), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();

            // Clear search
            doClearSearch();
        }
    }

    // Replace next
    @Override
    public void doReplaceNext(boolean changeMode) {
        int start = mContent.getSelectionStart(), len = mContent.getText().length();
        String temp;

        // Seek to the next match
        if (!changeMode) ++start;    // Bump to the next match if not a real replace

        start = mContent.getText().toString().toLowerCase(Locale.getDefault()).indexOf(mCriteria.toLowerCase(Locale.getDefault()), start);

        // Get the replacement text
        mLocalReplace = findViewById(R.id.edit_local_replace);
        temp = mLocalReplace.getText().toString().trim();

        // Add to history
        LocalReplaceHistory.add(temp, temp);

        if ((start >= 0) && (start < len) && (temp != null)) {
            if (changeMode) {
                mContent.getText().replace(start, start + mCriteria.length(), temp);

                // Advance past the replacement
                mNextPos = start + temp.length();
            } else {
                mNextPos = start;
            }

            // Get the latest length
            len = mContent.getText().length();

            if ((mNextPos >= 0) && (mNextPos < len)) {
                mCurPos = mNextPos;
                mContent.setSelection((int) mCurPos);
                mContent.requestFocus();
            } else
                updateStatus(getResources().getString(R.string.status_no_go_forward), mBounce);
        } else
            updateStatus(getResources().getString(R.string.status_no_go_forward), mBounce);
    }

    @Override
    public void onLocalFindSelected(int id) {
        if (id == (R.id.button_close)) {
            closeTopFragment();
            mContent.requestFocus();
        } else if (id == (R.id.button_search)) {
            doLocalFind();
        } else if (id == (R.id.button_clear)) {
            doClearSearch();
        }
    }

    //////////////////////
    // Local find related
    //////////////////////

    // Local find
    @Override
    public void doLocalFind() {
        mLocalFind = findViewById(R.id.edit_local_find);
        String temp = mLocalFind.getText().toString().trim();

        // Evaluate built-in variables
        if (mEvalBuiltInVariables) {
            // Add to history
            LocalFindHistory.add(temp, temp);
            temp = evalVariables(temp, true);
        }

        if (temp.equals(mCriteria) && (!mHits.isEmpty())) {
            // Continue search from the current location
            doGotoMatch(1, false);
        } else {
            // A new search
            mCriteria = temp;
            doFind();
        }
    }

    // Go to top/bottom
    private void doGoTo(boolean top) {
        try {
            String content;
            int start, end, pos, pos_temp;

            // Get selection
            content = Utils.getCurrentSelection(mContent);
            start = Math.min(mContent.getSelectionStart(), mContent.getSelectionEnd());
            end = Math.max(mContent.getSelectionStart(), mContent.getSelectionEnd());

            if (top) {
                if (content.length() == 0) {
                    // If already at top
                    if (start == 0) {
                        handleInNoteNavigation();
                        return;
                    }

                    pos = 0;

                    // Find bracket pair
                    if (end > 0) {
                        content = mContent.getText().toString();
                        if (Utils.isCloseBracket(content.charAt(end-1))) {
                            pos_temp = Utils.indexOfOpenBracket(content, end-1);
                            if ((pos_temp >= 0) && (pos_temp < content.length()))
                                pos = pos_temp;
                        }
                    }

                    mContent.setSelection(pos);
                } else {
                    if ((mAnchorPos >= 0) && (mAnchorPos < start))
                        mContent.setSelection((int) mAnchorPos, end);    // Extend to anchor
                    else
                        mContent.setSelection(0, end);    // Extend selection to the beginning
                }
            } else {
                String temp = mContent.getText().toString();

                // If already at end
                if (start == temp.length()) {
                    handleInNoteNavigation();
                    return;
                }

                if (content.length() == 0) {
                    pos = temp.length();

                    // Find bracket pair
                    if (start >= 0) {
                        content = mContent.getText().toString();
                        if (Utils.isOpenBracket(content.charAt(start-1))) {
                            pos_temp = Utils.indexOfCloseBracket(content, start-1);
                            if ((pos_temp >= 0) && (pos_temp < content.length()))
                                pos = pos_temp;
                        }
                    }

                    mContent.setSelection(pos);
                } else {
                    if ((mAnchorPos >= 0) && (mAnchorPos > end))
                        mContent.setSelection(start, (int) mAnchorPos);    // Extend to anchor
                    else
                        mContent.setSelection(start, temp.length());    // Extend selection to the end
                }
            }

            mContent.requestFocus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Go to previous/next search hit
    protected void doGotoMatch(int incr, boolean wrap) {

        if ((mHitIdx < 0) || (mHits.isEmpty())) {
            updateStatus(getResources().getString(R.string.status_no_search_conducted), mBounce);
        } else if ((mHitIdx == 0) && (mHits.size() == 1)) {    // Only one hit
            mContent.setSelection(mHits.get(mHitIdx).val());
            updateStatus((mHitIdx + 1) + "/" + mHits.size(), mFadeIn);
        } else if ((mHitIdx == 0) && (incr < 0)) {    // At top
            if (wrap) {
                mHitIdx = mHits.size() - 1;
                mContent.setSelection(mHits.get(mHitIdx).val());
                updateStatus((mHitIdx + 1) + "/" + mHits.size(), mBounce);
            }
            else
                updateStatus(getResources().getString(R.string.status_no_go_back), mBounce);
        } else if ((mHitIdx >= (mHits.size() - 1)) && (incr > 0)) {    // At bottom
            if (wrap) {
                mHitIdx = 0;
                mContent.setSelection(mHits.get(mHitIdx).val());
                updateStatus((mHitIdx + 1) + "/" + mHits.size(), mBounce);
            }
            else
                updateStatus(getResources().getString(R.string.status_no_go_forward), mBounce);
        } else {
            mHitIdx = mHitIdx + incr;
            mContent.setSelection(mHits.get(mHitIdx).val());
            updateStatus((mHitIdx + 1) + "/" + mHits.size(), null);
        }

        mContent.requestFocus();

        return;
    }

    // Verify the existence of hits
    protected boolean hasHits() {
        return !mHits.isEmpty();
    }

    // Set anchor
    protected void setupAnchor() {
        if (mMarkdownMode) {
            mMarkdownAnchorPos = mMarkdownView.getScrollY();
            updateStatus(Const.ANCHOR_SET_SYM, mSlideDown);
        }
        else {
            mAnchorPos = mContent.getSelectionStart();   // Define a new anchor
            updateStatus(Const.ANCHOR_SET_SYM, mSlideDown);
        }
    }

    // Reflow entire markdown view
    protected void doReflowAll() {
        // Call reflow all
        mMarkdownView.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMarkdownView.evaluateJavascript("neutriNote_utils.reflowAll();" , null);
            }
        }, 1);
    }

    // Handle edit status left swipe
    protected void handleEditStatusLeftSwipe() {
        doGotoAnchor();
    }

    // Goto anchor
    protected void doGotoAnchor() {
        // Sanity check
        if (mMarkdownMode) {
            mMarkdownView.scrollTo(0, mMarkdownAnchorPos);
        }
        else {
            if (mAnchorPos >= 0) {
                mContent.setSelection((int) mAnchorPos);    // Resume anchor
                mContent.requestFocus();
            } else {
                mContent.setSelection((int) mPosAtOpen);    // Resume opening position
                mContent.requestFocus();
            }
        }
    }

    // Goto last markdown scroll position
    protected void doGotoMarkdownViewPos() {
        if (mMarkdownMode) {
            doGotoAnchor();
        }
        else {
            try {
                // Compute scroll bar position
                float percent = Utils.getWebViewScrollPercentFloat(getApplicationContext(), mMarkdownView) / 100;
                if (percent > 0) {    // Only worth going if percent is non zero
                    mContent.setSelection(Math.round(mContent.length() * percent));
                    mContent.requestFocus();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Do local find
    private void doFind() {
        // Highlight matches if applicable
        if ((mCriteria.length() > 0) && (mContent.length() > 0)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                new HighlightTask(this).executeOnExecutor(CustomAsyncTask.THREAD_POOL_EXECUTOR);
            } else {
                new HighlightTask(this).execute();
            }

            // Add to history
            LocalFindHistory.add(mCriteria, mCriteria);

            // Refresh the menu
            invalidateOptionsMenu();
        }
    }

    // Clear search
    private void doClearSearch() {
        try {
            // Reset criteria
            mCriteria = null;
            mHitIdx = -1;
            mNextPos = 0;
            mHits.clear();

            // Remove highlights
            mPosAtClear = mContent.getSelectionStart();
            mContent.setText(mContent.getText().toString());
            mContent.setSelection((int) mPosAtClear);

            // Clear search bar
            mLocalFind = findViewById(R.id.edit_local_find);
            if (mLocalFind != null) {
                mLocalFind.setText(null);
                mLocalFind.requestFocus();
            }

            // Set status
            updateStatus(Const.EMPTY_SYM, null);
        } catch (Exception e) {
            e.printStackTrace();

            // Restore cursor position
            if (mPosAtClear > 0)
                mContent.setSelection((int) mPosAtClear);
        }
    }

    // Highlight search hits
    private class HighlightTask extends CustomAsyncTask<Void, Integer, Long> {
        private DisplayDBEntry activity;
        private String content;

        public HighlightTask(DisplayDBEntry activity) {
            this.activity = activity;
        }

        @Override
        protected Long doInBackground(Void... arg0) {
            long totalSize = 0;

            try {
                // Do regex matching
                Pattern pattern = Pattern.compile(Utils.regexCriteria(mCriteria), Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(content);

                mSpann = new SpannableStringBuilder(content);

                while (matcher.find()) {
                    int start = matcher.start();
                    int end = matcher.end();
                    mSpann.setSpan(new BackgroundColorSpan(ContextCompat.getColor(activity, R.color.highlight_color)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    mHits.add(new HitParcelable(start));

                    // Escape early if cancel() is called
                    if (isCancelled()) break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return totalSize;
        }

        @Override
        protected void onPreExecute() {
            // Clear the hit list
            mHits.clear();
            mHitIdx = -1;

            // Set content
            content = mContent.getText().toString();

            // Evalutate built-in variables
            if (mEvalBuiltInVariables)
                mCriteria = evalVariables(mCriteria, true);

            // Show the progress bar
            ProgressBar progressBar = findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Long result) {
            // Hide the progress bar when completed
            ProgressBar progressBar = findViewById(R.id.io_progress_bar);
            progressBar.setVisibility(View.GONE);

            // Call back
            activity.onLocalFindTaskFinished();
        }

        @Override
        protected void onCancelled() {
        }
    }

    // This is the callback for when your async task has finished
    public void onLocalFindTaskFinished() {
        // Set the cursor
        try {
            if (mCriteria.length() > 0) {
                int count = mHits.size();
                boolean changed;

                if (count > 0) {
                    mHitIdx = 0;

                    // Save change state
                    changed = mChanged;

                    mContent.setText(mSpann, EditText.BufferType.SPANNABLE);
                    mContent.setSelection(mHits.get(mHitIdx).val());

                    // Scroll to the hit
                    mContent.requestFocus();

                    // Restore change state
                    mChanged = changed;
                    toggleChanges();

                    updateStatus("'" + Utils.unescapeRegexSym(mCriteria) + "' (" + getResources().getString(R.string.status_hit_count) + count + ")", mZoomIn);

                } else if (mTitle.getText().toString().toUpperCase(Locale.getDefault()).contains(mCriteria.toUpperCase(Locale.getDefault()))) {
                    updateStatus("'" + Utils.unescapeRegexSym(mCriteria) + "' " + getResources().getString(R.string.status_hit_in_title), mBounce);
                } else {
                    updateStatus("'" + Utils.unescapeRegexSym(mCriteria) + "' " + getResources().getString(R.string.status_no_hit_found), mBounce);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            // Note: unable to restore content in some Oreo devices
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O) {
                // Jump to the first hit if one exists (at least do something useful)
                if (hasHits()) {
                    mHitIdx = 0;
                    mContent.setSelection(mHits.get(mHitIdx).val());

                    // Scroll to the hit
                    mContent.requestFocus();

                    // Save the position so that the screen can be restored in case of an immediate crash
                    doSavePos();
                }
            }
            else {
                mContent.setText(mContentSaved);
            }
        }
    }

    // Handle hits navigation
    private void handleHitsNavigation() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics
        int count = mHits.size();

        // Set title and message
        if ((mCriteria != null) && (!Utils.checkMultiWindowMode(this))) {
            String msg = getResources().getString(R.string.dialog_hits_naviagtion_message);
            msg = "\"" + Utils.unescapeRegexSym(mCriteria) + "\", " + getResources().getString(R.string.status_hit_count) + count + "\n\n" + msg;

            builder.setTitle(R.string.dialog_hits_navigation_title);
            builder.setMessage(msg);
        }

        // Set view
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        builder.setView(view);

        // Setup values
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numbers);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setOnScrollListener(new NumberPicker.OnScrollListener() {
                                       @Override
                                       public void onScrollStateChange(NumberPicker view, int scrollState) {
                                           // Show snippet and position in lab mode
                                           if ((mLabMode) && ((scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE))) {
                                               int pos = mHits.get(picker.getValue()).val();
                                               String snippet = Utils.getCurrentSurroundingText(mContentSaved, mCriteria, pos, Const.HIT_PREVIEW_LEN, true, true);

                                               if (snippet.length() > 0) {
                                                   // Add position
                                                   snippet = "<b>" + pos + "</b>" + Const.RIGHT_ARROW_SYM + "  " + snippet;
                                                   Toast.makeText(getApplicationContext(), Html.fromHtml(snippet), Toast.LENGTH_SHORT).show();
                                               }
                                           }
                                       }
                                   }
        );

        float size = mContent.length();
        Integer pos;
        String[] temp = new String[count];
        if (count > 0) {
            for (int i=0; i < count; i++) {
                pos = mHits.get(i).val();
                temp[i] = (i + 1) + " (" + Math.round(pos / size * 100) + "%)";
            }

            picker.setMinValue(0);
            picker.setMaxValue(count - 1);
            picker.setDisplayedValues(temp);
            picker.setValue(mHitIdx);
            picker.setWrapSelectorWheel(true);
        }

        builder.setPositiveButton(R.string.dialog_hits_navigation_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mHitIdx = picker.getValue();
                doGotoMatch(0, false);
            }
        });
        builder.setNegativeButton(R.string.dialog_hits_navigation_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        builder.setNeutralButton(R.string.dialog_hits_navigation_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                doClearSearch();
                return;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.show();

        // Show the dialog
        dialog.show();
    }

    // Handle in-note navigation
    protected void handleInNoteNavigation() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_in_note_navigation_dialog, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);

        // Dim dialog
        Utils.setDialogDimLevel(dialog, Const.DIALOG_DIM_LEVEL);

        // Reset last in-note index
        mInNoteLastIdx = -1;

        // Setup percentages
        ChipGroup chips = (ChipGroup) view.findViewById(R.id.in_note_navigation_chip_group);
        chips.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
                                             @Override
                                             public void onCheckedChanged(ChipGroup group, int checkedId) {
                                                 try {
                                                     int size, pos;

                                                     // Sanity check
                                                     if (checkedId == Const.IN_NOTE_MARKDOWN_LOCAL_FIND_ID)
                                                         return;

                                                     if (isMarkdownMode())
                                                         size = mMarkdownView.getContentHeight();
                                                     else
                                                         size = mContent.length();

                                                     pos = Math.round(size * Const.IN_NOTE_PERCENT_VALUES[checkedId] / 100);
                                                     String snippet = pos + " " + Const.RIGHT_ARROW_SYM + " " + Const.IN_NOTE_PERCENT_VALUES[checkedId] + "%";

                                                     updateStatus(snippet, mSlideDown);
                                                 }
                                                 catch (Exception e) {
                                                     e.printStackTrace();
                                                 }
                                             }
                                         }
        );

        // Get current position
        int cur_percent;
        boolean found = false;
        if (isMarkdownMode()) {
            cur_percent = Utils.getWebViewScrollPercent(getApplicationContext(), mMarkdownView);
        }
        else {
            cur_percent = Utils.getEditTextScrollPercent(mContent);
        }

        int count = Const.IN_NOTE_PERCENT_VALUES.length;
        for (int i=0; i < count; i++) {
            final int idx = i;

            // Setup percent
            Chip chip = new Chip(chips.getContext());

            chip.setId(idx);
            chip.setText(Const.IN_NOTE_PERCENT_VALUES[idx] + "%");
            chip.setCheckable(true);
            chips.addView(chip);

            // Mark current percent
            if ((!found) && (cur_percent <= Const.IN_NOTE_PERCENT_VALUES[idx])) {
                found = true;
                chip.setChecked(true);
            }

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Sanity
                        if (mInNoteLastIdx == idx) {
                            // Clear dialog dim
                            Utils.clearDialogDimLevel(dialog);

                            dialog.dismiss();
                        }
                        else
                            mInNoteLastIdx = idx;

                        if (isMarkdownMode()) {    // Markdown view
                            Utils.setWebViewScrollPositionPercent(getApplicationContext(), mMarkdownView, Const.IN_NOTE_PERCENT_VALUES[idx]);
                        }
                        else {    // Edit view
                            int size = mContent.length();
                            int pos = Math.round(size * Const.IN_NOTE_PERCENT_VALUES[idx] / 100);

                            mContent.setSelection(pos);
                            mContent.requestFocus();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            chip.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    // Sanity check
                    if (isMarkdownMode())
                        return true;

                    try {
                        int size = mContent.length();
                        int pos = Math.round(size * Const.IN_NOTE_PERCENT_VALUES[idx] / 100);
                        String snippet = Utils.getCurrentSurroundingText(mContentSaved, pos, Const.IN_NOTE_PREVIEW_LEN, true, true);

                        if (snippet.length() > 0) {
                            // Add position
                            snippet = pos + Const.RIGHT_ARROW_SYM + " " + snippet;
                            Snackbar snackbar = Utils.makeHighContrastSnackbar(display_dbentry, getCoordinatorLayout(), snippet, Const.IN_NOTE_PREVIEW_FONT_SIZE);
                            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                            snackbar.show();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            });
        }

        // Add markdown local search if applicable
        if (isMarkdownMode()) {
            Chip chip = new Chip(chips.getContext());

            chip.setId(Const.IN_NOTE_MARKDOWN_LOCAL_FIND_ID);
            chip.setText(getResources().getString(R.string.chip_local_find));
            chips.addView(chip);

            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleMarkdownLocalFind();
                }
            });
        }

        dialog.setContentView(chips.getRootView());
        dialog.show();
    }

    // Handle working set
    protected void handleWorkingSet() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_working_set_dialog, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);

        // Set dialog dim level
        Utils.setDialogDimLevel(dialog, Const.DIALOG_DIM_LEVEL);

        // Setup percentages
        ChipGroup chips = (ChipGroup) view.findViewById(R.id.working_set_chip_group);

        List<DBEntry> results = mDatasource.getWorkingSet(mWorkingSetSize+1);
        int count = results.size();

        // Record
        DBEntry entry;

        for (int i=0; i < count; i++) {
            final int idx = i;

            entry = results.get(idx);
            final int id = (int) entry.getId();
            final String title = entry.getTitle();

            // Sanity check
            if ((id < 0) || (title == null) || (title.length() == 0))
                continue;

            // Ignore current note
            if (title.equals(mTitleSaved))
                continue;

            // Setup choices
            Chip chip = new Chip(chips.getContext());
            chip.setId(id);
            chip.setText(title);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Clear dialog dim
                        Utils.clearDialogDimLevel(dialog);

                        // Dismiss before activity goes away
                        dialog.dismiss();

                        // For existing note only
                        if (id != -1) {
                            // Launch note
                            doLaunchNote(title, (long) id);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            chip.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    try {
                        // Get content preview
                        List<DBEntry> records = mDatasource.getRecordPreviewByTitle(title);
                        if (records.size() > 0) {
                            DBEntry entry = records.get(0);
                            String preview = Utils.subStringWordBoundary(entry.getContent(), 1, Const.WORKING_SET_PREVIEW_LEN).replaceAll("\\s+", " ");

                            // Show preview
                            Snackbar snackbar = Utils.makeHighContrastSnackbar(display_dbentry, getCoordinatorLayout(), preview, Const.WORKING_SET_PREVIEW_FONT_SIZE);
                            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                            snackbar.show();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    return true;
                }
            });

            chips.addView(chip);
        }

        // Setup funnel button
        Chip chip = new Chip(chips.getContext());
        chip.setText(getResources().getString(R.string.chip_funnel));
        chip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    // Handle funnel
                    handleFunnel();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        chips.addView(chip);

        dialog.setContentView(chips.getRootView());
        dialog.show();
    }

    // Launch a note
    private void doLaunchNote(String title, long id) {
        try {
            Log.d(Const.TAG, "nano - working set launching: '" + title);

            if (mActivity == null) {    // Fall back to a safer approach
                // Show progress
                Toast toast = Toast.makeText(getApplicationContext(), Const.WATCH_SYM, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                // Save current note
                doSave(false, false);

                // Launch search
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setAction(Intent.ACTION_SEARCH);
                intent.putExtra(SearchManager.QUERY, Const.TITLEREGONLY + title);
                startActivity(intent);

                // Leave
                leave();
            }
            else {
                // Show progress
                Toast toast = Toast.makeText(getApplicationContext(), Const.HOURGLASS_SYM, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();

                // Save current note
                doSave(false, false);

                // Launch editor
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setAction(Const.ACTION_VIEW_ENTRY);
                intent.putExtra(Const.EXTRA_ID, id);
                startActivity(intent);

                // Leave
                leave();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Search note
    private void doSearchNote(String criteria) {
        mActivity.doSearch(criteria);

        // Set status
        updateStatus(criteria, mSlideDown);

        // Return to note list
        handleHome();
    }

    // Append clipboard
    private void doAppendClipboard(String title) {
        // Check if the record already exists
        ArrayList<DBEntry> results = mDatasource.getRecordByTitle(title);
        if (results.size() == 1) {
            // Get clipboard content
            String clipboard_text = Utils.getClipboardText(DBApplication.getAppContext(), (ClipboardManager) DBApplication.getAppContext().getSystemService(mActivity.CLIPBOARD_SERVICE), -1, true);

            // Sanity check
            if (clipboard_text.length() == 0) {
                Snackbar.make(mActivity.findViewById(android.R.id.content), DBApplication.getAppContext().getResources().getString(R.string.status_empty_clipboard), Snackbar.LENGTH_SHORT).show();
                return;
            }

            DBEntry entry = results.get(0);

            StringBuilder sb = new StringBuilder();
            sb.append(entry.getContent());
            sb.append("\r\n\r\n");
            sb.append(clipboard_text);

            // Sanity check
            if (sb.length() < entry.getSize()) {
                Toast.makeText(DBApplication.getAppContext(), DBApplication.getAppContext().getResources().getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
                return;
            }

            // Update record
            mDatasource.updateRecord(entry.getId(), entry.getTitle(), sb.toString(), entry.getStar(), null, true, entry.getTitle());
            if (mLocationAware) {
                Location location = Utils.getLocation(getApplicationContext());
                if (location != null)
                    mDatasource.updateRecordCoordinates(entry.getId(), location.getLatitude(), location.getLongitude());
            }

            Toast.makeText(DBApplication.getAppContext(), title + Const.SPACE_CHAR + DBApplication.getAppContext().getResources().getString(R.string.status_updated_remotely), Toast.LENGTH_SHORT).show();
        }
    }

    ///////////////
    // Other tools
    ///////////////
    private void handleInsertBarcode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Handle runtime permissions
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                doInsertBarcode();
            else
                getCameraPermission(getApplicationContext());
        } else
            doInsertBarcode();
    }

    // Insert barcode
    private void doInsertBarcode() {
        try {
            PackageManager manager = getApplicationContext().getPackageManager();
            if (manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
                IntentIntegrator integrator = new IntentIntegrator(this);
                integrator.setCaptureActivity(CaptureActivityAnyOrientation.class);
                integrator.setOrientationLocked(false);
                integrator.initiateScan();
            } else
                updateStatus(getResources().getString(R.string.error_no_camera), mBounce);
        } catch (Exception e) {
            e.printStackTrace();
            updateStatus(getResources().getString(R.string.error_unexpected), mBounce);
        }
    }

    // Define a word
    private void doDefine() {
        int start = mContent.getSelectionStart();
        int end = mContent.getSelectionEnd();

        // Determine the word
        String criteria;
        if (start < end)
            criteria = mContent.getText().toString().substring(start, end);
        else
            criteria = Utils.getCurrentWord(mContent, start, false);

        // Sanity check
        if (criteria.length() == 0) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.warn_no_selected_word), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            return;
        }

        // Determine cursor position
        Layout layout = mContent.getLayout();
        int line = layout.getLineForOffset(start);
        int baseline = layout.getLineBaseline(line);
        int ascent = layout.getLineAscent(line);
        float y = baseline + ascent;
        float percent;

        // Determine scroll view visible rectangle
        ScrollView scrollView = this.findViewById(R.id.edit_scrollview);
        Rect scrollBounds = new Rect();
        scrollView.getDrawingRect(scrollBounds);
        y = y - scrollBounds.top;
        percent = y / (scrollBounds.bottom - scrollBounds.top);

        // Build intent
        Intent intent = new Intent(Const.COLORDICT_SEARCH_ACTION);
        intent.putExtra(Const.COLORDICT_EXTRA_QUERY, criteria); //Search Query
        intent.putExtra(Const.COLORDICT_EXTRA_FULLSCREEN, false); //
        intent.putExtra(Const.COLORDICT_EXTRA_HEIGHT, Const.COLORDICT_POPUP_DIM_X); //400pixel, if you don't specify, fill_parent"

        // Position popup based on current cursor position
        if (percent > 0.5)
            intent.putExtra(Const.COLORDICT_EXTRA_GRAVITY, Gravity.TOP);

        else
            intent.putExtra(Const.COLORDICT_EXTRA_GRAVITY, Gravity.BOTTOM);

        // Launch ColorDict
        if (Utils.isIntentAvailable(this, intent))
            this.startActivity(intent);
        else {
            doWebSearch(true);
        }
    }

    // Calculate
    private void doCalculate() {
        int start = mContent.getSelectionStart();
        int end = mContent.getSelectionEnd();

        String expr;

        // Determine the snippet
        if (start < end)
            expr = mContent.getText().toString().substring(start, end).trim();
        else
            expr = Utils.getCurrentSnippet(mContent, start, false).trim();

        // Sanity check
        if (expr.length() == 0) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.warn_no_selected_expression), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            return;
        }

        String result = Double.toString(Utils.eval(expr.toLowerCase()));

        // In error, fall back to remote math if user permitted
        if (result.equals(Const.NON_NUMBER_SYM)) {
            boolean remote_math_allowed = (mMathUrl.equals(Const.HTTP_SYM)) || (mMathUrl.equals(Const.HTTPS_SYM));    // Permission to conduct remote math
            if ((Utils.isConnected(getApplicationContext())) && (remote_math_allowed)) {
                // Setup request
                RequestQueue queue = Volley.newRequestQueue(this);
                String url = Const.MATHJS_URL + Uri.encode(expr) + "&" + Const.MATHJS_PRECISION_PARAM;

                // Request a string response from the provided URL.
                StringRequest request = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                String result = response;
                                Snackbar snackbar = Utils.makePasteSnackbar(display_dbentry, getCoordinatorLayout(), mContent, Const.EQUAL_SYM + result);
                                Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                                snackbar.show();
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_calculation), Snackbar.LENGTH_SHORT);
                                Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                                snackbar.show();
                            }
                        });

                // Add the request to the RequestQueue.
                queue.add(request);
            }
        }
        else {
            Snackbar snackbar = Utils.makePasteSnackbar(this, getCoordinatorLayout(), mContent, Const.EQUAL_SYM + result);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }


    // Search the web
    private void doWebSearch(boolean defineMode) {
        int start = mContent.getSelectionStart();
        int end = mContent.getSelectionEnd();

        // Determine the word
        String criteria;
        if (start < end)
            criteria = mContent.getText().toString().substring(start, end);
        else
            criteria = Utils.getCurrentSnippet(mContent, start, false);

        // Sanity check
        if (criteria.length() == 0) {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.warn_no_selected_word), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
            return;
        }

        // Web define mode
        if (defineMode) {
            criteria = Const.WEB_DEFINE_PATTERN_PREFIX + criteria;
        }

        Intent intent;
        if (criteria.startsWith(Const.CUSTOM_SCHEME)) {
            intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(criteria));
        }
        else {
            intent = new Intent(Intent.ACTION_WEB_SEARCH);
            Bundle bundle = new Bundle();
            bundle.putString(SearchManager.QUERY, criteria);
            intent.putExtras(bundle);
        }

        startActivity(intent);
    }

    // Export note link
    private void exportNoteLink() {
        int start = mContent.getSelectionStart();
        int end = mContent.getSelectionEnd();

        // Determine the word
        String criteria;
        if (start < end)
            criteria = mContent.getText().toString().substring(start, end);
        else
            criteria = Utils.getCurrentWord(mContent, start, false);

        // Build a note link
        String link = Utils.createNoteLink(mTitle.getText().toString(), criteria);

        // Paste the link in clipboard
        if (link.length() > 0)
            mClipboard.setPrimaryClip(ClipData.newPlainText(Const.CUSTOM_SCHEME, link));
    }

    ////////////////////
    // Markdown related
    ////////////////////

    // Trigger markdown view
    protected void showHideMarkdown(boolean markdownMode) {
        if (markdownMode) {
            mMarkdownMode = !markdownMode;
            toggleMarkdownView();
        }
    }

    // Scroll to anchor in markdown view
    private void doMarkdownViewScrollAnchor() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @SuppressLint("NewApi")
            public void run() {
                mMarkdownView.findAllAsync(Const.ANCHOR_MARKDOWN);
            }
        };
        handler.postDelayed(runnable, Const.SCROLL_DELAY);
    }

    // Inject anchor
    private String injectAnchor(String html, int pos) {
        StringBuilder builder = new StringBuilder();
        String anchor = Const.ANCHOR_MARKDOWN;
        int len = html.length();

        try {
            builder.append(html);

            if (pos >= len)
                builder.append(anchor);
            else
                builder.insert(pos, anchor);

            // Activate anchor
            mMarkdownAnchorActive = true;

            if ((!Character.isLetterOrDigit(html.charAt(pos-1))) && (html.charAt(pos-1) != Const.NEWLINE_CHAR) && (html.charAt(pos-1) != Const.SPACE_CHAR)) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.warn_markdown_cursor_position), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    // Build script
    protected String buildScript(boolean toc) {
        String js = null, script;

        script = makeFileName(getApplicationContext(), Const.CUSTOM_SCRIPT);
        ArrayList<DBEntry> results = mDatasource.getRecordByTitle(script);

        // Load user defined script
        if (results.size() == 1) {
            DBEntry entry = results.get(0);
            js = entry.getContent();
        }

        if ((js != null) && (js.length() > 0))
            // Load custom script
            js = Utils.loadCustomScript(js);

        else {
            // Load built-in script

            // PHP Markdown
            js = Const.MDX_CONVERT_JS;
            js += Const.MDX_EXTRA_JS;
            js += Const.NANO_JS;

            // Use LaTeX single dollar sign if preferred
            if (mLaTeXSingleDollar)
                js += Const.LATEX_SINGLE_DOLLAR_CONFIG;

            // MathJax
            js += "<script type='text/javascript' id='MathJax-script' async src='" + mMathUrl + "'></script>";

            // Handle TOC
            if (toc) {
                js += Const.JQUERY;
                js += Const.MDX_TOC_JS;
            }

            // Python support
            if (mParsePython)
                js += Const.PYTHON_JS;

            // Vue support
            if (mParseVue) {
                js += Const.VUE_JS;
                js += Const.APP_JS.replaceAll(Const.FILE_SCHEMA_PREFIX_SHORTCUT_SYM, "file://" + mLocalRepoPath);
            }

            // Alpine support
            if (mParseAlpine)
                js += Const.ALPINE_JS;

            // Mermaid support
            if (mParseMermaid)
                js += Const.MERMAID_JS;

            // Typograms support
            if (mParseTypograms)
                js += Const.TYPOGRAMS_JS;
        }

        // Add parser api
        js = Const.PARSER_API_JS + js;

        return js;
    }

    // Build clipboard script
    protected String buildClipboardScript(boolean toc) {
        String js;

        // Load built-in script

        // PHP Markdown
        js = Const.MDX_CONVERT_JS;
        js += Const.MDX_EXTRA_JS;
        js += Const.NANO_JS;

        // Use LaTeX single dollar sign if preferred
        if (mLaTeXSingleDollar)
            js += Const.LATEX_SINGLE_DOLLAR_CONFIG;

        // MathJax
        js += "<script type='text/javascript' id='MathJax-script' async src='" + mMathUrl + "'></script>";

        // Handle TOC
        if (toc) {
            js += Const.JQUERY;
            js += Const.MDX_TOC_JS;
        }

        // Python support
        if (mParsePython)
            js += Const.PYTHON_JS;

        // Vue support
        if (mParseVue) {
            js += Const.VUE_JS;
            js += Const.APP_JS.replaceAll(Const.FILE_SCHEMA_PREFIX_SHORTCUT_SYM, "file://" + mLocalRepoPath);
        }

        // Alpine support
        if (mParseAlpine)
            js += Const.ALPINE_JS;

        // Mermaid support
        if (mParseMermaid)
            js += Const.MERMAID_JS;

        // Typograms support
        if (mParseTypograms)
            js += Const.TYPOGRAMS_JS;

        // Add parser api
        js = Const.PARSER_API_JS + js;

        return js;
    }

    // Build style
    protected String buildStyle() {
        String custom_css = Const.NULL_SYM, css, style_sheet;
        String mode = Const.NULL_SYM;

        style_sheet = makeFileName(getApplicationContext(), Const.CUSTOM_STYLE_SHEET);
        ArrayList<DBEntry> results = mDatasource.getRecordByTitle(style_sheet);

        if (mTheme.equals(Const.SYSTEM_THEME)) {
            int flags = getApplicationContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (flags == Configuration.UI_MODE_NIGHT_YES) {
                mode = Const.NIGHT_THEME;
            }
            else {
                mode = Const.DAY_THEME;
            }
        }

        // Load built-in style sheet
        if ((mTheme.equals(Const.NIGHT_THEME)) || (mode.equals(Const.NIGHT_THEME)))
            css = Const.GFM_NIGHT_CSS;
        else if ((mTheme.equals(Const.DARK_THEME)) || (mode.equals(Const.DARK_THEME)))
            css = Const.GFM_DARK_CSS;
        else
            css = Const.GFM_LIGHT_CSS;

        css = mMarkdownFontFamily + mMarkdownMargin + css;

        // Load user defined style sheet
        if (results.size() == 1) {
            DBEntry entry = results.get(0);
            custom_css = entry.getContent();
        }

        if ((custom_css != null) && (custom_css.length() > 0)) {
            // Load custom styles
            custom_css = Utils.loadCustomStyles(custom_css);

            if (mAppendCustomStyle)
                css = css + custom_css;    // Append to built-in style
            else
                css = custom_css;          // Overwrite built-in style
        }

        return css;
    }

    // Build clipboard style
    protected String buildClipboardStyle() {
        String css;
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

        // Load built-in style sheet
        if ((mTheme.equals(Const.NIGHT_THEME)) || (mode.equals(Const.NIGHT_THEME)))
            css = Const.GFM_NIGHT_CSS;
        else if ((mTheme.equals(Const.DARK_THEME)) || (mode.equals(Const.DARK_THEME)))
            css = Const.GFM_DARK_CSS;
        else
            css = Const.GFM_LIGHT_CSS;

        // Apply clipboard style
        css = Const.CLIPBOARD_FONT_FAMILY + Const.CLIPBOARD_FONT_SIZE + mMarkdownMargin + css;

        return css;
    }

    // Build head
    protected String buildHead() {
        String custom_head, head = Const.NULL_SYM;

        custom_head = makeFileName(getApplicationContext(), Const.CUSTOM_HEAD);
        ArrayList<DBEntry> results = mDatasource.getRecordByTitle(custom_head);

        // Load user defined script
        if (results.size() == 1) {
            DBEntry entry = results.get(0);
            head = entry.getContent();
        }

        return head;
    }

    // Build source
    protected String buildSource(boolean useAnchor) {
        String source = mContent.getText().toString(), js, css, head;
        String file_schema_prefix_expanded;

        // Inject anchor
        if ((source.length() > 0) && (useAnchor) && (!source.contains(Const.TOC)))
            source = injectAnchor(source, mContent.getSelectionStart());

        // Replace file schema shortcut
        if (source.contains(Const.FILE_SCHEMA_PREFIX_SHORTCUT_SYM)) {
            file_schema_prefix_expanded = "file://" + mLocalRepoPath;
            source = source.replaceAll(Const.FILE_SCHEMA_PREFIX_SHORTCUT_SYM, file_schema_prefix_expanded);
        }

        // Get HTML head section
        head = Utils.extractHeadData(source);
        head += buildHead();

        // Build Javascript section
        js = buildScript(source.contains(Const.TOC));

        // Build style sheet
        css = buildStyle();

        return Const.WEBVIEW_DENSITY + css + "<xmp>" + source + "</xmp>" + js + head;
    }

    // Build clipboard source
    protected String buildClipboardSource() {
        String source = mContent.getText().toString(), js, css, head;

        // Get HTML head section
        head = Utils.extractHeadData(source);
        head += buildHead();

        // Build Javascript section
        js = buildClipboardScript(source.contains(Const.TOC));

        // Build style sheet
        css = buildClipboardStyle();

        // Get clipboard text
        source = Utils.getClipboardText(getApplicationContext(), mClipboard, -1, true).trim();
        if (source.length() == 0)    // Default
            source = "<h1 align='center'>" + Const.CLIPBOARD_SYM + "</h1>";

        return Const.WEBVIEW_DENSITY + css + "<xmp>" + source + "</xmp>" + js + head;
    }

    // Toggle menu related to markdown view
    protected void toggleMarkdownViewMenu(Menu menu, boolean visible) {
        MenuItem item;

        // Markdown toggle
        item = menu.findItem(R.id.menu_markdown_view);
        if (visible) {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_chrome_reader_mode_anim_vector));
        }
        else {
            item.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_mode_edit_anim_vector));
        }

        // Styles
        item = menu.findItem(R.id.menu_style);
        item.setVisible(visible);

        // Revert
        item = menu.findItem(R.id.menu_revert);
        item.setVisible(visible);

        // Edit tool
        item = menu.findItem(R.id.menu_edit_tools);
        item.setVisible(visible);

        // Encrpyt/decrypt
        item = menu.findItem(R.id.menu_encrypt_decrypt);
        item.setVisible(visible);

        // Paste calendar
        item = menu.findItem(R.id.menu_paste_calendar);
        item.setVisible(visible);

        // Clear cache
        item = menu.findItem(R.id.menu_clear_cache);
        item.setVisible(visible);
    }

    // Toggle markdown view
    protected void toggleMarkdownView() {
        if (mMarkdownMode) {
            // Turn off markdown mode
            mMarkdownMode = false;

            // Hide markdown view
            mMarkdownView.setVisibility(View.GONE);

            // Show the editor
            mScrollView.setVisibility(View.VISIBLE);
            mContent.requestFocus();

            // Show edit tools
            showEditToolFragment();

            // Show keyboard
            Utils.showKeyboard(this, mTitle, mContent);
        } else {
            // Turn on markdown mode
            mMarkdownMode = true;

            // Apply markdown
            if (!isMarkdownRendered()) {
                // Show the progress bar
                ProgressBar progressBar = findViewById(R.id.io_progress_bar);
                progressBar.setVisibility(View.VISIBLE);

                // Show markdown view
                mMarkdownView.getSettings().setDefaultTextEncodingName("utf-8");
                mMarkdownView.getSettings().setJavaScriptEnabled(true);       // For accessibility
                mMarkdownView.getSettings().setBuiltInZoomControls(true);     // Enable zoom
                mMarkdownView.getSettings().setDisplayZoomControls(false);    // Disable zoom control
                mMarkdownView.getSettings().setLoadWithOverviewMode(true);
                mMarkdownView.getSettings().setUseWideViewPort(true);

                if ((mSafeModeTag.length() > 0) && (mMetadata.contains(mSafeModeTag)))    // Use safe mode
                    mMarkdownView.loadDataWithBaseURL(Const.PREFIX_FILE + mLocalRepoPath + "/", mContent.getText().toString(), "text/html", "utf-8", null);

                else {
                    // Fix local image viewing problem
                    mMarkdownView.loadDataWithBaseURL(Const.PREFIX_FILE + mLocalRepoPath + "/", buildSource(true), "text/html", "utf-8", null);
                }
            }

            // Hide the editor
            mScrollView.setVisibility(View.GONE);

            // Close all fragments
            closeAllFragments();

            // Hide keyboard
            Utils.hideKeyboard(this, mTitle, mContent);

            // Show markdown view
            mMarkdownView.setVisibility(View.VISIBLE);

            // Set status
            updateStatus(mMetadata, mPushDownIn);
        }

        // Refresh the menu
        invalidateOptionsMenu();
    }

    // Javascript interface for communicating with the editor
    class EditViewJavaScriptInterface {
        private Context context;

        EditViewJavaScriptInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void log(String text) {
            Snackbar snackbar = Utils.makeCopySnackbar(display_dbentry, getCoordinatorLayout(), text);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }

    // Javascript for exporting HTML
    class MarkdownViewJavaScriptInterface {
        private Context context;

        MarkdownViewJavaScriptInterface(Context context) {
            this.context = context;
        }

        @JavascriptInterface
        public void processHTML(String html) {
            Intent shareIntent;
            shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType(Const.PLAIN_TEXT_TYPE);
            shareIntent.putExtra(Intent.EXTRA_TEXT, html);
            startActivity(shareIntent);
        }

        @JavascriptInterface
        public void exportHTMLJS(String html) {
            Utils.getSDState();

            if (Utils.isExternalStorageWritable()) {
                try {
                    String dir_path = mLocalRepoPath + "/" + Const.EXPORTED_HTML_PATH + "/";
                    String title = Utils.extractTitleFromFileName(getApplicationContext(), mTitle.getText().toString()) + Const.FILE_EXTENSION_HTML;
                    String full_path = dir_path + title;

                    // Make directory if not available yet
                    File dir = new File(dir_path);
                    if (!dir.isDirectory())
                        dir.mkdir();

                    if (dir.isDirectory()) {
                        FileOutputStream file = new FileOutputStream(full_path);
                        file.write(html.getBytes());
                        file.flush();
                        file.close();

                        // Also export to SAF
                        Utils.writeSpecialSAFFile(getApplicationContext(), mBackupUri, Const.EXPORTED_HTML_PATH, title, html);

                        Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.status_exported) + Utils.cleanPath(full_path), Snackbar.LENGTH_SHORT);
                        Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                        snackbar.show();
                    } else {
                        Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), dir_path + getResources().getString(R.string.error_create_path), Snackbar.LENGTH_SHORT);
                        Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                        snackbar.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_no_writable_external_storage), Snackbar.LENGTH_SHORT);
                Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                snackbar.show();
            }
        }
    }

    // Create a print job
    @TargetApi(19)
    private void createWebPrintJob(WebView view) {
        // Get a PrintManager instance
        PrintManager printManager = (PrintManager) this.getSystemService(Context.PRINT_SERVICE);

        // Get a print adapter instance
        PrintDocumentAdapter printAdapter;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            printAdapter = view.createPrintDocumentAdapter(mTitle.getText().toString());
        }
        else {
            printAdapter = view.createPrintDocumentAdapter();
        }

        // Create a print job with name and adapter instance
        String jobName = getString(R.string.app_name) + " : " + mTitle.getText().toString();
        PrintJob printJob = printManager.print(jobName, printAdapter, new PrintAttributes.Builder().build());
    }

    // Do HTML Export
    private void doExportHTML(boolean usePrint) {
        // Show the progress bar
        ProgressBar progressBar = findViewById(R.id.io_progress_bar);
        progressBar.setVisibility(View.VISIBLE);

        final WebView webview = findViewById(R.id.markdown_view);
        final boolean use_print = usePrint;
        webview.getSettings().setJavaScriptEnabled(true);

        // Register a new JavaScript interface called HTMLOUT
        webview.addJavascriptInterface(new MarkdownViewJavaScriptInterface(this), "HTMLOUT");

        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                view.loadUrl("javascript:window.HTMLOUT.exportHTMLJS('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");

                if (use_print)
                    createWebPrintJob(webview);

                // Hide the progress bar
                ProgressBar progressBar = findViewById(R.id.io_progress_bar);
                progressBar.setVisibility(View.GONE);
            }
        });

        // Apply markdown
        if ((mSafeModeTag.length() > 0) && (mMetadata.contains(mSafeModeTag)))    // Use safe mode
            webview.loadDataWithBaseURL(Const.PREFIX_FILE + mLocalRepoPath + "/", mContent.getText().toString(), "text/html", "utf-8", null);
        else
            webview.loadDataWithBaseURL(Const.PREFIX_FILE + mLocalRepoPath + "/", buildSource(false), "text/html", "utf-8", null);
    }

    // Export to markdown
    public void doExportMarkdown() {
        Utils.getSDState();

        if (Utils.isExternalStorageWritable()) {
            try {
                String dir_path = mLocalRepoPath + "/" + Const.EXPORTED_MARKDOWN_PATH + "/";
                String title = Utils.extractTitleFromFileName(getApplicationContext(), mTitle.getText().toString()) + Const.FILE_EXTENSION_MARKDOWN;
                String full_path = dir_path + title;

                // Make directory if not available yet
                File dir = new File(dir_path);
                if (!dir.isDirectory())
                    dir.mkdir();

                if (dir.isDirectory()) {
                    FileOutputStream file = new FileOutputStream(full_path);
                    file.write(mContent.getText().toString().getBytes());
                    file.flush();
                    file.close();

                    // Also export to SAF
                    Utils.writeSpecialSAFFile(getApplicationContext(), mBackupUri, Const.EXPORTED_MARKDOWN_PATH, title, mContent.getText().toString());

                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.status_exported) + Utils.cleanPath(full_path), Snackbar.LENGTH_SHORT);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                } else {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), dir_path + getResources().getString(R.string.error_create_path), Snackbar.LENGTH_SHORT);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_no_writable_external_storage), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }

    // Handle canvas stroke
    private void handleCanvasStroke() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("");

        // Set view
        View view = getLayoutInflater().inflate(R.layout.number_picker, null);
        builder.setView(view);

        // Setup values
        final NumberPicker picker = (NumberPicker) view.findViewById(R.id.numbers);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        final String[] items = mCanvasStrokes.split(";");
        int pos = Arrays.asList(items).indexOf(String.valueOf(mCanvasStroke));

        if (items.length > 0) {
            picker.setMinValue(0);
            picker.setMaxValue(items.length - 1);
            picker.setDisplayedValues(items);

            if (pos > 0)
                picker.setValue(pos);

            picker.setWrapSelectorWheel(true);  // Wrap around
            picker.startAnimation(mSlideDown);
        }

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_canvas_stroke_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                mCanvasStroke = items[picker.getValue()].charAt(0);
                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_canvas_stroke_cancel, new DialogInterface.OnClickListener() {
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

    // Handle drawing
    private void handleDraw() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set up layout
        LayoutInflater inflater = this.getLayoutInflater();
        View canvas_view = inflater.inflate(canvas, null);
        builder.setView(canvas_view);
        boolean multiWindowMode = Utils.checkMultiWindowMode(this);

        // Get canvas
        final EditText canvas = (EditText) canvas_view.findViewById(R.id.edit_canvas);

        Typeface font = FontCache.getFromAsset(this, "RobotoMono-Light.ttf");
        canvas.setTypeface(font);
        canvas.setTextColor(mCanvasForeground);
        canvas.setBackground(mCanvasBackground);

        // Set title
        if (multiWindowMode)
            builder.setTitle(Const.EMPTY_SYM);
        else
            builder.setTitle(getResources().getString(R.string.dialog_draw_title));

        // Determine dot size
        int dot_size = Const.CANVAS_DOT_SIZE;
        if (multiWindowMode) dot_size = Const.CANVAS_DOT_MINI_SIZE;

        canvas.setTextSize(dot_size);

        // Initialize the canvas
        final String current_drawing = Utils.getCurrentDrawing(mContent);
        final String current_selection = Utils.getCurrentSelection(mContent);

        if (current_drawing.length() > 0)
            canvas.setText(current_drawing);

        else {
            String line = Strings.padStart("", Const.CANVAS_MAX_COLS, Const.CANVAS_OFF);
            StringBuilder buf = new StringBuilder();
            for (int i = 0; i < Const.CANVAS_MAX_ROWS; i++)
                buf.append(line + Const.NEWLINE);
            canvas.setText(buf.toString());
        }

        // Set up mode
        final SwitchCompat canvas_mode = (SwitchCompat) canvas_view.findViewById(R.id.canvas_mode);

        // Set up settings
        Button button_settings = (Button) canvas_view.findViewById(R.id.button_settings);
        button_settings.setTypeface(FontCache.getFromAsset(this, "iconfonts.ttf"));
        button_settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (canvas_mode.isChecked())
                    handleCanvasStroke();
            }
        });

        // Hide in multi-window mode
        if (multiWindowMode) {
            canvas_mode.setVisibility(View.GONE);
            button_settings.setVisibility(View.GONE);
        }

        // Set up touch listener
        canvas.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                EditText canvas_view = (EditText) view;
                float x = event.getX();
                float y = event.getY();
                int pos = canvas_view.getOffsetForPosition(x, y);
                StringBuilder temp = new StringBuilder(canvas_view.getText().toString());

                if ((pos > 0) && (pos < temp.length()) && (temp.charAt(pos) != Const.NEWLINE_CHAR)) {
                    if (canvas_mode.isChecked()) {
                        // If stroke has not been set
                        if (mCanvasStroke == Const.CANVAS_OFF) {

                            // Use the first stroke by default
                            String[] items = mCanvasStrokes.split(";");
                            if ((items.length > 0) && (items[0].length() > 0))
                                mCanvasStroke = items[0].charAt(0);

                                // Fall back to the basic stroke
                            else
                                mCanvasStroke = Const.CANVAS_ON;
                        }

                        temp.setCharAt(pos, mCanvasStroke);
                    }

                    else
                        temp.setCharAt(pos, Const.CANVAS_OFF);

                    canvas_view.setText(temp.toString());
                    canvas_view.setSelection(pos);
                }

                return true;
            }
        });

        builder.setPositiveButton(getResources().getString(R.string.dialog_draw_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String drawing = canvas.getText().toString();

                // Prepare for undo
                updateUndo();

                // Insert the drawing
                if ((current_drawing.length() == 0) || (current_selection.startsWith(Const.DRAWING_SEPARATOR_SYM)))
                    drawing = Const.NEWLINE + Const.DRAWING_SEPARATOR_SYM + drawing + Const.DRAWING_SEPARATOR_SYM;

                Utils.insert(mContent, drawing);

                // Check resolution
                if ((!mFontFamily.contains("Mono")) || (Integer.parseInt(mFontSize) > 12)) {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.info_drawing_best_viewing_font), Snackbar.LENGTH_LONG).setAction(getResources().getString(R.string.snack_bar_button_done), mSnackbarOnClickListener);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }

                // Allow auto save
                mAutoSaveSafe = true;
            }
        });

        builder.setNegativeButton(getResources().getString(R.string.dialog_draw_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();

                // Allow auto save
                mAutoSaveSafe = true;
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                mAutoSaveSafe = true;
            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                mAutoSaveSafe = true;
            }
        });

        // Disallow auto save
        mAutoSaveSafe = false;

        // Show the dialog
        dialog.show();

        // Add some animations
        canvas.startAnimation(mFadeIn);
        canvas_mode.startAnimation(mFadeIn);

        // Show keyboard
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    // Handle show clipboard
    private void handleShowClipboard() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        WebView webview = new WebView(this);

        WebSettings settings = webview.getSettings();
        settings.setJavaScriptEnabled(true);                           // For accessibility
        settings.setBuiltInZoomControls(true);                         // Allow zoom
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);    // Offline cache

        // Apply markdown
        webview.loadDataWithBaseURL(Const.PREFIX_FILE + mLocalRepoPath + "/", buildClipboardSource(), "text/html", "utf-8", null);
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(final WebView view, String url) {
                super.onPageFinished(view, url);

                // Setup reflow
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        view.evaluateJavascript("neutriNote_utils.init(document);" , null);
                    }
                }, 100);
            }

            boolean scaleChangedRunnablePending = false;

            @Override
            public void onScaleChanged(final WebView webView, final float oldScale, final float newScale) {
                // Text reflow
                if (scaleChangedRunnablePending) return;
                scaleChangedRunnablePending = true;

                webView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        webView.evaluateJavascript("neutriNote_utils.updateScale(" + oldScale + "," + newScale + ");", null);
                        scaleChangedRunnablePending = false;
                    }
                }, 2);
            }
        });

        builder.setView(webview);
        builder.setPositiveButton(R.string.dialog_show_clipboard_paste, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Utils.insert(mContent, Utils.getClipboardText(getApplicationContext(), mClipboard, -1, true).trim());
                return;
            }
        });
        builder.setNeutralButton(DBApplication.getAppContext().getResources().getString(R.string.dialog_show_clipboard_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });

        // Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        // Show the dialog
        dialog.show();
    }

    // Load preferences
    protected void loadPref() {
        try {
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            mSharedPreferencesEditor = mSharedPreferences.edit();

            mLocalRepoPath = mSharedPreferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
            mBackupUri = Uri.parse(mSharedPreferences.getString(Const.PREF_BACKUP_URI, ""));
            mAutoSave = mSharedPreferences.getBoolean(Const.PREF_AUTO_SAVE, true);
            mAutoSaveInterval = Integer.parseInt(mSharedPreferences.getString(Const.PREF_AUTO_SAVE_INTERVAL, String.valueOf(Const.AUTO_SAVE_INTERVAL)));
            mLocationAware = mSharedPreferences.getBoolean(Const.PREF_LOCATION_AWARE, false);
            mTheme = mSharedPreferences.getString(Const.PREF_THEME, Const.DEFAULT_THEME);
            mLux = mSharedPreferences.getBoolean(Const.PREF_LUX, false);
            mFontFamily = mSharedPreferences.getString(Const.PREF_FONT_FAMILY, Const.DEFAULT_FONT_FAMILY);
            mFontSize = mSharedPreferences.getString(Const.PREF_FONT_SIZE, Const.DEFAULT_FONT_SIZE);
            mMargin = mSharedPreferences.getString(Const.PREF_MARGIN, Const.DEFAULT_MARGIN);
            mMathUrl = mSharedPreferences.getString(Const.PREF_MATH_URL, getResources().getString(R.string.pref_math_url_default));
            mParsePython = mSharedPreferences.getBoolean(Const.PREF_PARSE_PYTHON, false);
            mParseVue = mSharedPreferences.getBoolean(Const.PREF_PARSE_VUE, false);
            mParseAlpine = mSharedPreferences.getBoolean(Const.PREF_PARSE_ALPINE, false);
            mParseMermaid = mSharedPreferences.getBoolean(Const.PREF_PARSE_MERMAID, false);
            mParseTypograms = mSharedPreferences.getBoolean(Const.PREF_PARSE_TYPOGRAMS, false);
            mCopyAttachmentsToRepo = mSharedPreferences.getBoolean(Const.PREF_COPY_ATTACHMENTS_TO_REPO, false);

            // Sanity check
            if (((mMathUrl == null) || (mMathUrl.length() == 0)) || (!Utils.isHTTPS(mMathUrl)))
                mMathUrl = getResources().getString(R.string.pref_math_url_default);

            // Hacks
            mKeepDeletedCopies = mSharedPreferences.getBoolean(Const.PREF_KEEP_DELETED_COPIES, false);
            mShowToolBar = mSharedPreferences.getBoolean(Const.PREF_SHOW_TOOLBAR, true);
            mOpenInMarkdown = mSharedPreferences.getBoolean(Const.PREF_OPEN_IN_MARKDOWN, false);
            mNewNoteFileType = mSharedPreferences.getString(Const.PREF_NEW_NOTE_FILE_TYPE, Const.NEW_NOTE_FILE_TYPE);
            mMarkdownTrigger = mSharedPreferences.getString(Const.PREF_MARKDOWN_TRIGGER, "");
            mSafeModeTag =  mSharedPreferences.getString(Const.PREF_SAFE_MODE_TAG, "");
            mAutoToolBarTag = mSharedPreferences.getString(Const.PREF_AUTO_TOOLBAR_TAG, "");
            mLinkifyTrigger = mSharedPreferences.getString(Const.PREF_LINKIFY_TRIGGER, "");
            mLaTeXSingleDollar = mSharedPreferences.getBoolean(Const.PREF_LATEX_SINGLE_DOLLAR, false);
            mAppendCustomStyle = mSharedPreferences.getBoolean(Const.PREF_APPEND_CUSTOM_STYLE, false);
            mIndentChar = mSharedPreferences.getString(Const.PREF_INDENT_CHAR, Const.INDENTATION);
            mCustomDateFormat = mSharedPreferences.getString(Const.PREF_CUSTOM_DATE_FORMAT, "");
            mCustomTimeFormat = mSharedPreferences.getString(Const.PREF_CUSTOM_TIME_FORMAT, "");
            mMarkdownLocalCache = mSharedPreferences.getBoolean(Const.PREF_MARKDOWN_LOCAL_CACHE, true);
            mCanvasStrokes = mSharedPreferences.getString(Const.PREF_CANVAS_STROKES, Const.DEFAULT_CANVAS_STROKES);
            mFontSizeList = mSharedPreferences.getString(Const.PREF_FONT_SIZE_LIST, Const.DEFAULT_FONT_SIZE_LIST);
            mMarginList = mSharedPreferences.getString(Const.PREF_MARGIN_LIST, Const.DEFAULT_MARGIN_LIST);
            mEvalBuiltInVariables = mSharedPreferences.getBoolean(Const.PREF_EVAL_BUILT_IN_VARIALBES, false);
            mOled = mSharedPreferences.getBoolean(Const.PREF_OLED, false);
            mWorkingSetSize= Integer.parseInt(mSharedPreferences.getString(Const.PREF_WORKING_SET_SIZE, String.valueOf(Const.WORKING_SET_SIZE)));
            mLabMode = mSharedPreferences.getBoolean(Const.PREF_LAB_MODE, false);

            // Sanity checks
            if ((mCanvasStrokes == null) || (mCanvasStrokes.length() == 0))
                mCanvasStrokes = Const.DEFAULT_CANVAS_STROKES;

            if ((mFontSizeList == null) || (mFontSizeList.length() == 0))
                mFontSizeList = Const.DEFAULT_FONT_SIZE_LIST;
            else if (!mFontSizeList.equals(Const.DEFAULT_FONT_SIZE_LIST)) {
                if (!Utils.isDelimIntList(mFontSizeList, ";"))
                    mFontSizeList = Const.DEFAULT_FONT_SIZE_LIST;    // Fall back to default
            }

            if ((mMarginList == null) || (mMarginList.length() == 0))
                mFontSizeList = Const.DEFAULT_FONT_SIZE_LIST;
            else if (!mMarginList.equals(Const.DEFAULT_MARGIN_LIST)) {
                if (!Utils.isDelimIntList(mMarginList, ";"))
                    mMarginList = Const.DEFAULT_MARGIN_LIST;    // Fall back to default
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        mListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                // Implementation
                if (key.equals(Const.MIRROR_TIMESTAMP)) {
                    updateStatus(null, null);
                }
            }
        };

        mSharedPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    // Handle insert image
    protected void handleInsertImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // Handle runtime permissions
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
                doInsertImage();
            else
                getCameraPermission(getApplicationContext());
        } else
            doInsertImage();
    }

    // Get image from camera
    private void getImageFromCamera() {
        Utils.getSDState();

        if (Utils.isExternalStorageWritable()) {
            try {
                String tmp_path = mLocalRepoPath + "/" + Const.TMP_PATH + "/";

                // Make directory if not available yet
                File dir = new File(tmp_path);
                if (!dir.isDirectory())
                    dir.mkdir();

                if (dir.isDirectory()) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    File photo = new File(tmp_path, Const.TMP_IMAGE);

                    mTmpImageUri = FileProvider.getUriForFile(this,
                            Utils.mPackageName + ".provider",
                            photo);

                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mTmpImageUri);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivityForResult(intent, Const.REQUEST_CODE_INSERT_CAMERA_IMAGE);
                } else {
                    Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), tmp_path + getResources().getString(R.string.error_create_path), Snackbar.LENGTH_SHORT);
                    Utils.anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Snackbar snackbar = Snackbar.make(getCoordinatorLayout(), getResources().getString(R.string.error_no_writable_external_storage), Snackbar.LENGTH_SHORT);
            Utils.anchorSnackbar(snackbar, R.id.fragment_content);
            snackbar.show();
        }
    }

    // Get image from gallery
    private void getImageFromGallary() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), Const.REQUEST_CODE_INSERT_GALLERY_IMAGE);
    }

    // Insert image
    // Source: http://demonuts.com/2017/04/19/pick-image-from-gallery-or-camera-in-android-programmatically/
    protected void doInsertImage() {
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_image_dialog, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);

        dialog.setContentView(view);dialog.setContentView(view);

        // Set buttons
        TextView button_camera = (TextView) view.findViewById(R.id.camera);
        TextView button_gallery = (TextView) view.findViewById(R.id.gallery);

        button_camera.setTypeface(FontCache.getFromAsset(this, "iconfonts.ttf"));
        button_camera.setTextSize(getResources().getDimension(R.dimen.button_font_size_sm));
        button_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromCamera();
                dialog.dismiss();
            }
        });

        button_gallery.setTypeface(FontCache.getFromAsset(this, "iconfonts.ttf"));
        button_gallery.setTextSize(getResources().getDimension(R.dimen.button_font_size_sm));
        button_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageFromGallary();
                dialog.dismiss();
            }
        });

        dialog.show();

        // Animation
        button_camera.startAnimation(mZoomIn);
        button_gallery.startAnimation(mZoomIn);
    }

    // Get camera permission
    protected void getCameraPermission(Context context) {
        final List<String> permissions = new ArrayList<String>();
        List<String> messages = new ArrayList<String>();

        if (!Utils.addPermission(this, permissions, Manifest.permission.CAMERA))
            messages.add(getResources().getString(R.string.rationale_camera_permission));

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
                                ActivityCompat.requestPermissions(DisplayDBEntry.this, permissions.toArray(new String[permissions.size()]), Const.REQUEST_CODE_CAMERA_PERMISSION);
                            }
                        });
                return;
            }
        }
    }

    // Handle funnel
    private void handleFunnel() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics

        // Set up text boxes
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add an edit field for file search
        final AutoCompleteTextView search_str = new AutoCompleteTextView(this);
        search_str.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Setup autocomlete
        String[] tags = mDatasource.getAllActiveRecordsTitles(Const.SORT_BY_ACCESSED, Const.SORT_DESC);
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, R.layout.dropdown_list_item, tags);
        search_str.setAdapter(adapter);
        search_str.requestFocus();

        layout.addView(search_str);

        builder.setView(layout);

        // Set default value
        search_str.setText("");

        search_str.setHint(getResources().getString(R.string.hint_funnel));
        search_str.setTextColor(ContextCompat.getColor(this, R.color.theme_control_activated));
        search_str.setSingleLine();

        // Select all for easy correction
        search_str.setSelectAllOnFocus(true);

        // "Consume" the command
        Utils.insert(mContent, Const.NULL_SYM);

        // Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.dialog_funnel_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    // User clicked OK button
                    String title = search_str.getText().toString().trim();
                    Long temp_id = -1L;

                    // Check if the record already exists
                    ArrayList<DBEntry> results = mDatasource.getRecordByTitle(title);
                    if (results.size() == 1) {
                        temp_id = results.get(0).getId();
                    } else
                        title = Utils.makeFileName(getApplicationContext(), title);

                    // Launch note
                    doLaunchNote(title, (long) temp_id);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                return;
            }
        });

        builder.setNeutralButton(R.string.dialog_funnel_neutral, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    // User clicked button
                    String title = search_str.getText().toString().trim();

                    // Append clipboard
                    doAppendClipboard(title);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                return;
            }
        });

        builder.setNegativeButton(R.string.dialog_funnel_cancel, new DialogInterface.OnClickListener() {
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
    }

    // Handle markdown local search
    private void handleMarkdownLocalFind() {
        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Chain together various setter methods to set the dialog characteristics

        // Set up text boxes
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Add an edit field for markdown local search
        final AutoCompleteTextView search_str = new AutoCompleteTextView(this);
        search_str.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Setup autocomlete
        String[] items = LocalFindHistory.getAllValues();
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, R.layout.dropdown_list_item, items);
        search_str.setAdapter(adapter);
        search_str.requestFocus();

        layout.addView(search_str);

        builder.setView(layout);

        // Set default value
        search_str.setText(mCriteria);

        search_str.setHint(getResources().getString(R.string.hint_chip_local_find));
        search_str.setTextColor(ContextCompat.getColor(this, R.color.theme_control_activated));
        search_str.setSingleLine();

        // Select all for easy correction
        search_str.setSelectAllOnFocus(true);

        // Chain together various setter methods to set the dialog characteristics
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                try {
                    // User clicked OK button
                    String temp= search_str.getText().toString().trim();

                    // Update criteria
                    mCriteria = temp;

                    // Evaluate built-in variables
                    if (mEvalBuiltInVariables) {
                        // Add to history
                        LocalFindHistory.add(temp, temp);
                        temp = evalVariables(temp, true);
                    }

                    final String str = temp;
                    Handler handler = new Handler();
                    Runnable runnable = new Runnable() {
                        @SuppressLint("NewApi")
                        public void run() {
                            mMarkdownView.findAllAsync(str);
                        }
                    };
                    handler.postDelayed(runnable, Const.SCROLL_DELAY);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                return;
            }
        });

        builder.setNegativeButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
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
    }

    // Get date format (wrapper)
    protected SimpleDateFormat getDateFormat() {
        return Utils.getDateFormat(this, mCustomDateFormat);
    }

    // Check mirror existence
    protected boolean hasMirror() {
        // Sanity check
        if ((mLocalRepoPath == null) || (mLocalRepoPath.length() == 0) || (mBackupUri == null))
            return false;

        return Utils.hasSAFSubDir(getApplicationContext(), mBackupUri, Const.MIRROR_PATH);
    }

    // Update mirror
    protected void updateMirror(String destPath, String fileName) {
        // Show progress
        Toast toast = Toast.makeText(getApplicationContext(), Const.HOURGLASS_SYM, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

        // Copy to mirror
        DocumentFile dir = DocumentFile.fromTreeUri(getApplicationContext(), mBackupUri);
        DocumentFile dest_dir = Utils.getSAFSubDir(getApplicationContext(), dir, Const.MIRROR_PATH);
        DocumentFile attachment_dir = Utils.getSAFSubDir(getApplicationContext(), dest_dir, destPath);
        Utils.exportToSAFFile(getApplicationContext(), mLocalRepoPath + "/" + destPath + "/", fileName, attachment_dir);
    }
}
