package pt.ipleiria.estg.es2.byinvitationonly.US03;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.RatingBar;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.robotium.solo.Solo;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FileController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.DetailsSessionActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ActiveSessionsFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.AgendaFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ConferenceScheduleFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.HomepageFragment;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MainActivityTestCase extends ActivityInstrumentationTestCase2<MainActivity> {

    protected Solo solo;
    private float rating = 0;
    private LinkedList<String> sessionKeyList = new LinkedList<>();
    private Session session;

    public MainActivityTestCase() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setImHereSharedPrefs(false);
        solo = new Solo(getInstrumentation(), getActivity());
        deleteFileSessions(getActivity());
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
    /*Dado que estou no ecra "Calendario da Conferencia", e determinada sessão já começou,
     então deve-me ser mostrada uma opção para dar feedback dessa mesma sessão. */
    public void testFeedbackCalendario() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 2",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarSessions);
        assertTrue("A opção para dar feedback não apareceu", rb.getVisibility() == View.VISIBLE);
    }

    //[x]2
    //Dado que estou no ecra "Calendario da Conferencia",
    // e determinada sessão já começou e seleciono um feedback para a Sessão,
    // então deve ser atribuido o feedback que selecionei a essa mesma sessão e guardada no servidor.
    public void testFeedbackOnConfSchedule() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 2",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        solo.clickOnActionBarHomeButton();
        solo.sleep(2000);
        solo.clickOnText(solo.getString(R.string.title_conference_schedule_fragment));
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarSessions);
        solo.clickOnView(rb);
        assertTrue("Rating não foi atribuido corretamente", Math.abs(getSessionRatingFromFirebase() - (float) 3) < 0.001);
    }

    //[x]3
    //Dado que estou no ecra "Calendario da Conferencia" e não tenho rede e determinada sessão já começou,
    //e seleciono um feedback para a Sessão, então deve-me ser apresentada uma mensagem informativa
    //a indicar a falta de rede.
    public void testAlertDialogNoConnOnConfSchedule() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 3",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        solo.setWiFiData(false);
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarSessions);
        solo.clickOnView(rb);
        assertTrue("A mensagem de falta de rede não apareceu", solo.waitForText(solo.getString(R.string.error_connectivity)));
    }


    //[x]4
    /*Dado que estou no ecra "Minha Agenda", e determinada sessão já começou,
     então deve-me ser mostrada uma opção para dar feedback dessa mesma sessão. */
    public void testFeedbackAgenda() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 4",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        solo.clickOnText(solo.getString(R.string.checkbox_session_to_agenda));
        solo.sleep(7000);
        openMyAgendaFrag();
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarSessions);
        assertTrue("A opção para dar feedback não apareceu", rb.getVisibility() == View.VISIBLE);
    }

    //[x]5
    //Dado que estou no ecra "Minha Agenda", e determinada sessão já começou,
    // e seleciono um feedback para a Sessão,
    // então deve ser atribuido o feedback que selecionei a essa mesma sessão e guardada no servidor.
    public void testFeedbackOnMyAgenda() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 5",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        solo.clickOnText(solo.getString(R.string.checkbox_session_to_agenda));
        solo.sleep(7000);
        openMyAgendaFrag();

        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarSessions);
        solo.clickOnView(rb);
        assertTrue("Rating não foi atribuido corretamente", Math.abs(getSessionRatingFromFirebase() - (float) 3) < 0.001);
    }

    //[x]6
    //Dado que estou no ecra "Minha Agenda" e não tenho rede e determinada sessão já começou,
    //e seleciono um feedback para a Sessão, então deve-me ser
    // apresentada uma mensagem informativa a indicar a falta de rede.
    public void testAlertDialogNoConnOnMyAgenda() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 6",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        solo.clickOnText(solo.getString(R.string.checkbox_session_to_agenda));
        solo.sleep(7000);
        openMyAgendaFrag();
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarSessions);
        solo.setWiFiData(false);
        solo.clickOnView(rb);
        assertTrue("A mensagem de falta de rede não apareceu", solo.waitForText(solo.getString(R.string.error_connectivity)));
    }

    //[x]7
    /*Dado que estou no ecra "Sessões Ativas", então deve-me
    ser mostrada uma opção para dar feedback dessa mesma sessão.*/
    public void testFeedbackActiveSessions() {
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
        openActiveSessionsFrag();
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarSessions);
        assertTrue("A opção para dar feedback não apareceu", rb.getVisibility() == View.VISIBLE);
    }

    //[x]8
    //Dado que estou no ecra "Sessoes Ativas" e seleciono um feedback para a Sessão,
    // então deve ser atribuido o feedback que selecionei a essa mesma sessão e guardada no servidor.
    public void testFeedbackOnActiveSessions() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 8",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        solo.sleep(2000);
        solo.clickOnActionBarHomeButton();
        solo.clickOnText(solo.getString(R.string.title_active_session_fragment));
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarSessions);
        solo.clickOnView(rb);
        assertTrue("Rating não foi atribuido corretamente", Math.abs(getSessionRatingFromFirebase() - (float) 3) < 0.001);
    }

    //[x]9
    //Dado que estou no ecra "Sessoes Ativas" e não tenho rede e determinada sessão já começou,
    //e seleciono um feedback para a Sessão, então deve-me ser apresentada uma mensagem informativa a
    //indicar a falta de rede.
    public void testAlertDialogNoConnOnActiveSessions() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para testa 9",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openActiveSessionsFrag();
        solo.setWiFiData(false);
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarSessions);
        solo.clickOnView(rb);
        assertTrue("A mensagem de falta de rede não apareceu", solo.waitForText(solo.getString(R.string.error_connectivity)));
    }


    //[x]10
    /*Dado que estou no ecra de "Detalhes de Sessão", e a respectiva
    sessão já começou, então devo poder dar feedback dessa mesma sessão. */
    public void testFeedbackDetails() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 10",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        solo.clickOnText(newSession.getTitle());
        solo.waitForActivity(DetailsSessionActivity.class);
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarDetails);
        assertTrue("A opção para dar feedback não apareceu", rb.getVisibility() == View.VISIBLE);
        solo.goBack(); //sem o goBack está a dar erro de instrumentaçao, no debug mostrava que era por casa do resultado
    }

    //[X]11
    //Dado que estou no ecra de "Detalhes de Sessão", e a respectiva sessão já começou,
    // e seleciono um feedback para a Sessão,
    // então deve ser atribuido o feedback que selecionei a essa mesma sessão e guardado no servidor.
    public void testFeedbackSessionDetails() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 11",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        solo.clickOnText(newSession.getTitle());
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarDetails);
        solo.clickOnView(rb);
        solo.sleep(4000);
        assertTrue("Feedback da sessão não corresponde", Math.abs(getSessionRatingFromFirebase() - (float) 3) < 0.001);
        solo.goBack();
        assertTrue("Feedback da sessão não corresponde aos dados do servidor", Math.abs(getSessionRatingFromFirebase() - (float) 3) < 0.001);
    }

    //[x] 12
    //Dado que estou no ecra de "Detalhes de Sessão" e não tenho rede,
    // e a respectiva sessão já começou, e seleciono um feedback para a Sessão,
    // então deve-me ser apresentada uma mensagem informativa a indicar a falta de rede.
    public void testAlertDialogNoConnOnSessionDetails() {
        int remainingTimeExpected = 5;
        Session newSession = new Session(
                getTodayDate(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(remainingTimeExpected),
                "Sala Escondida",
                "Trilhinho",
                "Sessão para teste 12",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        solo.setWiFiData(false);
        openScheduleFrag();
        solo.clickOnText(newSession.getTitle());
        assertTrue("Não abriu os detalhes da sessão", solo.waitForActivity(DetailsSessionActivity.class));
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarDetails);
        solo.clickOnView(rb);
        assertTrue("A mensagem de falta de rede não apareceu", solo.waitForText(solo.getString(R.string.error_connectivity)));
        solo.clickOnText(solo.getString(R.string.ok));
        solo.goBack();
    }

    //[x]13
    /*Dado que estou no ecra principal, deve-me ser
    apresentada uma opção para poder dar feedback do evento.*/
    public void testFeedbackEvent() {
        assertTrue("O fragmento homepage nao foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof HomepageFragment);
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarHomepage);
        assertTrue("A opção para dar feedback não apareceu", rb.getVisibility() == View.VISIBLE);
    }

    //[x]14
    /*Dado que estou no ecra principal, e seleciono um feedback
     para o evento, então o valor do feedback deve ser guardado no servidor.*/
    public void _testEventRatingSubmit() {
        removeAllEventRating();
        testFeedbackEvent();
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarHomepage);
        solo.clickOnView(rb);
        solo.sleep(2000);
        assertTrue("O rating dado não é o mesmo que está no servidor", Math.abs(getEventRatingFromFirebase() - (float) 3) < 0.001);
        removeAllEventRating();
    }


    //[x] 15
    //Dado que estou no ecra principal e não tenho rede,
    // e seleciono um feedback para o evento, então deve-me ser apresentada
    //uma mensagem informativa a indicar a falta de rede.
    public void testAlertDialogNoConnOnHomepage() {
        removeAllEventRating();
        solo.setWiFiData(false);
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarHomepage);
        solo.clickOnView(rb);
        assertTrue("A mensagem de falta de rede não apareceu", solo.waitForText(solo.getString(R.string.error_connectivity)));
    }

    //[x]16
    /*Dado que estou no ecra principal, e já foi classifiquei
     o evento, então o feedback dado deve estar visivel.*/
    public void testSeeEventRating() {
        removeAllEventRating();
        testFeedbackEvent();
        RatingBar rb = (RatingBar) solo.getView(R.id.ratingBarHomepage);
        solo.clickOnView(rb);
        solo.sleep(2000);
        RatingBar ratingBar = (RatingBar) solo.getView(R.id.ratingBarHomepage);
        assertTrue("O feedback dado não está visivel", Math.abs(getEventRatingFromFirebase() - ratingBar.getRating()) < 0.001);
        removeAllEventRating();
    }

    //[x] 17
    // Dado que estou no ecra "Calendario da Conferencia", e determinada sessão já foi classificada,
    // então o feedback dado a essa sessão deve estar visivel.
    public void testSeeSessionRatingOnConfSchedule() {
        testFeedbackOnConfSchedule();
        solo.sleep(2000);
        RatingBar ratingBar = (RatingBar) solo.getView(R.id.ratingBarSessions);
        assertTrue("O feedback dado não está visivel", Math.abs(getSessionRatingFromFirebase() - ratingBar.getRating()) < 0.001);
    }

    //[]18
    //Dado que estou no ecra "Minha Agenda", e determinada sessão já foi classificada,
    //então o feedback dado a essa sessão deve estar visivel.
    public void testSeeSessionRatingOnMyAgenda() {
        testFeedbackOnMyAgenda();
        solo.sleep(2000);
        RatingBar ratingBar = (RatingBar) solo.getView(R.id.ratingBarSessions);
        assertTrue("O feedback dado não está visivel", Math.abs(getSessionRatingFromFirebase() - ratingBar.getRating()) < 0.001);
    }

    //[x]19
    //Dado que estou no ecra de "Detalhes de Sessão", e determinada sessão já foi classificada,
    //então o feedback dado a essa sessão deve estar visivel.
    public void testSeeSessionRatingDetails() {
        testFeedbackSessionDetails(); //teste da goBack, logo e necessário fazer o clickOnText novamente
        solo.clickOnText(session.getTitle());
        solo.sleep(2000);
        RatingBar ratingBar = (RatingBar) solo.getView(R.id.ratingBarDetails);
        assertTrue("O feedback dado não está visivel", Math.abs(getSessionRatingFromFirebase() - ratingBar.getRating()) < 0.001);
        solo.goBack();
    }

    //[x] 20
    /* Dado que estou no ecra "Calendario da Conferencia" e determinada sessão ainda não começou
    então a opção para dar feedback não deve estar visivel.*/
    public void testRatingNotVisibleOnCalendar() {
        Session newSession = new Session(
                getTodayDatePlusOneYear(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        RatingBar ratingBar = (RatingBar) solo.getView(R.id.ratingBarSessions);
        assertTrue("O rating bar não está invisivel, está visivel", ratingBar.getVisibility() == View.INVISIBLE);
    }

    // [x] 21
    /* Dado que estou no ecra "Detalhes de Sessao" e a sessão ainda não começou então a
     opção para dar feedback não deve estar visivel.*/
    public void testRatingNotVisibleOnSessionDetails() {
        Session newSession = new Session(
                getTodayDatePlusOneYear(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        solo.clickOnText(newSession.getTitle());
        solo.sleep(300);
        RatingBar ratingBar = (RatingBar) solo.getView(R.id.ratingBarDetails);
        assertTrue("O rating bar não está invisivel, está visivel", ratingBar.getVisibility() == View.INVISIBLE);
        solo.goBack();
    }

    // [x] 22
    /* Dado que estou no ecra "Minha Agenda" e determinada sessão ainda não começou
     então a opção para dar feedback não deve estar visivel. */
    public void testRatingNotVisibleOnMyAgenda() {
        Session newSession = new Session(
                getTodayDatePlusOneYear(),
                getTodayHourAndMinutes(),
                getTodayHourAndMinutesPlusMinutes(5),
                "Sala Escondida",
                "Trilhinho",
                "Sessão sem jeito",
                "Rato Mickey",
                "Nada a resumir.");
        createSessionOnServer(newSession);
        openScheduleFrag();
        solo.clickOnText(solo.getString(R.string.checkbox_session_to_agenda));
        solo.sleep(7000);
        openMyAgendaFrag();
        RatingBar ratingBar = (RatingBar) solo.getView(R.id.ratingBarSessions);
        assertTrue("O rating bar não está invisivel, está visivel", ratingBar.getVisibility() == View.INVISIBLE);
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

    private float getEventRatingFromFirebase() {
        Firebase f = FirebaseController.fbConferenceNode.child("Ratings");

        final Object obj = new Object();

        f.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    rating = Float.valueOf(child.getValue().toString());
                    synchronized (obj) {
                        obj.notifyAll();
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
        return rating;
    }

    private float getSessionRatingFromFirebase() {
        final Object obj = new Object();

        FirebaseController.getConferenceSessionsOnce(new FirebaseController.ValueFetched<LinkedList<Session>>() {
            @Override
            public void valuesFetched(LinkedList<Session> sessions) {
                session = sessions.getFirst();
                synchronized (obj) {
                    obj.notifyAll();
                }
            }
        }, solo.getCurrentActivity());
        try {
            synchronized (obj) {
                obj.wait();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return session.getMyRating();
    }

    private void removeAllEventRating() {
        Firebase eventRatings = FirebaseController.fbConferenceNode.child("Ratings");
        eventRatings.removeValue();
        solo.sleep(1000);
    }

    private void openActiveSessionsFrag() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText(solo.getString(R.string.title_active_session_fragment));
        solo.sleep(500);
        assertTrue("O fragmento das sessoes ativas nao foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof ActiveSessionsFragment);
    }

    private void openScheduleFrag() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText(solo.getString(R.string.title_conference_schedule_fragment));
        solo.sleep(500);
        assertTrue("O fragmento do calendario da conferencia nao foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof ConferenceScheduleFragment);
    }

    private void openMyAgendaFrag() {
        solo.clickOnActionBarHomeButton();
        solo.clickOnText(solo.getString(R.string.title_agenda_fragment));
        solo.sleep(500);
        assertTrue("O fragmento da Minha Agenda nao foi carregado",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof AgendaFragment);
    }

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

    private String getTodayDate() {
        GregorianCalendar calc = new GregorianCalendar();
        int year = calc.get(Calendar.YEAR);
        int month = calc.get(Calendar.MONTH) + 1;
        int day = calc.get(Calendar.DAY_OF_MONTH);
        return year + "/" + month + "/" + day;
    }

    private String getTodayDatePlusOneYear() {
        GregorianCalendar calc = new GregorianCalendar();
        calc.add(Calendar.YEAR, 1);
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

    private void removeSessionsOnServer() {
        for (String sessionKey : sessionKeyList) {
            if (!sessionKey.isEmpty()) {
                Firebase storedContact = new Firebase(sessionKey);
                storedContact.removeValue();
            }
        }
        solo.sleep(2000);
    }
}

