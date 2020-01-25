package app.esaal.webservices.responses.subjects;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import app.esaal.MainActivity;

public class Subject implements Serializable {
    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("arabicName")
    @Expose
    private String arabicName;

    @SerializedName("englishName")
    @Expose
    private String englishName;

    public String getName() {
        if (MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }

    public void setName(String name) {
        if (MainActivity.isEnglish)
            englishName = name;
        else
            arabicName = name;
    }
}
