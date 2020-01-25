package app.esaal.webservices.responses.payments;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

import app.esaal.classes.GlobalFunctions;
import app.esaal.webservices.responses.packages.Package;

public class Payment {
    @SerializedName("subScriptionDate")
    @Expose
    private String paymentDate;

    @SerializedName("subsciptionEndDate")
    @Expose
    private String endDate;

    @SerializedName("creationDate")
    @Expose
    private String creationDate;

    @SerializedName("subscription")
    @Expose
    public PaymentPackage paymentPackage;

    @SerializedName("remainQuestionNumber")
    @Expose
    public int remainQuestionNumber;

    public String getPaymentDate() {
        return GlobalFunctions.formatDate(paymentDate);
    }

    public String getEndDate() {
        return GlobalFunctions.formatDate(endDate);
    }

    public String getCreationDate() {
        return GlobalFunctions.formatDate(creationDate);
    }
}
