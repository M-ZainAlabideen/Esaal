package app.esaal.webservices.responses.countries;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.esaal.MainActivity;

public class Country {
    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("arabicName")
    @Expose
    private String arabicName;

    @SerializedName("englishName")
    @Expose
    private String englishName;

    @SerializedName("isGulfCountry")
    @Expose
    public boolean isGulfCountry;

    public String getName() {
        if(MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }
}
