package pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters.MyStatisticsRecyclerViewAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.Models.StatisticData;
import pt.ipleiria.estg.es2.byinvitationonly.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link StatisticsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {
    public static final int ARG_SECTION_NUMBER = 6;
    private MyStatisticsRecyclerViewAdapter mDataAdapter;
    private OnFragmentInteractionListener mListener;
    private FirebaseController.ValueFetched<StatisticData> conferenceHandler;
    private FirebaseController.ValueFetched<LinkedList<StatisticData>> sessionsHandler;
    private TextView conferenceTitle;
    private TextView conferenceRatingCount;
    private RatingBar conferenceAverageRatingBar;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    public static StatisticsFragment newInstance() {
        return new StatisticsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        conferenceHandler = new FirebaseController.ValueFetched<StatisticData>() {
            @Override
            public void valuesFetched(StatisticData statisticData) {
                loadConferenceData(statisticData);
            }
        };

        sessionsHandler = new FirebaseController.ValueFetched<LinkedList<StatisticData>>() {
            @Override
            public void valuesFetched(LinkedList<StatisticData> statisticDatas) {
                loadSessionsData(statisticDatas);
            }
        };

        FirebaseController.getConferenceStatistics(conferenceHandler);
        FirebaseController.getSessionsStatistics(sessionsHandler);
    }

    private void loadConferenceData(StatisticData statisticData) {
        conferenceTitle.setText(statisticData.getTitle());

        if (Integer.parseInt(statisticData.getNumRatings()) == 0) {
            conferenceRatingCount.setText(R.string.n_a_string);
            conferenceAverageRatingBar.setVisibility(View.INVISIBLE);

        } else {
            conferenceRatingCount.setText(statisticData.getNumRatings());
            conferenceAverageRatingBar.setRating(statisticData.getAverageRating());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_statistics, container, false);
        RecyclerView recyclerView = (RecyclerView) layout.findViewById(R.id.recyclerViewStatistics);
        conferenceTitle = (TextView) layout.findViewById(R.id.textViewConferenceTitle);
        conferenceRatingCount = (TextView) layout.findViewById(R.id.textViewCountConferenceRatings);
        conferenceAverageRatingBar = (RatingBar) layout.findViewById(R.id.ratingBarConference);
        /////////////////////////////////////////////////////////////////////////////
        mDataAdapter = new MyStatisticsRecyclerViewAdapter(getActivity());
        recyclerView.setAdapter(mDataAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
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
        FirebaseController.cancelValueEventHandler(conferenceHandler);
        FirebaseController.cancelValueEventHandler(sessionsHandler);
    }

    private void loadSessionsData(LinkedList<StatisticData> sessionStatisticDatas) {
        changeAdapterData(sessionStatisticDatas);
    }

    private synchronized void changeAdapterData(LinkedList<StatisticData> sessionStatisticDatas) {
        mDataAdapter.setSessionStatisticDatasList(sessionStatisticDatas);
    }

    public interface OnFragmentInteractionListener {
        void onSectionAttached(int ARG_SECTION_NUMBER);
    }
}
