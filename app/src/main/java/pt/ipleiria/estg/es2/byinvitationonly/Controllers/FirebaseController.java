package pt.ipleiria.estg.es2.byinvitationonly.Controllers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import pt.ipleiria.estg.es2.byinvitationonly.Database.DBAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Conference;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Message;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.Models.StatisticData;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;

public class FirebaseController {

    public static final String[] CONFERENCE_ATTRIBUTES = {"Abbreviation", "CallForPapers", "Dates",
            "FullName", "Location", "LogoURL", "Website", "Ratings", "Staff"};
    public static final String[] SESSION_ATTRIBUTES = {"Abstract", "Date", "End", "Presenter",
            "Room", "Start", "Title", "Track", "Ratings"};
    public static final String[] CONTACT_ATTRIBUTES = {"Name", "Email"};
    public static final String[] MESSAGE_ATTRIBUTES = {"Message", "Timestamp", "Read"};
    ////////////////////////
    //BD
    public static final String SERVER_URL = "https://my-bio-esof2.firebaseio.com";
    //
    ////////////////////////
    public static HashMap<ValueFetched, ValueEventListener> hashMapHandlerValueEvent = new HashMap<>();
    public static HashMap<ValueFetched, ChildEventListener> hashMapHandlerChildEvent = new HashMap<>();
    private static Firebase fb = new Firebase(SERVER_URL);
    public static Firebase fbContactsNode = fb.child("Contacts");
    public static Firebase fbSessionsNode = fb.child("Sessions");
    public static Firebase fbConferenceNode = fb.child("ConferenceData");
    public static Firebase fbMessagesNode = fb.child("Messages");
    private static ValueEventListener listener;
    private static boolean delayValues = false;
    private static LinkedList<Order> delayedOrders = new LinkedList<>();
    private static ValueEventListener listenerNotification;

    public static void sendContactData(Contact contact, Context context) {
        if (SharedPreferenceController.existLocalContactKey(context)) {
            updateContactOnServer(contact, context);
        } else {
            createNewContactOnServer(contact, context);
        }
    }

    private static void createNewContactOnServer(Contact contact, Context context) {
        String oldID;
        if (SharedPreferenceController.getLocalContactKey(context).contains("Anonymous")) {
            oldID = SharedPreferenceController.getLocalContactKey(context);

            Firebase newContact = fbContactsNode.push();
            newContact.child(CONTACT_ATTRIBUTES[0]).setValue(contact.getName());
            newContact.child(CONTACT_ATTRIBUTES[1]).setValue(contact.getEmail());
            saveContactKeyOnPhone(newContact.getKey(), context);

            // Atualiza o AnonymousID, pelo novo ID
            updateIdentifierOnRatings(oldID, newContact.getKey());
        } else {
            Firebase newContact = fbContactsNode.push();
            newContact.child(CONTACT_ATTRIBUTES[0]).setValue(contact.getName());
            newContact.child(CONTACT_ATTRIBUTES[1]).setValue(contact.getEmail());
            saveContactKeyOnPhone(newContact.getKey(), context);
        }
    }

