package pt.ipleiria.estg.es2.byinvitationonly;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.MenuItem;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.WhoIsHereFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;


public abstract class MyBaseActivity extends Activity {
    public static final int CONTACT_ORDER = 1;
    public static final String EXTRA_ISCHECKED = "isChecked";
    public static final String EXTRA_SESSION = "session";
    public static final String EXTRA_FRAG = "frag";
    protected boolean isChecked = false;
    protected MenuItem itemImHere;
    protected Contact myContact;
    private boolean tryCheckIn = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACT_ORDER) {
            myContact = SharedPreferenceController.getLocalStoredUserContact(MyBaseActivity.this);
            if (tryCheckIn) {
                if (resultCode == RESULT_OK) {
                    showConfirmWindow();
                } else {
                    setChecked(false);
                }
            } else {
                if (myContact.isValid()) {
                    FirebaseController.sendContactData(myContact, this);
                } else {
                    setChecked(false);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_i_am_here:
                invertCheckIn();
                return true;
            case R.id.action_settings:
                tryCheckIn = false;
                openSettingsActivity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void invertCheckIn() {
        if (!isChecked) {
            tryCheckIn = true;
            myContact = SharedPreferenceController.getLocalStoredUserContact(this);
            if (myContact != null && !myContact.getName().isEmpty() && !myContact.getEmail().isEmpty()) {
                showConfirmWindow();
            } else {
                showOrderFill();
            }
        } else {
            tryCheckIn = false;
            setChecked(false);
        }
    }

    protected abstract void changeActiveSectionFragment(int ARG_SECTION_NUMBER, int fragmentTitleId);

    private void showConfirmWindow() {
        AlertDialog.Builder adConstruct = new AlertDialog.Builder(this);
        adConstruct.setTitle(getString(R.string.confirmation))
                .setMessage(getString(R.string.ad_confirmShare) +
                        "\n" + getString(R.string.name) + ": " + myContact.getName() +
                        "\n" + getString(R.string.email) + ": " + myContact.getEmail())
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (NetworkController.existConnection(MyBaseActivity.this)) {
                            setChecked(true);
                            FirebaseController.sendContactData(myContact, MyBaseActivity.this);
                            changeActiveSectionFragment(WhoIsHereFragment.ARG_SECTION_NUMBER,
                                    R.string.title_who_is_here_fragment);
                        } else {
                            showConnectivityError();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .create()
                .show();
    }

    protected void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
        SharedPreferenceController.saveImHereSharedPref(MyBaseActivity.this, isChecked);
        itemImHere.setIcon(isChecked ? R.drawable.ic_action_group : R.drawable.ic_action_alone);
        if (!isChecked) {
            FirebaseController.removeContactOnServer(this);
            FirebaseController.cancelEventNotification(getApplicationContext());
        } else {
            FirebaseController.getMessages(getApplicationContext());
        }
    }

    private void showConnectivityError() {
        AlertDialog.Builder construct = new AlertDialog.Builder(this);
        construct.setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.error_connectivity))
                .setNeutralButton(getString(R.string.ok), null)
                .create()
                .show();
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(getApplicationContext(), ContactSettingsActivity.class);
        intent.putExtra(EXTRA_ISCHECKED, isChecked);
        startActivityForResult(intent, CONTACT_ORDER);
    }

    private void showOrderFill() {
        AlertDialog.Builder construtor = new AlertDialog.Builder(this);
        construtor.setTitle(getString(R.string.incomplete_data))
                .setMessage(getString(R.string.ad_incomplete_data))
                .setPositiveButton(R.string.yes,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                openSettingsActivity();
                            }
                        })
                .setNegativeButton(getString(R.string.cancel), null)
                .create()
                .show();
    }

}
