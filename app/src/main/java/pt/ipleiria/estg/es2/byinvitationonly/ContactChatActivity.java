package pt.ipleiria.estg.es2.byinvitationonly;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters.MyChatRecyclerViewAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.Database.DBAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Message;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;

public class ContactChatActivity extends Activity {

    public static String EXTRA_CONTACT = "contact";
    private Contact otherContact;
    private EditText txtMessage;
    private RecyclerView recyclerView;
    private MyChatRecyclerViewAdapter mDataAdapter;
    private FirebaseController.ValueFetched<Message> fireBaseHandler2;
    private Firebase[] intercommunicateNodes;
    private Firebase nodeNewReceivedMessages;
    private Context context;
    private DBAdapter dbAdapter;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.dbAdapter = new DBAdapter(getApplicationContext());
        setContentView(R.layout.activity_contact_chat);
        otherContact = (Contact) getIntent().getSerializableExtra(EXTRA_CONTACT);
        TextView textViewContact = (TextView) this.findViewById(R.id.textViewContactToChat);
        textViewContact.setText(otherContact.getName() + ":");
        txtMessage = (EditText) this.findViewById(R.id.editTextMessage);
        ImageButton ibSend = (ImageButton) findViewById(R.id.buttonSendMessage);
        ibSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = txtMessage.getText().toString();
                if (!NetworkController.existConnection(getApplicationContext())) {
                    showConnectivityError();
                } else {
                    if (message.isEmpty()) {
                        showEmptyMessageError();
                    } else {
                        sendMessage();
                    }
                }
            }
        });
        ImageButton ibAgenda = (ImageButton) findViewById(R.id.buttonAttachCalendar);
        ibAgenda.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkController.existConnection(getApplicationContext())) {
                    showConnectivityError();
                } else if (getAgenda().isEmpty()) {
                    showEmptyAgendaError();
                } else {
                    new AlertDialog.Builder(context)
                            .setMessage(getString(R.string.confirmation_send_agenda))
                            .setCancelable(false)
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    sendAgenda();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), null)
                            .show();
                }
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.recycler_chat);
        mDataAdapter = new MyChatRecyclerViewAdapter(this);
        recyclerView.setAdapter(mDataAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setVisibility(View.VISIBLE);

        pauseAndHideNotifications();

        FirebaseController.ValueFetched<LinkedList<Message>> fireBaseHandler1 =
                new FirebaseController.ValueFetched<LinkedList<Message>>() {
                    @Override
                    public void valuesFetched(LinkedList<Message> messages) {
                        changeAdapterData(sortListMessageByTimestamp(messages));
                    }
                };

        fireBaseHandler2 = new FirebaseController.ValueFetched<Message>() {
            @Override
            public void valuesFetched(Message message) {
                changeAdapterData(message);
            }
        };

        intercommunicateNodes = FirebaseController.getMyChatOnce(fireBaseHandler1, otherContact, SharedPreferenceController.getLocalStoredUserContact(this));
        nodeNewReceivedMessages = FirebaseController.getNewMessages(fireBaseHandler2, otherContact, SharedPreferenceController.getLocalStoredUserContact(this));
        this.context = this;
    }

    private void sendAgenda() {
        Message message = new Message(otherContact,
                SharedPreferenceController.getLocalStoredUserContact(this),
                getAgendaForAttach()
        );
        sendMessageToFirebase(message);
        changeAdapterData(message);
    }

    private LinkedList<Session> orderByDate(LinkedList<Session> sessionList) {
        Collections.sort(sessionList);
        return sessionList;
    }

    private String getAgendaForAttach() {
        StringBuilder sb = new StringBuilder();
        sb.append("My Agenda:\n\n");
        LinkedList<Session> sessions = getAgenda();
        sessions = orderByDate(sessions);
        int count = 1;
        for (Session s : sessions) {
            if (sessions.getLast().equals(s)) {
                sb.append("Session ").append(count++).append(":\n");
                sb.append(s.getTitle()).append("\n");
                sb.append(s.getDateFormattedString()).append("\n");
                sb.append(s.getStartHour()).append(" - ").append(s.getEndHour()).append("\n");
                sb.append(s.getRoom());
            } else {
                sb.append("Session ").append(count++).append(":\n");
                sb.append(s.getTitle()).append("\n");
                sb.append(s.getDateFormattedString()).append("\n");
                sb.append(s.getStartHour()).append(" - ").append(s.getEndHour()).append("\n");
                sb.append(s.getRoom()).append("\n\n");
            }
        }
        return sb.toString();
    }

    private LinkedList<Session> getAgenda() {
        LinkedList<Session> agenda = new LinkedList<>();
        for (Session s : dbAdapter.getSessions()) {
            if (s.isOnAgenda()) {
                agenda.add(s);
            }
        }
        return agenda;
    }

    private void pauseAndHideNotifications() {
        FirebaseController.cancelEventNotification(getApplicationContext());
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
    }

    private LinkedList<Message> sortListMessageByTimestamp(LinkedList<Message> chatList) {
        Collections.sort(chatList, new Comparator<Message>() {
            @Override
            public int compare(Message m1, Message m2) {
                return m1.getTimestamp().compareTo(m2.getTimestamp());
            }
        });
        return chatList;
    }

    private void showEmptyMessageError() {
        AlertDialog.Builder constructor = new AlertDialog.Builder(this);
        constructor.setTitle(getString(R.string.title_empty_message))
                .setMessage(getString(R.string.message_empty_message))
                .setNeutralButton(R.string.ok, null)
                .create()
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.empty, menu);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseController.cancelChildEventHandler(fireBaseHandler2, nodeNewReceivedMessages);
        FirebaseController.getMessages(getApplicationContext());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FirebaseController.cancelChildEventHandler(fireBaseHandler2, nodeNewReceivedMessages);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sendMessage() {
        Message message = new Message(otherContact,
                SharedPreferenceController.getLocalStoredUserContact(this),
                txtMessage.getText().toString()
        );

        sendMessageToFirebase(message);
        changeAdapterData(message);
        txtMessage.setText("");
    }

    private void sendMessageToFirebase(Message message) {
        FirebaseController.sendMessage(message);
    }

    private synchronized void changeAdapterData(final LinkedList<Message> chatList) {
        mDataAdapter.setChatList(chatList);
        recyclerView.smoothScrollToPosition(chatList.size());
        FirebaseController.setMessagesAsRead(intercommunicateNodes);
    }

    private synchronized void changeAdapterData(final Message message) {
        int lastPosition = mDataAdapter.addMessageToChatListMessage(message);
        recyclerView.smoothScrollToPosition(lastPosition);
        FirebaseController.setMessagesAsRead(intercommunicateNodes);
    }

    private void showConnectivityError() {
        AlertDialog.Builder construct = new AlertDialog.Builder(this);
        construct.setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.error_connectivity))
                .setNeutralButton(getString(R.string.ok), null)
                .create()
                .show();
    }

    private void showEmptyAgendaError() {
        AlertDialog.Builder construct = new AlertDialog.Builder(this);
        construct.setTitle(getString(R.string.warning))
                .setMessage(getString(R.string.empty_agenda))
                .setNeutralButton(getString(R.string.ok), null)
                .create()
                .show();
    }

}
