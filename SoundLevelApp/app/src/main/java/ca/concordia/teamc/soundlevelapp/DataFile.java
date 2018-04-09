package ca.concordia.teamc.soundlevelapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Thierry St-Arnaud on 04-27-2018.
 */

public class DataFile {
    String fileName;
    File file;
    private static final String TAG = "DataFile";

    public DataFile(Context context){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyMMddHHmmssZ");
        Date currentDate = new Date(System.currentTimeMillis());
        fileName = (simpleDateFormat.format(currentDate)+".bin");
        File newFile = new File(context.getFilesDir(), fileName);
        this.file = newFile;
    }

    public DataFile(Context context, String oldFileName){
        this.fileName = oldFileName;
        this.file = new File(context.getFilesDir(), oldFileName);
    }

    public void writeToFile(byte[] byteData){
        try{
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file, true));
            bos.write(byteData);
            bos.close();
        } catch (IOException e) {
            Log.e(TAG,"Couldn't read from " + fileName + ": ", e);
        }
    }

    public String getFileName() {
        return fileName;
    }

    public long getSize(){
        return file.length();
    }

    public byte[] getData() {
        byte[] data = new byte[(int) file.length()];
        try{
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
            bis.read(data);
            bis.close();
        } catch (IOException e) {
            Log.e(TAG,"Couldn't read from " + fileName + ": ", e);
            return null;
        }
        return data;
    }
}
