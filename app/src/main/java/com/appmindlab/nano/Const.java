package com.appmindlab.nano;

import android.app.NotificationManager;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by saelim on 7/28/2015.
 */
public class Const {
    // Constants
    protected final static String TAG = "neutrinote";
    protected final static String PACKAGE = "com.appmindlab.nano";
    protected final static String CONNECTOR_PACKAGE = "com.appmindlab.connector";
    protected final static String CONNECTORPLUS_PACKAGE = "com.appmindlab.connectorplus";
    protected final static String BACKUPPLUS_PACKAGE = "com.appmindlab.backupplus";

    protected final static String PROCESS_TEXT_ACTIVITY_NAME = ".MainActivityProcessTextAlias";

    protected final static String EXTRA_ID = "com.appmindlab.nano.ID";
    protected final static String EXTRA_CRITERIA = "com.appmindlab.nano.criteria";
    protected final static String EXTRA_TITLE = "com.appmindlab.nano.title";
    protected final static String EXTRA_CONTENT = "com.appmindlab.nano.content";
    protected final static String EXTRA_SHARED_CONTENT = "com.appmindlab.nano.shared_content";

    protected final static String EXTRA_FILEPATH = "com.appmindlab.nano.file_path";
    protected final static String EXTRA_URI = "com.appmindlab.nano.uri";

    protected final static String EXTRA_LIGHT_LEVEL = "com.appmindlab.nano.EXTRA_LIGHT_LEVEL";
    protected final static String EXTRA_LIGHT_LEVEL_THRESHOLD = "com.appmindlab.nano.EXTRA_LIGHT_LEVEL_THRESHOLD";

    protected final static String EXTRA_MAX_BACKUP_COUNT = "com.appmindlab.nano.EXTRA_MAX_BACKUP_COUNT";
    protected final static String EXTRA_MAX_BACKUP_AGE = "com.appmindlab.nano.EXTRA_MAX_BACKUP_AGE";    // Backup max age in days

