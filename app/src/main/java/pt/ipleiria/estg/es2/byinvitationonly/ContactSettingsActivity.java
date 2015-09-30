package pt.ipleiria.estg.es2.byinvitationonly;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;


public class ContactSettingsActivity extends PreferenceActivity {

    private boolean isChecked = false;
    private Intent intent;
    private LoginButton loginButton;
    private CallbackManager callbackManager;
    private updateUserInformationListener updateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        intent = getIntent();
        isChecked = intent.getBooleanExtra(MainActivity.EXTRA_ISCHECKED, false);
        MyPreferenceFragment mpf = new MyPreferenceFragment();
        updateListener = mpf;

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, mpf)
                .commit();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        MenuItem menuItem = menu.findItem(R.id.login_button);
        loginButton = (LoginButton) menuItem.getActionView();
        loginButton.setReadPermissions("email");
        defineFacebookCallbacks(this);
        return super.onCreateOptionsMenu(menu);
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

    private void defineFacebookCallbacks(final Context context) {
        if (loginButton != null) {
            callbackManager = CallbackManager.Factory.create();
            // Callback registration
            loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                            new GraphRequest.GraphJSONObjectCallback() {
                                @Override
                                public void onCompleted(JSONObject object, GraphResponse response) {
                                    try {
                                        updateListener.updateUserInformation(Profile.getCurrentProfile().getName(),
                                                object.getString("email"));
                                        Toast.makeText(context, "Login Successful", Toast.LENGTH_LONG).show();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                    Bundle parameters = new Bundle();
                    parameters.putString("fields", "email");
                    request.setParameters(parameters);
                    request.executeAsync();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(context, "Login Cancelled", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(FacebookException exception) {
                    Toast.makeText(context, "Error on Login", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public interface updateUserInformationListener {
        void updateUserInformation(String name, String email);
    }


}
