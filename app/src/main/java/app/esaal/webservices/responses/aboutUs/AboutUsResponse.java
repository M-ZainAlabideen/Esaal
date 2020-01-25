package app.esaal.webservices.responses.aboutUs;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.esaal.MainActivity;

public class AboutUsResponse {
    @SerializedName("aboutUsAr")
    @Expose
    private String aboutUsAr;

    @SerializedName("aboutUsEn")
    @Expose
    private String aboutUsEn;

    @SerializedName("studentTermsConditionAr")
    @Expose
    private String studentTermsAr;

    @SerializedName("studentTermsConditionEn")
    @Expose
    private String studentTermsEn;

    @SerializedName("teacherTermsConditionAr")
    @Expose
    private String teacherTermsAr;

    @SerializedName("teacherTermsConditionEn")
    @Expose
    private String teacherTermsEn;

    public String getAboutUs() {
        if (MainActivity.isEnglish)
            return aboutUsEn;
        else
            return aboutUsAr;
    }

    public String getStudentTermsCondition() {
        if(MainActivity.isEnglish)
            return studentTermsEn;
        else
            return studentTermsAr;
    }

    public String getTeacherTermsCondition() {
        if(MainActivity.isEnglish)
            return teacherTermsEn;
        else
            return teacherTermsAr;
    }
}
