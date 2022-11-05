package com.appmindlab.nano;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Objects;

/**
 * Created by saelim on 8/1/2015.
 */

public class LocalReplaceFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    OnLocalReplaceSelectedListener mCallback;
    protected boolean afterLongClick = false;

    // Container Activity must implement this interface
    public interface OnLocalReplaceSelectedListener {
        void onLocalReplaceSelected(int id);
        void doReplaceNext(boolean changeMode);
        void showHelp(View view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Button button_close, button_replace, button_next;
        AutoCompleteTextView edit_local_replace;

        Typeface font_awesome = FontCache.getFromAsset(getActivity(), "iconfonts.ttf");

        View v = inflater.inflate(R.layout.local_replace, container, false);

        edit_local_replace = (AutoCompleteTextView) v.findViewById(R.id.edit_local_replace);
        edit_local_replace.requestFocus();
        edit_local_replace.setImeOptions(EditorInfo.IME_ACTION_DONE|EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        edit_local_replace.setSingleLine(true);
        edit_local_replace.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mCallback.doReplaceNext(true);
                    return true;
                }
                return false;
            }
        });

        // Setup autocomlete
        String[] items = LocalReplaceHistory.getAllValues();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), R.layout.autocomplete_list_item, items);
        edit_local_replace.setAdapter(adapter);

        button_close  = (Button) v.findViewById(R.id.button_close);
        button_close.setOnClickListener(this);
        button_close.setOnLongClickListener(this);
        button_close.setTypeface(font_awesome);

        button_replace = (Button) v.findViewById(R.id.button_replace);
        button_replace.setOnClickListener(this);
        button_replace.setOnLongClickListener(this);
        button_replace.setTypeface(font_awesome);

        button_next = (Button) v.findViewById(R.id.button_next);
        button_next.setOnClickListener(this);
        button_next.setOnLongClickListener(this);
        button_next.setTypeface(font_awesome);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            if (context instanceof Activity)
            mCallback = (OnLocalReplaceSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnLocalReplaceSelectedListener");
        }
    }

    @Override
    public void onClick(View view) {
        mCallback.onLocalReplaceSelected(view.getId());
    }

    @Override
    public boolean onLongClick(View view) {
        mCallback.showHelp(view);
        return true;
    }
}

