package app.esaal.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.ArrayList;
import java.util.Calendar;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.classes.FixControl;
import app.esaal.classes.GlobalFunctions;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.questionsAndReplies.Attachment;
import app.esaal.webservices.responses.questionsAndReplies.Question;
import app.esaal.webservices.responses.questionsAndReplies.Reply;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

import static android.app.Activity.RESULT_OK;

public class AddReplyFragment extends Fragment {
    public static FragmentActivity activity;
    public static AddReplyFragment fragment;
    private SessionManager sessionManager;
    private Question question;
    private TypedFile imageTypedFile, videoTypedFile;
    private final int REQUEST_TAKE_GALLERY_VIDEO = 101;
    private final int REQUEST_TAKE_CAMERA_VIDEO = 102;
    private String imageUrl;
    private String videoUrl;
    private AlertDialog dialog;

    @BindView(R.id.fragment_add_reply_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_add_reply_tv_title)
    TextView title;
    @BindView(R.id.fragment_add_reply_iv_avatarImg)
    ImageView avatarImg;
    @BindView(R.id.fragment_add_reply_tv_subjectName)
    TextView subjectName;
    @BindView(R.id.fragment_add_reply_iv_questionImgAttach)
    ImageView questionImgAttach;
    @BindView(R.id.fragment_add_reply_iv_questionVideoAttach)
    ImageView questionVideoAttach;
    @BindView(R.id.fragment_add_reply_cl_videoContainer)
    ConstraintLayout videoContainer;
    @BindView(R.id.fragment_add_reply_iv_play)
    ImageView questionPlay;
    @BindView(R.id.fragment_add_reply_iv_deleteImgAttach)
    ImageView deleteImgAttach;
    @BindView(R.id.fragment_add_reply_iv_deleteVideoAttach)
    ImageView deleteVideoAttach;
    @BindView(R.id.fragment_add_reply_iv_replyPlay)
    ImageView replyPlay;
    @BindView(R.id.fragment_add_reply_tv_questionText)
    TextView questionText;
    @BindView(R.id.fragment_add_reply_et_replyText)
    TextView replyText;
    @BindView(R.id.fragment_add_reply_iv_replyImgAttach)
    ImageView replyImgAttach;
    @BindView(R.id.fragment_add_reply_iv_replyVideoAttach)
    ImageView replyVideoAttach;
    @BindView(R.id.fragment_add_reply_tv_addImgWord)
    TextView addImgWord;
    @BindView(R.id.fragment_add_reply_tv_addVideoWord)
    TextView addVideoWord;

    @BindView(R.id.loading)
    ProgressBar loading;


