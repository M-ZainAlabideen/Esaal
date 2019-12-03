package app.esaal.webservices.responses.notifications;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import app.esaal.MainActivity;
import app.esaal.classes.GlobalFunctions;

public class Notification {
    @SerializedName("userId")
    @Expose
    public int userId;

    @SerializedName("notificationId")
    @Expose
    public int notificationId;

    @SerializedName("requestQuestionId")
    @Expose
    public int requestQuestionId;

    @SerializedName("creationDate")
    @Expose
    private String creationDate;

    @SerializedName("arabicMessage")
    @Expose
    private String arabicMessage;

    @SerializedName("englishMessage")
    @Expose
    private String englishMessage;

    @SerializedName("materialArabicName")
    @Expose
    private String subjectArabicName;

    @SerializedName("materialEnglishName")
    @Expose
    private String subjectEnglishName;


    @SerializedName("isRead")
    @Expose
    public boolean isRead;

    public String getMessage(){
        if(MainActivity.isEnglish)
            return englishMessage;
        else
            return arabicMessage;
    }

    public String getSubjectName(){
        if(MainActivity.isEnglish)
            return subjectEnglishName;
        else
            return subjectArabicName;
    }
    public String getNotificationDate(){
        return GlobalFunctions.formatDate(creationDate);
    }
}
