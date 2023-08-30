package com.appmindlab.nano;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.KeyListener;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.javascriptengine.JavaScriptIsolate;
import androidx.javascriptengine.JavaScriptSandbox;

import com.google.android.material.snackbar.Snackbar;
import com.google.common.base.Joiner;
import com.google.common.util.concurrent.ListenableFuture;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.text.BreakIterator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

import static android.content.Context.CLIPBOARD_SERVICE;

/**
 * Created by saelim on 7/27/2015.
 */
public class Utils {
    // Package name
    protected static String mPackageName = "com.appmindlab.nano";

    // External storage
    protected static boolean mExternalStorageAvailable = false;
    protected static boolean mExternalStorageWriteable = false;

    // Synchronization
    protected static boolean mWriteLock = false;

    // UI
    protected static float mLightLevel = Const.LIGHT_LEVEL_THRESHOLD_DIRECT_SUNLIGHT;

    // JavaScript sandbox
    protected static ListenableFuture<JavaScriptSandbox> mJSSandbox;

    ///////////////////////
    // String Manipulation
    ///////////////////////

    // Determine if a string is numeric
    // Source: http://stackoverflow.com/a/1102916
    public static boolean isNumeric(String str) {
        NumberFormat formatter = NumberFormat.getInstance();
        ParsePosition pos = new ParsePosition(0);

        formatter.parse(str, pos);
        return str.length() == pos.getIndex();
    }

    // Determine if a delimited string contains only integers
    public static boolean isDelimIntList(String str, String delimiter) {
        String[] items = str.split(delimiter);

        for (int i = 0; i < items.length; i++)
            try {
                Integer.parseInt(items[i]);
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }

        return true;
    }

    // Convert url to https
    public static String toHTTPS(String url) {
        if (url.startsWith(Const.HTTPS_SYM))
            return url;

        if (url.startsWith("http://"))
            return Const.HTTPS_SYM + url.substring(Const.HTTP_SYM.length());
        else
            return Const.HTTPS_SYM + url;
    }

    // Verify https
    public static boolean isHTTPS(String url) {
        return url.startsWith(Const.HTTPS_SYM);
    }

    // Determine if a string contains decimal point
    public static boolean isDecimalNumber(String str) {
        return str.indexOf(".") != -1;
    }

    // Seek to next space
    protected static int seekToNextSpace(EditText text, int start) {
        Spannable temp = text.getText();
        final Pattern pattern = Pattern.compile("\\S+");
        final Matcher matcher = pattern.matcher(temp);

        int i, j = -1;

        while (matcher.find()) {
            i = matcher.start();
            j = matcher.end();
            if (i <= start && start <= j)
                break;
        }

        return j;
    }

    // Shorten timestamp
    protected static String shortenTimeStamp(Date date) {
        PrettyTime pretty_time = new PrettyTime(Locale.ENGLISH);
        String time_stamp = pretty_time.format(date);

        time_stamp = time_stamp.replace("minutes", "m").replace("hours", "h").replace("days", "d").replace("weeks", "w").replace("months", "mo").replace("years", "y");
        time_stamp = time_stamp.replace("minute", "m").replace("hour", "h").replace("day", "d").replace("week", "w").replace("month", "mo").replace("year", "y");

        if (time_stamp.contains("moments ago"))
            return "";

        return time_stamp.substring(0, Const.FASTSCROLL_TAB_LEN);
    }

    // Replace all occurrences of pattern with values
    protected static String replacePattern(String str, String pattern, Object[] values) {
        return String.format(Locale.getDefault(), str.replace("%", "%%").replace(pattern, "%s"), values);
    }

    // Replace string differences
    protected static synchronized void replaceDifference(EditText text, String replacement) {
        String original = text.getText().toString();
        int pos = org.apache.commons.lang3.StringUtils.indexOfDifference(original, replacement);

        // Skip if identical
        if (pos >= 0)
            text.getText().replace(pos, original.length(), replacement, pos, replacement.length());
    }

    // Replace string
    protected static synchronized void replaceString(EditText text, int start, int end, String replacement) {
        // Skip if identical
        if ((start >= 0) && (end >= 0) && (start < end))
            text.getText().replace(start, end, replacement);
    }

    // Remove zero width spaces
    protected static synchronized String removeZeroWidthSpaces(String src) {
        return src.replaceAll("[\\p{Cf}]", "");
    }

    // Replace selection
    protected static synchronized void replaceSelection(EditText text, String replacement) {
        int start = Math.min(text.getSelectionStart(), text.getSelectionEnd());
        int end = Math.max(text.getSelectionStart(), text.getSelectionEnd());

        if (start < end) {
            text.getText().replace(start, end, replacement);
        }
    }

    // Check to see if cursor is at word boundary
    protected static synchronized boolean isWordBoundary(EditText text) {
        int pos = text.getSelectionStart() - 1;

        if (pos >= 0)
            return ((text.getText().charAt(pos) == Const.SPACE_CHAR) || (text.getText().charAt(pos) == Const.NEWLINE_CHAR));

        return false;
    }

    // Clean all elements in a string array
    protected static String[] cleanStringArray(String[] src) {
        String[] target = new String[src.length];
        for (int i = 0; i < src.length; i++)
            target[i] = src[i].trim();

        return target;
    }

    // Encode and clean all elements in a string array
    protected static String[] encodeStringArray(String[] src) {
        String[] target = new String[src.length];
        for (int i = 0; i < src.length; i++)
            target[i] = Uri.encode(src[i].trim());

        return target;
    }