    // Preferences
    protected final static String PREF_LOCAL_REPO_PATH = "com.appmindlab.nano.pref_local_repo_path";
    protected final static String PREF_BACKUP_URI = "com.appmindlab.nano.pref_backup_uri";
    protected final static String PREF_INCREMENTAL_BACKUP = "com.appmindlab.nano.pref_incremental_backup";
    protected final static String PREF_NEW_NOTE_TITLE_TEMPLATE = "com.appmindlab.nano.pref_new_note_title_template";
    protected final static String PREF_NEW_NOTE_FILE_TYPE = "com.appmindlab.nano.pref_new_note_file_type";
    protected final static String PREF_CUSTOM_FILTERS = "com.appmindlab.nano.pref_custom_filters";
    protected final static String PREF_AUTO_SAVE = "com.appmindlab.nano.pref_auto_save";
    protected final static String PREF_LOCATION_AWARE = "com.appmindlab.nano.pref_location_aware";
    protected final static String PREF_LAZY_UPDATE = "com.appmindlab.nano.pref_lazy_update";
    protected final static String PREF_MATH_URL = "com.appmindlab.nano.pref_math_url";
    protected final static String PREF_COPY_ATTACHMENTS_TO_REPO = "com.appmindlab.nano.pref_copy_attachments_to_repo";
    protected final static String PREF_SHOW_HIDDEN = "com.appmindlab.nano.pref_show_hidden";
    protected final static String PREF_ORDER_BY = "com.appmindlab.nano.pref_order_by";
    protected final static String PREF_ORDER_BY_DIRECTION = "com.appmindlab.nano.pref_order_by_direction";
    protected final static String PREF_STAR_AT_TOP = "com.appmindlab.nano.pref_star_at_top";
    protected final static String PREF_THEME = "com.appmindlab.nano.pref_theme";
    protected final static String PREF_LUX = "com.appmindlab.nano.pref_lux";
    protected final static String PREF_OLED = "com.appmindlab.nano.pref_oled";
    protected final static String PREF_FONT_FAMILY = "com.appmindlab.nano.pref_font_family";
    protected final static String PREF_FONT_SIZE = "com.appmindlab.nano.pref_font_size";
    protected final static String PREF_FONT_SIZE_LIST = "com.appmindlab.nano.pref_font_size_list";
    protected final static String PREF_MARGIN = "com.appmindlab.nano.pref_margin";
    protected final static String PREF_MARGIN_LIST = "com.appmindlab.nano.pref_margin_list";
    protected final static String PREF_OPEN_IN_MARKDOWN = "com.appmindlab.nano.pref_open_in_markdown";
    protected final static String PREF_MARKDOWN_TRIGGER = "com.appmindlab.nano.pref_markdown_trigger";
    protected final static String PREF_SAFE_MODE_TAG = "com.appmindlab.nano.pref_safe_mode_tag";
    protected final static String PREF_AUTO_TOOLBAR_TAG = "com.appmindlab.nano.pref_auto_toolbar_tag";
    protected final static String PREF_LOCAL_PRIORITY_TAG = "com.appmindlab.nano.pref_local_priority_tag";
    protected final static String PREF_REMOTE_PRIORITY_TAG = "com.appmindlab.nano.pref_remote_priority_tag";
    protected final static String PREF_LINKIFY_TRIGGER = "com.appmindlab.nano.pref_linkify_trigger";
    protected final static String PREF_SHOW_TOOLBAR = "com.appmindlab.nano.pref_show_toolbar";
    protected final static String PREF_TOOLBOX_MODE = "com.appmindlab.nano.pref_toolbox_mode";
    protected final static String PREF_EXCLUDED_BUTTONS = "com.appmindlab.nano.pref_excluded_buttons";
    protected final static String PREF_LATEX_SINGLE_DOLLAR = "com.appmindlab.nano.pref_latex_single_dollar";
    protected final static String PREF_APPEND_CUSTOM_STYLE = "com.appmindlab.nano.pref_append_custom_style";
    protected final static String PREF_INDENT_CHAR = "com.appmindlab.nano.pref_indent_char";
    protected final static String PREF_CUSTOM_DATE_FORMAT = "com.appmindlab.nano.pref_custom_date_format";
    protected final static String PREF_CUSTOM_TIME_FORMAT = "com.appmindlab.nano.pref_custom_time_format";
    protected final static String PREF_ICON_BEHAVIOR = "com.appmindlab.nano.pref_icon_behavior";
    protected final static String PREF_MARKDOWN_LOCAL_CACHE = "com.appmindlab.nano.pref_markdown_local_cache";
    protected final static String PREF_PREVIEW_MODE = "com.appmindlab.nano.pref_preview_mode";
    protected final static String PREF_CANVAS_STROKES = "com.appmindlab.nano.pref_canvas_strokes";
    protected final static String PREF_KEEP_DELETED_COPIES = "com.appmindlab.nano.pref_keep_deleted_copies";
    protected final static String PREF_MAX_DELETED_COPIES_AGE = "com.appmindlab.nano.pref_max_deleted_copies_age";
    protected final static String PREF_MAX_SYNC_LOG_FILE_SIZE = "com.appmindlab.nano.pref_max_sync_log_file_size";
    protected final static String PREF_MAX_SYNC_LOG_FILE_AGE = "com.appmindlab.nano.pref_max_sync_log_file_age";
    protected final static String PREF_EVAL_BUILT_IN_VARIALBES = "com.appmindlab.nano.pref_eval_built_in_variables";
    protected final static String PREF_LOW_SPACE_MODE = "com.appmindlab.nano.pref_low_space_mode";
    protected final static String PREF_PARSE_PYTHON = "com.appmindlab.nano.pref_parse_python";
    protected final static String PREF_PARSE_VUE = "com.appmindlab.nano.pref_parse_vue";
    protected final static String PREF_PARSE_ALPINE = "com.appmindlab.nano.pref_parse_alpine";
    protected final static String PREF_PROCESS_TEXT_MODE = "com.appmindlab.nano.pref_process_text_mode";
    protected final static String PREF_WORKING_SET_SIZE = "com.appmindlab.nano.pref_working_set_size";
    protected final static String PREF_LAB_MODE = "com.appmindlab.nano.pref_lab_mode";
    protected final static String[] ALL_PREFS = {
            PREF_LOCAL_REPO_PATH,
            /* PREF_BACKUP_URI, */    // Excluded from preference backup
            PREF_INCREMENTAL_BACKUP,
            PREF_NEW_NOTE_TITLE_TEMPLATE,
            PREF_NEW_NOTE_FILE_TYPE,
            PREF_CUSTOM_FILTERS,
            PREF_AUTO_SAVE,
            PREF_LOCATION_AWARE,
            PREF_LAZY_UPDATE,
            PREF_MATH_URL,
            PREF_COPY_ATTACHMENTS_TO_REPO,
            PREF_SHOW_HIDDEN,
            PREF_ORDER_BY,
            PREF_ORDER_BY_DIRECTION,
            PREF_STAR_AT_TOP,
            PREF_THEME,
            PREF_LUX,
            PREF_OLED,
            PREF_FONT_FAMILY,
            PREF_FONT_SIZE,
            PREF_FONT_SIZE_LIST,
            PREF_MARGIN,
            PREF_MARGIN_LIST,
            PREF_OPEN_IN_MARKDOWN,
            PREF_MARKDOWN_TRIGGER,
            PREF_SAFE_MODE_TAG,
            PREF_AUTO_TOOLBAR_TAG,
            PREF_LOCAL_PRIORITY_TAG,
            PREF_REMOTE_PRIORITY_TAG,
            PREF_LINKIFY_TRIGGER,
            PREF_SHOW_TOOLBAR,
            PREF_TOOLBOX_MODE,
            PREF_EXCLUDED_BUTTONS,
            PREF_LATEX_SINGLE_DOLLAR,
            PREF_APPEND_CUSTOM_STYLE,
            PREF_INDENT_CHAR,
            PREF_CUSTOM_DATE_FORMAT,
            PREF_CUSTOM_TIME_FORMAT,
            PREF_ICON_BEHAVIOR,
            PREF_MARKDOWN_LOCAL_CACHE,
            PREF_PREVIEW_MODE,
            PREF_CANVAS_STROKES,
            PREF_KEEP_DELETED_COPIES,
            PREF_MAX_DELETED_COPIES_AGE,
            PREF_MAX_SYNC_LOG_FILE_SIZE,
            PREF_MAX_SYNC_LOG_FILE_AGE,
            PREF_EVAL_BUILT_IN_VARIALBES,
            PREF_LOW_SPACE_MODE,
            PREF_PARSE_PYTHON,
            PREF_PARSE_VUE,
            PREF_PARSE_ALPINE,
            PREF_PROCESS_TEXT_MODE,
            PREF_WORKING_SET_SIZE,
            PREF_LAB_MODE
    };

    // Boolean preferences
    protected final static String[] BOOL_PREFS = {
            PREF_INCREMENTAL_BACKUP,
            PREF_AUTO_SAVE,
            PREF_LOCATION_AWARE,
            PREF_STAR_AT_TOP,
            PREF_LUX,
            PREF_OLED,
            PREF_LAZY_UPDATE,
            PREF_COPY_ATTACHMENTS_TO_REPO,
            PREF_SHOW_HIDDEN,
            PREF_OPEN_IN_MARKDOWN,
            PREF_SHOW_TOOLBAR,
            PREF_LATEX_SINGLE_DOLLAR,
            PREF_APPEND_CUSTOM_STYLE,
            PREF_MARKDOWN_LOCAL_CACHE,
            PREF_KEEP_DELETED_COPIES,
            PREF_EVAL_BUILT_IN_VARIALBES,
            PREF_LOW_SPACE_MODE,
            PREF_PARSE_PYTHON,
            PREF_PARSE_VUE,
            PREF_PARSE_ALPINE,
            PREF_LAB_MODE
    };

