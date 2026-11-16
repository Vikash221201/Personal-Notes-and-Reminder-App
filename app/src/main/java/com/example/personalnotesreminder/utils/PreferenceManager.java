package com.example.personalnotesreminder.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

import androidx.appcompat.app.AppCompatDelegate;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PreferenceManager {

    private static final String PREF_NAME = "remindly_settings";

    public static final String KEY_THEME = "key_theme";
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_SYSTEM = 2;

    public static final String KEY_TIME_FORMAT = "key_time_format";
    public static final String FORMAT_12H = "12h";
    public static final String FORMAT_24H = "24h";

    public static final String KEY_REMINDER_SOUND_TYPE = "key_reminder_sound_type"; // "default", "alarm", "notification", "custom"
    public static final String KEY_REMINDER_SOUND_URI = "key_reminder_sound_uri";
    public static final String KEY_REMINDER_VOLUME = "key_reminder_volume"; // 0-100

    private final SharedPreferences prefs;

    public PreferenceManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Theme Management
    public int getThemeMode() {
        return prefs.getInt(KEY_THEME, THEME_SYSTEM);
    }

    public void setThemeMode(int themeMode) {
        prefs.edit().putInt(KEY_THEME, themeMode).apply();
    }

    public static void applyTheme(int themeMode) {
        switch (themeMode) {
            case THEME_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case THEME_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case THEME_SYSTEM:
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
        }
    }

    public void applySavedTheme() {
        applyTheme(getThemeMode());
    }

    // Time Format Management
    public String getTimeFormat() {
        return prefs.getString(KEY_TIME_FORMAT, FORMAT_12H);
    }

    public void setTimeFormat(String format) {
        prefs.edit().putString(KEY_TIME_FORMAT, format).apply();
    }

    public boolean is24HourFormat() {
        return FORMAT_24H.equals(getTimeFormat());
    }

    public String formatTime(long timestamp) {
        if (timestamp <= 0) return "";
        String pattern = is24HourFormat() ? "HH:mm" : "hh:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String formatDate(long timestamp) {
        if (timestamp <= 0) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    public String formatDateTime(long timestamp) {
        if (timestamp <= 0) return "";
        String timePattern = is24HourFormat() ? "HH:mm" : "hh:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, " + timePattern, Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    // Reminder Audio Settings
    public String getSoundType() {
        return prefs.getString(KEY_REMINDER_SOUND_TYPE, "default");
    }

    public void setSoundType(String type) {
        prefs.edit().putString(KEY_REMINDER_SOUND_TYPE, type).apply();
    }

    public String getSoundUri() {
        return prefs.getString(KEY_REMINDER_SOUND_URI, null);
    }

    public void setSoundUri(String uri) {
        prefs.edit().putString(KEY_REMINDER_SOUND_URI, uri).apply();
    }

    public Uri getActualSoundUri(Context context) {
        String customUri = getSoundUri();
        if (customUri != null && !customUri.isEmpty()) {
            return Uri.parse(customUri);
        }
        String type = getSoundType();
        if ("alarm".equals(type)) {
            Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            if (alarmUri != null) return alarmUri;
        }
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
    }

    public int getVolume() {
        return prefs.getInt(KEY_REMINDER_VOLUME, 80);
    }

    public void setVolume(int volume) {
        prefs.edit().putInt(KEY_REMINDER_VOLUME, volume).apply();
    }
}
