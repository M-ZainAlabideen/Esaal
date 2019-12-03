package app.esaal.webservices.responses.payments;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import app.esaal.MainActivity;

public class PaymentPackage {
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


    public String getName() {
        if (MainActivity.isEnglish)
            return englishName;
        else
            return arabicName;
    }
}
