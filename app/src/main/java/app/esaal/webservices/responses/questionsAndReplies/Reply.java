package app.esaal.webservices.responses.questionsAndReplies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class Reply {
    @SerializedName("id")
    @Expose
    public int id;

    @SerializedName("userId")
    @Expose
    public int userId;

    @SerializedName("requestQuestionId")
    @Expose
    public int questionId;

    @SerializedName("replayMessage")
    @Expose
     public String replyMessage;

    @SerializedName("isLiked")
    @Expose
    public boolean isLiked;

    @SerializedName("isDisliked")
    @Expose
    public boolean isDisliked;

    @SerializedName("attachments")
    @Expose
    public ArrayList<Attachment> attachments;

}
