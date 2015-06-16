package pt.ipleiria.estg.es2.byinvitationonly.US06;

import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.PatternMatcher;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.widget.ListView;
import android.widget.TextView;

import com.firebase.client.Firebase;

import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MainActivityTestCase1 extends ActivityInstrumentationTestCase2<MainActivity> {

    private LinkedList<String> contactKeyList = new LinkedList<>();

    public MainActivityTestCase1() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setImHereSharedPrefs(true);
        checkDrawerStatusIfOpenThenClose();

    }

    @Override
    protected void tearDown() throws Exception {
        final String localContactKey = getLocalContactKey();
        if (!localContactKey.isEmpty()) {
            Firebase storedContact = FirebaseController.fbContactsNode.child(localContactKey);
            storedContact.removeValue();
        }
        removeContactsOnServer();
        setImHereSharedPrefs(false);
        super.tearDown();
    }


    //[x]4
    /*Dados que estou no ecrã "Who is Here", a funcionalidade "I'm Here" está ativa e
    existem participantes disponíveis no evento para fazer networking e
    comunicar então quando seleciono um participante deve-me aparecer um ecrã para enviar
    um email ao participante selecionado.*/
    public void testClickOnPerson() {
        try {
            // register activity monitor for the send mail activity
            Instrumentation instrumentation = getInstrumentation();
            IntentFilter filter = new IntentFilter(Intent.ACTION_SENDTO);
            filter.addDataScheme("mailto");
            filter.addDataPath("*", PatternMatcher.PATTERN_SIMPLE_GLOB);
            Instrumentation.ActivityMonitor monitor = instrumentation.addMonitor(filter, null, true);

            Contact newContact = new Contact("Testemail", "Teste@mail.com");
            createContactTest(newContact);

            if (!isDrawerStatusOpen()) {
                invertDrawerStatus();
            }
            Thread.sleep(100);

            Fragment f = getActivity().getFragmentManager().findFragmentById(R.id.navigation_drawer);

            ListView lv = (ListView) f.getView();
            assertNotNull("Não foi encontrada a ListView", lv);
            TouchUtils.clickView(this, lv.getChildAt(2));

            assertEquals(0, monitor.getHits());

            TextView tv = (TextView) getActivity().findViewById(R.id.textViewName);
            assertNotNull("Não foi encontrada a TextView com o nome", tv);
            TouchUtils.clickView(this, tv);

            Thread.sleep(100);
            int hits = monitor.getHits();

            assertTrue("Não foi encontrada a atividade para escolher uma aplicação de envio de email", hits > 0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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


    private void setImHereSharedPrefs(boolean imhere) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getInstrumentation().getTargetContext().getString(R.string.i_am_here), imhere);
        editor.apply();
    }

    private String getLocalContactKey() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        return pref.getString(SharedPreferenceController.MY_LOCAL_CONTACT_KEY, "");
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

    private void invertDrawerStatus() {

        TouchUtils.drag(this, 0, getActivity().getWindow().getDecorView().getWidth() / 2, getActivity().getWindow().getDecorView().getHeight() / 2, getActivity().getWindow().getDecorView().getHeight() / 2, 5);
//        getActivity().getWindow().getCallback().onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, homeMenuItem);
        //View v = getActivity().getWindow().findViewById(android.R.id.home);
        //TouchUtils.clickView(this, v);
        //v.performClick();
        //Solo solo;
        //solo.clickOnActionBarHomeButton();/*
        //solo.sleep(1000);*/
    }

    private void createContactTest(Contact newContact) {
        createNewContactOnServer(newContact);
        //solo.sleep(2000);
    }

    private String createNewContactOnServer(Contact contact) {
        Firebase contactsNode = FirebaseController.fbContactsNode;
        Firebase newcontact = contactsNode.push();
        newcontact.child(FirebaseController.CONTACT_ATTRIBUTES[0]).setValue(contact.getName());
        newcontact.child(FirebaseController.CONTACT_ATTRIBUTES[1]).setValue(contact.getEmail());
        contactKeyList.add(newcontact.toString());
        return newcontact.toString();
    }

    private void removeContactsOnServer() {
        for (String contactKey : contactKeyList) {
            if (!contactKey.isEmpty()) {
                Firebase storedContact = new Firebase(contactKey);
                storedContact.removeValue();
            }
        }
    }
}

