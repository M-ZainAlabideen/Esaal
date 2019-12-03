package app.esaal.webservices.requests;

import com.google.gson.annotations.SerializedName;

public class TeacherRequest {
    @SerializedName("userid")
    public int userId;

    @SerializedName("firstname")
    public String firstName;

    @SerializedName("middlename")
    public String middleName;

    @SerializedName("lastname")
    public String lastName;

    @SerializedName("mobile")
    public String mobile;

    @SerializedName("email")
    public String email;

    @SerializedName("username")
    public String userName;

    @SerializedName("materialids")
    public String subjectsIds;

    @SerializedName("countryid")
    public int countryId;

    @SerializedName("iban")
    public String IBAN;

    @SerializedName("swiftcode")
    public String swiftCode;

    @SerializedName("accountnumber")
    public String accountNumber;

    @SerializedName("bankname")
    public String bankName;

    @SerializedName("bankaddress")
    public String bankAddress;

    @SerializedName("personaladdress")
    public String personalAddress;

    @SerializedName("description")
    public String description;


}
