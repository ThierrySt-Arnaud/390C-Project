package ca.concordia.teamc.soundlevelapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by Thilak on 2018-03-23.
 */

public class DataSetController extends SQLiteOpenHelper{

    private static final String TAG = "DatSetController";
    private DataSet dataSet;

    private static final String DATABASE_NAME = "SoundLevelApp.db";
    private static final String TABLE_DATASET = "DataSetTable";

    private static final String COL0 = "ID";
    private static final String COL1 = "PROJECT_NAME";
    private static final String COL2 = "LOCATION";
    private static final String COL3 = "DATE_OF_DOWNLOAD";
    private static final String COL4 = "DATE_START_RECORD";
    private static final String COL5 = "METER_REFERENCE_RECORD";
    private static final String COL6 = "DATA_FILE";

    private static final String[] COLUMNS = {COL0,COL1,COL2,COL3,COL4,COL5,COL6};

    DataSetController(Context context){
        super(context, DATABASE_NAME,null,1);
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
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_DATASET);
        onCreate(sqLiteDatabase);
    }


    public boolean addDataSet(DataSet dataSet){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, dataSet.getProjectName());
        contentValues.put(COL2, dataSet.getLocation());
        contentValues.put(COL3, Long.toString(dataSet.getDateOfDownload()));
        contentValues.put(COL4, Long.toString(dataSet.getDateStartRecord()));
        contentValues.put(COL5, dataSet.getMeterReferenceRecord());
        contentValues.put(COL6, dataSet.getDatafile());


        long result = sqLiteDatabase.insert(TABLE_DATASET, null, contentValues);
        //sqLiteDatabase.close();
        //return result;
        return result == -1;
    }


    public Integer deleteDataSet(String ProjectName){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        return sqLiteDatabase.delete(TABLE_DATASET, "PROJECT_NAME = ?", new String[] {ProjectName});

    }

    public int updateDataSet(DataSet dataSet){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, dataSet.getProjectName());
        contentValues.put(COL2, dataSet.getLocation());
        contentValues.put(COL3, dataSet.getDateOfDownload());
        contentValues.put(COL4, dataSet.getDateStartRecord());
        contentValues.put(COL5, dataSet.getMeterReferenceRecord());
        contentValues.put(COL6, dataSet.getDatafile());

        long result1 = sqLiteDatabase.update(TABLE_DATASET,contentValues,"PROJECT_NAME = ?", new String[] {String.valueOf(dataSet.getProjectName())});
        return sqLiteDatabase.update(TABLE_DATASET, contentValues,  "PROJECT_NAME = ?", new String[] { String.valueOf(dataSet.getProjectName()) });

    }

    public ArrayList<DataSet> getAllDataSet(){
        ArrayList<DataSet> dataSetArrayList = new ArrayList<DataSet>();

        String selectQuery = "SELECT * FROM " + TABLE_DATASET;

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do{
                DataSet dataSet;
                dataSet = new DataSet();
                dataSet.setDataSetID(Integer.parseInt(cursor.getString(0)));
                dataSet.setProjectName(cursor.getString(1));
                dataSet.setLocation(cursor.getString(2));
                dataSet.setDateOfDownload(Long.parseLong(cursor.getString(3)));
                dataSet.setDateStartRecord(Long.parseLong(cursor.getString(4)));
                dataSet.setMeterReferenceRecord(cursor.getString(5));
                dataSet.setDatafile(cursor.getString(6));

                dataSetArrayList.add(dataSet);
            }   while (cursor.moveToNext());
        }
        return dataSetArrayList;
    }


    public DataSet getSelectedDataSet(int ID){

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_DATASET, COLUMNS, "ID = ?",
                new String[] {Integer.toString(ID)}, null, null, null, null);
        //Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_METER + "WHERE " + SENSOR_NAME + "&&" + MAC_ADDRESS + "=?", new String[]{});

        if (cursor != null)
            cursor.moveToFirst();

        DataSet dataSet = new DataSet();
        dataSet.setDataSetID(Integer.parseInt(cursor.getString(0)));
        dataSet.setProjectName(cursor.getString(1));
        dataSet.setLocation(cursor.getString(2));
        dataSet.setDateOfDownload(Long.parseLong(cursor.getString(3)));
        dataSet.setDateStartRecord(Long.parseLong(cursor.getString(4)));
        dataSet.setMeterReferenceRecord(cursor.getString(5));
        dataSet.setDatafile((cursor.getString(6)));

        return dataSet;
    }

}


