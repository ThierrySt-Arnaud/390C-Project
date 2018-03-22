package ca.concordia.teamc.soundlevelapp;

/**
 * Created by Thilak on 2018-03-22.
 */

public class Metergetset {

    String sensorName;
    String macAddress;
    String location;
    String lastKnownProject;
    String lastConnectionDate;
    Boolean recordingStatus;
    String startRecordingDate;

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

    public String getLastConnectionDate() {
        return lastConnectionDate;
    }

    public void setLastConnectionDate(String lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
    }

    public Boolean getRecordingStatus() {
        return recordingStatus;
    }

    public void setRecordingStatus(Boolean recordingStatus) {
        this.recordingStatus = recordingStatus;
    }

    public String getStartRecordingDate() {
        return startRecordingDate;
    }

    public void setStartRecordingDate(String startRecordingDate) {
        this.startRecordingDate = startRecordingDate;
    }
}
