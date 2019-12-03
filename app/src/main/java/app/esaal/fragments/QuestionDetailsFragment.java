package app.esaal.fragments;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.RepliesAdapter;
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

public class QuestionDetailsFragment extends Fragment {
    public static FragmentActivity activity;
    public static QuestionDetailsFragment fragment;
    private SessionManager sessionManager;

    private Question question;
    private ArrayList<Reply> repliesList = new ArrayList<>();
    private RepliesAdapter repliesAdapter;
    private LinearLayoutManager layoutManager;
    private String imageUrl, videoUrl;
    private long millisecond;
    private String minutesStr;
    private String secondsStr;
    private long minutes;
    private long seconds;
    public static CountDownTimer countDownTimer;
    public static boolean isRunning = false;

    @BindView(R.id.fragment_question_details_cl_container)
    ConstraintLayout container;
    @BindView(R.id.fragment_question_details_cl_repliesContainer)
    ConstraintLayout repliesContainer;
    @BindView(R.id.fragment_question_details_v_reply)
    View reply;
    @BindView(R.id.fragment_question_details_iv_replyImg)
    ImageView replyImg;
    @BindView(R.id.fragment_question_details_tv_replyText)
    TextView replyText;
    @BindView(R.id.fragment_question_details_tv_cancel)
    TextView cancel;
    @BindView(R.id.fragment_question_details_iv_update)
    ImageView update;
    @BindView(R.id.fragment_question_details_tv_subjectName)
    TextView subjectName;
    @BindView(R.id.fragment_question_details_tv_questionText)
    TextView questionText;
    @BindView(R.id.fragment_question_details_iv_imgAttach)
    ImageView imgAttach;
    @BindView(R.id.fragment_question_details_iv_videoAttach)
    ImageView videoAttach;
    @BindView(R.id.fragment_question_details_iv_play)
    ImageView play;
    @BindView(R.id.fragment_question_details_iv_triangle)
    ImageView triangle;
    @BindView(R.id.fragment_question_details_rv_replies)
    RecyclerView replies;
    @BindView(R.id.loading)
    ProgressBar loading;

    public static QuestionDetailsFragment newInstance(FragmentActivity activity, int questionId) {
        fragment = new QuestionDetailsFragment();
        QuestionDetailsFragment.activity = activity;
        Bundle b = new Bundle();
        b.putInt("questionId", questionId);
        fragment.setArguments(b);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_question_details, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, true, false, false, "account", getString(R.string.questionsAndReplies));
        sessionManager = new SessionManager(activity);
        container.setVisibility(View.GONE);
        questionByIdApi(getArguments().getInt("questionId"));

