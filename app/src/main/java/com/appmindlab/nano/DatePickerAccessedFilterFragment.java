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
 * Created by saelim on 8/4/2015.
 */
public class DatePickerAccessedFilterFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year, month, day;

        c.add(Calendar.DAY_OF_MONTH, -1);

        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it
        DatePickerDialog dialog;

        if ((MainActivity.main_activity != null) && (MainActivity.main_activity.getAppTheme().equals(Const.DAY_THEME)))
            dialog = new DatePickerDialog(MainActivity.main_activity, R.style.DatePickerDialogThemeDay,this, year, month, day);
        else
            dialog = new DatePickerDialog(MainActivity.main_activity, R.style.DatePickerDialogThemeLux,this, year, month, day);

        // Move dialog to the front
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return dialog;
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        Calendar cal = Calendar.getInstance();
        cal.set(year, Calendar.JANUARY + month, day);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        if (MainActivity.main_activity != null)
            MainActivity.main_activity.doAccessedFilter(cal.getTime().getTime());
    }
}
