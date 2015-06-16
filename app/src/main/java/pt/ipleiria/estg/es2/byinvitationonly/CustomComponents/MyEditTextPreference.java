package pt.ipleiria.estg.es2.byinvitationonly.CustomComponents;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;

import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MyEditTextPreference extends EditTextPreference {

    public MyEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        loadContactSettings();
        defineListener();
    }

    public MyEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        loadContactSettings();
        defineListener();
    }

    public MyEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadContactSettings();
        defineListener();
    }

    public MyEditTextPreference(Context context) {
        super(context);
        loadContactSettings();
        defineListener();
    }


    private void loadContactSettings() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext());
        changeName(pref.getString(getContext().getString(R.string.pref_key_name), ""));
        changeEmail(pref.getString(getContext().getString(R.string.pref_key_email), ""));
    }

    /*@Override
    public CharSequence getSummary() {
        if (getEditText().getText().toString().isEmpty())
            return super.getSummary();
        else
            return getEditText().getText().toString();
    }*/

    private void defineListener() {
        setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                changeName((String) newValue);
                changeEmail((String) newValue);
                return true;
            }
        });
    }

    private void changeName(String newString) {
        if (getKey().equals(getContext().getString(R.string.pref_key_name))) {
            setSummary(newString.isEmpty() ? getContext().getString(R.string.request_fill_name) : newString);
        }
    }

    private void changeEmail(String newString) {
        if (getKey().equals(getContext().getString(R.string.pref_key_email))) {
            setSummary(newString.isEmpty() ? getContext().getString(R.string.request_fill_email) : newString);
        }
    }

}
