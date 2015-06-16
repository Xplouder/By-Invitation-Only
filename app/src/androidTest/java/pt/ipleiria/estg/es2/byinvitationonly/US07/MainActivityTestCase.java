package pt.ipleiria.estg.es2.byinvitationonly.US07;

import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.robotium.solo.Solo;

import java.util.HashMap;

import pt.ipleiria.estg.es2.byinvitationonly.ContactSettingsActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.WhoIsHereFragment;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.R;


public class MainActivityTestCase extends ActivityInstrumentationTestCase2<MainActivity> {

    protected Solo solo;
    private int result;

    public MainActivityTestCase() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setImHereSharedPrefs(false);
        solo = new Solo(getInstrumentation(), getActivity());
        enableCommunications();
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

    // [x]
    /*Dado que estou na aplicação, quando seleciono a opção "Settings",
    devo ser reencaminhado para o ecrã de preenchimento de detalhes do meu contato.*/
    public void testClickSettings() {
        solo.clickOnActionBarItem(R.id.action_settings);
        solo.assertCurrentActivity("Não mudou para a ContactSettingsActitivy", ContactSettingsActivity.class);
    }

    // [x]
    /*Dado que estou no ecrã de preenchimento de detalhes do meu contacto
    e a funcionalidade "I'm Here" está desactivada, quando termino
    o preenchimento dos dados e pressiono o botão de back
    devo ser reencaminhado para a atividade anterior.*/
    public void testBackSettingsImHereOff() {
        setUserContact(new Contact());
        testClickSettings();
        fillContactSettings();
        solo.goBack();
        verifyDataOnShared("Nome exemplo", "email@email.com");
        solo.assertCurrentActivity("Não foi para a activity anterior", MainActivity.class);
    }

    // [x]
    /*Dado que estou no ecrã de preenchimento de detalhes do meu contacto e
    a funcionalidade "I'm Here" está ativa, quando termino o
    preenchimento dos dados, pressiono o botão de back e não tenho uma ou
    mais informações do meu contacto preenchidas deve ser apresentada
    uma caixa de diálogo com a opção de voltar a preencher os contactos ou
    desactivar a funcionalidade "I'm Here".*/
    public void testBackSettingsImHereOnDontFilled() {
        openDialogDisableSharing();
        assertTrue("A caixa de dialogo que apareceu não é a correta", solo.searchText(solo.getString(R.string.ad_disableSharing)));
        assertTrue("A caixa de dialogo que apareceu não tem a opção voltar", solo.searchText(solo.getString(R.string.back)));
        assertTrue("A caixa de dialogo que apareceu não tem a opção desativar a funcionalidade Im Here",
                solo.searchText((solo.getString(R.string.disableSharing))));
    }


    // [x]
    /*Dado que estou no ecrã de preenchimento de detalhes do meu contacto e
    a funcionalidade "I'm Here" está ativa, quando termino o preenchimento dos dados,
    pressiono o botão de back e tenho as informações do meu contacto preenchidas (pelo menos nome e email),
    então os dados devem ser atualizados os dados que já estão no servidor.*/
    public void testBackSettingsImHereOnFilled() {
        openSettingsImHereOn();
        changeContactSettings();
        solo.goBack();
        int res = invokeContactDataRequestOnServer();
        assertTrue("Não existe no servidor o contacto teste criado", res == 1);
    }

    //[x]
    /*Dado que estou no ecrã de preenchimento de detalhes do meu contacto,
    a funcionalidade "I'm Here" está ativa e tenho a caixa de dialogo aberta,
    quando escolho a opção de "Voltar", a caixa de dialogo deve ser fechada.*/
    public void testBackOnDialog() {
        openDialogDisableSharing();
        solo.clickOnButton(solo.getString(R.string.back));
        solo.waitForDialogToClose();
        solo.assertCurrentActivity("Não ficou na ContactSettingsActivity", ContactSettingsActivity.class);
    }

    //[x]
    /*Dado que estou no ecrã de preenchimento de detalhes do meu contacto,
    a funcionalidade "I'm Here" está ativa e tenho a caixa de dialogo aberta,
    quando escolho a opção de "Desativar I'm Here", devo ser reencaminhado para
    a MainActivity e a funcionalidade "I'm Here" deve ser desativada.*/
    public void testDisableOnDialog() {
        openDialogDisableSharing();
        solo.clickOnButton(solo.getString(R.string.disableSharing));
        solo.waitForDialogToClose();
        assertIcon("O icone im here nao ficou em alone", R.id.action_i_am_here, R.drawable.ic_action_alone);
        solo.assertCurrentActivity("Não foi reencaminhado para a MainActivity", MainActivity.class);
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

    private void fillContactSettings() {
        solo.clickOnText(solo.getString(R.string.name));
        solo.waitForDialogToOpen();
        solo.enterText(0, "Nome exemplo");
        solo.clickOnButton(1);
        solo.waitForDialogToClose();

        solo.clickOnText(solo.getString(R.string.email));
        solo.waitForDialogToOpen();
        solo.enterText(0, "email@email.com");
        solo.clickOnButton(1);
        solo.waitForDialogToClose();
    }

    private void changeContactSettings() {
        clearEditText(solo.getString(R.string.name));
        solo.clickOnText(solo.getString(R.string.name));
        solo.waitForDialogToOpen();
        solo.enterText(0, "Nome exemplo2");
        solo.clickOnButton(1);
        solo.waitForDialogToClose();

        clearEditText(solo.getString(R.string.email));
        solo.clickOnText(solo.getString(R.string.email));
        solo.waitForDialogToOpen();
        solo.enterText(0, "email2@email.com");
        solo.clickOnButton(1);
        solo.waitForDialogToClose();
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

    private void openDialogForEditText(String button) {
        solo.clickOnText(button);
        solo.waitForDialogToOpen();
    }

    private void closeDialogForEditTextOk() {
        solo.clickOnButton(1);
        solo.waitForDialogToClose();
    }

    private void clearEditText(String button) {
        openDialogForEditText(button);
        solo.clearEditText(0);
        closeDialogForEditTextOk();
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

    private void openSettingsImHereOn() {
        confirmSubmission();
        testClickSettings();
    }

    private void openDialogDisableSharing() {
        openSettingsImHereOn();
        clearEditText(solo.getString(R.string.name));
        clearEditText(solo.getString(R.string.email));
        solo.goBack();
        solo.waitForDialogToOpen();
    }

    private void verifyDataOnShared(String nome, String email) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity().getApplicationContext());
        assertTrue("As alterações que o utilizador fez não foram efetuadas",
                pref.getString(solo.getString(R.string.pref_key_name), "").equals(nome) &&
                        pref.getString(solo.getString(R.string.pref_key_email), "").equals(email));
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

