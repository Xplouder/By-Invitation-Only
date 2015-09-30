package pt.ipleiria.estg.es2.byinvitationonly;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Spinner;

import com.facebook.appevents.AppEventsLogger;

import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.FilterSpinnerInteractionListener;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.NavigationDrawerFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ActiveSessionsFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.AgendaFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ConferenceScheduleFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.HomepageFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.StatisticsFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.WhoIsHereFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Interfaces.DrawerItemSwitcher;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;


public class MainActivity extends MyBaseActivity implements
        NavigationDrawerFragment.NavigationDrawerCallbacks,
        ConferenceScheduleFragment.OnFragmentInteractionListener,
        HomepageFragment.OnFragmentInteractionListener,
        WhoIsHereFragment.OnFragmentInteractionListener,
        ActiveSessionsFragment.OnFragmentInteractionListener,
        AgendaFragment.OnFragmentInteractionListener,
        StatisticsFragment.OnFragmentInteractionListener {

    public static final int DETAIL_SESSION = 2;
    public static final String EXTRA_NOTIFICATION = "notification";
    private Spinner spinnerFilter;
    private ConferenceScheduleFragment conferenceFrag;
    private DrawerItemSwitcher mDrawerItemSwitcher;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        isChecked = SharedPreferenceController.isImHereActive(MainActivity.this);

        mNavigationDrawerFragment.setOnAdapterWhoIsHere(isChecked);
        mDrawerItemSwitcher = mNavigationDrawerFragment;

        Intent i = getIntent();
        boolean notification = i.getBooleanExtra(EXTRA_NOTIFICATION, false);
        if (isChecked && !notification) {
            FirebaseController.getMessages(getApplicationContext());
        }
        if (notification) {
            final List<String> list = (List<String>) i.getSerializableExtra("lista");
            final LinkedList<Contact> contacts = new LinkedList<>();

            for (String l : list) {
                contacts.add(getContactFromBytes(l));
            }

            changeActiveSectionFragment(WhoIsHereFragment.ARG_SECTION_NUMBER, R.string.title_who_is_here_fragment);

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
            builderSingle.setIcon(R.mipmap.ic_launcher);
            builderSingle.setTitle(getString(R.string.choose_who));
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1);
            for (Contact c : contacts) {
                arrayAdapter.add(c.getName());
            }
            builderSingle.setNegativeButton(getString(R.string.cancel), null);

            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String strName = arrayAdapter.getItem(which);
                            Contact contact = new Contact();
                            for (Contact c : contacts) {
                                if (c.getName().contentEquals(strName)) {
                                    contact = c;
                                    break;
                                }
                            }
                            Intent intent = new Intent(getApplicationContext(), ContactChatActivity.class);
                            intent.putExtra(ContactChatActivity.EXTRA_CONTACT, contact);
                            startActivity(intent);
                        }
                    });
            builderSingle.show();
        }
    }

    private Contact getContactFromBytes(String l) {
        String[] aux = l.split("__");
        String[] auxEmail = aux[0].split("_");
        String[] auxName = aux[1].split("_");
        byte[] eB = new byte[auxEmail.length];
        byte[] eN = new byte[auxName.length];
        int i = 0;
        for (String x : auxEmail) {
            eB[i++] = Byte.valueOf(x);
        }
        i = 0;
        for (String x : auxName) {
            eN[i++] = Byte.valueOf(x);
        }
        return new Contact(new String(eN, StandardCharsets.US_ASCII), new String(eB, StandardCharsets.US_ASCII));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment frag;
        if (isChecked) {
            if (mNavigationDrawerFragment.imStaff()) {
                frag = navigationDrawerSwitchWithWISWithStatistics(position);
            } else {
                frag = navigationDrawerSwitchWithWIS(position);
            }
        } else {
            frag = navigationDrawerSwitchWithoutWIS(position);
        }

        if (frag != null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .replace(R.id.container, frag)
                    .commit();
        }
    }

    private Fragment navigationDrawerSwitchWithoutWIS(int position) {
        Fragment frag;
        switch (position) {
            case 0:
                frag = HomepageFragment.newInstance();
                break;
            case 1:
                frag = ConferenceScheduleFragment.newInstance();
                conferenceFrag = (ConferenceScheduleFragment) frag;
                break;
            case 2:
                frag = ActiveSessionsFragment.newInstance();
                break;
            case 3:
                frag = AgendaFragment.newInstance();
                break;
            default:
                return null;
        }
        return frag;
    }

    private Fragment navigationDrawerSwitchWithWIS(int position) {
        Fragment frag;
        switch (position) {
            case 0:
                frag = HomepageFragment.newInstance();
                break;
            case 1:
                frag = ConferenceScheduleFragment.newInstance();
                conferenceFrag = (ConferenceScheduleFragment) frag;
                break;
            case 2:
                frag = WhoIsHereFragment.newInstance();
                break;
            case 3:
                frag = ActiveSessionsFragment.newInstance();
                break;
            case 4:
                frag = AgendaFragment.newInstance();
                break;
            default:
                return null;
        }
        return frag;
    }

    private Fragment navigationDrawerSwitchWithWISWithStatistics(int position) {

        switch (position) {
            case 0:
                return HomepageFragment.newInstance();
            case 1:
                Fragment frag = ConferenceScheduleFragment.newInstance();
                conferenceFrag = (ConferenceScheduleFragment) frag;
                return frag;
            case 2:
                return WhoIsHereFragment.newInstance();
            case 3:
                return ActiveSessionsFragment.newInstance();
            case 4:
                return AgendaFragment.newInstance();
            case 5:
                if (!NetworkController.existConnection(this)) {
                    showConnectivityError();
                } else {
                    return StatisticsFragment.newInstance();
                }
                break;
            default:
                return null;
        }
        return null;
    }

    @Override
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_homepage_fragment);
                break;
            case 2:
                mTitle = getString(R.string.title_conference_schedule_fragment);
                break;
            case 3:
                mTitle = getString(R.string.title_who_is_here_fragment);
                break;
            case 4:
                mTitle = getString(R.string.title_active_session_fragment);
                break;
            case 5:
                mTitle = getString(R.string.title_agenda_fragment);
                break;
            case 6:
                mTitle = getString(R.string.title_statistics_fragment);
                break;
        }
    }

    @Override
    public boolean getChecked() {
        return isChecked;
    }

    @Override
    protected void setChecked(boolean isChecked) {
        super.setChecked(isChecked);
        mNavigationDrawerFragment.setOnAdapterWhoIsHere(isChecked);
    }

    @Override
    public void resetSpinnerItemPosition() {
        if (spinnerFilter != null) {
            spinnerFilter.setSelection(0);
        }
    }

    @Override
    public void updateActivityTitle(String title) {
        mTitle = title;
        restoreActionBar();
    }

    private void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(mTitle);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            if (mTitle != null && mTitle.equals(getString(R.string.title_conference_schedule_fragment))) {
                //Troca o menu
                getMenuInflater().inflate(R.menu.drawer_with_filter, menu);
                // Ativa opções de pesquisa
                MenuItem auxItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) auxItem.getActionView();
                defineSearchListeners(searchView);
                // Ativa o spinner de filtragem
                auxItem = menu.findItem(R.id.action_filter);
                spinnerFilter = (Spinner) auxItem.getActionView();
                ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                        R.array.spinner_filter_array, R.layout.custom_simple_spinner_dropdown_item);
                spinnerFilter.setAdapter(spinnerAdapter);
                FilterSpinnerInteractionListener listener = new FilterSpinnerInteractionListener(this, conferenceFrag);
                spinnerFilter.setOnTouchListener(listener);
                spinnerFilter.setOnItemSelectedListener(listener);
            } else {
                getMenuInflater().inflate(R.menu.drawer, menu);
            }
            itemImHere = menu.findItem(R.id.action_i_am_here);
            menu.findItem(R.id.action_i_am_here).setIcon(isChecked ? R.drawable.ic_action_group : R.drawable.ic_action_alone);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DETAIL_SESSION) {
            if (resultCode == RESULT_OK) {
                setChecked(true);
                changeActiveSectionFragment(WhoIsHereFragment.ARG_SECTION_NUMBER,
                        R.string.title_who_is_here_fragment);
            } else if (resultCode == RESULT_CANCELED) {
                setChecked(false);
            }
            if (resultCode == RESULT_FIRST_USER || resultCode == RESULT_CANCELED) {
                switch (data.getIntExtra(MyBaseActivity.EXTRA_FRAG, 0)) {
                    case ConferenceScheduleFragment.ARG_SECTION_NUMBER:
                        changeActiveSectionFragment(ConferenceScheduleFragment.ARG_SECTION_NUMBER,
                                R.string.title_conference_schedule_fragment);
                        break;
                    case ActiveSessionsFragment.ARG_SECTION_NUMBER:
                        changeActiveSectionFragment(ActiveSessionsFragment.ARG_SECTION_NUMBER,
                                R.string.title_active_session_fragment);
                        break;
                    case AgendaFragment.ARG_SECTION_NUMBER:
                        changeActiveSectionFragment(AgendaFragment.ARG_SECTION_NUMBER,
                                R.string.title_agenda_fragment);
                        break;
                }
            }
        }
    }

    @Override
    protected void changeActiveSectionFragment(int ARG_SECTION_NUMBER, int fragmentTitleId) {
        if (!isChecked && ARG_SECTION_NUMBER > 3) {
            mDrawerItemSwitcher.changeItemOnDrawer(ARG_SECTION_NUMBER - 2);
        } else {
            mDrawerItemSwitcher.changeItemOnDrawer(ARG_SECTION_NUMBER - 1);
        }
        updateActivityTitle(getString(fragmentTitleId));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_i_am_here:
                if (mTitle.equals(getString(R.string.title_who_is_here_fragment))) {
                    changeActiveSectionFragment(HomepageFragment.ARG_SECTION_NUMBER,
                            R.string.title_homepage_fragment);
                }
                if (mTitle.equals(getString(R.string.title_statistics_fragment))) {
                    changeActiveSectionFragment(HomepageFragment.ARG_SECTION_NUMBER,
                            R.string.title_homepage_fragment);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void openFilterDialog(final LinkedList<String> trackList) {
        final ListView listView = new ListView(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_multiple_choice,
                trackList);
        listView.setAdapter(adapter);
        listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);

        AlertDialog.Builder adFilter = new AlertDialog.Builder(this);
        adFilter.setView(listView)
                .setTitle(getString(R.string.request_filter_track))
                .setPositiveButton(getString(R.string.filter), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        LinkedList<String> filteredByTrackList = new LinkedList<>();
                        SparseBooleanArray checkedItemPositions = listView.getCheckedItemPositions();
                        for (int i = 0; i < trackList.size(); i++) {
                            if (checkedItemPositions.get(i)) {
                                String item = trackList.get(i);
                                filteredByTrackList.add(item);
                            }
                        }
                        conferenceFrag.showSessionsBySelectedTracks(filteredByTrackList);
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        spinnerFilter.setSelection(0);
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        spinnerFilter.setSelection(0);
                    }
                })
                .create().show();
    }

    private void showConnectivityError() {
        AlertDialog.Builder construct = new AlertDialog.Builder(this);
        construct.setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.error_connectivity))
                .setNeutralButton(getString(R.string.ok), null)
                .create()
                .show();
    }

    private void defineSearchListeners(final SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                conferenceFrag.search(searchView.getQuery());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                conferenceFrag.search(searchView.getQuery());
                return false;
            }
        });


        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                conferenceFrag.changeToOriginalSessionList();
                // reset no spinner filtro
                spinnerFilter.setSelection(0);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerFilter.setSelection(0);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    public void filterByOnAgenda() {
        this.conferenceFrag.filterByOnAgenda();
    }

}
