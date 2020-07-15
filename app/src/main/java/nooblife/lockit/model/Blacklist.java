package nooblife.lockit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.ArrayList;

@Entity(tableName = "apps")
public class Blacklist {

    @PrimaryKey
    @NonNull
    String profileName = "default";

    ArrayList<String> packageList;

    public Blacklist(ArrayList<String> packageList) {
        this.packageList = packageList;
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

    public ArrayList<String> getPackageList() {
        return packageList;
    }

    public void setPackageList(ArrayList<String> packageList) {
        this.packageList = packageList;
    }
}
