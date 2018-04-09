package ca.concordia.teamc.soundlevelapp;


public class Profile {

    String Project;
    String Location;
    String LastDate;


    protected void setProject(String project){
        Project = project;
    }

    protected void setLocation(String location){
        Location = location;
    }

    protected void setLastDate(String lastdate) {LastDate = lastdate; }



    protected String getProject(){

        return Project;
    }

    protected String getLocation(){

        return Location;
    }

    protected String getLastDate(){

        return LastDate;
    }
}