    private static void updateIdentifierOnRatings(final String oldID, final String newID) {

        // Itera os ratings da conferencia e atualiza o rating com o novo ID
        ValueEventListener conferenceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot rating : dataSnapshot.getChildren()) {
                    if (rating.getKey().equals(oldID)) {
                        String oldRating = (String) rating.getValue();
                        Map<String, Object> conferenceRating = new HashMap<>();
                        conferenceRating.put(newID, oldRating);
                        fbConferenceNode.child(CONFERENCE_ATTRIBUTES[7]).updateChildren(conferenceRating);
                        fbConferenceNode.child(CONFERENCE_ATTRIBUTES[7]).child(oldID).removeValue();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        fbConferenceNode.child(CONFERENCE_ATTRIBUTES[7]).addListenerForSingleValueEvent(conferenceListener);


        //iterar as sessoes, iterar os rating das sessoes
        ValueEventListener sessionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot session : dataSnapshot.getChildren()) {
                    for (DataSnapshot rating : session.child(SESSION_ATTRIBUTES[8]).getChildren()) {
                        if (rating.getKey().equals(oldID)) {
                            String oldRating = (String) rating.getValue();
                            Map<String, Object> conferenceRating = new HashMap<>();
                            conferenceRating.put(newID, oldRating);
                            session.getRef().child(SESSION_ATTRIBUTES[8]).updateChildren(conferenceRating);
                            session.getRef().child(SESSION_ATTRIBUTES[8]).child(oldID).removeValue();
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        fbSessionsNode.addListenerForSingleValueEvent(sessionsListener);


    }

    public static void sendMessage(Message m) {
        createNewMessage(m);
    }

    private static void createNewMessage(Message m) {
        // Criação de um nó para um novo recetor
        Firebase rec = fbMessagesNode.child(m.getReceiver().getContactInAscii());
        // Criação de um nó para um novo emissor para esse recetor
        Firebase sen = rec.child(m.getSender().getContactInAscii());
        // Criação de um ID para um nova mensagem
        Firebase newMessage = sen.push();
        // Gravação da mensagem no emissor para esse recetor
        newMessage.child(MESSAGE_ATTRIBUTES[0]).setValue(m.getMessage());
        newMessage.child(MESSAGE_ATTRIBUTES[1]).setValue(m.getTimestamp().toString());
        newMessage.child(MESSAGE_ATTRIBUTES[2]).setValue(String.valueOf(m.isRead()));
    }

    private static void saveContactKeyOnPhone(String newContactKey, Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(SharedPreferenceController.MY_LOCAL_CONTACT_KEY, newContactKey);
        editor.apply();
    }

    private static void updateContactOnServer(Contact contact, Context context) {
        String localContactKey = SharedPreferenceController.getLocalContactKey(context);
        // Referência para o node do contacto a atualizar
        Firebase storedContact = fbContactsNode.child(localContactKey);
        // Criação e preenchimento de um hashmap do contacto com os dados atualizados
        Map<String, Object> updatedContact = new HashMap<>();
        updatedContact.put(CONTACT_ATTRIBUTES[0], contact.getName());
        updatedContact.put(CONTACT_ATTRIBUTES[1], contact.getEmail());
        // Atualização na base de dados
        storedContact.updateChildren(updatedContact);
    }

    public static void removeContactOnServer(Context context) {
        if (SharedPreferenceController.existLocalContactKey(context)) {
            String localContactKey = SharedPreferenceController.getLocalContactKey(context);
            Firebase storedContact = fbContactsNode.child(localContactKey);
            storedContact.removeValue();
        }
    }

    public static void getConferenceSessionsContinuous(final ValueFetched<LinkedList<Session>> handler, final Context context) {

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedList<Session> sessionlist = new LinkedList<>();
                for (DataSnapshot session : dataSnapshot.getChildren()) {
                    if (hasAllAttributes(session)) {
                        sessionlist.add(createNewSession(session, context));
                    }
                }
                callHandler(handler, sessionlist);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        fbSessionsNode.addValueEventListener(listener);
        hashMapHandlerValueEvent.put(handler, listener);
    }

    public static void getConferenceSessionsOnce(final ValueFetched<LinkedList<Session>> handler, final Context context) {

        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedList<Session> sessionlist = new LinkedList<>();
                for (DataSnapshot session : dataSnapshot.getChildren()) {
                    sessionlist.add(createNewSession(session, context));
                }
                callHandler(handler, sessionlist);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        fbSessionsNode.addListenerForSingleValueEvent(listener);
        hashMapHandlerValueEvent.put(handler, listener);
    }

    public static void getContacts(final ValueFetched<LinkedList<Contact>> handler, final String localContactKey) {
        ValueEventListener listener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedList<Contact> contactsList = new LinkedList<>();
                for (DataSnapshot contact : dataSnapshot.getChildren()) {
                    if (!(contact.getRef().toString().endsWith(localContactKey))) {
                        contactsList.add(createNewContact(contact));
                    }
                }
                handler.valuesFetched(contactsList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        };
        fbContactsNode.addValueEventListener(listener);
        hashMapHandlerValueEvent.put(handler, listener);
    }

    public static void getMessages(final Context appContext) {

        listenerNotification = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                LinkedList<String> sendersForRead = new LinkedList<>();
                for (DataSnapshot sender : dataSnapshot.getChildren()) {
                    for (DataSnapshot message : sender.getChildren()) {
                        if (message.getChildrenCount() == 3) {
                            String emailSender = sender.getKey();
                            for (DataSnapshot data : message.getChildren()) {
                                if (data.getKey().contains(MESSAGE_ATTRIBUTES[2])) {
                                    if (!Boolean.valueOf(data.getValue().toString())) {
                                        if (!sendersForRead.contains(emailSender))
                                            sendersForRead.add(emailSender);
                                    }
                                }
                            }
                        }
                    }
                }

                if (!sendersForRead.isEmpty()) {

                    Intent rI = new Intent(appContext, MainActivity.class);
                    rI.putExtra(MainActivity.EXTRA_NOTIFICATION, true);
                    rI.putExtra("lista", sendersForRead);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(appContext);
                    stackBuilder.addParentStack(MainActivity.class);

                    stackBuilder.addNextIntent(rI);
                    PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification notification = new Notification.Builder(appContext)
                            .setContentTitle(appContext.getString(R.string.notification_new_message))
                            .setContentText(sendersForRead.size() + appContext.getString(R.string.notification_people_message))
                            .setContentIntent(resultPendingIntent)
                            .setAutoCancel(true)
                            .setSmallIcon(R.drawable.ic_launcher_notification).build();

                    NotificationManager mNotificationManager =
                            (NotificationManager) appContext.getSystemService(Context.NOTIFICATION_SERVICE);

                    mNotificationManager.notify(0, notification);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }

        };
        Firebase chatNode = fbMessagesNode.child(SharedPreferenceController.getLocalStoredUserContact(appContext).getContactInAscii());
        chatNode.addValueEventListener(listenerNotification);
    }

    private static Contact createNewContact(DataSnapshot contact) {
        Object o = contact.getValue();
        String name = "";
        String email = "";

        if (o instanceof HashMap) {
            HashMap contactHash = (HashMap) o;

            if (contactHash.containsKey(CONTACT_ATTRIBUTES[0])) {
                name = (String) contactHash.get(CONTACT_ATTRIBUTES[0]);
            }
            if (contactHash.containsKey(CONTACT_ATTRIBUTES[1])) {
                email = (String) contactHash.get(CONTACT_ATTRIBUTES[1]);
            }
        }
        return new Contact(name, email);
    }

    private static Message createNewMessage(DataSnapshot messageSnapshot, Contact otherContact, Contact myContact) {
        Object o = messageSnapshot.getValue();
        String message = "";
        String timestamp = "";
        String lida = "";

        if (o instanceof HashMap) {
            HashMap contactHash = (HashMap) o;

            if (contactHash.containsKey(MESSAGE_ATTRIBUTES[0])) {
                message = (String) contactHash.get(MESSAGE_ATTRIBUTES[0]);
            }
            if (contactHash.containsKey(MESSAGE_ATTRIBUTES[1])) {
                timestamp = (String) contactHash.get(MESSAGE_ATTRIBUTES[1]);
            }
            if (contactHash.containsKey(MESSAGE_ATTRIBUTES[2])) {
                lida = (String) contactHash.get(MESSAGE_ATTRIBUTES[2]);
            }
        }
        return new Message(myContact, otherContact, message, lida, timestamp);
    }

    private static boolean hasAllAttributes(DataSnapshot session) {
        Object o = session.getValue();

        if (o instanceof HashMap) {
            HashMap sessionHash = (HashMap) o;

            if (!sessionHash.containsKey(SESSION_ATTRIBUTES[0])) {
                return false;
            }
            if (!sessionHash.containsKey(SESSION_ATTRIBUTES[1])) {
                return false;
            }
            if (!sessionHash.containsKey(SESSION_ATTRIBUTES[2])) {
                return false;
            }
            if (!sessionHash.containsKey(SESSION_ATTRIBUTES[3])) {
                return false;
            }
            if (!sessionHash.containsKey(SESSION_ATTRIBUTES[4])) {
                return false;
            }
            if (!sessionHash.containsKey(SESSION_ATTRIBUTES[5])) {
                return false;
            }
            if (!sessionHash.containsKey(SESSION_ATTRIBUTES[6])) {
                return false;
            }
            if (!sessionHash.containsKey(SESSION_ATTRIBUTES[7])) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    private static Session createNewSession(DataSnapshot session, Context context) {
        Object o = session.getValue();
        String abstract_session = "";
        String date = "";
        String end = "";
        String presenter = "";
        String room = "";
        String start = "";
        String title = "";
        String track = "";
        String myRating = "0";

        if (o instanceof HashMap) {
            HashMap sessionHash = (HashMap) o;

            if (sessionHash.containsKey(SESSION_ATTRIBUTES[0])) {
                abstract_session = (String) sessionHash.get(SESSION_ATTRIBUTES[0]);
            }
            if (sessionHash.containsKey(SESSION_ATTRIBUTES[1])) {
                date = (String) sessionHash.get(SESSION_ATTRIBUTES[1]);
            }
            if (sessionHash.containsKey(SESSION_ATTRIBUTES[2])) {
                end = (String) sessionHash.get(SESSION_ATTRIBUTES[2]);
            }
            if (sessionHash.containsKey(SESSION_ATTRIBUTES[3])) {
                presenter = (String) sessionHash.get(SESSION_ATTRIBUTES[3]);
            }
            if (sessionHash.containsKey(SESSION_ATTRIBUTES[4])) {
                room = (String) sessionHash.get(SESSION_ATTRIBUTES[4]);
            }
            if (sessionHash.containsKey(SESSION_ATTRIBUTES[5])) {
                start = (String) sessionHash.get(SESSION_ATTRIBUTES[5]);
            }
            if (sessionHash.containsKey(SESSION_ATTRIBUTES[6])) {
                title = (String) sessionHash.get(SESSION_ATTRIBUTES[6]);
            }
            if (sessionHash.containsKey(SESSION_ATTRIBUTES[7])) {
                track = (String) sessionHash.get(SESSION_ATTRIBUTES[7]);
            }
            if (sessionHash.containsKey(SESSION_ATTRIBUTES[8])) {
                if (session.child(SESSION_ATTRIBUTES[8]).hasChild(SharedPreferenceController.getUserID(context))) {
                    myRating = (String) session.child(SESSION_ATTRIBUTES[8]).child(SharedPreferenceController.getUserID(context)).getValue();
                }
            }
        }

        DBAdapter dbAdapter = new DBAdapter(context);
        Session s = new Session(date, start, end, room, track, title,
                presenter, abstract_session, myRating, session.getRef().toString(), "false");
        s.setOnAgenda(dbAdapter.existsSessionOnAgenda(s));
        return s;
    }

    public static void getConferenceData(final ValueFetched<Conference> handler, final Context context) {

        listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot conference) {
                Object o = conference.getValue();
                String abbreviation = "";
                String fullName = "";
                String location = "";
                String dates = "";
                String logoURL = "";
                String website = "";
                String callForPapers = "";
                String myRating = "0";
                boolean canCreate = true;

                if (o instanceof HashMap) {
                    HashMap conferenceHash = (HashMap) o;

                    if (conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[0])) {
                        abbreviation = (String) conferenceHash.get(CONFERENCE_ATTRIBUTES[0]);
                    }
                    if (conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[1])) {
                        callForPapers = (String) conferenceHash.get(CONFERENCE_ATTRIBUTES[1]);
                    }
                    if (conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[2])) {
                        dates = (String) conferenceHash.get(CONFERENCE_ATTRIBUTES[2]);
                    }
                    if (conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[3])) {
                        fullName = (String) conferenceHash.get(CONFERENCE_ATTRIBUTES[3]);
                    }
                    if (conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[4])) {
                        location = (String) conferenceHash.get(CONFERENCE_ATTRIBUTES[4]);
                    }
                    if (conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[5])) {
                        logoURL = (String) conferenceHash.get(CONFERENCE_ATTRIBUTES[5]);
                    }
                    if (conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[6])) {
                        website = (String) conferenceHash.get(CONFERENCE_ATTRIBUTES[6]);
                    }
                    if (conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[7])) {
                        if (conference.child(CONFERENCE_ATTRIBUTES[7]).hasChild(SharedPreferenceController.getUserID(context))) {
                            myRating = (String) conference.child(CONFERENCE_ATTRIBUTES[7]).child(SharedPreferenceController.getUserID(context)).getValue();
                        }
                    }


                    if (!(conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[0]) ||
                            conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[1]) ||
                            conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[2]) ||
                            conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[3]) ||
                            conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[4]) ||
                            conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[5]) ||
                            conferenceHash.containsKey(CONFERENCE_ATTRIBUTES[6]))) {
                        canCreate = false;
                    }
                }

                if (canCreate) {
                    Conference newConference = new Conference(abbreviation, fullName, location, dates,
                            logoURL, website, callForPapers, Float.parseFloat(myRating), conference.getRef().toString());
                    handler.valuesFetched(newConference);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        };
        fbConferenceNode.addValueEventListener(listener);
        hashMapHandlerValueEvent.put(handler, listener);
    }

    public static void cancelValueEventHandler(ValueFetched handler) {
        fbConferenceNode.removeEventListener(hashMapHandlerValueEvent.get(handler));
    }

    public static void cancelChildEventHandler(ValueFetched handler, Firebase node) {
        node.removeEventListener(hashMapHandlerChildEvent.get(handler));
    }

    public static void cancelEventNotification(Context appContext) {
        Firebase chatNode = fbMessagesNode.child(SharedPreferenceController.getLocalStoredUserContact(appContext).getContactInAscii());
        if (listenerNotification != null)
            chatNode.removeEventListener(listenerNotification);
    }

    private static void callHandler(final ValueFetched<LinkedList<Session>> handler, LinkedList<Session> sessions) {
        if (!delayValues)
            handler.valuesFetched(sessions);
        else {
            delayedOrders.add(new Order(handler, sessions));
        }
    }

    public static void setOrdersDelayed(boolean value) {
        delayValues = value;
        if (!delayValues) {
            for (Order o : delayedOrders) {
                o.execute();
            }
            delayedOrders.clear();
        }
    }

    public static Firebase[] getMyChatOnce(final ValueFetched<LinkedList<Message>> handler, final Contact otherContact, final Contact myContact) {

        // sen1 => no das mensagens que eu enviei para o outro contacto
        Firebase rec1 = fbMessagesNode.child(myContact.getContactInAscii());
        Firebase sen1 = rec1.child(otherContact.getContactInAscii());

        // sen2 => no das mensagens que o outro contacto enviou para mim
        Firebase rec2 = fbMessagesNode.child(otherContact.getContactInAscii());
        Firebase sen2 = rec2.child(myContact.getContactInAscii());

        final LinkedList<Message> allMessages = new LinkedList<>();

        ValueEventListener listener1 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedList<Message> messageList = new LinkedList<>();
                for (DataSnapshot message : dataSnapshot.getChildren()) {
                    Message newMessage = createNewMessage(message, otherContact, myContact);
                    messageList.add(newMessage);
                }
                allMessages.addAll(messageList);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        ValueEventListener listener2 = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedList<Message> messageList = new LinkedList<>();
                for (DataSnapshot message : dataSnapshot.getChildren()) {
                    Message newMessage = createNewMessage(message, myContact, otherContact);
                    messageList.add(newMessage);
                }
                allMessages.addAll(messageList);
                handler.valuesFetched(allMessages);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        sen1.addListenerForSingleValueEvent(listener1);
        sen2.addListenerForSingleValueEvent(listener2);
        hashMapHandlerValueEvent.put(handler, listener);
        return new Firebase[]{sen1, sen2};
    }

    public static Firebase getNewMessages(final ValueFetched<Message> handler, final Contact otherContact, final Contact myContact) {

        Firebase myNewReceivedMessagesNode = fbMessagesNode
                .child(myContact.getContactInAscii())
                .child(otherContact.getContactInAscii());

        ChildEventListener listener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                //HashMap o = (HashMap) dataSnapshot.getValue();
                //long counter = dataSnapshot.getChildrenCount();
                //Message newMessage = createNewMessage(dataSnapshot, otherContact, myContact);
                //handler.valuesFetched(newMessage);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getChildrenCount() == 3) {
                    Message newMessage = createNewMessage(dataSnapshot, otherContact, myContact);
                    if (!newMessage.isRead()) {
                        handler.valuesFetched(newMessage);
                    }
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        myNewReceivedMessagesNode.addChildEventListener(listener);
        hashMapHandlerChildEvent.put(handler, listener);
        return myNewReceivedMessagesNode;
    }

    public static void setMessagesAsRead(Firebase[] nodes) {
        final Map<String, Object> messageRead = new HashMap<>();
        messageRead.put(MESSAGE_ATTRIBUTES[2], "true");

        ValueEventListener vel = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    child.getRef().updateChildren(messageRead);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        nodes[0].addListenerForSingleValueEvent(vel); // node of received messages from sender
    }

    public static void sendSessionRating(Session session, String localContactKey) {
        if (session.getFirebaseSessionNode() != null) {
            Firebase sessionRatingsNode = new Firebase(session.getFirebaseSessionNode()).child(CONFERENCE_ATTRIBUTES[7]);
            Map<String, Object> sessionRating = new HashMap<>();
            sessionRating.put(localContactKey, String.valueOf(session.getMyRating()));
            sessionRatingsNode.updateChildren(sessionRating);
        }
    }

    public static void sendConferenceRating(Conference conference, String localContactKey) {
        if (conference.getFirebaseConferenceNode() != null) {
            Firebase sessionRatingsNode = new Firebase(conference.getFirebaseConferenceNode()).child(CONFERENCE_ATTRIBUTES[7]);
            Map<String, Object> sessionRating = new HashMap<>();
            sessionRating.put(localContactKey, String.valueOf(conference.getMyRating()));
            sessionRatingsNode.updateChildren(sessionRating);
        }
    }

    public static void isStaffContact(final Contact localStoredUserContact, final ValueFetched<Boolean> handler) {
        final boolean[] isStaff = {false};
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot staffMember : dataSnapshot.getChildren()) {
                    if (((String) staffMember.getValue()).toLowerCase().equals(localStoredUserContact.getEmail().toLowerCase())) {
                        isStaff[0] = true;
                    }
                }
                handler.valuesFetched(isStaff[0]);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };
        fbConferenceNode.child(CONFERENCE_ATTRIBUTES[8]).addListenerForSingleValueEvent(listener);
    }

    public static void getConferenceStatistics(final ValueFetched<StatisticData> conferenceHandler) {

        ValueEventListener conferenceListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String title = "";
                int numRatings = 0;
                float totalRating = 0.0f;

                for (DataSnapshot conferenceData : dataSnapshot.getChildren()) {
                    // title
                    if (conferenceData.getKey().equals(CONFERENCE_ATTRIBUTES[3])) {
                        title = (String) conferenceData.getValue();
                    }
                    // ratings
                    if (conferenceData.getKey().equals(CONFERENCE_ATTRIBUTES[7])) {
                        for (DataSnapshot rating : conferenceData.getChildren()) {
                            numRatings++;
                            totalRating = totalRating + Float.valueOf(String.valueOf(rating.getValue()));
                        }
                    }
                }

                StatisticData conferenceStatisticData = new StatisticData(title, String.valueOf(numRatings), (totalRating / numRatings));
                conferenceHandler.valuesFetched(conferenceStatisticData);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        fbConferenceNode.addValueEventListener(conferenceListener);
        hashMapHandlerValueEvent.put(conferenceHandler, conferenceListener);
    }

    public static void getSessionsStatistics(final ValueFetched<LinkedList<StatisticData>> sessionsHandler) {

        ValueEventListener sessionsListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                LinkedList<StatisticData> sessionsStatisticData = new LinkedList<>();
                for (DataSnapshot session : dataSnapshot.getChildren()) {
                    String title = "";
                    int numRatings = 0;
                    float totalRating = 0.0f;

                    for (DataSnapshot sessionData : session.getChildren()) {
                        // title
                        if (sessionData.getKey().equals(SESSION_ATTRIBUTES[6])) {
                            title = (String) sessionData.getValue();
                        }
                        // ratings
                        if (sessionData.getKey().equals(SESSION_ATTRIBUTES[8])) {
                            for (DataSnapshot rating : sessionData.getChildren()) {
                                numRatings++;
                                totalRating = totalRating + Float.valueOf(String.valueOf(rating.getValue()));
                            }
                        }
                    }
                    sessionsStatisticData.add(new StatisticData(title, String.valueOf(numRatings), (totalRating / numRatings)));
                }
                sessionsHandler.valuesFetched(sessionsStatisticData);
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        };

        fbSessionsNode.addValueEventListener(sessionsListener);
        hashMapHandlerValueEvent.put(sessionsHandler, sessionsListener);
    }

    public interface ValueFetched<TData> {
        void valuesFetched(TData data);
    }

    private static final class Order {
        ValueFetched<LinkedList<Session>> handler;
        LinkedList<Session> sessions;

        public Order(ValueFetched<LinkedList<Session>> handler, LinkedList<Session> sessions) {
            this.handler = handler;
            this.sessions = sessions;
        }

        public void execute() {
            handler.valuesFetched(sessions);
        }
    }

}
