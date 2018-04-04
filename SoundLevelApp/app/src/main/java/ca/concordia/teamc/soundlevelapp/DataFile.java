package ca.concordia.teamc.soundlevelapp;

import android.content.Context;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by dinaalyousef on 2018-03-24.
 */

public class DataFile {

    private Context context;
<<<<<<< HEAD
    File file;
    int id;
    String projectName;
    String projectLocation;
    byte[] data;

    public DataFile(Context context, File file, String pName, String pLocation,byte[] data){
        this.context = context;
        this.projectName = pName;
        this.projectLocation = pLocation;
=======
    DataSet dataset;
    File file;
    float[] data;

    public DataFile(Context context, DataSet dataset, File file, float[] data){
        this.context = context;
        this.dataset = dataset;
>>>>>>> bluetooth-fix
        this.file = file;
        this.data = data;
    }

<<<<<<< HEAD
    public DataFile(Context context,int id, String pName, String pLocation, byte[] data){
        this.context = context;
        this.id = id;
        this.projectName = pName;
        this.projectLocation = pLocation;
        this.data = data;
    }

    public DataFile(Context context, String pName, String pLocation, byte[] data){
        this.context = context;
        this.projectName = pName;
        this.projectLocation = pLocation;
        this.data = data;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectLocation() {
        return projectLocation;
    }

    public void setProjectLocation(String projectLocation) {
        this.projectLocation = projectLocation;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
=======
    public DataFile(Context context, DataSet dataset, float[] data){
        this.context = context;
        this.dataset = dataset;
        this.data = data;

        writeFile();

    }

    public DataFile writeFile(){
        String csvFileString = dataset.getProjectName()+",";

        for (int i =0; i<data.length;i++){
            csvFileString += Float.toString(data[i]);
            if ( i != data.length-1){
                csvFileString += ",";
            }
        }

        Log.d("DataFile", "CSV File= "+ csvFileString);

        //file = context.openFileOutput(dataset.getProjectName()+".csv", Context.MODE_PRIVATE);
        file = new File(context.getFilesDir(),dataset.getProjectName()+".csv");
        Log.d("DataFile", "File Path: " + file.getPath());
        Log.d("DataFile", "File Path: " + file.getAbsolutePath());
        try{
            file.createNewFile();
            Log.d("DataFile", "CSV File created");
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(csvFileString);
            fileWriter.flush();
            fileWriter.close();
            Log.d("DataFile", "File written");
        }catch (IOException e){
            Log.e("DataFile", "Can not create/write file " + file.getPath(), e);
        }

        return this;
    }

    public DataFile writeFile(float[] data){
        String csvFileString = dataset.getProjectName()+",";

        for (int i =0; i<data.length;i++){
            csvFileString += Float.toString(data[i]);
            if ( i != data.length-1){
                csvFileString += ",";
            }
        }

        Log.d("DataFile", "CSV File= "+ csvFileString);

        //file = context.openFileOutput(dataset.getProjectName()+".csv", Context.MODE_PRIVATE);
        file = new File(context.getFilesDir(),dataset.getProjectName()+".csv");
        Log.d("DataFile", "File Path: " + file.getPath());
        Log.d("DataFile", "File Path: " + file.getAbsolutePath());
        try{
            file.createNewFile();
            Log.d("DataFile", "CSV File created");
            FileWriter fileWriter = new FileWriter(file, false);
            fileWriter.write(csvFileString);
            fileWriter.flush();
            fileWriter.close();
            Log.d("DataFile", "File written");
        }catch (IOException e){
            Log.e("DataFile", "Can not create/write file " + file.getPath(), e);
        }

        return this;
    }

    public String readFile(){
        try{
            char[] cbuf = new char[1024];
            FileReader fileReader = new FileReader(file);
            int charRead = fileReader.read(cbuf, 0, 1024);
            String readFile = new String(cbuf,0,charRead);

            return readFile;
        }catch (FileNotFoundException e){
            Log.e("DataFile", "Can not read file " + file.getPath(), e);
        }catch (IOException e){
            Log.e("DataFile", "Can not create/write file " + file.getPath(), e);
        }

        return "ERROR";
    }

    public boolean deleteFile(){
        if(file.delete()){
            Log.d("DataFile", "File Deleted");
            return true;
        }else{
            Log.d("DataFile", "File Not Deleted");
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
            Log.e("DataFile", "Can not read file " + f.getPath(), e);
        }catch (IOException e){
            Log.e("DataFile", "Can not create/write file " + f.getPath(), e);
        }

        return "ERROR";
    }

    public DataSet getDataset() {
        return dataset;
    }

    public void setDataset(DataSet dataset) {
        this.dataset = dataset;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public float[] getData() {
        return data;
    }

    public void setData(float[] data) {
        this.data = data;
>>>>>>> bluetooth-fix
    }
}
