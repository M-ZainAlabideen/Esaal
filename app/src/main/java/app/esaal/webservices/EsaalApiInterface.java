package app.esaal.webservices;

import java.util.ArrayList;

import app.esaal.classes.Constants;
import app.esaal.webservices.requests.LoginRequest;
import app.esaal.webservices.requests.SendMessageRequest;
import app.esaal.webservices.requests.StudentRequest;
import app.esaal.webservices.requests.TeacherRequest;
import app.esaal.webservices.requests.UpdateQuestionRequest;
import app.esaal.webservices.responses.aboutUs.AboutUsResponse;
import app.esaal.webservices.responses.authorization.ChangePasswordRequest;
import app.esaal.webservices.responses.authorization.User;
import app.esaal.webservices.responses.authorization.UserResponse;
import app.esaal.webservices.responses.balance.Balance;
import app.esaal.webservices.responses.contact.Contact;
import app.esaal.webservices.responses.countries.Country;
import app.esaal.webservices.responses.notifications.Notification;
import app.esaal.webservices.responses.packages.SelectPackageResponse;
import app.esaal.webservices.responses.payments.Payment;
import app.esaal.webservices.responses.questionsAndReplies.Question;
import app.esaal.webservices.responses.questionsAndReplies.Reply;
import app.esaal.webservices.responses.slider.Slider;
import app.esaal.webservices.responses.subjects.Subject;
import app.esaal.webservices.responses.packages.Package;
import butterknife.BindView;
import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.http.Query;
import retrofit.mime.TypedFile;

public interface EsaalApiInterface {

    //************************AUTHORIZATION****************************//

    @POST("/SignUpStudent")
    void studentSignUp(@Body StudentRequest studentRequest,
                       Callback<UserResponse> response);

    @POST("/UpdateStudent")
    void editStudentProfile(@Header(Constants.AUTHORIZATION) String authorization,
                            @Body StudentRequest studentRequest,
                            Callback<User> response);

    @Multipart
    @POST("/SignUpTeacher")
    void teacherSignUp(@Part("FirstName") String FirstName,
                       @Part("MiddleName") String MiddleName,
                       @Part("LastName") String LastName,
                       @Part("Mobile") String Mobile,
                       @Part("Email") String Email,
                       @Part("UserName") String UserName,
                       @Part("Password") String Password,
                       @Part("MaterialIds") String subjectsIds,
                       @Part("CountryId") int CountryId,
                       @Part("IBAN") String IBAN,
                       @Part("SwiftCode") String SwiftCode,
                       @Part("AccountNumber") String AccountNumber,
                       @Part("BankName") String BankName,
                       @Part("BankAddress") String BankAddress,
                       @Part("PersonalAddress") String PersonalAddress,
                       @Part("Description") String Description,
                       @Part("userimage") TypedFile userImage,
                       @Part("useridcardfront") TypedFile civilIdFront,
                       @Part("useridcardback") TypedFile civilIdBack,
                       @Part("usercertification") TypedFile certification,
                       Callback<UserResponse> response);

    @POST("/updateTeacher")
    void editTeacherProfile(@Header(Constants.AUTHORIZATION) String authorization,
                            @Body TeacherRequest teacherRequest,
                            Callback<User> response);

    @POST("/LogIn")
    void login(@Body LoginRequest loginRequest,
               Callback<UserResponse> response);

    @POST("/ForgetPassword")
    void forgetPassword(@Query("email") String email,
                        Callback<String> response);

    @POST("/ChangePassword")
    void changePassword(@Header(Constants.AUTHORIZATION) String authorization,
            @Body ChangePasswordRequest changePassRequest,
            Callback<Response> response);

    @GET("/GetUserData")
    void userById(@Header(Constants.AUTHORIZATION) String authorization,
                  @Query("userId") int userId,
                  Callback<User> response);

    @GET("/UserMatrials")
    void userSubjects(@Header(Constants.AUTHORIZATION) String authorization,
                      @Query("userid") int userId,
            Callback<ArrayList<Subject>> response);

    @GET("/Countries")
    void countries(Callback<ArrayList<Country>> response);

    @GET("/TeacherCridit")
    void teacherBalance(@Header(Constants.AUTHORIZATION) String authorization,
                        @Query("userid") int userId,
                        Callback<Balance> response);

    @POST("/RequestCredit")
    void withdrawBalance(@Header(Constants.AUTHORIZATION) String authorization,
                         @Query("userid") int userId,
                         Callback<User> response);
    //************************HOME****************************//

    @GET("/SliderPhoto")
    void slider(@Header(Constants.AUTHORIZATION) String authorization,
                Callback<ArrayList<Slider>> response);

    @GET("/GetUserNotifications")
    void notifications(@Header(Constants.AUTHORIZATION) String authorization,
                       @Query("userid") int userId,
                       Callback<ArrayList<Notification>> response);

    @POST("/UpdateUserNotifications")
    void updateNotifications(@Header(Constants.AUTHORIZATION) String authorization,
                             @Query("userid") int userId,
                             Callback<Response> response);


