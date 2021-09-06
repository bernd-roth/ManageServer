package at.co.netconsulting.manageactivity.general;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceModel {
    private Context _context;
    private SharedPreferences preferences;

    public SharedPreferenceModel(Context context){
        this._context = context;
    }

    public boolean getBooleanSharedPreference(String key){
        preferences = this._context.getSharedPreferences(key, android.content.Context.MODE_PRIVATE);
        return preferences.getBoolean(key, false);
    }

    public String getStringSharedPreference(String key){
        preferences = this._context.getSharedPreferences(key, android.content.Context.MODE_PRIVATE);
        return preferences.getString(key, null);
    }

    public float getFloatSharedPreference(String key){
        preferences = this._context.getSharedPreferences(key, android.content.Context.MODE_PRIVATE);
        return preferences.getFloat(key, 0);
    }

    public int getIntSharedPreference(String key){
        preferences = this._context.getSharedPreferences(key, android.content.Context.MODE_PRIVATE);
        return preferences.getInt(key, 0);
    }

    public void saveBooleanSharedPreference(String key, boolean value){
        preferences = this._context.getSharedPreferences(key, android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(key, value);
        editor.commit();
    }

    public void saveStringSharedPreference(String key, String value){
        preferences = this._context.getSharedPreferences(key, android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public void saveFloatSharedPreference(String key, float value){
        preferences = this._context.getSharedPreferences(key, android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putFloat(key, value);
        editor.commit();
    }

    public void saveIntSharedPreference(String key, int value){
        preferences = this._context.getSharedPreferences(key, android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(key, value);
        editor.commit();
    }

    public void deleteSharedPreference(String key){
        preferences = this._context.getSharedPreferences(key, android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(key);
        editor.apply();
    }
}