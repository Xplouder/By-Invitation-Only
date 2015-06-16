package pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FileController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Conference;
import pt.ipleiria.estg.es2.byinvitationonly.Utils.RoundedTransform;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomepageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomepageFragment extends Fragment {
    public static final int ARG_SECTION_NUMBER = 1;
    private FirebaseController.ValueFetched<Conference> firebaseHandler;
    private Conference conference;
    private OnFragmentInteractionListener mListener;
    private TextView tvFN;
    private TextView tvD;
    private TextView tvL;
    private ImageView iv;
    private TextView textCallForPapers;
    private TextView textWebsite;
    private ProgressBar pb;
    private RatingBar myRatingBar;

    public HomepageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment HomepageFragment.
     */
    public static HomepageFragment newInstance() {
        HomepageFragment fragment = new HomepageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_homepage, container, false);

        tvFN = (TextView) view.findViewById(R.id.textViewFullName);
        tvD = (TextView) view.findViewById(R.id.textViewDates);
        tvL = (TextView) view.findViewById(R.id.textViewLocation);
        iv = (ImageView) view.findViewById(R.id.imageView);
        textCallForPapers = (TextView) view.findViewById(R.id.textCallforPapers);
        textWebsite = (TextView) view.findViewById(R.id.textWebsite);
        pb = (ProgressBar) view.findViewById(R.id.progressBar);
        myRatingBar = (RatingBar) view.findViewById(R.id.ratingBarHomepage);

        view.setBackgroundColor(Color.WHITE);
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        FirebaseController.cancelValueEventHandler(firebaseHandler);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        try {
            loadConferenceData(FileController.importConference(getActivity()));
            firebaseHandler = new FirebaseController.ValueFetched<Conference>() {
                @Override
                public void valuesFetched(final Conference data) {
                    loadConferenceData(data);
                }
            };
            if (NetworkController.existConnection(getActivity())) {
                ProgressBar pb = (ProgressBar) getActivity().findViewById(R.id.progressBar);
                pb.setVisibility(View.VISIBLE);
            }
            FirebaseController.getConferenceData(firebaseHandler, getActivity());

            myRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (fromUser) {
                        if (NetworkController.existConnection(getActivity()) && conference.getFirebaseConferenceNode() != null) {
                            conference.setMyRating(myRatingBar.getRating());
                            FirebaseController.sendConferenceRating(conference, SharedPreferenceController.getUserID(getActivity()));
                        } else {
                            showConnectivityError();
                            ratingBar.setRating(conference.getMyRating());
                        }
                    }
                }
            });
        } catch (IOException e) {
            AlertDialog.Builder construct = new AlertDialog.Builder(getActivity());
            construct.setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.toast_error_files))
                    .setPositiveButton(getString(R.string.ok), null)
                    .create()
                    .show();
        }
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

    public void loadConferenceData(Conference conference) {
        this.conference = conference;
        if (mListener != null) {
            mListener.updateActivityTitle(this.conference.getAbbreviation());
        }
        loadDisplay();
        pb.setVisibility(View.GONE);
    }

    private void loadDisplay() {
        tvFN.setText(conference.getFullName());
        tvD.setText(conference.getDates());
        tvL.setText(conference.getLocation());
        Picasso.with(getActivity()).load(conference.getLogoURL()).transform(new RoundedTransform(10, 10)).into(iv);
        textCallForPapers.setText(Html.fromHtml("<a href=\"" + conference.getCallForPapers() + "\">Call for Papers</a> "));
        textCallForPapers.setMovementMethod(LinkMovementMethod.getInstance());
        textWebsite.setText(Html.fromHtml("<a href=\"" + conference.getWebsite() + "\">Website</a> "));
        textWebsite.setMovementMethod(LinkMovementMethod.getInstance());
        myRatingBar.setRating(conference.getMyRating());
    }

    private void showConnectivityError() {
        AlertDialog.Builder construct = new AlertDialog.Builder(getActivity());
        construct.setTitle(getActivity().getString(R.string.warning))
                .setMessage(getActivity().getString(R.string.error_connectivity))
                .setNeutralButton(getActivity().getString(R.string.ok), null)
                .create()
                .show();
    }

    public interface OnFragmentInteractionListener {
        void onSectionAttached(int ARG_SECTION_NUMBER);

        void updateActivityTitle(String title);
    }

}
