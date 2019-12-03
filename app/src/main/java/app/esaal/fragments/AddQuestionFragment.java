package app.esaal.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;

import java.io.File;
import java.util.ArrayList;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.SubjectsAdapter;
import app.esaal.classes.FixControl;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.questionsAndReplies.Attachment;
import app.esaal.webservices.responses.questionsAndReplies.Question;
import app.esaal.webservices.responses.subjects.Subject;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import static android.app.Activity.RESULT_OK;

public class AddQuestionFragment extends Fragment {
    public static FragmentActivity activity;
    public static AddQuestionFragment fragment;
    private SessionManager sessionManager;
    private Question question;
    private String comingFrom;

    private TypedFile imageTypedFile, videoTypedFile;
    private String imageUrl,videoUrl;
    private final int REQUEST_TAKE_GALLERY_VIDEO = 101;

    private ArrayList<Subject> subjectsList = new ArrayList<>();
    private SubjectsAdapter subjectsAdapter;
    private LinearLayoutManager layoutManager;
    public static int selectedSubjectId;


    @BindView(R.id.fragment_add_question_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_add_question_rv_subjects)
    RecyclerView subjects;
    @BindView(R.id.fragment_add_question_tv_subjectName)
    TextView subjectName;
    @BindView(R.id.fragment_add_question_et_questionText)
    EditText questionText;
    @BindView(R.id.fragment_add_question_iv_imgAttach)
    ImageView imgAttach;
    @BindView(R.id.fragment_add_question_iv_videoAttach)
    ImageView videoAttach;
    @BindView(R.id.fragment_add_question_iv_deleteVideoAttach)
    ImageView deleteVideoAttach;
    @BindView(R.id.fragment_add_question_iv_deleteImgAttach)
    ImageView deleteImgAttach;
    @BindView(R.id.fragment_add_question_iv_play)
    ImageView play;
    @BindView(R.id.fragment_add_question_tv_addVideoWord)
    TextView addVideoWord;
    @BindView(R.id.fragment_add_question_tv_addImgWord)
    TextView addImgWord;

    @BindView(R.id.loading)
    ProgressBar loading;

    public static AddQuestionFragment newInstance(FragmentActivity activity, String comingFrom, Question question) {
        fragment = new AddQuestionFragment();
        AddQuestionFragment.activity = activity;
        Bundle b = new Bundle();
        b.putString("comingFrom", comingFrom);
        b.putSerializable("question", question);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_add_question, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true,true,false,false,"account",getString(R.string.addQuestion));
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        container.setVisibility(View.GONE);
        subjectName.setVisibility(View.GONE);
        deleteImgAttach.setVisibility(View.GONE);
        deleteVideoAttach.setVisibility(View.GONE);
        comingFrom = getArguments().getString("comingFrom");
        question = (Question) getArguments().getSerializable("question");

        if (comingFrom.equals("update")) {
            setDataForUpdating();
        }

