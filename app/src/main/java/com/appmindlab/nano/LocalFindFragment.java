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

public class LocalFindFragment extends Fragment implements View.OnClickListener, View.OnLongClickListener {
    OnLocalFindSelectedListener mCallback;
    protected boolean afterLongClick = false;

    // Container Activity must implement this interface
    public interface OnLocalFindSelectedListener {
        void onLocalFindSelected(int id);
        void doLocalFind();
        void showHelp(View view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Button button_close, button_search, button_clear;
        AutoCompleteTextView edit_local_find;

        Typeface font_awesome = FontCache.getFromAsset(getActivity(), "iconfonts.ttf");

        View v = inflater.inflate(R.layout.local_find, container, false);

        edit_local_find = (AutoCompleteTextView) v.findViewById(R.id.edit_local_find);
        edit_local_find.requestFocus();
        edit_local_find.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        edit_local_find.setSingleLine(true);
        edit_local_find.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    mCallback.doLocalFind();
                    return true;
                }
                return false;
            }
        });

        // Setup autocomlete
        String[] items = LocalFindHistory.getAllValues();
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(Objects.requireNonNull(getActivity()), R.layout.autocomplete_list_item, items);
        edit_local_find.setAdapter(adapter);

        button_close  = (Button) v.findViewById(R.id.button_close);
        button_close.setOnClickListener(this);
        button_close.setOnLongClickListener(this);
        button_close.setTypeface(font_awesome);

        button_search = (Button) v.findViewById(R.id.button_search);
        button_search.setOnClickListener(this);
        button_search.setOnLongClickListener(this);
        button_search.setTypeface(font_awesome);

        button_clear = (Button) v.findViewById(R.id.button_clear);
        button_clear.setOnClickListener(this);
        button_clear.setOnLongClickListener(this);
        button_clear.setTypeface(font_awesome);

        return v;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            if (context instanceof Activity)
                mCallback = (OnLocalFindSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnLocalFindSelectedListener");
        }
    }

    @Override
    public void onClick(View view) {
        mCallback.onLocalFindSelected(view.getId());
    }

    @Override
    public boolean onLongClick(View view) {
        mCallback.showHelp(view);
        return false;
    }
}
