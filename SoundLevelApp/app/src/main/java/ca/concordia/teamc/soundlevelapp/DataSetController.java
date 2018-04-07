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

    private static final String DATABASE_NAME = "SoundLevelApp.db";
    private static final String TABLE_DATASET = "DataSetTable";

    private static final String COL0 = "ID";
    private static final String COL1 = "PROJECT_NAME";
    private static final String COL2 = "LOCATION";
    private static final String COL3 = "DATE_OF_DOWNLOAD";
    private static final String COL4 = "DATE_START_RECORD";
    private static final String COL5 = "METER_REFERENCE_RECORD";
    private static final String COL6 = "DATA_FILE";

    private static final String[] COLUMNS = {COL0,COL1,COL2,COL3,COL4,COL5};

    public DataSetController(Context context){
        super(context, DATABASE_NAME,null,1);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_DATASET + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " PROJECT_NAME TEXT, LOCATION TEXT, DATE_OF_DOWNLOAD TEXT, DATE_START_RECORD TEXT, METER_REFERENCE_RECORD TEXT, DATA_FILE TEXT)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP IF TABLE EXISTS "+ TABLE_DATASET);
        onCreate(sqLiteDatabase);
    }

    public boolean addDataSet(DataSet dataSet){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, dataSet.getProjectName());
        contentValues.put(COL2, dataSet.getLocation());
        contentValues.put(COL3, dataSet.getDateOfDownload());
        contentValues.put(COL4, dataSet.getDateStartRecord());
        contentValues.put(COL5, dataSet.getMeterReferenceRecord());
        contentValues.put(COL6, dataSet.getDatafile());


        long result = sqLiteDatabase.insert(TABLE_DATASET, null, contentValues);
        //sqLiteDatabase.close();
        //return result;
        if(result == -1) {
            return false;
        }else{
            return true;
        }
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
                dataSet.setProjectName(cursor.getString(1));
                dataSet.setLocation(cursor.getString(2));
                dataSet.setDateOfDownload(Integer.parseInt(cursor.getString(3)));
                dataSet.setDateStartRecord(Integer.parseInt(cursor.getString(4)));
                dataSet.setMeterReferenceRecord(cursor.getString(5));
                dataSet.setDatafile(cursor.getString(6));

                dataSetArrayList.add(dataSet);
            }   while (cursor.moveToNext());
        }
        return dataSetArrayList;
    }


    public DataSet getSelectedDataSet(String projectName){

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_DATASET, COLUMNS, "PROJECT_NAME = ?",
                new String[] { String.valueOf(projectName)}, null, null, null, null);
        //Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM "+ TABLE_METER + "WHERE " + SENSOR_NAME + "&&" + MAC_ADDRESS + "=?", new String[]{});

        if (cursor != null)
            cursor.moveToFirst();

        DataSet dataSet = new DataSet();
        // meter.setSensorId(Integer.parseInt(cursor.getString(0)));
        dataSet.setProjectName(cursor.getString(1));
        dataSet.setLocation(cursor.getString(2));
        dataSet.setDateOfDownload(Integer.parseInt(cursor.getString(3)));
        dataSet.setDateStartRecord(Integer.parseInt(cursor.getString(4)));
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
                dataSet.setDatSetId(Integer.parseInt(cursor.getString(0)));
                dataSet.setProjectName(cursor.getString(1));
                dataSet.setLocation(cursor.getString(2));
                dataSet.setDateOfDownload(Integer.parseInt(cursor.getString(3)));
                dataSet.setDateStartRecord(Integer.parseInt(cursor.getString(4)));
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
        dataSet.setDatSetId(Integer.parseInt(cursor.getString(0)));
        dataSet.setProjectName(cursor.getString(1));
        dataSet.setLocation(cursor.getString(2));
        dataSet.setDateOfDownload(Integer.parseInt(cursor.getString(3)));
        dataSet.setDateStartRecord(Integer.parseInt(cursor.getString(4)));
        dataSet.setMeterReferenceRecord(cursor.getString(5));
        dataSet.setDatafile(cursor.getString(6));

        return dataSet;
    }

    public int updateDataSetRecord(DataSet dataSet){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, dataSet.getProjectName());
        contentValues.put(COL2, dataSet.getLocation());
        contentValues.put(COL3, dataSet.getDateOfDownload());
        contentValues.put(COL4, dataSet.getDateStartRecord());
        contentValues.put(COL5, dataSet.getMeterReferenceRecord());
        contentValues.put(COL6, dataSet.getDatafile());

        return sqLiteDatabase.update(TABLE_DATASET, contentValues, COL0 + " = ?",
                new String[] { String.valueOf(dataSet.getDatSetId()) });
    }


    public void deleteDatasetRecord(DataSet dataSet){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_DATASET, COL0 + " =?",
                new String[] {String.valueOf(dataSet.getDatSetId())});
        sqLiteDatabase.close();
    }
*/