        layoutManager = new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false);
        subjectsAdapter = new SubjectsAdapter(activity, subjectsList);
        subjects.setLayoutManager(layoutManager);
        subjects.setAdapter(subjectsAdapter);
        if (subjectsList.size() > 0) {
            loading.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
        } else {
            subjectsApi();
        }
        if (MainActivity.isEnglish) {
            Typeface enBold = Typeface.createFromAsset(activity.getAssets(), "montserrat_medium.ttf");
            subjectName.setTypeface(enBold);
        } else {
            Typeface arBold = Typeface.createFromAsset(activity.getAssets(), "cairo_bold.ttf");
            Typeface cairo = Typeface.createFromAsset(activity.getAssets(), "cairo_regular.ttf");
            subjectName.setTypeface(arBold);
            questionText.setTypeface(cairo);
            addImgWord.setTypeface(cairo);
            addVideoWord.setTypeface(cairo);
        }
    }

    @OnClick(R.id.fragment_add_question_iv_captureImg)
    public void captureImgClick() {
        if(imageUrl == null) {
            ImagePicker.with(this)              //  Initialize ImagePicker with activity or fragment context
                    .setCameraOnly(false)               //  Camera mode
                    .setMultipleMode(false)              //  Select multiple images or single image
                    .setFolderMode(true)                //  Folder mode
                    .setShowCamera(true)                //  Show camera button
                    .setMaxSize(1)                     //  Max images can be selected
                    .setSavePath("ImagePicker")         //  Image capture folder name
                    .setAlwaysShowDoneButton(true)      //  Set always show done button in multiple mode
                    .setKeepScreenOn(true)              //  Keep screen on when selecting images
                    .start();
        }
        else{
            Snackbar.make(loading,getString(R.string.removeImgFirst),Snackbar.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.fragment_add_question_iv_captureVideo)
    public void captureVideoClick() {
        if(videoUrl == null) {
            final Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(intent1, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
        }
        else{
            Snackbar.make(loading,getString(R.string.removeVideoFirst),Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            if (images != null) {
                for (Image uri : images) {
                    imageTypedFile = new TypedFile("image/*", new File(uri.getPath()));
                    Glide.with(activity)
                            .load(uri.getPath())
                            .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                                    .error(R.mipmap.placeholder_attach))
                            .into(imgAttach);

                }
            }
        } else if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                Uri selectedVideo = data.getData();
                String[] filePathColumn = {MediaStore.Video.Media.DATA};
                Cursor cursor = getActivity().getContentResolver().query(selectedVideo,
                        filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String selectedVideoPath = cursor.getString(columnIndex);
                videoTypedFile = new TypedFile("video/*", new File(selectedVideoPath));
                Toast.makeText(activity, ""+selectedVideoPath.toString(), Toast.LENGTH_SHORT).show();

                Bitmap bmThumbnail;
                bmThumbnail = ThumbnailUtils.createVideoThumbnail(selectedVideoPath, MediaStore.Images.Thumbnails.MINI_KIND);
                try {

                    int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_attach);
                    int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_attach);
                    videoAttach.getLayoutParams().height = Height;
                    videoAttach.getLayoutParams().width = Width;
                    videoAttach.setImageBitmap(retriveVideoFrameFromVideo(selectedVideoPath));
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }

        if (imageTypedFile == null) {
            deleteImgAttach.setVisibility(View.GONE);
        } else {
            deleteImgAttach.setVisibility(View.VISIBLE);
        }

        if (videoTypedFile == null) {
            deleteVideoAttach.setVisibility(View.GONE);
        } else {
            deleteVideoAttach.setVisibility(View.VISIBLE);
        }

    }

    @SuppressLint("NewApi")
    public static Bitmap retriveVideoFrameFromVideo(String p_videoPath)
            throws Throwable
    {
        Bitmap m_bitmap = null;
        MediaMetadataRetriever m_mediaMetadataRetriever = null;
        try
        {
            m_mediaMetadataRetriever = new MediaMetadataRetriever();
            m_mediaMetadataRetriever.setDataSource(p_videoPath);
            m_bitmap = m_mediaMetadataRetriever.getFrameAtTime();
        }
        catch (Exception m_e)
        {
            throw new Throwable(
                    "Exception in retriveVideoFrameFromVideo(String p_videoPath)"
                            + m_e.getMessage());
        }
        finally
        {
            if (m_mediaMetadataRetriever != null)
            {
                m_mediaMetadataRetriever.release();
            }
        }
        return m_bitmap;
    }

    @OnClick(R.id.fragment_add_question_iv_deleteImgAttach)
    public void deleteImgAttach() {
        if (comingFrom.equals("update")) {
            for (Attachment value : question.attachments) {
                if (value.fileUrl != null && !value.fileUrl.isEmpty()) {
                    if (value.fileType.equals("i")) {
                        deleteAttachmentApi(value.id, "image");
                    }
                }
            }
        }
        imgAttach.setImageResource(R.mipmap.placeholder_attach);
        imageTypedFile = null;
        deleteImgAttach.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_add_question_iv_deleteVideoAttach)
    public void deleteVideoAttach() {
        if (comingFrom.equals("update")) {
            for (Attachment value : question.attachments) {
                if (value.fileUrl != null && !value.fileUrl.isEmpty()) {
                    if (value.fileType.equals("v")) {
                        deleteAttachmentApi(value.id, "video");
                    }
                }
            }
        }
        videoAttach.setImageResource(R.mipmap.placeholder_attach);
        videoTypedFile = null;
        deleteVideoAttach.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_add_question_tv_send)
    public void sendClick() {
        String questionTextStr = questionText.getText().toString();
        if (questionTextStr == null || questionTextStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterQuestion), Snackbar.LENGTH_SHORT).show();
        } else {
            if (comingFrom.equals("update")) {
                updateQuestionApi(question.id, selectedSubjectId, sessionManager.getUserId(), questionTextStr);
            } else {
                addQuestionApi(selectedSubjectId, questionTextStr);
            }
        }
    }

    private void subjectsApi() {
        EsaalApiConfig.getCallingAPIInterface().userSubjects(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                new Callback<ArrayList<Subject>>() {
                    @Override
                    public void success(ArrayList<Subject> subjects, Response response) {
                        loading.setVisibility(View.GONE);
                        container.setVisibility(View.VISIBLE);
                        int status = response.getStatus();
                        if (status == 200) {
                            subjectsList.addAll(subjects);
                            subjectsAdapter.notifyDataSetChanged();
                            selectedSubjectId = subjectsList.get(0).id;
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        int failureStatus = error.getResponse().getStatus();
                        if (failureStatus == 202) {
                            Snackbar.make(loading, getString(R.string.noSubjects), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setDataForUpdating() {
        subjectName.setVisibility(View.VISIBLE);
        subjectName.setText(question.subject.getName());
        questionText.setText(question.description);
        for (Attachment value : question.attachments) {
            if (value.fileUrl != null && !value.fileUrl.isEmpty()) {
                if (value.fileType.equals("i")) {
                    loadImages(value.fileUrl, imgAttach);
                    imageUrl = value.fileUrl;
                } else if (value.fileType.equals("v")) {
                    loadImages(value.fileUrl, videoAttach);
                    videoUrl = value.fileUrl;
                }
            }
        }
        if (imgAttach.getDrawable().getConstantState() == getResources().getDrawable(R.mipmap.placeholder_attach).getConstantState()) {
            deleteImgAttach.setVisibility(View.GONE);
        }
        else{
            deleteImgAttach.setVisibility(View.VISIBLE);
        }
        if (videoAttach.getDrawable().getConstantState() == getResources().getDrawable(R.mipmap.placeholder_attach).getConstantState()) {
            deleteVideoAttach.setVisibility(View.GONE);
        }
        else{
            deleteVideoAttach.setVisibility(View.VISIBLE);
        }
    }

    private void loadImages(String url, ImageView image) {
        Glide.with(activity)
                .load(url)
                .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                        .error(R.mipmap.placeholder_attach))
                .into(image);
    }

    private void addQuestionApi(int subjectId, String description) {
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().addQuestion(
                sessionManager.getUserToken(), subjectId, sessionManager.getUserId(), description,
                imageTypedFile, videoTypedFile, new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> question, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                            Snackbar.make(loading, getString(R.string.questionAdded), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        int failureStatus = error.getResponse().getStatus();
                        if (failureStatus == 201) {
                            Snackbar.make(loading, getString(R.string.noBalance), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void updateQuestionApi(int questionId, int subjectId, int userId, String Description) {
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().updateQuestion(
                sessionManager.getUserToken(),
                questionId,
                subjectId,
                userId,
                Description,
                imageTypedFile, videoTypedFile, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        loading.setVisibility(View.GONE);
                        int status = response2.getStatus();
                        if (status == 200) {
                            Snackbar.make(loading, getString(R.string.updateSuccess), Snackbar.LENGTH_SHORT).show();
                            GlobalFunctions.clearLastStack(activity);
                            Navigator.loadFragment(activity, QuestionDetailsFragment.newInstance(activity, question.id), R.id.activity_main_fl_container, false);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }

    private void deleteAttachmentApi(int requestAttachmentId, final String type) {
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().deleteAttachment(
                sessionManager.getUserToken(), requestAttachmentId,
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        loading.setVisibility(View.GONE);
                        int status = response2.getStatus();
                        if (status == 200) {
                            if (type.equals("image")) {
                                imgAttach.setImageResource(R.mipmap.placeholder_attach);
                                imageUrl = null;
                            } else if (type.equals("video")) {
                                videoAttach.setImageResource(R.mipmap.placeholder_attach);
                                videoUrl = null;
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }
}
