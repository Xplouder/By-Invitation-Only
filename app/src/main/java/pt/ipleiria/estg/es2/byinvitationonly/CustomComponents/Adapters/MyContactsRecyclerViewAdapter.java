package pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.util.ColorGenerator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import pt.ipleiria.estg.es2.byinvitationonly.ContactChatActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MyContactsRecyclerViewAdapter extends RecyclerView.Adapter<MyContactsRecyclerViewAdapter.ViewHolder> {
    private LayoutInflater inflater;
    private Context context;
    private List<Contact> contactlist = Collections.emptyList();
    private ItemClickListener clickListener;
    private ColorGenerator generator = ColorGenerator.MATERIAL;


    public MyContactsRecyclerViewAdapter(Context context, List<Contact> contactlist) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.contactlist = contactlist;
    }

    public void setClickListener(ItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setContactList(LinkedList<Contact> contactList) {
        this.contactlist = contactList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_who_is_here, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyContactsRecyclerViewAdapter.ViewHolder holder, final int position) {
        final Contact currentContact = contactlist.get(position);
        //final ImageViewClickListener clickListenerImageView =  new ImageViewClickListener(currentContact);

        holder.textName.setText(currentContact.getName());
        holder.textMail.setText(currentContact.getEmail());
        holder.image_avatar.setImageDrawable(generateImageDrawable(currentContact));
        holder.setClickListener(clickListener, currentContact);
    }

    private void showConnectivityError() {
        AlertDialog.Builder construct = new AlertDialog.Builder(context);
        construct.setTitle(context.getString(R.string.warning))
                .setMessage(context.getString(R.string.error_connectivity))
                .setNeutralButton(context.getString(R.string.ok), null)
                .create()
                .show();
    }

    private Drawable generateImageDrawable(Contact currentContact) {
        int color = generator.getColor(currentContact.getEmail());
        String[] letters = generateInitials(currentContact.getName());
        String image_text;

        if (letters.length <= 0) {
            image_text = "";
        } else if (letters.length == 1) {
            image_text = letters[0];
        } else {
            image_text = letters[0] + " " + letters[1];
        }

        return TextDrawable.builder().buildRound(image_text, color);
    }

    @Override
    public int getItemCount() {
        return contactlist.size();
    }

    private String[] generateInitials(String original) {
        String[] split = original.split(" ");
        String[] letters = new String[split.length];

        for (int i = 0; i < split.length; i++) {
            letters[i] = split[i].substring(0, 1);
        }

        return letters;
    }

    public interface ItemClickListener {
        void onItemClick(Contact c);
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private CardView cardView;
        private TextView textName;
        private TextView textMail;
        private ImageView image_avatar;
        private ImageView image_icon;
        private MyContactsRecyclerViewAdapter.ItemClickListener listener;
        private Contact contact;

        public ViewHolder(View v) {
            super(v);
            cardView = (CardView) itemView.findViewById(R.id.cardview_item_layout_contacts_container);
            textName = (TextView) v.findViewById(R.id.textViewName);
            textMail = (TextView) v.findViewById(R.id.textViewEmail);
            image_avatar = (ImageView) v.findViewById(R.id.image_view_avatar);
            image_icon = (ImageView) v.findViewById(R.id.imageMessageIcon);
            v.setOnClickListener(this);
            image_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (NetworkController.existConnection(context)) {
                        Intent intent = new Intent(context, ContactChatActivity.class);
                        intent.putExtra(ContactChatActivity.EXTRA_CONTACT, contact);
                        context.startActivity(intent);
                    } else {
                        showConnectivityError();
                    }
                }
            });
        }

        public void setClickListener(MyContactsRecyclerViewAdapter.ItemClickListener clickListener, Contact currentContact) {
            this.listener = clickListener;
            this.contact = currentContact;
        }


        @Override
        public void onClick(View v) {

            if (listener != null && contact != null) {
                listener.onItemClick(contact);
            }
        }
    }


}

