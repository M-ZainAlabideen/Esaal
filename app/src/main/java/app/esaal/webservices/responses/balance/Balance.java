package app.esaal.webservices.responses.balance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Balance {

    @SerializedName("totalAmount")
    @Expose
    public int totalAmount;

    @SerializedName("totalQuestionAnswer")
    @Expose
    public int totalQuestions;

}
