package app.esaal.webservices.responses.authorization;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User {

    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("firstName")
    @Expose
    public String firstName;

    @SerializedName("middleName")
    @Expose
    public String middleName;

    @SerializedName("lastName")
    @Expose
    public String lastName;

    @SerializedName("mobile")
    @Expose
    public String mobile;

    @SerializedName("email")
    @Expose
    public String email;

    @SerializedName("userName")
    @Expose
    public String userName;

    @SerializedName("isTeacher")
    @Expose
    public boolean isTeacher;

    @SerializedName("description")
    @Expose
    public String description;

    @SerializedName("countryId")
    @Expose
    public int countryId;

    @SerializedName("iban")
    @Expose
    public String IBAN;

    @SerializedName("swiftCode")
    @Expose
    public String swiftCode;

    @SerializedName("accountNumber")
    @Expose
    public String accountNumber;

    @SerializedName("bankName")
    @Expose
    public String bankName;

    @SerializedName("bankAddress")
    @Expose
    public String bankAddress;

    @SerializedName("personalAddress")
    @Expose
    public String personalAddress;

    @SerializedName("isRequest")
    @Expose
    public boolean isRequest;

    @SerializedName("isActive")
    @Expose
    public boolean isActive;
}
