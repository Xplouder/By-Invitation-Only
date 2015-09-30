package pt.ipleiria.estg.es2.byinvitationonly.Database;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DBHelper extends SQLiteOpenHelper {

    // TUTORIAL
    // http://developer.android.com/training/basics/data-storage/databases.html
    // http://www.tutorialspoint.com/android/android_sqlite_database.htm
    // http://pplware.sapo.pt/smartphones-tablets/android/tutorial-utilizao-do-sqlite-no-android-parte-i/

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Agenda.db";

    ////////////////////////////////////////////////////////
    public static final String TABLE_NAME = "agenda";
    public static final String ID = "FirebaseNode";
    public static final String ABSTRACT = "Abstract";
    public static final String DATE = "Date";
    public static final String END = "End";
    public static final String PRESENTER = "Presenter";
    public static final String ROOM = "Room";
    public static final String START = "Start";
    public static final String TITLE = "Title";
    public static final String TRACK = "Track";
    public static final String RATING = "Rating";
    public static final String ONAGENDA = "OnAgenda";

    private static final String DATABASE_CREATE = "create table " + TABLE_NAME +
            "( " + ID + " text not null, "
            + ABSTRACT + ", "
            + DATE + ", "
            + END + ", "
            + PRESENTER + ", "
            + ROOM + ", "
            + START + ", "
            + TITLE + ", "
            + TRACK + ", "
            + RATING + ", "
            + ONAGENDA + " );";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        Log.w(DBHelper.class.getName(), "Upgrading database from version " + oldVersion
                + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
