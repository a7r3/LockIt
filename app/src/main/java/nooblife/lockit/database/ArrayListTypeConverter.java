package nooblife.lockit.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

public class ArrayListTypeConverter {

    @TypeConverter
    public String fromApplicationPackagesList(ArrayList<String> packageList) {
        return new Gson().toJson(packageList, new TypeToken<ArrayList<String>>() {
        }.getType());
    }

    @TypeConverter
    public ArrayList<String> toApplicationPackagesList(String serializedPackageList) {
        return new Gson().fromJson(serializedPackageList, new TypeToken<ArrayList<String>>() {
        }.getType());
    }
}
