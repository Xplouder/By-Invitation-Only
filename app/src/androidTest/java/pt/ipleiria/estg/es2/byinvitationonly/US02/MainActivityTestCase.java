package pt.ipleiria.estg.es2.byinvitationonly.US02;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.robotium.solo.Solo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FileController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.CustomComponents.Adapters.MyConferenceRecyclerViewAdapter;
import pt.ipleiria.estg.es2.byinvitationonly.DetailsSessionActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.ConferenceScheduleFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.HomepageFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.WhoIsHereFragment;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Conference;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MainActivityTestCase extends ActivityInstrumentationTestCase2<MainActivity> {

    protected Solo solo;
    private int result;
    private Conference conference;
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
        removeAllSessionsOnServer();
        sendSessionAssetToServer();
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
    /*Dado que estou no ecrá "programa geral de conferencia", quando pressiono a opção de "Filtrar"
    e escolho a opção "Track", deve-me ser mostrada uma caixa de dialogo para filtrar a lista
    com os trilhos existentes nas sessoes*/
    public void testFilterOptionTrack() {
        Session s1 = new Session("2014/02/12", "08:10", "09:50", "este", "track 123", "exemplo1", "ola", "resolver");
        Session s2 = new Session("2014/02/12", "08:10", "09:50", "este", "track 321", "exemplo12", "adeus", "resolver");
        createSessionOnServer(s1);
        createSessionOnServer(s2);
        openConferenceScheduleFragment();
        openFilterDialog();
        LinkedList<String> trackList = new LinkedList<>();
        trackList.add(s1.getTrack());
        trackList.add(s2.getTrack());
        for (String track : trackList) {
            assertTrue("Não foram encontrados todos os tracks disponiveis", solo.searchText(track));
        }
    }

    //[x]
    /*Dado que estou no ecrá "programa geral de conferencia", quando pressiono a opção de
    "Filtrar" e escolho a opção "All" deve-me ser mostrada a listagem de todas as sessoes*/
    public void testFilterOptionAll() {
        openConferenceScheduleFragment();
        LinkedList<Session> originalSessionList = getSessionListFromMyConferenceRecyclerViewAdapter();
        View spinner = solo.getView(Spinner.class, 0);
        solo.clickOnView(spinner);
        assertTrue("Não Achou o texto " + getFilterOptions()[0], solo.searchText(getFilterOptions()[0]));
        solo.clickOnText(getFilterOptions()[0]);
        LinkedList<Session> sessionList = getSessionListFromMyConferenceRecyclerViewAdapter();
        assertTrue("Não foram encontradas todas as sessões diponiveis", compareSessionLists(originalSessionList, sessionList));
    }

    //[x]
    /*Dado que estou na "caixa de dialogo de filtrar", e clico no botão "Filtrar" então só
    me devem ser mostradas sessões com os trilhos selecionados.*/
    public void testFilterAction() {
        Session session = createDefaultSessionTestOnServer();
        openConferenceScheduleFragment();
        openFilterDialog();
        solo.clickOnText(session.getTrack());
        assertTrue("Não encontrou o botão de filtrar", solo.searchText(solo.getString(R.string.filter)));
        solo.clickOnText(solo.getString(R.string.filter));
        solo.waitForDialogToClose();
        solo.waitForView(R.id.my_recycler_view);
        LinkedList<Session> sessionList = getSessionListFromMyConferenceRecyclerViewAdapter();
        assertTrue("Não foram encontradas todas as sessões diponiveis", verifyListHaveSelectedTrack(session.getTrack(), sessionList));
    }

    //[x]
    /* Dado que estou na "caixa de dialogo de filtrar", e clico no botão "Cancelar" então
    devem-me ser mostradas todas as sessoes.*/
    public void testFilterCancelAction() {
        openConferenceScheduleFragment();
        LinkedList<Session> originalSessionList = getSessionListFromMyConferenceRecyclerViewAdapter();
        openFilterDialog();
        assertTrue("Não encontrou o botão de cancelar", solo.searchText(solo.getString(R.string.cancel)));
        solo.clickOnText(solo.getString(R.string.cancel));
        solo.waitForDialogToClose();
        solo.waitForView(R.id.my_recycler_view);
        LinkedList<Session> sessionList = getSessionListFromMyConferenceRecyclerViewAdapter();
        assertTrue("Não foram encontradas todas as sessões diponiveis", compareSessionLists(sessionList, originalSessionList));
    }


    //[x]
    /*Dado que estou no ecrã principal e tenho o Navigation Drawer aberto,
    quando pressiono o item da lista "Homepage",
    deve ser mostrado na mesma janela o fragmento com o titulo com a abreviatura da conferencia.*/
    public void testOpenDrawerMenuItemHomenpage() {
        solo.clickOnActionBarHomeButton();
        String homepage = solo.getString(R.string.title_homepage_fragment);
        solo.clickOnText(homepage);
        solo.sleep(2000);
        assertTrue("Não apareceu para o fragmento Homepage",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof HomepageFragment);
        assertTrue("Não encontrou o titulo da homepage", solo.searchText(getConference().getAbbreviation()));
    }

    // [x]
    /*Dado que estou no ecrã "programa geral da conferência",
    quando pressiono uma sessão listada,então é-me mostrado todos os detalhes dessa sessão num novo ecrã*/
    public void testShowDetailsSession() {
        openConferenceScheduleFragment();
        Session s = getSessionListFromMyConferenceRecyclerViewAdapter().getFirst();
        solo.clickOnText(s.getTitle());
        solo.waitForActivity(DetailsSessionActivity.class);

        assertTrue("Title não consistente", solo.searchText(s.getTitle()));
        assertTrue("Room não consistente", solo.searchText(s.getRoom()));
        assertTrue("Track não consistente", solo.searchText(s.getTrack()));
        assertTrue("Presenter não consistente", solo.searchText(s.getPresenter()));
        assertTrue("Data não consistente", solo.searchText(s.getDateFormattedString()));
        assertTrue("Hora de inicio não consistente", solo.searchText(s.getStartHour()));
        assertTrue("Hora de fim não consistente", solo.searchText(s.getEndHour()));
        //assertTrue("Abstract não consistente", solo.searchText(s.getAbstracts()));
        //por alguma razão desconhecida o solo não apanha os textos que são grandes.
        TextView tv = (TextView) solo.getView(R.id.textView_details_abstract);
        assertTrue("Hora de fim não consistente", s.getAbstracts().equals(tv.getText()));
        solo.goBack();
    }

    // [x]
   /* Dado que estou na janela "visualizar detalhes da sessão",
    quando pressiono o botão back, deve-me ser apresentado o ecrã "Programa geral da conferência"*/
    public void testBackButtonInConferenceSchedule() {
        openConferenceScheduleFragment();
        Session s = getSessionListFromMyConferenceRecyclerViewAdapter().getFirst();
        solo.clickOnText(s.getTitle());
        assertTrue("Não abriu os detalhes da sessão", solo.waitForActivity(DetailsSessionActivity.class));
        solo.goBack();
        assertTrue("Não apareceu para o fragmento Conference Schedule",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof ConferenceScheduleFragment);
    }

    // [x]
    /* Dado que estou no ecrã "programa geral da conferência",
    quando introduzo texto na caixa de texto para pesquisa,
    então deve ser mostradas as sessões que tenham informação com o texto introduzido*/
    public void testSearch() {
        openConferenceScheduleFragment();
        View viewBar = getActivity().findViewById(R.id.action_search);
        solo.clickOnView(viewBar);
        solo.enterText(0, "lun");
        assertTrue("Funcionalidade de pesquisa titulo não funciona", solo.waitForText("Lunch"));
        solo.clearEditText(0);
        solo.enterText(0, "spo");
        assertTrue("Funcionalidade de pesquisa autor não funciona", solo.waitForText("Acceptance Tests: Is natural language enough?"));
        solo.clearEditText(0);
        solo.enterText(0, "if you meet");
        assertTrue("Funcionalidade de pesquisa abstrac não funciona", solo.waitForText("Acceptance Tests: Is natural language enough?"));
    }

    // [x]
   /* Dado que estou no ecrã principal e tenho o Navigation Drawer aberto,
    quando pressiono o item da lista "Conference Schedule",
    deve ser mostrado na mesma janela o fragmento com o título "Conference Schedule"*/
    public void testOpenDrawerMenuItemConferenceSchedule() {
        String scheduleTitle = openConferenceScheduleFragment();
        assertTrue("Não apareceu para o fragmento Conference Schedule",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof ConferenceScheduleFragment);
        assertTrue("Não encontrou o titulo do conference shedule", solo.searchText(scheduleTitle));
    }

    // [x]
    /*Dado que estou no ecrã principal, tenho o Navigation Drawer aberto e o i'm here está ativo,
     quando pressiono o item da lista "Who is Here", deve ser mostrado na mesma janela o
     fragmento com o título "Who is Here".*/
    public void testOpenDrawerMenuItemWhoIsHereWithImHere() {
        enableImHere();
        if (!isDrawerStatusOpen()) {
            invertDrawerStatus();
        }
        String whoIsHere = solo.getString(R.string.title_who_is_here_fragment);
        solo.clickOnText(whoIsHere);
        solo.sleep(2000);
        assertTrue("Não apareceu para o fragmento Who is Here",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof WhoIsHereFragment);
        assertTrue("Não encontrou o titulo do who is here", solo.searchText(whoIsHere));
    }

    /*Dado que estou no ecrã principal, tenho o Navigation Drawer aberto e o i'm here está desactivado,
     não deve ser apresentado o item da lista "Who is Here". */
    public void testOpenDrawerMenuItemWhoIsHereWithoutImHere() {
        if (!isDrawerStatusOpen()) {
            invertDrawerStatus();
        }
        String whoIsHere = solo.getString(R.string.title_who_is_here_fragment);
        solo.sleep(2000);
        assertTrue("Encontrou o titulo do who is here", !solo.searchText(whoIsHere));
    }

    //[x]
    /*Dado que estou no ecrã "programa geral da conferência", então devem-me ser mostrada
    a listagem com as seguintes informações das sessões do programa geral da conferência:
    titulo, date, hora de inicio, hora de fim, room, track*/
    public void testInfoSessionList() {
        openConferenceScheduleFragment();
        Session session = new Session("2015/02/13", "09:00", "10:00", "Room A", "Design",
                "Architectures and Meta-Architectures", "Jonathan Archer", "Agile do not mean no planning at all. " +
                "They will, however, loose sight of the application architecture. Do you want software to do only what the client asks, " +
                "or do you want to maintain it too?");
        View viewBar = getActivity().findViewById(R.id.action_search);
        solo.clickOnView(viewBar);
        solo.enterText(0, session.getTitle());
        assertTrue("Titulo não corresponde aos dados da lista", solo.waitForText(session.getTitle()));
        assertTrue("Data não corresponde aos dados da lista", solo.waitForText(session.getDayMonthOnString()));
        assertTrue("Hora inicio não corresponde aos dados da lista", solo.waitForText(session.getStartHour()));
        assertTrue("Hora fim não corresponde aos dados da lista", solo.waitForText(session.getEndHour()));
        assertTrue("Sala não corresponde aos dados da lista", solo.waitForText(session.getRoom()));
        assertTrue("Track não corresponde aos dados da lista", solo.waitForText(session.getTrack()));
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

    private void enableImHere() {
        confirmSubmission();
        assertIcon("O icone não está no esperado", R.id.action_i_am_here, R.drawable.ic_action_group);
    }

    private void confirmSubmission() {
        openConfirmWindow();
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

    private void setImHereSharedPrefs(boolean imhere) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getInstrumentation().getTargetContext().getString(R.string.i_am_here), imhere);
        editor.apply();
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

    private String getLocalContactKey() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity().getApplicationContext());
        return pref.getString(SharedPreferenceController.MY_LOCAL_CONTACT_KEY, "");
    }

    private boolean verifyListHaveSelectedTrack(String track, LinkedList<Session> sessionList) {
        for (Session session : sessionList) {
            if (!session.getTrack().equalsIgnoreCase(track)) {
                return false;
            }
        }
        return true;
    }

    private String openConferenceScheduleFragment() {
        if (!isDrawerStatusOpen()) {
            solo.clickOnActionBarHomeButton();
        }
        assertTrue("Não foi encontrado o título no drawer", solo.searchText(solo.getString(R.string.title_conference_schedule_fragment)));
        String scheduleTitle = solo.getString(R.string.title_conference_schedule_fragment);
        solo.clickOnText(scheduleTitle);
        solo.sleep(1000);
        assertTrue("Não apareceu para o fragmento Conference Schedule",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof ConferenceScheduleFragment);
        ProgressBar pb = (ProgressBar) solo.getView(R.id.progressBar);
        while (pb.getVisibility() == View.VISIBLE) {
            solo.sleep(20);
        }
        return scheduleTitle;
    }

    private void openFilterDialog() {
        View spinner = solo.getView(Spinner.class, 0);
        solo.clickOnView(spinner);
        assertTrue("Não Achou o texto Track", solo.searchText(getFilterOptions()[1]));
        solo.clickOnText(solo.getString(R.string.track));
        solo.waitForDialogToOpen();
    }

    private Conference getConference() {
        final Object obj = new Object();
        FirebaseController.getConferenceData(new FirebaseController.ValueFetched<Conference>() {
            @Override
            public void valuesFetched(Conference c) {
                conference = c;
                synchronized (obj) {
                    obj.notifyAll();
                }
            }
        }, getActivity());
        try {
            synchronized (obj) {
                obj.wait();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return conference;
    }

    private boolean existStringOnSessionFile(Context context, String s) {
        try {
            File file = new File(context.getFilesDir(), FileController.SESSIONS_FILE);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\\|", -1);
                for (String part : parts)
                    if (part.contains(s))
                        return true;
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean compareSessionLists(LinkedList<Session> list1, LinkedList<Session> list2) {
        // NOTA: as listas têm de vir na mesma ordem, caso venham em ordens diferentes o teste irá falhar
        if (list1.size() != list2.size()) {
            return false;
        }

        for (int i = 0; i < list1.size(); i++) {
            Session s1 = list1.get(i);
            Session s2 = list2.get(i);
            if (!(s1.getAbstracts().equals(s2.getAbstracts()) &&
                    s1.getTrack().equals(s2.getTrack()) &&
                    s1.getTitle().equals(s2.getTitle()) &&
                    s1.getStartHour().equals(s2.getStartHour()) &&
                    s1.getEndHour().equals(s2.getEndHour()) &&
                    s1.getDateFormattedString().equals(s2.getDateFormattedString()) &&
                    s1.getPresenter().equals(s2.getPresenter()) &&
                    s1.getRoom().equals(s2.getRoom()))) {
                return false;
            }
        }
        return true;
    }

    private void invertDrawerStatus() {
        solo.clickOnActionBarHomeButton();
        solo.sleep(1000);
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

    private String[] getFilterOptions() {
        Resources res = getActivity().getResources();
        return res.getStringArray(R.array.spinner_filter_array);
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

    private void disableCommunications() {
        FirebaseController.setOrdersDelayed(true);
        solo.setWiFiData(false);
    }

    private LinkedList<Session> getSessionListFromMyConferenceRecyclerViewAdapter() {
        RecyclerView rv = (RecyclerView) getActivity().findViewById(R.id.my_recycler_view);
        MyConferenceRecyclerViewAdapter crva = (MyConferenceRecyclerViewAdapter) rv.getAdapter();
        return (LinkedList<Session>) crva.getSessionList();
    }

    private Session createDefaultSessionTestOnServer() {
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
        // Tempo para a criação da sessão no servidor ser efectuada
        solo.sleep(2000);
        return newSession;
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
    }

    private void removeSessionsOnServer() {
        for (String sessionKey : sessionKeyList) {
            if (!sessionKey.isEmpty()) {
                Firebase storedContact = new Firebase(sessionKey);
                storedContact.removeValue();
            }
        }
    }

    private void sendSessionAssetToServer() {
        Firebase fb = new Firebase(FirebaseController.SERVER_URL);
        try {
            InputStream is = getActivity().getAssets().open("DadosSessoes.csv");
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);

            importSessionData(br, fb);
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        solo.sleep(2000);
    }

    private void importSessionData(BufferedReader br, Firebase fb) throws IOException {
        Firebase db = fb.child("Sessions");

        String[] campos = br.readLine().split("\\|");

        String line;
        while ((line = br.readLine()) != null) {
            Firebase item = db.push();

            String[] parts = line.split("\\|", -1);
            for (int i = 0; i < campos.length; i++) {
                item.child(campos[i]).setValue(parts[i]);
            }
        }
    }

    private void removeAllSessionsOnServer() {
        Firebase allSessions = FirebaseController.fbSessionsNode;
        allSessions.removeValue();
        solo.sleep(1000);
    }

    private void enableNetwork() {
        solo.setWiFiData(true);
        while (!NetworkController.existConnection(getActivity())) {
            solo.sleep(20);
        }
    }
}

