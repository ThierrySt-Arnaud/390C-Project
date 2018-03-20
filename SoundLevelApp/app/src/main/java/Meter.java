/**
 * Created by thierry on 2018-03-20.
 */

public class Meter {
    String sensorName;
    String macAddress;
    String location;
    String lastKnownProject;
    String lastConnectionDate;
    int recordingStatus;
    String recordingStatusChangeDate;

    public Meter(String sensorName, String macAddress, String location, String lastKnownProject, String lastConnectionDate, int recordingStatus, String recordingStatusChangeDate) {
        this.sensorName = sensorName;
        this.macAddress = macAddress;
        this.location = location;
        this.lastKnownProject = lastKnownProject;
        this.lastConnectionDate = lastConnectionDate;
        this.recordingStatus = recordingStatus;
        this.recordingStatusChangeDate = recordingStatusChangeDate;
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

    public String getLastConnectionDate() {
        return lastConnectionDate;
    }

    public void setLastConnectionDate(String lastConnectionDate) {
        this.lastConnectionDate = lastConnectionDate;
    }

    public int getRecordingStatus() {
        return recordingStatus;
    }

    public void setRecordingStatus(int recordingStatus) {
        this.recordingStatus = recordingStatus;
    }

    public String getRecordingStatusChangeDate() {
        return recordingStatusChangeDate;
    }

    public void setRecordingStatusChangeDate(String recordingStatusChangeDate) {
        this.recordingStatusChangeDate = recordingStatusChangeDate;
    }
}
