package com.appmindlab.nano;

/**
 * Created by saelim on 7/31/2015.
 */

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

import androidx.annotation.Keep;
import androidx.fragment.app.Fragment;

import java.util.Locale;

@Keep
public class MarkdownSymbolFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener{
    OnMarkdownSymbolSelectedListener mCallback;

    // Container Activity must implement this interface
    public interface OnMarkdownSymbolSelectedListener {
        void onMarkdownSymbolSelected(int id);
        void showHelp(View view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getContext().getSharedPreferences(Const.PACKAGE + "_preferences", Context.MODE_PRIVATE);
        boolean lab_mode = sharedPreferences.getBoolean(Const.PREF_LAB_MODE, false);

        // Inflate the layout for this fragment
        Button button_indent, button_unindent, button_hash, button_asterisk, button_grave_accent, button_quotation;
        Button button_plus, button_dash, button_equal, button_vertical, button_backslash, button_slash, button_colon, button_semicolon;
        Button button_bracket_left, button_bracket_right;
        Button button_square_bracket_left, button_square_bracket_right;
        Button button_curly_bracket_left, button_curly_bracket_right;
        Button button_bracket_less, button_bracket_greater;
        Button button_underscore, button_dollar, button_bang, button_question;
        Button button_close, button_undo, button_text_expand;

        Typeface font = FontCache.getFromAsset(getActivity(), "RobotoMono-Regular.ttf");
        Typeface font_awesome = FontCache.getFromAsset(getActivity(), "iconfonts.ttf");

        View v;

        v = inflater.inflate(R.layout.markdown_symbol, container, false);

        button_indent = (Button) v.findViewById(R.id.button_indent);
        button_indent.setOnClickListener(this);
        button_indent.setOnLongClickListener(this);
        button_indent.setTypeface(font);

        button_unindent = (Button) v.findViewById(R.id.button_unindent);
        button_unindent.setOnClickListener(this);
        button_unindent.setOnLongClickListener(this);
        button_unindent.setTypeface(font);

        button_hash = (Button) v.findViewById(R.id.button_hash);
        button_hash.setOnClickListener(this);
        button_hash.setTypeface(font);

        button_asterisk = (Button) v.findViewById(R.id.button_asterisk);
        button_asterisk.setOnClickListener(this);
        button_asterisk.setTypeface(font);

        button_grave_accent = (Button) v.findViewById(R.id.button_grave_accent);
        button_grave_accent.setOnClickListener(this);
        button_grave_accent.setTypeface(font);

        button_quotation = (Button) v.findViewById(R.id.button_quotation);
        button_quotation.setOnClickListener(this);
        button_quotation.setTypeface(font);

        button_plus = (Button) v.findViewById(R.id.button_plus);
        button_plus.setOnClickListener(this);
        button_plus.setTypeface(font);

        button_dash = (Button) v.findViewById(R.id.button_dash);
        button_dash.setOnClickListener(this);
        button_dash.setTypeface(font);

        button_equal = (Button) v.findViewById(R.id.button_equal);
        button_equal.setOnClickListener(this);
        button_equal.setTypeface(font);

        button_backslash = (Button) v.findViewById(R.id.button_backslash);
        button_backslash.setOnClickListener(this);
        button_backslash.setTypeface(font);

        button_slash = (Button) v.findViewById(R.id.button_slash);
        button_slash.setOnClickListener(this);
        button_slash.setTypeface(font);

        button_vertical = (Button) v.findViewById(R.id.button_vertical);
        button_vertical.setOnClickListener(this);
        button_vertical.setTypeface(font);

        button_colon = (Button) v.findViewById(R.id.button_colon);
        button_colon.setOnClickListener(this);
        button_colon.setTypeface(font);

        button_semicolon = (Button) v.findViewById(R.id.button_semicolon);
        button_semicolon.setOnClickListener(this);
        button_semicolon.setTypeface(font);

        button_bracket_left = (Button) v.findViewById(R.id.button_bracket_left);
        button_bracket_left.setOnClickListener(this);
        button_bracket_left.setTypeface(font);

        button_bracket_right = (Button) v.findViewById(R.id.button_bracket_right);
        button_bracket_right.setOnClickListener(this);
        button_bracket_right.setTypeface(font);

        button_square_bracket_left = (Button) v.findViewById(R.id.button_square_bracket_left);
        button_square_bracket_left.setOnClickListener(this);
        button_square_bracket_left.setTypeface(font);

        button_square_bracket_right = (Button) v.findViewById(R.id.button_square_bracket_right);
        button_square_bracket_right.setOnClickListener(this);
        button_square_bracket_right.setTypeface(font);

        button_bracket_less = (Button) v.findViewById(R.id.button_bracket_less);
        button_bracket_less.setOnClickListener(this);
        button_bracket_less.setTypeface(font);

        button_bracket_greater = (Button) v.findViewById(R.id.button_bracket_greater);
        button_bracket_greater.setOnClickListener(this);
        button_bracket_greater.setTypeface(font);

        button_curly_bracket_left = (Button) v.findViewById(R.id.button_curly_bracket_left);
        button_curly_bracket_left.setOnClickListener(this);
        button_curly_bracket_left.setTypeface(font);

        button_curly_bracket_right = (Button) v.findViewById(R.id.button_curly_bracket_right);
        button_curly_bracket_right.setOnClickListener(this);
        button_curly_bracket_right.setTypeface(font);

        button_underscore = (Button) v.findViewById(R.id.button_underscore);
        button_underscore.setOnClickListener(this);
        button_underscore.setTypeface(font);

        button_dollar = (Button) v.findViewById(R.id.button_dollar);
        button_dollar.setOnClickListener(this);
        button_dollar.setTypeface(font);

        button_bang = (Button) v.findViewById(R.id.button_bang);
        button_bang.setOnClickListener(this);
        button_bang.setTypeface(font);

        button_question = (Button) v.findViewById(R.id.button_question);
        button_question.setOnClickListener(this);
        button_question.setTypeface(font);

        button_close = (Button) v.findViewById(R.id.button_close);
        button_close.setOnClickListener(this);
        button_close.setOnLongClickListener(this);
        button_close.setTypeface(font_awesome);

        // Quick access to common functions
        button_undo = (Button) v.findViewById(R.id.button_undo);
        button_undo.setOnClickListener(this);
        button_undo.setOnLongClickListener(this);
        button_undo.setTypeface(font_awesome);
        if (!lab_mode)
            button_undo.setVisibility(View.GONE);

        button_text_expand = (Button) v.findViewById(R.id.button_text_expand);
        button_text_expand.setOnClickListener(this);
        button_text_expand.setOnLongClickListener(this);
        button_text_expand.setTypeface(font_awesome);
        if (!lab_mode)
            button_text_expand.setVisibility(View.GONE);

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            if (context instanceof Activity)
                mCallback = (OnMarkdownSymbolSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnMarkdownSymbolsSelectedListener");
        }
    }

    @Override
    public void onClick(View view) {
        mCallback.onMarkdownSymbolSelected(view.getId());
    }

    @Override
    public boolean onLongClick(View view) {
        mCallback.showHelp(view);
        return true;
    }
}