/*
    public void addMeterData(DataSet dataSet){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, dataSet.getProjectName());
        contentValues.put(COL2, dataSet.getLocation());
        contentValues.put(COL3, dataSet.getDateOfDownload());
        contentValues.put(COL4, dataSet.getDateStartRecord());
        contentValues.put(COL5, dataSet.getMeterReferenceRecord());
        contentValues.put(COL6, dataSet.getDatafile());

        long result = sqLiteDatabase.insert(TABLE_DATASET, null, contentValues);
        sqLiteDatabase.close();
    }

    // Read meter record
    public ArrayList<DataSet> getAllDataSetRecord(){
        ArrayList<DataSet> dataSetRecords = new ArrayList<DataSet>();

        String selectQuery = "SELECT * FROM " + TABLE_DATASET;

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do{
                DataSet dataSet = new DataSet();
                dataSet.setDataSetID(Integer.parseInt(cursor.getString(0)));
                dataSet.setProjectName(cursor.getString(1));
                dataSet.setLocation(cursor.getString(2));
                dataSet.setDateOfDownload(cursor.getLong(3));
                dataSet.setDateStartRecord(cursor.getLong(4));
                dataSet.setMeterReferenceRecord(cursor.getString(5));
                dataSet.setDatafile(cursor.getString(6));

                dataSetRecords.add(dataSet);
            }   while (cursor.moveToNext());
        }
        return dataSetRecords;
    }

    public DataSet getSelectedDataSetRecord(String projectName){

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_DATASET, COLUMNS, " projectName = ?",
                new String[] { String.valueOf(projectName)}, null, null, null, null);
        //Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_METER + "WHERE " + SENSOR_NAME + "&&" + MAC_ADDRESS + "=?", new String[]{});

        if (cursor != null)
            cursor.moveToFirst();

        DataSet dataSet = new DataSet();
        dataSet.setDataSetID(Integer.parseInt(cursor.getString(0)));
        dataSet.setProjectName(cursor.getString(1));
        dataSet.setLocation(cursor.getString(2));
        dataSet.setDateOfDownload(cursor.getLong(3));
        dataSet.setDateStartRecord(cursor.getLong(4));
        dataSet.setMeterReferenceRecord(cursor.getString(5));
        dataSet.setDatafile(cursor.getString(6));

        return dataSet;
    }

    public DataSet getSelectedDataSetRecord(int ID){

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_DATASET, COLUMNS, " ID = ?", new String[] { Integer.toString(ID)}, null, null, null, null);
        //Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_METER + "WHERE " + SENSOR_NAME + "&&" + MAC_ADDRESS + "=?", new String[]{});
        DataSet dataSet = new DataSet();

        if (cursor != null && cursor.moveToFirst()){
            dataSet.setDatSetId(Integer.parseInt(cursor.getString(0)));
            dataSet.setProjectName(cursor.getString(1));
            dataSet.setLocation(cursor.getString(2));
            dataSet.setDateOfDownload(cursor.getLong(3));
            dataSet.setDateStartRecord(cursor.getLong(4));
            dataSet.setMeterReferenceRecord(cursor.getString(5));
            dataSet.setDatafile(cursor.getString(6));
        }

        return dataSet;
    }

    public int updateDataSetRecord(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, dataSet.getProjectName());
        contentValues.put(COL2, dataSet.getLocation());
        contentValues.put(COL3, dataSet.getDateOfDownload());
        contentValues.put(COL4, dataSet.getDateStartRecord());
        contentValues.put(COL5, dataSet.getMeterReferenceRecord());
        contentValues.put(COL6, dataSet.getDatafile());

        return sqLiteDatabase.update(TABLE_DATASET, contentValues, COL0 + " = ?",
                new String[] { String.valueOf(dataSet.getDataSetID()) });
    }


    public void deleteDatasetRecord(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_DATASET, COL0 + " =?",
                new String[] {String.valueOf(dataSet.getDataSetID())});
        sqLiteDatabase.close();
    }
*/
