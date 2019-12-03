package app.esaal.webservices.responses.authorization;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class UserResponse {
    @SerializedName("token")
    @Expose
    public String token;

    @SerializedName("user")
    @Expose
    public User user;

    @SerializedName("issubscribe")
    @Expose
    public boolean hasPackages;
}
