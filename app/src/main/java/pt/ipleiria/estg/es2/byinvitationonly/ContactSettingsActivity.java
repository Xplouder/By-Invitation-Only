package pt.ipleiria.estg.es2.byinvitationonly;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;

public class ContactSettingsActivity extends PreferenceActivity {

    private boolean isChecked = false;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        isChecked = intent.getBooleanExtra(MainActivity.EXTRA_ISCHECKED, false);
        addPreferencesFromResource(R.xml.contact_preferences);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            this.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String name = prefs.getString(getString(R.string.pref_key_name), "");
        String email = prefs.getString(getString(R.string.pref_key_email), "");
        if (name.isEmpty() || email.isEmpty()) {
            if (!isChecked) {
                setResult(RESULT_CANCELED);
                super.onBackPressed();
            } else {
                AlertDialog.Builder construtor = new AlertDialog.Builder(this);
                construtor.setTitle(getString(R.string.confirmation))
                        .setMessage(getString(R.string.ad_disableSharing))
                        .setPositiveButton(getString(R.string.disableSharing), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ContactSettingsActivity.super.onBackPressed();
                            }
                        })
                        .setNegativeButton(getString(R.string.back), null)
                        .create()
                        .show();
            }
        } else {
            setResult(RESULT_OK, intent);
            super.onBackPressed();
        }
    }

}
