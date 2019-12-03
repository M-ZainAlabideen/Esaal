package app.esaal.webservices.responses.questionsAndReplies;

import com.google.gson.annotations.SerializedName;

public class Attachment {
    @SerializedName("id")
    public int id;

    @SerializedName("fileUrl")
    public String fileUrl;

    @SerializedName("fileType")
    public String fileType;
}
