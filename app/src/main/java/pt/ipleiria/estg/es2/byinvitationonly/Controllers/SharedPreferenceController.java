package pt.ipleiria.estg.es2.byinvitationonly.Controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Random;

import pt.ipleiria.estg.es2.byinvitationonly.Models.Contact;
import pt.ipleiria.estg.es2.byinvitationonly.R;


public class SharedPreferenceController {

    public static final String MY_LOCAL_CONTACT_KEY = "pt.ipleiria.estg.es2.byinvitationonly.MY_LOCAL_CONTACT_KEY";
    public static final String FILTER_STATE = "pt.ipleiria.estg.es2.byinvitationonly.FILTER_STATE";

    public static Contact getLocalStoredUserContact(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String name = prefs.getString(context.getString(R.string.pref_key_name), "");
        String email = prefs.getString(context.getString(R.string.pref_key_email), "");
        return new Contact(name, email);
    }

    public static boolean isImHereActive(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.i_am_here), false);
    }

    public static void saveImHereSharedPref(Context context, boolean imhere) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(context.getString(R.string.i_am_here), imhere);
        editor.apply();
    }

    public static String getLocalContactKey(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(MY_LOCAL_CONTACT_KEY, "");
    }

    public static boolean existLocalContactKey(Context context) {
        if (getLocalContactKey(context).equals("")) {
            return false;
        } else if (getLocalContactKey(context).contains("Anonymous")) {
            return false;
        }
        return true;
    }

    public static String getUserID(Context context) {
        if (existLocalContactKey(context)) {
            return getLocalContactKey(context);
        } else {
            if (getLocalContactKey(context).contains("Anonymous")) {
                return getLocalContactKey(context);
            } else {
                Random rand = new Random(System.currentTimeMillis());
                int randomId = rand.nextInt((999999998 - 1) + 1) + 1;

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = pref.edit();
                editor.putString(SharedPreferenceController.MY_LOCAL_CONTACT_KEY, "Anonymous" + randomId);
                editor.apply();

                return getLocalContactKey(context);
            }
        }
    }

    public static void disableFilterState(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FILTER_STATE, false);
        editor.apply();
    }

    public static void enableFilterState(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(FILTER_STATE, true);
        editor.apply();
    }

    public static boolean getFilterState(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getBoolean(FILTER_STATE, false);
    }

}