    protected final static String AUTO_BACKUP_LOG = "com.appmindlab.nano.auto_backup_log";
    protected final static String AUTO_MIRROR_LOG = "com.appmindlab.nano.auto_mirror_log";
    protected final static String MIRROR_TIMESTAMP = "com.appmindlab.nano.mirror_timestamp";
    protected final static String SYNC_LOG = "com.appmindlab.nano.sync_log";
    protected static final String TILE_SERVICE_STATE = "com.appmindlab.nano.tile_service_state";

    // Backup related
    protected final static String BACKUP_PREF = "backup_preference";
    protected final static String BACKUP_PREF_KEY = "neutriNote";

    // Defaults
    protected final static String DEFAULT_THEME = "day";
    protected final static String DEFAULT_FONT_FAMILY = "Roboto Mono Regular";
    protected final static String DEFAULT_FONT_SIZE = "14";
    protected final static String DEFAULT_FONT_SIZE_LIST = "8;10;12;14;16;18;24;32;48";
    protected final static String DEFAULT_MARGIN = "16";
    protected final static String DEFAULT_MARGIN_LIST = "8;16;24";
    protected final static String DEFAULT_CANVAS_STROKES = "\u25CF;:;\\;/;_;-;`;,;";
    protected final static String DEFAULT_EXCLUDED_BUTTONS = ";";

    // Links
    protected final static String MATHJS_URL = "https://mathjs.herokuapp.com/v4/?expr=";
    protected final static String MATHJS_EXPR_PARAM = "expr=";
    protected final static String MATHJS_PRECISION_PARAM = "precision=100";
    protected final static String UNSET_URL = "https://foo";
    protected final static String HELP_URL = "https://appml.github.io/nano/";
    protected final static String OFFICIAL_URL = "https://neutrinote.wordpress.com";
    protected final static String CUSTOM_SCHEME = "https://neutrinote.io/";
    protected final static String CUSTOM_SCHEME_SEARCH_OP = "search";
    protected final static int CUSTOM_SCHEME_PARAM_MAX_LEN = 32;

    // Intents
    protected final static String ACTION_REQUEST_SYNC = "com.appmindlab.connector.ACTION_REQUEST_SYNC";
    protected final static String ACTION_REQUEST_SYNC_PLUS = "com.appmindlab.connectorplus.ACTION_REQUEST_SYNC";
    protected final static String ACTION_SCHEDULE_BACKUP = "com.appmindlab.nano.ACTION_SCHEDULE_BACKUP";
    protected final static String ACTION_INCREMENTAL_BACKUP = "com.appmindlab.nano.ACTION_INCREMENTAL_BACKUP";
    protected final static String ACTION_FULL_BACKUP = "com.appmindlab.nano.ACTION_FULL_BACKUP";
    protected final static String ACTION_UPDATE_WIDGET = "com.appmindlab.nano.ACTION_UPDATE_WIDGET";
    protected final static String ACTION_VIEW_ENTRY = "com.appmindlab.nano.ACTION_VIEW_ENTRY";
    protected static final String ACTION_CHANGE_DISPLAY_SETTINGS = "com.appmindlab.nano.ACTION_CHANGE_DISPLAY_SETTINGS";
    protected static final String ACTION_UPDATE_SYNC_LOG = "com.appmindlab.nano.ACTION_UPDATE_SYNC_LOG";
    protected static final String ACTION_AUTO_SEND = "com.google.android.gm.action.AUTO_SEND";

    // States
    protected final static String STATE_ID = "mId";
    protected final static String STATE_ORDER_BY = "mOrderBy";
    protected final static String STATE_ORDER_DIRECTION = "mOrderDirection";
    protected final static String STATE_TITLE_BAR_VISIBLE = "mTitleBarVisible";
    protected final static String STATE_TOOL_BAR_VISIBLE = "mToolBarVisible";
    protected final static String STATE_EDIT_TOOL_FRAGMENT_VISIBLE = "mEditToolFragmentVisible";
    protected final static String STATE_MARKDOWN_MODE = "mMarkdownMode";
    protected final static String STATE_IMMERSIVE_MODE = "mImmersiveMode";
    protected final static String STATE_CRITERIA = "mCriteria";
    protected final static String STATE_DATE_FILTER = "mDateFilter";
    protected final static String STATE_SELECTION_STATE = "selectionState";
    protected final static String STATE_NAVIGATION_POSITION = "navigationPosition";
    protected final static String STATE_HITS = "mHits";
    protected final static String STATE_HIT_INDEX = "mHitIdx";
    protected final static String STATE_NEXT_POS = "mNextPos";
    protected final static String STATE_ANCHOR_POS = "mAnchorPos";
    protected final static String STATE_MARKDOWN_ANCHOR_POS = "mMarkdownAnchorPos";
    protected final static String STATE_FULL_PATH = "mFullPath";
    protected final static String STATE_DIR_PATH = "mDirPath";
    protected final static String STATE_SUB_DIR_PATH = "mSubDirPath";
    protected final static String STATE_CHANGED = "mChanged";
    protected final static String STATE_AUTOSAVE_SAFE = "mAutoSaveSafe";
    protected final static String STATE_SNAPSHOT_SAFE = "mSnapshotSafe";
    protected final static String STATE_RELOAD_SAFE = "mReloadSafe";
    protected final static String STATE_MIRROR_SAFE = "mMirrorSafe";
    protected final static String STATE_CANVAS_STROKE = "mCanvasStroke";
    protected final static String STATE_AUTO_THEME_APPLIED = "mAutoThemeApplied";

