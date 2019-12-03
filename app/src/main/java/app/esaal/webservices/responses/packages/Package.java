package app.esaal.webservices.responses.packages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.webservices.responses.subjects.Subject;

public class Package {
    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("arabicName")
    @Expose
    private String arabicName;


    @SerializedName("englishName")
    @Expose
    private String englishName;

    @SerializedName("price")
    @Expose
    public int price;

    @SerializedName("numberOfQuestion")
    @Expose
    public int numberOfQuestion;

    @SerializedName("subscripeMaterials")
    @Expose
    public ArrayList<PackageSubject> packageSubjects;

    @SerializedName("periodId")
    @Expose
    public int periodId;

    @SerializedName("arabicAbout")
    @Expose
    public String arabicAbout;

    @SerializedName("englishAbout")
    @Expose
    public String englishAbout;

    @SerializedName("colorCode")
    @Expose
    public String colorCode;


    @SerializedName("code")
    @Expose
    public String code;


    public String getName() {
        if (MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }


}
