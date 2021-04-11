package com.appmindlab.nano;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.Locale;

/**
 * Created by saelim on 7/31/2015.
 */

public class EditToolFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener  {
    OnEditToolFragmentSelectedListener mCallback;

    // Container Activity must implement this interface
    public interface OnEditToolFragmentSelectedListener {
        void onEditToolSelected(int id);
        void showHelp(View view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Button button_save, button_undo, button_redo;
        Button button_markdown, button_timestamp, button_datestamp, button_locationstamp, button_text_expand, button_draw;
        Button button_top, button_bottom;
        Button button_local_find, button_local_replace;
        Button button_barcode, button_image;
        Button button_define, button_calculate, button_web_search;
        Button button_encrypt, button_decrypt;
        Button button_close;

        Typeface font_awesome = FontCache.getFromAsset(getActivity(), "iconfonts.ttf");

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String[] excluded = sharedPreferences.getString(Const.PREF_EXCLUDED_BUTTONS, Const.DEFAULT_EXCLUDED_BUTTONS).toLowerCase(Locale.getDefault()).split(";");
        boolean lab_mode = sharedPreferences.getBoolean(Const.PREF_LAB_MODE, false);
        String toolbox_mode = sharedPreferences.getString(Const.PREF_TOOLBOX_MODE, Const.TOOLBOX_MODE_STATEFUL);

        View v;

        if (toolbox_mode.equals(Const.TOOLBOX_MODE_STATELESS))
            v = inflater.inflate(R.layout.edit_tool_stateless, container, false);
        else if (toolbox_mode.equals(Const.TOOLBOX_MODE_PIN_SAVE))
            v = inflater.inflate(R.layout.edit_tool_pin_save, container, false);
        else
            v = inflater.inflate(R.layout.edit_tool, container, false);

        button_save = (Button) v.findViewById(R.id.button_save);
        button_save.setOnClickListener(this);
        button_save.setOnLongClickListener(this);
        button_save.setTypeface(font_awesome);

        button_undo = (Button) v.findViewById(R.id.button_undo);
        button_undo.setOnClickListener(this);
        button_undo.setOnLongClickListener(this);
        button_undo.setTypeface(font_awesome);

        button_redo = (Button) v.findViewById(R.id.button_redo);
        button_redo.setOnClickListener(this);
        button_redo.setOnLongClickListener(this);
        button_redo.setTypeface(font_awesome);

        button_markdown = (Button) v.findViewById(R.id.button_markdown);
        button_markdown.setOnClickListener(this);
        button_markdown.setOnLongClickListener(this);
        button_markdown.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_MARKDOWN))
            button_markdown.setVisibility(View.GONE);

