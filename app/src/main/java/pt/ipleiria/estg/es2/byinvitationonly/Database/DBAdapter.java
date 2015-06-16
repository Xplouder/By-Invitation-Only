package pt.ipleiria.estg.es2.byinvitationonly.Database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

import java.util.LinkedList;

import pt.ipleiria.estg.es2.byinvitationonly.Models.Session;

public class DBAdapter {
    private DBHelper dbHelper;

    public DBAdapter(Context context) {
        dbHelper = new DBHelper(context);
    }

    public void addSession(Session session) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DBHelper.ID, session.getFirebaseSessionNode());
        values.put(DBHelper.ABSTRACT, session.getAbstracts());
        values.put(DBHelper.DATE, session.getDateFormattedString());
        values.put(DBHelper.END, session.getEndHour());
        values.put(DBHelper.PRESENTER, session.getPresenter());
        values.put(DBHelper.ROOM, session.getRoom());
        values.put(DBHelper.START, session.getStartHour());
        values.put(DBHelper.TITLE, session.getTitle());
        values.put(DBHelper.TRACK, session.getTrack());
        values.put(DBHelper.RATING, String.valueOf(session.getMyRating()));
        values.put(DBHelper.ONAGENDA, String.valueOf(session.isOnAgenda()));
        db.insert(DBHelper.TABLE_NAME, null, values);
        db.close();
    }

    public void removeSession(Session session) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String escapedID = DatabaseUtils.sqlEscapeString(session.getFirebaseSessionNode());
        db.delete(DBHelper.TABLE_NAME, DBHelper.ID + " = " + escapedID, null);
        db.close();
    }

    public LinkedList<Session> getSessions() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        LinkedList<Session> sessionOnAgenda = new LinkedList<>();
        Cursor cursor = db.rawQuery("select * from " + DBHelper.TABLE_NAME, null);
        while (cursor.moveToNext()) {

            // String date, String startHour, String endHour, String room,
            // String track, String title, String presenter, String abstracts,
            // String myRating, String firebaseSessionNode, String onAgenda

            sessionOnAgenda.add(new Session(cursor.getString(2), cursor.getString(6), cursor.getString(3),
                    cursor.getString(5), cursor.getString(8), cursor.getString(7), cursor.getString(4),
                    cursor.getString(1), cursor.getString(9), cursor.getString(0), cursor.getString(10)));
        }
        cursor.close();
        db.close();
        return sessionOnAgenda;
    }

    public boolean existsSessionOnAgenda(Session session) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String escapedID = DatabaseUtils.sqlEscapeString(session.getFirebaseSessionNode());
        Cursor cursor = db.rawQuery(
                "select * from " + DBHelper.TABLE_NAME +
                        " where " + DBHelper.ID + "=" + escapedID, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return true;
        } else {
            cursor.close();
            db.close();
            return false;
        }
    }

    public void updateSession(Session session) {
        removeSession(session);
        addSession(session);
    }

    public void removeAllSessions() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.execSQL("delete from " + DBHelper.TABLE_NAME);
        db.close();
    }
}
