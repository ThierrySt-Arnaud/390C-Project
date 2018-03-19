package ca.concordia.teamc.soundlevelapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Thilak on 2018-03-18.
 */

public class DatatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";

    private static final String DATABASE_NAME = "SoundMeterTable.db";
    private static final String TABLE_NAME = "SoundMeterTable";
    private static final String COL0 = "ID";
    private static final String COL1 = "sensor_name";
    private static final String COL2 = "MAC_Adress";
    private static final String COL3 = "Location";
    private static final String COL4 = "LastKnownProject";
    private static final String COL5 = "LastConnectionDate";
    private static final String COL6 = "RecordingStatus";
    private static final String COL7 = "StartRecordingDate";



    public DatatabaseHelper(Context context){
        super(context, TABLE_NAME, null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "ID INTEGER PRIMARY KEY AUTOINCREMENT, "+ COL1 +" TEXT";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}

