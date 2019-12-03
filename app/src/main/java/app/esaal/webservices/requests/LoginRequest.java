package app.esaal.webservices.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginRequest {
    @SerializedName("username")
    @Expose
    public String userName;

    @SerializedName("password")
    @Expose
    public String password;
}
