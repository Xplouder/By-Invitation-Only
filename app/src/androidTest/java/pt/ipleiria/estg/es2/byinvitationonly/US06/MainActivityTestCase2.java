package pt.ipleiria.estg.es2.byinvitationonly.US06;

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
import java.util.LinkedList;
import java.util.Map;

import pt.ipleiria.estg.es2.byinvitationonly.Controllers.FirebaseController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.NetworkController;
import pt.ipleiria.estg.es2.byinvitationonly.Controllers.SharedPreferenceController;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.HomepageFragment;
import pt.ipleiria.estg.es2.byinvitationonly.Drawer.SectionFragments.WhoIsHereFragment;
import pt.ipleiria.estg.es2.byinvitationonly.MainActivity;
import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.byinvitationonly.R;


public class MainActivityTestCase2 extends ActivityInstrumentationTestCase2<MainActivity> {

    private Contact myContact;
    private Solo solo;
    private int result;
    private LinkedList<String> contactKeyList = new LinkedList<>();

    public MainActivityTestCase2() {
        super(MainActivity.class);
    }

    public static void removeContactOnServer(String contactKey) {
        Firebase storedContact = new Firebase(contactKey);
        storedContact.removeValue();
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

    //[x] 1
   /* Dado que a funcionalidade "I'm Here" está ativa e
    existem participantes que estão disponíveis no evento para fazer networking e comunicar,
    quando escolho a opção "Who is Here" do navegador, então deve ser apresentado o ecrã "Who is Here"
    com a listagem de todos os participantes disponíveis excepto os dados do próprio participante.*/
    public void testListWhoIsHere() {
        removeAllContactsOnServer();
        //Garante que existe apenas 1 contacto disponivel
        Contact c1 = new Contact("teste1", "Text1@mail.com");
        createContactTest(c1);
        enableImHere();
        assertFalse("O nome do meu contacto aparece na lista", solo.searchText(myContact.getName()));
        assertFalse("O email do meu contacto aparece na lista", solo.searchText(myContact.getEmail()));
        assertTrue("Nome do contacto não esta na lista", solo.searchText(c1.getName()));
        assertTrue("Email do contacto não esta na lista", solo.searchText(c1.getEmail()));

    }

    // [x]2
    /* Dado que a funcionalidade "I'm Here" está ativa e escolho a opção "Who is Here" do navegador
    caso não haja participantes  disponíveis no evento para fazer networking e comunicar então
    deve-me ser apresentado o ecrã "Who is Here" com uma mensagem a informar
    que não existem participantes disponíveis.*/
    public void testMessageIfNoPeople() {
        enableImHere();
        removeAllContactsOnServer();
        solo.searchText(solo.getString(R.id.empty_data_WhoIsHere));
    }

    //[X] 3
    /*Dado que estou no ecrã "Who is Here" e a funcionalidade "I'm here" está ativa,
    quando clico no botão "I'm here", a funcionalidade "I'm here" deverá ser desativada e
    devo ser reencaminhado para o ecrã principal.*/
    public void testWhoIsHereDeactivated() {
        // Ativa o I'm Here onde fica automaticamente no frag Who Is Here
        enableImHere();
        // Desativar I'm Here
        solo.clickOnActionBarItem(R.id.action_i_am_here);
        solo.sleep(2000);
        assertIcon("A funcionalidade I'm Here não foi desativada", R.id.action_i_am_here, R.drawable.ic_action_alone);
        assertTrue("Não foi apresentada a Homepage",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof HomepageFragment);
    }

    //[X] 5
    /* Dado que a funcionalidade "I'm Here" está ativa, estou no ecrã "Who is Here"
    e existem participantes que estão disponíveis no evento para fazer networking e comunicar,
    quando um participante que previamente tinha a funcionalidade "I'm Here"
    desativa faz checkin então a listagem de participantes que estou a ver é atualizada
    aparecendo os participantes previamente listados juntamente com o novo participante.*/
    public void testNewPersonOnList() {
        removeContactsOnServer();
        // Garantia que só existe um contacto no servidor
        Contact c1 = new Contact("teste1", "teste@teste1.com");
        createContactTest(c1);
        enableImHere();
        assertTrue("Não foi encontrado nenhum participante teste ", solo.searchText(c1.getName()) && solo.searchText(c1.getEmail()));
        Contact c2 = new Contact("testeNewContact", "testNewContact@testNewContact.com");
        createContactTest(c2);
        assertTrue("Não foi encontrado nenhum participante teste ", solo.searchText(c2.getName()) && solo.searchText(c2.getEmail()));
    }

    //[x]6
    /*Dado que a funcionalidade "I'm Here" está ativa, estou no ecrã "Who is Here" e
    existem participantes que estão disponíveis no evento para fazer networking e comunicar,
    quando um participante que previamente tinha a funcionalidade "I'm Here" ativa faz checkout então a
    listagem de participantes que estou a ver é atualizada aparecendo os participantes previamente listados
    menos o participante que fez o checkout.*/
    public void testWhenPersonCheckout() {
        removeContactsOnServer();
        // Garantia que só existe um contacto no servidor
        Contact c1 = new Contact("testeContactoCheckout", "testeContactoCheckout@checkout.com");
        String keyContact = createNewContactOnServer(c1);
        enableImHere();
        assertTrue("Não foi encontrado nenhum participante teste ", solo.searchText(c1.getName()) && solo.searchText(c1.getEmail()));
        removeContactOnServer(keyContact);
        assertFalse("Contacto não foi removido com sucesso ", solo.searchText(c1.getName()) && solo.searchText(c1.getEmail()));
    }

    //[x]7
    /*Dado que a funcionalidade "I'm Here" está ativa, estou no ecrã "Who is Here" e existem participantes
    que estão disponíveis no evento para fazer networking e comunicar, quando um participante que previamente tinha a
    funcionalidade "I'm Here" ativa altera os seu dados pessoais então a listagem de participantes que estou a ver é atualizada
    aparecendo os participantes previamente listados juntamente com os novos dados do participante que fez a ateração.*/
    public void testIfAPersonChangePersonalData() {
        removeContactsOnServer();
        // Garantia que só existe um contacto no servidor
        Contact c1 = new Contact("teste1", "teste@teste1.com");
        String keyContact = createNewContactOnServer(c1);
        enableImHere();
        assertTrue("Não foi encontrado nenhum participante teste ", solo.searchText(c1.getName()) && solo.searchText(c1.getEmail()));
        Contact c2 = new Contact("testeAlterado", "testeAlterado@alterado.com");
        updateContactOnServer(keyContact, c2);
        // delay de garantia para o servidor atualize o contacto e este ser recebido no dispositivo
        solo.sleep(2000);
        assertTrue("Não foi encontrado o participante que alterou os seus dados ", solo.searchText(c2.getName()) && solo.searchText(c2.getEmail()));
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

    //[x]8
    /*Dado que a funcionalidade "I'm Here" está ativa e não existe rede, quando escolho a
    opção "Who is Here" do navegador, então deve ser apresentado o ecrã "Who is Here" com
    uma lista de participantes vazios juntamente com uma caixa de diálogo a informar que não existe rede.*/
    public void testAlertDialogIfNoExistsNetwork() {
        enableImHere();
        // Desativa a rede e troca de fragmento só para garantir que o teste está num fragmento diferente do Who is Here
        solo.setWiFiData(false);
        if (!isDrawerStatusOpen()) {
            invertDrawerStatus();
        }
        solo.clickOnText(solo.getString(R.string.title_homepage_fragment));
        solo.sleep(1000);
        assertTrue("Não trocou de fragmento", getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof HomepageFragment);
        // Abre o fragmento Who is Here
        if (!isDrawerStatusOpen()) {
            invertDrawerStatus();
        }
        solo.clickOnText(solo.getString(R.string.title_who_is_here_fragment));
        solo.sleep(1000);
        assertTrue("Não trocou para o fragmento Who is Here",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof WhoIsHereFragment);

        solo.waitForDialogToOpen();
        assertTrue("Caixa de dialogo não apareceu", solo.searchText(solo.getString(R.string.error_connectivity)));
    }

    private void setImHereSharedPrefs(boolean imhere) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getInstrumentation().getTargetContext());
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getInstrumentation().getTargetContext().getString(R.string.i_am_here), imhere);
        editor.apply();
    }

    private void assertIcon(String message, int item, int waitedIcon) {
        TextView tv = (TextView) solo.getCurrentActivity().findViewById(item);
        BitmapDrawable icon = (BitmapDrawable) tv.getCompoundDrawables()[0];
        BitmapDrawable esperado = (BitmapDrawable) solo.getCurrentActivity().getDrawable(waitedIcon);
        assertTrue("Não foi encontrado o icon esperado", esperado != null);
        boolean same = esperado.getBitmap().sameAs(icon.getBitmap());
        assertTrue(message, same);
    }

    private String getLocalContactKey() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity().getApplicationContext());
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
        solo.clickOnActionBarHomeButton();
        solo.sleep(1000);
    }

    private void openConfirmWindow() {
        setUserContact(new Contact("nome exemplo", "email@email.com"));

        solo.clickOnActionBarItem(R.id.action_i_am_here);
        assertTrue("Não apareceu caixa de dialogo", solo.waitForDialogToOpen());
    }

    private void setUserContact(Contact contact) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity());
        SharedPreferences.Editor e = prefs.edit();
        e.putString(solo.getString(R.string.pref_key_name), contact.getName());
        e.putString(solo.getString(R.string.pref_key_email), contact.getEmail());
        e.apply();
        this.myContact = contact;
    }

    private void createContactTest(Contact newContact) {
        createNewContactOnServer(newContact);
        solo.sleep(2000);
    }

    private String createNewContactOnServer(Contact contact) {
        Firebase contactsNode = FirebaseController.fbContactsNode;
        Firebase newcontact = contactsNode.push();
        newcontact.child(FirebaseController.CONTACT_ATTRIBUTES[0]).setValue(contact.getName());
        newcontact.child(FirebaseController.CONTACT_ATTRIBUTES[1]).setValue(contact.getEmail());
        contactKeyList.add(newcontact.toString());
        return newcontact.toString();
    }

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
        //assertTrue("Não voltou para o ecra principal", solo.searchText(solo.getString(R.string.title_homepage_fragment)));
        solo.sleep(2000);
        assertTrue("Não apareceu o fragmento Who is Here",
                getActivity().getFragmentManager().findFragmentById(R.id.container) instanceof WhoIsHereFragment);
        solo.waitForView(R.id.action_i_am_here);

        int res = invokeContactDataRequestOnServer();
        assertTrue("Não existe no servidor o contacto teste criado", res == 1);

        final String localContactKey = getLocalContactKey();
        Firebase storedContact = FirebaseController.fbContactsNode.child(localContactKey);
        storedContact.removeValue();

        assertIcon("O icone não mudou para o esperado", R.id.action_i_am_here, R.drawable.ic_action_group);
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

    private void removeContactsOnServer() {
        for (String contactKey : contactKeyList) {
            if (!contactKey.isEmpty()) {
                Firebase storedContact = new Firebase(contactKey);
                storedContact.removeValue();
            }
        }
    }

    private void updateContactOnServer(String keyContact, Contact contact) {

        // Referência para o node do contacto a atualizar
        Firebase storedContact = new Firebase(keyContact);

        // Criação e preenchimento de um hashmap do contacto com os dados atualizados
        Map<String, Object> updatedContact = new HashMap<>();
        updatedContact.put(FirebaseController.CONTACT_ATTRIBUTES[0], contact.getName());
        updatedContact.put(FirebaseController.CONTACT_ATTRIBUTES[1], contact.getEmail());

        // Atualização na base de dados
        storedContact.updateChildren(updatedContact);
    }

    public void removeAllContactsOnServer() {
        Firebase nodeContacts = FirebaseController.fbContactsNode;
        nodeContacts.removeValue();
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