    // UI
    protected final static int PREVIEW_LEN = 80;
    protected final static int SOFT_PREVIEW_LEN = PREVIEW_LEN + 1;
    protected final static int HIT_PREVIEW_LEN = 60;
    protected final static int IN_NOTE_PREVIEW_LEN = 80;
    protected final static int IN_NOTE_PREVIEW_FONT_SIZE = 14;
    protected final static int WORKING_SET_PREVIEW_LEN = 80;
    protected final static int WORKING_SET_PREVIEW_FONT_SIZE = 14;
    protected final static int WIDGET_LEN = 140;
    protected final static int FASTSCROLL_TAB_LEN = 4;
    protected final static String PREVIEW_AT_START = "start";
    protected final static String PREVIEW_AT_END = "end";
    protected final static String PREVIEW_OFF = "off";
    protected final static String PREVIEW_LAZY = "lazy";
    protected final static String ICON_BEHAVIOR_DISABLED = "0";
    protected final static String ICON_BEHAVIOR_ENABLED = "1";
    protected final static String ICON_BEHAVIOR_SNOOZE = "2";
    protected final static int ICON_MINI_SIZE = 40;
    protected final static int CLIPBOARD_PREVIEW_LEN = 80;
    protected final static String SORT_BY_TITLE = "title COLLATE NOCASE";
    protected final static String SORT_BY_MODIFIED = "modified";
    protected final static String SORT_BY_ACCESSED = "accessed";
    protected final static String SORT_BY_STAR = "star";
    protected final static String SORT_ASC = "ASC";
    protected final static String SORT_DESC = "DESC";
    protected final static String DAY_THEME = "day";
    protected final static String NIGHT_THEME = "night";
    protected final static String DARK_THEME = "dark";
    protected final static int LIGHT_LEVEL_THRESHOLD_STARLIGHT = 10;
    protected final static int LIGHT_LEVEL_THRESHOLD_LIVING_ROOM = 50;
    protected final static int LIGHT_LEVEL_THRESHOLD_OFFICE = 80;
    protected final static int LIGHT_LEVEL_THRESHOLD_NATURAL_LIGHT = 100;
    protected final static int LIGHT_LEVEL_THRESHOLD_DIRECT_SUNLIGHT = 1000;
    protected final static int DIALOG_TITLE_SIZE = 18;
    protected final static int DIALOG_PADDING = 48;
    protected final static float DIALOG_DIM_LEVEL = 0.3f;
    protected static final int MAIN_STATUS_SWIPE_H_MIN_DISTANCE = 85;
    protected static final int MAIN_STATUS_SWIPE_H_MAX_OFF_PATH = 200;
    protected static final int MAIN_STATUS_SWIPE_H_THRESHOLD_VELOCITY = 100;
    protected static final int EDIT_STATUS_SWIPE_H_MIN_DISTANCE = 80;
    protected static final int EDIT_STATUS_SWIPE_H_MAX_OFF_PATH = 800;
    protected static final int EDIT_STATUS_SWIPE_H_THRESHOLD_VELOCITY = 50;
    protected static final int EDIT_STATUS_SWIPE_V_MIN_DISTANCE = 20;
    protected static final int EDIT_STATUS_SWIPE_V_MAX_OFF_PATH = 800;
    protected static final int EDIT_STATUS_SWIPE_V_THRESHOLD_VELOCITY = 30;
    protected static final int EDIT_CONTENT_SWIPE_V_MIN_DISTANCE = 20;
    protected static final int EDIT_CONTENT_SWIPE_V_MAX_OFF_PATH = 800;
    protected static final String TOOLBOX_MODE_STATEFUL = "stateful";
    protected static final String TOOLBOX_MODE_STATELESS = "stateless";
    protected static final String TOOLBOX_MODE_PIN_SAVE = "pin_save";

    // Buttons
    protected final static String BUTTON_MARKDOWN = "markdown";
    protected final static String BUTTON_TIMESTAMP = "time";
    protected final static String BUTTON_DATESTAMP = "date";
    protected final static String BUTTON_LOCATIONSTAMP = "location";
    protected final static String BUTTON_EXPAND = "expand";
    protected final static String BUTTON_DRAW = "draw";
    protected final static String BUTTON_TOP = "top";
    protected final static String BUTTON_BOTTOM = "bottom";
    protected final static String BUTTON_LOCAL_FIND = "find";
    protected final static String BUTTON_LOCAL_REPLACE = "replace";
    protected final static String BUTTON_BARCODE = "barcode";
    protected final static String BUTTON_IMAGE = "image";
    protected final static String BUTTON_OCR = "ocr";
    protected final static String BUTTON_DEFINE = "define";
    protected final static String BUTTON_CALCULATE = "calculate";
    protected final static String BUTTON_WEB_SEARCH = "search";
    protected final static String BUTTON_ENCRYPT = "encrypt";
    protected final static String BUTTON_DECRYPT = "decrypt";

    // HTML
    protected final static String WEBVIEW_DENSITY = "<meta name='viewport' content='target-densitydpi=device-dpi' />";
    protected final static String TOC = "<span id='toc' />";
    protected final static String LATEX_SINGLE_DOLLAR_CONFIG = "<script>MathJax = { tex: { inlineMath: [['$', '$'], ['\\(', '\\)']] } };</script>";
    protected final static String JAVASCRIPT_INTERFACE = "appm";

    // Javascript
    protected final static String PARSER_API_JS = "<script type='text/javascript' src='file:///android_asset/html/parser-api.js'></script>";
    protected final static String MDX_CONVERT_JS = "<script type='text/javascript' src='file:///android_asset/html/mdx-convert.js'></script>";
    protected final static String MDX_EXTRA_JS = "<script type='text/javascript' src='file:///android_asset/html/mdx-extra.js'></script>";
    protected final static String MDX_TOC_JS = "<script type='text/javascript' src='file:///android_asset/html/mdx-toc.js'></script>";
    protected final static String JQUERY = "<script type='text/javascript' src='file:///android_asset/html/jqry.js'></script>";
    protected final static String PYTHON_JS = "<script type='text/python'></script><head><script type='text/javascript' src='https://cdnjs.cloudflare.com/ajax/libs/brython/3.7.0/brython.min.js'></script><script type='text/javascript' src='https://cdnjs.cloudflare.com/ajax/libs/brython/3.7.0/brython_stdlib.js'></script><script language='javascript'>window.onload = brython()</script></head>";
    protected final static String VUE_JS = "<script src='https://cdn.jsdelivr.net/npm/vue'></script>";
    protected final static String ALPINE_JS = "<script src='https://cdn.jsdelivr.net/gh/alpinejs/alpine@v2.x.x/dist/alpine.min.js' defer></script>";
    protected final static String APP_JS = "<script type='text/javascript' src='file://./app.js'></script>";
    protected final static String NANO_JS = "<script type='text/javascript' src='file:///android_asset/html/nano.js'></script>";
    protected final static String CUSTOM_SCRIPT = "~neutrinote_script";

