package pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Message;
import pt.ipleiria.estg.es2.byinvitationonly.R;


public class MyChatRecyclerViewAdapter extends RecyclerView.Adapter<MyChatRecyclerViewAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private Context context;
    private List<Message> chatList = Collections.emptyList();

    public MyChatRecyclerViewAdapter(Context context) {
        inflater = LayoutInflater.from(context);
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = inflater.inflate(R.layout.row_chat, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        final String m = chatList.get(position).getMessage();

        if (!chatList.get(position).getReceiver().getEmail().equals(SharedPreferenceController.getLocalStoredUserContact(context).getEmail())) {
            viewHolder.rl.setGravity(Gravity.END);
            viewHolder.mTextView.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.my_balloon, null));
        } else {
            viewHolder.rl.setGravity(Gravity.START);
            viewHolder.mTextView.setBackground(ResourcesCompat.getDrawable(context.getResources(), R.drawable.other_balloon, null));
        }
        viewHolder.mTextView.setText(m);
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    public void setChatList(List<Message> chatList) {
        this.chatList = chatList;
        notifyDataSetChanged();
    }

    public int addMessageToChatListMessage(Message newMessage) {
        this.chatList.add(newMessage);
        notifyItemInserted(chatList.size());
        return chatList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTextView;
        public RelativeLayout rl;

        public ViewHolder(View v) {
            super(v);
            rl = (RelativeLayout) v.findViewById(R.id.relative_layout_chat_card);
            mTextView = (TextView) v.findViewById(R.id.textView_card_chat);
        }

        @Override
        public void onClick(View v) {
        }
    }

}


