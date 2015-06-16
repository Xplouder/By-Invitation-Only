package pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import pt.ipleiria.estg.es2.byinvitationonly.Models.StatisticData;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MyStatisticsRecyclerViewAdapter extends RecyclerView.Adapter<MyStatisticsRecyclerViewAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<StatisticData> sessionStatisticDatas = Collections.emptyList();
    private Context context;

    public MyStatisticsRecyclerViewAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    public void setSessionStatisticDatasList(List<StatisticData> sessionStatisticDatas) {
        this.sessionStatisticDatas = sessionStatisticDatas;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = inflater.inflate(R.layout.row_statistic_session, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        final StatisticData sessionStatisticData = sessionStatisticDatas.get(position);
        viewHolder.textTittle.setText(sessionStatisticData.getTitle());

        if (Integer.parseInt(sessionStatisticData.getNumRatings()) == 0) {
            viewHolder.textRatingCounter.setText(context.getString(R.string.n_a_string));
            viewHolder.ratingBarSession.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.textRatingCounter.setText(sessionStatisticData.getNumRatings());
            viewHolder.ratingBarSession.setVisibility(View.VISIBLE);
            viewHolder.ratingBarSession.setRating(sessionStatisticData.getAverageRating());
        }
    }

    @Override
    public int getItemCount() {
        return sessionStatisticDatas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView textTittle;
        private TextView textRatingCounter;
        private RatingBar ratingBarSession;

        public ViewHolder(View v) {
            super(v);
            textTittle = (TextView) v.findViewById(R.id.textViewSessionTitle);
            textRatingCounter = (TextView) v.findViewById(R.id.textViewCountSessionRatings);
            ratingBarSession = (RatingBar) v.findViewById(R.id.ratingBarSession);
        }
    }


}