    // Style sheet
    protected final static String GFM_LIGHT_CSS = "<link rel='stylesheet' type='text/css' href='file:///android_asset/html/gfm-light.css'>";
    protected final static String GFM_NIGHT_CSS = "<link rel='stylesheet' type='text/css' href='file:///android_asset/html/gfm-night.css'>";
    protected final static String GFM_DARK_CSS = "<link rel='stylesheet' type='text/css' href='file:///android_asset/html/gfm-dark.css'>";
    protected final static String REVISION_CSS = "<link rel='stylesheet' type='text/css' href='file:///android_asset/html/revision.css'>";
    protected final static String CLIPBOARD_FONT_STYLE = "<style>@font-face { font-family: 'Roboto Mono'; src: url('file:///android_asset/RobotoMono-Regular.ttf') } div#content{font-family: 'Roboto Mono'; font-weight: 500;font-size: 8px}</style>";
    protected final static String CUSTOM_STYLE_SHEET = "~neutrinote_styles";

    // Canvas
    protected final static int CANVAS_MAX_COLS = 42;
    protected final static int CANVAS_MAX_ROWS = 20;
    protected final static int CANVAS_DOT_SIZE = 11;
    protected final static int CANVAS_DOT_MINI_SIZE = 4;
    protected final static char CANVAS_ON = '\u25CF';
    protected final static char CANVAS_OFF = '\u0020';
    protected final static int CANVAS_SIZE_MIN = (CANVAS_MAX_COLS + 1) * CANVAS_MAX_ROWS;
    protected final static int CANVAS_SIZE_MAX = (CANVAS_MAX_COLS + 1) * CANVAS_MAX_ROWS + 4;

    // Fragments
    protected final static String EDIT_TOOL_FRAGMENT_TAG = "edit_tool_fragment";
    protected final static String MAKRDOWN_SYMBOL_FRAGMENT_TAG= "markdown_symbol_fragment";
    protected final static String LOCAL_FIND_FRAGMENT_TAG = "local_find_fragment";
    protected final static String LOCAL_REPLACE_FRAGMENT_TAG = "local_replace_fragment";
    protected final static String DATE_PICKER_CALENDAR_VIEW_FRAGMENT_TAG = "date_picker_calendar_view_fragment";
    protected final static String DATE_PICKER_MODIFIED_FILTER_FRAGMENT_TAG = "date_picker_modified_filter_fragment";
    protected final static String DATE_PICKER_ACCESSED_FILTER_FRAGMENT_TAG = "date_picker_accessed_filter_fragment";

    // Symbols
    protected final static String ALL_SYM = "all";
    protected final static String STARRED_SYM = "starred";
    protected final static String STAR_SYM = "\uF005";
    protected final static String UNSTAR_SYM = "\uF006";
    protected final static String CLIPBOARD_SYM = "\uD83D\uDCCB";
    protected final static String HOURGLASS_SYM = "\u23F3";
    protected final static String WATCH_SYM = "\u231A";
    protected final static String LINK_SYM = "\uD83D\uDD17";
    protected final static String INVALID_SYM = "INVALID";
    protected final static String NUM_SYM = "#";
    protected final static String UP_SYM = "..";
    protected final static String RIGHT_ARROW_SYM = "\u25B6";
    protected final static String ADD_SYM = "\uF0FE";
    protected final static String REV_INSERT_SYM = "[+] ";
    protected final static String REV_DELETE_SYM = "[-] ";
    protected final static String REV_DELTA_SYM = "[*] ";
    protected final static String REV_FILE_REMOVED_SYM = "[x] ";
    protected final static String REV_LINE_SYM = "========";
    protected final static String DELIM_SYM = "\u0020\u2014\u0020";
    protected final static String ELLIPSIS_SYM = "\u0020\u2026\u0020";
    protected final static String REVERT_SYM = "\u0020\u21e0\u0020";
    protected final static String READ_SYM = "\u0020\u2014\u0020";
    protected final static String UNREAD_SYM = "\u0020\u22ef\u0020";
    protected final static String LEFT_QUOTE_SYM = "\u201c";
    protected final static String RIGHT_QUOTE_SYM = "\u201d";
    protected final static String LINE_SYM = "- - - - -";
    protected final static String DRAWING_SEPARATOR_SYM = "---\n";
    protected final static String HORIZONTAL_LINE = "\n\n=^..^=\n\n";
    protected final static String BEAR_SYM = "ʕ´•㉨•`ʔ";
    protected final static String AT_SYM = "\u0040\u0020";
    protected final static String BULLET_SYM = "\u0020\u2022\u0020";
    protected final static String SHELL_SYM = "neutriNote$";
    protected final static String WEBSERVICE_SYM = "neutriNote?";
    protected final static String WEBSERVICE_JSON_SYM = "neutriNote_json?";
    protected final static String REPLACE_SYM = "neutriNote#replace";
    protected final static String REMOVE_SYM = "neutriNote#remove";
    protected final static String LINEBREAK_SYM = "neutriNote#linebreak";
    protected final static String MORPH_SYM = "neutriNote#morph";
    protected final static String SORT_SYM = "neutriNote#sort";
    protected final static String REVERSE_SORT_SYM = "neutriNote#rsort";
    protected final static String TRIM_SYM = "neutriNote#trim";
    protected final static String REMOVE_ZERO_WIDTH_SPACE_SYM = "neutrNote#nohiddenspace";
    protected final static String ENCODE_SYM = "neutriNote#encode";
    protected final static String DECODE_SYM = "neutriNote#decode";
    protected final static String STRIP_HTML_SYM = "neutriNote#stripHTML";
    protected final static String LAUNCH_SYM = "neutriNote#launch";
    protected final static String FUNNEL_SYM = "neutriNote#funnel";
    protected final static String NEEDLE_SYM = "neutriNote#needle";
    protected final static String OVERRIDE_SYM = "neutriNote#override";
    protected final static String SYNC_SYM = "neutriNote#sync";
    protected final static String CREATE_NOTE_LINK_SYM = "neutriNote#createlink";
    protected final static String PARAMETER_SYM = "???";
    protected final static String COMMENT_SYM = "#";
    protected final static String EMPTY_SYM = " ";
    protected final static String SIZE_UP_SYM = "+ ";
    protected final static String SIZE_DOWN_SYM = "- ";
    protected final static String ANCHOR_SET_SYM = "          + \n";
    protected final static String EQUAL_SYM = " = ";
    protected final static String NULL_SYM = "";
    protected final static String CODE_FENCE_SYM = "```";
    protected final static String REVISION_DELIM = ", ";
    protected final static String NON_NUMBER_SYM = "NaN";
    protected final static String HTTP_SYM = "http://";
    protected final static String HTTPS_SYM = "https://";
    protected final static String FILE_SCHEMA_PREFIX_SHORTCUT_SYM = "file://.";

