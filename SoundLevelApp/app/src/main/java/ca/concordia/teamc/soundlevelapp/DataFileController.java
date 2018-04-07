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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dinaalyousef on 2018-03-25.
 */

public class DataFileController extends SQLiteOpenHelper {
    private Context context;
    private DataFile dataFile = null;

    private static final String DATABASE_NAME = "SoundLevelApp.db";
    private static final String TABLE_DATAFILE = "DataFileTable";

    private static final String COL0 = "ID";
    private static final String COL1 = "PROJECT_NAME";
    private static final String COL2 = "LOCATION";
    private static final String COL3 = "DATA";

    private static final String[] COLUMNS = {COL0,COL1,COL2,COL3};

    public DataFileController(Context context, String pName, String pLocation, byte[] data){
        super(context, DATABASE_NAME,null,1);
        this.context = context;
        this.dataFile = new DataFile(context,pName,pLocation,data);
        writeFile();
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

    public void addDataFile(){
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

    public int updateDataFileRecord(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(COL1, dataFile.getProjectName());
        contentValues.put(COL2, dataFile.getProjectLocation());
        contentValues.put(COL3, dataFile.getData().toString());

        return sqLiteDatabase.update(TABLE_DATAFILE, contentValues, COL0 + " = ?",
                new String[] { String.valueOf(dataFile.getId()) });
    }


    public void deleteDatasetRecord(){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABLE_DATAFILE, COL0 + " =?",
                new String[] {String.valueOf(dataFile.getId())});
        sqLiteDatabase.close();
    }

    public void writeFile(){
        String fileString = dataFile.getProjectName();

        for (int i =0; i<dataFile.getData().length;i++){
            fileString += Byte.toString(dataFile.getData()[i]);
            if ( i != dataFile.getData().length-1){
                fileString += ",";
            }
        }

        Log.d("DataFileController", "File= "+ fileString);

        dataFile.setFile(new File(context.getFilesDir(), Long.toString(System.currentTimeMillis())));
        File file = dataFile.getFile();
        Log.d("DataFileController", "File Path: " + file.getPath());
        Log.d("DataFileController", "File Path: " + file.getAbsolutePath());
        try{
            file.createNewFile();
            Log.d("DataFileController", "File created");
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(fileString);
            fileWriter.flush();
            fileWriter.close();
            Log.d("DataFileController", "File written");
        }catch (IOException e){
            Log.e("DataFileController", "Can not create/write file " + file.getPath(), e);
        }
    }

    public void writeFile(byte[] data){
        String fileString = dataFile.getProjectName()+",";

        for (int i =0; i<data.length;i++){
            fileString += Float.toString(data[i]);
            if ( i != data.length-1){
                fileString += ",";
            }
        }

        Log.d("DataFileController", "File= "+ fileString);

        dataFile.setFile(new File(context.getFilesDir(), Long.toString(System.currentTimeMillis())));
        File file = dataFile.getFile();

        Log.d("DataFileController", "File Path: " + file.getPath());
        Log.d("DataFileController", "File Path: " + file.getAbsolutePath());
        try{
            file.createNewFile();
            Log.d("DataFileController", "File created");
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(fileString);
            fileWriter.flush();
            fileWriter.close();
            Log.d("DataFileController", "File written");
        }catch (IOException e){
            Log.e("DataFileController", "Can not create/write file " + file.getPath(), e);
        }
    }

    public String readFile(){
        File file = dataFile.getFile();
        try{
            char[] cbuf = new char[1024];
            FileReader fileReader = new FileReader(file);
            int charRead = fileReader.read(cbuf, 0, 1024);
            String readFile = new String(cbuf,0,charRead);

            return readFile;
        }catch (FileNotFoundException e){
            Log.e("DataFileController", "Can not read file " + file.getPath(), e);
        }catch (IOException e){
            Log.e("DataFileController", "Can not create/write file " + file.getPath(), e);
        }

        return "ERROR";
    }

    public boolean deleteFile(){
        File file = dataFile.getFile();

        if(file.delete()){
            Log.d("DataFileController", "File Deleted");
            return true;
        }else{
            Log.d("DataFileController", "File Not Deleted");
            return false;
        }
    }

    public static String readFile(File f){
        try{
            char[] cbuf = new char[1024];
            FileReader fileReader = new FileReader(f);
            int charRead = fileReader.read(cbuf, 0, 1024);
            String readFile = new String(cbuf,0,charRead);

            return readFile;
        }catch (FileNotFoundException e){
            Log.e("DataFileController", "Can not read file " + f.getPath(), e);
        }catch (IOException e){
            Log.e("DataFileController", "Can not create/write file " + f.getPath(), e);
        }

        return "ERROR";
    }

    public static String readFile(String path){
        try{
            File f = new File(path);
            char[] cbuf = new char[1024];
            FileReader fileReader = new FileReader(f);
            int charRead = fileReader.read(cbuf, 0, 1024);
            String readFile = new String(cbuf,0,charRead);

            return readFile;
        }catch (FileNotFoundException e){
            Log.e("DataFileController", "Can not read file ");
        }catch (IOException e){
            Log.e("DataFileController", "Can not create/write file ");
        }

        return "ERROR";
    }
}
