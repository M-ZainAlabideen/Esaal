package app.esaal.webservices.requests;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StudentRequest {

    @SerializedName("userid")
    @Expose
    public int userId;

    @SerializedName("firstname")
    @Expose
    public String firstName;

    @SerializedName("middlename")
    public String middleName;

    @SerializedName("lastname")
    @Expose
    public String lastName;

    @SerializedName("mobile")
    @Expose
    public String mobile;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("username")
    @Expose
    public String username;

    @SerializedName("password")
    @Expose
    public String password;


}