    // Built-in variables
    protected final static String TITLE_VAR = "@title";
    protected final static String CREATED_VAR = "@created";
    protected final static String MODIFIED_VAR = "@modified";
    protected final static String ACCESSED_VAR = "@accessed";
    protected final static String CLIPBOARD_VAR = "@clipboard";
    protected final static String YESTERDAY_VAR = "@yesterday";
    protected final static String TODAY_VAR = "@today";
    protected final static String TOMORROW_VAR = "@tomorrow";
    protected final static String NOW_VAR = "@now";
    protected final static String LAST_YEAR_VAR = "@year-";
    protected final static String NEXT_YEAR_VAR = "@year+";
    protected final static String LAST_MONTH_VAR = "@month-";
    protected final static String NEXT_MONTH_VAR = "@month+";
    protected final static String LAST_WEEK_VAR = "@week-";
    protected final static String NEXT_WEEK_VAR = "@week+";

    // Custom variables
    protected final static String CUSTOM_VAR_PREFIX = "@@";

    // Blank spaces
    protected final static String INDENTATION = "    ";    // 4 spaces
    protected final static char SPACE_CHAR = ' ';
    protected final static String NEWLINE = "\n";
    protected final static char NEWLINE_CHAR = '\n';
    protected final static String NEWLINE_ENTITY = "<nano:br>";
    protected final static String BLANK_LINE = "\n\n";
    protected final static String ANCHOR_MARKDOWN = "\u2758";
    protected final static String ANCHOR_MARKDOWN_HTML = "<span style='opacity:0'>" + ANCHOR_MARKDOWN + "</span>";

    // Time
    protected final static int ONE_SECOND = 1000;
    protected final static int HALF_SECOND = 500;
    protected final static int QUARTER_SECOND = 250;
    protected final static int MILISECOND = 1;
    protected final static int BACKOFF = 30 * ONE_SECOND;
    protected final static int RECENCY = 3;    // Recency in days
    protected final static int SYNC_NOISE_INTERVAL = 2 * 60 * ONE_SECOND;    // Interval with false sync signals
    protected final static int AUTO_SAVE_INTERVAL = 30;
    protected final static int AUTO_SAVE_BACKOFF = AUTO_SAVE_INTERVAL * 2;
    protected final static int AUTO_BACKUP_FREQ = 24;    // Daily
    protected final static int AUTO_MIRROR_FREQ = 1;     // Every hour
    protected final static int AUTO_RELAUNCH_DELAY = 2 * ONE_SECOND;
    protected final static int SYNC_TILE_REFRESH_PERIOD = 3 * ONE_SECOND + QUARTER_SECOND;
    protected final static int SYNC_TILE_REFRESH_DELAY = 1 * QUARTER_SECOND;
    protected final static int MIN_RELAUNCH_INTERVAL = 1;    // Minimum buffer between launch (in minute)
    protected final static int SCROLL_DELAY = ONE_SECOND;
    protected final static int REFRESH_DELAY = 300 * MILISECOND;
    protected final static int IMMERSIVE_MODE_DELAY = 3000 * MILISECOND;
    protected final static String DATE_STATUS_FORMAT = "EEE, MMM d, yyyy";
    protected final static String DATE_FORMAT = "EEE, MMM d, yyyy";
    protected final static String DATE_TIME_FORMAT = "EEEE, MMM dd, yyyy h:mm a";
    protected final static String DIRPATH_DATE_FORMAT = "yyyy-MM-dd_kk_mm_ss";
    protected final static String DIRPATH_FINE_DATE_FORMAT = "yyyy-MM-dd_hh_mm_ss_SSSS_a";
    protected final static String SNOOZE_PATTERN = "[+][0-9]+[mhdwMY]\\b";
    protected final static String SNOOZE_HOUR = "h";
    protected final static String SNOOZE_MINUTE = "m";
    protected final static String SNOOZE_DAY = "d";
    protected final static String SNOOZE_WEEK = "w";
    protected final static String SNOOZE_MONTH = "M";
    protected final static String SNOOZE_YEAR = "y";

    // Database
    protected final static int CONFLICT_POLICY = SQLiteDatabase.CONFLICT_ROLLBACK;

