package pt.ipleiria.estg.es2.byinvitationonly.US05;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.firebase.client.Firebase;
import com.robotium.solo.Solo;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Database.DBAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.DetailsSessionActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ActiveSessionsFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.AgendaFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ConferenceScheduleFragment;
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
        deleteAllSessionsOnAgenda(getActivity());
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


    //[x]1
    /*Dado que estou no ecra "Calendario da Conferencia", e determinada sessão
     tem participaçao marcada, então deve-me ser visivel que essa sessão
     já tem participação marcada.*/
    public void testCheckOnAgendaOnSchedule() {
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
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        //  solo.sleep(10000);
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
    }

    //[x]2
    /*Dado que estou no ecra "Sessões Ativas", e determinada sessão tem
    participaçao marcada, então deve-me ser visivel que essa sessão já
    tem participação marcada.*/
    public void testCheckOnAgendaOnActiveSessions() {
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
        openActiveSessionsFrag();
        CheckBox cb = (CheckBox) solo.getView(R.id.checkBox);
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        //solo.sleep(10000);
        getInstrumentation().waitForIdleSync();
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
    }

    //[x] 3
    /*Dado que estou no ecra "Minha Agenda", e determinada sessão tem participaçao marcada,
    então deve-me ser visivel que essa sessão já tem participação marcada.*/
    public void testCheckOnAgenda() {
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
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        //  solo.sleep(10000);
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        openMyAgendaFrag();
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
    }

    //[x]4
    /*Dado que estou no ecra "Detalhes Sessao", e determinada
    sessão tem participaçao marcada,
    então deve-me ser visivel que essa sessão já tem participação marcada. */
    public void testCheckOnAgendaOnDetails() {
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
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        //  solo.sleep(10000);
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        solo.clickOnText(session.getTitle());
        solo.waitForActivity(DetailsSessionActivity.class);
        solo.sleep(1000);
        cb = (CheckBox) solo.getView(R.id.checkBoxDetails);
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        solo.goBack();
    }

    //[x]5
    /*Dado que estou no ecra "Calendario da Conferencia", e escolho a
    opção "Marcar participação" na listagem de sessões, essa sessão
    deve ficar marcada no ficheiro "DadosSessoes" e ser listada na
    minha agenda.  */
    public void testOnAgendaSaveOnFileSchedule() {
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
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        //  solo.sleep(10000);
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        openMyAgendaFrag();
        assertTrue("O titulo da sessão não corresponde à sessão que vem do ficheiro", solo.searchText(getSessionsOnAgenda().getFirst().getTitle()));
    }

    //[X]6
    /*Dado que estou no ecra "Sessões Ativas", e escolho a opção
    "Marcar participação" na listagem de sessões, essa sessão deve
    ficar marcada no ficheiro "DadosSessoes" e ser listada na minha agenda. */
    public void testOnAgendaSaveOnFileActiveSessions() {
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
        openActiveSessionsFrag();
        CheckBox cb = (CheckBox) solo.getView(R.id.checkBox);
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        //solo.sleep(10000);
        getInstrumentation().waitForIdleSync();
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        openMyAgendaFrag();
        assertTrue("O titulo da sessão não corresponde à sessão que vem do ficheiro", solo.searchText(getSessionsOnAgenda().getFirst().getTitle()));
    }

    //[x]7
    /*Dado que estou no ecra de "Detalhes da sessão",
    quando seleciono a opção
    "Marcar Participação" essa sessão deve ficar marcada no
    ficheiro "DadosSessoes" e ser listada na minha agenda.*/
    public void testOnAgendaSaveOnFileSessionDetails() {
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
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        //  solo.sleep(10000);
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        solo.clickOnText(session.getTitle());
        solo.waitForActivity(DetailsSessionActivity.class);
        solo.sleep(1000);
        cb = (CheckBox) solo.getView(R.id.checkBoxDetails);
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        solo.goBack();
        openMyAgendaFrag();
        assertTrue("O titulo da sessão não corresponde à sessão que vem do ficheiro", solo.searchText(getSessionsOnAgenda().getFirst().getTitle()));
    }

    //[x]8
    /*Dado que estou no ecra "Calendario da Conferencia", e escolho
    a opção "Desmarcar participação", essa sessão deve deixar de estar
    marcada no ficheiro "DadosSessoes" e deixa de ser listada na minha agenda. */
    public void testOnAgendaDeleteFromFileOnSchedule() {
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
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        //  solo.sleep(10000);
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        cb = (CheckBox) solo.getView(R.id.checkBox);
        solo.clickOnView(cb);
        //solo.sleep(10000);
        openMyAgendaFrag();
        getInstrumentation().waitForIdleSync();
        assertTrue("O titulo da sessão não corresponde à sessão que vem do ficheiro", getSessionsOnAgenda().isEmpty());
        assertFalse("Encontrou a sessao", solo.searchText(session.getTitle()));
    }

    //[x]9
    /*Dado que estou no ecra "Sessões Ativas", e escolho a opção "Desmarcar participação",
    essa sessão deve deixar de estar marcada no ficheiro
    "DadosSessoes" e deixa de ser listada na minha agenda.*/
    public void testOnAgendaDeleteFromFileOnActiveSessions() {
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
        openActiveSessionsFrag();
        CheckBox cb = (CheckBox) solo.getView(R.id.checkBox);
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        //solo.sleep(10000);
        getInstrumentation().waitForIdleSync();
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        cb = (CheckBox) solo.getView(R.id.checkBox);
        solo.clickOnView(cb);
        //solo.sleep(10000);
        getInstrumentation().waitForIdleSync();
        openMyAgendaFrag();
        assertTrue("A sessao está no ficheiro", getSessionsOnAgenda().isEmpty());
        assertFalse("Encontrou a sessao", solo.searchText(session.getTitle()));
    }

    //[x]10
    /*Dado que estou no ecra "Minha Agenda", e escolho a opção "Desmarcar
    participação", essa sessão deve deixar de estar marcada no ficheiro
    "DadosSessoes" e deixa de ser listada na minha agenda.*/
    public void testOnAgendaDeleteFromFileOnMyAgenda() {
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
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        //  solo.sleep(10000);
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        openMyAgendaFrag();
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        cb = (CheckBox) solo.getView(R.id.checkBox);
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        //solo.sleep(10000);
        openMyAgendaFrag();
        assertTrue("O titulo da sessão não corresponde à sessão que vem do ficheiro", getSessionsOnAgenda().isEmpty());
        assertFalse("Encontrou a sessao", solo.searchText(session.getTitle()));
    }

    //[x]11
    /*Dado que estou no ecra de "Detalhes da sessão", quando seleciono a
    opção "Desmarcar Participação" essa sessão deve deixar de
    estar marcada no ficheiro "DadosSessoes" e deixa de ser listada
    na minha agenda.*/
    public void testOnAgendaDeleteFromFileOnSessionsDetails() {
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
        assertTrue("Opçao para marcar a minha participação não aparece", cb.getVisibility() == View.VISIBLE);
        assertTrue("A sessao esta na lista", getSessionsOnAgenda().isEmpty());
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        //  solo.sleep(10000);
        assertTrue("A sessao esta nao esta na lista", getSessionsOnAgenda().getFirst().getTitle().equals(session.getTitle()));
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        solo.clickOnText(session.getTitle());
        solo.waitForActivity(DetailsSessionActivity.class);
        solo.sleep(1000);
        cb = (CheckBox) solo.getView(R.id.checkBoxDetails);
        assertTrue("Opçao para marcar a minha participação não funciona", cb.isChecked());
        solo.goBack();
        solo.clickOnText(session.getTitle());
        solo.waitForActivity(DetailsSessionActivity.class);
        solo.sleep(1000);
        cb = (CheckBox) solo.getView(R.id.checkBoxDetails);
        solo.clickOnView(cb);
        solo.sleep(1000);
        solo.goBack();
        solo.sleep(10000);
        openMyAgendaFrag();
        getInstrumentation().waitForIdleSync();
        assertTrue("O titulo da sessão não corresponde à sessão que vem do ficheiro", getSessionsOnAgenda().isEmpty());
        assertFalse("Encontrou a sessao", solo.searchText(session.getTitle()));
    }

    //[x]12
    /*Dado que estou na aplicação, quando clico no texto "Minha Agenda"
    do navegador deve-me ser apresentado o ecra "Minha Agenda" com as
    Sessoes que estão marcadas com participação.*/
    public void testMyAgendaVisible() {
        testCheckOnAgenda();
        Session session = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão que não deve aparecer",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(session);
        for (Session s : getSessionsOnAgenda()) {
            assertTrue("Nao está visivel uma sessão que está a true no ficheiro", solo.searchText(s.getTitle()));
        }
        assertFalse("Está visivel a sessão que está a false no ficheiro", solo.searchText(session.getTitle()));
    }

    //[x]13
    /*Dado que estou no ecrá "calendario de conferencia",
    quando pressiono a opção de "Filtrar" e escolho a opção
    "Minhas Sessões", deve-me ser mostrada as Sessões marcadas
    no ficheiro "DadosSessoes".*/
    public void testFilterOnAgendaOnSchedule() {
        Session session1 = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito 1",
                "Rato Mickey",
                "Nada a resumir.");
        Session session2 = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(10),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito 2",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(session1);
        createSessionOnServer(session2);
        openScheduleFrag();
        CheckBox cb = (CheckBox) solo.getView(R.id.checkBox);
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        openFilterDialog();
        solo.sleep(1000);
        assertTrue("A sessão com onAgenda ativo não foi filtrada corretamente", solo.searchText(session1.getTitle()));
        assertFalse("A sessão com onAgenda desligado apareceu", solo.searchText(session2.getTitle()));
    }

    //[x]14
    /*Dado que estou no ecrá "programa geral de conferencia", pressiono
    a opção de "Filtrar" e escolho a opção "Minhas Sessões" e foram
    carregadas as sessoes que estam marcadas no ficheiro "DadosSessoes",
    e escolho a opção "Desmarcar participação" na listagem de sessoes,
    essa sessão deve deixar de estar marcada no ficheiro "DadosSessoes"
    e deixa de estar listada na listagem atual.*/
    public void testFilterOnAgendaDisableOnSchedule() {
        testFilterOnAgendaOnSchedule();
        CheckBox cb = (CheckBox) solo.getView(R.id.checkBox);
        solo.clickOnView(cb);
        getInstrumentation().waitForIdleSync();
        assertTrue("A sessão com onAgenda desativo não foi filtrada retirada da lista", getSessionsOnAgenda().isEmpty());
        assertFalse("A sessão desapareceu do ficheiro mas continuou na listagem", solo.searchText("Sessão sem jeito 1"));
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

    private void deleteAllSessionsOnAgenda(Context context) {
        DBAdapter dbAdapter = new DBAdapter(context);
        dbAdapter.removeAllSessions();
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
        sessionKeyList.add(newSessionKey.toString());
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

    private void openActiveSessionsFrag() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText(solo.getString(R.string.title_active_session_fragment));
        solo.sleep(1000);
        assertTrue("O fragmento das sessoes ativas nao foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof ActiveSessionsFragment);
    }

    private void openMyAgendaFrag() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText(solo.getString(R.string.title_agenda_fragment));
        solo.sleep(500);
        assertTrue("O fragmento da Minha Agenda nao foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof AgendaFragment);
    }

    private void removeAllSessions() {
        Firebase allSessions = FirebaseController.fbSessionsNode;
        allSessions.removeValue();
        solo.sleep(1000);
    }

    private LinkedList<Session> getSessionsOnAgenda() {
        DBAdapter dbAdapter = new DBAdapter(getActivity());
        LinkedList<Session> sessionsOnAgenda = new LinkedList<>();
        for (Session s : dbAdapter.getSessions()) {
            if (s.isOnAgenda()) {
                sessionsOnAgenda.add(s);
            }
        }
        return sessionsOnAgenda;
    }

    private void openFilterDialog() {
        View spinner = solo.getView(Spinner.class, 0);
        solo.clickOnView(spinner);
        assertTrue("Não Achou o texto On Agenda", solo.searchText(getFilterOptions()[2]));
        solo.clickOnText(getFilterOptions()[2]);
        solo.waitForDialogToOpen();
    }

    private String[] getFilterOptions() {
        Resources res = getActivity().getResources();
        return res.getStringArray(R.array.spinner_filter_array);
    }
}