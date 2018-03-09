package ca.concordia.teamc.soundlevelapp;


public class Profile {
    String Name;
    String Project;
    String Location;

    protected void setName(String name) {
        Name = name;
    }
    protected void setProject(String project){
        Project = project;
    }

    protected void setLocation(String location){
        Location = location;
    }

    protected String getName(){
        return Name;
    }

    protected String getProject(){

        return Project;
    }

    protected String getLocation(){

        return Location;
    }
}
