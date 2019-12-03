package app.esaal.webservices.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class SendMessageRequest {
    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("mobile")
    @Expose
    public String mobile;
    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("subject")
    @Expose
    public String subject;

    @SerializedName("message")
    @Expose
    public String message;
}
