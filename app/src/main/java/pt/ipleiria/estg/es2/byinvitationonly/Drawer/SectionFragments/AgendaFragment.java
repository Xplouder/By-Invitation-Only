package pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters.MyAgendaRecyclerViewAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.Database.DBAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.DetailsSessionActivity;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.MyBaseActivity;
import pt.ipleiria.estg.es2.byinvitationonly.R;


public class AgendaFragment extends Fragment implements MyAgendaRecyclerViewAdapter.OnEmptyListListener {
    public static final int ARG_SECTION_NUMBER = 5;
    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private TextView emptyView;
    private MyAgendaRecyclerViewAdapter mDataAdapter;
    private LinkedList<Session> sessionList;
    private DBAdapter dbAdapter;

    public AgendaFragment() {
        // Required empty public constructor
    }

    public static AgendaFragment newInstance() {
        return new AgendaFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbAdapter = new DBAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_agenda, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.my_recycler_view);
        emptyView = (TextView) layout.findViewById(R.id.empty_data);
        mDataAdapter = new MyAgendaRecyclerViewAdapter(getActivity(), sessionList, this);
        mDataAdapter.setClickListener(new MyAgendaRecyclerViewAdapter.ItemClickListener() {
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

    public void loadSessionData(LinkedList<Session> sessionList) {
        this.sessionList = orderByDate(sessionList);
        changeAdapterData(this.sessionList);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private LinkedList<Session> orderByDate(LinkedList<Session> sessionList) {
        Collections.sort(sessionList);
        return sessionList;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        LinkedList<Session> onlyAgendaSessions = new LinkedList<>();
        for (Session s : dbAdapter.getSessions()) {
            if (s.isOnAgenda()) {
                onlyAgendaSessions.add(s);
            }
        }
        loadSessionData(onlyAgendaSessions);
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

    @Override
    public void setVisibleEmptyData() {
        emptyView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    public interface OnFragmentInteractionListener {
        void onSectionAttached(int ARG_SECTION_NUMBER);

        boolean getChecked();
    }
}

