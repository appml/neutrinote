package com.appmindlab.nano;

/**
 * Created by saelim on 6/22/2015.
 */

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionMode;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;
import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.google.android.material.snackbar.Snackbar;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private Cursor mCursor;
    private DataSource mDatasource;

    // Main list related
    private MainActivity mActivity;
    private boolean mInActionMode = false;
    private int mLastPosition = -1;
    private List<Integer> mUndoable = new ArrayList<>();

    // Multiple select
    private MultiSelector mMultiSelector = new MultiSelector();

    // Action mode
    protected ModalMultiSelectorCallback mActionMode;

    // Shared preferences
    protected SharedPreferences mSharedPreferences;

    // Animation
    private Animation mRotateCenter, mRotateCenterInfinite;

    // Custom fonts
    private HashMap mCustomFonts;

    @NonNull
    @Override
    public String getSectionName(int position) {
        DBEntry entry = getItem(position);
        String criteria = mActivity.getCriteria();
        int len = 1;

        if (mActivity.getOrderBy().equals(Const.SORT_BY_TITLE)) {
            if ((criteria != null) && (criteria.length() == 1) && (Character.isLetter(criteria.charAt(0))))
                len = 2;

            return Utils.getFirstSymbol(entry.getTitle(), len);
        }
        else {
            if (criteria.equals(Const.ACCESSED_AFTER_FILTER))
                return Utils.shortenTimeStamp(entry.getAccessed());

            return Utils.shortenTimeStamp(entry.getModified());
        }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class ViewHolder extends SwappingHolder
            implements View.OnClickListener, View.OnLongClickListener {
        // each data item is just a string in this case
        private TextView mTitle;
        private TextView mContent;
        private TextView mMetadata;
        private TextView mDate;
        private TextView mSize;
        private TextView mStar;
        private ImageView mImage;
        private CardView mCardView;

        private long mId = -1L;

        public ViewHolder(View v) {
            super(v, mMultiSelector);

            // UI elements
            mTitle = (TextView) v.findViewById(R.id.title);
            mContent = (TextView) v.findViewById(R.id.content);
            mMetadata = (TextView) v.findViewById(R.id.metadata);
            mStar = (TextView) v.findViewById(R.id.star);
            mDate = (TextView) v.findViewById(R.id.date);
            mSize = (TextView) v.findViewById(R.id.size);
            mImage = (ImageView) v.findViewById(R.id.icon);
            mCardView = (CardView) v.findViewById(R.id.card_view);

            // Font
            Typeface font = Typeface.createFromAsset(DBApplication.getAppContext().getAssets(), "iconfonts.ttf");

            // UI when selected
            setSelectionModeBackgroundDrawable(ContextCompat.getDrawable(DBApplication.getAppContext(), R.drawable.background_activated));
            setDefaultModeBackgroundDrawable(v.getBackground());

            // Setup event handlers
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            v.setLongClickable(true);

            // Setup star
            mStar.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    mActivity.setCriteria(Const.STARRED_SYM);
                    mActivity.refreshList();
                    mActivity.scrollToTop();
                    mActivity.transitionList();
                    return false;
                }
            });

            // Setup metadata
            mMetadata.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // Toggle between similar and related queries
                    String criteria = mActivity.getCriteria();
                    if ((criteria != null) && (criteria.equals(Const.SIMILARQUERY + mMetadata.getText().toString())))
                        mActivity.setCriteria(Const.RELATEDQUERY + mMetadata.getText().toString());

                    else
                        mActivity.setCriteria(Const.SIMILARQUERY + mMetadata.getText().toString());

                    mActivity.refreshList();
                    mActivity.scrollToTop();
                    mActivity.transitionList();

                    return true;
                }
            });

            // Setup star
            mStar.setTypeface(font);
            mStar.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {

                                             // Hide unstarred item from list with animated effects
                                             String criteria = mActivity.getCriteria();
                                             if ((criteria != null) && (criteria.equals(Const.STARRED_SYM)))
                                                 notifyItemRemoved(getAdapterPosition());

                                             updateStar(mStar, mDatasource.toggleRecordStarStatus(mId));
                                             mActivity.doBasicAppDataBackup();

                                             // Allocate time for animation
                                             mActivity.refreshListDelayed(Const.REFRESH_DELAY);
                                         }
                                     }
            );
        }

        @Override
        public void onClick(View v) {
            if (mMultiSelector.tapSelection(this)) {
                // Selection is on, so tapSelection() toggled item selection.
                updateIcon();
            } else {
                // Selection is off; handle normal item click here.
                sendDisplayIntent(this);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            mActivity.startSupportActionMode(mActionMode);
            mMultiSelector.setSelected(this, true);

            // Update icon
            updateIcon();

            return true;
        }

        // Update star
        public void updateStar(TextView v, int star) {
            if (star == 1) {
                v.setText(Const.STAR_SYM);
                v.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.star_on));
            }
            else {
                v.setText(Const.STAR_SYM);
                v.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.star_off));
            }
        }

        // Update icon
        public void updateIcon() {
            String title = mTitle.getText().toString();
            String metadata = mMetadata.getText().toString();
            int color;

            if (mMultiSelector.isSelected(this.getAdapterPosition(), this.getItemId())) {
                AnimatedVectorDrawable drawable = (AnimatedVectorDrawable) ContextCompat.getDrawable(DBApplication.getAppContext(), R.drawable.ic_done_anim_vector);
                mImage.setImageDrawable(drawable);
                mImage.clearAnimation();
                drawable.start();
            }
            else {
                TextDrawable drawable;

                if ((metadata == null) || (metadata.length() == 0)) {
                    color = ContextCompat.getColor(DBApplication.getAppContext(), R.color.no_metadata_icon_background);
                    try {
                        drawable = TextDrawable.builder().buildRound(Utils.getFirstSymbol(title, 1), color);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                        drawable = TextDrawable.builder().buildRound(Const.NULL_SYM, color);
                    }
                    mMetadata.setVisibility(View.GONE);
                }
                else {
                    ColorGenerator generator = ColorGenerator.MATERIAL;
                    color = generator.getColor(metadata.toLowerCase(Locale.getDefault()));
                    mMetadata.setVisibility(View.VISIBLE);
                    drawable = TextDrawable.builder().buildRound(Utils.getFirstSymbol(title, 1), color);
                }

                mImage.setImageDrawable(drawable);
            }
        }

        // Update size
        public void updateSize(long size) {
            mSize.setText(Utils.readableFileSize(size));

            if (size > Const.SEVERE_FILE_SIZE)
                mSize.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_size_severe));
            else if (size > Const.CRITICAL_FILE_SIZE)
                mSize.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_size_critical));
            else if (size > Const.WARN_FILE_SIZE)
                mSize.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_size_warn));
            else if (size > Const.EARLY_WARN_FILE_SIZE)
                mSize.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_size_early_warn));
            else if (size > Const.MEDIUM_FILE_SIZE)
                mSize.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_size_medium));
        }

        // Minify
        public void minify() {
            // Shrink icon
            mImage.getLayoutParams().width = Const.ICON_MINI_SIZE;
            mImage.getLayoutParams().height = Const.ICON_MINI_SIZE;

            // Hide preview
            mContent.setVisibility(View.GONE);
        }

        // Retrieve entry ID
        public long getId() {
            return this.mId;
        }
    }

    public void add(int position, DBEntry entry) {
        notifyItemInserted(position);
    }

    public void remove(DBEntry entry, int position) {
        mDatasource.markRecordDeletedById(entry.getId(), 1);
        notifyItemRemoved(position);
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CustomAdapter(Cursor cursor, MainActivity activity, DataSource datasource) {
        swapCursor(cursor);
        mDatasource = datasource;
        mActivity = activity;

        // Set up action mode
        setupActionMode();

        // Set up animation
        setupAnimation();

        // Set up custom fonts
        setupCustomFonts();

        // Shared preferences
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // Create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_list_item, parent, false);

        // Set the view's size, margins, paddings and layout parameters
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Steps:
        // 1. Get element from your dataset at this position
        // 2. Replace the contents of the view with that element
        final DBEntry entry = getItem(position);
        String title = entry.getTitle(), content = entry.getContent(), metadata = entry.getMetadata();

        String criteria = mActivity.getCriteria();
        String criteria_first = Utils.extractFirstQueryItem(criteria);
        String preview_mode = mSharedPreferences.getString(Const.PREF_PREVIEW_MODE, Const.PREVIEW_AT_END);
        String icon_behavior = mSharedPreferences.getString(Const.PREF_ICON_BEHAVIOR, Const.ICON_BEHAVIOR_DISABLED);
        String order_by = mSharedPreferences.getString(Const.PREF_ORDER_BY, Const.SORT_BY_TITLE);

        SimpleDateFormat sdf = new SimpleDateFormat(Const.DATE_TIME_FORMAT, Locale.getDefault());
        PrettyTime pretty_time = new PrettyTime(Locale.getDefault());

        Date timestamp;
        String timestamp_str;

        // Apply theme
        applyTheme(holder);

        // Setup preview
        setupPreview(holder);

        // Remove newlines for more compact view
        content = Utils.safeCompactString(content);

        if ((criteria == null) || (criteria.length() < 2) || (criteria.equals(Const.STARRED_SYM)) || (criteria.equals(Const.ALL_SYM)) ||  criteria.equals(Const.MODIFIED_AFTER_FILTER) || criteria.equals(Const.ACCESSED_AFTER_FILTER) || criteria.equals(Const.MODIFIED_NEARBY_FILTER)) {
            // Extract preview
            if (content.length() > Const.PREVIEW_LEN) {
                if (preview_mode.equals(Const.PREVIEW_AT_START)) {
                    content = content.substring(0, Const.PREVIEW_LEN);
                    content = Utils.subStringWordBoundary(content, 0, Const.PREVIEW_LEN - 1);
                }
                else {
                    content = content.substring(content.length() - Const.PREVIEW_LEN, content.length());
                    content = Utils.subStringWordBoundary(content, 1, Const.PREVIEW_LEN);
                }
            }
            else {
                content = Utils.subStringWordBoundary(content, 0, content.length());
            }

            holder.mTitle.setText(title);
            holder.mContent.setText(content);
            holder.mMetadata.setText(metadata);
        }
        else {
            // Handle content preview
            if (!preview_mode.equals(Const.PREVIEW_OFF)) {
                int pos = Utils.safeIndexOf(content, criteria_first, order_by.equals(Const.SORT_BY_TITLE));

                // Extract preview
                if ((pos > 0) && ((Utils.usePreviewHighlight(criteria)))) {
                    if ((pos + Const.PREVIEW_LEN) > content.length()) {
                        content = content.substring(pos);    // till the end
                        content = Utils.subStringWordBoundary(content, 0, content.length());
                    } else {
                        content = content.substring(pos, (pos + Const.PREVIEW_LEN));
                        content = Utils.subStringWordBoundary(content, pos, Const.PREVIEW_LEN - 1);    // Ok to skip the last word
                    }

                    doHighlight(holder.mContent, content, criteria_first);
                } else {
                    if (content.length() > Const.PREVIEW_LEN) {
                        if (preview_mode.equals(Const.PREVIEW_AT_START)) {
                            content = content.substring(0, Const.PREVIEW_LEN);
                            content = Utils.subStringWordBoundary(content, 0, Const.PREVIEW_LEN - 1);
                        } else {
                            content = content.substring(content.length() - Const.PREVIEW_LEN, content.length());
                            content = Utils.subStringWordBoundary(content, 1, Const.PREVIEW_LEN);
                        }

                        holder.mContent.setText(content);
                    } else {
                        content = Utils.subStringWordBoundary(content, 0, content.length());
                        holder.mContent.setText(content);
                    }
                }
            }

            // Apply highlight to titles or metadata as needed
            if ((criteria.startsWith(Const.TITLEONLY)) || (criteria.startsWith(Const.TITLEREGONLY))) {
                doHighlight(holder.mTitle, title, criteria_first);
                holder.mMetadata.setText(metadata);
            }

            else if ((criteria.startsWith(Const.METADATAONLY)) || (criteria.startsWith(Const.METADATAREGONLY))) {
                holder.mTitle.setText(title);
                doHighlight(holder.mMetadata, metadata, criteria_first);
            }

            else if ((criteria.startsWith(Const.TAGALLQUERY)) || (criteria.startsWith(Const.TAGANYQUERY))) {
                holder.mTitle.setText(title);
                holder.mMetadata.setText(metadata);
            }

            else {
                doHighlight(holder.mTitle, title, criteria_first);
                doHighlight(holder.mMetadata, metadata, criteria_first);
            }
        }

        if ((criteria != null) && (criteria.equals(Const.ACCESSED_AFTER_FILTER)))
            timestamp = entry.getAccessed();
        else
            timestamp = entry.getModified();

        if (entry.getAccessed().after(entry.getModified())) {
            timestamp_str = sdf.format(timestamp) + Const.READ_SYM + pretty_time.format(timestamp);
            holder.mDate.setText(timestamp_str);
        }

        else {
            timestamp_str = sdf.format(timestamp) + Const.UNREAD_SYM + pretty_time.format(timestamp);
            holder.mDate.setText(timestamp_str);
        }

        holder.updateSize(entry.getSize());
        holder.mId = entry.getId();

        // Update icon
        holder.updateIcon();
        holder.updateStar(holder.mStar, entry.getStar());

        // Minify when preview is disabled
        if (preview_mode.equals(Const.PREVIEW_OFF))
            holder.minify();

        // Animate
        if (!icon_behavior.equals(Const.ICON_BEHAVIOR_DISABLED))
            doAnimation(holder, position, icon_behavior);
    }

    // Highlight search term
    private void doHighlight(TextView text, String content, String criteria) {
        int pos = content.toLowerCase(Locale.getDefault()).indexOf(criteria.toLowerCase(Locale.getDefault()));

        if (pos == -1)
            text.setText(content);

        else {
            SpannableStringBuilder spann = new SpannableStringBuilder(content);
            spann.setSpan(new BackgroundColorSpan(ContextCompat.getColor(DBApplication.getAppContext(), R.color.highlight_color)), pos, (pos + criteria.length()), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            text.setText(spann, EditText.BufferType.SPANNABLE);
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return (mCursor == null) ? 0 : mCursor.getCount();
    }

    // Set datasource
    public void setDatasource(DataSource datasource) {
        mDatasource = datasource;
    }

    // Change cursor
    public synchronized void changeCursor(Cursor cursor) {
        Cursor old = swapCursor(cursor);
        if (old != null) {
            old.close();
        }

        // Clear selections
        mMultiSelector.clearSelections();
    }

    // Swap cursor
    public synchronized Cursor swapCursor(Cursor cursor) {
        if (mCursor == cursor) {
            return null;
        }

        Cursor oldCursor = mCursor;
        this.mCursor = cursor;
        if (cursor != null) {
            this.notifyDataSetChanged();

            // Reset animation
            resetAnimation();
        }

        // Clear selections
        mMultiSelector.clearSelections();

        return oldCursor;
    }

    // Get item
    private DBEntry getItem(int pos) {
        mCursor.moveToPosition(pos);
        return mDatasource.cursorToRecord(mCursor);
    }

    // Setup action mode
    private void setupActionMode() {
        mActionMode = new ModalMultiSelectorCallback(mMultiSelector) {

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                MenuItem item = menu.findItem(R.id.menu_show_map);
                boolean location_aware = mSharedPreferences.getBoolean(Const.PREF_LOCATION_AWARE, false);
                if (location_aware)
                    item.setVisible(true);
                else
                    item.setVisible(false);

                return super.onPrepareActionMode(actionMode, menu);
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                super.onCreateActionMode(actionMode, menu);
                actionMode.getMenuInflater().inflate(R.menu.main_list_action_mode, menu);
                mInActionMode = true;
                return true;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menu_delete:
                        handleDeletion();
                        mMultiSelector.clearSelections();
                        actionMode.finish();
                        return true;
                    case R.id.menu_show_map:
                        doShowMap();
                        mMultiSelector.clearSelections();
                        actionMode.finish();
                        return true;
                    case R.id.menu_metadata:
                        handleMetadata();
                        mMultiSelector.clearSelections();
                        actionMode.finish();
                        return true;
                    case R.id.menu_append_clipboard:
                        doAppendClipboard();
                        mMultiSelector.clearSelections();
                        actionMode.finish();
                        return true;
                    default:
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                super.onDestroyActionMode(actionMode);
                mMultiSelector.clearSelections();
                mActivity.refreshList();  // WORKAROUND: refresh the list to remove multi-select artifacts
                mInActionMode = false;
            }
        };
    }

    // Open up the editor
    private void sendDisplayIntent(ViewHolder result) {
        // Create an intent
        long id = result.getId();

        Intent intent = new Intent(mActivity, DisplayDBEntry.class);
        intent.putExtra(Const.EXTRA_ID, id);
        intent.putExtra(Const.EXTRA_CRITERIA, mActivity.getCriteria());

        // Show progress bar
        mActivity.showIOProgressBar(mActivity.getResources().getString(R.string.status_opening) + "\"" + result.mTitle.getText() + "\" " + Const.ELLIPSIS_SYM);

        // Send the intent
        mActivity.startActivity(intent);
    }

    // Handle deletion
    private void handleDeletion() {
        int count = 0;
        String msg = "";

        // Reset undo list
        mUndoable = new ArrayList<>();

        // Update UI
        for (int i = getItemCount(); i >= 0; i--) {
            if (mMultiSelector.isSelected(i, 0)) {
                mUndoable.add(i);
                notifyItemRemoved(i);
                count++;
            }
        }

        // Show undo bar
        if (count == 0)
            return;
        else if (count == 1)
            msg = getItem(mUndoable.get(0)).getTitle() + DBApplication.getAppContext().getResources().getString(R.string.status_deleted_remotely);
        else
            msg = count + DBApplication.getAppContext().getResources().getString(R.string.status_items_deleted);

        // Show undo bar
        Snackbar.make(mActivity.findViewById(R.id.coordinator), msg, Snackbar.LENGTH_SHORT).setCallback(
                new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        switch(event) {
                            case Snackbar.Callback.DISMISS_EVENT_ACTION:
                                // Undo delete
                                undoDeletion();
                                break;
                            case Snackbar.Callback.DISMISS_EVENT_TIMEOUT:
                                // Delete
                                doDeletion();
                                break;
                        }
                    }
                }).setAction(DBApplication.getAppContext().getResources().getString(R.string.snack_bar_button_undo), new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Undo delete
                undoDeletion();
            }
        }).show();
    }

    // Perform deletion
    private void doDeletion() {
        int count = 0;

        // Allocate time for animation
        mActivity.refreshListDelayed(Const.REFRESH_DELAY);

        for (Integer i:mUndoable) {
            mDatasource.markRecordDeletedById(getItem(i).getId(), 1);
            count++;
        }

        if (count > 1)
            Toast.makeText(DBApplication.getAppContext(), count + DBApplication.getAppContext().getResources().getString(R.string.status_items_deleted), Toast.LENGTH_SHORT).show();
        else if (count == 1)
            Toast.makeText(DBApplication.getAppContext(), getItem(mUndoable.get(0)).getTitle() + DBApplication.getAppContext().getResources().getString(R.string.status_deleted_remotely), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(DBApplication.getAppContext(), DBApplication.getAppContext().getResources().getString(R.string.info_select_one), Toast.LENGTH_SHORT).show();
    }

    // Perform undo deletion
    private void undoDeletion() {
        for (Integer i:mUndoable) {
            notifyItemInserted(i);
            notifyItemRangeChanged(i, getItemCount());
        }

        // Reset undoable
        mUndoable.clear();
    }

    // Build map source
    protected String buildMapSource(double centerLatitude, double centerLongtitude, String markers) {
        String html;

        html =  "<html>";

        html += "<head>";
        html += "<link rel='stylesheet' href='" + Const.LEAFLET_CSS + "' />";
        html += "<meta name='viewport' content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no' />";
        html += "</head>";

        html += "<body>";

        html += "<div id='map' style='height: " + Const.MAP_HEIGHT + "'px></div>";

        html += "<script src='" + Const.LEAFLET_JS + "'></script>";
        html += "<script type='text/javascript' src='html/bounce-marker.js'></script>";

        html += "<script>";
        html += "var map = L.map('map').setView([" + centerLatitude + ", " + centerLongtitude + "], " + Const.MAP_DEFAULT_ZOOM + ");";
        html += "L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "    attribution: '&copy; <a href=\"https://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors'\n" +
                "}).addTo(map);";
        html += markers;
        html += "</script>";

        html += "</body>";

        html += "</html>";

        return html;
    }

    // Handle show map
    private void handleShowMap(double centerLatitude, double centerLongtitude, String markers) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        if (!Utils.checkMultiWindowMode(mActivity)) {
            builder.setTitle(DBApplication.getAppContext().getResources().getString(R.string.dialog_show_map_title));
            builder.setMessage("");
        }

        WebView webView = new WebView(mActivity);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);      // For accessibility
        settings.setBuiltInZoomControls(true);    // Enable zoom
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);    // Offline cache

        webView.loadDataWithBaseURL("file:///android_asset/", buildMapSource(centerLatitude, centerLongtitude, markers), "text/html", "utf-8", null);
        webView.setWebViewClient(new WebViewClient() {
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                view.loadUrl(url);
                return true;
            }
        });

        builder.setView(webView);

        builder.setPositiveButton(DBApplication.getAppContext().getResources().getString(R.string.dialog_show_map_ok), new DialogInterface.OnClickListener() {
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

    // Show notes on map
    private void doShowMap() {
        int count = 0;
        DBEntry entry = null;
        String markers = "";
        double latitude = -1L, longtitude = -1L, center_latitude = -1L, center_longtitude = -1L;

        if (!Utils.isConnected(mActivity)) {
            Toast.makeText(DBApplication.getAppContext(), DBApplication.getAppContext().getResources().getString(R.string.error_network), Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = getItemCount(); i >= 0; i--) {
            if (mMultiSelector.isSelected(i, 0)) {
                entry = getItem(i);
                latitude = entry.getLatitude();
                longtitude = entry.getLongitude();

                if ((latitude != -1L) && (longtitude != -1L)) {
                    markers += "L.marker([" + latitude + ", " + longtitude + "], {bounceOnAdd: true}).addTo(map).bindPopup('<b>" + entry.getTitle() + "</b>').openPopup();";

                    if ((center_latitude == -1L) && (center_longtitude == -1L)) {
                        center_latitude = latitude;
                        center_longtitude = longtitude;
                    }
                }

                count++;
            }
        }

        if ((center_latitude == -1L) && (center_longtitude == -1L)) {
            Toast.makeText(DBApplication.getAppContext(), DBApplication.getAppContext().getResources().getString(R.string.status_no_location_data), Toast.LENGTH_SHORT).show();
        }
        else if ((count > 0) && (entry != null)) {
            handleShowMap(center_latitude, center_longtitude, markers);
        }
        else
            Toast.makeText(DBApplication.getAppContext(), DBApplication.getAppContext().getResources().getString(R.string.info_select_one), Toast.LENGTH_SHORT).show();

        mActivity.refreshList();
    }

    // Verify there is at least one selected item
    private boolean hasSelectedItem() {
        for (int i = getItemCount(); i >= 0; i--) {
            if (mMultiSelector.isSelected(i, 0)) {
                return true;
            }
        }

        return false;
    }

    // Handle metadata
    private void handleMetadata() {
        // Sanity check
        if (!hasSelectedItem()) {
            Toast.makeText(DBApplication.getAppContext(), DBApplication.getAppContext().getResources().getString(R.string.info_select_one), Toast.LENGTH_SHORT).show();
            return;
        }

        // Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);

        // Multi-window mode
        boolean multiWindowMode = Utils.checkMultiWindowMode(mActivity);
        String delim = multiWindowMode ? Const.EMPTY_SYM : Const.NEWLINE;

        // Chain together various setter methods to set the dialog characteristics
        String msg = DBApplication.getAppContext().getResources().getString(R.string.dialog_metadata_message);

        // Add an edit field for metadata search
        final AutoCompleteTextView metadata_str = new AutoCompleteTextView(mActivity);

        // Setup autocomlete
        String[] tags = mDatasource.getUniqueMetadata();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mActivity, R.layout.dropdown_list_item, tags);
        metadata_str.setAdapter(adapter);

        builder.setView(metadata_str);
        metadata_str.setImeOptions(EditorInfo.IME_ACTION_DONE | EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Remember the selected items at this point before they go away
        final ArrayList<Long> ids = new ArrayList<Long>();
        String titles = delim;
        for (int i=0, j=-1; i < getItemCount(); i++) {
            if (mMultiSelector.isSelected(i, 0)) {
                ids.add(getItem(i).getId());

                // Selected titles
                titles = titles + Const.BULLET_SYM + getItem(i).getTitle() + delim;

                // Set default metadata
                if (j == -1) {
                    metadata_str.setText(getItem(i).getMetadata());
                    j = i;
                }
            }
        }

        // Truncate in multiwindow mode
        if (multiWindowMode) {
            if (titles.length() > Const.DIALOG_PREVIEW_LEN)
                titles = Utils.subStringWordBoundary(titles, 0, Const.DIALOG_PREVIEW_LEN - 1) + Const.DIALOG_PREVIEW_MORE_SYM;
        }

        msg = titles + Const.NEWLINE + msg;
        builder.setMessage(msg).setTitle(R.string.dialog_metadata_title);

        builder.setPositiveButton(R.string.dialog_metadata_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String metadata = metadata_str.getText().toString().trim().replaceAll("\\r\\n|\\r|\\n|" + Const.DELIMITER + "|" + Const.SUBDELIMITER, " ");
                doSetMetadata(ids, metadata);
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
    protected void doSetMetadata(ArrayList<Long> ids, String metadata) {
        int count = 0;

        for (int i=0; i < ids.size(); i++) {
            mDatasource.updateRecordMetadata(ids.get(i), metadata);
            count++;
        }

        if (count > 0)
            Toast.makeText(DBApplication.getAppContext(), count + DBApplication.getAppContext().getResources().getString(R.string.status_items_updated), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(DBApplication.getAppContext(), DBApplication.getAppContext().getResources().getString(R.string.info_select_one), Toast.LENGTH_SHORT).show();

        mActivity.refreshList();

        // Backup basic app data
        mActivity.doBasicAppDataBackup();
    }

    // Set append clipboard
    protected void doAppendClipboard() {
        String clipboard_text = Utils.getClipboardText(DBApplication.getAppContext(), (ClipboardManager) DBApplication.getAppContext().getSystemService(mActivity.CLIPBOARD_SERVICE), -1, true);

        // Sanity check
        if (clipboard_text.length() == 0) {
            Snackbar.make(mActivity.findViewById(android.R.id.content), DBApplication.getAppContext().getResources().getString(R.string.status_empty_clipboard), Snackbar.LENGTH_SHORT).show();
            return;
        }

        boolean store_location = mSharedPreferences.getBoolean(Const.PREF_LOCATION_AWARE, false);
        Location location = null;
        if (store_location)
            location = Utils.getLocation(DBApplication.getAppContext());

        int count = 0;

        for (int i=0; i < getItemCount(); i++) {
            if (mMultiSelector.isSelected(i, 0)) {
                ArrayList<DBEntry> results = mDatasource.getRecordById(getItem(i).getId());
                if (results.size() == 1) {
                    DBEntry entry = results.get(0);

                    StringBuilder sb = new StringBuilder();
                    sb.append(entry.getContent());
                    sb.append("\r\n\r\n");
                    sb.append(clipboard_text);

                    // Sanity check
                    if (sb.length() < entry.getSize()) {
                        Toast.makeText(DBApplication.getAppContext(), count + DBApplication.getAppContext().getResources().getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
                        continue;
                    }

                    mDatasource.updateRecord(entry.getId(), entry.getTitle(), sb.toString(), entry.getStar(), null, true, entry.getTitle());
                    count++;

                    if ((store_location) && (location != null))
                        mDatasource.updateRecordCoordinates(entry.getId(), location.getLatitude(), location.getLongitude());
                }
            }
        }

        if (count > 0) {
            // Set pending fresh flag
            MainActivity.setPendingStatus(true);
            Toast.makeText(DBApplication.getAppContext(), count + DBApplication.getAppContext().getResources().getString(R.string.status_items_updated), Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(DBApplication.getAppContext(), DBApplication.getAppContext().getResources().getString(R.string.info_select_one), Toast.LENGTH_SHORT).show();

        mActivity.refreshList();
    }

    // Get selection states
    protected Bundle getSelectionStates() {
        Bundle bundle = mMultiSelector.saveSelectionStates();
        bundle.putBoolean("inActionMode", mInActionMode);
        return bundle;
    }

    // Set selection states
    protected void setSelectionStates(Bundle savedStates) {
        if (savedStates.getBoolean("inActionMode"))
            mActivity.startSupportActionMode(mActionMode);

        mMultiSelector.restoreSelectionStates(savedStates);
    }

    // Apply theme
    protected void applyTheme(ViewHolder holder) {
        // Retrieve theme preference
        String theme = mSharedPreferences.getString(Const.PREF_THEME, "day");

        // Retrieve lux preference
        boolean lux = mSharedPreferences.getBoolean(Const.PREF_LUX, false);

        String mode = Const.NULL_SYM;

        if (theme.equals(Const.SYSTEM_THEME)) {
            int flags = DBApplication.getAppContext().getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
            if (flags == Configuration.UI_MODE_NIGHT_YES) {
                mode = Const.NIGHT_THEME;
            }
            else {
                mode = Const.DAY_THEME;
            }
        }

        if ((theme.equals("night")) || (mode.equals(Const.NIGHT_THEME)))  {
            holder.mTitle.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_title_night));
            holder.mTitle.setBackgroundColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_background_night));

            holder.mContent.setTextColor(Utils.getWhiteColor(DBApplication.getAppContext(), R.color.list_item_content_night, lux));
            holder.mContent.setBackgroundColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_background_night));

            holder.mSize.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_size_night));
            holder.mDate.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_date_night));
            holder.mMetadata.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_metadata_night));
            holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.cardview_background_night));
        }
        else if ((theme.equals("dark")) || (mode.equals(Const.DARK_THEME)))  {
            holder.mTitle.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_title_dark));
            holder.mTitle.setBackgroundColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_background_dark));

            holder.mContent.setTextColor(Utils.getWhiteColor(DBApplication.getAppContext(), R.color.list_item_content_dark, lux));
            holder.mContent.setBackgroundColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_background_dark));

            holder.mSize.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_size_dark));
            holder.mDate.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_date_dark));
            holder.mMetadata.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_metadata_dark));
            holder.mCardView.setCardBackgroundColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.cardview_background_dark));
        }
        else {
            holder.mTitle.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_title_day));
            holder.mTitle.setBackgroundColor(Utils.getWhiteColor(DBApplication.getAppContext(), R.color.list_item_background_day, lux));

            holder.mContent.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_content_day));
            holder.mContent.setBackgroundColor(Utils.getWhiteColor(DBApplication.getAppContext(), R.color.list_item_background_day, lux));

            holder.mSize.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_size_day));
            holder.mDate.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_date_day));
            holder.mMetadata.setTextColor(ContextCompat.getColor(DBApplication.getAppContext(), R.color.list_item_metadata_day));
            holder.mCardView.setCardBackgroundColor(Utils.getWhiteColor(DBApplication.getAppContext(), R.color.cardview_background_day, lux));
        }
    }

    // Setup preview
    protected void setupPreview(ViewHolder holder) {
        try {
            String font_family = mSharedPreferences.getString(Const.PREF_FONT_FAMILY, Const.DEFAULT_FONT_FAMILY);

            if (font_family.equals(Const.SYSTEM_FONT_NAME)) {
                holder.mContent.setTypeface(Typeface.DEFAULT);
            } else if (font_family.equals("Monospace")) {
                holder.mContent.setTypeface(Typeface.MONOSPACE);
            } else if (font_family.equals("Serif")) {
                holder.mContent.setTypeface(Typeface.SERIF);
            } else if (font_family.equals("Sans Serif")) {
                holder.mContent.setTypeface(Typeface.SANS_SERIF);
            } else if (font_family.equals("Roboto Light")) {
                holder.mContent.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            } else if (font_family.equals("Roboto Medium")) {
                holder.mContent.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            } else if (font_family.equals("Roboto Condensed Light")) {
                holder.mContent.setTypeface(FontCache.getFromAsset(DBApplication.getAppContext(), "RobotoCondensed-Light.ttf"));
            } else if (font_family.equals("Roboto Condensed Regular")) {
                holder.mContent.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));
            } else if (font_family.equals("Roboto Mono Light")) {
                holder.mContent.setTypeface(FontCache.getFromAsset(DBApplication.getAppContext(), "RobotoMono-Light.ttf"));
            } else if (font_family.equals("Roboto Mono Regular")) {
                holder.mContent.setTypeface(FontCache.getFromAsset(DBApplication.getAppContext(), "RobotoMono-Regular.ttf"));
            } else if (font_family.equals("Roboto Slab Light")) {
                holder.mContent.setTypeface(FontCache.getFromAsset(DBApplication.getAppContext(), "RobotoSlab-Light.ttf"));
            } else if (font_family.equals("Roboto Slab Regular")) {
                holder.mContent.setTypeface(FontCache.getFromAsset(DBApplication.getAppContext(), "RobotoSlab-Regular.ttf"));
            } else if (font_family.equals("Roboto Slab Bold")) {
                holder.mContent.setTypeface(FontCache.getFromAsset(DBApplication.getAppContext(), "RobotoSlab-Bold.ttf"));
            } else if (mCustomFonts != null) {
                CustomFont font = (CustomFont) mCustomFonts.get(font_family);
                if (font != null) {
                    String local_repo_path = mSharedPreferences.getString(Const.PREF_LOCAL_REPO_PATH, null);
                    String path = local_repo_path + "/" + Const.CUSTOM_FONTS_PATH + "/" + font.getPath();
                    holder.mContent.setTypeface(FontCache.getFromFile(path));
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Setup animation
    protected void setupAnimation() {
        mRotateCenter = AnimationUtils.loadAnimation(DBApplication.getAppContext(), R.anim.rotate_center);
        mRotateCenterInfinite = AnimationUtils.loadAnimation(DBApplication.getAppContext(), R.anim.rotate_center_infinite);
    }

    // Reset animation
    protected void resetAnimation() {
        mLastPosition = -1;
    }

    // Do animation
    private void doAnimation(ViewHolder holder, int position, String behavior)
    {
        // Animate newly entered item
        if (behavior.equals(Const.ICON_BEHAVIOR_ENABLED)) {
            if (position > mLastPosition) {
                holder.mImage.startAnimation(mRotateCenter);
                mLastPosition = position;
            }
        }
        else if (behavior.equals(Const.ICON_BEHAVIOR_SNOOZE)) {
            DBEntry entry = getItem(position);
            String metadata = entry.getMetadata();
            Calendar now;

            if ((metadata != null) && (metadata.length() > 0) && (metadata.contains("+"))) {

                Pattern pattern = Pattern.compile(Const.SNOOZE_PATTERN);
                Matcher matcher = pattern.matcher(metadata);

                if (matcher.find()) {
                    String snooze = metadata.substring(matcher.start(), matcher.end());
                    int val = Integer.parseInt(snooze.substring(1, snooze.length() - 1));

                    if (snooze.endsWith(Const.SNOOZE_MINUTE)) {
                        now = Calendar.getInstance(Locale.getDefault());
                        now.add(Calendar.MINUTE, -1 * val);
                        if ((entry.getAccessed().before(now.getTime()))) {
                            holder.mImage.startAnimation(mRotateCenterInfinite);
                            return;
                        }
                    }

                    if (snooze.endsWith(Const.SNOOZE_HOUR)) {
                        now = Calendar.getInstance(Locale.getDefault());
                        now.add(Calendar.HOUR, -1 * val);
                        if ((entry.getAccessed().before(now.getTime()))) {
                            holder.mImage.startAnimation(mRotateCenterInfinite);
                            return;
                        }
                    }

                    if (snooze.endsWith(Const.SNOOZE_DAY)) {
                        now = Calendar.getInstance(Locale.getDefault());
                        now.add(Calendar.HOUR, -24 * val);
                        if ((entry.getAccessed().before(now.getTime()))) {
                            holder.mImage.startAnimation(mRotateCenterInfinite);
                            return;
                        }
                    }

                    if (snooze.endsWith(Const.SNOOZE_WEEK)) {
                        now = Calendar.getInstance(Locale.getDefault());
                        now.add(Calendar.HOUR, -24 * 7 * val);
                        if ((entry.getAccessed().before(now.getTime()))) {
                            holder.mImage.startAnimation(mRotateCenterInfinite);
                            return;
                        }
                    }

                    if (snooze.endsWith(Const.SNOOZE_MONTH)) {
                        now = Calendar.getInstance(Locale.getDefault());
                        now.add(Calendar.DAY_OF_MONTH, -1 * val);
                        if ((entry.getAccessed().before(now.getTime()))) {
                            holder.mImage.startAnimation(mRotateCenterInfinite);
                            return;
                        }

                        if (snooze.endsWith(Const.SNOOZE_YEAR)) {
                            now = Calendar.getInstance();
                            now.add(Calendar.YEAR, -1 * val);
                            if ((entry.getAccessed().before(now.getTime()))) {
                                holder.mImage.startAnimation(mRotateCenterInfinite);
                                return;

                            }
                        }
                    }
                }
            }
        }
    }

    // Setup custom fonts
    private void setupCustomFonts() {
        String fonts_file;

        // Load font configuration
        fonts_file = Utils.makeFileName(DBApplication.getAppContext(), Const.CUSTOM_FONTS_FILE);
        ArrayList<DBEntry> results = mDatasource.getRecordByTitle(fonts_file);
        if (results.size() == 1) {
            // Initialize
            mCustomFonts = new HashMap();

            // Build a list of custom fonts, each separated by a blank line
            DBEntry entry = results.get(0);
            String[] fonts = entry.getContent().split(Const.BLANK_LINE);
            for (int i=0; i < fonts.length; i++) {
                // Skip if commented out
                if (fonts[i].startsWith(Const.COMMENT_SYM))
                    continue;

                // Load parameters, each separated by a newline
                String[] params = fonts[i].split(Const.NEWLINE);

                // Sanity check
                if (params.length != 4) continue;

                // Save the configuration
                mCustomFonts.put(params[0], new CustomFont(params[0], params[1], params[2], params[3]));
            }
        }
    }
}