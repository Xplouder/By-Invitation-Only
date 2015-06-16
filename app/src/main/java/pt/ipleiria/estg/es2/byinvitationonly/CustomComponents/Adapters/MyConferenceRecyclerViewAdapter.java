package pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Database.DBAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.R;


public class MyConferenceRecyclerViewAdapter extends RecyclerView.Adapter<MyConferenceRecyclerViewAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Session> sessionList = Collections.emptyList();
    private ItemClickListener clickListener;
    private Context context;
    private DBAdapter dbAdapter;

    public MyConferenceRecyclerViewAdapter(Context context, List<Session> sessionList) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.sessionList = sessionList;
        dbAdapter = new DBAdapter(context);
    }

    public List<Session> getSessionList() {
        return sessionList;
    }

    public void setSessionList(List<Session> sessionList) {
        this.sessionList = sessionList;
        notifyDataSetChanged();
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_session, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
        final Session currentSession = sessionList.get(position);

        viewHolder.textTittle.setText(currentSession.getTitle());
        viewHolder.textStartEnd.setText(currentSession.getStartHour() + " - " + currentSession.getEndHour());
        viewHolder.textDate.setText(currentSession.getDayMonthOnString());
        viewHolder.textTrack.setText(currentSession.getTrack());
        viewHolder.textRoom.setText(currentSession.getRoom());
        viewHolder.myRatingBar.setRating(currentSession.getMyRating());
        if (currentSession.hasBegun()) {
            viewHolder.myRatingBar.setVisibility(View.VISIBLE);
        } else {
            viewHolder.myRatingBar.setVisibility(View.INVISIBLE);
        }
        viewHolder.checkBox.setChecked(currentSession.isOnAgenda());
        viewHolder.setClickListener(clickListener, currentSession);
    }

    @Override
    public int getItemCount() {
        return sessionList.size();
    }

    public interface ItemClickListener {
        void onItemClick(Session s);

        void notifyThatSessionOnOriginalListChangedOnAgenda(Session session);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CheckBox checkBox;
        private TextView textTittle;
        private TextView textRoom;
        private TextView textTrack;
        private TextView textDate;
        private TextView textStartEnd;
        private RatingBar myRatingBar;
        private MyConferenceRecyclerViewAdapter.ItemClickListener listener;
        private Session session;

        public ViewHolder(View v) {
            super(v);
            textTittle = (TextView) v.findViewById(R.id.text_view_session_row_titulo);
            textRoom = (TextView) v.findViewById(R.id.text_view_session_row_sala);
            textTrack = (TextView) v.findViewById(R.id.text_view_session_row_track);
            textDate = (TextView) v.findViewById(R.id.text_view_session_row_date);
            textStartEnd = (TextView) v.findViewById(R.id.text_view_session_start_end);
            myRatingBar = (RatingBar) v.findViewById(R.id.ratingBarSessions);
            checkBox = (CheckBox) v.findViewById(R.id.checkBox);
            checkBox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (SharedPreferenceController.getFilterState(context)) {
                        clickListener.notifyThatSessionOnOriginalListChangedOnAgenda(session);
                        int temp = sessionList.indexOf(session);
                        sessionList.remove(session);
                        notifyItemRemoved(temp);
                    }
                    session.setOnAgenda(checkBox.isChecked());
                    if (checkBox.isChecked()) {
                        dbAdapter.addSession(session);
                    } else {
                        dbAdapter.removeSession(session);
                    }
                }
            });

            v.setOnClickListener(this);
            myRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (fromUser) {
                        if (NetworkController.existConnection(context) && session.getFirebaseSessionNode() != null) {
                            session.setMyRating(myRatingBar.getRating());
                            FirebaseController.sendSessionRating(session, SharedPreferenceController.getUserID(context));
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
        }

        public void setClickListener(MyConferenceRecyclerViewAdapter.ItemClickListener clickListener, Session currentSession) {
            this.listener = clickListener;
            this.session = currentSession;
        }

        @Override
        public void onClick(View v) {
            if (listener != null && session != null) {
                listener.onItemClick(session);
            }
        }

        private void showConnectivityError() {
            AlertDialog.Builder construct = new AlertDialog.Builder(context);
            construct.setTitle(context.getString(R.string.warning))
                    .setMessage(context.getString(R.string.error_connectivity))
                    .setNeutralButton(context.getString(R.string.ok), null)
                    .create()
                    .show();
        }
    }


}
