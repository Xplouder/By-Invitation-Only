package pt.ipleiria.estg.es2.byinvitationonly.US04;

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
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MainActivityTestCase extends ActivityInstrumentationTestCase2<MainActivity> {

    private Solo solo;
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
    /*Dado que estou na aplicação, Quando pressiono o botão "I'm here" e as
    informações do meu contacto não estão preenchidas (pelo menos nome e
    email), Então deve ser-me exibida uma mensagem perguntando se pretendo
    preencher as informações do meu contacto ou cancelar a acção.*/
    public void testNotFilledContact() {
        setUserContact(new Contact());
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        assertTrue("Não apareceu caixa de dialogo", solo.waitForDialogToOpen());
        assertTrue("Caixa de dialogo errada", solo.waitForText(solo.getString(R.string.ad_incomplete_data)));
        assertTrue("Não tem botão para aceitar", solo.searchText(solo.getString(R.string.yes)));
        assertTrue("Não tem botão para cancelar", solo.searchText(solo.getString(R.string.cancel)));
    }

    // [X]
    /*Dado que estou na aplicação, Quando pressiono o botão "I'm here" e as
    informações do meu contacto estão preenchidas (pelo menos nome e email),
    Então deve ser-me exibida uma mensagem de confirmação da activação desta
    funcionalidade.*/
    public void testConfirmationWindow() {
        openConfirmWindow();
        assertTrue("A mensagem de confirmação não apareceu",
                solo.waitForText(getActivity().getString(R.string.confirmation), 1, 5000));
    }

    // [X]
    /*Dado que estou na mensagem de confirmação da funcionalidade "I'm here",
    Quando pressiono no botão "Sim" e tenho rede, Então os meus dados devem
    ser submetidos e partilhados com os restantes participantes que activaram a
    funcionalidade "I'm here" e o botão deve mudar para o estado activo.*/
    public void testConfirmSubmission() {
        confirmSubmission();
        solo.clickOnActionBarItem(R.id.action_i_am_here);
    }


    // [X]
    /*Dado que estou na mensagem de confirmação da funcionalidade "I'm here",
    Quando pressiono o botão "Não", Então deve ser exibido o ecrã inicial
    mantendo-se a funcionalidade "I'm here" desactivada.*/
    public void testConfirmSubmissionNotAccepted() {
        openConfirmWindow();
        solo.clickOnButton(solo.getString(R.string.no));
        solo.waitForDialogToClose();
        solo.assertCurrentActivity("Não fiquei na main activity", MainActivity.class);
        assertIcon("Icon mudou", R.id.action_i_am_here, R.drawable.ic_action_alone);
    }

    // [x]
    /*Dado que estou a visualizar a mensagem perguntando se pretendo preencher as
    informações do meu contacto, Quando selecciono "Sim", Então devo ser
    reenchaminhado para o ecrã de preenchimento de detalhes do meu contacto.*/
    public void testConfirmContactFill() {
        setUserContact(new Contact());
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        solo.waitForDialogToOpen();
        solo.clickOnButton(solo.getString(R.string.yes));
        assertTrue("Não mudou para a atividade de definiçoes de contacto pessoais", solo.waitForActivity(ContactSettingsActivity.class, 5000));
    }

    // [X]
    /*Dado que estou a visualizar a mensagem perguntando se pretendo preencher as
    informações do meu contacto, Quando selecciono "Não", Então deve ser
    exibido o ecrã inicial mantendo-se a funcionalidade "I'm here" desactivada.*/
    public void testDontFillContactData() {
        setUserContact(new Contact());
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        solo.waitForDialogToOpen();
        solo.clickOnButton(solo.getString(R.string.cancel));
        solo.waitForDialogToClose();
        solo.assertCurrentActivity("Não ficou na main activity", MainActivity.class);
        assertIcon("Icon mudou", R.id.action_i_am_here, R.drawable.ic_action_alone);
    }

    // [X]
    /*Dado que estou no ecrã de preenchimento de detalhes do meu contacto,
    Quando termino o preenchimento dos dados, pressiono o botão de back, e
    tenho as informações do meu contacto preenchidas (pelo menos nome e email),
    Então deve-me ser exibida uma mensagem de confirmação da activação desta
    funcionalidade.*/
    public void testSaveContactData() {
        testConfirmContactFill();
        fillContactSettings();
        solo.goBack();
        assertTrue("Não voltou à Activity anterior", solo.waitForActivity(MainActivity.class));
        assertTrue("Não apareceu a janela de confirmação", solo.searchText(solo.getString(R.string.ad_confirmShare)));
    }

    // [x]
    /*Dado que estou na mensagem de confirmação da funcionalidade "I'm here",
    Quando pressiono no botão "Sim" e não tenho rede, Então deve-me ser
    apresentada uma mensagem informativa a indicar a falta de rede.*/
    public void testConfirmSubmissionNoConn() {
        openConfirmWindow();

        //garantir que nao tem rede (desligar wifi e mobile date)
        solo.setWiFiData(false);
        //solo.setMobileData(false); no nosso ambiente de testes isto não existe

        solo.clickOnButton(getActivity().getString(R.string.yes));

        boolean showed = solo.waitForText(getActivity().getString(R.string.error_connectivity));

        //voltar a ligar o wifi e mobile date
        solo.setWiFiData(true);
        //solo.setMobileData(true); no nosso ambiente de testes isto não existe

        assertTrue("A mensagem informativa a indicar a falta de rede não apareceu.", showed);

    }

    // [x]
    /*Dado que estou no ecrã de preenchimento de detalhes do meu contacto,
    Quando termino o preenchimento dos dados, pressiono o botão de back, e não
    tenho as informações do meu contacto preenchidas (pelo menos nome e email),
    Então deve ser exibido o ecrã inicial mantendo-se a funcionalidade "I'm here"
    desactivada.*/
    public void testIncompleteDataContact() {
        testConfirmContactFill();
        clearEditText(solo.getString(R.string.name));
        clearEditText(solo.getString(R.string.email));
        solo.goBack();
        confirmInitialDisplayAndImHereAlone();
    }

    // [x]
    /*Dado que estou na aplicação, Quando o botão "I'm here" está activo, Então
    devo poder pressioná-lo de maneira a desactivar a funcionalidade "I'm here".*/
    public void testCheckout() {
        confirmSubmission();
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        assertIcon("O icone não mudou para o esperado", R.id.action_i_am_here, R.drawable.ic_action_alone);
    }

    // [x] Part 1/2
    /*Dado que estou na aplicação, Então o botão "I'm here" deve estar sempre visível
    e deve estar activo caso eu a funcionalidade "I'm here" esteja activa (e vice-
    versa).*/
    public void testActiveIcon() {
        confirmSubmission();
        assertIcon("O icone não está no esperado", R.id.action_i_am_here, R.drawable.ic_action_group);
    }

    // [x] Part 2/2
    /*Dado que estou na aplicação, Então o botão "I'm here" deve estar sempre visível
    e deve estar activo caso eu a funcionalidade "I'm here" esteja activa (e vice-
    versa).*/
    public void testDeactiveIcon() {
        assertIcon("O icone não está no esperado", R.id.action_i_am_here, R.drawable.ic_action_alone);
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

    private void openConfirmWindow() {
        setUserContact(new Contact("nome exemplo", "email@email.com"));
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        assertTrue("Não apareceu caixa de dialogo", solo.waitForDialogToOpen());
    }

    private void confirmInitialDisplayAndImHereAlone() {
        solo.assertCurrentActivity("Não ficou na main activity", MainActivity.class);
        assertIcon("O icon mudou, não se mantendo no esperado", R.id.action_i_am_here, R.drawable.ic_action_alone);
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

