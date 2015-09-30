package pt.ipleiria.estg.es2.byinvitationonly.Models;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class Session implements Serializable, Comparable<Session> {
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
    private Date date;
    private String startHour;
    private String endHour;
    private String room;
    private String track;
    private String title;
    private String presenter;
    private String abstracts;
    private float myRating;
    private String firebaseSessionNode;
    private boolean onAgenda;

    // Constructor for sessions from File
    public Session(String date, String startHour, String endHour, String room, String track, String title, String presenter, String abstracts) {
        this.startHour = startHour;
        this.date = stringToDate(date);
        this.endHour = endHour;
        this.room = room;
        this.track = track;
        this.title = title;
        this.presenter = presenter;
        this.abstracts = abstracts;
        this.myRating = 0.0f;
        this.firebaseSessionNode = null;
    }

    // Constructor for sessions from Firebase
    public Session(String date, String startHour, String endHour, String room,
                   String track, String title, String presenter, String abstracts,
                   String myRating, String firebaseSessionNode, String onAgenda) {
        this.startHour = startHour;
        this.date = stringToDate(date);
        this.endHour = endHour;
        this.room = room;
        this.track = track;
        this.title = title;
        this.presenter = presenter;
        this.abstracts = abstracts;
        this.myRating = Float.parseFloat(myRating);
        if (firebaseSessionNode.equals("null"))
            this.firebaseSessionNode = null;
        else
            this.firebaseSessionNode = firebaseSessionNode;
        this.onAgenda = Boolean.valueOf(onAgenda);
    }

    private Date stringToDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            Log.d("Input Date error", "Incorrect input date format!");
            return new Date(0);
        }
    }

    public boolean isOnAgenda() {
        return onAgenda;
    }

    public void setOnAgenda(boolean b) {
        onAgenda = b;
    }

    public Date getDate() {
        return date;
    }

    public String getDayMonthOnString() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        // A API do Java começa os meses com indice 0 em Janeiro
        int month = cal.get(Calendar.MONTH) + 1;
        String monthName = getMonthForInt(month - 1);
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.valueOf(day) + " " + monthName;
    }

    public String getDateFormattedString() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        // A API do Java começa os meses com indice 0 em Janeiro
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);
        return String.valueOf(year) + "/" + (month < 10 ? ("0" + month) : (month)) + "/" + (day < 10 ? ("0" + day) : (day));
    }

    public String getStartHour() {
        return startHour;
    }

    public String getEndHour() {
        return endHour;
    }

    public String getRoom() {
        return room;
    }

    public String getTrack() {
        return track;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPresenter() {
        return presenter;
    }

    public String getAbstracts() {
        return abstracts;
    }

    @Override
    public int compareTo(@NonNull Session anotherSession) {

        if (this.getDate() == null || anotherSession.getDate() == null)
            return 0;

        int aux1;
        int aux2;
        //Se as datas dos dias for igual, compara a hora inicial
        if (getDate().compareTo(anotherSession.getDate()) == 0) {
            aux1 = getStartHour().compareToIgnoreCase(anotherSession.getStartHour());
            if (aux1 > 0) {
                return 1;
            } else if (aux1 == 0) {
                //Se as datas dos dias forem iguais e os startSession iguais tambem
                aux2 = getEndHour().compareToIgnoreCase(anotherSession.getEndHour());
                if (aux2 > 0) {
                    return 1;
                } else if (aux2 == 0) {
                    return 0;
                } else if (aux2 < 0) {
                    return -1;
                }
            } else if (aux1 < 0) {
                return -1;
            }
        }

        return getDate().compareTo(anotherSession.getDate());
    }

    public String getDay() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        return String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
    }

    public String getMonthAndYear() {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        return String.valueOf(month) + " - " + year;
    }

    private int getIntStartHour() {
        String[] sArray = startHour.split(":");
        return Integer.parseInt(sArray[0]);
    }

    private int getIntStartMinute() {
        String[] sArray = startHour.split(":");
        return Integer.parseInt(sArray[1]);
    }

    private int getIntEndHour() {
        String[] sArray = endHour.split(":");
        return Integer.parseInt(sArray[0]);
    }

    private int getIntEndMinute() {
        String[] sArray = endHour.split(":");
        return Integer.parseInt(sArray[1]);
    }

    public boolean hasBegun() {
        GregorianCalendar localDate = new GregorianCalendar();
        GregorianCalendar calAux = new GregorianCalendar();
        calAux.setTime(date);
        int year = calAux.get(Calendar.YEAR);
        int month = calAux.get(Calendar.MONTH);
        int day = calAux.get(Calendar.DAY_OF_MONTH);
        GregorianCalendar sessionStartDate = new GregorianCalendar(year, month, day, getIntStartHour(), getIntStartMinute());
        return sessionStartDate.before(localDate);
    }

    public boolean isActive() {
        GregorianCalendar localDate = new GregorianCalendar();
        GregorianCalendar calAux = new GregorianCalendar();
        calAux.setTime(date);
        int year = calAux.get(Calendar.YEAR);
        int month = calAux.get(Calendar.MONTH);
        int day = calAux.get(Calendar.DAY_OF_MONTH);
        GregorianCalendar sessionStartDate = new GregorianCalendar(year, month, day, getIntStartHour(), getIntStartMinute());
        GregorianCalendar sessionEndDate = new GregorianCalendar(year, month, day, getIntEndHour(), getIntEndMinute());
        return sessionStartDate.before(localDate) && localDate.before(sessionEndDate);
    }

    private String getMonthForInt(int num) {
        String month = "";
        DateFormatSymbols dfs = new DateFormatSymbols();
        String[] months = dfs.getMonths();
        if (num >= 0 && num <= 11) {
            month = months[num];
        }
        return month;
    }

    public float getMyRating() {
        return myRating;
    }

    public void setMyRating(Float myRating) {
        this.myRating = myRating;
    }

    public String getFirebaseSessionNode() {
        return firebaseSessionNode;
    }
}