        button_timestamp = (Button) v.findViewById(R.id.button_timestamp);
        button_timestamp.setOnClickListener(this);
        button_timestamp.setOnLongClickListener(this);
        button_timestamp.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_TIMESTAMP))
            button_timestamp.setVisibility(View.GONE);

        button_datestamp = (Button) v.findViewById(R.id.button_datestamp);
        button_datestamp.setOnClickListener(this);
        button_datestamp.setOnLongClickListener(this);
        button_datestamp.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_DATESTAMP))
            button_datestamp.setVisibility(View.GONE);

        button_locationstamp = (Button) v.findViewById(R.id.button_locationstamp);
        button_locationstamp.setOnClickListener(this);
        button_locationstamp.setOnLongClickListener(this);
        button_locationstamp.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_LOCATIONSTAMP))
            button_locationstamp.setVisibility(View.GONE);

        button_text_expand = (Button) v.findViewById(R.id.button_text_expand);
        button_text_expand.setOnClickListener(this);
        button_text_expand.setOnLongClickListener(this);
        button_text_expand.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_EXPAND))
            button_text_expand.setVisibility(View.GONE);

        button_draw = (Button) v.findViewById(R.id.button_draw);
        button_draw.setOnClickListener(this);
        button_draw.setOnLongClickListener(this);
        button_draw.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_DRAW))
            button_draw.setVisibility(View.GONE);

        button_top = (Button) v.findViewById(R.id.button_top);
        button_top.setOnClickListener(this);
        button_top.setOnLongClickListener(this);
        button_top.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_TOP))
            button_top.setVisibility(View.GONE);

        button_bottom = (Button) v.findViewById(R.id.button_bottom);
        button_bottom.setOnClickListener(this);
        button_bottom.setOnLongClickListener(this);
        button_bottom.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_BOTTOM))
            button_bottom.setVisibility(View.GONE);

        button_local_find = (Button) v.findViewById(R.id.button_local_find);
        button_local_find.setOnClickListener(this);
        button_local_find.setOnLongClickListener(this);
        button_local_find.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_LOCAL_FIND))
            button_local_find.setVisibility(View.GONE);

        button_local_replace = (Button) v.findViewById(R.id.button_local_replace);
        button_local_replace.setOnClickListener(this);
        button_local_replace.setOnLongClickListener(this);
        button_local_replace.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_LOCAL_REPLACE))
            button_local_replace.setVisibility(View.GONE);

        button_barcode = (Button) v.findViewById(R.id.button_barcode);
        button_barcode.setOnClickListener(this);
        button_barcode.setOnLongClickListener(this);
        button_barcode.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_BARCODE))
            button_barcode.setVisibility(View.GONE);

        button_image = (Button) v.findViewById(R.id.button_image);
        button_image.setOnClickListener(this);
        button_image.setOnLongClickListener(this);
        button_image.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_IMAGE))
            button_image.setVisibility(View.GONE);

        button_define = (Button) v.findViewById(R.id.button_define);
        button_define.setOnClickListener(this);
        button_define.setOnLongClickListener(this);
        button_define.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_DEFINE))
            button_define.setVisibility(View.GONE);

        button_calculate = (Button) v.findViewById(R.id.button_calculate);
        button_calculate.setOnClickListener(this);
        button_calculate.setOnLongClickListener(this);
        button_calculate.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_CALCULATE))
            button_calculate.setVisibility(View.GONE);

        button_web_search = (Button) v.findViewById(R.id.button_web_search);
        button_web_search.setOnClickListener(this);
        button_web_search.setOnLongClickListener(this);
        button_web_search.setTypeface(font_awesome);

        if (Arrays.asList(excluded).contains(Const.BUTTON_WEB_SEARCH))
            button_web_search.setVisibility(View.GONE);

        button_encrypt = (Button) v.findViewById(R.id.button_encrypt);
        button_encrypt.setOnClickListener(this);
        button_encrypt.setOnLongClickListener(this);
        button_encrypt.setTypeface(font_awesome);

        if ((Arrays.asList(excluded).contains(Const.BUTTON_ENCRYPT)) || (!Utils.hasPackage(getActivity(), Const.OKC_PACKAGE_NAME)))
            button_encrypt.setVisibility(View.GONE);

        button_decrypt = (Button) v.findViewById(R.id.button_decrypt);
        button_decrypt.setOnClickListener(this);
        button_decrypt.setOnLongClickListener(this);
        button_decrypt.setTypeface(font_awesome);

        if ((Arrays.asList(excluded).contains(Const.BUTTON_DECRYPT)) || (!Utils.hasPackage(getActivity(), Const.OKC_PACKAGE_NAME)))
            button_decrypt.setVisibility(View.GONE);

        button_close = (Button) v.findViewById(R.id.button_close);
        button_close.setOnClickListener(this);
        button_close.setOnLongClickListener(this);
        button_close.setTypeface(font_awesome);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            if (context instanceof Activity)
                mCallback = (OnEditToolFragmentSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnEditToolSelectedListener");
        }
    }

    @Override
    public void onClick(View view) {
        mCallback.onEditToolSelected(view.getId());
    }

    @Override
    public boolean onLongClick(View view) {
        mCallback.showHelp(view);
        return true;
    }
}
