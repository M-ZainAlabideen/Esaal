package app.esaal.webservices.responses.questionsAndReplies;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import app.esaal.webservices.responses.subjects.Subject;

public class Question implements Serializable {


    @SerializedName("id")
    public int id;

    @SerializedName("materialId")
    public int materialId;

    @SerializedName("userId")
    public int userId;


    @SerializedName("description")
    public String description;

    @SerializedName("attachment")
    public ArrayList<Attachment> attachments;

    @SerializedName("replayQuestions")
    public ArrayList<Reply> replies;

    @SerializedName("material")
    @Expose
    public Subject subject;

    @SerializedName("isPending")
    @Expose
    public boolean isPending;

    @SerializedName("pendingDate")
    @Expose
    public String pendingDate;

    @SerializedName("remainTime")
    @Expose
    public double remainTime;


    @SerializedName("pendingUserId")
    @Expose
    public int pendingUserId;

    @SerializedName("creationDate")
    @Expose
    public String creationDate;
}