    // Count words
    protected static int countWords(final String phrase) {
        try {
            String temp;
            String[] items;

            temp = phrase.replaceAll("[^\\p{Alpha}]+", " ");
            temp = temp.trim();

            items = temp.split(" ");

            return items.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Add comma to thousands
    protected static String injectComma(long value) {
        try {
            return String.format("%,d", value);
        } catch (Exception e) {
            return Long.toString(value);
        }
    }

    // Check preset criteria
    protected static boolean isPreset(String criteria) {
        return (criteria.equals(Const.ALL_SYM) || criteria.equals(Const.NUM_SYM) || criteria.equals(Const.STARRED_SYM) || criteria.equals(Const.MODIFIED_AFTER_FILTER) || criteria.equals(Const.ACCESSED_AFTER_FILTER) || criteria.equals(Const.MODIFIED_NEARBY_FILTER) || (criteria.length() == 1 && Character.isLetter(criteria.charAt(0))));
    }

    // Clean up criteria
    protected static String cleanCriteria(String criteria) {
        if (criteria == null)
            return null;

        // Remove leading comma
        criteria = criteria.replaceAll("^,", "");

        // Remove trailing comma
        criteria = criteria.replaceAll(",$", "");

        if (criteria.startsWith(Const.ANDQUERY))
            criteria = criteria.substring(Const.ANDQUERY.length()).trim();

        else if (criteria.startsWith(Const.ANDGQUERY))
            criteria = criteria.substring(Const.ANDGQUERY.length()).trim();

        else if (criteria.startsWith(Const.ORQUERY))
            criteria = criteria.substring(Const.ORQUERY.length()).trim();

        else if (criteria.startsWith(Const.ORGQUERY))
            criteria = criteria.substring(Const.ORGQUERY.length()).trim();

        else if (criteria.startsWith(Const.JOINQUERY))
            criteria = criteria.substring(Const.JOINQUERY.length()).trim();

        else if (criteria.startsWith(Const.INQUERY))
            criteria = criteria.substring(Const.INQUERY.length()).trim();

        else if (criteria.startsWith(Const.SIMILARQUERY))
            return criteria.substring(Const.SIMILARQUERY.length()).trim();

        else if (criteria.startsWith(Const.RELATEDQUERY))
            return criteria.substring(Const.RELATEDQUERY.length()).trim();

        else if (criteria.startsWith(Const.TAGALLQUERY))
            return criteria.substring(Const.TAGALLQUERY.length()).trim();

        else if (criteria.startsWith(Const.TAGANYQUERY))
            return criteria.substring(Const.TAGANYQUERY.length()).trim();

        else if (criteria.startsWith(Const.TITLEONLY))
            return criteria.substring(Const.TITLEONLY.length()).trim();

        else if (criteria.startsWith(Const.TITLEREGONLY))
            return criteria.substring(Const.TITLEREGONLY.length()).trim();

        else if (criteria.startsWith(Const.METADATAONLY))
            return criteria.substring(Const.METADATAONLY.length()).trim();

        else if (criteria.startsWith(Const.METADATAREGONLY))
            return criteria.substring(Const.METADATAREGONLY.length()).trim();

        else
            return criteria.trim();

        String parts[] = criteria.split(",");
        for (int i = 0; i < parts.length; i++)
            parts[i] = parts[i].replace("?", "\\w").replace("*", "\\w*").trim();

        return Joiner.on(",").join(parts);
    }

    // Convert criteria into Regex syntax
    protected static String regexCriteria(String criteria) {
        String regex = cleanCriteria(criteria);

        if (regex.length() > 0)
            return regex.replace(",", "|");
        else
            return regex;
    }

    // Escape regex symbols
    protected static String escapeRegexSym(String str) {
        return str.replaceAll("[\\<\\(\\[\\{\\\\\\^\\-\\=\\$\\!\\|\\]\\}\\)‌​\\?\\*\\+\\.\\>]", "\\\\$0");
    }

    // Unescape regex symbols
    protected static String unescapeRegexSym(String str) {
        return str.replaceAll("\\\\", "");
    }

    // Evaluate built-in variables
    protected static String evalGlobalVariables(Context context, String str, String customDateFormat, String customTimeFormat, boolean escape) {
        if (str.contains(Const.TOMORROW_VAR)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, +1);

            str = str.replaceAll(Const.TOMORROW_VAR, Utils.getDateFormat(context, customDateFormat).format(cal.getTime()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.YESTERDAY_VAR)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -1);

            str = str.replaceAll(Const.YESTERDAY_VAR, Utils.getDateFormat(context, customDateFormat).format(cal.getTime()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.TODAY_VAR)) {
            str = str.replaceAll(Const.TODAY_VAR, Utils.getDateFormat(context, customDateFormat).format(new Date()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.NOW_VAR)) {
            str = str.replaceAll(Const.NOW_VAR, Utils.getTimeFormat(context, customTimeFormat).format(new Date()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.LAST_YEAR_VAR)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.YEAR, -1);

            str = str.replaceAll(Const.LAST_YEAR_VAR, Utils.getDateFormat(context, customDateFormat).format(cal.getTime()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.NEXT_YEAR_VAR)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.YEAR, +1);

            str = str.replaceAll(Const.NEXT_YEAR_VAR, Utils.getDateFormat(context, customDateFormat).format(cal.getTime()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.LAST_MONTH_VAR)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -1);

            str = str.replaceAll(Const.LAST_MONTH_VAR, Utils.getDateFormat(context, customDateFormat).format(cal.getTime()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.NEXT_MONTH_VAR)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, +1);

            str = str.replaceAll(Const.NEXT_MONTH_VAR, Utils.getDateFormat(context, customDateFormat).format(cal.getTime()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.LAST_WEEK_VAR)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, -7);

            str = str.replaceAll(Const.LAST_WEEK_VAR, Utils.getDateFormat(context, customDateFormat).format(cal.getTime()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.NEXT_WEEK_VAR)) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_YEAR, +7);

            str = str.replaceAll(Const.NEXT_WEEK_VAR, Utils.getDateFormat(context, customDateFormat).format(cal.getTime()));
            if (escape) str = escapeRegexSym(str);
        }

        if (str.contains(Const.CLIPBOARD_VAR)) {
            str = str.replaceAll(Const.CLIPBOARD_VAR, Utils.getClipboardText(context, (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE), -1, false));
            if (escape) str = escapeRegexSym(str);
        }

        return str;
    }

    // Multi-line indent
    protected static synchronized boolean indentMultiLine(EditText text, String indentChar, boolean forward) {
        try {
            String str = text.getText().toString(), selection;
            int start = Math.min(text.getSelectionStart(), text.getSelectionEnd());
            int end = Math.max(text.getSelectionStart(), text.getSelectionEnd());
            int indent_len = indentChar.length();

            if (start < end) {
                selection = Const.NEWLINE + str.substring(start, end);

                if (forward) {    // Indent
                    selection = selection.replaceAll(Const.NEWLINE, Const.NEWLINE + indentChar);
                    replaceSelection(text, selection.substring(1));

                    // Retain selection
                    end = start + selection.length() - 1;
                    start = start + indent_len;
                    text.setSelection(start, end);
                }
                else {            // Un-indent
                    // Sanity check
                    if ((start > 0) && (str.charAt(start-1) == Const.NEWLINE_CHAR))
                        return false;

                    if (str.charAt(start) == Const.SPACE_CHAR)
                        return false;

                    selection = selection.replaceAll(Const.NEWLINE + indentChar, Const.NEWLINE);
                    replaceSelection(text, selection.substring(1));

                    // Retain selection
                    end = start + selection.length() - 1;

                    // Also un-indent the first line
                    if ((!forward) && (start >= indent_len)) {
                        text.getText().replace(start - indent_len, start, "");
                        start = start - indent_len;
                        end = end - indent_len;
                    }

                    text.setSelection(start, end);
                }

                return true;
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Insert markdown symbol with auto indent
    protected static synchronized void insertMarkdownSymbolAutoIndent(EditText text, String symbol, String indentChar) {
        try {
            String str = text.getText().toString(), indented_symbol, indentation;
            int pos = Math.min(text.getSelectionStart(), text.getSelectionEnd());
            int indent_len = indentChar.length();
            int i, j, k, l;
            boolean skip = false;

            // Multi-line indent
            if (indentMultiLine(text, indentChar, true)) return;

            // Determine indentation and insert spaces
            if ((pos > 0) && (str.charAt(pos - 1) == Const.NEWLINE_CHAR)) {  // Make sure the previous character is newline

                if (!((pos > 2) && (str.charAt(pos - 2) == Const.NEWLINE_CHAR))) {  // Make sure not consecutive newlines

                    // Prefix the symbol with indentation
                    indented_symbol = indentChar + symbol;

                    // Find the last occurrence of the symbol
                    j = str.lastIndexOf(indented_symbol, pos);

                    // Check for blank line
                    if (j > 0) {
                        // Remove indentation prefix
                        j += indent_len;

                        // After last indentation
                        k = str.indexOf(Const.BLANK_LINE, j);

                        // Last symbol with or without indentation
                        l = str.lastIndexOf(symbol, pos);

                        // Before cursor
                        if (((k > 0) && (k < pos - 2)) || (j < l))
                            skip = true;
                    }

                    if ((j > 0) && (!skip)) {

                        // Find the newline right before the symbol
                        i = str.lastIndexOf(Const.NEWLINE, j);

                        // Get the supposed indentation
                        indentation = str.substring(i + 1, j);

                        if ((i >= 0) && (i < j) && (indentation.trim().length() == 0))
                            insertMarkdownSymbol(text, indentation);
                    }
                }
            }

            // Also handle first bullet
            insertMarkdownSymbol(text, symbol);
        } catch (Exception e) {
            // Insert markdown symbol as usual if exception
            insertMarkdownSymbol(text, symbol);
            e.printStackTrace();
        }
    }

    // Insert a markdown symbol
    protected static synchronized void insertMarkdownSymbol(EditText text, String symbol) {
        int start = Math.min(text.getSelectionStart(), text.getSelectionEnd());
        int end = Math.max(text.getSelectionStart(), text.getSelectionEnd());

        if ((start >= 0) && (end >= 0))
            text.getText().replace(start, end, symbol, 0, symbol.length());
    }

    // Multi-line insert markdown symbols
    protected static synchronized boolean insertMarkdownSymbolMultiLine(EditText text, String symbol) {
        try {
            String str = text.getText().toString(), selection;
            int start = Math.min(text.getSelectionStart(), text.getSelectionEnd());
            int end = Math.max(text.getSelectionStart(), text.getSelectionEnd());

            if (start < end) {
                selection = str.substring(start, end);
                if (selection.contains(Const.NEWLINE)) {
                    selection = Const.NEWLINE + selection;
                    selection = selection.replaceAll(Const.NEWLINE, Const.NEWLINE + symbol + Const.SPACE_CHAR);    // Indent
                    replaceSelection(text, selection.substring(1));
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Insert a pair of markdown symbols
    protected static synchronized void insertMarkdownSymbolPair(EditText text, String leftSymbol, String rightSymbol, String defaultSymbol, String indentChar) {
        int start = Math.min(text.getSelectionStart(), text.getSelectionEnd());
        int end = Math.max(text.getSelectionStart(), text.getSelectionEnd());

        if ((start >= 0) && (end >= 0)) {
            if (start == end) {
                if (indentChar != null)
                    insertMarkdownSymbolAutoIndent(text, defaultSymbol, indentChar);
                else
                    text.getText().insert(start, defaultSymbol);
            }
            else {
                text.getText().insert(start, leftSymbol);
                text.getText().insert(end + 1, rightSymbol);
            }
        }
    }

    // Insert a pair of markdown symbols or insert multi-line
    protected static synchronized void insertMarkdownSymbolPairOrMultiLine(EditText text, String leftSymbol, String rightSymbol, String defaultSymbol, String indentChar) {
        // Multi-line insert mode
        if (insertMarkdownSymbolMultiLine(text, defaultSymbol)) return;

        // Insert symbol pair
        insertMarkdownSymbolPair(text, leftSymbol, rightSymbol, defaultSymbol, indentChar);
    }

    // Fill blank spaces with markdown symbols
    protected static synchronized void fillMarkdownSymbol(EditText text, String symbol, String indentChar) {
        int start = Math.min(text.getSelectionStart(), text.getSelectionEnd());
        int end = Math.max(text.getSelectionStart(), text.getSelectionEnd());
        String source, target;

        if ((start >= 0) && (end >= 0)) {
            if (start == end)
                if (indentChar != null)
                    insertMarkdownSymbolAutoIndent(text, symbol, indentChar);
                else
                    text.getText().insert(start, symbol);
            else {
                source = text.getText().toString().substring(start, end);
                target = source.replaceAll(" ", symbol);
                text.getText().replace(start, end, target);
            }
        }
    }

    // Fill blank spaces with markdown symbols or insert multi-line
    protected static synchronized void fillMarkdownSymbolOrMultiLine(EditText text, String symbol, String indentChar) {
        // Multi-line insert mode
        if (insertMarkdownSymbolMultiLine(text, symbol)) return;

        // Fill symbol
        fillMarkdownSymbol(text, symbol, indentChar);
    }

    // Unindent
    protected static synchronized void unIndent(EditText text, String indentChar) {
        int start = Math.min(text.getSelectionStart(), text.getSelectionEnd());
        int end = Math.max(text.getSelectionStart(), text.getSelectionEnd());
        int indent_len = indentChar.length();
        String temp = text.getText().toString();

        // Multi-line unindent
        if (indentMultiLine(text, indentChar, false)) return;

        if ((end - indent_len) < 0)  // Not enough room to dedent
            return;

        if ((start >= 0) && (end >= 0))
            if (temp.substring(end - indent_len, end).equals(indentChar))
                text.getText().replace(end - indent_len, end, "");
    }

    // Call to insert a string
    protected static synchronized void insert(EditText text, String str) {
        if ((text != null) && (str != null)) {
            int start = Math.min(text.getSelectionStart(), text.getSelectionEnd());
            int end = Math.max(text.getSelectionStart(), text.getSelectionEnd());

            if ((start >= 0) && (end >= 0)) {
                text.getText().replace(start, end, str, 0, str.length());
                text.setSelection(start + str.length());
            }
        }
    }

    // Sort
    protected static synchronized String sort(String str) {
        List<String> lines;
        StringBuilder sb;

        if (str != null) {
            lines = Arrays.asList(str.split(Const.NEWLINE));

            // Sort
            Collections.sort(lines, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareToIgnoreCase(s2);
                }
            });

            // Reconstruct the string
            sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line);
                sb.append(Const.NEWLINE);
            }

            return sb.toString().trim();
        }

        return "";
    }

    // Reverse sort
    protected static synchronized String rsort(String str) {
        List<String> lines;
        StringBuilder sb;

        if (str != null) {
            lines = Arrays.asList(str.split(Const.NEWLINE));

            // Reverse sort
            Collections.reverse(lines);

            // Reconstruct the string
            sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line);
                sb.append(Const.NEWLINE);
            }

            return sb.toString().trim();
        }

        return "";
    }

    // Strip HTML
    protected static String stripHtml(String html) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        else
            return Html.fromHtml(html).toString();
    }

    // Evaluate JavaScript
    protected static void cliEvalJS(Context context, AppCompatActivity activity, View view, EditText text, String code, int timeout, boolean raw) {
        Thread t = new Thread() {
            public void run() {
                try {
                    if (mJSSandbox == null)
                        mJSSandbox = JavaScriptSandbox.createConnectedInstanceAsync(context);

                    JavaScriptIsolate isolate = mJSSandbox.get().createIsolate();
                    String str, snippet;

                    // Prepare template only if raw mode is false
                    if (raw)
                        snippet = code;
                    else
                        snippet = Const.CLI_EVAL_JS_TEMPLATE.replace(Const.PARAMETER_SYM, code);

                    ListenableFuture<String> result = isolate.evaluateJavaScriptAsync(snippet);
                    str = result.get(timeout, TimeUnit.SECONDS);

                    // Clean up
                    mJSSandbox.get().close();
                    mJSSandbox = null;

                    Snackbar snackbar = makePasteSnackbar(activity, view, text, Const.EQUAL_SYM + str);
                    anchorSnackbar(snackbar, R.id.fragment_content);
                    snackbar.show();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    // Extract head data
    protected static String extractHeadData(String html) {
        String temp = "";
        String[] items;
        int i, j;

        // Sanity check
        i = html.toLowerCase(Locale.getDefault()).indexOf("<head>");
        j = html.toLowerCase(Locale.getDefault()).indexOf("</head>");
        if ((i < 0) || (j < 0))
            return temp;

        items = html.split("(?i)<head>");
        if ((items.length == 2) && (i < j)) {
            items = items[1].split("(?i)</head>");
            if (items.length > 0)
                temp = items[0].trim().replace("\"", "'");
        }

        return temp;
    }

    // Load custom script
    protected static String loadCustomScript(String script) {
        if (!script.startsWith("<script"))
            script = "<script type='text/javascript'>" + script + "</script>";

        return script;
    }

    // Load custom styles
    protected static String loadCustomStyles(String styles) {
        if ((!styles.startsWith("<link")) && (!styles.startsWith("<style")) && (!styles.startsWith("<script")))
            styles = "<style>" + styles + "</style>";

        return styles;
    }

    // Clean file path
    protected static String cleanPath(String path) {
        path = path.trim();

        if (path.startsWith("//")) {
            path = path.replace("//", "/");
        }

        return path;
    }

    // Get parent path
    protected static String getParentPath(String path) {
        return path.substring(0, path.lastIndexOf("/"));
    }

    // Uri-decode a path's file name with prefix
    protected static String decodePathFileNameWithPrefix(String prefix, String path) {
        int pos = path.lastIndexOf(prefix) + 1;
        return path.substring(0, pos) + Uri.decode(path.substring(pos));
    }

    // Uri-decode a path's file name
    protected static String decodePathFileName(String path) {
        int pos = path.lastIndexOf('/') + 1;
        return path.substring(0, pos) + Uri.decode(path.substring(pos));
    }

    // Uri-encode a path's file name
    protected static String encodePathFileName(String path) {
        int pos = path.lastIndexOf('/') + 1;
        return path.substring(0, pos) + Uri.encode(path.substring(pos));
    }

    // Get revision summary
    protected static String getRevisionSummary(Context context, String original, String revised) {
        try {
            List<String> lines_original = Arrays.asList(original.split(Const.NEWLINE));
            List<String> lines_revised = Arrays.asList(revised.split(Const.NEWLINE));
            List<String> changes = new ArrayList<>();

            Patch patch = DiffUtils.diff(lines_original, lines_revised);
            List<Delta> deltas = patch.getDeltas();

            for (Delta delta : deltas) {
                if (delta.getType() == Delta.TYPE.INSERT) {
                    changes.add("<p class='inserted'>L" + delta.getRevised().getPosition() + ": " + TextUtils.join(Const.REVISION_DELIM, delta.getRevised().getLines()) + " <span class='inserted-sign'>+</span></p>");
                }
                else if (delta.getType() == Delta.TYPE.DELETE) {
                    changes.add("<p class='deleted'>L" + delta.getRevised().getPosition() + ": - - <span class='deleted-sign'>-</span></p>");
                }
                else if (delta.getType() == Delta.TYPE.CHANGE) {
                    changes.add("<p class='modified'>L" + delta.getRevised().getPosition() + ": " + TextUtils.join(Const.REVISION_DELIM, delta.getRevised().getLines()) + "<span class='modified-sign'></span></p>");
                }
            }

            return TextUtils.join(Const.NEWLINE, changes);
        }
        catch (Exception e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.status_no_change);
        }
    }

    // Get revision summary string
    protected static String getRevisionSummaryStr(Context context, String original, String revised) {
        try {
            List<String> lines_original = Arrays.asList(original.split(Const.NEWLINE));
            List<String> lines_revised = Arrays.asList(revised.split(Const.NEWLINE));
            List<String> changes = new ArrayList<>();

            Patch patch = DiffUtils.diff(lines_original, lines_revised);
            List<Delta> deltas = patch.getDeltas();

            for (Delta delta : deltas) {
                if (delta.getType() == Delta.TYPE.INSERT) {
                    changes.add(Const.REV_INSERT_SYM + delta.getRevised().getPosition() + ": " + TextUtils.join(Const.REVISION_DELIM, delta.getRevised().getLines()));
                }
                else if (delta.getType() == Delta.TYPE.DELETE) {
                    changes.add(Const.REV_DELETE_SYM + delta.getRevised().getPosition() + ": " + TextUtils.join(Const.REVISION_DELIM, delta.getRevised().getLines()));
                }
                else if (delta.getType() == Delta.TYPE.CHANGE) {
                    changes.add(Const.REV_DELTA_SYM + delta.getRevised().getPosition() + ": " + TextUtils.join(Const.REVISION_DELIM, delta.getRevised().getLines()));
                }
            }

            return TextUtils.join(Const.NEWLINE, changes);
        }
        catch (Exception e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.status_no_change);
        }
    }

    ///////////////////
    // System Settings
    ///////////////////
    // Get system date format
    protected static SimpleDateFormat getSystemDateFormat(Context context, Locale locale) {
        DateFormat date_format;

        date_format = android.text.format.DateFormat.getDateFormat(context);
        return new SimpleDateFormat(((SimpleDateFormat) date_format).toLocalizedPattern() + " (E) ", locale);
    }

    // Get system time format
    protected static SimpleDateFormat getSystemTimeFormat(Context context, Locale locale) {
        DateFormat time_format;

        time_format = android.text.format.DateFormat.getTimeFormat(context);
        return new SimpleDateFormat(((SimpleDateFormat) time_format).toLocalizedPattern(), locale);
    }

    // Return date format
    protected static SimpleDateFormat getDateFormat(Context context, String customDateFormat) {
        SimpleDateFormat date_format;

        if ((customDateFormat == null) || (customDateFormat.length() == 0))
            date_format = Utils.getSystemDateFormat(context, Locale.getDefault());
        else
            try {
                date_format = new SimpleDateFormat(customDateFormat);
            }
            catch (Exception e) {
                e.printStackTrace();
                date_format = Utils.getSystemDateFormat(context, Locale.getDefault());
            }

        return date_format;
    }

    // Return time format
    protected static SimpleDateFormat getTimeFormat(Context context, String customTimeFormat) {
        SimpleDateFormat time_format;

        if ((customTimeFormat == null) || (customTimeFormat.length() == 0))
            time_format = Utils.getSystemTimeFormat(context, Locale.getDefault());
        else
            try {
                time_format = new SimpleDateFormat(customTimeFormat);
            }
            catch (Exception e) {
                e.printStackTrace();
                time_format = Utils.getSystemTimeFormat(context, Locale.getDefault());
            }

        return time_format;
    }

    ////////
    // I/O
    ////////

    // Validate title
    protected static boolean validateTitle(String title) {
        // Sanity check
        if ((title == null) || (title.length() == 0))
            return false;

        for (int i = 0; i < Const.INVALID_TITLE_CHARS.length; i++)
            if (title.contains(Const.INVALID_TITLE_CHARS[i]))
                return false;

        return true;
    }

    // Validate file size
    protected static boolean validateFileSize(String content) {
        return (content.length() < (Const.MAX_FILE_SIZE));
    }

    // Get SD card state
    protected static void getSDState() {
        String state = Environment.getExternalStorageState();

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but all we need
            //  to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }
    }

    // Check whether external storage is writable
    protected static boolean isExternalStorageWritable() {
        return mExternalStorageWriteable;
    }

    // Check whether external storage is available
    protected static boolean isExternalStorageAvailable() {
        return mExternalStorageAvailable;
    }

    // Check whether to use file name as note title
    protected static boolean fileNameAsTitle(Context context) {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieve preference values
        String local_repo_path = shared_preferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
        File file = new File(local_repo_path + "/" + Const.MULTI_TYPE);

        // Log.d(Const.TAG, "nano - multi_type location: " + local_repo_path + "/" + Const.MULTI_TYPE + " exist: " + file.exists());

        return file.exists();
    }

    // Get note title template
    protected static String getNewNoteTitleTemplate(Context context) {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieve preference values
        String template = shared_preferences.getString(Const.PREF_NEW_NOTE_TITLE_TEMPLATE, Const.NEW_NOTE_TITLE_TEMPLATE);

        if (!template.contains(Const.NEW_NOTE_TITLE_COUNT_SYM))
            template = template + Const.SPACE_CHAR + Const.NEW_NOTE_TITLE_COUNT_SYM;

        return template;
    }

    // Make file name
    protected static String makeFileName(Context context, String path) {
        if (fileNameAsTitle(context))
            return path + ".txt";
        else
            return path;
    }

    // Make next file name
    protected static String makeNextFileName(Context context, String path) {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieve preference values
        String file_type = shared_preferences.getString(Const.PREF_NEW_NOTE_FILE_TYPE, Const.NEW_NOTE_FILE_TYPE);

        if (fileNameAsTitle(context))
            return path + file_type;    // File type template only works for multi-type mode
        else
            return path;
    }

    // Make deleted title
    protected static String makeDeletedTitle(String title) {
        SimpleDateFormat sdf = new SimpleDateFormat(Const.DIRPATH_DATE_FORMAT, Locale.getDefault());
        String prefix = sdf.format(new Date());

        return prefix + "." + title;
    }

    // Get title from file name
    protected static String getTitleFromFileName(Context context, File file) {
        if (Utils.fileNameAsTitle(context))
            return file.getName();
        else
            return Utils.stripExtension("txt", file.getName());
    }

    // Get title from document file name
    protected static String getTitleFromDocumentFileName(Context context, DocumentFile file) {
        if (Utils.fileNameAsTitle(context))
            return file.getName();
        else
            return Utils.stripExtension("txt", file.getName());
    }

    // Guess title from any file name
    protected static String guessTitleFromFileName(Context context, File file) {
        String file_name = file.getName();

        if (Utils.fileNameAsTitle(context))
            return file_name;
        else
            return file_name.substring(0, file_name.lastIndexOf('.'));
    }

    // Extract title from file name
    protected static String extractTitleFromFileName(Context context, String fileName) {
        if (Utils.fileNameAsTitle(context))
            return fileName.substring(0, fileName.lastIndexOf('.'));
        else
            return fileName;
    }

    // Get file name from title
    protected static String getFileNameFromTitle(Context context, String title) {
        if (Utils.fileNameAsTitle(context))
            return title;
        else
            return title + ".txt";
    }

    // Get the list of files from a directory
    protected static File[] getFileListFromDirectory(Context context, File dir, String path) {
        final String temp = path;

        if (Utils.fileNameAsTitle(context)) {
            return dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    File file = new File(temp + "/" + name);
                    return (!file.isDirectory()) && (!name.startsWith("."));
                }
            });
        }
        else {
            // .txt files only
            return dir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase(Locale.getDefault()).endsWith(".txt");
                }
            });
        }
    }

    // Get file count from a directory
    protected static int getFileCountFromDirectory(Context context, File dir, String path) {
        File[] files = getFileListFromDirectory(context, dir, path);

        if (files != null)
            return files.length;

        else
            return -1;
    }

    // Read file
    protected static String readFile(File file) {
        StringBuilder buf = new StringBuilder();
        BufferedReader reader;
        FileInputStream in;
        String line;

        // Sanity check
        if (file.length() > Const.MAX_FILE_SIZE)
            return "";

        try {
            in = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(in));

            while ((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append('\n');
            }

            reader.close();
            in.close();

        } catch (Exception e) {
            Log.i(Const.TAG, "readFile: failed");
            e.printStackTrace();
        }

        return buf.toString();
    }

    // Determine file existence
    protected static boolean fileExists(Context context, String path, String title) {
        File temp1, temp2, temp3, temp4;

        if ((Utils.fileNameAsTitle(context)) || (Arrays.asList(Const.UNCACHE_FILES).contains(title))) {
            temp1 = new File(path + "/" + title);
            temp2 = new File(path + "/" + title.toLowerCase(Locale.getDefault()));

            return ((temp1.exists()) || (temp2.exists()));
        }
        else {
            temp1 = new File(path + "/" + title + ".txt");
            temp2 = new File(path + "/" + title + ".TXT");
            temp3 = new File(path + "/" + title.toLowerCase(Locale.getDefault()) + ".txt");
            temp4 = new File(path + "/" + title.toLowerCase(Locale.getDefault()) + ".TXT");

            return ((temp1.exists()) || (temp2.exists()) || (temp3.exists()) || (temp4.exists()));
        }
    }

    // Delete a file
    protected static void deleteFile(Context context, String path, String title) {
        File temp1, temp2;
        String path_lower, path_upper;

        if (Utils.fileNameAsTitle(context)) {
            path = path + "/" + title;

            temp1 = new File(path);
            if (temp1.exists())
                temp1.delete();
        }
        else {
            path_lower = path + "/" + title + ".txt";
            path_upper = path + "/" + title + ".TXT";

            temp1 = new File(path_lower);
            temp2 = new File(path_upper);

            if (temp1.exists())
                temp1.delete();

            else if (temp2.exists())
                temp2.delete();
        }
    }

    // Write to local repo file
    protected static void writeLocalRepoFile(Context context, String title, String content) {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieve preference values
        String local_repo_path = shared_preferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
        boolean file_name_as_title = fileNameAsTitle(context);

        // Update the monitored path
        if (local_repo_path.length() > 0) {
            try {
                File temp;

                if (file_name_as_title)
                    temp = new File(local_repo_path + "/" + title);
                else
                    temp = new File(local_repo_path + "/" + title + ".txt");

                FileOutputStream file = new FileOutputStream(temp);
                file.write(content.getBytes());

                file.flush();
                file.close();
            } catch (Exception e) {
                Log.i(Const.TAG, "writeLocalRepoFile: failed");
                e.printStackTrace();
            }
        }
    }

    // Write to local repo file without adjusting title
    protected static void writeLocalRepoFileAndTitle(Context context, String title, String content) {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieve preference values
        String local_repo_path = shared_preferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
        boolean file_name_as_title = fileNameAsTitle(context);

        // Update the monitored path
        if (local_repo_path.length() > 0) {
            try {
                File temp;
                temp = new File(local_repo_path + "/" + title);

                FileOutputStream file = new FileOutputStream(temp);
                file.write(content.getBytes());

                file.flush();
                file.close();
            } catch (Exception e) {
                Log.i(Const.TAG, "writeLocalRepoFile: failed");
                e.printStackTrace();
            }
        }
    }

    // Write to local repo file
    protected static void writeLocalRepoFile(Context context, String path, String title, String content) {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieve preference values
        String local_repo_path = shared_preferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
        boolean file_name_as_title = fileNameAsTitle(context);

        // Update the monitored path
        if (local_repo_path.length() > 0) {
            try {
                // Create directory if it does not exist
                File dir = new File(local_repo_path + "/" + path);
                if (!dir.exists())
                    dir.mkdirs();

                File temp;

                if (file_name_as_title)
                    temp = new File(local_repo_path + "/" + path + "/" + title);
                else
                    temp = new File(local_repo_path + "/" + path + "/" + title + ".txt");

                FileOutputStream file = new FileOutputStream(temp);
                file.write(content.getBytes());

                file.flush();
                file.close();
            } catch (Exception e) {
                Log.i(Const.TAG, "writeLocalRepoFile: failed");
                e.printStackTrace();
            }
        }
    }

    // Append to local repo file
    protected static void appendLocalRepoFile(Context context, String title, String content, long maxSize) {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieve preference values
        String local_repo_path = shared_preferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
        boolean file_name_as_title = fileNameAsTitle(context);

        // Update the monitored path
        if (local_repo_path.length() > 0) {
            try {
                File temp;
                FileOutputStream file;

                if (file_name_as_title)
                    temp = new File(local_repo_path + "/" + title);
                else
                    temp = new File(local_repo_path + "/" + title + ".txt");

                // If max file size is imposed
                if ((maxSize > 0) && (temp.length() > maxSize))
                    file = new FileOutputStream(temp);    // Truncate oversized file
                else
                    file = new FileOutputStream(temp, true);    // Apend mode

                file.write(content.getBytes());

                file.flush();
                file.close();
            } catch (Exception e) {
                Log.i(Const.TAG, "writeLocalRepoFile: failed");
                e.printStackTrace();
            }
        }
    }

    // Append to local repo file
    protected static void appendLocalRepoFile(Context context, String path, String title, String content, long maxSize) {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieve preference values
        String local_repo_path = shared_preferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
        boolean file_name_as_title = fileNameAsTitle(context);

        // Update the monitored path
        if (local_repo_path.length() > 0) {
            try {
                // Create directory if it does not exist
                File dir = new File(local_repo_path + "/" + path);
                if (!dir.exists())
                    dir.mkdirs();

                File temp;
                FileOutputStream file;

                if (file_name_as_title)
                    temp = new File(local_repo_path + "/" + path + "/" + title);
                else
                    temp = new File(local_repo_path + "/" + path + "/" + title + ".txt");

                // If max file size is imposed
                if ((maxSize > 0) && (temp.length() > maxSize))
                    file = new FileOutputStream(temp);                  // Truncate oversized file
                else
                    file = new FileOutputStream(temp, true);    // Append mode

                file.write(content.getBytes());

                file.flush();
                file.close();
            } catch (Exception e) {
                Log.i(Const.TAG, "appendLocalRepoFile: failed");
                e.printStackTrace();
            }
        }
    }

    // Append to a log file
    protected static void appendLogFile(Context context, String fullPath, String title, String content, long maxSize) {
        if (fullPath.length() > 0) {
            try {
                // Create directory if it does not exist
                File dir = new File(fullPath);
                if (!dir.exists())
                    dir.mkdirs();

                File temp;
                FileOutputStream file;

                temp = new File(fullPath + "/" + title);

                // If max file size is imposed
                if ((maxSize > 0) && (temp.length() > maxSize)) {
                    SimpleDateFormat sdf = new SimpleDateFormat(Const.DIRPATH_DATE_FORMAT, Locale.getDefault());

                    // Retire current log file
                    temp.renameTo(new File(fullPath + "/" + sdf.format(new Date()) + "_" + title));

                    // Create a new blank log
                    temp = new File(fullPath + "/" + title);
                    file = new FileOutputStream(temp);
                }
                else
                    // Append mode
                    file = new FileOutputStream(temp, true);

                file.write(content.getBytes());

                file.flush();
                file.close();
            } catch (Exception e) {
                Log.i(Const.TAG, "appendLogFile: failed");
                e.printStackTrace();
            }
        }
    }

    // Append to sync log
    protected static void appendSyncLogFile(Context context, String path, String title, String msg, long maxSize, int maxSyncLogFileAge) {
        // Sanity check
        if (!fileExists(context, path, Const.SYNC_LOG_FILE)) {
            // Fall back to app path on removable storage
            path = getAppPathRemovableStorage(context);
            if (!fileExists(context, path, Const.SYNC_LOG_FILE))
                return;
        }

        // Create log folder if applicable
        File log_dir = new File(path, Const.LOG_PATH);
        if (!log_dir.exists())
            makeFolder(path + "/" + Const.LOG_PATH);

        if ((title != null) && (!isHiddenFile(title))) {    // Not a hidden file

            // Build log entry
            SimpleDateFormat sdf = new SimpleDateFormat(Const.DIRPATH_DATE_FORMAT, Locale.getDefault());
            String str = Const.NEWLINE + Const.REV_LINE_SYM;
            str = str + Const.NEWLINE + sdf.format(new Date()) + ", " + title;
            str = str + Const.NEWLINE + msg;

            // Append to log file
            appendLogFile(context, path + "/" + Const.LOG_PATH, Const.SYNC_HISTORY_FILE, str, maxSize);

            // Purge oldest log file
            purgeSyncLogs(context, path + "/" + Const.LOG_PATH, maxSyncLogFileAge);
        }
    }

    // Purge old sync logs
    protected static void purgeSyncLogs(Context context, String path, int maxSyncLogFileAge) {
        File dir = new File(path);

        // Sanity check
        if (dir.isDirectory())
            return;

        // Get a list of files
        File[] files = dir.listFiles();

        // Sort by modified date (descending)
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
            }
        });

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1 * maxSyncLogFileAge);

        for (File file : files) {
            if (!file.isDirectory()) {
                if (cal.getTime().after(new Date(file.lastModified())))
                    file.delete();
            }
        }
    }

    // Write to local repo file
    protected static void deleteLocalRepoFile(Context context, String title) {
        final SharedPreferences shared_preferences = PreferenceManager.getDefaultSharedPreferences(context);

        // Retrieve preference values
        String local_repo_path = shared_preferences.getString(Const.PREF_LOCAL_REPO_PATH, "");
        boolean file_name_as_title = fileNameAsTitle(context);

        // Update the monitored path
        if (local_repo_path.length() > 0) {
            try {
                File temp;

                if (file_name_as_title)
                    temp = new File(local_repo_path + "/" + title);
                else
                    temp = new File(local_repo_path + "/" + title + ".txt");

                temp.delete();
            } catch (Exception e) {
                Log.i(Const.TAG, "writeLocalRepoFile: failed");
                e.printStackTrace();
            }
        }
    }

    // Move file
    protected static boolean moveFile(Context context, String srcPath, String srcFile, String destPath) {
        if (copyFile(context, srcPath, srcFile, destPath)) {
            deleteFile(context, srcPath, srcFile);
            return true;
        }

        return false;
    }

    // Copy file
    protected static boolean copyFile(Context context, String srcPath, String srcFile, String destPath) {
        try {
            String src_file = srcPath + srcFile;
            String dest_file = destPath + srcFile;

            // Create output directory if it does not exist
            File dir = new File(destPath);
            if (!dir.exists())
                dir.mkdirs();

            // Sanity check
            File file = new File(src_file);
            if (!file.exists())
                return true;

            // Sanity check, no need to copy if exist for faster return
            file = new File(dest_file);
            if (file.exists())
                return true;

            InputStream in = new BufferedInputStream(new FileInputStream(src_file));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dest_file));

            byte[] buffer = new byte[Const.BUFFER_SIZE];
            int count;

            while ((count = in.read(buffer)) != -1)
                out.write(buffer, 0, count);

            in.close();

            // Write the output file
            out.flush();
            out.close();

            return true;

        } catch (FileNotFoundException e) {
            Log.e(Const.TAG, e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(Const.TAG, e.getMessage());
            return false;
        }
    }

    // Copy file (full path provided)
    protected static boolean copyFile(Context context, String src, String dest) {
        try {
            // Sanity check
            File file = new File(src);
            if (!file.exists())
                return true;

            InputStream in = new BufferedInputStream(new FileInputStream(src));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));

            byte[] buffer = new byte[Const.BUFFER_SIZE];
            int count;

            while ((count = in.read(buffer)) != -1)
                out.write(buffer, 0, count);

            in.close();

            // Write the output file
            out.flush();
            out.close();

            return true;

        } catch (FileNotFoundException e) {
            Log.e(Const.TAG, e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(Const.TAG, e.getMessage());
            return false;
        }
    }

    // Write stream to file
    protected static boolean writeFile(Context context, InputStream input, String destPath, String destFile) {
        try {
            String dest_file = destPath + destFile;

            // Create output directory if it does not exist
            File dir = new File(destPath);
            if (!dir.exists())
                dir.mkdirs();

            // Sanity check, no need to copy if exist for faster return
            File file = new File(dest_file);
            if (file.exists())
                return true;

            InputStream in = new BufferedInputStream(input);
            OutputStream out = new BufferedOutputStream(new FileOutputStream(dest_file));

            byte[] buffer = new byte[Const.BUFFER_SIZE];
            int count;

            while ((count = in.read(buffer)) != -1)
                out.write(buffer, 0, count);

            in.close();

            // Write the output file
            out.flush();
            out.close();

            return true;

        } catch (FileNotFoundException e) {
            Log.e(Const.TAG, e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(Const.TAG, e.getMessage());
            return false;
        }
    }

    // Determine mimetype
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);

        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }

        return type;
    }

    // Create SAF file
    protected static void createSAFFile(Context context, DocumentFile dir, String title, String content) {
        try {
            String file_name = Utils.getFileNameFromTitle(context, title);
            DocumentFile file = dir.createFile(getMimeType(file_name), file_name);

            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(file.getUri(), "wt");

            // Write to the file
            FileOutputStream out = new FileOutputStream(fd.getFileDescriptor());
            out.write(content.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.i(Const.TAG, "writeSAFFile: failed");
            e.printStackTrace();
        }
    }

    // Write SAF file
    protected static void writeSAFFile(Context context, DocumentFile dir, String title, String content) {
        try {
            String file_name = Utils.getFileNameFromTitle(context, title);
            DocumentFile file = dir.findFile(file_name);

            // Create a new file
            if (file == null) {
                file = dir.createFile(getMimeType(file_name), file_name);
            }

            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(file.getUri(), "wt");

            // Write to the file
            FileOutputStream out = new FileOutputStream(fd.getFileDescriptor());
            out.write(content.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.i(Const.TAG, "writeSAFFile: failed");
            e.printStackTrace();
        }
    }

    // Write SAF file
    protected static void writeSAFFile(Context context, Uri uri, String content) {
        try {
            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(uri, "wt");

            // Write to the file
            FileOutputStream out = new FileOutputStream(fd.getFileDescriptor());
            out.write(content.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.i(Const.TAG, "writeSAFFile: failed");
            e.printStackTrace();
        }
    }

    // Write SAF file only if outdated
    protected static void writeSAFFile(Context context, DocumentFile dir, String title, String content, Date lastModified) {
        try {
            String file_name = getFileNameFromTitle(context, title);
            DocumentFile file = dir.findFile(file_name);
            Date src_lastmodified, dest_lastmodified;
            boolean is_new = false;

            // Create a new file
            if (file == null) {
                file = dir.createFile(getMimeType(file_name), file_name);
                is_new = true;
            }

            Log.d(Const.TAG, "nano - writeSAFFile: checking " + file_name + " ...");

            // Sanity check
            if (!is_new) {
                src_lastmodified = new Date(lastModified.getTime());
                dest_lastmodified = new Date(file.lastModified());

                Log.d(Const.TAG, "nano - writeSAFFile: checking " + title + " ... src_lastmodified: " + src_lastmodified + ", dest_lastmodified: " + dest_lastmodified);

                if (src_lastmodified.equals(dest_lastmodified) || (src_lastmodified.before(dest_lastmodified)))
                    return;
            }

            Log.d(Const.TAG, "nano - writeSAFFile: writing " + title + "...");

            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(file.getUri(), "wt");

            // Write to the file
            FileOutputStream out = new FileOutputStream(fd.getFileDescriptor());

            out.write(content.getBytes());
            out.flush();
            out.close();

            Log.d(Const.TAG, "nano - writeSAFFile: " + file_name + " processed.");
        } catch (Exception e) {
            Log.i(Const.TAG, "nano - writeSAFFile: failed");
            e.printStackTrace();
        }
    }

    // Write SAF file without extension
    protected static void writeSAFFileNoExtension(Context context, DocumentFile dir, String title, String content) {
        try {
            DocumentFile file = dir.findFile(title);

            // Create a new file
            if (file == null) {
                file = dir.createFile(null, title);
            }

            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(file.getUri(), "wt");

            // Write to the file
            FileOutputStream out = new FileOutputStream(fd.getFileDescriptor());
            out.write(content.getBytes());
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.i(Const.TAG, "writeSAFFileNoExtension: failed");
            e.printStackTrace();
        }
    }

    // Write special file to SAF
    protected static void writeSpecialSAFFile(Context context, Uri parentUri, String path, String title, String content) {
        try {
            // Also export to SAF
            DocumentFile parent_dir = DocumentFile.fromTreeUri(context, parentUri);
            DocumentFile child_dir = parent_dir.findFile(path);

            if (child_dir == null) {
                child_dir = parent_dir.createDirectory(path);
            }

            writeSAFFileNoExtension(context, child_dir, title, content);
        } catch (Exception e) {
            Log.i(Const.TAG, "writeSpecialSAFFile: failed");
            e.printStackTrace();
        }
    }

    // Copy folder
    protected static String copyFolder(Context context, File src, File dest, boolean overwrite) {
        String status = "";

        try {
            // Nothing to copy
            if (!src.exists())
                return "";

            if (src.isDirectory()) {
                if (!dest.exists() && !dest.mkdirs())
                    return dest.getAbsolutePath() + ": " + context.getResources().getString(R.string.error_invalid_local_storage_path);

                String[] items = src.list();
                for (int i = 0; i < items.length; i++)
                    status += copyFolder(context, new File(src, items[i]), new File(dest, items[i]), overwrite);
            } else {
                // Setup destination folder
                File dir = dest.getParentFile();
                if (dir != null && !dir.exists() && !dir.mkdirs())
                    return dir.getAbsolutePath() + ": " + context.getResources().getString(R.string.error_invalid_local_storage_path);

                if (!overwrite) {
                    // Skip if the version on disk is newer
                    if (src.lastModified() < dest.lastModified())
                        return "";
                }

                InputStream in = new BufferedInputStream(new FileInputStream(src));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));

                // Perform copying
                byte[] buf = new byte[Const.BUFFER_SIZE];
                int len;

                while ((len = in.read(buf)) > 0)
                    out.write(buf, 0, len);

                in.close();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        } catch (Exception e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        }

        return status;
    }

    // Make folder
    protected static boolean makeFolder(String path) {
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdirs();

        return true;
    }

    // Get SAF sub directory
    protected static DocumentFile getSAFSubDir(Context context, DocumentFile parentDir, String path) {
        try {
            DocumentFile childDir = parentDir.findFile(path);

            if (childDir == null) {
                childDir = parentDir.createDirectory(path);
            }

            if (!childDir.isDirectory())
                return null;

            return childDir;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Check SAF sub directory
    protected static boolean hasSAFSubDir(Context context, Uri uri, String path) {
        try {
            DocumentFile parent_dir = DocumentFile.fromTreeUri(context, uri);
            return parent_dir.findFile(path) != null;
        }
        catch (Exception e) {
            return false;
        }
    }

    // Get SAF sub directory uri
    protected static Uri getSAFSubDirUri(Context context, Uri uri, String path) {
        DocumentFile parent_dir = DocumentFile.fromTreeUri(context, uri);
        DocumentFile child_dir = parent_dir.findFile(path);

        if (child_dir != null)
            return child_dir.getUri();

        return null;
    }

    // Delete SAF file
    protected static boolean deleteSAFSubDirFile(Context context, Uri uri, String path, String title) {
        try {
            DocumentFile parent_dir = DocumentFile.fromTreeUri(context, uri);
            DocumentFile child_dir = parent_dir.findFile(path);

            DocumentFile file;
            String title_upper, title_lower;

            if (child_dir != null) {
                if (Utils.fileNameAsTitle(context)) {
                    file = child_dir.findFile(title);
                    if (file != null) {
                        file.delete();
                        return true;
                    }
                } else {
                    title_lower = title + ".txt";
                    title_upper = title + ".TXT";

                    file = child_dir.findFile(title_lower);
                    if (file != null) {
                        file.delete();
                        return true;
                    } else {
                        file = child_dir.findFile(title_upper);
                        if (file != null) {
                            file.delete();
                            return true;
                        }
                    }
                }
            }

            return false;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Strip file extension
    protected static String stripExtension(String extension, String fileName) {
        String extension_upper = extension.toUpperCase(Locale.getDefault());

        extension = "." + extension;

        if (fileName.endsWith(extension)) {
            return fileName.substring(0, fileName.length() - extension.length());
        } else if (fileName.endsWith(extension_upper)) {
            return fileName.substring(0, fileName.length() - extension_upper.length());
        }

        return "";  // Ignore files with invalid extensions
    }

    // Get directory from path
    protected static String getDirFromPath(String path) {
        String dir = null;

        if (path.contains("/"))
            dir = path.substring(0, path.lastIndexOf("/") + 1);

        return dir;
    }

    // Get file name from path
    protected static String getFileNameFromPath(String path) {
        String file_name = null;

        if (path.contains("/"))
            file_name = path.substring(path.lastIndexOf("/") + 1);

        return file_name;
    }

    // Determine if the title is an attachment
    protected static boolean isAttachment(String fileName) {
        return fileName.startsWith(Const.ATTACHMENT_PATH + "/");
    }

    // Determine if the title is that of a hidden file
    protected static boolean isHiddenFile(String fileName) {
        return fileName.startsWith(Const.HIDE_PATTERN_PREFIX);
    }

    // Expand file path
    protected static String expandFullPath(String path, String fileName) {
        if (isAttachment(fileName))
            return path + "/" + fileName;

        return fileName;
    }

    // Initialize app path
    protected static void initAppPath(Context context) {
        try {
            String state = Environment.getExternalStorageState();

            if (Environment.MEDIA_MOUNTED.equals(state)) {
                // Implicitly create app specific folder if not yet exist
                context.getExternalFilesDir(null);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get app path from removable storage
    protected static String getAppPathRemovableStorage(Context context) {
        try {
            File dir;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                dir = context.getExternalFilesDir(null);
            else {
                File[] dirs = ContextCompat.getExternalFilesDirs(context, null);
                dir = dirs[dirs.length - 1];
            }

            if (dir == null || !dir.exists() || !dir.canRead() || !dir.canWrite()) {
                return null;
            }

            return dir.getAbsolutePath();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Convert file size
    protected static String readableFileSize(long size) {
        if (size <= 0)
            return "0 Byte";

        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};

        int digit = (int) (Math.log10(size) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digit)) + " " + units[digit];
    }

    // Delete non empty folder recursively
    protected static void deleteDirectories(File fileOrDir) {
        if (fileOrDir.isDirectory())
            for (File child : fileOrDir.listFiles())
                deleteDirectories(child);

        fileOrDir.delete();
    }

    // Read from SAF file
    protected static String readFromSAFFile(Context context, DocumentFile src) {
        try {
            InputStream in = context.getContentResolver().openInputStream(src.getUri());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(Const.NEWLINE);
            }

            in.close();

            return sb.toString();
        }
        catch (Exception e) {
            Log.e(Const.TAG, e.getMessage());
            return Const.NULL_SYM;
        }
    }

    // Export to SAF file
    protected static boolean exportToSAFFile(Context context, String srcPath, String srcFile, DocumentFile destDir) {
        try {
            // Input file path
            String src_file = srcPath + srcFile;

            // Sanity check
            if (!destDir.exists()) return false;

            // Get the file
            DocumentFile file = destDir.findFile(srcFile);
            if (file == null) {
                file = destDir.createFile("application/octet-stream", srcFile);
            }

            ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(file.getUri(), "wt");

            // Write to the file
            InputStream in = new BufferedInputStream(new FileInputStream(src_file));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(fd.getFileDescriptor()));

            byte[] buffer = new byte[Const.BUFFER_SIZE];
            int count;

            while ((count = in.read(buffer)) != -1)
                out.write(buffer, 0, count);

            in.close();

            // Write the output file
            out.flush();
            out.close();

            return true;

        } catch (FileNotFoundException e) {
            Log.e(Const.TAG, e.getMessage());
            return false;
        } catch (Exception e) {
            Log.e(Const.TAG, e.getMessage());
            return false;
        }
    }

    // Import from SAF folder
    protected static String importFromSAFFolder(Context context, DocumentFile srcDir, String destPath, boolean overwrite) {
        String status = "";

        try {
            // Nothing to copy
            if (!srcDir.exists()) {
                Log.d(Const.TAG, "nano - importFromSAFFolder: srcDir does not exist");
                return status;
            }

            // Prepare destination folder
            File destDir = new File(destPath);
            if (!destDir.isDirectory()) {
                destDir.mkdir();

                // Sanity check
                if (!destDir.exists()) {
                    Log.d(Const.TAG, "nano - importFromSAFFolder: destPath does not exist");
                    return status;
                }
            }

            if (srcDir.isDirectory()) {
                for (DocumentFile file : srcDir.listFiles()) {
                    // Setup source
                    ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(file.getUri(), "r");

                    // Setup destination
                    File dest = new File(destPath + "/" + file.getName());
                    if ((!overwrite) && (dest.exists()))
                        continue;

                    InputStream in = new BufferedInputStream(new FileInputStream(fd.getFileDescriptor()));
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(dest));

                    byte[] buffer = new byte[Const.BUFFER_SIZE];
                    int count;

                    while ((count = in.read(buffer)) != -1)
                        out.write(buffer, 0, count);

                    in.close();

                    // Write the output file
                    out.flush();
                    out.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        } catch (Exception e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        }

        return status;
    }

    // Move to SAF folder
    protected static void moveToSAFFolder(Context context, Uri parentUri, File src, DocumentFile destDir, boolean deleteSrc, boolean deleteDest, boolean overwrite) {
        DocumentFile parent_dir = DocumentFile.fromTreeUri(context, parentUri);
        String path = destDir.getName();

        try {
            // Sanity check
            if ((parentUri == null) || (!parent_dir.exists()) || (!src.exists()))
                return;

            // 1. Remove SAF folder
            if ((deleteDest) && (destDir.exists()))
                destDir.delete();

            // 2. Create a new SAF folder
            destDir = getSAFSubDir(context, parent_dir, path);

            // 3. Export to destination folder
            exportToSAFFolder(context, src, destDir, overwrite);

            // 4. Delete source folder
            if (deleteSrc)
                src.delete();
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    // Dump to SAF folder
    protected static String dumpToSAFFolder(Context context, File srcDir, DocumentFile destDir) {
        String status = "";
        File src;
        DocumentFile dest;

        try {
            // Sanity check
            if ((!srcDir.exists()) || (!destDir.exists()))
                return status;

            if (srcDir.isDirectory()) {
                File[] files = srcDir.listFiles();

                for (int i = 0; i < files.length; i++) {
                    dest = destDir.createFile("application/octet-stream", files[i].getName());

                    // Source file
                    src = files[i];

                    ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(dest.getUri(), "wt");

                    // Write to the file
                    InputStream in = new BufferedInputStream(new FileInputStream(src));
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(fd.getFileDescriptor()));

                    byte[] buffer = new byte[Const.BUFFER_SIZE];
                    int count;

                    while ((count = in.read(buffer)) != -1)
                        out.write(buffer, 0, count);

                    in.close();

                    // Write the output file
                    out.flush();
                    out.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        } catch (Exception e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        }

        return status;
    }

    // Export to SAF folder
    protected static String exportToSAFFolder(Context context, File srcDir, DocumentFile destDir, boolean overwrite) {
        String status = "";
        File src;
        DocumentFile dest;
        long src_lastmodified, dest_lastmodified;
        boolean is_new = false;

        try {
            // Sanity check
            if ((!srcDir.exists()) || (!destDir.exists()))
                return status;

            if (srcDir.isDirectory()) {
                File[] files = srcDir.listFiles();

                // Sorted files based on last modified dates
                if (files != null && files.length > 1) {
                    Arrays.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File object1, File object2) {
                            return (int) ((object1.lastModified() > object2.lastModified()) ? object1.lastModified(): object2.lastModified());
                        }
                    });
                }

                for (int i = 0; i < files.length; i++) {
                    dest = destDir.findFile(files[i].getName());
                    if (dest == null) {
                        // Create a new file
                        dest = destDir.createFile("application/octet-stream", files[i].getName());
                        is_new = true;
                    }
                    else if (!overwrite)
                        continue;

                    // Source file
                    src = files[i];

                    // Skip file if no new changes
                    if (!is_new) {
                        src_lastmodified = src.lastModified();
                        dest_lastmodified = dest.lastModified();
                        if (src_lastmodified < dest_lastmodified)
                            continue;
                    }

                    ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(dest.getUri(), "wt");

                    // Write to the file
                    InputStream in = new BufferedInputStream(new FileInputStream(src));
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(fd.getFileDescriptor()));

                    byte[] buffer = new byte[Const.BUFFER_SIZE];
                    int count;

                    while ((count = in.read(buffer)) != -1)
                        out.write(buffer, 0, count);

                    in.close();

                    // Write the output file
                    out.flush();
                    out.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        } catch (Exception e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        }

        return status;
    }

    // Export to SAF folder by last modified time
    protected static String exportToSAFFolderByLastModified(Context context, File srcDir, DocumentFile destDir, long lastModified, boolean overwrite) {
        String status = "";
        File src;
        DocumentFile dest;
        long src_lastmodified, dest_lastmodified;
        boolean is_new = false;

        try {
            // Sanity check
            if ((!srcDir.exists()) || (!destDir.exists()))
                return status;

            if (srcDir.isDirectory()) {
                File[] files = srcDir.listFiles();

                // Sorted files based on last modified dates
                if (files != null && files.length > 1) {
                    Arrays.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File object1, File object2) {
                            return (int) ((object1.lastModified() > object2.lastModified()) ? object1.lastModified(): object2.lastModified());
                        }
                    });
                }

                for (int i = 0; i < files.length; i++) {
                    Log.d(Const.TAG, "nano - exportToSAFFolderByLastModified: handling " + files[i].getName() + " [ file last modified: " + files[i].lastModified() + ", threshold last modified: " + lastModified + " ]");
                    // Skip if last modified time is earlier
                    if (files[i].lastModified() < lastModified) continue;

                    Log.d(Const.TAG, "nano - exportToSAFFolderByLastModified: processing " + files[i].getName());

                    dest = destDir.findFile(files[i].getName());
                    if (dest == null) {
                        // Create a new file
                        dest = destDir.createFile("application/octet-stream", files[i].getName());
                        is_new = true;
                    }
                    else if (!overwrite)
                        continue;

                    // Source file
                    src = files[i];

                    // Skip file if no new changes
                    if (!is_new) {
                        src_lastmodified = src.lastModified();
                        dest_lastmodified = dest.lastModified();
                        if (src_lastmodified < dest_lastmodified)
                            continue;
                    }

                    ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(dest.getUri(), "wt");

                    // Write to the file
                    InputStream in = new BufferedInputStream(new FileInputStream(src));
                    OutputStream out = new BufferedOutputStream(new FileOutputStream(fd.getFileDescriptor()));

                    byte[] buffer = new byte[Const.BUFFER_SIZE];
                    int count;

                    while ((count = in.read(buffer)) != -1)
                        out.write(buffer, 0, count);

                    in.close();

                    // Write the output file
                    out.flush();
                    out.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        } catch (Exception e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        }

        return status;
    }

    // Export to SAF folder
    protected static String exportToSAFNestedFolder(Context context, File src, DocumentFile destDir) {
        String status = "";
        DocumentFile file;
        long src_lastmodified, file_lastmodified;
        boolean is_new = false;

        try {
            // Sanity check
            if ((!src.exists()) || (!destDir.exists()))
                return status;

            if (src.isDirectory()) {
                String[] items = src.list();
                for (int i = 0; i < items.length; i++)
                    status += exportToSAFNestedFolder(context, new File(src, items[i]), destDir);
            } else {
                file = destDir.findFile(src.getName());
                if (file == null) {
                    // Create a new file
                    file = destDir.createFile("application/octet-stream", src.getName());
                    is_new = true;
                }

                // Sanity check
                if (!is_new) {
                    src_lastmodified = src.lastModified();
                    file_lastmodified = file.lastModified();
                    if (src_lastmodified < file_lastmodified)
                        return status;
                }

                ParcelFileDescriptor fd = context.getContentResolver().openFileDescriptor(file.getUri(), "wt");

                // Write to the file
                InputStream in = new BufferedInputStream(new FileInputStream(src));
                OutputStream out = new BufferedOutputStream(new FileOutputStream(fd.getFileDescriptor()));

                byte[] buffer = new byte[Const.BUFFER_SIZE];
                int count;

                while ((count = in.read(buffer)) != -1)
                    out.write(buffer, 0, count);

                in.close();

                // Write the output file
                out.flush();
                out.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        } catch (Exception e) {
            e.printStackTrace();
            return context.getResources().getString(R.string.error_unexpected);
        }

        return status;
    }

    // Get clipboard text
    protected static String getClipboardText(Context context, ClipboardManager clipboard, int len, boolean raw) {
        try {
            if (clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                ClipData.Item item = clip.getItemAt(0);
                String str = item.coerceToText(context).toString();

                // Remove spaces
                if (!raw)
                    str = str.replaceAll("\\r\\n|\\r|\\n", " ");

                // Truncate
                if ((len > 0) && (str.length() > len))
                    str = str.substring(0, len) + Const.ELLIPSIS_SYM;

                return str;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    // Set clipboard text
    protected static void setClipboardText(Context context, ClipboardManager clipboard, String label, String str) {
        try {
            ClipData clip = ClipData.newPlainText(label, str);
            clipboard.setPrimaryClip(clip);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Make clipboard status
    protected static String makeClipboardStatus(Context context, ClipboardManager clipboard, int len, boolean raw) {
        String str;

        str = getClipboardText(context, clipboard, len, raw).trim();
        if (str.length() > 0)
            str = Const.LEFT_QUOTE_SYM + str + Const.RIGHT_QUOTE_SYM;

        return str;
    }

    // Create a note link
    protected static String createNoteLink(String title, String criteria) {
        String link = "";

        if ((title != null) && (title.length() > 0)) {
            link = Const.CUSTOM_SCHEME + Uri.encode(title);

            if (criteria != null) {
                criteria = criteria.trim();

                if ((criteria.length() > 0) && (criteria.length() <= Const.CUSTOM_SCHEME_PARAM_MAX_LEN))
                    link = link + "?" + Const.CUSTOM_SCHEME_SEARCH_OP + "=" + Uri.encode(criteria);
            }
        }

        return link;
    }

    // Create a note link from clipboard
    protected static String createNoteLinkFromClipboard(Context context, String title, ClipboardManager clipboard) {
        String link = "";
        String criteria;

        try {
            if (clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                ClipData.Item item = clip.getItemAt(0);
                criteria = item.coerceToText(context).toString().replaceAll("\\r\\n|\\r|\\n", " ");

                link = createNoteLink(title, criteria);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return link;
    }

    // Determine linked image path
    protected static String getLinkedImagePath(EditText text) {
        String str = getCurrentSnippet(text, text.getSelectionStart(), false);

        if (str.contains("(") && (str.contains(")"))) {
            int i = str.indexOf("(");
            int j = str.indexOf(")");

            if ((i >= 0) && (j >= 0) && (i < j))
                return str.substring(i+1, j);
        }

        return str;
    }

    // Determine the current snippet
    protected static synchronized String getCurrentSnippet(EditText text, int start, boolean reset) {
        Spannable temp = text.getText();
        final Pattern pattern = Pattern.compile("\\S+");
        final Matcher matcher = pattern.matcher(temp);

        String snippet = "";

        int i = 0;
        int j = 0;

        while (matcher.find()) {
            i = matcher.start();
            j = matcher.end();
            if (i <= start && start <= j) {
                snippet = temp.subSequence(i, j).toString();

                if (reset)
                    text.setSelection(i, j);

                break;
            }
        }

        return snippet;
    }

    // Get enclosed snippet within start and end symbols
    protected static synchronized String getEnclosedSnippet(EditText text, String startSym, String endSym) {
        String str;
        int start, end, current;

        str = text.getText().toString();

        // Extract snippet
        current = text.getSelectionStart();

        // Determine the boundary
        start = str.lastIndexOf(startSym, current);
        if (start == -1) {
            start = str.indexOf(startSym, current);
        }

        end = str.indexOf(endSym, current);
        if (end == -1) {
            end = str.lastIndexOf(endSym, current);
        }

        if ((start != -1) && (end != -1) && (start < end)) {
            end += endSym.length();

            // Pre-select the snippet to be replaced
            text.setSelection(start, end);

            // Return snippet
            return str.substring(start, end);
        }

        return str;
    }

    // Get enclosed drawing within start and end symbols
    protected static synchronized String getEnclosedDrawing(EditText text) {
        String str;
        int start, end, current;

        str = text.getText().toString();

        // Extract snippet
        current = text.getSelectionStart();

        // Determine the boundary
        start = str.lastIndexOf(Const.DRAWING_SEPARATOR_SYM, current);
        if (start == -1) {
            start = str.indexOf(Const.DRAWING_SEPARATOR_SYM, current);
        }

        end = str.indexOf(Const.DRAWING_SEPARATOR_SYM, current);
        if (end == -1) {
            end = str.lastIndexOf(Const.DRAWING_SEPARATOR_SYM, current);
        }

        if ((start != -1) && (end != -1) && (start < end)) {
            end += Const.DRAWING_SEPARATOR_SYM.length();

            // Pre-select the snippet to be replaced
            if (((end - start) > (Const.CANVAS_SIZE_MIN)) && ((end - start) <= (Const.CANVAS_SIZE_MAX + 4)))
                text.setSelection(start, end);

            // Return snippet
            return str.substring(start, end);
        }

        return str;
    }

    // Get text surrounding current position
    protected static synchronized String getCurrentSurroundingText(String str, String focus, int pos, int len, boolean compact, boolean highlight) {
        try {
            int start = Math.max(0, pos - len);
            int end = Math.min(pos + len, str.length());
            String snippet;

            // Build snippet
            snippet = str.substring(start, pos);
            if (highlight)
                snippet += "<b><u>" + focus + "</u></b>";
            else
                snippet += focus;
            snippet += str.substring(pos + focus.length(), end);

            // Compact snippet
            if (compact) {
                snippet = snippet.trim();
                snippet = snippet.replaceAll(System.getProperty("line.separator"), " ");
            }

            // Remove non-words
            snippet = subStringWordBoundary(snippet, 0, snippet.length());

            return snippet;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Get text surrounding current position
    protected static synchronized String getCurrentSurroundingText(String str, int pos, int len, boolean compact, boolean highlight) {
        try {
            int start = Math.max(0, pos - len);
            int end = Math.min(pos + len, str.length());
            String snippet;

            // Build snippet
            snippet = str.substring(start, end);

            // Compact snippet
            if (compact) {
                snippet = snippet.trim();
                snippet = snippet.replaceAll(System.getProperty("line.separator"), " ");
            }

            // Remove non-words
            snippet = subStringWordBoundary(snippet, 0, snippet.length());

            return snippet;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    // Determine the current word
    protected static synchronized String getCurrentWord(EditText text, int start, boolean reset) {
        Spannable temp = text.getText();
        final Pattern pattern = Pattern.compile("\\w+");
        final Matcher matcher = pattern.matcher(temp);

        String word = "";

        int i = 0;
        int j = 0;

        while (matcher.find()) {
            i = matcher.start();
            j = matcher.end();
            if (i <= start && start <= j) {
                word = temp.subSequence(i, j).toString();

                if (reset)
                    text.setSelection(i, j);

                break;
            }
        }

        return word;
    }

    // Get substring based on word boundary
    protected static String subStringWordBoundary(String content, int pos, int len) {
        if (content != null && content.length() >= len) {
            BreakIterator bi = BreakIterator.getWordInstance(Locale.getDefault());
            int start, end;

            bi.setText(content);
            start = bi.next(pos);

            if (bi.isBoundary(len)) {
                if (start >= 0)
                    return content.substring(start, len).trim();
            } else {
                end = bi.preceding(len);
                if ((start >= 0) && (end > start))
                    return content.substring(start, end).trim();
            }
        }

        return content;
    }

    // Compact a string safely
    protected static String safeCompactString(String content) {
        String str = content;

        try {
            str = content.replace("\n", " ").replace("\r", " ").trim();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return str;
    }

    // Lookup a string safely
    protected static int safeIndexOf(String content, String criteria, boolean seekToFirst) {
        int pos = -1;

        try {
            if (seekToFirst)
                pos = content.toLowerCase().indexOf(criteria.toLowerCase());
            else
                pos = content.toLowerCase().lastIndexOf(criteria.toLowerCase());
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return pos;
    }

    // Is open brack?
    protected static boolean isOpenBracket(char ch) {
        return (ch == '{' || ch == '(' || ch == '[' || ch == '<');
    }

    // Is close brack?
    protected static boolean isCloseBracket(char ch) {
        return (ch == '}' || ch == ')' || ch == ']' || ch == '>');
    }

    // Find the peer bracket
    protected static int indexOfPeerBracket(EditText text) {
        String str = text.getText().toString();
        int pos = Math.max(text.getSelectionStart(), text.getSelectionEnd());

        // Sanity check
        if (str.isEmpty())
            return -1;
        else if (pos == 0)
            return -1;

        char current = str.charAt(pos-1);
        if (isOpenBracket(current))
            return indexOfCloseBracket(str, pos);
        else if (isCloseBracket(current))
            return indexOfOpenBracket(str, pos);

        return -1;
    }

    // Find close bracket
    // Source: https://stackoverflow.com/a/19712146
    protected static int indexOfCloseBracket(String str, int pos) {
        Stack<Character> stack = new Stack<>();
        char cur, prev;
        int delta = 0;
        boolean done = false;

        // Sanity check
        if (str.isEmpty())
            return -1;

        for (int i = pos; (i < str.length() && !done); i++, delta++) {
            cur = str.charAt(i);
            if (isOpenBracket(cur)) {
                stack.push(cur);
            }

            if (isCloseBracket(cur)) {
                if (stack.isEmpty())
                    return -1;

                prev = stack.peek();
                if (cur == '}' && prev == '{' || cur == ')' && prev == '(' || cur == ']' && prev == '[' || cur == '>' && prev == '<') {
                    stack.pop();
                    done = stack.isEmpty();
                }
                else
                    return -1;
            }
        }

        if (stack.isEmpty())
            return pos + delta;
        else
            return -1;
    }

    // Find open bracket
    // Source: https://stackoverflow.com/a/19712146
    protected static int indexOfOpenBracket(String str, int pos) {
        Stack<Character> stack = new Stack<>();
        char cur, prev;
        int delta = 0;
        boolean done = false;

        // Sanity check
        if (str.isEmpty())
            return -1;

        for (int i = pos; (i > 0 && !done); i--, delta++) {
            cur = str.charAt(i);

            if (isCloseBracket(cur)) {
                stack.push(cur);
            }

            if (isOpenBracket(cur)) {
                if (stack.isEmpty())
                    return -1;

                prev = stack.peek();
                if (cur == '{' && prev == '}' || cur == '(' && prev == ')' || cur == '[' && prev == ']' || cur == '<' && prev == '>') {
                    stack.pop();
                    done = stack.isEmpty();
                }
                else
                    return -1;
            }
        }

        if (stack.isEmpty())
            return pos - delta + 2;
        else
            return -1;
    }

    // Get current drawing
    protected static synchronized String getCurrentDrawing(EditText text) {
        String drawing;
        String header = Const.DRAWING_SEPARATOR_SYM;
        String footer = Const.DRAWING_SEPARATOR_SYM;
        int start = text.getSelectionStart();
        int end = text.getSelectionEnd();
        int len;

        if (start < end)
            drawing = Utils.getCurrentSelection(text);    // Get selected text
        else
            drawing = getEnclosedDrawing(text);

        // Remove guiding lines
        if (drawing.startsWith(header))
            drawing = drawing.substring(header.length());

        int i = drawing.indexOf(footer);
        if (i > 0)
            drawing = drawing.substring(0, i);

        len = drawing.length();
        if ((len >= Const.CANVAS_SIZE_MIN) && (len <= Const.CANVAS_SIZE_MAX)) {
            return drawing;
        }

        return "";
    }

    // Get current selection
    protected static synchronized String getCurrentSelection(EditText text) {
        int start = Math.min(text.getSelectionStart(), text.getSelectionEnd());
        int end = Math.max(text.getSelectionStart(), text.getSelectionEnd());

        if (start < end)
            return text.getText().toString().substring(start, end);

        return "";
    }

    // Convert uri to file path
    protected static String uri2Path(Context context, Uri uri){
        String path = "";
        String id;
        int idx;

        Pattern pattern = Pattern.compile("(\\d+)$");
        Matcher matcher = pattern.matcher(uri.toString());

        // Sanity check
        if (!matcher.find()) return "";
        id = matcher.group();

        String[] column = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                column,
                MediaStore.Images.Media._ID + "=?",
                new String[]{id},
                null);

        idx = cursor.getColumnIndex(column[0]);
        if (cursor.moveToFirst())
            path = cursor.getString(idx);
        cursor.close();

        return path;
    }

    // Get title from uri
    @SuppressLint("Range")
    protected static String getTitleFromUri(Context context, Uri uri) {
        String title = Const.NULL_SYM;
        int i;

        // First attempt to retrieve title
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    title = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }

        // Another attempt to retrieve title
        if (title == null) {
            title = uri.getPath();
            i = title.lastIndexOf('/');
            if (i != -1)
                title = title.substring(i+1);
        }

        // Determine the title to return
        if (fileNameAsTitle(context))
            return title;
        else
            return title.substring(0, title.lastIndexOf('.'));
    }

    // Get content from uri
    protected static String getContentFromUri(Context context, Uri uri) {
        try {
            InputStream in = context.getContentResolver().openInputStream(uri);
            String line;

            // Get content
            StringBuilder buf = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            while ((line = reader.readLine()) != null) {
                buf.append(line);
                buf.append('\n');
            }
            reader.close();
            in.close();

            return buf.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return Const.NULL_SYM;
        }
    }

    // Write to Uri
    protected static void writeContentToUri(Context context, Uri uri, String content) {
        try {
            OutputStream out = context.getContentResolver().openOutputStream(uri);
            OutputStreamWriter writer = new OutputStreamWriter(out);
            writer.write(content);
            writer.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Decode bitmap Uri
    // Source: http://www.truiton.com/2016/11/optical-character-recognition-android-ocr/
    protected static Bitmap decodeBitmapUri(Context context, Uri uri, int width, int height) throws FileNotFoundException {
        if ((width > 0) && (height > 0)) {
            int target_w = width;
            int target_h = height;
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);

            int photo_w = options.outWidth;
            int photo_h = options.outHeight;
            int scale = Math.min(photo_w / target_w, photo_h / target_h);

            options.inJustDecodeBounds = false;
            options.inSampleSize = scale;

            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options);
        }
        else
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri));
    }

    // Rotate bitmap
    protected static Bitmap rotateBitmap(Bitmap bitmap, int rotation){
        Matrix matrix = new Matrix();

        if (rotation != 0) {
            matrix.preRotate(rotation);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        }

        return bitmap;
    }

    // Get bitmap rotation information
    protected static int getImageRotation(String path){
        try {
            File file = new File(path);

            if (file.exists()) {
                ExifInterface exif = new ExifInterface(file.getPath());
                int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                return exifToDegrees(rotation);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Convert image orientation to degrees
    protected static int exifToDegrees(int exifOrientation) {
        if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) { return 90; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {  return 180; }
        else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {  return 270; }
        return 0;
    }


    /////////////
    // UI Tools
    /////////////

    // To DP
    protected static int toDP(Context context, int val) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(val * density);
    }

    // To SP
    protected static int pxToSp(Context context, float px) {
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        return Math.round(px / scaledDensity);
    }

    // Compute web view height
    protected static float computeWebViewHeight(WebView view, float scale) {
        return view.getContentHeight() * scale - view.getHeight();
    }

    // Compute scroll view height
    protected static float computeScrollViewHeight(ScrollView view) {
        return view.getChildAt(0).getHeight();
    }

    // Determine the current line
    protected static synchronized int getCurrentCursorLine(EditText text) {
        int selection_start = Selection.getSelectionStart(text.getText());
        Layout layout = text.getLayout();

        if ((!(selection_start == -1)) && (layout != null)) {
            return layout.getLineForOffset(selection_start);
        }

        return -1;
    }

    // Check multi window mode
    protected static boolean checkMultiWindowMode(AppCompatActivity activity) {
        if (Build.VERSION.SDK_INT >= 24)
            return activity.isInMultiWindowMode();
        else
            return false;
    }

    // Get system fonts
    protected Map<String, Typeface> getSSystemFontMap() {
        Map<String, Typeface> font_map = null;
        try {
            //Typeface typeface = Typeface.class.newInstance();
            Typeface typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL);
            Field field = Typeface.class.getDeclaredField("sSystemFontMap");
            field.setAccessible(true);
            font_map = (Map<String, Typeface>) field.get(typeface);
            for (Map.Entry<String, Typeface> font : font_map.entrySet()) {
                Log.d(Const.TAG, font.getKey() + ": " + font.getValue() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return font_map;
    }

    // More readable criteria
    protected static String convertCriteriaToStatus(Context context, String criteria, long dateFilter) {
        SimpleDateFormat sdf = new SimpleDateFormat(Const.DATE_STATUS_FORMAT);

        if (criteria.equals(Const.MODIFIED_AFTER_FILTER))
            return context.getResources().getString(R.string.status_modified_after) + " " + sdf.format(dateFilter);

        else if (criteria.equals(Const.ACCESSED_AFTER_FILTER))
            return context.getResources().getString(R.string.status_accessed_after) + " " + sdf.format(dateFilter);

        else if (criteria.equals(Const.MODIFIED_NEARBY_FILTER))
            return getAddress(context);

        return criteria;
    }

    // Determine whether preview highlight is needed
    protected static boolean usePreviewHighlight(String criteria) {
        if     (criteria.startsWith(Const.TITLEONLY) ||
                criteria.startsWith(Const.TITLEREGONLY) ||
                criteria.startsWith(Const.METADATAONLY) ||
                criteria.startsWith(Const.METADATAREGONLY) ||
                criteria.startsWith(Const.TAGALLQUERY) ||
                criteria.startsWith(Const.TAGANYQUERY)) {
            return false;
        }

        return true;
    }

    // Extract the first query item
    protected static String extractFirstQueryItem(String criteria) {
        // Sanity check
        if ((criteria == null) || (criteria.length() == 0))
            return criteria;

        if (criteria.startsWith(Const.TITLEONLY)) {
            criteria = criteria.substring(Const.TITLEONLY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.TITLEREGONLY)) {
            criteria = criteria.substring(Const.TITLEREGONLY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.METADATAONLY)) {
            criteria = criteria.substring(Const.METADATAONLY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.METADATAREGONLY)) {
            criteria = criteria.substring(Const.METADATAREGONLY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.SIMILARQUERY)) {
            criteria = criteria.substring(Const.SIMILARQUERY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.RELATEDQUERY)) {
            criteria = criteria.substring(Const.RELATEDQUERY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.TAGALLQUERY)) {
            criteria = criteria.substring(Const.TAGALLQUERY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.TAGANYQUERY)) {
            criteria = criteria.substring(Const.TAGANYQUERY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.ANDQUERY)) {
            criteria = criteria.substring(Const.ANDQUERY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.ORQUERY)) {
            criteria = criteria.substring(Const.ORQUERY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.ANDGQUERY)) {
            criteria = criteria.substring(Const.ANDGQUERY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.ORGQUERY)) {
            criteria = criteria.substring(Const.ORGQUERY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.JOINQUERY)) {
            criteria = criteria.substring(Const.JOINQUERY.length());
            criteria = criteria.split(",")[0];
        }
        else if (criteria.startsWith(Const.INQUERY)) {
            criteria = criteria.substring(Const.INQUERY.length());
            String[] parts = criteria.split(",");
            if (parts.length > 1)
                criteria = parts[1];
            else
                criteria = parts[0];
        }

        return criteria.trim();
    }

    // Show keyboard
    protected static void showKeyboard(Context context, EditText title, EditText content) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(content, InputMethodManager.SHOW_IMPLICIT);

            title.setKeyListener((KeyListener) title.getTag());
            content.setKeyListener((KeyListener) content.getTag());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Show keyboard after immersive mode
    protected static void showKeyboardAfterImmersiveMode(Context context, EditText title, EditText content) {
        try {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(content, InputMethodManager.SHOW_IMPLICIT);

            title.setKeyListener((KeyListener) title.getTag());
            content.setKeyListener((KeyListener) content.getTag());

            // Avoid resetting scroll position
            title.requestFocus();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Hide keyboard
    protected static void hideKeyboard(Context context, EditText title, EditText content) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(content.getWindowToken(), 0);

        KeyListener listener;

        listener = title.getKeyListener();
        if (listener != null) {
            title.setTag(listener);
            title.setKeyListener(null);
        }

        listener = content.getKeyListener();
        if (listener != null) {
            content.setTag(listener);
            content.setKeyListener(null);
        }
    }

    // Show simple ok/cancel dialog
    protected static void showMessageOKCancel(Context context, String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(context.getResources().getString(R.string.button_ok), okListener)
                .setNegativeButton(context.getResources().getString(R.string.button_cancel), null)
                .create()
                .show();
    }

    // Anchor snackbar to certain id
    protected static void anchorSnackbar(Snackbar snackbar, int id) {
        CoordinatorLayout.LayoutParams params;

        params = (CoordinatorLayout.LayoutParams) snackbar.getView().getLayoutParams();
        params.setAnchorId(id);
        params.anchorGravity = Gravity.TOP;
        params.gravity = Gravity.TOP;
        snackbar.getView().setLayoutParams(params);
    }

    // Make a custom snackbar
    protected static Snackbar makeSnackbar(AppCompatActivity activity, View coordinatorLayout, String msg, int fontSize) {
        Snackbar snackbar;
        TextView text_view;
        View view;

        snackbar = Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        view = snackbar.getView();
        view.setBackground(ContextCompat.getDrawable(activity, R.drawable.custom_snackbar_background));

        text_view = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
        text_view.setTypeface(Typeface.SANS_SERIF);
        text_view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        text_view.setTextColor(ContextCompat.getColor(activity, R.color.snackbar_foreground));

        // Linkify if applicable
        text_view.setLinksClickable(true);
        text_view.setAutoLinkMask(Linkify.ALL);
        text_view.setMovementMethod(LinkMovementMethod.getInstance());
        text_view.setLinkTextColor(ContextCompat.getColor(activity, R.color.snackbar_linkify_color));
        Linkify.addLinks(text_view, Linkify.ALL);

        return snackbar;
    }

    // Make a high contrast snackbar
    protected static Snackbar makeHighContrastSnackbar(AppCompatActivity activity, View coordinatorLayout, String msg, int fontSize) {
        Snackbar snackbar;
        TextView text_view;
        View view;

        snackbar = Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_LONG);

        view = snackbar.getView();
        view.setBackground(ContextCompat.getDrawable(activity, R.drawable.custom_snackbar_background));

        text_view = (snackbar.getView()).findViewById(com.google.android.material.R.id.snackbar_text);
        text_view.setTypeface(Typeface.SANS_SERIF);
        text_view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        text_view.setTextColor(ContextCompat.getColor(activity, R.color.high_contrast_snackbar_foreground));

        return snackbar;
    }

    // Make a paste snackbar
    protected static Snackbar makePasteSnackbar(AppCompatActivity activity, View view, final EditText content, final String result) {
        Snackbar snackbar;

        snackbar = Snackbar.make(view, result, Snackbar.LENGTH_LONG);
        snackbar.setAction(activity.getResources().getString(R.string.snack_bar_button_paste), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Seek to next space
                int pos = seekToNextSpace(content, content.getSelectionEnd());

                // Sanity check
                if (pos == -1) pos = content.getSelectionEnd();

                // Paste result
                content.setSelection(pos);
                insert(content, result);
            }
        });

        return snackbar;
    }

    // Make a copy snackbar
    protected static Snackbar makeCopySnackbar(AppCompatActivity activity, View view, final String result) {
        Snackbar snackbar;

        snackbar = Snackbar.make(view, result, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(activity.getResources().getString(R.string.snack_bar_button_copy), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add to clipboard
                ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(Const.CUSTOM_SCHEME, result);
                clipboard.setPrimaryClip(clip);
            }
        });

        return snackbar;
    }

    // Get edit text scroll percentage
    protected static int getEditTextScrollPercent(EditText content) {
        float start = content.getSelectionStart();
        float length = content.length();
        float percent = start / length * 100;

        return Math.round(percent);
    }

    // Get webview scroll percentage
    protected static int getWebViewScrollPercent(Context context, WebView view) {
        float height = view.getContentHeight() * view.getScaleY();
        float total = height * context.getResources().getDisplayMetrics().density - view.getHeight();
        float percent = Math.min(view.getScrollY() / (total - context.getResources().getDisplayMetrics().density), 1) * 100;

        return Math.round(percent);
    }

    // Get webview scroll percentage in float
    protected static float getWebViewScrollPercentFloat(Context context, WebView view) {
        float height = view.getContentHeight() * view.getScaleY();
        float total = height * context.getResources().getDisplayMetrics().density - view.getHeight();
        float percent = Math.min(view.getScrollY() / (total - context.getResources().getDisplayMetrics().density), 1) * 100;

        return percent;
    }

    // Set webview scroll position by percent
    protected static void setWebViewScrollPositionPercent(Context context, WebView view, int percent) {
        int pos;
        int size = view.getContentHeight();

        if (percent < 100)
            pos = Math.round(size * percent / 100);
        else
            pos = Math.round(size * Const.IN_NOTE_MARKDOWN_MAX_PERCENT / 100);

        view.scrollTo(0, pos * ((int) context.getResources().getDisplayMetrics().density));
    }

    // Set dialog background dim level
    protected static void setDialogDimLevel(Dialog dialog, float dim) {
        Window window = dialog.getWindow();

        if(window != null){
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(dim);
        }
    }

    // Clear dialog background dim level
    protected static void clearDialogDimLevel(Dialog dialog) {
        Window window = dialog.getWindow();

        if(window != null){
            dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }
    }

    ///////////////////
    // Synchronization
    //////////////////

    protected static boolean checkWriteLock() { return mWriteLock; }
    protected static synchronized void acquireWriteLock() { mWriteLock = true; }
    protected static synchronized void releaseWriteLock() { mWriteLock = false; }

    //////////////////////
    // Package Management
    //////////////////////

    // Check package
    protected static boolean hasPackage(Context context, String target) {
        List<ApplicationInfo> packages;
        PackageManager manager = context.getPackageManager();
        packages = manager.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(target)) return true;
        }
        return false;
    }

    // Check intent availability
    protected static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List list = packageManager.queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    // Launch package
    protected static boolean launchPackage(Context context, String target) {
        if (hasPackage(context, target)) {
            try {
                Intent intent = context.getPackageManager().getLaunchIntentForPackage(target);
                context.startActivity(intent);
                return true;
            }
            catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        else
            return false;
    }

    // Check version number
    protected static String getVersion(Context context) {
        String version = "";
        PackageManager manager = context.getPackageManager();
        try {
            PackageInfo info = manager.getPackageInfo(Const.PACKAGE, 0);
            version = info.versionName;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return version;
        }
    }

    // Set light level
    protected static void setLightLevel(float lightLevel) {
        mLightLevel = lightLevel;
    }

    // Get light level
    protected static float getLightLevel() {
        return mLightLevel;
    }

    // Get white color
    protected static int getWhiteColor(Context context, int defaultColor, boolean lux) {
        int color;
        float light_level;

        color = ContextCompat.getColor(context, defaultColor);

        if (lux) {
            light_level = getLightLevel();

            if (light_level > Const.LIGHT_LEVEL_THRESHOLD_OFFICE)
                color = ContextCompat.getColor(context, R.color.white_daylight);

            else if ((light_level > Const.LIGHT_LEVEL_THRESHOLD_LIVING_ROOM) && (light_level <= Const.LIGHT_LEVEL_THRESHOLD_OFFICE))
                color = ContextCompat.getColor(context, R.color.white_office);

            else if ((light_level > Const.LIGHT_LEVEL_THRESHOLD_STARLIGHT) && (light_level <= Const.LIGHT_LEVEL_THRESHOLD_LIVING_ROOM))
                color = ContextCompat.getColor(context, R.color.white_living_room);

            else if (light_level <= Const.LIGHT_LEVEL_THRESHOLD_STARLIGHT)
                color = ContextCompat.getColor(context, R.color.white_starlight);
        }

        return color;
    }

    // Send sync request
    protected static void sendSyncRequest(Context context, String path, Uri uri) {
        Intent intent;

        if (Utils.hasPackage(context, Const.CONNECTORPLUS_PACKAGE)) {
            intent = new Intent(Const.ACTION_REQUEST_SYNC_PLUS);
            context.sendBroadcast(intent);
        }
        else if (Utils.hasPackage(context, Const.CONNECTOR_PACKAGE)) {
            intent = new Intent(Const.ACTION_REQUEST_SYNC);
            context.sendBroadcast(intent);
        }

        // Alert repo monitor(s) via file
        if (Utils.fileExists(context, path, Const.NOOP_FILE)) {
            Utils.writeLocalRepoFile(context, Const.NOOP_FILE, Const.NULL_SYM);
        }

        // Alert repo monitor(s) via scope storage
        if (!uri.equals(Uri.EMPTY)) {
            Uri file_uri = Utils.getSAFSubDirUri(context, uri, Const.NOOP_FILE);
            if (file_uri != null)
                Utils.writeSAFFile(context, file_uri, Const.NULL_SYM);
        }
    }

    ////////////
    // Location
    ////////////
    // Is the location near?
    protected static boolean isNearBy(Location here, double latitude, double longitude) {
        Location location = new Location(here);

        location.setLatitude(latitude);
        location.setLongitude(longitude);

        double distance = location.distanceTo(here);
        if (distance < Const.NEARBY)
            return true;

        return false;
    }

    // Compute distance
    protected static double getDistance(Location here, double latitude, double longitude) {
        Location location = new Location(here);

        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return location.distanceTo(here);
    }

    // Get location
    protected static Location getLocation(Context context) {

        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location location, best_location = null;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, context.getResources().getString(R.string.status_location_permission_denied), Toast.LENGTH_LONG).show();
            return null;
        }

        try {
            long age, best_age = Long.MIN_VALUE;
            float accuracy, best_accuracy = Float.MAX_VALUE;
            List<String> providers = manager.getAllProviders();

            for (String provider : providers) {
                location = manager.getLastKnownLocation(provider);

                if (location != null) {
                    accuracy = location.getAccuracy();
                    age = location.getTime();

                    if (accuracy < best_accuracy) {
                        best_location = location;
                        best_accuracy = accuracy;
                        best_age = age;
                    }
                }
            }

            // Return best reading
            return best_location;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Translate location to address
    protected static String getAddress(Context context) {
        StringBuilder address_str = new StringBuilder();
        Address address;
        Location location = getLocation(context);

        Geocoder gcd = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);

            if (addresses.size() > 0) {
                address = addresses.get(0);
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    address_str.append(address.getAddressLine(i)).append(Const.NEWLINE);
                }
            }
            else
                address_str.append(context.getResources().getString(R.string.error_location_unknown)).append(Const.NEWLINE);

            // Append coordinates
            address_str.append(Const.AT_SYM + location.getLatitude() + ", " + location.getLongitude()).append(Const.NEWLINE);
        } catch (Exception e) {
            if (location == null)
                address_str.append(context.getResources().getString(R.string.error_location_unknown)).append(Const.NEWLINE);
            else
                address_str.append(Const.AT_SYM + location.getLatitude() + ", " + location.getLongitude()).append(Const.NEWLINE);
        }

        return address_str.toString();
    }

    //////////////////////
    // Validation
    //////////////////////

    // Validate the criteria for local find
    protected static boolean validateLocalFindCriteria(String criteria) {
        if (criteria.equals(Const.ALL_SYM) || criteria.equals(Const.STARRED_SYM) || criteria.equals(Const.NUM_SYM))
            return false;

        if ((criteria.equals(Const.MODIFIED_AFTER_FILTER)) || (criteria.equals(Const.ACCESSED_AFTER_FILTER)) || (criteria.equals(Const.MODIFIED_NEARBY_FILTER)))
            return false;

        if (criteria.startsWith(Const.TITLEONLY))
            return false;

        if (criteria.startsWith(Const.TITLEREGONLY))
            return false;

        if (criteria.startsWith(Const.METADATAREGONLY))
            return false;

        if (criteria.startsWith(Const.METADATAONLY))
            return false;

        if (criteria.startsWith(Const.SIMILARQUERY))
            return false;

        if (criteria.startsWith(Const.RELATEDQUERY))
            return false;

        if (criteria.startsWith(Const.TAGALLQUERY))
            return false;

        if (criteria.startsWith(Const.TAGANYQUERY))
            return false;

        if (criteria.startsWith(Const.ANDQUERY))
            return true;

        if (criteria.startsWith(Const.ANDGQUERY))
            return true;

        if (criteria.startsWith(Const.ORQUERY))
            return true;

        if (criteria.startsWith(Const.ORGQUERY))
            return true;

        if (criteria.startsWith(Const.JOINQUERY))
            return true;

        if (criteria.startsWith(Const.INQUERY))
            return true;

        if (criteria.length() == 1)
            return false;

        return true;
    }

    // Validate the criteria for boolean search
    protected static boolean validateBooleanSearchCriteria(String criteria) {
        return (criteria.contains(","));
    }

    // Check network connection
    protected static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        return (activeNetwork != null && activeNetwork.isConnectedOrConnecting());
    }

    //////////////////////
    // Permissions
    //////////////////////

    // Add new permission
    protected static boolean addPermission(AppCompatActivity activity, List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);

            // Check for Rationale Option
            if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, permission))
                return false;
        }
        return true;
    }

    // Force add new permission
    protected static boolean addPermissionForced(AppCompatActivity activity, List<String> permissionsList, String permission) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
            permissionsList.add(permission);

        return false;
    }

    /////////
    // Math
    /////////

    // Expression evaluator
    // Source: http://stackoverflow.com/questions/3422673/evaluating-a-math-expression-given-in-string-form
    protected static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()) return Double.NaN;
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else return Double.NaN;
                } else {
                    return Double.NaN;
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

    /////////////////
    // Notification
    ////////////////

    // Create a notification channel
    // Source: https://stackoverflow.com/a/44524976
    protected static void makeNotificationChannel(NotificationManager manager, String id, String name, String desc, int level) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
            return;

        NotificationChannel channel = new NotificationChannel(id, name, level);
        channel.setDescription(desc);
        channel.setSound(null, null);
        manager.createNotificationChannel(channel);
    }
}
