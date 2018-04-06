package ca.concordia.teamc.soundlevelapp;

/**
 * Created by Thilak on 2018-03-22.
 */

public class DataSet {

    int datSetId;
    String projectName;
    String location;
    int dateOfDownload;
    int dateStartRecord;
    String meterReferenceRecord;
    String datafile;

/*
    public DataSet(int datSetId, String projectName, String location, int dateOfDownload, int dateStartRecord, String meterReferenceRecord, String datafile) {
        this.datSetId = datSetId;
        this.projectName = projectName;
        this.location = location;
        this.dateOfDownload = dateOfDownload;
        this.dateStartRecord = dateStartRecord;
        this.meterReferenceRecord = meterReferenceRecord;
        this.datafile = datafile;
    }




    public int getDatSetId() {
        return datSetId;
    }

    public void setDatSetId(int datSetId) {
        this.datSetId = datSetId;
    }

    */

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

    public int getDateOfDownload() {
        return dateOfDownload;
    }

    public void setDateOfDownload(int dateOfDownload) {
        this.dateOfDownload = dateOfDownload;
    }

    public int getDateStartRecord() {
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
