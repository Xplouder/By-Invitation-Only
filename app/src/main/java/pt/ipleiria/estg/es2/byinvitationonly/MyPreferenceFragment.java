package pt.ipleiria.estg.es2.byinvitationonly;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

public class MyPreferenceFragment extends PreferenceFragment implements ContactSettingsActivity.updateUserInformationListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.contact_preferences);
    }


    @Override
    public void updateUserInformation(String name, String email) {
        Preference preferenceName = getPreferenceManager().findPreference(getString(R.string.pref_key_name));
        preferenceName.getEditor().putString(getString(R.string.pref_key_name), name).commit();

        Preference preferenceEmail = getPreferenceManager().findPreference(getString(R.string.pref_key_email));
        preferenceEmail.getEditor().putString(getString(R.string.pref_key_email), email).commit();

        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.contact_preferences);
    }
}
