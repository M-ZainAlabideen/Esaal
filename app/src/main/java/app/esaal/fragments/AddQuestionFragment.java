package app.esaal.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.nguyenhoanglam.imagepicker.model.Config;
import com.nguyenhoanglam.imagepicker.model.Image;
import com.nguyenhoanglam.imagepicker.ui.imagepicker.ImagePicker;
import com.vincent.videocompressor.VideoCompress;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

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
import id.zelory.compressor.Compressor;
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
    private AlertDialog dialog;

    private TypedFile imageTypedFile, videoTypedFile;
    private String imageUrl, videoUrl;
    private final int REQUEST_TAKE_GALLERY_VIDEO = 101;
    private final int REQUEST_TAKE_CAMERA_VIDEO = 102;

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
        if (activity == null) {
            activity = getActivity();
        }
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        container.setVisibility(View.GONE);
        subjectName.setVisibility(View.GONE);
        deleteImgAttach.setVisibility(View.GONE);
        deleteVideoAttach.setVisibility(View.GONE);
        imgAttach.setVisibility(View.GONE);
        videoAttach.setVisibility(View.GONE);
        play.setVisibility(View.GONE);

        selectedSubjectId = -1;
        comingFrom = getArguments().getString("comingFrom");
        question = (Question) getArguments().getSerializable("question");

        if (comingFrom.equals("update")) {
            setDataForUpdating();
            MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.editQuestion));
        } else {
            MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.addQuestion));
        }
        GlobalFunctions.hasNewNotificationsApi(activity);

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
        if (imageUrl == null) {
            if (!GlobalFunctions.isWriteExternalStorageAllowed(activity)) {
                GlobalFunctions.requestWriteExternalStoragePermission(activity);
            } else if (!GlobalFunctions.isReadExternalStorageAllowed(activity)) {
                GlobalFunctions.requestReadExternalStoragePermission(activity);
            } else {
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
        } else {
            Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer), getString(R.string.removeImgFirst), Snackbar.LENGTH_SHORT).show();
        }

    }

    @OnClick(R.id.fragment_add_question_iv_captureVideo)
    public void captureVideoClick() {
        if (videoUrl == null) {
            if (!GlobalFunctions.isWriteExternalStorageAllowed(activity)) {
                GlobalFunctions.requestWriteExternalStoragePermission(activity);
            } else if (!GlobalFunctions.isReadExternalStorageAllowed(activity)) {
                GlobalFunctions.requestReadExternalStoragePermission(activity);
            } else if (!GlobalFunctions.isCameraPermission(activity)) {
                GlobalFunctions.requestCameraPermission(activity);
            } else {
                createCaptureVideoMethodsDialog();
            }
        } else {
            Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer), getString(R.string.removeVideoFirst), Snackbar.LENGTH_SHORT).show();
        }
    }

    public void createCaptureVideoMethodsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final View popUpView = activity.getLayoutInflater().inflate(R.layout.custom_dialog_capture_video, null);
        TextView gallery = (TextView) popUpView.findViewById(R.id.custom_dialog_capture_video_tv_gallery);
        TextView camera = (TextView) popUpView.findViewById(R.id.custom_dialog_capture_video_tv_camera);

        builder.setView(popUpView);
        dialog = builder.create();
        dialog.show();

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseVideoFromGallery();
                closePopUp();
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeVideoFromCamera();
                closePopUp();
            }
        });
    }


    public void closePopUp() {
        dialog.cancel();

    }

    private void chooseVideoFromGallery() {
        final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);
    }

    private void takeVideoFromCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, REQUEST_TAKE_CAMERA_VIDEO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Config.RC_PICK_IMAGES && resultCode == RESULT_OK && data != null) {
            ArrayList<Image> images = data.getParcelableArrayListExtra(Config.EXTRA_IMAGES);
            if (images != null) {
                for (Image uri : images) {

                    //compress the image
                    File file = new File(uri.getPath());
                    try {
                        file = new Compressor(activity).compressToFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageTypedFile = new TypedFile("image/*", new File(file.getPath()));
                   loadImages(file.getPath(),imgAttach);
                }
            }
        } else if (resultCode == RESULT_OK && data != null) {
            Uri selectedVideoUri = data.getData();
            String videoPath = "";
            long videoDuration = checkVideoDurationValidation(selectedVideoUri);
            if (videoDuration > 120000) {
                Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer), getString(R.string.videoDuration), Snackbar.LENGTH_SHORT).show();
            } else {
                if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                    videoPath = getPath(selectedVideoUri);
                } else if (requestCode == REQUEST_TAKE_CAMERA_VIDEO) {
                    videoPath = getRealPath(selectedVideoUri);
                }
                createCompressedVideoPath(videoPath);
            }
        }

        if (imageTypedFile == null) {
            imgAttach.setVisibility(View.GONE);
            deleteImgAttach.setVisibility(View.GONE);
        } else {
            imgAttach.setVisibility(View.VISIBLE);
            deleteImgAttach.setVisibility(View.VISIBLE);
        }

    }

    private void createCompressedVideoPath(String videoPath) {
        String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String outputPath = outputDir + File.separator + "VID_" + sessionManager.getUserId() + Calendar.getInstance().getTimeInMillis() + ".mp4";
        compressVideo(videoPath, outputPath);
    }

    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    public String getRealPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } else
            return null;
    }

    private long checkVideoDurationValidation(Uri uri) {
        Cursor cursor = MediaStore.Video.query(activity.getContentResolver(), uri, new
                String[]{MediaStore.Video.VideoColumns.DURATION});
        long duration = 0;
        if (cursor != null && cursor.moveToFirst()) {
            duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Video
                    .VideoColumns.DURATION));
            cursor.close();
        }

        return duration;
    }

    private void compressVideo(String input, final String output) {
        VideoCompress.VideoCompressTask task = VideoCompress.compressVideoLow(input, output, new VideoCompress.CompressListener() {
            @Override
            public void onStart() {
                //Start Compress
                loading.setVisibility(View.VISIBLE);
                GlobalFunctions.DisableLayout(container);
            }

            @Override
            public void onSuccess() {
                //Finish successfully
                loading.setVisibility(View.GONE);
                GlobalFunctions.EnableLayout(container);
                Log.d("VIDEO-TAG", output);
                setVideo(output);
            }

            @Override
            public void onFail() {
                //Failed
            }

            @Override
            public void onProgress(float percent) {
                //Progress
            }
        });
    }

    private void setVideo(String finalVideoPath) {
        if (finalVideoPath != null) {
            videoTypedFile = new TypedFile("video/*", new File(finalVideoPath));

            if (videoTypedFile == null) {
                videoAttach.setVisibility(View.GONE);
                play.setVisibility(View.GONE);
                deleteVideoAttach.setVisibility(View.GONE);
            } else {
                videoAttach.setVisibility(View.VISIBLE);
                play.setVisibility(View.VISIBLE);
                deleteVideoAttach.setVisibility(View.VISIBLE);
            }

            try {
                int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_attach);
                int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_attach);
                videoAttach.getLayoutParams().height = Height;
                videoAttach.getLayoutParams().width = Width;
                play.getLayoutParams().height = Height;
                play.getLayoutParams().width = Width;
                videoAttach.setImageBitmap(GlobalFunctions.loadVideoFrameFromPath(finalVideoPath));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
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
        imgAttach.setVisibility(View.GONE);
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
        videoAttach.setVisibility(View.GONE);
        play.setVisibility(View.GONE);
        deleteVideoAttach.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_add_question_tv_send)
    public void sendClick() {
        String questionTextStr = questionText.getText().toString();
        if (questionTextStr == null || questionTextStr.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer), getString(R.string.enterQuestion), Snackbar.LENGTH_SHORT).show();
        } else {
            if (comingFrom.equals("update")) {
                updateQuestionApi(question.id, selectedSubjectId, sessionManager.getUserId(), questionTextStr);
            } else {
                if (selectedSubjectId == -1) {
                    Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer), getString(R.string.selectSubject), Snackbar.LENGTH_SHORT).show();
                } else {
                    addQuestionApi(selectedSubjectId, questionTextStr);
                }
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

                            if (comingFrom.equals("update")) {
                                selectedSubjectId = question.subjectId;
                                subjectsAdapter.notifyDataSetChanged();
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        if (error.getResponse() != null && error.getResponse().getStatus() == 202) {
                            Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer), getString(R.string.noSubjects), Snackbar.LENGTH_SHORT).show();
                        } else {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
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
                    videoUrl = value.fileUrl;
                    loadImages(value.videoFrameUrl, videoAttach);
                }
            }
        }
        if (imageUrl == null || imageUrl.isEmpty()) {
            imgAttach.setVisibility(View.GONE);
            deleteImgAttach.setVisibility(View.GONE);
        } else {
            imgAttach.setVisibility(View.VISIBLE);
            deleteImgAttach.setVisibility(View.VISIBLE);
        }
        if (videoUrl == null || videoUrl.isEmpty()) {
            videoAttach.setVisibility(View.GONE);
            play.setVisibility(View.GONE);
            deleteVideoAttach.setVisibility(View.GONE);
        } else {
            videoAttach.setVisibility(View.VISIBLE);
            play.setVisibility(View.VISIBLE);
            deleteVideoAttach.setVisibility(View.VISIBLE);
        }
    }

    private void loadImages(String url, ImageView image) {
        int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_attach);
        int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_attach);
        image.getLayoutParams().height = Height;
        image.getLayoutParams().width = Width;

        Glide.with(activity)
                .load(url)
                .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                        .error(R.mipmap.placeholder_attach))
                .into(image);
    }

    private void addQuestionApi(int subjectId, String description) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().addQuestion(
                sessionManager.getUserToken(), subjectId, sessionManager.getUserId(), description,
                imageTypedFile, videoTypedFile, new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> question, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            getFragmentManager().popBackStack();
                            Navigator.loadFragment(activity, HomeFragment.newInstance(activity), R.id.activity_main_fl_container, false);
                            Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer), getString(R.string.questionAdded), Snackbar.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        if (error.getResponse() != null && error.getResponse().getStatus() == 201) {
                            Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer), getString(R.string.noBalance), Snackbar.LENGTH_SHORT).show();
                        } else {
                            loading.setVisibility(View.GONE);
                            Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void updateQuestionApi(int questionId, int subjectId, int userId, String Description) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
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
                        GlobalFunctions.EnableLayout(container);
                        int status = response2.getStatus();
                        if (status == 200) {
                            Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer), getString(R.string.updateSuccess), Snackbar.LENGTH_SHORT).show();
                            getFragmentManager().popBackStack();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        GlobalFunctions.EnableLayout(container);
                    }
                }
        );
    }

    private void deleteAttachmentApi(int requestAttachmentId, final String type) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().deleteAttachment(
                sessionManager.getUserToken(), requestAttachmentId,
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
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
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_add_question_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        GlobalFunctions.EnableLayout(container);
                    }
                }
        );
    }

}