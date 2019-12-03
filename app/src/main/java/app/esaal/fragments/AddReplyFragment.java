package app.esaal.fragments;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import app.esaal.adapters.RepliesAdapter;
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
    private String imageUrl;
    private String videoUrl;

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
    @BindView(R.id.fragment_add_reply_iv_play)
    ImageView questionPlay;
    @BindView(R.id.fragment_add_reply_iv_deleteImgAttach)
    ImageView deleteImgAttach;
    @BindView(R.id.fragment_add_reply_iv_deleteVideoAttach)
    ImageView deleteVideoAttach;
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
        MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.addReply));
        FixControl.setupUI(container, activity);
        sessionManager = new SessionManager(activity);
        container.setVisibility(View.GONE);
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
                        GlobalFunctions.generalErrorMessage(loading, activity);
                    }
                }
        );
    }

    private void setData() {
        subjectName.setText(question.subject.getName());
        questionText.setText(question.description);
        for (Attachment value : question.attachments) {
            if (value.fileUrl != null && !value.fileUrl.isEmpty()) {
                if (value.fileType.equals("i")) {
                    loadImages(value.fileUrl, questionImgAttach);
                    imageUrl = value.fileUrl;
                } else if (value.fileType.equals("v")) {
                    loadImages(value.fileUrl, questionVideoAttach);
                    videoUrl = value.fileUrl;
                }
            }
        }
    }

    private void loadImages(String url, ImageView image) {
        Glide.with(activity)
                .load(url)
                .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                        .error(R.mipmap.placeholder_attach))
                .into(image);
    }

    @OnClick(R.id.fragment_add_reply_iv_captureImg)
    public void captureImgClick() {
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


    @OnClick(R.id.fragment_add_reply_iv_captureVideo)
    public void captureVideoClick() {
        final Intent intent1 = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent1, "Select Video"), REQUEST_TAKE_GALLERY_VIDEO);

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
                            .into(replyImgAttach);
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

                Bitmap bmThumbnail;
                bmThumbnail = ThumbnailUtils.createVideoThumbnail(selectedVideoPath, MediaStore.Images.Thumbnails.MICRO_KIND);
                replyVideoAttach.setImageBitmap(bmThumbnail);

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

    @OnClick(R.id.fragment_add_reply_iv_play)
    public void playClick() {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Snackbar.make(loading,getString(R.string.noVideo),Snackbar.LENGTH_SHORT).show();
        } else {
            Navigator.loadFragment(activity, VideoPlayerFragment.newInstance(activity, videoUrl), R.id.activity_main_fl_container, true);
        }
    }

    @OnClick(R.id.fragment_add_reply_iv_questionImgAttach)
    public void imgAttachClick() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Snackbar.make(loading,getString(R.string.noImage),Snackbar.LENGTH_SHORT).show();
        } else {
            ArrayList<String> images = new ArrayList<>();
            images.add(imageUrl);
            Navigator.loadFragment(activity, ImageGestureFragment.newInstance(activity, images,0), R.id.activity_main_fl_container, true);
        }
    }


    @OnClick(R.id.fragment_add_reply_iv_deleteImgAttach)
    public void deleteImgAttach() {
        replyImgAttach.setImageResource(R.mipmap.placeholder_attach);
        imageTypedFile = null;
        deleteImgAttach.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_add_reply_iv_deleteVideoAttach)
    public void deleteVideoAttach() {
        replyVideoAttach.setImageResource(R.mipmap.placeholder_attach);
        videoTypedFile = null;
        deleteVideoAttach.setVisibility(View.GONE);
    }

    @OnClick(R.id.fragment_add_reply_tv_send)
    public void sendClick() {
        String replyTextStr = replyText.getText().toString();
        if (replyTextStr == null || replyTextStr.isEmpty()) {
            Snackbar.make(loading, getString(R.string.enterReply), Snackbar.LENGTH_SHORT).show();
        } else {

            addReplyApi(replyTextStr);
        }
    }

    private void addReplyApi(String replyMessage) {
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().addReply(
                sessionManager.getUserToken(),
                question.id,
                sessionManager.getUserId(),
                replyMessage,
                imageTypedFile, videoTypedFile, new Callback<ArrayList<Reply>>() {
                    @Override
                    public void success(ArrayList<Reply> reply, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            Snackbar.make(loading, getString(R.string.replyAdded), Snackbar.LENGTH_SHORT).show();
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
}
