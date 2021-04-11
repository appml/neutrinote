package com.appmindlab.nano;

/**
 * Created by saelim on 7/28/2015.
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.text.Editable;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DirectoryChooserDialog
{
    private boolean mIsNewFolderEnabled = true;
    private String mSDCardDirectory = "/storage";
    private Context mContext;
    private TextView mTitleView;

    private String mDir = "/";
    private List<String> mSubdirs = null;
    private ChosenDirectoryListener mChosenDirectoryListener = null;
    private ArrayAdapter<String> mListAdapter = null;

    //////////////////////////////////////////////////////
    // Callback interface for selected directory
    //////////////////////////////////////////////////////
    public interface ChosenDirectoryListener
    {
        public void onChosenDir(String chosenDir);
    }

    public DirectoryChooserDialog(Context context, ChosenDirectoryListener chosenDirectoryListener)
    {
        mContext = context;

        // In versions after Android P, only app folder supports file access
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
            mSDCardDirectory = Utils.getAppPathRemovableStorage(context);
        else
            mSDCardDirectory = Environment.getExternalStorageDirectory().getAbsolutePath();

        // mSDCardDirectory = "/";
        mChosenDirectoryListener = chosenDirectoryListener;

        try
        {
            mSDCardDirectory = new File(mSDCardDirectory).getCanonicalPath();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////////
    // setNewFolderEnabled() - enable/disable new folder button
    ///////////////////////////////////////////////////////////////////////

    public void setNewFolderEnabled(boolean isNewFolderEnabled)
    {
        mIsNewFolderEnabled = isNewFolderEnabled;
    }

    public boolean getNewFolderEnabled()
    {
        return mIsNewFolderEnabled;
    }

    ///////////////////////////////////////////////////////////////////////
    // chooseDirectory() - load directory chooser dialog for initial
    // default sdcard directory
    ///////////////////////////////////////////////////////////////////////

    public void chooseDirectory()
    {
        // Initial directory is sdcard directory
        chooseDirectory(mSDCardDirectory);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // chooseDirectory(String dir) - load directory chooser dialog for initial
    // input 'dir' directory
    ////////////////////////////////////////////////////////////////////////////////

    public void chooseDirectory(String dir)
    {
        File dirFile = new File(dir);
        if (! dirFile.exists() || ! dirFile.isDirectory())
        {
            dir = mSDCardDirectory;
        }

        try
        {
            dir = new File(dir).getCanonicalPath();
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return;
        }

        mDir = dir;
        mSubdirs = getDirectories(dir);

        class DirectoryOnClickListener implements DialogInterface.OnClickListener
        {
            public void onClick(DialogInterface dialog, int item)
            {
                String subFolder = "" + ((AlertDialog) dialog).getListView().getAdapter().getItem(item);

                // Navigate into the sub-directory
                if (Const.UP_SYM.equals(subFolder)) {
                    int firstSlash = mDir.indexOf('/');
                    int lastSlash = mDir.lastIndexOf('/');
                    if (firstSlash >= 0 && lastSlash > firstSlash) {
                        mDir = mDir.substring(0, lastSlash);
                    }
                } else {
                    mDir += "/" + subFolder;
                }

                updateDirectory();
            }
        }

        AlertDialog.Builder dialogBuilder =
                createDirectoryChooserDialog(dir, mSubdirs, new DirectoryOnClickListener());

        dialogBuilder.setPositiveButton(R.string.dialog_directory_chooser_ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                // Current directory chosen
                if (mChosenDirectoryListener != null)
                {
                    // Call registered listener supplied with the chosen directory
                    mChosenDirectoryListener.onChosenDir(mDir);
                }
            }
        }).setNegativeButton(R.string.dialog_directory_chooser_cancel, null);

        final AlertDialog dirsDialog = dialogBuilder.create();

        // Show directory chooser dialog
        dirsDialog.show();
    }

    private boolean createSubDir(String newDir)
    {
        File newDirFile = new File(newDir);
        if (! newDirFile.exists() )
        {
            return newDirFile.mkdir();
        }

        return false;
    }

    private List<String> getDirectories(String dir)
    {
        List<String> dirs = new ArrayList<String>();

        try
        {
            File dirFile = new File(dir);
            if (! dirFile.exists() || ! dirFile.isDirectory())
            {
                return dirs;
            }

            if (!dir.equals(mSDCardDirectory))
                dirs.add(Const.UP_SYM);

            for (File file : dirFile.listFiles())
            {
                if ( file.isDirectory() )
                {
                    dirs.add( file.getName() );
                }
            }
        }
        catch (Exception e)
        {
        }

        Collections.sort(dirs, new Comparator<String>() {
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        return dirs;
    }

    // A workaround to fix title
    @SuppressWarnings("deprecation")
    private Spanned prepareTitle(String title) {
        Spanned spanned;

        // Work around to remove redundant slash
        title = Utils.cleanPath(title);
        title = "<b>" + mContext.getResources().getString(R.string.dialog_directory_chooser_selected_folder) + "</b>" + title;

        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            spanned = Html.fromHtml(title,Html.FROM_HTML_MODE_LEGACY);
        } else {
            spanned = Html.fromHtml(title);
        }

        return spanned;
    }

    private AlertDialog.Builder createDirectoryChooserDialog(String title, List<String> listItems,
                                                             DialogInterface.OnClickListener onClickListener)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);

        // Create custom view for AlertDialog title containing
        // current directory TextView and possible 'New folder' button.
        // Current directory TextView allows long directory path to be wrapped to multiple lines.
        LinearLayout titleLayout = new LinearLayout(mContext);
        titleLayout.setOrientation(LinearLayout.VERTICAL);

        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "RobotoCondensed-Light.ttf" );
        Typeface font_awesome = Typeface.createFromAsset(mContext.getAssets(), "iconfonts.ttf");

        mTitleView = new TextView(mContext);
        mTitleView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mTitleView.setTextColor(Color.WHITE);
        mTitleView.setTypeface(font);
        mTitleView.setTextSize(16);
        mTitleView.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        mTitleView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        mTitleView.setText(prepareTitle(title));

        Button newDirButton = new Button(mContext);

        newDirButton.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        newDirButton.setText(Const.ADD_SYM);
        newDirButton.setBackgroundColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        newDirButton.setTextSize(24);
        newDirButton.setTextColor(Color.WHITE);
        newDirButton.setTypeface(font_awesome);
        newDirButton.setContentDescription(mContext.getResources().getString(R.string.dialog_directory_chooser_new_directory_button));
        newDirButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final EditText input = new EditText(mContext);
                input.setSingleLine();

                // Show new folder name input dialog
                new AlertDialog.Builder(mContext).
                        setTitle(R.string.dialog_directory_chooser_new_directory_title).
                        setView(input).setPositiveButton(R.string.dialog_directory_chooser_ok, new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int whichButton)
                    {
                        Editable newDir = input.getText();
                        String newDirName = newDir.toString();
                        // Create new directory
                        if ( createSubDir(mDir + "/" + newDirName) )
                        {
                            // Navigate into the new directory
                            mDir += "/" + newDirName;
                            updateDirectory();
                        }
                        else
                        {
                            Toast.makeText(
                                    mContext, "Failed to create '" + newDirName +
                                            "' folder", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).setNegativeButton(R.string.dialog_directory_chooser_cancel, null).show();
            }
        });

        if (! mIsNewFolderEnabled)
        {
            newDirButton.setVisibility(View.GONE);
        }

        titleLayout.addView(mTitleView);
        titleLayout.addView(newDirButton);

        dialogBuilder.setCustomTitle(titleLayout);

        mListAdapter = createListAdapter(listItems);

        dialogBuilder.setSingleChoiceItems(mListAdapter, -1, onClickListener);
        dialogBuilder.setCancelable(false);

        return dialogBuilder;
    }

    private void updateDirectory()
    {
        mSubdirs.clear();
        mSubdirs.addAll(getDirectories(mDir));
        mTitleView.setText(prepareTitle(mDir));

        mListAdapter.notifyDataSetChanged();
    }

    private ArrayAdapter<String> createListAdapter(List<String> items)
    {
        return new ArrayAdapter<String>(mContext,
                android.R.layout.select_dialog_item, android.R.id.text1, items)
        {
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);

                if (v instanceof TextView)
                {
                    // Enable list item (directory) text wrapping
                    TextView tv = (TextView) v;
                    tv.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    tv.setEllipsize(null);
                }
                return v;
            }
        };
    }
}