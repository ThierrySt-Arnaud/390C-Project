package ca.concordia.teamc.soundlevelapp;

import android.content.ContentValues;
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
    private static final String COL1 = "SENSOR_NAME";
    private static final String COL2 = "MAC_ADDRESS";
    private static final String COL3 = "LOCATION";
    private static final String COL4 = "LAST_KNOWN_PROJECT";
    private static final String COL5 = "LAST_CONNECTION_DATE";
    private static final String COL6 = "RECORDING_STATUS";
    private static final String COL7 = "START_RECORDING_DATA";



    public DatatabaseHelper(Context context){
        super(context, TABLE_NAME, null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_NAME + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " SENSOR_NAME TEXT, MAC_ADDRESS TEXT, LOCATION TEXT, LAST_KNOWN_PROJECT TEXT, LAST_CONNECTION_DATE TEXT, RECORDING_STATUS TEXT, START_RECORDING_DATA TEXT)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP IF TABLE EXISTS "+ TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public boolean addData(String sensorName,String macAddress, String location, String lastKnownProject, String lastConnectionDate , String recordingStatus, String startRecordingData){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, sensorName);
        contentValues.put(COL2, macAddress);
        contentValues.put(COL3, location);
        contentValues.put(COL4, lastKnownProject);
        contentValues.put(COL5, lastConnectionDate);
        contentValues.put(COL6, recordingStatus);
        contentValues.put(COL7, startRecordingData);

        long result = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);

        if(result == -1){
            return false;
        }else{
            return true;
        }
    }
}

