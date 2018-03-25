package ca.concordia.teamc.soundlevelapp;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by blux1 on 2018-03-25.
 */

public class DataFileController {
    DataFile dataFile;

    public DataFileController(Context context, DataSet ds, File file, float[] data){
        dataFile = new DataFile(context,ds,file,data);
    }

    public DataFileController(DataFile df){
        dataFile = df;
    }

    public DataFile createFile(){
        return dataFile.writeFile();
    }

    public DataFile updateFile(float[] data){
        return dataFile.writeFile(data);
    }

    public String readFile(){
        return dataFile.readFile();
    }

    public static String readFile(File f){
        return DataFile.readFile(f);
    }

    public boolean deleteFile(){
        return dataFile.deleteFile();
    }
}
