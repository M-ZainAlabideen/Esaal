package app.esaal.webservices.responses.aboutUs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.esaal.MainActivity;

public class AboutUsResponse {
    @SerializedName("aboutusAr")
    @Expose
    private String aboutUsAr;

    @SerializedName("aboutusEn")
    @Expose
    private String aboutUsEn;

    public String getAboutUs(){
        if(MainActivity.isEnglish)
            return aboutUsEn;
        else
            return aboutUsAr;
    }

}
