package pt.ipleiria.estg.es2.byinvitationonly.US08;

import android.content.SharedPreferences;
import android.graphics.drawable.BitmapDrawable;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.ActivityInstrumentationTestCase2;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.robotium.solo.Solo;

import java.util.HashMap;
import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.ContactChatActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.WhoIsHereFragment;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Message;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


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
        removeAllContactsOnServer();
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
    /*Dado que estou no ecrã "Who is here" e tenho rede,
    quando seleciono o icon de "chat" de um contacto deve-me
    ser mostrado um novo ecrã com uma janela de conversação
    do contacto selecionado*/
    public void testWindowChat() {
        solo.setWiFiData(true);
        removeAllContactsOnServer();
        createNewContactOnServer(testContact);
        confirmSubmission();
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.imageMessageIcon));
        solo.assertCurrentActivity("Não apareceu o ecrã de conversação", ContactChatActivity.class);
        assertTrue("O ecrã de conversação que apareceu não era para o contacto selecionado anteriormente",
                solo.searchText("Test"));
    }

    //[x]2
    /*Dado que estou no ecrã de "chat" e tenho rede, quando
    seleciono a opção "Enviar mensagem" e a caixa de texto
    não está vazia, então a mensagem que eu escrevi deve
    ser enviada para o contacto selecionado*/
    public void testSendMessage() {
        testWindowChat();
        EditText et = (EditText) solo.getCurrentActivity().findViewById(R.id.editTextMessage);
        solo.waitForView(et);
        solo.clearEditText(et);
        solo.enterText(et, "Mensagem Test");
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.buttonSendMessage));
        assertTrue("A mensagem não existe no servidor", isMessageOnServer("Mensagem Test"));
        removeAllMessagesOnServer();
    }

    //[x]3
    /*Dado que estou no ecrã de "chat" e tenho rede, quando seleciono
    a opção "Enviar mensagem" e a caixa de texto está vazia, então
    não deve ser enviado nada para o contacto selecionado*/
    public void testEmptyMessage() {
        testWindowChat();
        EditText et = (EditText) solo.getCurrentActivity().findViewById(R.id.editTextMessage);
        solo.waitForView(et);
        solo.clearEditText(et);
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.buttonSendMessage));
        assertTrue("A caixa de dialogo nao abriu", solo.waitForDialogToOpen());
        assertTrue("O titulo da mensagem nao está correto", solo.searchText(solo.getString(R.string.title_empty_message)));
        assertTrue("A mensagem nao está correta", solo.searchText(solo.getString(R.string.message_empty_message)));
        solo.clickOnText(solo.getString(R.string.ok));
        assertTrue("A caixa de dialogo nao fechou", solo.waitForDialogToClose());
        assertFalse("A mensagem existe no servidor", isMessageOnServer(""));
    }

    //[x] 4
    //Dado que estou no ecrã de "chat" e tenho rede,
    // quando recebo uma mensagem do contacto com que estou a fazer chat,
    // deve-me aparecer a mensagem recebida no ecrã
    public void testReceivedMessages() {
        testWindowChat();
        Message message = new Message(SharedPreferenceController.getLocalStoredUserContact(getActivity()),
                testContact, "Teste Mensagem para teste 4");
        sendMessageToFirebase(message);
        assertTrue("A mensagem não apareceu", solo.waitForText("Teste Mensagem para teste 4"));
        removeAllMessagesOnServer();
    }

    //[x]5
    /*Dado que estou no ecrã "Who is Here" e não tenho rede,
    quando seleciono o icon de "chat" deve-me ser mostrada
    uma mensagem a informar que não tenho rede*/
    public void testGoChatNoConn() {
        removeAllContactsOnServer();
        createNewContactOnServer(testContact);
        solo.setWiFiData(true);
        confirmSubmission();
        solo.setWiFiData(false);
        assertTrue("Icone para chat nao apareceu", solo.waitForView(solo.getCurrentActivity().findViewById(R.id.imageMessageIcon)));
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.imageMessageIcon));
        assertTrue("A caixa de dialogo nao abriu", solo.waitForDialogToOpen());
        assertTrue("O titulo da mensagem nao está correto", solo.searchText(solo.getString(R.string.warning)));
        assertTrue("A mensagem nao está correta", solo.searchText(solo.getString(R.string.error_connectivity)));
    }

    //[x]6
    /*Dado que estou no ecrã de "chat" e não tenho rede, quando
    seleciono a opção "Enviar mensagem", então deve-me ser
    mostrada uma mensagem a informar que não tenho rede*/
    public void testSendMessageNoConn() {
        testWindowChat();
        solo.setWiFiData(false);
        solo.clickOnView(solo.getCurrentActivity().findViewById(R.id.buttonSendMessage));
        assertTrue("A caixa de dialogo nao abriu", solo.waitForDialogToOpen());
        assertTrue("O titulo da mensagem nao está correto", solo.searchText(solo.getString(R.string.warning)));
        assertTrue("A mensagem nao está correta", solo.searchText(solo.getString(R.string.error_connectivity)));
    }

    //[x]7
    /*Dado que estou no ecrã de "chat", quando seleciono a
    opção "back" devo ser reencaminhado para o ecrã "Who Is Here"*/
    public void testChatBack() {
        testWindowChat();
        solo.goBack();
        assertTrue("Nao mudou para MainActivity", solo.waitForActivity(MainActivity.class));
        assertTrue("Não trocou para who is here", getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof WhoIsHereFragment);
    }

    //[x]8
    /*Dado que estou a navegar na aplicação e tenho a funcionalidade
    I'm Here ativada, quando recebo uma nova mensagem deve-me ser
    mostrada uma notificação a informar-me que tenho novas mensagens*/
    public void testSeeNot() {
        removeAllMessagesOnServer();
        confirmSubmission();
        Message message = new Message(SharedPreferenceController.getLocalStoredUserContact(getActivity()),
                testContact, "Teste Mensagem para teste 8");
        sendMessageToFirebase(message);
        //A partir daqui o teste tem de ser feito manualmente
        //porque não conseguimos testar comportamentos fora da aplicação
        /*
            Verficar que a notificação apareceu.
         */
    }

    //[x]9
    /*Dado que estou a navegar na aplicação, tenho a funcionalidade
    I'm Here ativada e recebo uma notificação de mensagens novas,
    quando seleciono a notificação então deve-me ser mostrada uma
    caixa de dialogo com os contactos que me enviaram novas mensagens*/
    public void testClickNotSeeAlertDialog() {
        testSeeNot();
        //A partir daqui o teste tem de ser feito manualmente
        //porque não conseguimos testar comportamentos fora da aplicação
        /*
            Expandir a area de notificações
            Clicar na notificação
            Verificar que apareceu a alert dialog com o utilizador com o nome "Test"
         */
    }

    //[x]10
    /*Dado que estou a navegar na aplicação, tenho a funcionalidade
    I'm Here ativada e recebo uma notificação de mensagens novas,
    quando seleciono a notificação e me é mostrada a caixa de dialogo
    com os contactos que me enviaram mensagens, então quando seleciono
    um contacto da caixa de dialogo devo ser reencaminhado
    para o ecrã de chat desse contacto*/
    public void testClickAlertDialogSeeMessage() {
        testClickNotSeeAlertDialog();
        //A partir daqui o teste tem de ser feito manualmente
        //porque não conseguimos testar comportamentos fora da aplicação
        /*
            Clicar no nome "Test" na alert dialog
            Verificar que apareceu a mensagem "Teste Mensagem para teste 8"
            no ecra de chat do contacto "Test"
         */
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

    private void sendMessageToFirebase(Message message) {
        FirebaseController.sendMessage(message);
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

