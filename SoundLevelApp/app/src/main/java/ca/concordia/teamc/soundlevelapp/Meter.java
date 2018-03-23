package ca.concordia.teamc.soundlevelapp;

//Item class for BaseAdapter

public class Meter {
    private String itemName;
    private String itemDescription;

    public Meter (String name, String description) {
        this.itemName = name;
        this.itemDescription = description;
    }

    public String getItemName() {
        return this.itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }
}
