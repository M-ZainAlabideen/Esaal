package app.esaal.webservices.responses.balance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Balance {

    @SerializedName("isRequest")
    @Expose
    public boolean isRequest;

    @SerializedName("totalAmount")
    @Expose
    public double totalAmount;

    @SerializedName("totalQuestionAnswer")
    @Expose
    public int totalQuestions;

}

