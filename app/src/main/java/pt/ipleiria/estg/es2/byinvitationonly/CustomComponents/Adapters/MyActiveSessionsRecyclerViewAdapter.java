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

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FileController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SessionHelper;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;

public class MyActiveSessionsRecyclerViewAdapter extends RecyclerView.Adapter<MyActiveSessionsRecyclerViewAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Session> activeSessionList = Collections.emptyList();
    private ItemClickListener clickListener;
    private Context context;


    public MyActiveSessionsRecyclerViewAdapter(Context context, List<Session> activeSessionList) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.activeSessionList = activeSessionList;
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_active_session, viewGroup, false);
        return new ViewHolder(view);
    }

    public List<Session> getActiveSessionList() {
        return activeSessionList;
    }

    public void setActiveSessionList(List<Session> activeSessionList) {
        this.activeSessionList = activeSessionList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final Session currentSession = activeSessionList.get(position);

        viewHolder.textViewDay.setText(currentSession.getDay());
        viewHolder.textViewMonthYear.setText(currentSession.getMonthAndYear());
        viewHolder.textViewStartHourEndHour.setText(currentSession.getStartHour() + " - " + currentSession.getEndHour());
        viewHolder.textViewTittle.setText(currentSession.getTitle());
        viewHolder.textViewTrack.setText(currentSession.getTrack());
        viewHolder.textViewRoom.setText(currentSession.getRoom());
        viewHolder.textViewRemainingTime.setText(SessionHelper.calculateRemainingTimeString(currentSession.getEndHour()));
        viewHolder.myRatingBar.setRating(currentSession.getMyRating());
        viewHolder.setClickListener(clickListener, currentSession);

        viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSession.setOnAgenda(viewHolder.checkBox.isChecked());
                FileController.updateSessionStateOnAgenda(context, currentSession);
            }
        });
    }

    @Override
    public int getItemCount() {
        return activeSessionList.size();
    }

    public interface ItemClickListener {
        void onItemClick(Session s);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        /*o dia, a hora de inicio, a hora de fim, o título, a room, se existente, o track
         (track) de cada sessão e o tempo restante para a mesma acabar.*/
        private TextView textViewDay;
        private TextView textViewMonthYear;
        private TextView textViewStartHourEndHour;
        private TextView textViewTittle;
        private TextView textViewRoom;
        private TextView textViewTrack;
        private TextView textViewRemainingTime;
        private MyActiveSessionsRecyclerViewAdapter.ItemClickListener listener;
        private Session session;
        private RatingBar myRatingBar;
        private CheckBox checkBox;

        public ViewHolder(View v) {
            super(v);
            textViewDay = (TextView) v.findViewById(R.id.text_view_active_session_row_day);
            textViewMonthYear = (TextView) v.findViewById(R.id.text_view_active_session_row_month_year);
            textViewStartHourEndHour = (TextView) v.findViewById(R.id.text_view_active_session_row_start_hour_end_hour);
            textViewTittle = (TextView) v.findViewById(R.id.text_view_active_session_row_title);
            textViewRoom = (TextView) v.findViewById(R.id.text_view_active_session_row_room);
            textViewTrack = (TextView) v.findViewById(R.id.text_view_active_session_row_track);
            textViewRemainingTime = (TextView) v.findViewById(R.id.text_view_active_session_remaining_time);
            checkBox = (CheckBox) v.findViewById(R.id.checkBox);
            v.setOnClickListener(this);
            myRatingBar = (RatingBar) v.findViewById(R.id.ratingBarSessions);
            myRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                @Override
                public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                    if (fromUser) {
                        if (NetworkController.existConnection(context)) {
                            session.setMyRating(myRatingBar.getRating());
                            FirebaseController.sendSessionRating(session, SharedPreferenceController.getUserID(context));
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
                    FileController.updateSessionStateOnAgenda(context, session);
                }
            });
        }

        public void setClickListener(MyActiveSessionsRecyclerViewAdapter.ItemClickListener clickListener, Session currentSession) {
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
