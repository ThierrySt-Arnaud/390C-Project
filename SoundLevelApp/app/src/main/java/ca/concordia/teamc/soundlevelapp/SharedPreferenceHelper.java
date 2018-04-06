package ca.concordia.teamc.soundlevelapp;

import android.content.Context;
import android.content.SharedPreferences;


public class SharedPreferenceHelper {
    private SharedPreferences sharedPreferences;
    public SharedPreferenceHelper(Context context)
    {
        sharedPreferences = context.getSharedPreferences("ProfilePreference",
                Context.MODE_PRIVATE );
    }
    public void saveProfileName(Profile profile)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("profileProject",profile.getProject() );
        editor.putString("profileLocation",profile.getLocation() );
        editor.putString("lastdate",profile.getLastDate() );
        editor.commit();
    }



    public String getProfileProject()
    {
        return sharedPreferences.getString("profileProject", null);
    }
    public String getProfileLocation()
    {
        return sharedPreferences.getString("profileLocation", null);
    }
    public String getLastDate()
    {
        return sharedPreferences.getString("profilelastdate",null);
    }
}