    @GET("/GetSubscriptions")
    void packages(@Header(Constants.AUTHORIZATION) String authorization,
                  Callback<ArrayList<Package>> response);


    @POST("/MakeSubscription")
    void selectPackage(@Header(Constants.AUTHORIZATION) String authorization,
                       @Query("userid") int userId,
                       @Query("subscriptionid") int subscriptionId,
                       Callback<SelectPackageResponse> response);

    @GET("/GetUserSubscriptions")
    void payments(@Header(Constants.AUTHORIZATION) String authorization,
                  @Query("userid") int userId,
                  Callback<ArrayList<Payment>> response);

    @GET("/Matrials")
    void subjects(@Query("materialid") String materialId,
                  Callback<ArrayList<Subject>> response);

    //************************CONTACTS AND ABOUT-US****************************//

    @GET("/ContactUs")
    void contacts(@Header(Constants.AUTHORIZATION) String authorization,
                  Callback<ArrayList<Contact>> response);

    @POST("/ContactUs")
    void sendMessage(@Header(Constants.AUTHORIZATION) String authorization,
                     @Body SendMessageRequest sendMessageRequest,
                     Callback<String> response);

    @GET("/AboutUs")
    void aboutUs(@Header(Constants.AUTHORIZATION) String authorization,
                 Callback<AboutUsResponse> response);

    //************************QUESTIONS AND ANSWERS****************************//

    @Multipart
    @POST("/AddQuestion")
    void addQuestion(@Header(Constants.AUTHORIZATION) String authorization,
                     @Part("MaterialId") int subjectId,
                     @Part("UserId") int userId,
                     @Part("Description") String description,
                     @Part("attachmentimage") TypedFile attachmentImage,
                     @Part("attachmentvideo") TypedFile attachmentVideo,
                     Callback<ArrayList<Question>> response);

    @Multipart
    @POST("/AddReplay")
    void addReply(@Header(Constants.AUTHORIZATION) String authorization,
                  @Part("RequestQuestionId") int questionId,
                  @Part("UserId") int userId,
                  @Part("ReplayMessage") String replyMessage,
                  @Part("attachmentimage") TypedFile attachmentImage,
                  @Part("attachmentvideo") TypedFile attachmentVideo,
                  Callback<ArrayList<Reply>> response);

    @GET("/GetUserQuestions")
    void questions(@Header(Constants.AUTHORIZATION) String authorization,
                   @Query("userid") int userId,
                   @Query("pageNum") int pageNum,
                   Callback<ArrayList<Question>> response);

    @GET("/GetUserQuestionsByMaterials")
    void filterResult(@Header(Constants.AUTHORIZATION) String authorization,
                      @Query("userid") int userId,
                      @Query("materialid") int subjectId,
                      @Query("pageNum") int pageNum,
                      Callback<ArrayList<Question>> response);

    @GET("/GetUserSearshQuestions")
    void searchResults(@Header(Constants.AUTHORIZATION) String authorization,
                       @Query("userid") int userId,
                       @Query("strSearch") String query,
                       @Query("pageNum") int pageNum,
                       Callback<ArrayList<Question>> response
    );

    @GET("/GetQuestion")
    void questionById(@Header(Constants.AUTHORIZATION) String authorization,
                      @Query("questionid") int questionId,
                      Callback<ArrayList<Question>> response);

    @POST("/LikeReplay")
    void like(@Header(Constants.AUTHORIZATION) String authorization,
              @Query("replayid") int replayId,
              Callback<Response> response);

    @POST("/DislikeReplay")
    void dislike(@Header(Constants.AUTHORIZATION) String authorization,
                 @Query("replayid") int replayId,
                 Callback<Response> response);

    @Multipart
    @POST("/UpdateQuestion")
    void updateQuestion(@Header(Constants.AUTHORIZATION) String authorization,
                        @Part("RequestQuestionId") int requestQuestionId,
                        @Part("MaterialId") int subjectId,
                        @Part("UserId") int UserId,
                        @Part("Description") String Description,
                        @Part("attachmentimage") TypedFile attachmentImage,
                        @Part("attachmentvideo") TypedFile attachmentVideo,
                        Callback<Response> response);


    @POST("/DeleteQuestionAttachment")
    void deleteAttachment(@Header(Constants.AUTHORIZATION) String authorization,
                          @Query("requestattachmentid") int requestAttachmentId,
                          Callback<Response> response);


    @POST("/PendQuestion")
    void makePending(@Header(Constants.AUTHORIZATION) String authorization,
                     @Query("requestquestionid") int requestQuestionId,
                     @Query("userid") int userId,
                     Callback<Question> response);

    @POST("/RemovePendQuestion")
    void removePending(
            @Header(Constants.AUTHORIZATION) String authorization,
            @Query("requestquestionid") int requestQuestionId,
            @Query("userid") int userId,
            Callback<Question> response
    );
}
