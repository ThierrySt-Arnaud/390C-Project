package ca.concordia.teamc.soundlevelapp;

/**
 * Created by Thilak on 2018-03-22.
 */

public class DataSet {

    int datSetId;
    String projectName;
    String location;
    long dateOfDownload;
    long dateStartRecord;
    String meterReferenceRecord;
    String datafile;

    public DataSet(String projectName, String location, long dateOfDownload, long dateStartRecord, String meterReferenceRecord, String datafile) {
        this.projectName = projectName;
        this.location = location;
        this.dateOfDownload = dateOfDownload;
        this.dateStartRecord = dateStartRecord;
        this.meterReferenceRecord = meterReferenceRecord;
        this.datafile = datafile;

    }

    public DataSet(){

    }

    public int getDatSetId() {
        return datSetId;
    }

    public void setDatSetId(int datSetId) {
        this.datSetId = datSetId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getDateOfDownload() {
        return dateOfDownload;
    }

    public void setDateOfDownload(int dateOfDownload) {
        this.dateOfDownload = dateOfDownload;
    }

    public long getDateStartRecord() {
        return dateStartRecord;
    }

    public void setDateStartRecord(int dateStartRecord) {
        this.dateStartRecord = dateStartRecord;
    }

    public String getMeterReferenceRecord() {
        return meterReferenceRecord;
    }

    public void setMeterReferenceRecord(String meterReferenceRecord) {
        this.meterReferenceRecord = meterReferenceRecord;
    }

    public String getDatafile() {
        return datafile;
    }

    public void setDatafile(String datafile) {
        this.datafile = datafile;
    }
}
