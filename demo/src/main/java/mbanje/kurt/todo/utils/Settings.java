/*
 * Copyright (c) 2015. Peirr, Inc - All Rights Reserved.
 * Unauthorized copying of this file, via any means is strictly prohibited.
 * Proprietary and Confidential
 */

package mbanje.kurt.todo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by kurt on 2014/11/26.
 * Streamlines calls to sharedpreferences to reduce amount of code duplication
 */
public class Settings {

    public static final String PREF_DATABASE = "database";

    public enum SettingsPreferenceMode {
        SHARED, DEFAULT_SHARED
    }

    public static final String PREFS_NAME = "peirr_workout";
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private SettingsPreferenceMode mode;
    private SharedPreferences.OnSharedPreferenceChangeListener listener;

    public Settings(Context ctx) {
        super();
        setPreferenceMode(ctx, SettingsPreferenceMode.SHARED);
    }

    public Settings(Context ctx, SettingsPreferenceMode mode) {
        super();
        setPreferenceMode(ctx, mode);
    }

    public Settings(Context ctx, SharedPreferences.OnSharedPreferenceChangeListener listener) {
        super();
        this.listener = listener;
        setPreferenceMode(ctx, SettingsPreferenceMode.SHARED);
    }


    public void setPreferenceMode(Context context, SettingsPreferenceMode mode) {
        switch (mode) {
            case SHARED:
                settings = context.getSharedPreferences(PREFS_NAME, Context.MODE_MULTI_PROCESS);
                break;
            case DEFAULT_SHARED:
                settings = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
                break;
        }
        if (listener != null) {
            settings.registerOnSharedPreferenceChangeListener(listener);
        }
    }


    public void commit() {
        if (editor != null) {
            editor.apply();
        }
    }

    public void clear() {
        if (editor != null) {
            editor.clear();
            editor.apply();
        }
    }

    public boolean getBoolean(String string) {
        return settings.getBoolean(string, false);
    }

    public boolean getBoolean(String string, boolean defaultValue) {
        return settings.getBoolean(string, defaultValue);
    }

    public void setBoolean(String name, boolean value, boolean... commit) {
        if (editor == null) {
            editor = settings.edit();
        }
        editor.putBoolean(name, value);
        if (commit != null && commit.length > 0) {
            if (commit[0]) {
                editor.apply();
            }
        } else {
            editor.apply();
        }
    }

    public String getString(String s) {
        return settings.getString(s, null);
    }

    public String getString(String s, String defaultValue) {
        return settings.getString(s, defaultValue);
    }

    public void setString(String name, String value, boolean... commit) {
        if (editor == null) {
            editor = settings.edit();
        }
        editor.putString(name, value);
        if (commit != null && commit.length > 0) {
            if (commit[0]) {
                editor.apply();
            }
        } else {
            editor.apply();
        }
    }

    public Set<String> getStringSet(String name) {
        return settings.getStringSet(name, new HashSet<String>());
    }

    public Set<String> getStringSet(String name, HashSet<String> defaultValue) {
        return settings.getStringSet(name, defaultValue);
    }

    public void setStringSet(String name, Set<String> value, boolean... commit) {
        if (editor == null) {
            editor = settings.edit();
        }
        editor.putStringSet(name, value);
        if (commit != null && commit.length > 0) {
            if (commit[0]) {
                editor.apply();
            }
        } else {
            editor.apply();
        }
    }


    public int getInt(String i) {
        return settings.getInt(i, -1);
    }

    public int getInt(String i, int defaultValue) {
        return settings.getInt(i, defaultValue);
    }

    public void setInt(String name, int value, boolean... commit) {
        if (editor == null) {
            editor = settings.edit();
        }
        editor.putInt(name, value);
        if (commit != null && commit.length > 0) {
            if (commit[0]) {
                editor.apply();
            }
        } else {
            editor.apply();
        }
    }

    public long getLong(String i) {
        return settings.getLong(i, -1);
    }

    public long getLong(String i, long defaultValue) {
        return settings.getLong(i, defaultValue);
    }

    public void setLong(String name, long value, boolean... commit) {
        if (editor == null) {
            editor = settings.edit();
        }
        editor.putLong(name, value);
        if (commit != null && commit.length > 0) {
            if (commit[0]) {
                editor.apply();
            }
        } else {
            editor.apply();
        }
    }


    public static class Builder {

        private Settings settings;

        public Builder(Context context, SettingsPreferenceMode mode) {
            settings = new Settings(context, mode);
        }

        public Builder setLong(String name, long value) {
            settings.setLong(name, value, false);
            return this;
        }

        public Builder setInt(String name, int value) {
            settings.setInt(name, value, false);
            return this;
        }

        public Builder setStringSet(String name, Set<String> value, boolean... commit) {
            settings.setStringSet(name, value, false);
            return this;
        }

        public Builder setString(String name, String value, boolean... commit) {
            settings.setString(name, value, false);
            return this;
        }

        public Builder setBoolean(String name, boolean value) {
            settings.setBoolean(name, value, false);
            return this;
        }

        public void build() {
            settings.commit();
        }
    }


}