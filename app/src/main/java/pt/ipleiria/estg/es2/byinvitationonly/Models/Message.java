package pt.ipleiria.estg.es2.byinvitationonly.Models;

import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {
    private Contact receiver;
    private Contact sender;
    private String message;
    private Timestamp timestamp;
    private boolean read;

    public Message(Contact receiver, Contact sender, String message) {
        this.receiver = receiver;
        this.sender = sender;
        this.message = message;
        timestamp = new Timestamp(System.currentTimeMillis());
        this.read = false;
    }

    public Message(Contact receiver, Contact sender, String message, String read, String timetamp) {
        this.receiver = receiver;
        this.sender = sender;
        this.message = message;
        this.timestamp = convertStringToTimestamp(timetamp);
        this.read = read.equalsIgnoreCase("true");
    }

    private Timestamp convertStringToTimestamp(String timetampStr) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
            Date parsedDate = dateFormat.parse(timetampStr);
            return new Timestamp(parsedDate.getTime());
        } catch (Exception e) {//this generic but you can control another types of exception
            Log.e("Error", "Não foi possivel converter o timestamp");
            return null;
        }
    }

    public Contact getReceiver() {
        return receiver;
    }

    public Contact getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public boolean isRead() {
        return read;
    }
}
