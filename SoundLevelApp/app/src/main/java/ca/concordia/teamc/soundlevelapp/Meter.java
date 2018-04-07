package ca.concordia.teamc.soundlevelapp;

/**
 * Created by Thilak on 2018-03-22.
 */

public class Meter {

    int sensorId;
    String sensorName;
    String macAddress;
    String location;
    String lastKnownProject;
    String lastConnectionDate;
    boolean recordingStatus;
    String startRecordingDate;

    public Meter() {
    }

    public Meter(String sensorName, String macAddress, String location, String lastKnownProject, String lastConnectionDate, boolean recordingStatus, String startRecordingDate) {
        this.sensorName = sensorName;
        this.macAddress = macAddress;
        this.location = location;
        this.lastKnownProject = lastKnownProject;
        this.lastConnectionDate = lastConnectionDate;
        this.recordingStatus = recordingStatus;
        this.startRecordingDate = startRecordingDate;
    }

    public int getSensorId(){
        return sensorId;
    }

    public void setSensorId(int sensorId) {
        this.sensorId = sensorId;
    }


    public String getSensorName() {
        return sensorName;
    }

    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getLastKnownProject() {
        return lastKnownProject;
    }

    public void setLastKnownProject(String lastKnownProject) {
        this.lastKnownProject = lastKnownProject;
    }

    public long getLastConnectionDate() {
        return lastConnectionDate;
    }

    public void setLastConnectionDate(long lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
    }

    public boolean getRecordingStatus() {
        return recordingStatus;
    }

    public void setRecordingStatus(boolean recordingStatus) {
        this.recordingStatus = recordingStatus;
    }

    public long getStartRecordingDate() {
        return startRecordingDate;
    }

    public void setStartRecordingDate(long startRecordingDate) {
        this.startRecordingDate = startRecordingDate;
    }
}