    // I/O
    protected final static String[] INVALID_TITLE_CHARS = {"\\", "/", "<", ">", ":", "\"", "|", "?", "*", "\n"};
    protected final static String EXPORT_PATH = "neutrinote_export";
    protected final static String INCREMENTAL_BACKUP_PATH = "merged";
    protected final static String MIRROR_PATH = "mirror";
    protected final static int MAX_BACKUP_COUNT = 10;
    protected final static int MAX_BACKUP_AGE = -1;
    protected final static String MAX_DELETED_COPIES_AGE = "-1";
    protected final static String BACKUP_WORK_NAME = "NANO_BACKUP_WORK";
    protected final static String MIRROR_WORK_NAME = "NANO_MIRROR_WORK";
    protected final static String MIRROR_WORK_TAG = "NANO_MIRROR_TAG";
    protected final static String MIRROR_ONETIME_WORK_NAME = "NANO_ONETIME_MIRROR_WORK";
    protected final static String MIRROR_ONETIME_WORK_TAG = "NANO_ONETIME_MIRROR_TAG";
    protected final static String MIRROR_INSTANT_WORK_NAME = "NANO_INSTANT_MIRROR_WORK";
    protected final static String MIRROR_INSTANT_WORK_TAG = "NANO_INSTANT_MIRROR_TAG";
    protected final static int ONE_KB = 1024;
    protected final static int BUFFER_SIZE = 100 * ONE_KB;               // 100 KB
    protected final static int MAX_FILE_SIZE = 1572864;                  // 1.5 MB
    protected final static int WARN_FILE_SIZE = 921600;                  // 900 KB
    protected final static int CRITICAL_FILE_SIZE = 1258291;             // 1.2 MB
    protected final static int SEVERE_FILE_SIZE = 1468006;               // 1.4 MB
    protected final static int MAX_LINKIFY_FILE_SIZE = 100 * 1024;       // 100 KB
    protected final static int CURSOR_SAFE_CONTENT_LEN = 200 * 1024;     // 200 KB
    protected final static int INSTANCE_SAFE_CONTENT_LEN = 200 * 1024;   // 200 KB
    protected final static int MAX_SYNC_LOG_FILE_SIZE = 200;
    protected final static int MAX_SYNC_LOG_FILE_AGE = 7;                // 1 week
    protected final static int SAFE_SEARCH_HISTORY_SIZE = 3 * 1024;      // 3 KB
    protected final static int MAX_SEARCH_HISTORY_SIZE = 5 * 1024;       // 5 KB

    protected final static String TRASH_PATH = "trash_bin";
    protected final static String CONFLICT_LABEL = "-conflict-";

    protected final static String IMPORT_ERROR_PATH = "import_errors";

    protected final static String EXPORTED_HTML_PATH = "exported_html";
    protected final static String EXPORTED_MARKDOWN_PATH = "exported_md";

    protected final static String ATTACHMENT_PATH = "attachments";

    protected final static String TMP_PATH = "tmp";
    protected final static String LOG_PATH = "log";

    protected final static String FILE_EXTENSION_HTML = ".html";
    protected final static String FILE_EXTENSION_MARKDOWN = ".md";
    protected final static String[] EXPORT_TYPES = {FILE_EXTENSION_HTML, FILE_EXTENSION_MARKDOWN};

    protected final static String NEW_NOTE_TITLE_TEMPLATE = "New Note (%)";
    protected final static String NEW_NOTE_TITLE_COUNT_SYM = "(%)";
    protected final static String NEW_NOTE_FILE_TYPE = ".txt";

    protected final static String NOOP_FILE = "~neutrinote_noop.txt";
    protected final static String SYNC_LOG_FILE = "~neutrinote_sync.log";
    protected final static String SYNC_HISTORY_FILE = "~neutrinote_sync_history.txt";

    protected final static String[] UNCACHE_FILES = {SYNC_LOG_FILE};    // Files not mirrored in database

    // Reserved folder names
    protected final static String[] RESERVED_FOLDER_NAMES = {
            EXPORT_PATH,
            INCREMENTAL_BACKUP_PATH,
            MIRROR_PATH,
            TRASH_PATH,
            IMPORT_ERROR_PATH,
            EXPORTED_HTML_PATH,
            EXPORTED_MARKDOWN_PATH,
            ATTACHMENT_PATH,
            LOG_PATH,
            TMP_PATH
    };

    // Search related
    protected final static String TITLEONLY = "title:";
    protected final static String TITLEREGONLY = "titlereg:";
    protected final static String METADATAONLY = "meta:";
    protected final static String METADATAREGONLY = "metareg:";
    protected final static String SIMILARQUERY = "similar:";
    protected final static String RELATEDQUERY = "related:";
    protected final static String TAGALLQUERY = "tag:";
    protected final static String TAGANYQUERY = "tag*:";
    protected final static String ANDQUERY = "and:";
    protected final static String ORQUERY = "or:";
    protected final static String ANDGQUERY = "andg:";
    protected final static String ORGQUERY = "org:";
    protected final static String JOINQUERY = "join:";
    protected final static String INQUERY = "in:";
    protected final static String SHOW_PATTERN = "/%";
    protected final static String HIDE_PATTERN = "~%";
    protected final static String HIDE_PATTERN_PREFIX = "~";
    protected final static String WEB_DEFINE_PATTERN_PREFIX = "define ";
    protected final static String EXCLUDE_LARGE_FILES = " LENGTH(" + DBHelper.COLUMN_CONTENT + ") < " + MAX_FILE_SIZE;    // Exclude large files
    protected final static String SEARCH_HISTORY_FILE = "~neutrinote_search_history";

    // Special criteria
    protected final static String MODIFIED_AFTER_FILTER = "modified_after_filter";
    protected final static String ACCESSED_AFTER_FILTER = "accessed_after_filter";
    protected final static String MODIFIED_NEARBY_FILTER = "modified_nearby_filter";

    // Undo/redo
    protected final static int MAX_SNAPSHOTS = 25;

    // Import/export
    protected final static String DELIMITER = "\n";
    protected final static String SUBDELIMITER = ":";
    protected final static String SETTINGS_DELIMITER = "|";
    protected final static String APP_DATA_FILE = "~neutrinote_app_data";
    protected final static String APP_SETTINGS_FILE = "~neutrinote_settings_data";

    // Font
    protected final static String CUSTOM_FONTS_FILE = "~neutrinote_fonts";
    protected final static String CUSTOM_FONTS_PATH = "fonts";

