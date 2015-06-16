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
import java.util.Comparator;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FileController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SessionHelper;
import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters.MyActiveSessionsRecyclerViewAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.DetailsSessionActivity;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.MyBaseActivity;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActiveSessionsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ActiveSessionsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActiveSessionsFragment extends Fragment {
    public static final int ARG_SECTION_NUMBER = 4;
    private LinkedList<Session> active_sessionList = new LinkedList<>();
    private RecyclerView recyclerView;
    private TextView emptyView;
    private MyActiveSessionsRecyclerViewAdapter mDataAdapter;
    private OnFragmentInteractionListener mListener;
    private FirebaseController.ValueFetched<LinkedList<Session>> fireBaseHandler;
    private ProgressBar pb;
    private BroadcastReceiver broadcastReceiver;

    public ActiveSessionsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConferenceScheduleFragment.
     */
    public static ActiveSessionsFragment newInstance() {
        ActiveSessionsFragment fragment = new ActiveSessionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void loadSessionData(LinkedList<Session> sessionList) {
        LinkedList<Session> sL = new LinkedList<>();
        for (Session s : sessionList) {
            if (s.isActive()) {
                sL.add(s);
            }
        }
        this.active_sessionList = orderByRemainingTime(sL);
        changeAdapterData(this.active_sessionList);
        recyclerView.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
    }

    private LinkedList<Session> orderByRemainingTime(LinkedList<Session> aSL) {
        Collections.sort(aSL, new Comparator<Session>() {
            @Override
            public int compare(Session s1, Session s2) {
                if (!s1.getEndHour().isEmpty() && !s2.getEndHour().isEmpty()) {
                    long s1RemainingTime = SessionHelper.calculateRemainingTimeLong(s1.getEndHour());
                    long s2RemainingTime = SessionHelper.calculateRemainingTimeLong(s2.getEndHour());

                    if (s1RemainingTime > s2RemainingTime) {
                        return 1;
                    }
                    if (s1RemainingTime < s2RemainingTime) {
                        return -1;
                    }
                }
                return 0;
            }
        });
        return aSL;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fireBaseHandler = new FirebaseController.ValueFetched<LinkedList<Session>>() {
            @Override
            public void valuesFetched(LinkedList<Session> firebaseSessionList) {
                loadSessionData(firebaseSessionList);
            }
        };
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().compareTo(Intent.ACTION_TIME_TICK) == 0) {
                    refreshList();
                }
            }

            private void refreshList() {
                if (NetworkController.existConnection(getActivity())) {
                    FirebaseController.getConferenceSessionsOnce(fireBaseHandler, getActivity());
                } else {
                    loadSessionData(FileController.importSessions(getActivity()));
                }
            }
        };
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_active_sessions, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.my_recycler_view);
        emptyView = (TextView) layout.findViewById(R.id.empty_data);
        pb = (ProgressBar) layout.findViewById(R.id.progressBar);
        mDataAdapter = new MyActiveSessionsRecyclerViewAdapter(getActivity(), active_sessionList);
        mDataAdapter.setClickListener(new MyActiveSessionsRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Session s) {
                Intent intent = new Intent(getActivity(), DetailsSessionActivity.class);
                intent.putExtra(MyBaseActivity.EXTRA_SESSION, s);
                intent.putExtra(MyBaseActivity.EXTRA_ISCHECKED, mListener.getChecked());
                intent.putExtra(MyBaseActivity.EXTRA_FRAG, ARG_SECTION_NUMBER);
                getActivity().startActivityForResult(intent, MainActivity.DETAIL_SESSION);
            }
        });
        recyclerView.setAdapter(mDataAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setVisibility(View.VISIBLE);
        return layout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mListener.onSectionAttached(ARG_SECTION_NUMBER);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        if (broadcastReceiver != null)
            getActivity().unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadSessionData(FileController.importSessions(getActivity()));
        if (NetworkController.existConnection(getActivity())) {
            pb.setVisibility(View.VISIBLE);
            FirebaseController.getConferenceSessionsOnce(fireBaseHandler, getActivity());
        }
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_TIME_TICK));
    }

    private synchronized void changeAdapterData(LinkedList<Session> sessions) {
        if (sessions.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        mDataAdapter.setActiveSessionList(sessions);
    }

    public interface OnFragmentInteractionListener {
        void onSectionAttached(int ARG_SECTION_NUMBER);

        boolean getChecked();
    }
}
