package pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters.MyConferenceRecyclerViewAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.Database.DBAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.DetailsSessionActivity;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.MyBaseActivity;
import pt.ipleiria.estg.es2.byinvitationonly.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ConferenceScheduleFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ConferenceScheduleFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConferenceScheduleFragment extends Fragment {
    public static final int ARG_SECTION_NUMBER = 2;
    private LinkedList<Session> sessionList = new LinkedList<>();
    private RecyclerView recyclerView;
    private TextView emptyView;
    private MyConferenceRecyclerViewAdapter mDataAdapter;
    private OnFragmentInteractionListener mListener;
    private FirebaseController.ValueFetched<LinkedList<Session>> fireBaseHandler;
    private ProgressBar pb;
    private BroadcastReceiver broadcastReceiver;
    private LinearLayoutManager linearLayoutManager;
    private Context context;
    private DBAdapter dbAdapter;

    public ConferenceScheduleFragment() {
        // Required empty public constructor
    }

    public static ConferenceScheduleFragment newInstance() {
        return new ConferenceScheduleFragment();
    }

    public void loadSessionDataFromServer(LinkedList<Session> sessionList) {
        this.sessionList = orderByDate(sessionList);
        if (!SharedPreferenceController.getFilterState(context) || pb.getVisibility() == View.VISIBLE) {
            changeAdapterData(this.sessionList);
            recyclerView.setVisibility(View.VISIBLE);
            pb.setVisibility(View.GONE);
        }
    }

    public void loadSessionDataFromFile(LinkedList<Session> sessionList) {
        this.sessionList = orderByDate(sessionList);
        changeAdapterData(this.sessionList);
        recyclerView.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
    }

    private LinkedList<Session> orderByDate(LinkedList<Session> sessionList) {
        Collections.sort(sessionList);
        return sessionList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter = new DBAdapter(getActivity());
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    refreshRatingOnSessionsThatChangedState();
                }
            }

            private void refreshRatingOnSessionsThatChangedState() {
                linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                for (int i = linearLayoutManager.findFirstVisibleItemPosition();
                     i <= linearLayoutManager.findLastVisibleItemPosition(); i++) {

                    if (!mDataAdapter.getSessionList().isEmpty()) {
                        Session session = mDataAdapter.getSessionList().get(i);
                        View cardView = linearLayoutManager.findViewByPosition(i);

                        if (session != null && cardView != null) {
                            int visibility = cardView.findViewById(R.id.ratingBarSessions).getVisibility();

                            if (session.hasBegun() && visibility != View.VISIBLE) {
                                cardView.findViewById(R.id.ratingBarSessions).setVisibility(View.VISIBLE);
                            } else if (!session.hasBegun() && visibility == View.VISIBLE) {
                                cardView.findViewById(R.id.ratingBarSessions).setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            }
        };
        this.context = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_conference_schedule, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.my_recycler_view);
        emptyView = (TextView) layout.findViewById(R.id.empty_data);
        mDataAdapter = new MyConferenceRecyclerViewAdapter(getActivity(), sessionList);
        mDataAdapter.setClickListener(new MyConferenceRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Session s) {
                Intent intent = new Intent(getActivity(), DetailsSessionActivity.class);
                intent.putExtra(MyBaseActivity.EXTRA_SESSION, s);
                intent.putExtra(MyBaseActivity.EXTRA_ISCHECKED, mListener.getChecked());
                intent.putExtra(MyBaseActivity.EXTRA_FRAG, ARG_SECTION_NUMBER);
                getActivity().startActivityForResult(intent, MainActivity.DETAIL_SESSION);
            }

            @Override
            public void notifyThatSessionOnOriginalListChangedOnAgenda(Session session) {
                for (Session originalSession : sessionList) {
                    if (originalSession.getAbstracts().equals(session.getAbstracts()) &&
                            originalSession.getDateFormattedString().equals(session.getDateFormattedString()) &&
                            originalSession.getEndHour().equals(session.getEndHour()) &&
                            originalSession.getPresenter().equals(session.getPresenter()) &&
                            originalSession.getStartHour().equals(session.getStartHour()) &&
                            originalSession.getTitle().equals(session.getTitle()) &&
                            originalSession.getTrack().equals(session.getTrack())) {

                        originalSession.setOnAgenda(false);
                    }
                }
            }
        });
        recyclerView.setAdapter(mDataAdapter);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setVisibility(View.VISIBLE);
        pb = (ProgressBar) layout.findViewById(R.id.progressBar);
        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mListener.onSectionAttached(ARG_SECTION_NUMBER);
            mListener.resetSpinnerItemPosition();
            SharedPreferenceController.disableFilterState(getActivity());
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        FirebaseController.cancelValueEventHandler(fireBaseHandler);
        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fireBaseHandler = new FirebaseController.ValueFetched<LinkedList<Session>>() {
            @Override
            public void valuesFetched(LinkedList<Session> firebaseSessionList) {
                loadSessionDataFromServer(firebaseSessionList);
            }
        };
        if (NetworkController.existConnection(getActivity())) {
            pb.setVisibility(View.VISIBLE);
        }
        FirebaseController.getConferenceSessionsContinuous(fireBaseHandler, getActivity());
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    public void showSessionsBySelectedTracks(LinkedList<String> selectedTrackList) {
        LinkedList<Session> newSessionList = new LinkedList<>();
        for (Session session : sessionList) {
            for (String selectedTrack : selectedTrackList) {
                if (session.getTrack().equalsIgnoreCase(selectedTrack)) {
                    newSessionList.add(session);
                }
            }
        }
        changeAdapterData(orderByDate(newSessionList));
    }

    public void changeToOriginalSessionList() {
        changeAdapterData(sessionList);
    }

    private synchronized void changeAdapterData(LinkedList<Session> sessions) {
        if (sessions.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        mDataAdapter.setSessionList(sessions);
    }

    public void search(CharSequence query) {
        LinkedList<Session> newSessionList = new LinkedList<>();
        String queryLC = query.toString().toUpperCase();
        for (Session session : sessionList) {
            if (session.getAbstracts().toUpperCase().contains(queryLC) ||
                    session.getDayMonthOnString().toUpperCase().contains(queryLC) ||
                    session.getEndHour().toUpperCase().contains(queryLC) ||
                    session.getPresenter().toUpperCase().contains(queryLC) ||
                    session.getRoom().toUpperCase().contains(queryLC) ||
                    session.getStartHour().toUpperCase().contains(queryLC) ||
                    session.getTitle().toUpperCase().contains(queryLC) ||
                    session.getTrack().toUpperCase().contains(queryLC)) {
                newSessionList.add(session);
            }
        }
        changeAdapterData(orderByDate(newSessionList));
    }

    public LinkedList<String> getTrackList() {
        LinkedList<String> trackList = new LinkedList<>();
        for (Session session : sessionList) {
            if (!isTrackInList(trackList, session.getTrack()) && !session.getTrack().isEmpty()) {
                trackList.add(session.getTrack());
            }
        }
        return trackList;
    }

    private boolean isTrackInList(LinkedList<String> trackList, String track) {
        for (int i = 0; i < trackList.size(); i++) {
            if (trackList.get(i).equalsIgnoreCase(track)) {
                return true;
            }
        }
        return false;
    }

    public void filterByOnAgenda() {
        LinkedList<Session> onlyAgendaSessions = new LinkedList<>();
        for (Session s : dbAdapter.getSessions()) {
            if (s.isOnAgenda()) {
                onlyAgendaSessions.add(s);
            }
        }
        changeAdapterData(orderByDate(onlyAgendaSessions));
    }

    public interface OnFragmentInteractionListener {
        void onSectionAttached(int ARG_SECTION_NUMBER);

        boolean getChecked();

        void resetSpinnerItemPosition();
    }
}
