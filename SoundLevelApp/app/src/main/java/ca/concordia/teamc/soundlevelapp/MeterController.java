package ca.concordia.teamc.soundlevelapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Thilak on 2018-03-18.
 */

public class MeterController extends SQLiteOpenHelper {

    private static MeterController mInstance = null;

    private static final String TAG = "MeterController";

    private static final String DATABASE_NAME = "SoundLevelApp.db";
    private static final String TABLE_METER = "SoundMeterTable";

    private static final String COL0 = "ID";
    private static final String COL1 = "SENSOR_NAME";
    private static final String COL2 = "MAC_ADDRESS";
    private static final String COL3 = "LOCATION";
    private static final String COL4 = "LAST_KNOWN_PROJECT";
    private static final String COL5 = "LAST_CONNECTION_DATE";
    private static final String COL6 = "RECORDING_STATUS";
    private static final String COL7 = "START_RECORDING_DATA";

    private static final String[] COLUMNS = {COL0,COL1,COL2,COL3,COL4,COL5,COL6,COL7};

    public static MeterController getInstance(Context ctx) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new MeterController(ctx.getApplicationContext());
        }
        return mInstance;
    }

    private MeterController(Context context){
        super(context, DATABASE_NAME, null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createDataSetTable = "CREATE TABLE DataSetTable (ID INTEGER PRIMARY KEY AUTOINCREMENT, PROJECT_NAME TEXT, LOCATION TEXT, DATE_OF_DOWNLOAD INTEGER, DATE_START_RECORD INTEGER, METER_REFERENCE_RECORD TEXT, DATA_FILE TEXT)";
        String createDataFileTable = "CREATE TABLE DataFileTable (ID INTEGER PRIMARY KEY AUTOINCREMENT, PROJECT_NAME TEXT, LOCATION TEXT, DATA TEXT)";
        String createMeterTable = "CREATE TABLE SoundMeterTable (ID INTEGER PRIMARY KEY AUTOINCREMENT, SENSOR_NAME TEXT, MAC_ADDRESS TEXT, LOCATION TEXT, LAST_KNOWN_PROJECT TEXT, LAST_CONNECTION_DATE INTEGER, RECORDING_STATUS TEXT, START_RECORDING_DATA INTEGER)";
        sqLiteDatabase.execSQL(createDataSetTable);
        sqLiteDatabase.execSQL(createDataFileTable);
        sqLiteDatabase.execSQL(createMeterTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_METER);
        onCreate(sqLiteDatabase);
    }

    //create meter record


    public boolean addMeterData(Meter meter){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, meter.getSensorName());
        contentValues.put(COL2, meter.getMacAddress());
        contentValues.put(COL3, meter.getLocation());
        contentValues.put(COL4, meter.getLastKnownProject());
        contentValues.put(COL5, Long.toString(meter.getLastConnectionDate()));
        contentValues.put(COL6, Boolean.toString(meter.getRecordingStatus()));
        contentValues.put(COL7, Long.toString(meter.getStartRecordingDate()));

        long result = sqLiteDatabase.insert(TABLE_METER, null, contentValues);
        //sqLiteDatabase.close();
        //return result;
        return (result == -1);
    }
    public Cursor showData(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor data = sqLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_METER, null);
        return data;
    }


    public Integer deleteMeterData(String SensorName){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.delete(TABLE_METER, "SENSOR_NAME = ?", new String[] {SensorName});

    }

    public int updateMeterRecord(Meter meter){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, meter.getSensorName());
        contentValues.put(COL2, meter.getMacAddress());
        contentValues.put(COL3, meter.getLocation());
        contentValues.put(COL4, meter.getLastKnownProject());
        contentValues.put(COL5, Long.toString(meter.getLastConnectionDate()));
        contentValues.put(COL6, Boolean.toString(meter.getRecordingStatus()));
        contentValues.put(COL7, Long.toString(meter.getStartRecordingDate()));

        //long result1 = sqLiteDatabase.update(TABLE_METER,contentValues,"SENSOR_NAME = ?", new String[] {String.valueOf(meter.getSensorName())});
        return sqLiteDatabase.update(TABLE_METER, contentValues,  "ID = ?", new String[] { Integer.toString(meter.getSensorId()) });

    }

    public ArrayList<Meter> getAllMeterRecord(){
        ArrayList<Meter> meterRecordList = new ArrayList<Meter>();

        String selectQuery = "SELECT * FROM " + TABLE_METER;

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            try {
                do {
                    Meter meter;
                    meter = new Meter(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), Long.parseLong(cursor.getString(5)), Boolean.parseBoolean(cursor.getString(6)), Long.parseLong(cursor.getString(7)));
                    meter.setSensorId(Integer.parseInt(cursor.getString(0)));

                    meterRecordList.add(meter);
                } while (cursor.moveToNext());
            } catch (Exception e) {
                Log.e(TAG,"Couldn't read from Meter Table: " + e);
            } finally {
                cursor.close();
            }
        }
        return meterRecordList;
    }


    public Meter getSelectedMeterRecord(int ID){

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_METER, COLUMNS, "ID = ?",
                new String[] { Integer.toString(ID)}, null, null, null, null);
        //Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_METER + "WHERE " + SENSOR_NAME + "&&" + MAC_ADDRESS + "=?", new String[]{});
        Meter meter = null;
        if (cursor.moveToFirst()) {
            try {
                meter = new Meter(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), Long.parseLong(cursor.getString(5)), Boolean.parseBoolean(cursor.getString(6)), Long.parseLong(cursor.getString(7)));
                meter.setSensorId(Integer.parseInt(cursor.getString(0)));
            } catch (Exception e) {
                Log.e(TAG,"Couldn't read from Meter Table: " + e);
                meter = null;
            } finally {
                cursor.close();
            }
        }

        return meter;
    }
    public Meter getSelectedMeterRecord(String macAddress){

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_METER, COLUMNS, "MAC_ADDRESS = ?",
                new String[] { macAddress }, null, null, null, null);
        //Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_METER + "WHERE " + SENSOR_NAME + "&&" + MAC_ADDRESS + "=?", new String[]{});

        Meter meter = null;
        if (cursor.moveToFirst()){
            try {
                meter = new Meter(cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),Long.parseLong(cursor.getString(5)),Boolean.parseBoolean(cursor.getString(6)),Long.parseLong(cursor.getString(7)));
                meter.setSensorId(Integer.parseInt(cursor.getString(0)));
            } catch (Exception e) {
                Log.e(TAG,"Couldn't read from Meter Table: " + e);
                meter = null;
            } finally {
                cursor.close();
            }
        }

        return meter;
    }
}


