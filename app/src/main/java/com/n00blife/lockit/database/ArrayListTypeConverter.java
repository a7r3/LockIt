package com.n00blife.lockit.database;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

import androidx.room.TypeConverter;

public class ArrayListTypeConverter {

    @TypeConverter
    public String fromApplicationPackagesList(ArrayList<String> packageList) {
        return new Gson().toJson(packageList, new TypeToken<ArrayList<String>>(){}.getType());
    }

    @TypeConverter
    public ArrayList<String> toApplicationPackagesList(String serializedPackageList) {
        return new Gson().fromJson(serializedPackageList, new TypeToken<ArrayList<String>>(){}.getType());
    }
}
