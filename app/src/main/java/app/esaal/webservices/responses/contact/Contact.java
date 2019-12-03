package app.esaal.webservices.responses.contact;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Contact {
    @SerializedName("name")
    @Expose
    public String name;

    @SerializedName("value")
    @Expose
    public String value;

}