/*
    public void addMeterData(Meter meter){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, meter.getSensorName());
        contentValues.put(COL2, meter.getMacAddress());
        contentValues.put(COL3, meter.getLocation());
        contentValues.put(COL4, meter.getLastKnownProject());
        contentValues.put(COL5, meter.getLastConnectionDate());
        contentValues.put(COL6, meter.getRecordingStatus());
        contentValues.put(COL7, meter.getStartRecordingDate());

        long result = sqLiteDatabase.insert(TABLE_METER, null, contentValues);
        sqLiteDatabase.close();
    }

    // Read meter record
    public ArrayList<Meter> getAllMeterRecord(){
        ArrayList<Meter> meterRecordList = new ArrayList<Meter>();

        String selectQuery = "SELECT * FROM " + TABLE_METER;

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do{
                Meter meter = new Meter();
                meter.setSensorName(cursor.getString(1));
                meter.setMacAddress(cursor.getString(2));
                meter.setLocation(cursor.getString(3));
                meter.setLastKnownProject(cursor.getString(4));
                meter.setLastConnectionDate(cursor.getLong(5));
                meter.setRecordingStatus(cursor.getInt(6) > 0);
                meter.setStartRecordingDate(cursor.getLong(7));

                meterRecordList.add(meter);
            }   while (cursor.moveToNext());
        }
        return meterRecordList;
    }

/*  public Meter getSelectedMeterRecord(String sensorName){

      SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

      Cursor cursor = sqLiteDatabase.query(TABLE_METER, COLUMNS, " sensorName = ?",
              new String[] { String.valueOf(sensorName)}, null, null, null, null);
      Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_METER + "WHERE " + SENSOR_NAME + "&&" + MAC_ADDRESS + "=?", new String[]{});

      if (cursor != null)
          cursor.moveToFirst();

      Meter meter = new Meter(cursor.getString(1),cursor.getString(2));
      meter.setSensorName(cursor.getString(1));
      meter.setMacAddress(cursor.getString(2));
      meter.setLocation(cursor.getString(3));
      meter.setLastKnownProject(cursor.getString(4));
      meter.setLastConnectionDate(cursor.getLong(5));
      meter.setRecordingStatus(cursor.getInt(6) > 0);
      meter.setStartRecordingDate(cursor.getLong(7));

      return meter;
    }

    public Meter getSelectedMeterRecord(int sensorID){

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_METER, COLUMNS, " ID = ?", new String[] { Integer.toString(sensorID)}, null, null, null, null);
        Meter meter = new Meter();

        if (cursor != null && cursor.moveToFirst()){
            meter.setSensorId(cursor.getInt(0));
            meter.setSensorName(cursor.getString(1));
            meter.setMacAddress(cursor.getString(2));
            meter.setLocation(cursor.getString(3));
            meter.setLastKnownProject(cursor.getString(4));
            meter.setLastConnectionDate(cursor.getLong(5));
            meter.setRecordingStatus(cursor.getInt(6) > 0);
            meter.setStartRecordingDate(cursor.getLong(7));
        }

        return meter;
    }

    public int updateMeterRecord(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, meter.getSensorName());
        contentValues.put(COL2, meter.getMacAddress());
        contentValues.put(COL3, meter.getLocation());
        contentValues.put(COL4, meter.getLastKnownProject());
        contentValues.put(COL5, meter.getLastConnectionDate());
        contentValues.put(COL6, meter.getRecordingStatus());
        contentValues.put(COL7, meter.getStartRecordingDate());

        return sqLiteDatabase.update(TABLE_METER, contentValues, COL0 + " = ?",
                new String[] { String.valueOf(meter.getSensorId()) });
    }


    public void deleteMeterRecord(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_METER, COL0 + " =?",
                new String[] {String.valueOf(meter.getSensorId())});
        sqLiteDatabase.close();
    }*/

    /*    public ArrayList<Meter> getSelectedMeterInfo(string sensorName, String macAddress){
        ArrayList<Meter> selectedMeterRecordList = new ArrayList<Meter>();

        String[] columns = {ID, SENSOR_NAME, MAC_ADDRESS, LOCATION, LAST_KNOWN_PROJECT, LAST_CONNECTION_DATE, RECORDING_STATUS, START_RECORDING_DATA};
        String selection = SENSOR_NAME + MAC_ADDRESS + " =?";
        String[] selectionArgs = {String.valueOf(sensorName)};

        Cursor cursor =

        //SelectMeter selectMeter(String sensorName, String macAddress){

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
*/
