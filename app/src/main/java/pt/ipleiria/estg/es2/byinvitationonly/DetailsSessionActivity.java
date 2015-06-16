package pt.ipleiria.estg.es2.byinvitationonly;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Database.DBAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;


public class DetailsSessionActivity extends MyBaseActivity {
    private RatingBar myRatingBar;
    private Session session;
    private CheckBox checkBox;
    private BroadcastReceiver broadcastReceiver;
    private Context context;
    private DBAdapter dbAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Intent i = getIntent();
        this.dbAdapter = new DBAdapter(getApplicationContext());
        session = (Session) i.getSerializableExtra(EXTRA_SESSION);
        isChecked = i.getBooleanExtra(EXTRA_ISCHECKED, false);
        this.context = this;
        loadData();

        myRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    if (NetworkController.existConnection(getApplicationContext())) {
                        session.setMyRating(myRatingBar.getRating());
                        FirebaseController.sendSessionRating(session, SharedPreferenceController.getUserID(getApplicationContext()));
                        if (dbAdapter.existsSessionOnAgenda(session)) {
                            dbAdapter.updateSession(session);
                        }
                    } else {
                        showConnectivityError();
                        ratingBar.setRating(session.getMyRating());
                    }
                }
            }
        });

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                session.setOnAgenda(checkBox.isChecked());
                if (checkBox.isChecked()) {
                    dbAdapter.addSession(session);
                } else {
                    dbAdapter.removeSession(session);
                }
            }
        });

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    refreshRatingOnChangedState();
                }
            }

            private void refreshRatingOnChangedState() {
                View view = findViewById(R.id.ratingBarDetails);
                int visibility = view.getVisibility();

                if (session.hasBegun() && visibility != View.VISIBLE) {
                    view.setVisibility(View.VISIBLE);
                } else if (!session.hasBegun() && visibility == View.VISIBLE) {
                    view.setVisibility(View.INVISIBLE);
                }
            }
        };

        this.registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private void loadData() {
        TextView textViewTittle = (TextView) this.findViewById(R.id.text_view_session_details_title);
        TextView textViewDate = (TextView) this.findViewById(R.id.textView_details_date);
        TextView textViewStartHour = (TextView) this.findViewById(R.id.textView_details_strat_hour);
        TextView textViewEndHour = (TextView) this.findViewById(R.id.textView_details_hour_end);
        TextView textViewRoom = (TextView) this.findViewById(R.id.textView_details_room);
        TextView textViewTrack = (TextView) this.findViewById(R.id.textView_details_track);
        TextView textViewPresenter = (TextView) this.findViewById(R.id.textView_details_presenter);
        TextView textViewAbstract = (TextView) this.findViewById(R.id.textView_details_abstract);
        myRatingBar = (RatingBar) this.findViewById(R.id.ratingBarDetails);
        checkBox = (CheckBox) this.findViewById(R.id.checkBoxDetails);
        textViewTittle.setText(session.getTitle());
        textViewDate.setText(session.getDateFormattedString());
        textViewStartHour.setText(session.getStartHour());
        textViewEndHour.setText(session.getEndHour());
        textViewRoom.setText(session.getRoom());
        textViewTrack.setText(session.getTrack());
        textViewPresenter.setText(session.getPresenter());
        textViewAbstract.setText(session.getAbstracts());
        if (session.hasBegun()) {
            myRatingBar.setVisibility(View.VISIBLE);
        } else {
            myRatingBar.setVisibility(View.INVISIBLE);
        }
        myRatingBar.setRating(session.getMyRating());
        checkBox.setChecked(session.isOnAgenda());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.drawer, menu);
        itemImHere = menu.findItem(R.id.action_i_am_here);
        menu.findItem(R.id.action_i_am_here).setIcon(isChecked ? R.drawable.ic_action_group : R.drawable.ic_action_alone);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!isChecked)
            setResult(RESULT_CANCELED, getIntent());
        else
            setResult(RESULT_FIRST_USER, getIntent());
        super.onBackPressed();
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
    protected void changeActiveSectionFragment(int ARG_SECTION_NUMBER, int fragmentTitleId) {
        setResult(RESULT_OK, getIntent());
        this.finish();
    }

    private void showConnectivityError() {
        AlertDialog.Builder construct = new AlertDialog.Builder(this);
        construct.setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.error_connectivity))
                .setNeutralButton(R.string.ok, null)
                .create()
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null)
            this.unregisterReceiver(broadcastReceiver);
    }

    public Session getSession() {
        return this.session;
    }
}
