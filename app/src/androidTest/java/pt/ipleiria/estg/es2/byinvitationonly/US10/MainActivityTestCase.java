package pt.ipleiria.estg.es2.byinvitationonly.US10;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.robotium.solo.Solo;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FileController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.HomepageFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.StatisticsFragment;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.Models.StatisticData;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MainActivityTestCase extends ActivityInstrumentationTestCase2<MainActivity> {
    protected Solo solo;
    private int result;
    private LinkedList<String> sessionKeyList = new LinkedList<>();
    private LinkedList<StatisticData> listStatisticsData;
    private StatisticData statisticsDataConference;

    public MainActivityTestCase() {
        super(MainActivity.class);
    }

    public static void sendConferenceRating(Firebase keyNode, Float rating, String localContactKey) {
        if (keyNode != null) {
            Firebase sessionRatingsNode = keyNode.child(FirebaseController.CONFERENCE_ATTRIBUTES[7]);
            Map<String, Object> sessionRating = new HashMap<>();
            sessionRating.put(localContactKey, String.valueOf(rating));
            sessionRatingsNode.updateChildren(sessionRating);
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setImHereSharedPrefs(false);
        solo = new Solo(getInstrumentation(), getActivity());
        enableCommunications();
        deleteFileSessions(getActivity());
        removeAllSessions();
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
    //Dado que estou na aplicação e que sou um membro da comissão organizadora, quando clico no texto "Estatisticas"
    // do navegador deve-me ser apresentado o ecra "Estatisticas".
    public void testStatsFragmentOpen() {
        enableImHere();
        openStatsFrag();
    }

    //[x]2
    //Dado que sou um membro da comissão organizadora e estou no ecrã "Estatísticas"
    // deve-me ser apresentado a classificação media de cada sessão juntamente com o
    // número de classificacões obtidas por sessão.
    public void testSessionRatingMean() {
        Float ratingExpectedMean = createSessionsAndCalculateMeanRating();
        testStatsFragmentOpen();
        LinkedList<StatisticData> statisticsData = getSessionsMeansRatingsFromFirebase();
        assertEquals("A sessão 1 não tem o valor de rating correto", statisticsData.getFirst().getAverageRating(), ratingExpectedMean);

    }

    //[x]3
    //Dado que sou um membro da comissão organizadora e estou no ecrã "Estatísticas"
    // deve-me ser apresentado a classificação media do evento e o número de classificações obtidas do evento
    public void testEventRatingMean() {
        removeAllEventRating();
        Float ratingExpectedMean = calculateEventMeanRating();
        testStatsFragmentOpen();
        StatisticData statisticsData = getEventRatingFromFirebase();
        assertEquals("A conferencia não tem o valor de rating correto", statisticsData.getAverageRating(), ratingExpectedMean);
    }

    //[x]4
    //Dado que sou um membro da comissão organizadora e estou no ecrã "Estatísticas" se uma sessão listada
    // não tiver classificações recebidas então deve aparecer
    // invés uma mensagem a informar que não existem classificações.
    public void testSessionRatingNA() {
        Session session1 = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão 1",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(session1);
        testStatsFragmentOpen();
        solo.sleep(1000);
        RatingBar ratingbar = (RatingBar) getActivity().findViewById(R.id.ratingBarSession);
        assertTrue("A mensagem a informar que a sessao nao tme rating nao aparece", solo.searchText(solo.getString(R.string.n_a_string)));
        assertTrue("A ratingbar não aparece invisivel", ratingbar.getVisibility() == View.INVISIBLE);
    }

    //[x]5
    //Dado que sou um membro da comissão organizadora e estou no ecrã "Estatísticas"
    // se a conferência não tiver classificações recebidas então deve aparecer
    // invés uma mensagem a informar que não existem classificações.
    public void testEventRatingNA() {
        removeAllEventRating();
        testStatsFragmentOpen();
        solo.sleep(1000);
        RatingBar ratingbar = (RatingBar) getActivity().findViewById(R.id.ratingBarConference);
        assertTrue("A mensagem a informar que a sessao nao tme rating nao aparece", solo.searchText(solo.getString(R.string.n_a_string)));
        assertTrue("A ratingbar não aparece invisivel", ratingbar.getVisibility() == View.INVISIBLE);
    }

    //[x]6
    //Dado que estou na aplicação e que sou um membro da comissão organizadora e não tenho rede,
    // quando clico no texto "Estatisticas"
    // do navegador deve-me ser apresentada uma mensagem a informar que não existe rede.
    public void testErrorDialog() {
        confirmSubmission(true);
        solo.setWiFiData(false);
        solo.sleep(2000);
        solo.clickOnActionBarHomeButton();
        solo.sleep(1000);
        solo.clickOnText(solo.getString(R.string.title_statistics_fragment));
        solo.waitForDialogToOpen();
        assertTrue("A mensagem a informar que não tenho rede nao aparece", solo.searchText(solo.getString(R.string.error_connectivity)));

    }

    //[x]7
    /*Dado que estou no ecrã de "Estatísticas" quando desligo a opção
    I'm Here devo ser reencaminhado para o ecrã da página inicial.*/
    public void testAutomaticRedirectToHomepage() {
        testStatsFragmentOpen();
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        assertIcon("O icone não está no esperado", R.id.action_i_am_here, R.drawable.ic_action_alone);
        solo.sleep(1000);
        assertTrue("O fragmento da pagina inicial não foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof HomepageFragment);
    }

    //[x]8
    //Dado que não sou um membro da comissão organizadora,
    // quando faço "i'm here" não me deve ser visivel a opçao "estatisticas" no navegador
    public void testIfNotStaffMember() {
        setUserContact(new Contact("userNotStaffMember", "teste@teste.com"));
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        confirmSubmission(false);
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        solo.sleep(1000);
        solo.clickOnActionBarHomeButton();
        assertFalse("Opção de estatisticas aparece", solo.searchText(solo.getString(R.string.title_statistics_fragment)));
    }




  /*         _
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


    private void deleteFileSessions(Context context) {
        File file = new File(context.getFilesDir(), FileController.SESSIONS_FILE);
        if (file.exists()) {
            assertTrue("As sessoes nao foram apagadas", file.delete());
        }
    }

    private void removeAllSessions() {
        Firebase allSessions = FirebaseController.fbSessionsNode;
        allSessions.removeValue();
        solo.sleep(1000);
    }

    private void checkDrawerStatusIfOpenThenClose() {
        if (isDrawerStatusOpen()) {
            invertDrawerStatus();
        }
    }

    private String getLocalContactKey() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity().getApplicationContext());
        return pref.getString(SharedPreferenceController.MY_LOCAL_CONTACT_KEY, "");
    }

    private void removeSessionsOnServer() {
        for (String sessionKey : sessionKeyList) {
            if (!sessionKey.isEmpty()) {
                Firebase storedContact = new Firebase(sessionKey);
                storedContact.removeValue();
            }
        }
        solo.sleep(2000);
    }

    private void setImHereSharedPrefs(boolean imhere) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getInstrumentation().getTargetContext().getString(R.string.i_am_here), imhere);
        editor.apply();
    }

    private boolean isDrawerStatusOpen() {
        DrawerLayout mDrawerLayout = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        return mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void invertDrawerStatus() {
        try {
            solo.clickOnActionBarHomeButton();
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void enableImHere() {
        confirmSubmission(true);
        assertIcon("O icone não está no esperado", R.id.action_i_am_here, R.drawable.ic_action_group);
        // delay para aparecer a opção de estatisticas no drawe ou seja, a
        // resposta do servidor em como que o utilizador logado é da administracao
        solo.sleep(2000);
    }

    private void confirmSubmission(Boolean staffMember) {
        if (staffMember) {
            openConfirmWindow();
        }
        solo.clickOnButton(solo.getString(R.string.yes));
        solo.waitForDialogToClose();

        //nota: não conseguimos garantir que temos rede, apenas podemos
        // ligar os componentes que permitam que o dispositivo tenha rede

        solo.setWiFiData(true);
        //solo.setMobileData(true); no nosso ambiente de testes isto não é necessario testar

        assertTrue("Não mudou para a WhoIsHere", solo.searchText(solo.getString(R.string.title_who_is_here_fragment)));

        solo.waitForView(R.id.action_i_am_here);

        int res = invokeContactDataRequestOnServer();
        assertTrue("Não existe no servidor o contacto teste criado", res == 1);

        final String localContactKey = getLocalContactKey();
        Firebase storedContact = FirebaseController.fbContactsNode.child(localContactKey);
        storedContact.removeValue();

        assertIcon("O icone não mudou para o esperado", R.id.action_i_am_here, R.drawable.ic_action_group);
    }

    private void openConfirmWindow() {
        setUserContact(new Contact("userUS10", "jorge.jesus@bio.com"));
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        assertTrue("Não apareceu caixa de dialogo", solo.waitForDialogToOpen());
    }

    private void setUserContact(Contact contact) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity());
        SharedPreferences.Editor e = prefs.edit();
        e.putString(solo.getString(R.string.pref_key_name), contact.getName());
        e.putString(solo.getString(R.string.pref_key_email), contact.getEmail());
        e.apply();
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
                if (result != 0) {
                    exit = true;
                }
                if (System.currentTimeMillis() - start > 2000) {
                    exit = true;
                }
            } catch (InterruptedException ignored) {
            }
        } while (!exit);
        return result;
    }

    private void assertIcon(String message, int item, int waitedIcon) {
        TextView tv = (TextView) solo.getCurrentActivity().findViewById(item);
        BitmapDrawable icon = (BitmapDrawable) tv.getCompoundDrawables()[0];
        BitmapDrawable esperado = (BitmapDrawable) solo.getCurrentActivity().getDrawable(waitedIcon);
        assertTrue("Não foi encontrado o icon esperado", esperado != null);
        boolean same = esperado.getBitmap().sameAs(icon.getBitmap());
        assertTrue(message, same);
    }

    private void openStatsFrag() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText(solo.getString(R.string.title_statistics_fragment));
        solo.sleep(1000);
        assertTrue("O fragmento das estatisticas nao foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof StatisticsFragment);
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

    private Float createSessionsAndCalculateMeanRating() {
        Session session1 = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão 1",
                "Rato Mickey",
                "Nada a resumir.");
        Firebase keySession1 = createSessionOnServer(session1);

        Float[] ratings = {4.0f, 2.0f, 5.0f};

        sendSessionRating(keySession1, ratings[0], "user1");
        sendSessionRating(keySession1, ratings[1], "user2");
        sendSessionRating(keySession1, ratings[2], "user3");
        return (ratings[0] + ratings[1] + ratings[2]) / ratings.length;
    }

    private Float calculateEventMeanRating() {
        Session session1 = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão 1",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(session1);

        Float[] ratings = {4.0f, 2.0f, 5.0f};

        sendConferenceRating(FirebaseController.fbConferenceNode, ratings[0], "user1");
        sendConferenceRating(FirebaseController.fbConferenceNode, ratings[1], "user2");
        sendConferenceRating(FirebaseController.fbConferenceNode, ratings[2], "user3");
        return (ratings[0] + ratings[1] + ratings[2]) / ratings.length;
    }

    public void sendSessionRating(Firebase keyNode, Float rating, String localContactKey) {
        if (keyNode != null) {
            Firebase sessionRatingsNode = keyNode.child(FirebaseController.CONFERENCE_ATTRIBUTES[7]);
            Map<String, Object> sessionRating = new HashMap<>();
            sessionRating.put(localContactKey, String.valueOf(rating));
            sessionRatingsNode.updateChildren(sessionRating);
        }
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

    private Firebase createSessionOnServer(Session newSession) {
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
        sessionKeyList.add(newSessionKey.toString());
        // Tempo para a criação da sessão no servidor ser efectuada
        solo.sleep(4000);
        return newSessionKey;
    }

    public LinkedList<StatisticData> getSessionsMeansRatingsFromFirebase() {
        final Object obj = new Object();

        FirebaseController.getSessionsStatistics(new FirebaseController.ValueFetched<LinkedList<StatisticData>>() {
            @Override
            public void valuesFetched(LinkedList<StatisticData> statisticsData) {
                listStatisticsData = statisticsData;
                synchronized (obj) {
                    obj.notifyAll();
                }
            }
        });
        try {
            synchronized (obj) {
                obj.wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return listStatisticsData;
    }

    private StatisticData getEventRatingFromFirebase() {
        final Object obj = new Object();

        FirebaseController.getConferenceStatistics(new FirebaseController.ValueFetched<StatisticData>() {
            @Override
            public void valuesFetched(StatisticData statisticData) {
                statisticsDataConference = statisticData;
                synchronized (obj) {
                    obj.notifyAll();
                }
            }
        });
        try {
            synchronized (obj) {
                obj.wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return statisticsDataConference;
    }

    private void removeAllEventRating() {
        Firebase eventRatings = FirebaseController.fbConferenceNode.child("Ratings");
        eventRatings.removeValue();
        solo.sleep(1000);
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

