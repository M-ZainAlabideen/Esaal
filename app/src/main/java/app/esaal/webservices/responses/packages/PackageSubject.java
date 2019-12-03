package app.esaal.webservices.responses.packages;

import com.google.gson.annotations.SerializedName;

import app.esaal.webservices.responses.subjects.Subject;

public class PackageSubject {
    @SerializedName("material")
    public Subject subject;
}
