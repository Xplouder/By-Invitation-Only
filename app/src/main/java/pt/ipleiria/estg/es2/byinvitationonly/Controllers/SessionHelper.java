package pt.ipleiria.estg.es2.byinvitationonly.Controllers;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class SessionHelper {
    public static String calculateRemainingTimeString(String endHour) {
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            String ct = df.format(c.getTime());
            Date outDate;
            Date currentTime;

            outDate = df.parse(endHour);
            currentTime = df.parse(ct);

            long different = outDate.getTime() - currentTime.getTime();
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            long elapsedMinutes = different / minutesInMilli;

            if (elapsedMinutes < 1) {
                return "<1";
            } else {
                return String.valueOf(elapsedMinutes);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static long calculateRemainingTimeLong(String endHour) {
        try {
            Calendar c = Calendar.getInstance();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm", Locale.ENGLISH);
            String ct = df.format(c.getTime());
            Date outDate;
            Date currentTime;

            outDate = df.parse(endHour);
            currentTime = df.parse(ct);

            long different = outDate.getTime() - currentTime.getTime();
            long secondsInMilli = 1000;
            long minutesInMilli = secondsInMilli * 60;
            return different / minutesInMilli;

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
