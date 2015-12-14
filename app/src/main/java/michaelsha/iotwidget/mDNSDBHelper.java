package michaelsha.iotwidget;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by michael on 12/13/15.
 */
public class mDNSDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "mdns.db";

    public mDNSDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    /* Inner class that defines the table contents */
    public static abstract class mDNSEntry implements BaseColumns {
        public static final String TABLE_NAME = "entry";
        public static final String COLUMN_NAME_MDNS = "mdns";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_IP = "ip";
        public static final String COLUMN_NAME_PORT = "port";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String INT_TYPE = " INT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + mDNSEntry.TABLE_NAME + " (" +
                    mDNSEntry._ID + " INTEGER PRIMARY KEY," +
                    mDNSEntry.COLUMN_NAME_MDNS + TEXT_TYPE + "UNIQUE KEY" + COMMA_SEP +
                    mDNSEntry.COLUMN_NAME_TYPE + TEXT_TYPE + COMMA_SEP +
                    mDNSEntry.COLUMN_NAME_IP + TEXT_TYPE + COMMA_SEP +
                    mDNSEntry.COLUMN_NAME_PORT + INT_TYPE +
                    " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + mDNSEntry.TABLE_NAME;

}
