package com.appmindlab.nano;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

/**
 * Created by saelim on 8/3/2015.
 */
public class DatePickerCalendarViewFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {
    private int mCalled = 1;  // A workaround to avoid being set twice

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog;

        if ((DisplayDBEntry.display_dbentry != null) && (DisplayDBEntry.display_dbentry.getAppTheme().equals(Const.DAY_THEME)))
            dialog = new DatePickerDialog(DisplayDBEntry.display_dbentry, R.style.DatePickerDialogThemeDay,this, year, month, day);
        else
            dialog = new DatePickerDialog(DisplayDBEntry.display_dbentry, R.style.DatePickerDialogThemeLux,this, year, month, day);

        // Move dialog to the front
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        Calendar cal = Calendar.getInstance();
        cal.set(year, Calendar.JANUARY + month, day);

        if ((mCalled % 2) != 0)
            Utils.insert(DisplayDBEntry.display_dbentry.getContent(), DisplayDBEntry.display_dbentry.getDateFormat().format(cal.getTime().getTime()));

        mCalled += 1;  // A workaround
    }
}
