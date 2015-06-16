package pt.ipleiria.estg.es2.byinvitationonly.US01;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.firebase.client.Firebase;
import com.robotium.solo.Solo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FileController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters.MyActiveSessionsRecyclerViewAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.DetailsSessionActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ActiveSessionsFragment;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MainActivityTestCase extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
    private LinkedList<String> sessionKeyList = new LinkedList<>();

    public MainActivityTestCase() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setImHereSharedPrefs(false);
        solo = new Solo(getInstrumentation(), getActivity());
        enableCommunications();
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

    //[x]
    /*Dado que estou na aplicação, deve-me ser mostrado o texto "Sessões Ativas" no navegador.*/
    public void testFindActiveSessionOnNavDrawer() {
        if (!isDrawerStatusOpen()) {
            solo.clickOnActionBarHomeButton();
        }
        assertTrue("Não foi encontrado o texto Sessões Ativas no drawer", solo.searchText(solo.getString(R.string.title_active_session_fragment)));
    }

    //[x]
    /*Dado que estou na aplicação, quando clico no texto "Sessões Ativas"
    do navegador deve-me ser apresentado o fragmento das "Sessões Ativas".*/
    public void testClickActiveSessionOnNavDrawer() {
        assertFalse("O drawer está aberto", isDrawerStatusOpen());
        assertFalse("O fragmento das Sessoes Ativas esta carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof ActiveSessionsFragment);
        testFindActiveSessionOnNavDrawer();
        solo.clickOnText(solo.getString(R.string.title_active_session_fragment));
        solo.sleep(1000);
        assertFalse(isDrawerStatusOpen());
        assertTrue("O fragmento das Sessoes Ativas não foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof ActiveSessionsFragment);
        ProgressBar pb = (ProgressBar) solo.getView(R.id.progressBar);
        while (pb.getVisibility() == View.VISIBLE) {
            solo.sleep(20);
        }
    }

    //[X]
    /*Dado que estou no ecrã das "Sessões Ativas" e existe rede, deve-me ser apresentada uma
    lista das sessões dos dados do servidor que estão a decorrer neste momento com a sua informação:
    o dia, a hora de inicio, a hora de fim, o título, a room, se existente, o track (track)
    de cada sessão e o tempo restante para a mesma acabar.*/
    public void testCheckListWithNetwork() {
        solo.setWiFiData(true);
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openFragAndVerifyIfCreatedSessionIsActive(newSession, remainingTimeExpected);
    }

    /*Dado que estou no ecrã "Sessões Ativas" e as horas e date do dispositivo passam a
    corresponder a uma Sessão que estava no instante anterior não listada então essa Sessão
    deve ser agora aparecer na listagem de sessões ativas.*/
    public void testDeactivatedToActivatedSession() {
        //Cria uma sessão que vai ficar ativa daqui a [1 - 2] minutos
        int delayedStart = 2;
        int end = 5;
        int remainingTimeExpected = end - delayedStart;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutesPlusMinutes(2),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openFragAndVerifyIfCreatedSessionIsNotActive(newSession);
        // Tempo de espera para que a sessão que não era ativa passe a ficar ativa (2000 + 118000 = 2 min)
        solo.sleep(118000);
        verifyIfCreatedSessionIsActive(newSession, remainingTimeExpected);
    }

    /*Dado que estou no ecrã "Sessões Ativas" e as horas e date do dispositivo
    passam a não corresponder a uma Sessão que estava no instante anterior na
    listagem das sessões ativas então essa Sessão deve ser removida dessa mesma listagem.*/
    public void testActivatedToDeactivatedSession() {
        //Cria uma sessão que vai ficar desativa passado 2 minutos
        int remainingTimeExpected = 2;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openFragAndVerifyIfCreatedSessionIsActive(newSession, remainingTimeExpected);
        // Tempo de espera para que a sessão que não era ativa passe a ficar ativa (2 min)
        solo.sleep(118000);
        verifyIfCreatedSessionIsNotActive(newSession);
    }

    /*Dado que estou no ecrã "Sessões Ativas" e não tenho sessões a decorrer neste momento,
     então deve-me ser exibido um texto a
     informar que não existem sessões ativas naquele momento. */
    public void testMessageIfNoActiveSessions() {
        testClickActiveSessionOnNavDrawer();
        assertTrue("Não foi encontrado o texto a indicar que não ha sessoes ativas",
                solo.waitForText(solo.getString(R.string.no_active_sessions), 1, 2000));
    }

    // [X]
    /*Dado que estou no ecrã "Sessões Ativas" e existe pelo menos uma sessão
    ativa, quando clico numa sessão listada, então é-me mostrada a actividade
    de Detalhes da Sessão com a respectiva Sessão.*/
    public void testClickOnListToSessionDetails() {
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        testClickActiveSessionOnNavDrawer();
        solo.clickOnText(newSession.getTitle());
        solo.waitForActivity(DetailsSessionActivity.class);

        assertTrue("Title não consistente", solo.searchText(newSession.getTitle()));
        assertTrue("Room não consistente", solo.searchText(newSession.getRoom()));
        assertTrue("Track não consistente", solo.searchText(newSession.getTrack()));
        assertTrue("Abrstract não consistente", solo.searchText(newSession.getAbstracts()));
        assertTrue("EndHour não consistente", solo.searchText(newSession.getEndHour()));
        assertTrue("Presenter não consistente", solo.searchText(newSession.getPresenter()));
        assertTrue("hour não consistente", solo.searchText(newSession.getStartHour()));
        solo.goBack();
    }

    //[X]
    /* Dado que estou no ecrã "Sessões Ativas" de 1 em 1 minuto o dispositivo atualiza a
     informação de Tempo Restante de cada uma das sessões listadas.*/
    public void testAutoRefresh() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        testClickActiveSessionOnNavDrawer();
        assertTrue("Não foi encontrado o ramaining time esperado", solo.searchText(String.valueOf(remainingTimeExpected)));
        // 1 minuto
        solo.sleep(60000);
        assertTrue("Não foi encontrado o remaining time esperado", solo.searchText(String.valueOf(remainingTimeExpected - 1)));
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

    private void openFragAndVerifyIfCreatedSessionIsActive(Session newSession, int remainingTime) {
        testClickActiveSessionOnNavDrawer();
        verifyIfCreatedSessionIsActive(newSession, remainingTime);
    }

    private void openFragAndVerifyIfCreatedSessionIsNotActive(Session newSession) {
        testClickActiveSessionOnNavDrawer();
        verifyIfCreatedSessionIsNotActive(newSession);
    }

    private void verifyIfCreatedSessionIsActive(Session newSession, int remainingTimeExpected) {
        assertTrue("O numero de sessões ativas não é o esperado", getActiveSessions().size() == 1);
        boolean validator = false;
        for (Session activeSession : getActiveSessions()) {
            if (newSession.getTitle().equals(activeSession.getTitle()) &&
                    newSession.getDay().equals(activeSession.getDay()) &&
                    newSession.getMonthAndYear().equals(activeSession.getMonthAndYear()) &&
                    newSession.getStartHour().equals(activeSession.getStartHour()) &&
                    newSession.getEndHour().equals(activeSession.getEndHour()) &&
                    newSession.getRoom().equals(activeSession.getRoom()) &&
                    newSession.getTrack().equals(activeSession.getTrack())) {
                validator = true;
                break;
            }
        }
        assertTrue("A sessão ativa criada não aparece na lista de sessoes ativas", validator);
        assertTrue("O tempo restante da sessao de teste não apareceu", solo.searchText(String.valueOf(remainingTimeExpected)));
    }

    private void verifyIfCreatedSessionIsNotActive(Session newSession) {
        RecyclerView rv = (RecyclerView) getActivity().findViewById(R.id.my_recycler_view);
        MyActiveSessionsRecyclerViewAdapter adapter = (MyActiveSessionsRecyclerViewAdapter) rv.getAdapter();
        LinkedList ativeSessionList = (LinkedList<Session>) adapter.getActiveSessionList();


        assertTrue("O numero de sessões ativas não é o esperado", getActiveSessions().size() == 0);
    }

    private void writeSessionsOnSessionFile(Context context, LinkedList<Session> sessions) {
        PrintWriter out = null;
        int counter = 0;
        try {
            File outFile = new File(context.getFilesDir(), FileController.SESSIONS_FILE);
            out = new PrintWriter(new FileWriter(outFile));
            String text = "Date|Start|End|Room|Track|Title|Presenter|Abstract|Rating|FirebaseNode|OnAgenda";
            out.println(text);
            for (Session session : sessions) {
                text = session.getDateFormattedString() + "|" +
                        session.getStartHour() + "|" +
                        session.getEndHour() + "|" +
                        session.getRoom() + "|" +
                        session.getTrack() + "|" +
                        session.getTitle() + "|" +
                        session.getPresenter() + "|" +
                        session.getAbstracts() + "|" +
                        session.getMyRating() + "|" +
                        session.getFirebaseSessionNode() + "|" +
                        session.isOnAgenda();
                counter++;
                if (counter < sessions.size()) {
                    out.println(text);
                } else {
                    out.print(text);
                }
            }
            out.flush();
        } catch (IOException e) {
            Log.e("Error", "Failed to write on file: ", e);
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    private void checkDrawerStatusIfOpenThenClose() {
        if (isDrawerStatusOpen()) {
            invertDrawerStatus();
        }
    }

    private LinkedList<Session> getActiveSessions() {
        RecyclerView rv = (RecyclerView) getActivity().findViewById(R.id.my_recycler_view);
        MyActiveSessionsRecyclerViewAdapter adapter = (MyActiveSessionsRecyclerViewAdapter) rv.getAdapter();
        return (LinkedList<Session>) adapter.getActiveSessionList();
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

    private String getLocalContactKey() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity().getApplicationContext());
        return pref.getString(SharedPreferenceController.MY_LOCAL_CONTACT_KEY, "");
    }

    private void setImHereSharedPrefs(boolean imhere) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getInstrumentation().getTargetContext().getString(R.string.i_am_here), imhere);
        editor.apply();
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
        sessionKeyList.add(newSessionKey.toString());
        // Tempo para a criação da sessão no servidor ser efectuada
        solo.sleep(2000);
    }

    private void createSessionOnLocalFile(Session newSession) {
        LinkedList<Session> newActiveSessionList = new LinkedList<>();
        newActiveSessionList.add(newSession);
        writeSessionsOnSessionFile(getActivity(), newActiveSessionList);
    }

    private void removeSessionsOnServer() {
        for (String sessionKey : sessionKeyList) {
            if (!sessionKey.isEmpty()) {
                Firebase storedContact = new Firebase(sessionKey);
                storedContact.removeValue();
            }
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

    private void removeAllSessions() {
        Firebase allSessions = FirebaseController.fbSessionsNode;
        allSessions.removeValue();
        solo.sleep(1000);
    }
}