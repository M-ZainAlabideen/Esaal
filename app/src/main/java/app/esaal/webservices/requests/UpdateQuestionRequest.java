package app.esaal.webservices.requests;

import com.google.gson.annotations.SerializedName;

public class UpdateQuestionRequest {
    @SerializedName("requestQuestionId")
    public int requestQuestionId;

    @SerializedName("materialId")
    public int materialId;

    @SerializedName("userId")
    public int userId;

    @SerializedName("description")
    public String description;
}
