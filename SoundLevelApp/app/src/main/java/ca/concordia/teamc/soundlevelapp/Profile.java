package ca.concordia.teamc.soundlevelapp;


public class Profile {

    String Project;
    String Location;


    protected void setProject(String project){
        Project = project;
    }

    protected void setLocation(String location){
        Location = location;
    }



    protected String getProject(){

        return Project;
    }

    protected String getLocation(){

        return Location;
    }
}
