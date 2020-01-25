package app.esaal.webservices.responses.questionsAndReplies;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Attachment implements Serializable {
    @SerializedName("id")
    public int id;

    @SerializedName("fileUrl")
    public String fileUrl;

    @SerializedName("fileType")
    public String fileType;

    @SerializedName("filePlaceholderUrl")
    public String videoFrameUrl;
}
