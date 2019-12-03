package app.esaal.webservices.responses.packages;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SelectPackageResponse {
    @SerializedName("returnUrl")
    @Expose
    public String returnUrl;

    @SerializedName("subscriptionId")
    @Expose
     public int subscriptionId;

    @SerializedName("paymentId")
    @Expose
     public String paymentId;
}
