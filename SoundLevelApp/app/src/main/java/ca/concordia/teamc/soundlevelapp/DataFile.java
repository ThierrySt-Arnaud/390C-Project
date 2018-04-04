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
    File file;
    int id;
    String projectName;
    String projectLocation;
    byte[] data;

    public DataFile(Context context, File file, String pName, String pLocation,byte[] data){
        this.context = context;
        this.projectName = pName;
        this.projectLocation = pLocation;
        this.file = file;
        this.data = data;
    }

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
    }
}
