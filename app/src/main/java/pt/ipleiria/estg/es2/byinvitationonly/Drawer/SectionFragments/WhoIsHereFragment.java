package pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters.MyContactsRecyclerViewAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link WhoIsHereFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link WhoIsHereFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WhoIsHereFragment extends Fragment {
    public static final int ARG_SECTION_NUMBER = 3;

    private MyContactsRecyclerViewAdapter mDataAdapter;
    private OnFragmentInteractionListener mListener;
    private FirebaseController.ValueFetched<LinkedList<Contact>> fireBaseHandler;
    private TextView emptyView;
    private LinkedList<Contact> contactList = new LinkedList<>();
    private RecyclerView recyclerView;
    private ProgressBar pb;

    public WhoIsHereFragment() {
        // Required empty public constructor
    }

    public static WhoIsHereFragment newInstance() {
        return new WhoIsHereFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
            mListener.onSectionAttached(ARG_SECTION_NUMBER);
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    "Must implement onFragmentIteractionListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fireBaseHandler = new FirebaseController.ValueFetched<LinkedList<Contact>>() {
            @Override
            public void valuesFetched(final LinkedList<Contact> contact) {
                loadContactData(contact);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_who_is_here, container, false);
        recyclerView = (RecyclerView) layout.findViewById(R.id.my_recycler_view_whoIsHere);
        emptyView = (TextView) layout.findViewById(R.id.empty_data_WhoIsHere);
        mDataAdapter = new MyContactsRecyclerViewAdapter(getActivity(), contactList);
        mDataAdapter.setClickListener(new MyContactsRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onItemClick(Contact c) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO,
                        Uri.parse("mailto:" + Uri.encode(c.getEmail())));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, getActivity().getString(R.string.app_name));
                startActivity(emailIntent);
            }
        });
        recyclerView.setAdapter(mDataAdapter);
        defineRecyclerViewLayout();
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        //fim teste
        recyclerView.setVisibility(View.VISIBLE);
        pb = (ProgressBar) layout.findViewById(R.id.progressBarWhoIsHere);

        return layout;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        FirebaseController.cancelValueEventHandler(fireBaseHandler);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FirebaseController.getContacts(fireBaseHandler, SharedPreferenceController.getLocalContactKey(getActivity()));
        if (!NetworkController.existConnection(getActivity())) {
            pb.setVisibility(View.GONE);
            AlertDialog.Builder construct = new AlertDialog.Builder(getActivity());
            construct.setTitle(getString(R.string.warning))
                    .setMessage(getString(R.string.error_connectivity))
                    .setPositiveButton(getString(R.string.ok), null)
                    .create()
                    .show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        defineRecyclerViewLayout();
    }

    private void defineRecyclerViewLayout() {
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                    StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS,
                    StaggeredGridLayoutManager.VERTICAL));
        } else if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }
    }

    private void loadContactData(LinkedList<Contact> firebaseContactList) {
        this.contactList = firebaseContactList;
        changeAdapterData(this.contactList);
        recyclerView.setVisibility(View.VISIBLE);
        pb.setVisibility(View.GONE);
    }

    private void changeAdapterData(LinkedList<Contact> contactList) {
        if (contactList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        mDataAdapter.setContactList(contactList);
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onSectionAttached(int ARG_SECTION_NUMBER);

    }


}
