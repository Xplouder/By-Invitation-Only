package pt.ipleiria.estg.es2.byinvitationonly.US09;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.robotium.solo.Solo;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.ContactChatActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Database.DBAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ConferenceScheduleFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.WhoIsHereFragment;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.R;


public class MainActivityTestCase extends ActivityInstrumentationTestCase2<MainActivity> {

    protected Solo solo;
    private boolean msgExist = false;
    private int result;
    private LinkedList<String> contactKeyList = new LinkedList<>();
    private Contact testContact = new Contact("Test", "test@example.pt");


    public MainActivityTestCase() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setImHereSharedPrefs(false);
        solo = new Solo(getInstrumentation(), getActivity());
        enableCommunications();
        deleteAllSessionsOnAgenda(getActivity());
        removeAllSessionsOnServer();
        removeAllContactsOnServer();
        removeAllMessagesOnServer();
        createNewContactOnServer(testContact);
        solo.unlockScreen();
        checkDrawerStatusIfOpenThenClose();
    }

    @Override
    protected void tearDown() throws Exception {
        final String localContactKey = getLocalContactKey();
        if (!localContactKey.isEmpty()) {
            Firebase storedContact = FirebaseController.fbContactsNode.child(localContactKey);
            storedContact.removeValue();
        }
        solo.finishOpenedActivities();
        super.tearDown();
    }

    //[x]1
    /*Dado que estou no ecrã de "chat" e não tenho rede, quando seleciono a opção
    "Enviar Agenda" então deve-me aparecer uma mensagem informativa a informar que não existe rede. */
    public void testNoConn() {
        confirmSubmission();
        ImageView iv = (ImageView) solo.getView(R.id.imageMessageIcon);
        solo.clickOnView(iv);
        solo.waitForActivity(ContactChatActivity.class);
        solo.setWiFiData(false);
        ImageButton ib = (ImageButton) solo.getView(R.id.buttonAttachCalendar);
        solo.clickOnView(ib);
        assertTrue("Nao apareceu o titulo respetivo a mensagem informativa que não existe rede", solo.searchText(
                solo.getString(R.string.warning)));
        assertTrue("Nao apareceu a mensagem respetiva a mensagem informativa que não existe rede", solo.searchText(
                solo.getString(R.string.error_connectivity)));
    }

    //[x]2
    /*Dado que estou no ecrã de "chat", tenho rede e a minha agenda está vazia,
    quando seleciono a opção "Enviar Agenda" então deve ser apresentado uma
    mensagem informativa a informar que a minha agenda está vazia e não envia
     a minha agenda ao contacto com que estou a conversar.*/
    public void testEmptyAgenda() {
        confirmSubmission();
        ImageView iv = (ImageView) solo.getView(R.id.imageMessageIcon);
        solo.clickOnView(iv);
        ImageButton ib = (ImageButton) solo.getView(R.id.buttonAttachCalendar);
        solo.clickOnView(ib);
        solo.waitForDialogToOpen();
        assertTrue("Nao apareceu o titulo respetivo a mensagem informativa que a agenda está vazia", solo.searchText(
                solo.getString(R.string.warning)));
        assertTrue("Nao apareceu a mensagem respetiva a mensagem informativa que a agenda está vazia", solo.searchText(
                solo.getString(R.string.empty_agenda)));
        assertFalse("A mensagem foi enviada para o servidor", isMessageOnServer(""));
    }

    //[x]3
    /*Dado que estou no ecrã de "Chat", tenho rede e a minha agenda não está vazia, quando seleciono a
    opção de enviar agenda deve ser apresentada uma caixa de dialogo com a confirmação do envio.*/
    public void testSendAgendaCheckDialog() {
        removeAllSessionsOnServer();
        Session session = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(session);
        openScheduleFrag();
        CheckBox cb = (CheckBox) solo.getView(R.id.checkBox);
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        confirmSubmission();
        ImageView iv = (ImageView) solo.getView(R.id.imageMessageIcon);
        solo.clickOnView(iv);
        ImageButton ib = (ImageButton) solo.getView(R.id.buttonAttachCalendar);
        solo.clickOnView(ib);
        assertTrue("Nao apareceu a confirmação do envio", solo.searchText(solo.getString(R.string.confirmation_send_agenda)));
    }

    //[x]4
    /*Dado que estou na caixa dialogo de confirmação de envio da agenda,
    quando carrego na opção "sim" então deve ser enviada a listagem de sessoes
    (informação a apresentar de cada agenda agenda:
    dia, hora inicio, hora fim, titulo e room ) presentes na minha agenda
    para o contacto que estou a conversar. */
    public void testSendAgenda() {
        testSendAgendaCheckDialog();
        solo.clickOnText(solo.getString(R.string.yes));
        solo.sleep(2000);
        assertTrue("A mensagem nao foi enviada para o servidor", isMessageOnServer(getAgendaForAttach()));
        Session s = getAgenda().getFirst();
        assertTrue("O dia não foi enviado", solo.searchText(s.getDateFormattedString()));
        assertTrue("A hora de inicio nao foi enviada", solo.searchText(s.getStartHour()));
        assertTrue("A hora de fim nao foi enviada", solo.searchText(s.getEndHour()));
        assertTrue("O titulo nao foi enviado", solo.searchText(s.getTitle()));
        assertTrue("A room nao foi enviada", solo.searchText(s.getRoom()));
    }

    //[x]5
    /*Dado que estou na caixa dialogo de confirmação de envio da agenda,
    quando carrego na opção "não" então não deve ser enviada a minha agenda
    para o contacto que estou a conversar */
    public void testeDontSendAgenda() {
        testSendAgendaCheckDialog();
        solo.clickOnText(solo.getString(R.string.no));
        assertFalse("A mensagem foi enviada para o servidor", isMessageOnServer(getAgendaForAttach()));
    }

    /*
              _
             ( )
              H
              H
             _H_
          .-'-.-'-.
         /         \
        |           |
        |   .-------'._
        |  / /  '.' '. \
        |  \ \ @   @ / /
        |   '---------'
        |    _______|
        |  .'-+-+-+|
        |  '.-+-+-+|    AUXILIAR FUNCTIONS
        |    """""" |
        '-.__   __.-'
             """
    */

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
        DBAdapter dbAdapter = new DBAdapter(getActivity());
        LinkedList<Session> sessionsOnAgenda = new LinkedList<>();
        for (Session s : dbAdapter.getSessions()) {
            if (s.isOnAgenda()) {
                sessionsOnAgenda.add(s);
            }
        }
        return sessionsOnAgenda;
    }

    private LinkedList<Session> orderByDate(LinkedList<Session> sessionList) {
        Collections.sort(sessionList);
        return sessionList;
    }

    private void removeAllSessionsOnServer() {
        Firebase allSessions = FirebaseController.fbSessionsNode;
        allSessions.removeValue();
        solo.sleep(1000);
    }

    private String getTodayDate() {
        GregorianCalendar calc = new GregorianCalendar();
        int year = calc.get(Calendar.YEAR);
        int month = calc.get(Calendar.MONTH) + 1;
        int day = calc.get(Calendar.DAY_OF_MONTH);
        return year + "/" + month + "/" + day;
    }

    private String getTodayHourAndMinutes() {
        GregorianCalendar calendar = new GregorianCalendar();
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour < 10 ? ("0" + hour) : (hour)) + ":" + (minute < 10 ? ("0" + minute) : (minute));
    }

    private String getTodayHourAndMinutesPlusMinutes(int plusMinutes) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(Calendar.MINUTE, plusMinutes);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour < 10 ? ("0" + hour) : (hour)) + ":" + (minute < 10 ? ("0" + minute) : (minute));
    }

    private void createSessionOnServer(Session newSession) {
        Firebase fbSessions = FirebaseController.fbSessionsNode;
        Firebase newSessionKey = fbSessions.push();
        newSessionKey.child(FirebaseController.SESSION_ATTRIBUTES[0]).setValue(newSession.getAbstracts());
        newSessionKey.child(FirebaseController.SESSION_ATTRIBUTES[1]).setValue(newSession.getDateFormattedString());
        newSessionKey.child(FirebaseController.SESSION_ATTRIBUTES[2]).setValue(newSession.getEndHour());
        newSessionKey.child(FirebaseController.SESSION_ATTRIBUTES[3]).setValue(newSession.getPresenter());
        newSessionKey.child(FirebaseController.SESSION_ATTRIBUTES[4]).setValue(newSession.getRoom());
        newSessionKey.child(FirebaseController.SESSION_ATTRIBUTES[5]).setValue(newSession.getStartHour());
        newSessionKey.child(FirebaseController.SESSION_ATTRIBUTES[6]).setValue(newSession.getTitle());
        newSessionKey.child(FirebaseController.SESSION_ATTRIBUTES[7]).setValue(newSession.getTrack());
        // Tempo para a criação da sessão no servidor ser efectuada
        solo.sleep(4000);
    }

    private void openScheduleFrag() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText(solo.getString(R.string.title_conference_schedule_fragment));
        solo.sleep(1000);
        assertTrue("O fragmento do calendario da conferencia nao foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof ConferenceScheduleFragment);
    }

    private void deleteAllSessionsOnAgenda(Context context) {
        DBAdapter dbAdapter = new DBAdapter(context);
        dbAdapter.removeAllSessions();
    }

    private boolean isMessageOnServer(final String message) {
        Firebase f = FirebaseController.fbMessagesNode
                .child(testContact.getContactInAscii())
                .child(getLocalContact().getContactInAscii());

        final Object obj = new Object();

        f.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    for (DataSnapshot childChild : child.getChildren()) {
                        if (childChild.getKey().contains(FirebaseController.MESSAGE_ATTRIBUTES[0])) {
                            String m = (String) childChild.getValue();
                            if (m.contains(message)) {
                                msgExist = true;
                                synchronized (obj) {
                                    obj.notifyAll();
                                }
                            }
                        }
                    }
                }
                synchronized (obj) {
                    obj.notifyAll();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
        try {
            synchronized (obj) {
                obj.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return msgExist;
    }

    private Contact getLocalContact() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String name = prefs.getString(getActivity().getString(R.string.pref_key_name), "");
        String email = prefs.getString(getActivity().getString(R.string.pref_key_email), "");
        return new Contact(name, email);
    }

    private void confirmSubmission() {
        openConfirmWindow();
        solo.clickOnButton(solo.getString(R.string.yes));
        solo.waitForDialogToClose();

        //nota: não conseguimos garantir que temos rede, apenas podemos
        // ligar os componentes que permitam que o dispositivo tenha rede

        solo.setWiFiData(true);
        //solo.setMobileData(true); no nosso ambiente de testes isto não é necessario testar

        assertTrue("Não apareceu para o fragmento Who is Here",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof WhoIsHereFragment);

        solo.waitForView(R.id.action_i_am_here);

        int res = invokeContactDataRequestOnServer();
        assertTrue("Não existe no servidor o contacto teste criado", res == 1);

        final String localContactKey = getLocalContactKey();
        Firebase storedContact = FirebaseController.fbContactsNode.child(localContactKey);
        storedContact.removeValue();

        assertIcon("O icone não mudou para o esperado", R.id.action_i_am_here, R.drawable.ic_action_group);
    }

    private void openConfirmWindow() {
        setUserContact(new Contact("nome exemplo", "email@email.com"));
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        assertTrue("Não apareceu caixa de dialogo", solo.waitForDialogToOpen());
    }

    private void assertIcon(String message, int item, int waitedIcon) {
        TextView tv = (TextView) solo.getCurrentActivity().findViewById(item);
        BitmapDrawable icon = (BitmapDrawable) tv.getCompoundDrawables()[0];
        BitmapDrawable esperado = (BitmapDrawable) solo.getCurrentActivity().getDrawable(waitedIcon);
        assertTrue("Não foi encontrado o icon esperado", esperado != null);
        boolean same = esperado.getBitmap().sameAs(icon.getBitmap());
        assertTrue(message, same);
    }

    private int invokeContactDataRequestOnServer() {
        final String localContactKey = getLocalContactKey();

        result = 0;
        FirebaseController.fbContactsNode.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (localContactKey.endsWith(child.getKey())) {
                        if (compareReceivedContactData(child))
                            result = 1;
                        else
                            result = 2;
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        boolean exit = false;
        long start = System.currentTimeMillis();
        do {
            try {
                Thread.sleep(10);
                if (result != 0)
                    exit = true;
                if (System.currentTimeMillis() - start > 2000)
                    exit = true;
            } catch (InterruptedException ignored) {
            }
        } while (!exit);
        return result;
    }

    private void setUserContact(Contact contact) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity());
        SharedPreferences.Editor e = prefs.edit();
        e.putString(solo.getString(R.string.pref_key_name), contact.getName());
        e.putString(solo.getString(R.string.pref_key_email), contact.getEmail());
        e.apply();
    }

    private void removeContactsOnServer() {
        for (String contactKey : contactKeyList) {
            if (!contactKey.isEmpty()) {
                Firebase storedContact = new Firebase(contactKey);
                storedContact.removeValue();
            }
        }
    }

    private boolean compareReceivedContactData(DataSnapshot child) {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity().getApplicationContext());
        String local_name = pref.getString(solo.getString(R.string.pref_key_name), "");
        String local_email = pref.getString(solo.getString(R.string.pref_key_email), "");

        String server_contact_name = "";
        String server_contact_email = "";

        Object o = child.getValue();
        if (o instanceof HashMap) {
            HashMap contactHash = (HashMap) o;
            if (contactHash.containsKey("Name")) {
                server_contact_name = (String) contactHash.get("Name");
            }
            if (contactHash.containsKey("Email")) {
                server_contact_email = (String) contactHash.get("Email");
            }
        }

        return server_contact_name.equals(local_name) && server_contact_email.equals(local_email);
    }

    private void removeAllContactsOnServer() {
        Firebase allContacts = FirebaseController.fbContactsNode;
        allContacts.removeValue();
    }

    private void removeAllMessagesOnServer() {
        Firebase allContacts = FirebaseController.fbMessagesNode;
        allContacts.removeValue();
    }

    private String createNewContactOnServer(Contact contact) {
        Firebase contactsNode = FirebaseController.fbContactsNode;
        Firebase newcontact = contactsNode.push();
        newcontact.child(FirebaseController.CONTACT_ATTRIBUTES[0]).setValue(contact.getName());
        newcontact.child(FirebaseController.CONTACT_ATTRIBUTES[1]).setValue(contact.getEmail());
        contactKeyList.add(newcontact.toString());
        return newcontact.toString();
    }

    private void setImHereSharedPrefs(boolean imhere) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getInstrumentation().getTargetContext().getString(R.string.i_am_here), imhere);
        editor.apply();
    }

    private String getLocalContactKey() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity().getApplicationContext());
        return pref.getString(SharedPreferenceController.MY_LOCAL_CONTACT_KEY, "");
    }

    private void invertDrawerStatus() {
        try {
            solo.clickOnActionBarHomeButton();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void checkDrawerStatusIfOpenThenClose() {
        if (isDrawerStatusOpen()) {
            invertDrawerStatus();
        }
    }

    private boolean isDrawerStatusOpen() {
        DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void enableCommunications() {
        solo.setWiFiData(true);
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                FirebaseController.setOrdersDelayed(false);
            }
        });
        while (!NetworkController.existConnection(getActivity())) {
            solo.sleep(20);
        }
    }
}