        if (MainActivity.isEnglish) {
            Typeface enBold = Typeface.createFromAsset(activity.getAssets(), "montserrat_medium.ttf");
            subjectName.setTypeface(enBold);
        } else {
            Typeface arBold = Typeface.createFromAsset(activity.getAssets(), "cairo_bold.ttf");
            Typeface cairo = Typeface.createFromAsset(activity.getAssets(), "cairo_regular.ttf");
            subjectName.setTypeface(arBold);
            questionText.setTypeface(cairo);
        }
    }

    @OnClick(R.id.fragment_question_details_iv_play)
    public void playClick() {
        if (videoUrl == null || videoUrl.isEmpty()) {
            Snackbar.make(loading, getString(R.string.noVideo), Snackbar.LENGTH_SHORT).show();
        } else {
            Navigator.loadFragment(activity, VideoPlayerFragment.newInstance(activity, videoUrl), R.id.activity_main_fl_container, true);
        }
    }

    @OnClick(R.id.fragment_question_details_iv_imgAttach)
    public void imgAttachClick() {
        if (imageUrl == null || imageUrl.isEmpty()) {
            Snackbar.make(loading, getString(R.string.noImage), Snackbar.LENGTH_SHORT).show();
        } else {
            ArrayList<String> images = new ArrayList<>();
            images.add(imageUrl);
            Navigator.loadFragment(activity, ImageGestureFragment.newInstance(activity, images, 0), R.id.activity_main_fl_container, true);
        }
    }

    @OnClick(R.id.fragment_question_details_v_reply)
    public void replyClick() {
        if (sessionManager.isTeacher()) {
            if (replyText.getText().toString().equals(getString(R.string.Reservation))) {
                makePendingApi(question.id);
            } else {
                Navigator.loadFragment(activity, AddReplyFragment.newInstance(activity, question.id), R.id.activity_main_fl_container, true);
            }
        }
    }

    @OnClick(R.id.fragment_question_details_tv_cancel)
    public void cancelClick() {
        removePendingApi(question.id);
    }

    @OnClick(R.id.fragment_question_details_iv_update)
    public void updateClick() {
        Navigator.loadFragment(activity, AddQuestionFragment.newInstance(activity, "update", question), R.id.activity_main_fl_container, true);
    }

    private void setData() {
        if (sessionManager.isTeacher()) {
            update.setVisibility(View.GONE);
            if (question.replies == null || question.replies.size() == 0) {
                replyText.setText(getString(R.string.Reservation));
                replyImg.setVisibility(View.INVISIBLE);
            }

            millisecond = (long) question.remainTime;
            if (question.isPending) {
                if (question.pendingUserId == sessionManager.getUserId()) {
                    setRemainingTime();
                } else {
                    replyText.setVisibility(View.INVISIBLE);
                    cancel.setVisibility(View.VISIBLE);
                    cancel.setText(getString(R.string.canNotReply));
                    cancel.setTextColor(Color.parseColor("#FF0000"));
                }
            } else {
                cancel.setVisibility(View.GONE);
            }
        } else {
            reply.setVisibility(View.GONE);
            replyImg.setVisibility(View.GONE);
            replyText.setVisibility(View.GONE);
            cancel.setVisibility(View.GONE);
        }
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

        if (question.replies != null && question.replies.size() > 0) {
            update.setVisibility(View.GONE);
            repliesList.clear();
            repliesList.addAll(question.replies);
            layoutManager = new LinearLayoutManager(activity);
            repliesAdapter = new RepliesAdapter(activity, repliesList, new RepliesAdapter.OnItemClickListener() {
                @Override
                public void replyClick(int position) {
                    Navigator.loadFragment(activity, AddReplyFragment.newInstance(activity, question.id), R.id.activity_main_fl_container, true);
                }

                @Override
                public void likeClick(int position, ImageView likeImg, ImageView dislikeImg, ProgressBar loading) {
                    likeApi(repliesList.get(position).id, likeImg, dislikeImg, loading);
                }

                @Override
                public void dislikeClick(int position, ImageView likeImg, ImageView dislikeImg, ProgressBar loading) {
                    dislikeApi(repliesList.get(position).id, likeImg, dislikeImg, loading);
                }

                @Override
                public void imgAttach(int position, ArrayList<String> images) {
                    if (images == null || images.isEmpty()) {
                        Snackbar.make(loading, getString(R.string.noImage), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Navigator.loadFragment(activity, ImageGestureFragment.newInstance(activity, images, 0), R.id.activity_main_fl_container, true);
                    }
                }

                @Override
                public void videoAttach(int position, String videoUrl) {
                    if (videoUrl == null || videoUrl.isEmpty()) {
                        Snackbar.make(loading, getString(R.string.noVideo), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Navigator.loadFragment(activity, VideoPlayerFragment.newInstance(activity, videoUrl), R.id.activity_main_fl_container, true);
                    }
                }
            });
            replies.setLayoutManager(layoutManager);
            replies.setAdapter(repliesAdapter);
        } else {
            triangle.setVisibility(View.GONE);
            repliesContainer.setVisibility(View.GONE);
        }

    }

    private void loadImages(String url, ImageView image) {
        Glide.with(activity)
                .load(url)
                .apply(new RequestOptions().placeholder(R.mipmap.placeholder_attach)
                        .error(R.mipmap.placeholder_attach))
                .into(image);
    }

    private void questionByIdApi(final int questionId) {
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

    private void likeApi(int replyId, final ImageView likeImg, final ImageView dislikeImg, final ProgressBar loading) {
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().like(sessionManager.getUserToken(),
                replyId, new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        loading.setVisibility(View.GONE);
                        int status = response2.getStatus();
                        if (status == 200) {
                            if (likeImg.getDrawable().getConstantState() == getResources().getDrawable(R.mipmap.ic_like_sel).getConstantState()) {
                                likeImg.setImageResource(R.mipmap.ic_like_unsel);
                            } else if (likeImg.getDrawable().getConstantState() == getResources().getDrawable(R.mipmap.ic_like_unsel).getConstantState()) {
                                likeImg.setImageResource(R.mipmap.ic_like_sel);
                                dislikeImg.setImageResource(R.mipmap.ic_dislike_unsel);
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }

    private void dislikeApi(int replyId, final ImageView likeImg, final ImageView dislikeImg, final ProgressBar loading) {
        loading.setVisibility(View.VISIBLE);
        EsaalApiConfig.getCallingAPIInterface().dislike(
                sessionManager.getUserToken(),
                replyId,
                new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        loading.setVisibility(View.GONE);
                        int status = response2.getStatus();
                        if (status == 200) {
                            if (dislikeImg.getDrawable().getConstantState() == getResources().getDrawable(R.mipmap.ic_dislike_sel).getConstantState()) {
                                dislikeImg.setImageResource(R.mipmap.ic_dislike_unsel);
                            } else if (dislikeImg.getDrawable().getConstantState() == getResources().getDrawable(R.mipmap.ic_dislike_unsel).getConstantState()) {
                                dislikeImg.setImageResource(R.mipmap.ic_dislike_sel);
                                likeImg.setImageResource(R.mipmap.ic_like_unsel);
                            }
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }

    private void makePendingApi(int questionId) {
        EsaalApiConfig.getCallingAPIInterface().makePending(
                sessionManager.getUserToken(),
                questionId,
                sessionManager.getUserId(),
                new Callback<Question>() {
                    @Override
                    public void success(Question question, Response response) {
                        int status = response.getStatus();
                        if (status == 200) {
                            millisecond = (long) question.remainTime;
                            setRemainingTime();
                        } else if (status == 204) {
                            Snackbar.make(loading, getString(R.string.oneReservation), Snackbar.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                    }
                }
        );
    }

    private void removePendingApi(int questionId) {
        EsaalApiConfig.getCallingAPIInterface().removePending(
                sessionManager.getUserToken(),
                questionId,
                sessionManager.getUserId(),
                new Callback<Question>() {
                    @Override
                    public void success(Question question, Response response) {
                        int status = response.getStatus();
                        if (status == 200) {
                            deleteRemainingTime();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                }
        );
    }

    private void setRemainingTime() {
        cancel.setVisibility(View.VISIBLE);
        replyImg.setVisibility(View.VISIBLE);
        replyText.setText(getString(R.string.reply));
        minutes = TimeUnit.MILLISECONDS.toMinutes(millisecond);
        seconds = TimeUnit.MILLISECONDS.toSeconds(millisecond) -
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisecond));

        if (minutes < 10) {
            minutesStr = "0" + minutes;
        } else {
            minutesStr = minutes + "";
        }
        if (seconds < 10) {
            secondsStr = "0" + seconds;
        } else {
            secondsStr = seconds + "";
        }
        cancel.setText(minutesStr + ":" + secondsStr + "  " + getString(R.string.cancel));

        countDownTimer = new CountDownTimer(millisecond, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                isRunning = true;
                long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished));

                if (minutes < 10) {
                    minutesStr = "0" + minutes;
                } else {
                    minutesStr = minutes + "";
                }
                if (seconds < 10) {
                    secondsStr = "0" + seconds;
                } else {
                    secondsStr = seconds + "";
                }
                cancel.setText(minutesStr + ":" + secondsStr + "  " + getString(R.string.cancel));
            }

            @Override
            public void onFinish() {
                isRunning = false;
                removePendingApi(question.id);
            }
        }.start();
    }

    private void deleteRemainingTime() {
        countDownTimer.cancel();
        replyImg.setVisibility(View.INVISIBLE);
        replyText.setText(getString(R.string.Reservation));
        cancel.setVisibility(View.GONE);
    }
}