    public static AddReplyFragment newInstance(FragmentActivity activity, int questionId) {
        fragment = new AddReplyFragment();
        AddReplyFragment.activity = activity;
        Bundle b = new Bundle();
        b.putInt("questionId", questionId);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_add_reply, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (activity == null) {
            activity = getActivity();
        }
        MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.addReply));
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        GlobalFunctions.hasNewNotificationsApi(activity);

        container.setVisibility(View.GONE);
        replyImgAttach.setVisibility(View.GONE);
        replyVideoAttach.setVisibility(View.GONE);
        replyPlay.setVisibility(View.GONE);
        deleteVideoAttach.setVisibility(View.GONE);
        deleteImgAttach.setVisibility(View.GONE);

        if (!sessionManager.isTeacher()) {
            title.setText(getString(R.string.esaalStudent));
            title.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
            avatarImg.setImageResource(R.mipmap.ic_student);
        }

        if (question == null) {
            questionByIdApi(getArguments().getInt("questionId"));
        } else {
            loading.setVisibility(View.GONE);
            container.setVisibility(View.VISIBLE);
            setData();
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

    private void questionByIdApi(int questionId) {
        EsaalApiConfig.getCallingAPIInterface().questionById(
                sessionManager.getUserToken(),
                questionId,
                sessionManager.getUserId(),
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        container.setVisibility(View.VISIBLE);
                        int status = response.getStatus();
                        if (status == 200) {
                            fragment.question = questions.get(0);
                            setData();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_add_reply_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setData() {
        imageUrl = null;
        videoUrl = null;
        subjectName.setText(question.subject.getName());
        questionText.setText(question.description);
        for (Attachment value : question.attachments) {
            if (value.fileUrl != null && !value.fileUrl.isEmpty()) {
                if (value.fileType.equals("i")) {
                    loadImages(value.fileUrl, questionImgAttach);
                    imageUrl = value.fileUrl;
                } else if (value.fileType.equals("v")) {
                    videoUrl = value.fileUrl;
                    loadImages(value.videoFrameUrl,questionVideoAttach);
                }
            }
        }
        if (imageUrl == null || imageUrl.isEmpty()) {
            questionImgAttach.setVisibility(View.GONE);
        }
        if (videoUrl == null || videoUrl.isEmpty()) {
            videoContainer.setVisibility(View.GONE);
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

    @OnClick(R.id.fragment_add_reply_iv_captureImg)
    public void captureImgClick() {
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
    }


    @OnClick(R.id.fragment_add_reply_iv_captureVideo)
    public void captureVideoClick() {
            if (!GlobalFunctions.isWriteExternalStorageAllowed(activity)) {
                GlobalFunctions.requestWriteExternalStoragePermission(activity);
            } else if (!GlobalFunctions.isReadExternalStorageAllowed(activity)) {
                GlobalFunctions.requestReadExternalStoragePermission(activity);
            } else if (!GlobalFunctions.isCameraPermission(activity)) {
                GlobalFunctions.requestCameraPermission(activity);
            } else {
                createCaptureVideoMethodsDialog();
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
                    imageTypedFile = new TypedFile("image/*", new File(uri.getPath()));
                    loadImages(uri.getPath(),replyImgAttach);
                }

            }
        } else if (resultCode == RESULT_OK) {
            {
                Uri selectedVideoUri = data.getData();
                String videoPath = "";
                long videoDuration = checkVideoDurationValidation(selectedVideoUri);
                if (videoDuration > 120000) {
                    Snackbar.make(activity.findViewById(R.id.fragment_add_reply_cl_outerContainer), getString(R.string.videoDuration), Snackbar.LENGTH_SHORT).show();
                } else {
                    if (requestCode == REQUEST_TAKE_GALLERY_VIDEO) {
                        videoPath = getPath(selectedVideoUri);
                    } else if (requestCode == REQUEST_TAKE_CAMERA_VIDEO) {
                        videoPath = getRealPath(selectedVideoUri);
                    }
                    createCompressedVideoPath(videoPath);
                }
            }
        }
            if (imageTypedFile == null) {
                replyImgAttach.setVisibility(View.GONE);
                deleteImgAttach.setVisibility(View.GONE);
            } else {
                replyImgAttach.setVisibility(View.VISIBLE);
                deleteImgAttach.setVisibility(View.VISIBLE);
            }
        }

    private void createCompressedVideoPath(String videoPath) {
        String outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
        String outputPath = outputDir + File.separator + "VID_" + sessionManager.getUserId() + Calendar.getInstance().getTimeInMillis() + ".mp4";
        compressVideo(videoPath, outputPath);
    }

    private String getPath(Uri uri) {
        String[] projection = {MediaStore.Video.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
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

    private void compressVideo(String input, final String output){
        VideoCompress.VideoCompressTask task = VideoCompress.compressVideoLow(input,output, new VideoCompress.CompressListener() {
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
                Log.d("VIDEO-TAG",output);
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

    private void setVideo(String selectedVideoPath) {
        if (selectedVideoPath != null) {
            videoTypedFile = new TypedFile("video/*", new File(selectedVideoPath));

            if (videoTypedFile == null) {
                replyVideoAttach.setVisibility(View.GONE);
                replyPlay.setVisibility(View.GONE);
                deleteVideoAttach.setVisibility(View.GONE);
            } else {
                replyVideoAttach.setVisibility(View.VISIBLE);
                replyPlay.setVisibility(View.VISIBLE);
                deleteVideoAttach.setVisibility(View.VISIBLE);
            }

            try {

                int Width = FixControl.getImageWidth(activity, R.mipmap.placeholder_attach);
                int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_attach);
                replyVideoAttach.getLayoutParams().height = Height;
                replyVideoAttach.getLayoutParams().width = Width;
                replyPlay.getLayoutParams().height = Height;
                replyPlay.getLayoutParams().width = Width;

                replyVideoAttach.setImageBitmap(GlobalFunctions.loadVideoFrameFromPath(selectedVideoPath));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    @OnClick(R.id.fragment_add_reply_iv_play)
    public void playClick() {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_add_reply_cl_outerContainer), getString(R.string.noVideo), Snackbar.LENGTH_SHORT).show();
        } else {
            Navigator.loadFragment(activity, UrlsFragment.newInstance(activity, videoUrl, "video"), R.id.activity_main_fl_container, true);
        }
    }

    @OnClick(R.id.fragment_add_reply_iv_questionImgAttach)
    public void imgAttachClick() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_add_reply_cl_outerContainer), getString(R.string.noImage), Snackbar.LENGTH_SHORT).show();
        } else {
            ArrayList<String> images = new ArrayList<>();
            images.add(imageUrl);
            Navigator.loadFragment(activity, ImageGestureFragment.newInstance(activity, images, 0), R.id.activity_main_fl_container, true);
        }
    }


    @OnClick(R.id.fragment_add_reply_iv_deleteImgAttach)
    public void deleteImgAttach() {
        replyImgAttach.setImageResource(R.mipmap.placeholder_attach);
        imageTypedFile = null;
        deleteImgAttach.setVisibility(View.GONE);
        replyImgAttach.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_add_reply_iv_deleteVideoAttach)
    public void deleteVideoAttach() {
        replyVideoAttach.setImageResource(R.mipmap.placeholder_attach);
        videoTypedFile = null;
        deleteVideoAttach.setVisibility(View.GONE);
        replyVideoAttach.setVisibility(View.GONE);
        replyPlay.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_add_reply_tv_send)
    public void sendClick() {
        String replyTextStr = replyText.getText().toString();
        if (replyTextStr == null || replyTextStr.isEmpty()) {
            Snackbar.make(activity.findViewById(R.id.fragment_add_reply_cl_outerContainer), getString(R.string.enterReply), Snackbar.LENGTH_SHORT).show();
        } else {

            addReplyApi(replyTextStr);
        }
    }

    private void addReplyApi(String replyMessage) {
        loading.setVisibility(View.VISIBLE);
        GlobalFunctions.DisableLayout(container);
        EsaalApiConfig.getCallingAPIInterface().addReply(
                sessionManager.getUserToken(),
                question.id,
                sessionManager.getUserId(),
                replyMessage,
                imageTypedFile, videoTypedFile, new Callback<ArrayList<Reply>>() {
                    @Override
                    public void success(ArrayList<Reply> reply, Response response) {
                        loading.setVisibility(View.GONE);
                        GlobalFunctions.EnableLayout(container);
                        int status = response.getStatus();
                        if (status == 200) {
                            Snackbar.make(activity.findViewById(R.id.fragment_add_reply_cl_outerContainer), getString(R.string.replyAdded), Snackbar.LENGTH_SHORT).show();
                            //fix it
                            getFragmentManager().popBackStack();
                            //activity.onBackPressed();
                            //GlobalFunctions.clearStack(activity);
                            //Navigator.loadFragment(activity, QuestionDetailsFragment.newInstance(activity, question.id), R.id.activity_main_fl_container, false);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        Snackbar.make(activity.findViewById(R.id.fragment_add_reply_cl_outerContainer),getString(R.string.generalError), Snackbar.LENGTH_SHORT).show();
                        GlobalFunctions.EnableLayout(container);
                    }
                }
        );
    }
}
