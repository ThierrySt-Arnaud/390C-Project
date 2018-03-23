package ca.concordia.teamc.soundlevelapp;

//Item class for Adapter

public class Data {
    private String itemName;
    private String itemLocation;
    private String itemDateStarted;
    private String itemDateDownloaded;

    public Data (String name, String location, String datestarted, String datedownloaded) {
        this.itemName = name;
        this.itemLocation = location;
        this.itemDateStarted = datestarted;
        this.itemDateDownloaded = datedownloaded;
    }

    public String getItemName() {
        return this.itemName;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public String getItemDateStarted() {return itemDateStarted;}

    public String getItemDateDownloaded() {return itemDateDownloaded; }

}



