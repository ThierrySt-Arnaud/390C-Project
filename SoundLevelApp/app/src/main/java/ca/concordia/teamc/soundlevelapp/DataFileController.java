package ca.concordia.teamc.soundlevelapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dinaalyousef on 2018-03-25.
 */

public class DataFileController extends SQLiteOpenHelper {
    private Context context;
    private static final String DATABASE_NAME = "SoundLevelApp.db";
    private static final String TABLE_DATAFILE = "DataFileTable";

    private static final String COL0 = "ID";
    private static final String COL1 = "PROJECT_NAME";
    private static final String COL2 = "LOCATION";
    private static final String COL3 = "DATA";

    private static final String[] COLUMNS = {COL0,COL1,COL2,COL3};

    public DataFileController(Context context){
        super(context, DATABASE_NAME,null,1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTable = "CREATE TABLE " + TABLE_DATAFILE + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                " PROJECT_NAME TEXT, LOCATION TEXT, DATA TEXT)";
        sqLiteDatabase.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TABLE_DATAFILE);
        onCreate(sqLiteDatabase);
    }

    public void addDataFile(DataFile dataFile){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, dataFile.getProjectName());
        contentValues.put(COL2, dataFile.getProjectLocation());
        contentValues.put(COL3, dataFile.getData().toString());

        long result = sqLiteDatabase.insert(TABLE_DATAFILE, null, contentValues);
        sqLiteDatabase.close();
    }

    // Read datafile record
    public ArrayList<DataFile> getAllDataFileRecord(){
        ArrayList<DataFile> dataFileRecords = new ArrayList<DataFile>();

        String selectQuery = "SELECT * FROM " + TABLE_DATAFILE;

        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        Cursor cursor = sqLiteDatabase.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()){
            do{
                int id = Integer.parseInt(cursor.getString(0));
                String pName = cursor.getString(1);
                String pLocation = cursor.getString(2);
                byte[] data = cursor.getString(3).getBytes();
                DataFile dataFile = new DataFile(context, id, pName,pLocation,data);

                dataFileRecords.add(dataFile);
            }   while (cursor.moveToNext());
        }
        return dataFileRecords;
    }

    public DataFile getSelectedDataFileRecord(String projectName){

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(TABLE_DATAFILE, COLUMNS, " projectName = ?",
                new String[] { String.valueOf(projectName)}, null, null, null, null);

        if (cursor != null)
            cursor.moveToFirst();

        int id = Integer.parseInt(cursor.getString(0));
        String pName = cursor.getString(1);
        String pLocation = cursor.getString(2);
        byte[] data = cursor.getString(3).getBytes();

        DataFile dataFile = new DataFile(context, id, pName,pLocation,data);

        return dataFile;
    }

    public int updateDataFileRecord(DataFile dataFile){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, dataFile.getProjectName());
        contentValues.put(COL2, dataFile.getProjectLocation());
        contentValues.put(COL3, dataFile.getData().toString());

        return sqLiteDatabase.update(TABLE_DATAFILE, contentValues, COL0 + " = ?",
                new String[] { String.valueOf(dataFile.getId()) });
    }


    public void deleteDatasetRecord(DataFile dataFile){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_DATAFILE, COL0 + " =?",
                new String[] {String.valueOf(dataFile.getId())});
        sqLiteDatabase.close();
    }
}
