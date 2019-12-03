package app.esaal.fragments;

import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.duolingo.open.rtlviewpager.RtlViewPager;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import app.esaal.MainActivity;
import app.esaal.R;
import app.esaal.adapters.QuestionsAdapter;
import app.esaal.adapters.SliderAdapter;
import app.esaal.classes.FixControl;
import app.esaal.classes.Navigator;
import app.esaal.classes.SessionManager;
import app.esaal.webservices.EsaalApiConfig;
import app.esaal.webservices.responses.notifications.Notification;
import app.esaal.webservices.responses.questionsAndReplies.Question;
import app.esaal.webservices.responses.slider.Slider;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class HomeFragment extends Fragment {
    public static FragmentActivity activity;
    public static HomeFragment fragment;
    private SessionManager sessionManager;
    private ArrayList<Slider> sliderList = new ArrayList<>();
    private SliderAdapter sliderAdapter;
    private int currentPage = 0;
    private int NUM_PAGES = 0;

    private ArrayList<Question> questionsList = new ArrayList<>();
    private QuestionsAdapter questionsAdapter;
    LinearLayoutManager layoutManager;

    @BindView(R.id.fragment_home_iv_sliderPlaceholder)
    ImageView sliderPlaceholder;
    @BindView(R.id.fragment_home_vp_slider)
    RtlViewPager slider;
    @BindView(R.id.home_circleIndicator_sliderCircle)
    CircleIndicator sliderCircles;
    @BindView(R.id.fragment_home_tv_latestQuestionsWord)
    TextView latestQuestionsWord;
    @BindView(R.id.fragment_home_rv_latestQuestions)
    RecyclerView latestQuestions;
    @BindView(R.id.fragment_home_iv_questionMark)
    ImageView questionMark;
    @BindView(R.id.fragment_home_tv_introText)
    TextView introText;
    @BindView(R.id.fragment_home_tv_addQuestion)
    TextView addQuestion;

    @BindView(R.id.loading)
    ProgressBar loading;

    public static HomeFragment newInstance(FragmentActivity activity) {
        fragment = new HomeFragment();
        HomeFragment.activity = activity;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View childView = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this, childView);
        return childView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.setupAppbar(true, true, true, false, "home", getString(R.string.ask));
        sessionManager = new SessionManager(activity);
        if (sessionManager.isTeacher()) {
            MainActivity.addQuestion.setVisibility(View.GONE);
            addQuestion.setVisibility(View.GONE);
        }
        int Height = FixControl.getImageHeight(activity, R.mipmap.placeholder_slider);
        sliderPlaceholder.getLayoutParams().height = Height;

        if (sliderList.size() <= 0) {
            sliderApi();
        } else {
            setupSlider();
        }

        slider.setCurrentItem(0, true);
        final Handler handler = new Handler();
        final Runnable update = new Runnable() {
            @Override
            public void run() {
                if (currentPage == NUM_PAGES) {
                    currentPage = 0;
                }
                slider.setCurrentItem(currentPage++, true);
            }
        };


        Timer swipeTimer = new Timer();
        swipeTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(update);
            }
        }, 5000, 5000);


        layoutManager = new LinearLayoutManager(activity);
        questionsAdapter = new QuestionsAdapter(activity, questionsList);
        latestQuestions.setLayoutManager(layoutManager);
        latestQuestions.setAdapter(questionsAdapter);

        questionsApi();


        if (MainActivity.isEnglish) {
            Typeface enBold = Typeface.createFromAsset(activity.getAssets(), "montserrat_medium.ttf");
            latestQuestionsWord.setTypeface(enBold);
            latestQuestionsWord.setTypeface(enBold);
        } else {
            Typeface arBold = Typeface.createFromAsset(activity.getAssets(), "cairo_bold.ttf");
            introText.setTypeface(arBold);
            introText.setTypeface(arBold);
        }
    }

    @OnClick(R.id.fragment_home_tv_addQuestion)
    public void addQuestionClick() {
        Navigator.loadFragment(activity, AddQuestionFragment.newInstance(activity, "add", null), R.id.activity_main_fl_container, true);
    }

    private void setupViews(boolean hasQuestions) {
        if (hasQuestions) {
            latestQuestionsWord.setVisibility(View.VISIBLE);
            latestQuestions.setVisibility(View.VISIBLE);

            questionMark.setVisibility(View.GONE);
            introText.setVisibility(View.GONE);
            addQuestion.setVisibility(View.GONE);
        } else {
            latestQuestionsWord.setVisibility(View.GONE);
            latestQuestions.setVisibility(View.GONE);

            questionMark.setVisibility(View.VISIBLE);
            if (!sessionManager.isTeacher()) {
                introText.setVisibility(View.VISIBLE);
                addQuestion.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setupSlider() {
        NUM_PAGES = sliderList.size();
        sliderAdapter = new SliderAdapter(activity, sliderList);
        slider.setAdapter(sliderAdapter);
        sliderCircles.setViewPager(slider);
        sliderCircles.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float v, int i1) {
                currentPage = position;
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

    }

    private void sliderApi() {
        EsaalApiConfig.getCallingAPIInterface().slider(
                sessionManager.getUserToken(),
                new Callback<ArrayList<Slider>>() {
                    @Override
                    public void success(ArrayList<Slider> sliders, Response response) {
                        int status = response.getStatus();
                        if (status == 200) {
                            sliderList.addAll(sliders);
                            setupSlider();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });
    }

    private void questionsApi() {
        EsaalApiConfig.getCallingAPIInterface().questions(
                sessionManager.getUserToken(),
                sessionManager.getUserId(),
                0,
                new Callback<ArrayList<Question>>() {
                    @Override
                    public void success(ArrayList<Question> questions, Response response) {
                        loading.setVisibility(View.GONE);
                        int status = response.getStatus();
                        if (status == 200) {
                            latestQuestionsWord.setVisibility(View.VISIBLE);
                            if (questions != null && questions.size() > 0) {
                                questionsList.clear();
                                for (Question value : questions) {
                                    if (value.isPending && (sessionManager.getUserId() == value.pendingUserId)) {
                                        questionsList.add(value);
                                    }
                                }
                                for (Question value : questions) {
                                    if (!value.isPending) {
                                        questionsList.add(value);
                                    }
                                }
                            }
                            questionsAdapter.notifyDataSetChanged();
                            setupViews(true);
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        loading.setVisibility(View.GONE);
                        int failureStatus = error.getResponse().getStatus();
                        if (failureStatus == 201) {
                            setupViews(false);
                        }
                    }
                }
        );
    }

}