    // Shortcuts
    protected final static String SHORTCUTS_FILE = "~neutrinote_shortcuts";
    protected final static String SHORTCUTS_DELIMITER = "|";
    protected final static String SHORTCUTS_PARAMS_DELIMITER = " ,";
    protected final static String SHORTCUTS_PATTERN_DELIMITER = " ";

    // Colordict related
    protected final static int COLORDICT_POPUP_DIM_X = 600;
    protected final static String COLORDICT_SEARCH_ACTION = "colordict.intent.action.SEARCH";
    protected final static String COLORDICT_EXTRA_QUERY = "EXTRA_QUERY";
    protected final static String COLORDICT_EXTRA_FULLSCREEN = "EXTRA_FULLSCREEN";
    protected final static String COLORDICT_EXTRA_HEIGHT = "EXTRA_HEIGHT";
    protected final static String COLORDICT_EXTRA_WIDTH = "EXTRA_WIDTH";
    protected final static String COLORDICT_EXTRA_GRAVITY = "EXTRA_GRAVITY";
    protected final static String COLORDICT_EXTRA_MARGIN_LEFT = "EXTRA_MARGIN_LEFT";
    protected final static String COLORDICT_EXTRA_MARGIN_TOP = "EXTRA_MARGIN_TOP";
    protected final static String COLORDICT_EXTRA_MARGIN_BOTTOM = "EXTRA_MARGIN_BOTTOM";
    protected final static String COLORDICT_EXTRA_MARGIN_RIGHT = "EXTRA_MARGIN_RIGHT";

    // Open Key Chain related
    protected final static String OKC_PACKAGE_NAME = "org.sufficientlysecure.keychain";
    protected final static String OKC_INTENT_PREFIX = OKC_PACKAGE_NAME + ".action.";
    protected final static String OKC_EXTRA_PREFIX = OKC_PACKAGE_NAME + ".";
    protected final static String OKC_ENCRYPT_TEXT = OKC_INTENT_PREFIX + "ENCRYPT_TEXT";
    protected final static String OKC_ENCRYPT_EXTRA_TEXT = OKC_EXTRA_PREFIX + "EXTRA_TEXT";
    protected final static String OKC_DECRYPT_ACTIVITY = OKC_PACKAGE_NAME + ".ui.DecryptActivity";
    protected final static String OKC_BEGIN_SYM = "-----BEGIN PGP MESSAGE-----";
    protected final static String OKC_END_SYM = "-----END PGP MESSAGE-----";

    // Location
    protected final static String LEAFLET_JS = "https://unpkg.com/leaflet@1.7.1/dist/leaflet.js";
    protected final static String LEAFLET_CSS = "https://unpkg.com/leaflet@1.7.1/dist/leaflet.css";
    protected final static double NEARBY = 100;
    protected final static int MAP_WIDTH = 300;
    protected final static int MAP_HEIGHT = 300;
    protected final static int MAP_DEFAULT_ZOOM = 7;

    // MIME
    protected final static String PLAIN_TEXT_TYPE = "text/plain";
    protected final static String IMAGE_JPEG_TYPE = "image/jpeg";
    protected final static String PREFIX_FILE = "file:///";
    protected final static String PREFIX_ASSET = "file:///android_asset/";

    // File type
    protected final static String MULTI_TYPE = "~neutrinote_multitype.txt";
    protected final static String MULTI_TYPE_TITLE = "~neutrinote_multitype";

    // Request codes
    protected final static int REQUEST_CODE_LOCATION_PERMISSION = 123;
    protected final static int REQUEST_CODE_STORAGE_PERMISSIONS = 124;
    protected final static int REQUEST_CODE_CAMERA_PERMISSION = 125;
    protected final static int REQUEST_CODE_PICK_BACKUP_URI = 126;
    protected final static int REQUEST_CODE_PICK_RESTORE_URI = 127;
    protected final static int REQUEST_CODE_GET_IMAGE = 10;
    protected final static int REQUEST_CODE_INSERT_GALLERY_IMAGE = 11;
    protected final static int REQUEST_CODE_INSERT_CAMERA_IMAGE = 12;

    // Voice memo
    protected final static String VOICE_MEMO_TITLE = "neutriNote memo";

    // Local find and replace
    protected final static int LOCAL_FIND_CACHE_SIZE = 1024;    // In bytes
    protected final static int LOCAL_REPLACE_CACHE_SIZE = 1024;    // In bytes
    protected final static int NUM_HITS_THRESHOLD = 5;    // Number of hits to trigger replace all

    // In-note navigation
    protected final static int[] IN_NOTE_PERCENT_VALUES = {
            0,
            10,
            20,
            30,
            40,
            50,
            60,
            70,
            80,
            90,
            100
    };

    protected final static int IN_NOTE_MARKDOWN_MAX_PERCENT = 200;
    protected final static int IN_NOTE_MARKDOWN_LOCAL_FIND_ID = -1;

    // Working set navigation
    protected final static int WORKING_SET_SIZE = 6;

    // Text expansion
    protected final static int MAX_EXTRA_LEN = 128;    // In bytes, to ensure safe code

    // Image
    protected final static String TMP_IMAGE = "neutrinote_tmp.jpg";    // Temporary image

    // OCR
    protected final static String OCR_PACKAGE_NAME = "io.github.subhamtyagi.ocr";

    // Process text modes
    protected final static int PROCESS_TEXT_DISABLED = 0;
    protected final static int PROCESS_TEXT_PASTE = 1;
    protected final static int PROCESS_TEXT_SEARCH = 2;

    // Notification
    protected final static String BACKUP_CHANNEL_ID = "backup";
    protected final static String BACKUP_CHANNEL_NAME = "Backup";
    protected final static String BACKUP_CHANNEL_DESC = "Backup Notification";
    protected final static int BACKUP_CHANNEL_LEVEL = NotificationManager.IMPORTANCE_DEFAULT;

    // Error
    protected final static int ERROR_UNEXPECTED = 2;    // Unexpected error code
}